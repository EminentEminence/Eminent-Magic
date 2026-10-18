package emmy.eminentmagic.procedures;

import emmy.eminentmagic.network.EminentMagicModVariables;

public class RemoveLocalDimensionBarriersProcedure {
	public static void execute() {
		EminentMagicModVariables.OverworldBarriers = new com.google.gson.JsonArray();
	}
}