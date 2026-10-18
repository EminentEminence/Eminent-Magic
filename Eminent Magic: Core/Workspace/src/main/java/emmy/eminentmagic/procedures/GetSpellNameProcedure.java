package emmy.eminentmagic.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

public class GetSpellNameProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		if (GetCurrentSpellIDProcedure.execute(entity) > 0) {
			return Component.translatable(("spell.eminent_magic.name." + new java.text.DecimalFormat("##").format(GetCurrentSpellIDProcedure.execute(entity)))).getString();
		}
		return "No Spell Selected";
	}
}