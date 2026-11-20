package emmy.eminentmagic.procedures;

import net.minecraft.world.entity.Entity;

public class StaffOnPlayerStoppedUsingProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		entity.getPersistentData().putBoolean("chargingSpell", false);
	}
}