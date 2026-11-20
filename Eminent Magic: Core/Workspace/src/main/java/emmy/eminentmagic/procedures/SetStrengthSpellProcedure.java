package emmy.eminentmagic.procedures;

import net.minecraft.world.entity.Entity;

import emmy.eminentmagic.network.EminentMagicModVariables;

public class SetStrengthSpellProcedure {
	public static void execute(Entity player, double value) {
		if (player == null)
			return;
		{
			EminentMagicModVariables.PlayerVariables _vars = player.getData(EminentMagicModVariables.PLAYER_VARIABLES);
			_vars.spellStrength = value;
			_vars.markSyncDirty();
		}
	}
}