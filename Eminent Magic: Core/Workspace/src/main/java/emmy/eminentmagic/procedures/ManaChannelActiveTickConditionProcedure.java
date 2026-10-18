package emmy.eminentmagic.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import emmy.eminentmagic.init.EminentMagicModMobEffects;

public class ManaChannelActiveTickConditionProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if (IsHoldingStaffProcedure.execute(entity) && !((entity instanceof LivingEntity _entUseItem0 ? _entUseItem0.getUseItem() : ItemStack.EMPTY).getItem() == ItemStack.EMPTY.getItem())) {
			entity.getPersistentData().putBoolean("spellCancelled", false);
			ConsumeManaProcedure.execute(world, entity, entity.getPersistentData().getDoubleOr("manaChannelRate", 0));
		} else {
			entity.getPersistentData().putBoolean("spellCancelled", true);
			if (entity instanceof LivingEntity _entity)
				_entity.removeEffect(EminentMagicModMobEffects.MANA_CHANNEL);
		}
	}
}