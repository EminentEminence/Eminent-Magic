package emmy.eminentmagic.procedures;

import net.minecraft.world.level.LevelAccessor;

public class RemoveLocalDimensionBarriersProcedure {
	public static void execute(LevelAccessor world) {
		emmy.eminentmagic.barrier.BarrierService.clear(world);
	}
}