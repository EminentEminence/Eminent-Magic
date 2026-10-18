package emmy.eminentmagic.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import emmy.eminentmagic.init.EminentMagicModAttributes;

public class GetManaStrengthProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return "" + Math
				.round(entity instanceof LivingEntity _livingEntity0 && _livingEntity0.getAttributes().hasAttribute(EminentMagicModAttributes.MANA_STRENGTH) ? _livingEntity0.getAttribute(EminentMagicModAttributes.MANA_STRENGTH).getBaseValue() : 0);
	}
}