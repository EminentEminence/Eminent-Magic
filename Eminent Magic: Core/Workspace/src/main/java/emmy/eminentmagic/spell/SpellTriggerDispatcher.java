package emmy.eminentmagic.spell;

import java.util.OptionalDouble;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

import emmy.eminentmagic.item.SpellItem;

public final class SpellTriggerDispatcher {
	private static final ThreadLocal<PaidMana> CURRENT_PAYMENT = new ThreadLocal<>();

	private SpellTriggerDispatcher() {
	}

	public static boolean fire(Entity caster, ItemStack spellStack, SpellTrigger trigger) {
		if (caster == null || caster.level().isClientSide()) {
			return false;
		}
		return SpellItem.getSpellId(spellStack).flatMap(SpellCatalog::get)
				.map(spell -> fire(caster, spellStack, spell, trigger)).orElse(false);
	}

	public static boolean fire(Entity caster, ItemStack spellStack, SpellDefinition spell, SpellTrigger trigger) {
		return fire(caster, spellStack, spell, trigger, null);
	}

	public static boolean fire(Entity caster, ItemStack spellStack, SpellDefinition spell, SpellTrigger trigger, double manaCostPaid) {
		return fire(caster, spellStack, spell, trigger, Double.valueOf(manaCostPaid));
	}

	private static boolean fire(Entity caster, ItemStack spellStack, SpellDefinition spell, SpellTrigger trigger, Double manaCostPaid) {
		if (caster == null || caster.level().isClientSide() || spell == null || trigger == null
				|| SpellCatalog.get(spell.id()).filter(spell::equals).isEmpty()
				|| (manaCostPaid != null && (!Double.isFinite(manaCostPaid) || manaCostPaid < 0))) {
			return false;
		}
		PaidMana previousPayment = CURRENT_PAYMENT.get();
		// Procedure handlers run synchronously during event dispatch.
		if (manaCostPaid == null) {
			CURRENT_PAYMENT.remove();
		} else {
			CURRENT_PAYMENT.set(new PaidMana(caster, manaCostPaid));
		}
		try {
			NeoForge.EVENT_BUS.post(new SpellTriggerEvent(spell, trigger, SpellTriggerContext.create(caster, spellStack)));
			return true;
		} finally {
			if (previousPayment == null) {
				CURRENT_PAYMENT.remove();
			} else {
				CURRENT_PAYMENT.set(previousPayment);
			}
		}
	}

	public static OptionalDouble currentManaCostPaid(Entity caster) {
		PaidMana payment = CURRENT_PAYMENT.get();
		return payment != null && payment.caster() == caster
				? OptionalDouble.of(payment.amount())
				: OptionalDouble.empty();
	}

	private record PaidMana(Entity caster, double amount) {
	}
}