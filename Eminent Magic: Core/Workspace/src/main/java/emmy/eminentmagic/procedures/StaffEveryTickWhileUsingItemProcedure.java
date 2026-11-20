package emmy.eminentmagic.procedures;

import net.minecraft.world.entity.Entity;

public class StaffEveryTickWhileUsingItemProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		entity.getPersistentData().putBoolean("chargingSpell", true);
	}
}