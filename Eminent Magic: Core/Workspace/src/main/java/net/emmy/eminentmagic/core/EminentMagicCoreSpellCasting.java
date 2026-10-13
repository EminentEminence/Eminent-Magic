package net.emmy.eminentmagic.core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@EventBusSubscriber(modid = EminentMagicCoreMod.MODID)
public final class EminentMagicCoreSpellCasting {
	private static final Map<ResourceLocation, SpellHooks> SPELL_HOOKS = new ConcurrentHashMap<>();
	private static final Map<UUID, CastState> CASTS = new ConcurrentHashMap<>();

	private EminentMagicCoreSpellCasting() {
	}

	public static void registerSpellHooks(ResourceLocation spellId, SpellHooks hooks) {
		if (spellId == null || hooks == null) {
			throw new IllegalArgumentException("Spell ID and hooks are required");
		}
		SPELL_HOOKS.put(spellId, hooks);
	}

	public static boolean canPrepareCast(ServerPlayer player, EminentMagicCoreStaffItem.SelectedSpell selectedSpell) {
		if (player == null || player.level().isClientSide() || selectedSpell == null || CASTS.containsKey(player.getUUID())) {
			return false;
		}
		return SPELL_HOOKS.getOrDefault(selectedSpell.definition().id(), SpellHooks.EMPTY).onCast() != null
				&& isCurrentSelection(player, selectedSpell);
	}

	public static CastResult prepareCast(ServerPlayer player, EminentMagicCoreStaffItem.SelectedSpell selectedSpell,
			Vec3 target, double totalCost, double spellControl, boolean allowReserves) {
		if (!canPrepareCast(player, selectedSpell) || target == null) {
			return CastResult.INVALID_CONTEXT;
		}
		if (!Double.isFinite(totalCost) || totalCost < 0) {
			return CastResult.INVALID_COST;
		}
		if (totalCost > 0 && (!Double.isFinite(spellControl) || spellControl <= 0)) {
			return CastResult.INVALID_CONTROL;
		}

		int channelTicks = 0;
		if (totalCost > 0) {
			double requestedTicks = 16.0 * totalCost / spellControl;
			if (!Double.isFinite(requestedTicks) || requestedTicks > Integer.MAX_VALUE) {
				return CastResult.INVALID_CONTROL;
			}
			channelTicks = Math.max(1, (int) Math.ceil(requestedTicks));
		}

		InteractionHand hand = player.getOffhandItem() == selectedSpell.staffStack()
				? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
		CastState state = new CastState(player, selectedSpell, target, totalCost, allowReserves, hand, channelTicks);
		return CASTS.putIfAbsent(player.getUUID(), state) == null ? CastResult.READY_FOR_UPFRONT_PAYMENT : CastResult.ALREADY_CASTING;
	}

	public static CastResult startCast(ServerPlayer player, EminentMagicCoreStaffItem.SelectedSpell selectedSpell,
			Vec3 target, double totalCost, double spellControl, boolean allowReserves) {
		CastResult preparation = prepareCast(player, selectedSpell, target, totalCost, spellControl, allowReserves);
		if (preparation != CastResult.READY_FOR_UPFRONT_PAYMENT) {
			return preparation;
		}
		double upfrontPayment = getUpfrontPayment(player);
		boolean paid = EminentMagicCoreManaPayment.consumeMana(player, upfrontPayment, allowReserves)
				== EminentMagicCoreManaPayment.ManaPaymentResult.PAID;
		if (!paid) {
			showInsufficientMana(player);
		}
		return resolveUpfrontPayment(player, upfrontPayment, paid);
	}

	public static double getUpfrontPayment(ServerPlayer player) {
		CastState state = getPendingState(player, CastPhase.UPFRONT_PAYMENT);
		return state == null ? 0 : state.totalCost * 0.2;
	}

	public static CastResult resolveUpfrontPayment(ServerPlayer player, double paidAmount, boolean paid) {
		CastState state = getPendingState(player, CastPhase.UPFRONT_PAYMENT);
		if (state == null) {
			return CastResult.INVALID_CONTEXT;
		}
		if (!sameAmount(paidAmount, state.totalCost * 0.2)) {
			CASTS.remove(player.getUUID(), state);
			return CastResult.INVALID_COST;
		}
		if (!paid) {
			CASTS.remove(player.getUUID(), state);
			return CastResult.INSUFFICIENT_MANA;
		}
		if (state.channelTicks == 0) {
			return completeCast(state);
		}
		state.phase = CastPhase.CHANNELING;
		return CastResult.CHANNELING;
	}

