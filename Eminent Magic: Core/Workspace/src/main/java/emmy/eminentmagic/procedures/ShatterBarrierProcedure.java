package emmy.eminentmagic.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

import emmy.eminentmagic.network.EminentMagicModVariables;

public class ShatterBarrierProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, double barrierIndex) {
		com.google.gson.JsonObject barrierJsonObject = new com.google.gson.JsonObject();
		if (world instanceof ServerLevel _level)
			_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(), "");
		barrierJsonObject = EminentMagicModVariables.OverworldBarriers.get((int) barrierIndex).getAsJsonObject();
		double originX = barrierJsonObject.has("originX") ? barrierJsonObject.get("originX").getAsDouble() : 0.0D;
		double originY = barrierJsonObject.has("originY") ? barrierJsonObject.get("originY").getAsDouble() : 0.0D;
		double originZ = barrierJsonObject.has("originZ") ? barrierJsonObject.get("originZ").getAsDouble() : 0.0D;
		double angleX = barrierJsonObject.has("angleX") ? barrierJsonObject.get("angleX").getAsDouble() : 0.0D;
		double angleY = barrierJsonObject.has("angleY") ? barrierJsonObject.get("angleY").getAsDouble() : 1.0D;
		double angleZ = barrierJsonObject.has("angleZ") ? barrierJsonObject.get("angleZ").getAsDouble() : 0.0D;
		double scale = barrierJsonObject.has("scale") ? barrierJsonObject.get("scale").getAsDouble() : 1.0D;
		BarrierShatterEffectProcedure.execute(world, originX, originY, originZ, angleX, angleY, angleZ, Math.max(0.5D, scale / 2.0D), 24);
		int barrierIndexInt = (int) Math.round(barrierIndex);
		if (barrierIndexInt >= 0 && barrierIndexInt < EminentMagicModVariables.OverworldBarriers.size()) {
			EminentMagicModVariables.OverworldBarriers.remove(barrierIndexInt);
		}
	}
}