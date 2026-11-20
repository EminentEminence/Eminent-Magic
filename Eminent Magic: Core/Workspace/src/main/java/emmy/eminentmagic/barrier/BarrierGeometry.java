package emmy.eminentmagic.barrier;

import java.util.Optional;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BarrierGeometry {
	private static final double EPSILON = 1.0E-8D;
	private static final double TAU = Math.PI * 2.0D;

	private BarrierGeometry() {
	}

	public static Optional<SegmentHit> intersect(Barrier barrier, Vec3 start, Vec3 end) {
		if (barrier == null || start == null || end == null) {
			return Optional.empty();
		}
		PlaneBasis basis = basis(barrier);
		Vec3 relativeStart = start.subtract(barrier.position());
		Vec3 relativeEnd = end.subtract(barrier.position());
		double startDistance = relativeStart.dot(basis.normal());
		double endDistance = relativeEnd.dot(basis.normal());
		double denominator = startDistance - endDistance;
		if (Math.abs(denominator) < EPSILON) {
			return Optional.empty();
		}
		double segmentT = startDistance / denominator;
		if (segmentT < 0.0D || segmentT > 1.0D) {
			return Optional.empty();
		}

		Vec3 hitPoint = start.lerp(end, segmentT);
		Vec3 relativeHit = hitPoint.subtract(barrier.position());
		double localX = relativeHit.dot(basis.right());
		double localY = relativeHit.dot(basis.up());
		if (!isInsideHexagon(localX, localY, barrier.radius())) {
			return Optional.empty();
		}
		return Optional.of(new SegmentHit(segmentT, hitPoint));
	}

	public static boolean isInsideHexagon(double x, double y, double radius) {
		if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(radius) || radius <= 0) {
			return false;
		}
		double sign = 0.0D;
		for (int index = 0; index < 6; index++) {
			double angleA = TAU * index / 6.0D;
			double angleB = TAU * (index + 1) / 6.0D;
			double xA = radius * Math.cos(angleA);
			double yA = radius * Math.sin(angleA);
			double xB = radius * Math.cos(angleB);
			double yB = radius * Math.sin(angleB);
			double cross = (xB - xA) * (y - yA) - (yB - yA) * (x - xA);
			if (Math.abs(cross) <= EPSILON) {
				continue;
			}
			if (sign == 0.0D) {
				sign = Math.signum(cross);
			} else if (sign * cross < -EPSILON) {
				return false;
			}
		}
		return true;
	}

	public static PlaneBasis basis(Barrier barrier) {
		Quaternionf rotation = new Quaternionf().rotationXYZ(
				(float) Math.toRadians(barrier.rotationX()),
				(float) -Math.toRadians(barrier.rotationY()),
				(float) Math.toRadians(barrier.rotationZ()));
		return new PlaneBasis(
				rotate(rotation, new Vector3f(1.0F, 0.0F, 0.0F)),
				rotate(rotation, new Vector3f(0.0F, 1.0F, 0.0F)),
				rotate(rotation, new Vector3f(0.0F, 0.0F, 1.0F)));
	}

	public static AABB bounds(Barrier barrier) {
		return new AABB(barrier.position(), barrier.position()).inflate(barrier.radius());
	}

	public static Vec3 pointOnPerimeter(Barrier barrier, double edgePosition) {
		double wrappedPosition = edgePosition - Math.floor(edgePosition / 6.0D) * 6.0D;
		int edge = (int) Math.floor(wrappedPosition);
		double edgeT = wrappedPosition - edge;
		double angleA = TAU * edge / 6.0D;
		double angleB = TAU * ((edge + 1) % 6) / 6.0D;
		double xA = barrier.radius() * Math.cos(angleA);
		double yA = barrier.radius() * Math.sin(angleA);
		double xB = barrier.radius() * Math.cos(angleB);
		double yB = barrier.radius() * Math.sin(angleB);
		PlaneBasis basis = basis(barrier);
		return barrier.position()
				.add(basis.right().scale(xA * (1.0D - edgeT) + xB * edgeT))
				.add(basis.up().scale(yA * (1.0D - edgeT) + yB * edgeT));
	}

	private static Vec3 rotate(Quaternionf rotation, Vector3f vector) {
		Vector3f transformed = rotation.transform(vector);
		return new Vec3(transformed.x, transformed.y, transformed.z);
	}

	public record PlaneBasis(Vec3 right, Vec3 up, Vec3 normal) {
	}

	public record SegmentHit(double segmentT, Vec3 point) {
	}
}
