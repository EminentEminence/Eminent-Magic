package emmy.eminentmagic.procedures;

import org.checkerframework.checker.units.qual.t;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;

import emmy.eminentmagic.network.EminentMagicModVariables;

public class OnBarrierCollisionProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, double barrierIndex, double t, Vec3 barrierNormal, Vec3 hitPoint, Vec3 movement) {
		if (entity == null || barrierNormal == null || hitPoint == null || movement == null)
			return;
		Vec3 remainingMovement = Vec3.ZERO;
		Vec3 tangentialMovement = Vec3.ZERO;
		Vec3 clampPos = Vec3.ZERO;
		com.google.gson.JsonObject barrierJsonObject = new com.google.gson.JsonObject();
		double damageToApply = 0;
		double barrierMana = 0;
		double remainingMana = 0;
		barrierJsonObject = EminentMagicModVariables.OverworldBarriers.get((int) barrierIndex).getAsJsonObject();
		if (entity instanceof Projectile) {
			assert Boolean.TRUE; //#dbg:OnBarrierCollision:StopMovement
			remainingMovement = movement.scale((1 - t));
			tangentialMovement = remainingMovement.subtract((barrierNormal.scale(DotProductProcedure.execute(barrierNormal, remainingMovement))));
			if (entity instanceof AbstractArrow arrow) {
				damageToApply = Math.ceil(2 * arrow.getDeltaMovement().length());
			}
			entity.setDeltaMovement(new Vec3((tangentialMovement.x()), (tangentialMovement.y()), (tangentialMovement.z())));
			clampPos = hitPoint.add((barrierNormal.scale(0.05)));
			{
				Entity _ent = entity;
				_ent.teleportTo((clampPos.x()), (clampPos.y()), (clampPos.z()));
				if (_ent instanceof ServerPlayer _serverPlayer)
					_serverPlayer.connection.teleport((clampPos.x()), (clampPos.y()), (clampPos.z()), _ent.getYRot(), _ent.getXRot());
			}
			if (world instanceof ServerLevel _level)
				_level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, (hitPoint.x()), (hitPoint.y()), (hitPoint.z()), 3, 0.05, 0.05, 0.05, 0.01);
			if (entity instanceof net.minecraft.world.entity.projectile.Fireball fireball) {
				fireball.level().explode(fireball, fireball.getX(), fireball.getY(), fireball.getZ(), 0.1F, Level.ExplosionInteraction.NONE);
			}
			entity.discard();
		}
		barrierMana = barrierJsonObject.get("mana").getAsDouble();
		barrierJsonObject.remove("mana");
		remainingMana = barrierMana - damageToApply;
		if (remainingMana <= 0) {
			ShatterBarrierProcedure.execute(world, x, y, z, Math.round(barrierIndex));
		}
		barrierJsonObject.addProperty("mana", remainingMana);
	}
}