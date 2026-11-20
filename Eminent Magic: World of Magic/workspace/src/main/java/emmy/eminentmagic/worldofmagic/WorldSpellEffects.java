package emmy.eminentmagic.worldofmagic;

import java.util.Comparator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import emmy.eminentmagic.spell.SpellTrigger;
import emmy.eminentmagic.spell.SpellTriggerContext;
import emmy.eminentmagic.spell.SpellTriggerEvent;

@EventBusSubscriber(modid = EminentMagicWorldMod.MODID)
public final class WorldSpellEffects {
	private WorldSpellEffects() {
	}

	@SubscribeEvent
	public static void onSpellCast(SpellTriggerEvent event) {
		if (event.trigger() != SpellTrigger.ON_CAST) {
			return;
		}
		switch (event.spell().id().toString()) {
			case "eminent_magic_world:zoltraak" -> castZoltraak(event.context());
			case "eminent_magic_world:freeze_coffin" -> castFreezeCoffin(event.context());
		}
	}

	private static void castZoltraak(SpellTriggerContext context) {
		Entity caster = context.get("entity");
		if (!(caster.level() instanceof ServerLevel level)) {
			return;
		}
		double spellControl = context.get("spellControl");
		double spellStrength = context.get("spellStrength");
		double range = spellControl + 5;
		double radius = Math.max((spellStrength / Math.max(spellControl, 1)) / 40, 0.1);
		Vec3 start = caster.getEyePosition(1.0F);
		Vec3 intendedEnd = start.add(caster.getViewVector(1.0F).scale(range));
		BlockHitResult hit = level.clip(new ClipContext(start, intendedEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, caster));
		Vec3 beam = hit.getLocation().subtract(start);
		double beamLength = beam.length();
		if (beamLength <= 0.0001) {
			return;
		}
		Vec3 forward = beam.normalize();
		Vec3 referenceUp = Math.abs(forward.y()) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
		Vec3 right = forward.cross(referenceUp).normalize();
		Vec3 up = right.cross(forward).normalize();
		int steps = Math.max(1, (int) Math.ceil(beamLength));
		int particlesPerStep = (int) Math.max(Math.pow(radius, 4), 5);
		for (int step = 0; step <= steps; step++) {
			double distance = Math.min(step, beamLength);
			Vec3 center = start.add(forward.scale(distance));
			for (int particle = 0; particle < particlesPerStep; particle++) {
				double angle = caster.getRandom().nextDouble() * Math.PI * 2;
				double radialDistance = Math.sqrt(caster.getRandom().nextDouble()) * radius;
				Vec3 position = center.add(right.scale(Math.cos(angle) * radialDistance)).add(up.scale(Math.sin(angle) * radialDistance));
				level.sendParticles(ParticleTypes.DRAGON_BREATH, position.x(), position.y(), position.z(), 1, 0, 0, 0, 0);
			}
		}
	}

	private static void castFreezeCoffin(SpellTriggerContext context) {
		Entity caster = context.get("entity");
		if (!(caster.level() instanceof ServerLevel level)) {
			return;
		}
		double x = context.get("x");
		double y = context.get("y");
		double z = context.get("z");
		double spellControl = context.get("spellControl");
		double spellStrength = context.get("spellStrength");
		double radius = spellControl / 10 + 1;
		double speed = radius / 10;
		for (int degree = 0; degree < 360; degree++) {
			double angle = Math.toRadians(degree);
			level.sendParticles(ParticleTypes.SNOWFLAKE, x, y + 0.25, z, 0, Math.cos(angle) * speed, 0.1, Math.sin(angle) * speed, 1);
			level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0, Math.cos(angle) * speed / 1.5, 0, Math.sin(angle) * speed / 1.5, 1);
			for (int offset = 0; offset < (int) radius; offset++) {
				double targetX = Math.cos(angle) * offset + x;
				double targetZ = Math.sin(angle) * offset + z;
				BlockPos floor = BlockPos.containing(targetX, y - 1, targetZ);
				BlockPos target = BlockPos.containing(targetX, y, targetZ);
				if (level.getBlockState(floor).canOcclude() && level.getBlockState(target).isAir()) {
					level.setBlock(target, Blocks.SNOW.defaultBlockState(), 3);
				} else if (level.getBlockState(floor).is(Blocks.WATER)) {
					level.setBlock(floor, Blocks.FROSTED_ICE.defaultBlockState(), 3);
				}
				Vec3 center = new Vec3(targetX, y, targetZ);
				for (Entity targetEntity : level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(0.5), entity -> entity != caster)
						.stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList()) {
					targetEntity.clearFire();
					targetEntity.makeStuckInBlock(Blocks.AIR.defaultBlockState(), new Vec3(0.25, 0.05, 0.25));
					targetEntity.setTicksFrozen((int) (70 + 5 * spellStrength));
					if (targetEntity instanceof LivingEntity living) {
						living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, (int) (70 + 5 * spellStrength), 1, true, true));
					}
				}
			}
		}
	}
}