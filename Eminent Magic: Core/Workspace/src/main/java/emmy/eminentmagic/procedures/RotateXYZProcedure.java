package emmy.eminentmagic.procedures;

import net.minecraft.world.phys.Vec3;

public class RotateXYZProcedure {
	public static Vec3 execute(double X, double Y, double Z, double aX, double aY, double aZ) {
		double cX = 0;
		double cY = 0;
		double cZ = 0;
		double sX = 0;
		double sY = 0;
		double sZ = 0;
		double y1 = 0;
		double z1 = 0;
		double x1 = 0;
		double y2 = 0;
		double z2 = 0;
		double x2 = 0;
		cX = Math.cos(aX);
		sX = Math.sin(aX);
		cY = Math.cos(aY);
		sY = Math.sin(aY);
		cZ = Math.cos(aZ);
		sZ = Math.sin(aZ);
		y1 = Y * cX - Z * sX;
		z1 = Y * sX + Z * cX;
		x1 = X;
		x2 = x1 * cY + z1 * sY;
		z2 = z1 * cY - x1 * sY;
		y2 = y1;
		return new Vec3((x2 * cZ - y2 * sZ), (x2 * sZ + y2 * cZ), z2);
	}
}