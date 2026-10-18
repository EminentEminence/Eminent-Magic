package emmy.eminentmagic.procedures;

import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

public class BarrierShatterEffectProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, double angleX, double angleY, double angleZ, double radius, int particleCount) {
		if (world == null)
			return;

		Vec3 normal = new Vec3(angleX, angleY, angleZ);
		if (normal.lengthSqr() < 1.0E-6D)
			normal = new Vec3(0.0D, 1.0D, 0.0D);
		else
			normal = normal.normalize();

		Vec3 referenceUp = Math.abs(normal.y) > 0.9D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
		Vec3 planeRight = normal.cross(referenceUp).normalize();
		Vec3 planeUp = planeRight.cross(normal).normalize();

		for (int i = 0; i < Math.max(6, particleCount); i++) {
			double angle = ThreadLocalRandom.current().nextDouble(0.0D, 2.0D * Math.PI);
			double distance = ThreadLocalRandom.current().nextDouble(0.0D, radius);
			double localX = Math.cos(angle) * distance;
			double localZ = Math.sin(angle) * distance;
			Vec3 offset = planeRight.scale(localX).add(planeUp.scale(localZ));
			double burstX = x + offset.x;
			double burstY = y + offset.y;
			double burstZ = z + offset.z;
			double velocityX = offset.x * 0.22D;
			double velocityY = offset.y * 0.22D;
			double velocityZ = offset.z * 0.22D;

			if (world instanceof ServerLevel _serverLevel) {
				_serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, burstX, burstY, burstZ, 1, velocityX, velocityY, velocityZ, 0.08D);
			} else {
				world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, burstX, burstY, burstZ, velocityX, velocityY, velocityZ);
			}
		}

		world.playSound(null, BlockPos.containing(x, y, z), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.8F, 0.7F);
	}
}
