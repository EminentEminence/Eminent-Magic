package emmy.eminentmagic.procedures;

import net.minecraft.world.entity.Entity;

import emmy.eminentmagic.mana.ManaSpendingService;

public class ConsumeManaProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		return ManaSpendingService.tryConsume(entity.level(), entity, entity.getPersistentData().getDoubleOr("manaChannelRate", 0));
	}
}
