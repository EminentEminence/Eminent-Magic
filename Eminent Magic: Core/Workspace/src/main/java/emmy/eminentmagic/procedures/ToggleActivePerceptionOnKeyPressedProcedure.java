package emmy.eminentmagic.procedures;

import net.minecraft.world.entity.Entity;

public class ToggleActivePerceptionOnKeyPressedProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity.getPersistentData().getBooleanOr("activePerception", false)) {
			entity.getPersistentData().putBoolean("activePerception", false);
		} else {
			entity.getPersistentData().putBoolean("activePerception", true);
		}
	}
}