package emmy.eminentmagic.procedures;

import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

import emmy.eminentmagic.network.EminentMagicModVariables;

public class GetLocalDimensionBarriersProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		double arrayIndex = 0;
		com.google.gson.JsonObject barrierJsonObject = new com.google.gson.JsonObject();
		if ((world instanceof Level _lvl ? _lvl.dimension() : (world instanceof WorldGenLevel _wgl ? _wgl.getLevel().dimension() : Level.OVERWORLD)) == Level.OVERWORLD) {
			if (EminentMagicModVariables.OverworldBarriers.size() <= 0) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("No barriers active in this dimension"), false);
			}
			for (int index0 = 0; index0 < (int) EminentMagicModVariables.OverworldBarriers.size(); index0++) {
				barrierJsonObject = EminentMagicModVariables.OverworldBarriers.get((int) arrayIndex).getAsJsonObject();
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal(barrierJsonObject.toString()), false);
				arrayIndex = arrayIndex + 1;
			}
		}
	}
}