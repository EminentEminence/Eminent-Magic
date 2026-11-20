package emmy.eminentmagic.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import emmy.eminentmagic.init.EminentMagicModAttributes;

public class GetManaReservesProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return "" + Math
				.round(entity instanceof LivingEntity _livingEntity0 && _livingEntity0.getAttributes().hasAttribute(EminentMagicModAttributes.MANA_RESERVES) ? _livingEntity0.getAttribute(EminentMagicModAttributes.MANA_RESERVES).getBaseValue() : 0);
	}
}