	public static double getNextChannelPayment(ServerPlayer player) {
		CastState state = getPendingState(player, CastPhase.CHANNELING);
		if (state == null || state.channelTicksRemaining <= 0) {
			return 0;
		}
		return state.remainingChannelCost / state.channelTicksRemaining;
	}

	public static CastResult resolveChannelPayment(ServerPlayer player, double paidAmount, boolean paid) {
		CastState state = getPendingState(player, CastPhase.CHANNELING);
		if (state == null) {
			return CastResult.INVALID_CONTEXT;
		}
		double expectedPayment = state.remainingChannelCost / state.channelTicksRemaining;
		if (!sameAmount(paidAmount, expectedPayment)) {
			CASTS.remove(player.getUUID(), state);
			return CastResult.INVALID_COST;
		}
		if (!paid) {
			CASTS.remove(player.getUUID(), state);
			return CastResult.INSUFFICIENT_MANA;
		}

		state.channelTick++;
		state.remainingChannelCost -= expectedPayment;
		state.channelTicksRemaining--;
		if (!dispatch(state, Trigger.WHILE_CHANNELING)) {
			CASTS.remove(player.getUUID(), state);
			return CastResult.HANDLER_FAILED;
		}
		if (state.channelTicksRemaining == 0) {
			state.remainingChannelCost = 0;
			return completeCast(state);
		}
		return CastResult.CHANNELING;
	}

	public static boolean cancel(ServerPlayer player) {
		return player != null && CASTS.remove(player.getUUID()) != null;
	}

	public static Optional<CastSnapshot> getCastSnapshot(ServerPlayer player) {
		CastState state = player == null ? null : CASTS.get(player.getUUID());
		return state == null ? Optional.empty() : Optional.of(new CastSnapshot(state.spell.definition().id(), state.phase.name(),
				state.totalCost, state.remainingChannelCost, state.channelTick, state.activeTick, state.allowReserves));
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event) {
		for (Map.Entry<UUID, CastState> entry : CASTS.entrySet()) {
			CastState state = entry.getValue();
			ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
			if (player == null || !player.isAlive()) {
				CASTS.remove(entry.getKey(), state);
				continue;
			}
			if (state.phase == CastPhase.CHANNELING) {
				if (!isStillUsingStaff(player, state)) {
					CASTS.remove(entry.getKey(), state);
				} else {
					payChannelTick(player);
				}
			} else if (state.phase == CastPhase.ACTIVE) {
				if (!dispatch(state, Trigger.WHILE_CASTING)) {
					CASTS.remove(entry.getKey(), state);
					continue;
				}
				state.activeTick++;
				if (state.activeTick >= state.spell.definition().duration()) {
					CASTS.remove(entry.getKey(), state);
				}
			}
		}
	}

	private static CastResult payChannelTick(ServerPlayer player) {
		double payment = getNextChannelPayment(player);
		if (!Double.isFinite(payment) || payment <= 0) {
			cancel(player);
			return CastResult.INVALID_COST;
		}
		CastSnapshot snapshot = getCastSnapshot(player).orElse(null);
		if (snapshot == null) {
			return CastResult.INVALID_CONTEXT;
		}
		boolean paid = EminentMagicCoreManaPayment.consumeMana(player, payment, snapshot.allowReserves())
				== EminentMagicCoreManaPayment.ManaPaymentResult.PAID;
		if (!paid) {
			showInsufficientMana(player);
		}
		return resolveChannelPayment(player, payment, paid);
	}

	private static void showInsufficientMana(ServerPlayer player) {
		player.displayClientMessage(Component.translatable("message.eminent_magic.out_of_mana"), true);
	}

