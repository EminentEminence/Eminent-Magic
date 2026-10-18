package emmy.eminentmagic.procedures;

import net.minecraft.world.phys.Vec3;

public class DotProductProcedure {
	public static double execute(Vec3 VectorA, Vec3 VectorB) {
		if (VectorA == null || VectorB == null)
			return 0;
		return VectorA.x() * VectorB.x() + VectorA.y() * VectorB.y() + VectorA.z() * VectorB.z();
	}
}