package emmy.eminentmagic.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import emmy.eminentmagic.barrier.Barrier;
import emmy.eminentmagic.barrier.BarrierService;

public class SimpleBarrierProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		var hit = entity.level().clip(new ClipContext(entity.getEyePosition(1f),
				entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(2)),
				ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos();
		Vec3 position = new Vec3(hit.getX(), hit.getY(), hit.getZ());
		Barrier createdBarrier = BarrierService.create(world, entity, position, Vec3.ZERO, 1, 20);
		if (createdBarrier != null && BarrierService.getBarrier(world, createdBarrier.id().toString()).isPresent()) {
			if (entity instanceof Player _player && !_player.level().isClientSide())
				_player.displayClientMessage(Component.literal("Created barrier"), false);
		}
	}
}