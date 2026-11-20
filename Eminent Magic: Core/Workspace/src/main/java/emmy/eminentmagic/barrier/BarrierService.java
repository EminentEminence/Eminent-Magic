package emmy.eminentmagic.barrier;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import emmy.eminentmagic.init.EminentMagicModAttributes;
import emmy.eminentmagic.init.BarrierGameRules;
import emmy.eminentmagic.procedures.BarrierShatterEffectProcedure;
import emmy.eminentmagic.spell.SpellTriggerDispatcher;

public final class BarrierService {
	public enum NumericProperty {
		POSITION_X,
		POSITION_Y,
		POSITION_Z,
		ROTATION_X,
		ROTATION_Y,
		ROTATION_Z,
		RADIUS,
		HEALTH
	}

	private BarrierService() {
	}

	public static Barrier create(LevelAccessor world, Entity caster, Vec3 position, Vec3 rotation, double radius, double health) {
		if (!(world instanceof ServerLevel level) || !(caster instanceof ServerPlayer player) || player.level() != level
				|| position == null || rotation == null || !isFinite(position) || !isFinite(rotation)
				|| !Double.isFinite(radius) || radius <= 0 || !Double.isFinite(health) || health <= 0) {
			return null;
		}

		BarrierHit pickupHit = findNearestHit(level, player.getEyePosition(), position, player.getUUID()).orElse(null);
		if (pickupHit != null && shatter(level, pickupHit.barrier())) {
			double castCost = SpellTriggerDispatcher.currentManaCostPaid(player).orElse(health);
			refundMana(player, pickupHit.barrier().health() * 0.85D + castCost);
			return null;
		}

		BarrierSavedData data = BarrierSavedData.get(level);
		Barrier barrier = new Barrier(UUID.randomUUID(), position.x, position.y, position.z,
				Mth.wrapDegrees(rotation.x), Mth.wrapDegrees(rotation.y), Mth.wrapDegrees(rotation.z),
				radius, health, player.getUUID());
		data.put(barrier);
		return barrier;
	}

	public static List<Barrier> getBarriers(LevelAccessor world) {
		return world instanceof ServerLevel level ? BarrierSavedData.get(level).barriers() : List.of();
	}

	public static Optional<Barrier> getBarrier(LevelAccessor world, String id) {
		return parseId(id).flatMap(parsedId -> getBarriers(world).stream().filter(barrier -> barrier.id().equals(parsedId)).findFirst());
	}

	public static boolean remove(LevelAccessor world, Barrier barrier) {
		return world instanceof ServerLevel level && barrier != null && BarrierSavedData.get(level).remove(barrier.id());
	}

	public static boolean removeById(LevelAccessor world, String id) {
		return world instanceof ServerLevel level && parseId(id).map(parsedId -> BarrierSavedData.get(level).remove(parsedId)).orElse(false);
	}

	public static boolean removeAtIndex(LevelAccessor world, int index) {
		if (!(world instanceof ServerLevel level)) {
			return false;
		}
		List<Barrier> barriers = BarrierSavedData.get(level).barriers();
		if (index < 0 || index >= barriers.size()) {
			return false;
		}
		return BarrierSavedData.get(level).remove(barriers.get(index).id());
	}

	public static int clear(LevelAccessor world) {
		return world instanceof ServerLevel level ? BarrierSavedData.get(level).clear() : 0;
	}

