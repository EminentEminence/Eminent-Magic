package emmy.eminentmagic.procedures;

import net.minecraft.world.entity.Entity;

public class IsHoldingStaffProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		return emmy.eminentmagic.staff.StaffService.isHoldingStaff(entity);
	}
}