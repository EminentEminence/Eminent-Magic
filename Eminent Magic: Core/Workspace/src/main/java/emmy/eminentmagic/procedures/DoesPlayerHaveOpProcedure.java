package emmy.eminentmagic.procedures;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

public class DoesPlayerHaveOpProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		return entity instanceof Player _playerCmd0 && _playerCmd0.hasPermissions(4);
	}
}