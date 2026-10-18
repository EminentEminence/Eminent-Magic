package emmy.eminentmagic.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.core.particles.SimpleParticleType;

import emmy.eminentmagic.init.EminentMagicModParticleTypes;
import emmy.eminentmagic.init.EminentMagicModAttributes;

public class CapeRendererProcedure {
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
		numSegments = 120d / 5;
		capeLength = Math.log10(numParticles) * 4.25 - 7;
		particlesPerSegment = numParticles / numSegments;
		numRenderedParticles = particlesPerSegment;
		for (int index0 = 0; index0 < (int) numSegments; index0++) {
			while (numRenderedParticles >= 1) {
				height = Math.min(capeLength, 1.6) * Math.pow(Mth.nextDouble(RandomSource.create(), 0, 1), 0.5);
				Distance = 0.25 + Math.pow(height / 1.5, 2.5) * (capeLength / 1.5);
				world.addParticle((SimpleParticleType) (EminentMagicModParticleTypes.MANA_PARTICLE.get()),
						(otherEntity.getX() + Distance * Math.cos(Math.toRadians(otherEntity.getYRot() - degreeOffset)) + Mth.nextDouble(RandomSource.create(), (-0.25) * Math.min(capeLength, 1.6), 0.25 * Math.min(capeLength, 1.6))),
						((otherEntity.getY() + 1.5) - height),
						(otherEntity.getZ() + Distance * Math.sin(Math.toRadians(otherEntity.getYRot() - degreeOffset)) + Mth.nextDouble(RandomSource.create(), (-0.25) * Math.min(capeLength, 1.6), 0.25 * Math.min(capeLength, 1.6))), 0, (-0.2), 0);
				numRenderedParticles = numRenderedParticles - 1;
			}
			numRenderedParticles = numRenderedParticles + particlesPerSegment;
			degreeOffset = degreeOffset + 5;
		}
	}
}