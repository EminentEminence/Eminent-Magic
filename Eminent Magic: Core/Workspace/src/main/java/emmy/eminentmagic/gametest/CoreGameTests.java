package emmy.eminentmagic.gametest;

import java.util.function.Consumer;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import emmy.eminentmagic.EminentMagicMod;
import emmy.eminentmagic.barrier.Barrier;
import emmy.eminentmagic.barrier.BarrierGeometry;
import emmy.eminentmagic.barrier.BarrierService;
import emmy.eminentmagic.init.BarrierGameRules;
import emmy.eminentmagic.init.EminentMagicModAttributes;
import emmy.eminentmagic.item.SpellItem;
import emmy.eminentmagic.item.SpellStaffItem;
import emmy.eminentmagic.mana.ManaSpendingService;
import emmy.eminentmagic.spell.SpellCatalog;
import emmy.eminentmagic.spell.SpellCatalogGameTests;
import emmy.eminentmagic.spell.SpellCastingService;
import emmy.eminentmagic.spell.SpellControlService;
import emmy.eminentmagic.spell.SpellDefinition;
import emmy.eminentmagic.staff.StaffService;

public final class CoreGameTests {
	public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
			DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, EminentMagicMod.MODID);

	public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPELL_CATALOG =
			TEST_FUNCTIONS.register("spell_catalog", () -> CoreGameTests::spellCatalog);
	public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPELL_CONFIG_MERGE =
			TEST_FUNCTIONS.register("spell_config_merge", () -> SpellCatalogGameTests::configOverrides);
	public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPELL_COST =
			TEST_FUNCTIONS.register("spell_cost", () -> CoreGameTests::spellCost);
	public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPELL_ITEM_ROUND_TRIP =
			TEST_FUNCTIONS.register("spell_item_round_trip", () -> CoreGameTests::spellItemRoundTrip);
	public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MANA_ATTRIBUTES =
			TEST_FUNCTIONS.register("mana_attributes", () -> CoreGameTests::manaAttributes);
	public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MANA_SPENDING =
			TEST_FUNCTIONS.register("mana_spending", () -> CoreGameTests::manaSpending);
	public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MANA_REJECTIONS =
			TEST_FUNCTIONS.register("mana_rejections", () -> CoreGameTests::manaRejections);
	public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STAFF_SLOT_OPERATIONS =
			TEST_FUNCTIONS.register("staff_slot_operations", () -> CoreGameTests::staffSlotOperations);
	public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STAFF_HAND_PRIORITY =
			TEST_FUNCTIONS.register("staff_hand_priority", () -> CoreGameTests::staffHandPriority);
	public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STAFF_CASTING =
			TEST_FUNCTIONS.register("staff_casting", () -> CoreGameTests::staffCasting);
	public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPELL_CONTROL_ADJUSTMENTS =
			TEST_FUNCTIONS.register("spell_control_adjustments", () -> CoreGameTests::spellControlAdjustments);
	public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BARRIER_CODEC =
			TEST_FUNCTIONS.register("barrier_codec", () -> CoreGameTests::barrierCodec);
	public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BARRIER_GEOMETRY =
			TEST_FUNCTIONS.register("barrier_geometry", () -> CoreGameTests::barrierGeometry);
	public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BARRIER_REPLACEMENT_REFUND =
			TEST_FUNCTIONS.register("barrier_replacement_refund", () -> CoreGameTests::barrierReplacementRefund);
	public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BARRIER_DEGRADATION =
			TEST_FUNCTIONS.register("barrier_degradation", () -> CoreGameTests::barrierDegradation);

	private CoreGameTests() {
	}

	private static void spellCatalog(GameTestHelper helper) {
		SpellDefinition definition = SpellCatalog.getById("trigger_probe").orElse(null);
		check(helper, definition != null, "The trigger_probe spell definition was not loaded");
		check(helper, definition.id().equals(ResourceLocation.fromNamespaceAndPath(EminentMagicMod.MODID, "trigger_probe")),
				"The spell catalog returned an unexpected ID");
		check(helper, SpellCatalog.getById("eminent_magic:trigger_probe").isPresent(),
				"The spell catalog could not resolve a namespaced ID");
		check(helper, SpellCatalog.getById("missing_test_spell").isEmpty(),
				"The spell catalog resolved an unknown ID");
		check(helper, !SpellCatalog.all().isEmpty(), "The spell catalog is empty");
		helper.succeed();
	}

	private static void spellCost(GameTestHelper helper) {
		SpellDefinition definition = new SpellDefinition(
				ResourceLocation.fromNamespaceAndPath(EminentMagicMod.MODID, "cost_test"),
				"Cost test", 10, 2, 1, "test", false, null, null, null);
		double cost = SpellCastingService.calculateManaCost(definition, 5, 4);
		check(helper, Math.abs(cost - 14) < 1.0E-9, "Spell cost scaling produced " + cost + " instead of 14");
		helper.succeed();
	}

	private static void spellItemRoundTrip(GameTestHelper helper) {
		SpellDefinition definition = SpellCatalog.getById("trigger_probe").orElse(null);
		check(helper, definition != null, "Cannot test spell items without the trigger_probe definition");
		ItemStack spell = SpellItem.createStack(definition);
		ItemStack staff = SpellStaffItem.createStack(definition);
		check(helper, SpellItem.getSpellId(spell).filter(definition.id()::equals).isPresent(),
				"A spell item did not preserve its namespaced spell ID");
		check(helper, SpellItem.getSpellId(staff).filter(definition.id()::equals).isPresent(),
				"A staff item did not preserve its namespaced spell ID");
		helper.succeed();
	}

	private static void manaAttributes(GameTestHelper helper) {
		ServerPlayer player = createServerPlayer(helper);
		check(helper, player.getAttributes().hasAttribute(EminentMagicModAttributes.MANA_RESERVES),
				"Players are missing the mana reserves attribute");
		check(helper, player.getAttributes().hasAttribute(EminentMagicModAttributes.MANA_CURRENT),
				"Players are missing the current mana attribute");
		check(helper, player.getAttributes().hasAttribute(EminentMagicModAttributes.MANA_STRENGTH),
				"Players are missing the mana strength attribute");
		check(helper, player.getAttributes().hasAttribute(EminentMagicModAttributes.MANA_CONTROL),
				"Players are missing the mana control attribute");
		check(helper, player.getAttributes().hasAttribute(EminentMagicModAttributes.MANA_PERCEPTION),
				"Players are missing the mana perception attribute");
		helper.succeed();
	}

	private static void manaSpending(GameTestHelper helper) {
		ServerPlayer player = createServerPlayer(helper);
		player.getAttribute(EminentMagicModAttributes.MANA_RESERVES).setBaseValue(100);
		player.getAttribute(EminentMagicModAttributes.MANA_CURRENT).setBaseValue(20);

		check(helper, ManaSpendingService.tryConsume(helper.getLevel(), player, 5),
				"A valid server-side mana spend was rejected");
		double remaining = player.getAttributeValue(EminentMagicModAttributes.MANA_CURRENT);
		check(helper, Math.abs(remaining - 15) < 1.0E-9,
				"Mana spending left " + remaining + " mana instead of 15");
		helper.succeed();
	}

	private static void manaRejections(GameTestHelper helper) {
		ServerPlayer player = createServerPlayer(helper);
		player.getAttribute(EminentMagicModAttributes.MANA_RESERVES).setBaseValue(50);
		player.getAttribute(EminentMagicModAttributes.MANA_CURRENT).setBaseValue(10);

		check(helper, !ManaSpendingService.tryConsume(helper.getLevel(), player, Double.NaN),
				"NaN mana cost was accepted");
		check(helper, !ManaSpendingService.tryConsume(helper.getLevel(), player, Double.POSITIVE_INFINITY),
				"Infinite mana cost was accepted");
		check(helper, !ManaSpendingService.tryConsume(helper.getLevel(), player, -1),
				"Negative mana cost was accepted");
		check(helper, !ManaSpendingService.tryConsume(helper.getLevel(), player, 51),
				"A cost above the player's reserves was accepted");
		double currentMana = player.getAttributeValue(EminentMagicModAttributes.MANA_CURRENT);
		check(helper, Math.abs(currentMana - 10) < 1.0E-9,
				"Rejected mana requests changed the player's current mana");

		var zombie = EntityType.ZOMBIE.create(helper.getLevel(), EntitySpawnReason.COMMAND);
		check(helper, zombie != null, "Could not create an entity for the non-player guard test");
		check(helper, !ManaSpendingService.tryConsume(helper.getLevel(), zombie, 1),
				"Mana spending accepted a non-player entity");
		helper.succeed();
	}

	private static void staffSlotOperations(GameTestHelper helper) {
		ServerPlayer player = createServerPlayer(helper);
		SpellDefinition definition = SpellCatalog.getById("trigger_probe").orElse(null);
		check(helper, definition != null, "Cannot test staff slots without the trigger_probe spell definition");
		if (definition == null) {
			return;
		}
		player.setItemInHand(InteractionHand.MAIN_HAND, SpellStaffItem.createStack(definition));

		check(helper, StaffService.isHoldingStaff(player), "The staff API did not recognize a held staff");
		check(helper, StaffService.getSelectedSpell(player).map(definition::equals).orElse(false),
				"The staff did not resolve its legacy slot-zero spell");
		check(helper, StaffService.setSpellInHeldStaffSlot(player, 0, definition),
				"The staff API rejected a valid slot write");
		check(helper, StaffService.getSpellInHeldStaffSlot(player, 0).map(definition::equals).orElse(false),
				"The staff slot did not preserve its namespaced spell definition");
		check(helper, StaffService.getSelectedSpellSlot(player.getMainHandItem()) == 0,
				"The default selected staff slot was not zero");
		check(helper, !StaffService.setSpellInHeldStaffSlot(player, 1, definition),
				"The staff API accepted an out-of-range slot write");
		check(helper, !StaffService.selectHeldStaffSpellSlot(player, 1),
				"The staff API accepted an out-of-range slot selection");
		check(helper, StaffService.setSpellInHeldStaffSlot(player, 0, null),
				"The staff API rejected clearing a valid slot");
		check(helper, StaffService.getSelectedSpell(player).isEmpty(),
				"Clearing slot zero left its previous legacy spell assigned");
		helper.succeed();
	}

	private static void staffHandPriority(GameTestHelper helper) {
		ServerPlayer player = createServerPlayer(helper);
		SpellDefinition definition = SpellCatalog.getById("trigger_probe").orElse(null);
		check(helper, definition != null, "Cannot test staff hand priority without the trigger_probe spell definition");
		if (definition == null) {
			return;
		}
		player.setItemInHand(InteractionHand.MAIN_HAND, SpellStaffItem.createStack(definition));
		player.setItemInHand(InteractionHand.OFF_HAND, SpellStaffItem.createStack(definition));

		check(helper, StaffService.getHeldStaffHand(player).orElse(null) == InteractionHand.OFF_HAND,
				"The staff API did not prefer the offhand");
		check(helper, StaffService.setSpellInHeldStaffSlot(player, 0, null),
				"The staff API could not update the offhand staff");
		check(helper, StaffService.getSelectedSpell(player).isEmpty(),
				"The held-staff API did not resolve the offhand staff first");
		check(helper, StaffService.getSelectedSpell(player.getMainHandItem()).map(definition::equals).orElse(false),
				"Updating the offhand staff unexpectedly changed the main-hand staff");
		helper.succeed();
	}

	private static void staffCasting(GameTestHelper helper) {
		ServerPlayer player = createServerPlayer(helper);
		SpellDefinition definition = SpellCatalog.getById("trigger_probe").orElse(null);
		check(helper, definition != null, "Cannot test staff casting without the trigger_probe spell definition");
		if (definition == null) {
			return;
		}
		player.getAttribute(EminentMagicModAttributes.MANA_RESERVES).setBaseValue(100);
		player.getAttribute(EminentMagicModAttributes.MANA_CURRENT).setBaseValue(100);
		player.setItemInHand(InteractionHand.MAIN_HAND, SpellStaffItem.createStack(definition));

		check(helper, StaffService.castSelectedSpell(player), "The staff API rejected a valid selected-spell cast");
		double remainingMana = player.getAttributeValue(EminentMagicModAttributes.MANA_CURRENT);
		check(helper, Math.abs(remainingMana - 75) < 1.0E-9,
				"Staff casting left " + remainingMana + " mana instead of 75");
		helper.succeed();
	}

	private static void spellControlAdjustments(GameTestHelper helper) {
		ServerPlayer player = createServerPlayer(helper);
		player.getAttribute(EminentMagicModAttributes.MANA_STRENGTH).setBaseValue(3);
		player.getAttribute(EminentMagicModAttributes.MANA_CONTROL).setBaseValue(2);
		var variables = player.getData(emmy.eminentmagic.network.EminentMagicModVariables.PLAYER_VARIABLES);

		SpellControlService.adjustStrength(player, -1);
		check(helper, variables.spellStrength == 1, "Spell strength fell below its minimum");
		SpellControlService.adjustStrength(player, 1);
		SpellControlService.adjustStrength(player, 1);
		SpellControlService.adjustStrength(player, 1);
		check(helper, variables.spellStrength == 3, "Spell strength did not clamp to its mana attribute");
		SpellControlService.adjustControl(player, 1);
		SpellControlService.adjustControl(player, 1);
		check(helper, variables.spellControl == 2, "Spell control did not clamp to its mana attribute");
		SpellControlService.adjustControl(player, -1);
		check(helper, variables.spellControl == 1, "Spell control fell below its minimum");
		check(helper, !StaffService.isHoldingStaff(player), "The control test unexpectedly requires a held staff");
		helper.succeed();
	}

	private static void barrierCodec(GameTestHelper helper) {
		Barrier barrier = new Barrier(UUID.randomUUID(), 1.25D, -2.5D, 3.75D, 10.0D, -45.0D, 2.0D, 4.5D, 17.25D, UUID.randomUUID());
		var encoded = Barrier.CODEC.encodeStart(JsonOps.INSTANCE, barrier).result().orElse(null);
		check(helper, encoded != null, "A valid barrier could not be encoded");
		if (encoded == null) {
			return;
		}
		Barrier decoded = Barrier.CODEC.parse(JsonOps.INSTANCE, encoded).result().orElse(null);
		check(helper, barrier.equals(decoded), "Barrier serialization did not preserve all fields");
		check(helper, Barrier.CODEC.parse(JsonOps.INSTANCE, encoded.deepCopy()).result().isPresent(),
				"The barrier codec could not decode a saved barrier");
		helper.succeed();
	}

	private static void barrierGeometry(GameTestHelper helper) {
		UUID owner = UUID.randomUUID();
		Barrier barrier = new Barrier(UUID.randomUUID(), 0, 0, 0, 0, 0, 0, 2, 10, owner);
		var centerHit = BarrierGeometry.intersect(barrier, new Vec3(0, 0, -1), new Vec3(0, 0, 1));
		check(helper, centerHit.isPresent(), "A segment through the barrier center was not blocked");
		if (centerHit.isPresent()) {
			check(helper, Math.abs(centerHit.get().segmentT() - 0.5D) < 1.0E-9D,
					"The barrier returned an incorrect segment hit position");
		}
		check(helper, BarrierGeometry.intersect(barrier, new Vec3(1.8D, 0.8D, -1), new Vec3(1.8D, 0.8D, 1)).isEmpty(),
				"The hexagonal boundary accepted a point outside the hexagon");
		check(helper, BarrierGeometry.intersect(barrier, new Vec3(-0.5D, 0, 1), new Vec3(0.5D, 0, 1)).isEmpty(),
				"A segment parallel to the barrier plane was treated as a crossing");

		Barrier rotated = barrier.withRotation(0, 90, 0);
		check(helper, BarrierGeometry.intersect(rotated, new Vec3(1, 0, 0), new Vec3(-1, 0, 0)).isPresent(),
				"Y-axis yaw did not rotate the barrier plane");
		check(helper, BarrierService.projectileImpactDamage(2.4D) == 5.0D,
				"Projectile damage did not use ceil(2 × impact speed)");
		helper.succeed();
	}

	private static void barrierReplacementRefund(GameTestHelper helper) {
		ServerPlayer player = createServerPlayer(helper);
		player.getAttribute(EminentMagicModAttributes.MANA_RESERVES).setBaseValue(100);
		player.getAttribute(EminentMagicModAttributes.MANA_CURRENT).setBaseValue(10);
		Vec3 firstPosition = player.getEyePosition().add(0, 0, 10);
		Barrier first = BarrierService.create(helper.getLevel(), player, firstPosition, Vec3.ZERO, 2, 20);
		check(helper, first != null, "The server rejected a valid barrier creation");
		if (first == null) {
			return;
		}
		check(helper, BarrierService.setNumericProperty(helper.getLevel(), first, BarrierService.NumericProperty.HEALTH, 10),
				"Could not set the barrier's remaining health");
		ServerPlayer otherPlayer = createServerPlayer(helper);
		otherPlayer.getAttribute(EminentMagicModAttributes.MANA_RESERVES).setBaseValue(100);
		otherPlayer.getAttribute(EminentMagicModAttributes.MANA_CURRENT).setBaseValue(0);
		Barrier foreign = BarrierService.create(helper.getLevel(), otherPlayer, firstPosition.add(0.1D, 0, 0), Vec3.ZERO, 2, 8);
		check(helper, foreign != null, "Could not create a barrier owned by another player");
		check(helper, ManaSpendingService.tryConsume(helper.getLevel(), player, 5),
				"Could not spend mana for the replacement barrier");
		Vec3 pickupPosition = firstPosition.add(2.0D, 0, 10);
		Barrier pickupResult = BarrierService.create(helper.getLevel(), player, pickupPosition, Vec3.ZERO, 2, 5);
		check(helper, pickupResult == null, "Picking up a nearby barrier created a replacement");
		check(helper, BarrierService.getBarrier(helper.getLevel(), first.id().toString()).isEmpty(),
				"The picked-up barrier remained active");
		check(helper, BarrierService.getBarriers(helper.getLevel()).stream()
				.noneMatch(barrier -> barrier.owner().equals(player.getUUID())
						&& barrier.position().distanceToSqr(pickupPosition) <= 0.25D),
				"Picking up a barrier left another owned barrier at the pickup point");
		check(helper, foreign != null && BarrierService.getBarrier(helper.getLevel(), foreign.id().toString()).isPresent(),
				"Replacing a nearby barrier removed another player's barrier");
		check(helper, Math.abs(player.getAttributeValue(EminentMagicModAttributes.MANA_CURRENT) - 18.5D) < 1.0E-9D,
				"Pickup did not refund 85% of remaining barrier health and the recast cost");
		check(helper, otherPlayer.getAttributeValue(EminentMagicModAttributes.MANA_CURRENT) == 0,
				"Replacing a nearby barrier refunded mana to another player");

		Vec3 cappedPosition = player.getEyePosition().add(0, 0, 30);
		Barrier capped = BarrierService.create(helper.getLevel(), player, cappedPosition, Vec3.ZERO, 2, 20);
		check(helper, capped != null, "Could not create a barrier for the refund-cap test");
		player.getAttribute(EminentMagicModAttributes.MANA_CURRENT).setBaseValue(99);
		Barrier cappedPickup = BarrierService.create(helper.getLevel(), player, cappedPosition.add(2.0D, 0, 10), Vec3.ZERO, 2, 5);
		check(helper, cappedPickup == null, "Refund-cap pickup created a replacement barrier");
		check(helper, player.getAttributeValue(EminentMagicModAttributes.MANA_CURRENT) <= 100,
				"Barrier pickup exceeded the player's mana capacity");

		ServerPlayer creativePlayer = createServerPlayer(helper);
		creativePlayer.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
		creativePlayer.getAttribute(EminentMagicModAttributes.MANA_RESERVES).setBaseValue(100);
		creativePlayer.getAttribute(EminentMagicModAttributes.MANA_CURRENT).setBaseValue(25);
		Vec3 creativePosition = creativePlayer.getEyePosition().add(0, 0, 40);
		Barrier creativeFirst = BarrierService.create(helper.getLevel(), creativePlayer, creativePosition, Vec3.ZERO, 2, 20);
		check(helper, creativeFirst != null, "Could not create a creative-mode barrier");
		check(helper, ManaSpendingService.tryConsume(helper.getLevel(), creativePlayer, 5),
				"Creative mode rejected a barrier mana payment");
		Barrier creativePickup = BarrierService.create(helper.getLevel(), creativePlayer,
				creativePosition.add(0, 0, 10), Vec3.ZERO, 2, 5);
		check(helper, creativePickup == null, "Picking up a creative-mode barrier created a replacement");
		check(helper, BarrierService.getBarrier(helper.getLevel(), creativeFirst.id().toString()).isEmpty(),
				"Picking up a creative-mode barrier left it active");
		check(helper, creativePlayer.getAttributeValue(EminentMagicModAttributes.MANA_CURRENT) == 25,
				"Barrier pickup refunded mana in creative mode");

		BarrierService.remove(helper.getLevel(), foreign);

		Vec3 lookPosition = player.getEyePosition().add(0, 0, 50);
		Barrier offRay = BarrierService.create(helper.getLevel(), player, lookPosition.add(5, 0, 0), Vec3.ZERO, 2, 7);
		Barrier created = BarrierService.create(helper.getLevel(), player, lookPosition, Vec3.ZERO, 2, 5);
		check(helper, created != null, "A cast that missed owned barriers did not create a barrier");
		check(helper, offRay != null && BarrierService.getBarrier(helper.getLevel(), offRay.id().toString()).isPresent(),
				"A cast picked up an owned barrier that the look ray missed");
		BarrierService.remove(helper.getLevel(), offRay);
		BarrierService.remove(helper.getLevel(), created);
		helper.succeed();
	}

	private static void barrierDegradation(GameTestHelper helper) {
		check(helper, BarrierGameRules.BARRIER_DEGRADATION_RATE != null,
				"The barrier degradation gamerule was not registered");
		if (BarrierGameRules.BARRIER_DEGRADATION_RATE == null) {
			return;
		}
		ServerPlayer player = createServerPlayer(helper);
		Barrier barrier = BarrierService.create(helper.getLevel(), player, player.position().add(70, 30, 30), Vec3.ZERO, 2, 10);
		check(helper, barrier != null, "The server rejected a valid barrier creation");
		if (barrier == null) {
			return;
		}

		GameRules.IntegerValue degradationRate = helper.getLevel().getGameRules().getRule(BarrierGameRules.BARRIER_DEGRADATION_RATE);
		int originalRate = degradationRate.get();
		degradationRate.set(0, helper.getLevel().getServer());
		BarrierService.applyDegradationRule(helper.getLevel());
		check(helper, BarrierService.getBarrier(helper.getLevel(), barrier.id().toString()).map(Barrier::health).orElse(0.0D) == 10.0D,
				"Zero gamerule value did not disable barrier degradation");

		degradationRate.set(2, helper.getLevel().getServer());
		BarrierService.applyDegradationRule(helper.getLevel());
		check(helper, BarrierService.getBarrier(helper.getLevel(), barrier.id().toString()).map(Barrier::health).orElse(0.0D) == 8.0D,
				"The gamerule did not remove the configured health per second");
		degradationRate.set(originalRate, helper.getLevel().getServer());
		BarrierService.remove(helper.getLevel(), barrier);
		helper.succeed();
	}

	private static void check(GameTestHelper helper, boolean condition, String message) {
		if (!condition) {
			throw helper.assertionException(message);
		}
	}

	private static ServerPlayer createServerPlayer(GameTestHelper helper) {
		UUID id = UUID.randomUUID();
		return FakePlayerFactory.get(helper.getLevel(), new GameProfile(id, "core-test-" + id.toString().substring(0, 6)));
	}
}
