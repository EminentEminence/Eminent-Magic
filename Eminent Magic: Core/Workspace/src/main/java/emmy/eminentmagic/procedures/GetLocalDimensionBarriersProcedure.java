package emmy.eminentmagic.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

public class GetLocalDimensionBarriersProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		for (emmy.eminentmagic.barrier.Barrier barrieriterator : emmy.eminentmagic.barrier.BarrierService.getBarriers(world)) {
			if (entity instanceof Player _player && !_player.level().isClientSide())
				_player.displayClientMessage(Component.literal(("Barrier " + java.util.Optional.ofNullable(barrieriterator).map(barrier -> barrier.id().toString()).orElse("") + " health="
						+ emmy.eminentmagic.barrier.BarrierService.getNumericProperty(barrieriterator, emmy.eminentmagic.barrier.BarrierService.NumericProperty.HEALTH))), false);
		}
	}
}