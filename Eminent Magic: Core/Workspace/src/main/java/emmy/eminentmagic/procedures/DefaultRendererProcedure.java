package emmy.eminentmagic.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.core.particles.SimpleParticleType;

import emmy.eminentmagic.init.EminentMagicModParticleTypes;
import emmy.eminentmagic.init.EminentMagicModAttributes;

public class DefaultRendererProcedure {
	public static void execute(LevelAccessor world, Entity otherEntity) {
		if (otherEntity == null)
			return;
		double numParticles = 0;
		double degreeOffset = 0;
		double particlesPerSegment = 0;
		double numSegments = 0;
		double numRenderedParticles = 0;
		double height = 0;
		double Distance = 0;
		double capeLength = 0;
		numParticles = otherEntity instanceof LivingEntity _livingEntity0 && _livingEntity0.getAttributes().hasAttribute(EminentMagicModAttributes.MANA_CURRENT) ? _livingEntity0.getAttribute(EminentMagicModAttributes.MANA_CURRENT).getValue() : 0;
		degreeOffset = 30;
		numSegments = 360d / 5;
		capeLength = Math.log10(numParticles) * 4.25 - 7;
		particlesPerSegment = numParticles / numSegments;
		numRenderedParticles = particlesPerSegment;
		for (int index0 = 0; index0 < (int) numSegments; index0++) {
			while (numRenderedParticles >= 1) {
				height = 2.5 * Math.pow(numParticles / 100, Math.log10(2)) * Math.pow(Mth.nextDouble(RandomSource.create(), 0, 1), 0.1);
				Distance = (0.25 + Math.pow(height / 1.5, 2.5) * (capeLength / 1.5)) / Math.pow(2.5 * Math.pow(numParticles / 100, Math.log10(2)), 2.75);
				world.addParticle((SimpleParticleType) (EminentMagicModParticleTypes.MANA_PARTICLE.get()),
						(otherEntity.getX() + Distance * Math.cos(Math.toRadians(otherEntity.getYRot() - degreeOffset)) + Mth.nextDouble(RandomSource.create(), -0.15, 0.15)),
						(((otherEntity.getY() + 2.5 * Math.pow(numParticles / 100, Math.log10(2))) - height / 1.25) - Mth.nextDouble(RandomSource.create(), 0, height / 6)),
						(otherEntity.getZ() + Distance * Math.sin(Math.toRadians(otherEntity.getYRot() - degreeOffset)) + Mth.nextDouble(RandomSource.create(), -0.15, 0.15)), 0, (-0.2), 0);
				numRenderedParticles = numRenderedParticles - 1;
			}
			numRenderedParticles = numRenderedParticles + particlesPerSegment;
			degreeOffset = degreeOffset + 5;
		}
	}
}