package emmy.eminentmagic.procedures;

import net.minecraft.world.entity.Entity;

import emmy.eminentmagic.network.EminentMagicModVariables;

public class GetControlSpellStrengthProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return new java.text.DecimalFormat("##").format(entity.getData(EminentMagicModVariables.PLAYER_VARIABLES).spellStrength);
	}
}