package emmy.eminentmagic.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import emmy.eminentmagic.init.EminentMagicModAttributes;
import emmy.eminentmagic.init.EminentMagicModSpellItems;
import emmy.eminentmagic.network.EminentMagicModVariables;
import emmy.eminentmagic.procedures.ConsumeManaProcedure;
import emmy.eminentmagic.spell.SpellCatalog;
import emmy.eminentmagic.spell.SpellCastingService;
import emmy.eminentmagic.spell.SpellDefinition;
import emmy.eminentmagic.spell.SpellTrigger;
import emmy.eminentmagic.spell.SpellTriggerDispatcher;

public final class SpellStaffItem extends Item {
	private static final String CHANNEL_FAILED_TAG = "eminent_magic_channel_failed";
	private static final int CAST_COOLDOWN_TICKS = 10;
	private static final int MAX_CHANNEL_TICKS = 72000;

	public SpellStaffItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		SpellDefinition spell = SpellItem.getSpellId(stack).flatMap(SpellCatalog::get).orElse(null);
		if (spell == null) {
			return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.FAIL;
		}
		if (spell.channelled() ? manaControl(player) <= 0 : channelDurationTicks(spell, player) == 0) {
			if (!level.isClientSide()) {
				player.displayClientMessage(Component.translatable("message.eminent_magic.mana_control_required"), true);
			}
			return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.FAIL;
		}
		if (!level.isClientSide()) {
			CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(CHANNEL_FAILED_TAG));
		}
		player.startUsingItem(hand);
		return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		SpellDefinition spell = SpellItem.getSpellId(stack).flatMap(SpellCatalog::get).orElse(null);
		if (spell == null || !(entity instanceof Player player)) {
			return 0;
		}
		return spell.channelled() ? MAX_CHANNEL_TICKS : channelDurationTicks(spell, player);
	}

	@Override
	public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
		if (level.isClientSide() || !(entity instanceof Player player)) {
			return;
		}
		SpellDefinition spell = SpellItem.getSpellId(stack).flatMap(SpellCatalog::get).orElse(null);
		if (spell == null) {
			CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(CHANNEL_FAILED_TAG, true));
			player.stopUsingItem();
			return;
		}
		int duration = spell.channelled() ? MAX_CHANNEL_TICKS : channelDurationTicks(spell, player);
		double manaCost = manaCost(spell, player);
		double manaPerTick = spell.channelled() ? manaControl(player) / 20 : manaCost / duration;
		if (duration <= 0 || !Double.isFinite(manaPerTick) || manaPerTick < 0
				|| (spell.channelled() && manaPerTick == 0) || !ConsumeManaProcedure.execute(level, player, manaPerTick)) {
			CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(CHANNEL_FAILED_TAG, true));
			player.stopUsingItem();
			return;
		}
		if (spell.channelled()) {
			SpellTriggerDispatcher.fire(player, stack, SpellTrigger.WHILE_CHANNELLING);
		} else {
			SpellTriggerDispatcher.fire(player, stack, SpellTrigger.WHILE_CHARGING);
		}
	}

	@Override
	public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(CHANNEL_FAILED_TAG));
		return false;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (!level.isClientSide() && entity instanceof Player player) {
			boolean channelFailed = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBooleanOr(CHANNEL_FAILED_TAG, false);
			SpellDefinition spell = SpellItem.getSpellId(stack).flatMap(SpellCatalog::get).orElse(null);
			CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
				tag.remove(CHANNEL_FAILED_TAG);
			});
			if (!channelFailed && spell != null && !spell.channelled()
					&& SpellTriggerDispatcher.fire(player, stack, SpellTrigger.ON_CAST)) {
				player.getCooldowns().addCooldown(stack, CAST_COOLDOWN_TICKS);
			}
		}
		return stack;
	}

	public static ItemStack createStack(SpellDefinition spell) {
		ItemStack stack = new ItemStack(EminentMagicModSpellItems.SPELL_STAFF.get());
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(SpellItem.SPELL_ID_TAG, spell.id().toString()));
		stack.set(DataComponents.CUSTOM_NAME, Component.literal(spell.displayName() + " Staff"));
		return stack;
	}

	private static int channelDurationTicks(SpellDefinition spell, Player player) {
		double manaControl = manaControl(player);
		if (!Double.isFinite(manaControl) || manaControl <= 0) {
			return 0;
		}
		double ticks = manaCost(spell, player) / manaControl * 20;
		if (!Double.isFinite(ticks)) {
			return 0;
		}
		return (int) Math.max(1, Math.min(MAX_CHANNEL_TICKS, Math.ceil(ticks)));
	}

	private static double manaControl(Player player) {
		return player.getAttributeValue(EminentMagicModAttributes.MANA_CONTROL);
	}

	private static double manaCost(SpellDefinition spell, Player player) {
		var variables = player.getData(EminentMagicModVariables.PLAYER_VARIABLES);
		return Math.max(0, SpellCastingService.calculateManaCost(spell, variables.spellControl, variables.spellStrength));
	}
}