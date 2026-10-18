package emmy.eminentmagic.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

public class RenderManaProcedure {
	public static void execute(LevelAccessor world, Entity otherEntity) {
		if (otherEntity == null)
			return;
		DefaultRendererProcedure.execute(world, otherEntity);
	}
}