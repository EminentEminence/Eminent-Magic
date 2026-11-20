package emmy.eminentmagic.mana;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelAccessor;

import emmy.eminentmagic.init.EminentMagicModAttributes;
import emmy.eminentmagic.init.EminentMagicModGameRules;

public final class ManaSpendingService {
	private ManaSpendingService() {
	}

	/**
	 * Spends mana on the authoritative server. Client-side calls and invalid costs are rejected.
	 */
	public static boolean tryConsume(LevelAccessor level, Entity entity, double manaCost) {
		if (!(level instanceof ServerLevel world) || !(entity instanceof ServerPlayer player)
				|| player.level() != world || !Double.isFinite(manaCost) || manaCost < 0 || !player.isAlive()) {
			return false;
		}
		if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
			return true;
		}

		var reservesAttribute = player.getAttribute(EminentMagicModAttributes.MANA_RESERVES);
		var currentManaAttribute = player.getAttribute(EminentMagicModAttributes.MANA_CURRENT);
		if (reservesAttribute == null || currentManaAttribute == null) {
			return false;
		}

		double reserves = reservesAttribute.getValue();
		double currentMana = currentManaAttribute.getValue();
		if (!Double.isFinite(reserves) || reserves < 0 || !Double.isFinite(currentMana) || currentMana < 0) {
			return false;
		}
		if (manaCost > reserves) {
			showComplexityFailure(player);
			return false;
		}

		double remainingMana = manaCost;
		if (currentMana >= remainingMana) {
			currentManaAttribute.setBaseValue(currentMana - remainingMana);
			remainingMana = 0;
		} else {
			currentManaAttribute.setBaseValue(0);
			remainingMana -= currentMana;
		}

		if (remainingMana > 0) {
			double selfDamage = Math.round((remainingMana / (0.2 * reserves)) * 20);
			if (player.getHealth() <= selfDamage && selfDamage > 0) {
				remainingMana *= (selfDamage - player.getHealth()) / selfDamage;
				selfDamage = player.getHealth() - 1;
			} else {
				remainingMana = 0;
			}
			ResourceKey<net.minecraft.world.damagesource.DamageType> damageType = ResourceKey.create(Registries.DAMAGE_TYPE,
					ResourceLocation.fromNamespaceAndPath("eminent_magic", "not_enough_mana"));
			player.hurt(new DamageSource(world.holderOrThrow(damageType)), (float) selfDamage);
		}

		if (remainingMana > 0 && world.getGameRules().getBoolean(EminentMagicModGameRules.SPELLS_CONSUME_RESERVE)) {
			if (reserves >= remainingMana) {
				reservesAttribute.setBaseValue(reserves - remainingMana);
				remainingMana = 0;
			} else {
				reservesAttribute.setBaseValue(0);
				remainingMana -= reserves;
			}
		}

		if (remainingMana > 0) {
			player.displayClientMessage(Component.translatable("message.eminent_magic.out_of_mana"), true);
			return false;
		}
		return true;
	}

	private static void showComplexityFailure(ServerPlayer player) {
		int messageNumber = Mth.nextInt(RandomSource.create(), 1, 3);
		String messageKey = "message.eminent_magic.spell_complexity_failure_" + messageNumber;
		player.displayClientMessage(Component.translatable(messageKey), true);
	}
}
