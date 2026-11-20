package emmy.eminentmagic.init;

import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber
public final class BarrierGameRules {
	public static GameRules.Key<GameRules.IntegerValue> BARRIER_DEGRADATION_RATE;

	private BarrierGameRules() {
	}

	@SubscribeEvent
	public static void register(FMLCommonSetupEvent event) {
		BARRIER_DEGRADATION_RATE = GameRules.register(
				"barrierDegradationRate",
				GameRules.Category.MISC,
				GameRules.IntegerValue.create(1));
	}
}
