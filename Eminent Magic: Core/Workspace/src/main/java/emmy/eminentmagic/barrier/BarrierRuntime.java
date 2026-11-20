package emmy.eminentmagic.barrier;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import emmy.eminentmagic.init.EminentMagicModParticleTypes;

@EventBusSubscriber
public final class BarrierRuntime {
	private static final int MAX_OUTLINE_PARTICLES = 96;

	private BarrierRuntime() {
	}

	@SubscribeEvent
	public static void onProjectileTick(EntityTickEvent.Pre event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Projectile projectile) || !(projectile.level() instanceof ServerLevel level)) {
			return;
		}
		Vec3 start = projectile.position();
		Vec3 end = start.add(projectile.getDeltaMovement());
		if (start.distanceToSqr(end) < 1.0E-12D) {
			return;
		}
		BarrierService.findNearestHit(level, start, end).ifPresent(hit -> {
			BarrierService.damage(level, hit.barrier(),
					BarrierService.projectileImpactDamage(projectile.getDeltaMovement().length()));
			level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
					hit.point().x, hit.point().y, hit.point().z, 4, 0.05D, 0.05D, 0.05D, 0.01D);
			projectile.discard();
			event.setCanceled(true);
		});
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level)) {
			return;
		}
		List<Barrier> barriers = BarrierService.getBarriers(level);
		if (barriers.isEmpty()) {
			return;
		}

		if (Math.floorMod(level.getGameTime(), 20L) == 0L) {
			BarrierService.applyDegradationRule(level);
		}
		renderOutlines(level, BarrierService.getBarriers(level));
	}

	private static void renderOutlines(ServerLevel level, List<Barrier> barriers) {
		SimpleParticleType particle = EminentMagicModParticleTypes.MANA_PARTICLE.get();
		for (Barrier barrier : barriers) {
			int count = Math.max(6, (int) Math.min(MAX_OUTLINE_PARTICLES, Math.round(15.0D * barrier.radius())));
			for (int index = 0; index < count; index++) {
				double edgePosition = ThreadLocalRandom.current().nextDouble(0.0D, 6.0D);
				Vec3 point = BarrierGeometry.pointOnPerimeter(barrier, edgePosition);
				level.sendParticles(particle, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
			}
		}
	}
}
