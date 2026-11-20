package emmy.eminentmagic.procedures;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.SimpleParticleType;

import javax.annotation.Nullable;

import emmy.eminentmagic.init.EminentMagicModParticleTypes;
import emmy.eminentmagic.init.EminentMagicModMobEffects;

@EventBusSubscriber
public class OnEntityChannelingTickProcedure {
	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent.Pre event) {
		execute(event, event.getEntity().level(), event.getEntity());
	}

	public static void execute(LevelAccessor world, Entity entity) {
		execute(null, world, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(EminentMagicModMobEffects.MANA_CHANNEL)) {
			if (world instanceof ServerLevel _level)
				_level.sendParticles((SimpleParticleType) (EminentMagicModParticleTypes.MANA_PARTICLE.get()), (entity.getX()), (entity.getY()), (entity.getZ()), (int) (15 * Math.sqrt(entity.getPersistentData().getDoubleOr("timeChannelling", 0))),
						(0.2 * Math.sqrt(entity.getPersistentData().getDoubleOr("timeChannelling", 0))), (0.5 * Math.sqrt(entity.getPersistentData().getDoubleOr("timeChannelling", 0))),
						(0.2 * Math.sqrt(entity.getPersistentData().getDoubleOr("timeChannelling", 0))), 0);
			if (entity.getPersistentData().getDoubleOr("timeChannelling", 0) >= 10) {// Start of user code block channeling outline
				entity.getPersistentData().putBoolean("eminent_magic:channeling_outline", true);
				// End of user code block channeling outline
			} else {// Start of user code block channeling outline inactive
				entity.getPersistentData().putBoolean("eminent_magic:channeling_outline", false);
				// End of user code block channeling outline inactive
			}
			entity.getPersistentData().putDouble("timeChannelling", (entity.getPersistentData().getDoubleOr("timeChannelling", 0) + 0.1));
		}
	}
}