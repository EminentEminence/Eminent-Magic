package net.emmy.eminentmagic.core;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class EminentMagicCoreManaPayment {
	private EminentMagicCoreManaPayment() {
	}

	public static ManaPaymentResult consumeMana(LivingEntity entity, double amount, boolean allowReserves) {
		if (entity == null || !Double.isFinite(amount) || amount < 0) {
			return ManaPaymentResult.INVALID_AMOUNT;
		}
		if (entity.level().isClientSide()) {
			return ManaPaymentResult.NOT_SERVER_AUTHORITY;
		}
		if (amount == 0 || entity instanceof Player player && player.getAbilities().instabuild) {
			return ManaPaymentResult.PAID;
		}

		double currentMana = EminentMagicCoreManaAttributes.getManaCurrent(entity);
		double reserves = allowReserves ? EminentMagicCoreManaAttributes.getManaReserves(entity) : 0;
		if (amount > currentMana + reserves) {
			return ManaPaymentResult.INSUFFICIENT_MANA;
		}

		double manaPayment = Math.min(currentMana, amount);
		double reservePayment = amount - manaPayment;
		EminentMagicCoreManaAttributes.setManaCurrent(entity, currentMana - manaPayment);
		if (reservePayment > 0) {
			EminentMagicCoreManaAttributes.setManaReserves(entity, reserves - reservePayment);
		}
		return ManaPaymentResult.PAID;
	}

	public enum ManaPaymentResult {
		PAID,
		INSUFFICIENT_MANA,
		INVALID_AMOUNT,
		NOT_SERVER_AUTHORITY
	}
}
