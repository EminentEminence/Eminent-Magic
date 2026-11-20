package emmy.eminentmagic.barrier;

import java.util.Objects;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

public record Barrier(UUID id, double x, double y, double z, double rotationX, double rotationY, double rotationZ, double radius,
		double health, UUID owner) {
	private static final Codec<UUID> UUID_CODEC = Codec.STRING.comapFlatMap(value -> {
		try {
			return DataResult.success(UUID.fromString(value));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(() -> "Invalid UUID: " + value);
		}
	}, UUID::toString);

	public static final Codec<Barrier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			UUID_CODEC.fieldOf("id").forGetter(Barrier::id),
			Codec.DOUBLE.fieldOf("x").forGetter(Barrier::x),
			Codec.DOUBLE.fieldOf("y").forGetter(Barrier::y),
			Codec.DOUBLE.fieldOf("z").forGetter(Barrier::z),
			Codec.DOUBLE.fieldOf("rotation_x").forGetter(Barrier::rotationX),
			Codec.DOUBLE.fieldOf("rotation_y").forGetter(Barrier::rotationY),
			Codec.DOUBLE.fieldOf("rotation_z").forGetter(Barrier::rotationZ),
			Codec.DOUBLE.fieldOf("radius").forGetter(Barrier::radius),
			Codec.DOUBLE.fieldOf("health").forGetter(Barrier::health),
			UUID_CODEC.fieldOf("owner").forGetter(Barrier::owner)
	).apply(instance, Barrier::new));

	public Barrier {
		Objects.requireNonNull(id);
		Objects.requireNonNull(owner);
		if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
				|| !Double.isFinite(rotationX) || !Double.isFinite(rotationY) || !Double.isFinite(rotationZ)
				|| !Double.isFinite(radius) || radius <= 0 || !Double.isFinite(health) || health <= 0) {
			throw new IllegalArgumentException("Barrier fields must be finite and radius and health must be positive");
		}
	}

	public Vec3 position() {
		return new Vec3(x, y, z);
	}

	public Barrier withPosition(double x, double y, double z) {
		return new Barrier(id, x, y, z, rotationX, rotationY, rotationZ, radius, health, owner);
	}

	public Barrier withRotation(double rotationX, double rotationY, double rotationZ) {
		return new Barrier(id, x, y, z, rotationX, rotationY, rotationZ, radius, health, owner);
	}

	public Barrier withRadius(double radius) {
		return new Barrier(id, x, y, z, rotationX, rotationY, rotationZ, radius, health, owner);
	}

	public Barrier withHealth(double health) {
		return new Barrier(id, x, y, z, rotationX, rotationY, rotationZ, radius, health, owner);
	}
}
