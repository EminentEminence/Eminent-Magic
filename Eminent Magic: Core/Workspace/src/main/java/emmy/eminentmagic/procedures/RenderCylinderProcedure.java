package emmy.eminentmagic.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;

public class RenderCylinderProcedure {
	public static Vec3 execute(double height, double radius) {
		double angleDeg = 0;
		double partialRadius = 0;
		partialRadius = Mth.nextDouble(RandomSource.create(), 0, radius);
		angleDeg = Mth.nextInt(RandomSource.create(), 0, 360);
		return new Vec3((partialRadius * Math.cos(Math.toRadians(angleDeg))), (Mth.nextDouble(RandomSource.create(), 0, height)), (partialRadius * Math.sin(Math.toRadians(angleDeg))));
	}
}