	@SubscribeEvent
	public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		CASTS.remove(event.getEntity().getUUID());
	}

	@SubscribeEvent
	public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		CASTS.remove(event.getEntity().getUUID());
	}

	private static CastResult completeCast(CastState state) {
		if (!dispatch(state, Trigger.ON_CAST)) {
			CASTS.remove(state.player.getUUID(), state);
			return CastResult.HANDLER_FAILED;
		}
		if (state.spell.definition().duration() > 0 && SPELL_HOOKS.get(state.spell.definition().id()).whileCasting() != null) {
			state.phase = CastPhase.ACTIVE;
			return CastResult.CAST;
		}
		CASTS.remove(state.player.getUUID(), state);
		return CastResult.CAST;
	}

	private static boolean dispatch(CastState state, Trigger trigger) {
		SpellHooks hooks = SPELL_HOOKS.get(state.spell.definition().id());
		if (hooks == null) {
			return trigger != Trigger.ON_CAST;
		}
		Consumer<SpellCastContext> handler = switch (trigger) {
			case ON_CAST -> hooks.onCast();
			case WHILE_CASTING -> hooks.whileCasting();
			case WHILE_CHANNELING -> hooks.whileChanneling();
		};
		if (handler == null) {
			return trigger != Trigger.ON_CAST;
		}
		try {
			handler.accept(state.context());
			return true;
		} catch (RuntimeException exception) {
			EminentMagicCoreMod.LOGGER.error("Spell trigger {} failed for {}.", trigger, state.spell.definition().id(), exception);
			return false;
		}
	}

	private static boolean isCurrentSelection(ServerPlayer player, EminentMagicCoreStaffItem.SelectedSpell selectedSpell) {
		return EminentMagicCoreStaffItem.getSelectedSpell(player)
				.filter(current -> current.staffStack() == selectedSpell.staffStack()
					&& current.slot() == selectedSpell.slot()
					&& current.definition().id().equals(selectedSpell.definition().id()))
				.isPresent();
	}

	private static boolean isStillUsingStaff(ServerPlayer player, CastState state) {
		return player.isUsingItem() && player.getUsedItemHand() == state.hand
				&& player.getUseItem().getItem() == state.spell.staffStack().getItem()
				&& isCurrentSelection(player, state.spell);
	}

	private static CastState getPendingState(ServerPlayer player, CastPhase phase) {
		if (player == null || player.level().isClientSide()) {
			return null;
		}
		CastState state = CASTS.get(player.getUUID());
		return state != null && state.phase == phase ? state : null;
	}

	private static boolean sameAmount(double actual, double expected) {
		return Double.isFinite(actual) && Math.abs(actual - expected) <= Math.ulp(Math.max(Math.abs(actual), Math.abs(expected))) * 2;
	}

	public enum CastResult {
		READY_FOR_UPFRONT_PAYMENT,
		INSUFFICIENT_MANA,
		INVALID_CONTEXT,
		INVALID_COST,
		INVALID_CONTROL,
		ALREADY_CASTING,
		CHANNELING,
		CAST,
		HANDLER_FAILED
	}

	public enum Trigger {
		ON_CAST,
		WHILE_CHANNELING,
		WHILE_CASTING
	}

	private enum CastPhase {
		UPFRONT_PAYMENT,
		CHANNELING,
		ACTIVE
	}

	public record SpellHooks(Consumer<SpellCastContext> onCast, Consumer<SpellCastContext> whileChanneling,
			Consumer<SpellCastContext> whileCasting) {
		public static final SpellHooks EMPTY = new SpellHooks(null, null, null);
	}

	public record SpellCastContext(ServerPlayer caster, ItemStack staff, int selectedSlot,
			EminentMagicCoreSpellRegistry.SpellDefinition spell, Vec3 target, double totalCost,
			int channelTick, int activeTick) {
	}

	public record CastSnapshot(ResourceLocation spellId, String phase, double totalCost, double remainingChannelCost,
			int channelTick, int activeTick, boolean allowReserves) {
	}

	private static final class CastState {
		private final ServerPlayer player;
		private final EminentMagicCoreStaffItem.SelectedSpell spell;
		private final Vec3 target;
		private final double totalCost;
		private final boolean allowReserves;
		private final InteractionHand hand;
		private final int channelTicks;
		private int channelTicksRemaining;
		private double remainingChannelCost;
		private int channelTick;
		private int activeTick;
		private CastPhase phase = CastPhase.UPFRONT_PAYMENT;

		private CastState(ServerPlayer player, EminentMagicCoreStaffItem.SelectedSpell spell, Vec3 target,
				double totalCost, boolean allowReserves, InteractionHand hand, int channelTicks) {
			this.player = player;
			this.spell = spell;
			this.target = target;
			this.totalCost = totalCost;
			this.allowReserves = allowReserves;
			this.hand = hand;
			this.channelTicks = channelTicks;
			this.channelTicksRemaining = channelTicks;
			this.remainingChannelCost = totalCost * 0.8;
		}

		private SpellCastContext context() {
			return new SpellCastContext(player, spell.staffStack(), spell.slot(), spell.definition(), target,
					totalCost, channelTick, activeTick);
		}
	}
}
