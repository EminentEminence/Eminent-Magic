package emmy.eminentmagic.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

import emmy.eminentmagic.network.EminentMagicModVariables;

public class SimpleBarrierProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		com.google.gson.JsonObject barrierJsonObject = new com.google.gson.JsonObject();
		if (entity instanceof Player _clientCheck && !_clientCheck.level().isClientSide()) {
			if (entity instanceof Player _player && !_player.level().isClientSide())
				_player.displayClientMessage(Component.literal("Creating Barrier"), false);
			Vec3 start = entity.getEyePosition(1f);
			Vec3 end = start.add(entity.getViewVector(1f).scale(1.0)); // or your desired range
			BlockHitResult hit = entity.level().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity));
			Vec3 hitLocation = hit.getLocation();
			barrierJsonObject.addProperty("originX", hitLocation.x);
			barrierJsonObject.addProperty("originY", hitLocation.y);
			barrierJsonObject.addProperty("originZ", hitLocation.z);
			barrierJsonObject.addProperty("angleX", (entity.getLookAngle().x));
			barrierJsonObject.addProperty("angleY", (entity.getLookAngle().y));
			barrierJsonObject.addProperty("angleZ", (entity.getLookAngle().z));
			barrierJsonObject.addProperty("mana", 20);
			barrierJsonObject.addProperty("scale", 2);
			barrierJsonObject.addProperty("remainingDuration", 600);
			barrierJsonObject.addProperty("owner", (entity.getStringUUID()));
			EminentMagicModVariables.OverworldBarriers.add(barrierJsonObject);
			if (entity instanceof Player _player && !_player.level().isClientSide())
				_player.displayClientMessage(Component.literal(("Number of Barriers: " + EminentMagicModVariables.OverworldBarriers.size())), false);
		}
	}
}