	public static boolean setPosition(LevelAccessor world, Barrier barrier, double x, double y, double z) {
		if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
			return false;
		}
		return update(world, barrier, current -> current.withPosition(x, y, z));
	}

	public static boolean setPositionX(LevelAccessor world, Barrier barrier, double value) {
		return Double.isFinite(value) && update(world, barrier,
				current -> current.withPosition(value, current.y(), current.z()));
	}

	public static boolean setPositionY(LevelAccessor world, Barrier barrier, double value) {
		return Double.isFinite(value) && update(world, barrier,
				current -> current.withPosition(current.x(), value, current.z()));
	}

	public static boolean setPositionZ(LevelAccessor world, Barrier barrier, double value) {
		return Double.isFinite(value) && update(world, barrier,
				current -> current.withPosition(current.x(), current.y(), value));
	}

	public static boolean setRotation(LevelAccessor world, Barrier barrier, double x, double y, double z) {
		if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
			return false;
		}
		return update(world, barrier,
				current -> current.withRotation(Mth.wrapDegrees(x), Mth.wrapDegrees(y), Mth.wrapDegrees(z)));
	}

	public static boolean setRotationX(LevelAccessor world, Barrier barrier, double value) {
		return Double.isFinite(value) && update(world, barrier,
				current -> current.withRotation(Mth.wrapDegrees(value), current.rotationY(), current.rotationZ()));
	}

	public static boolean setRotationY(LevelAccessor world, Barrier barrier, double value) {
		return Double.isFinite(value) && update(world, barrier,
				current -> current.withRotation(current.rotationX(), Mth.wrapDegrees(value), current.rotationZ()));
	}

	public static boolean setRotationZ(LevelAccessor world, Barrier barrier, double value) {
		return Double.isFinite(value) && update(world, barrier,
				current -> current.withRotation(current.rotationX(), current.rotationY(), Mth.wrapDegrees(value)));
	}

	public static boolean setRadius(LevelAccessor world, Barrier barrier, double radius) {
		return Double.isFinite(radius) && radius > 0 && update(world, barrier, current -> current.withRadius(radius));
	}

	public static boolean setHealth(LevelAccessor world, Barrier barrier, double health) {
		if (!(world instanceof ServerLevel level) || barrier == null || !Double.isFinite(health) || health < 0) {
			return false;
		}
		if (health == 0) {
			return shatter(level, barrier);
		}
		return update(level, barrier, current -> current.withHealth(health));
	}

	public static double getNumericProperty(Barrier barrier, NumericProperty property) {
		if (barrier == null || property == null) {
			return 0.0D;
		}
		return switch (property) {
			case POSITION_X -> barrier.x();
			case POSITION_Y -> barrier.y();
			case POSITION_Z -> barrier.z();
			case ROTATION_X -> barrier.rotationX();
			case ROTATION_Y -> barrier.rotationY();
			case ROTATION_Z -> barrier.rotationZ();
			case RADIUS -> barrier.radius();
			case HEALTH -> barrier.health();
		};
	}

	public static boolean setNumericProperty(LevelAccessor world, Barrier barrier, NumericProperty property, double value) {
		if (barrier == null || property == null || !Double.isFinite(value)) {
			return false;
		}
		return switch (property) {
			case POSITION_X -> setPositionX(world, barrier, value);
			case POSITION_Y -> setPositionY(world, barrier, value);
			case POSITION_Z -> setPositionZ(world, barrier, value);
			case ROTATION_X -> setRotationX(world, barrier, value);
			case ROTATION_Y -> setRotationY(world, barrier, value);
			case ROTATION_Z -> setRotationZ(world, barrier, value);
			case RADIUS -> setRadius(world, barrier, value);
			case HEALTH -> setHealth(world, barrier, value);
		};
	}

	public static Optional<BarrierHit> findNearestHit(LevelAccessor world, Vec3 start, Vec3 end) {
		return findNearestHit(world, start, end, null);
	}

	private static Optional<BarrierHit> findNearestHit(LevelAccessor world, Vec3 start, Vec3 end, UUID owner) {
		if (start == null || end == null || !isFinite(start) || !isFinite(end)) {
			return Optional.empty();
		}
		AABB movementBounds = new AABB(start, end).inflate(1.0D);
		return getBarriers(world).stream()
				.filter(barrier -> owner == null || barrier.owner().equals(owner))
				.filter(barrier -> BarrierGeometry.bounds(barrier).intersects(movementBounds))
				.map(barrier -> BarrierGeometry.intersect(barrier, start, end)
						.map(hit -> new BarrierHit(barrier, hit.segmentT(), hit.point())))
				.flatMap(Optional::stream)
				.min(Comparator.comparingDouble(BarrierHit::segmentT));
	}

	public static double projectileImpactDamage(double speed) {
		return Double.isFinite(speed) && speed > 0 ? Math.ceil(2.0D * speed) : 0.0D;
	}

	public static boolean damage(LevelAccessor world, Barrier barrier, double damage) {
		if (!(world instanceof ServerLevel level) || barrier == null || !Double.isFinite(damage) || damage <= 0) {
			return false;
		}
		Barrier current = BarrierSavedData.get(level).get(barrier.id());
		if (current == null) {
			return false;
		}
		double remainingHealth = current.health() - damage;
		if (remainingHealth <= 0) {
			return shatter(level, current);
		}
		BarrierSavedData.get(level).put(current.withHealth(remainingHealth));
		return true;
	}

	public static void degrade(ServerLevel level, int healthPerSecond) {
		if (healthPerSecond <= 0) {
			return;
		}
		for (Barrier barrier : BarrierSavedData.get(level).barriers()) {
			damage(level, barrier, healthPerSecond);
		}
	}

	public static boolean shatter(LevelAccessor world, Barrier barrier) {
		return world instanceof ServerLevel level && barrier != null && shatter(level, barrier);
	}

	public static void applyDegradationRule(ServerLevel level) {
		int healthPerSecond = level.getGameRules().getInt(BarrierGameRules.BARRIER_DEGRADATION_RATE);
		degrade(level, healthPerSecond);
	}

	private static boolean update(LevelAccessor world, Barrier barrier, UnaryOperator<Barrier> update) {
		if (!(world instanceof ServerLevel level) || barrier == null) {
			return false;
		}
		BarrierSavedData data = BarrierSavedData.get(level);
		Barrier current = data.get(barrier.id());
		if (current == null) {
			return false;
		}
		try {
			data.put(update.apply(current));
			return true;
		} catch (IllegalArgumentException exception) {
			return false;
		}
	}

	private static boolean shatter(ServerLevel level, Barrier barrier) {
		Barrier current = BarrierSavedData.get(level).get(barrier.id());
		if (current == null || !BarrierSavedData.get(level).remove(current.id())) {
			return false;
		}
		BarrierGeometry.PlaneBasis basis = BarrierGeometry.basis(current);
		Vec3 normal = basis.normal();
		BarrierShatterEffectProcedure.execute(level, current.x(), current.y(), current.z(),
				normal.x, normal.y, normal.z, current.radius(), 24);
		return true;
	}

	private static void refundMana(ServerPlayer player, double refund) {
		if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE || !Double.isFinite(refund) || refund <= 0) {
			return;
		}
		var currentMana = player.getAttribute(EminentMagicModAttributes.MANA_CURRENT);
		var maxMana = player.getAttribute(EminentMagicModAttributes.MANA_RESERVES);
		if (currentMana == null || maxMana == null) {
			return;
		}
		double cap = maxMana.getValue();
		double current = currentMana.getValue();
		if (!Double.isFinite(cap) || cap < 0 || !Double.isFinite(current)) {
			return;
		}
		currentMana.setBaseValue(Math.min(cap, Math.max(0.0D, current) + refund));
	}

	private static Optional<UUID> parseId(String id) {
		if (id == null) {
			return Optional.empty();
		}
		try {
			return Optional.of(UUID.fromString(id));
		} catch (IllegalArgumentException exception) {
			return Optional.empty();
		}
	}

	private static boolean isFinite(Vec3 vector) {
		return Double.isFinite(vector.x) && Double.isFinite(vector.y) && Double.isFinite(vector.z);
	}

	public record BarrierHit(Barrier barrier, double segmentT, Vec3 point) {
	}
}
