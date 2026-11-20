package emmy.eminentmagic.procedures;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

import java.util.Comparator;

import emmy.eminentmagic.init.EminentMagicModAttributes;

@EventBusSubscriber
public class ActivePerceptionProcedure {
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		execute(event, event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		execute(null, world, x, y, z, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (entity.getPersistentData().getBooleanOr("activePerception", false) && world.isClientSide()) {
			{
				final Vec3 _center = new Vec3(x, y, z);
				for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(Math.min(Math.max(20,
						(entity instanceof LivingEntity _livingEntity1 && _livingEntity1.getAttributes().hasAttribute(EminentMagicModAttributes.MANA_PERCEPTION) ? _livingEntity1.getAttribute(EminentMagicModAttributes.MANA_PERCEPTION).getValue() : 0)
								* 2),
						128) / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList()) {
					if (!(entity == entityiterator)) {
						RenderManaProcedure.execute(world, entityiterator);
					}
				}
			}
		}
		if (entity.getPersistentData().getBooleanOr("activePerception", false) && !world.isClientSide()) {
			if (!(entity != null && emmy.eminentmagic.mana.ManaSpendingService.tryConsume(entity.level(), entity, 1))) {
				entity.getPersistentData().putBoolean("activePerception", false);
			}
		}
	}
}