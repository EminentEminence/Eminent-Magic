package emmy.eminentmagic.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import emmy.eminentmagic.init.EminentMagicModAttributes;

public class GetCurrentManaProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return new java.text.DecimalFormat("##")
				.format(entity instanceof LivingEntity _livingEntity0 && _livingEntity0.getAttributes().hasAttribute(EminentMagicModAttributes.MANA_CURRENT) ? _livingEntity0.getAttribute(EminentMagicModAttributes.MANA_CURRENT).getValue() : 0);
	}
}