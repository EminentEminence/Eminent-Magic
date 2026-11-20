package emmy.eminentmagic.procedures;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import emmy.eminentmagic.init.EminentMagicModMobEffects;

public class ManaChannelActiveTickConditionProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (IsHoldingStaffProcedure.execute(entity) && !((entity instanceof LivingEntity _entUseItem0 ? _entUseItem0.getUseItem() : ItemStack.EMPTY).getItem() == ItemStack.EMPTY.getItem())) {
			entity.getPersistentData().putBoolean("spellCancelled", false);
			if (!(entity != null && emmy.eminentmagic.mana.ManaSpendingService.tryConsume(entity.level(), entity, (entity.getPersistentData().getDoubleOr("manaChannelRate", 0))))) {
				entity.getPersistentData().putBoolean("spellCancelled", true);
				if (entity instanceof LivingEntity _entity)
					_entity.removeEffect(EminentMagicModMobEffects.MANA_CHANNEL);
			}
		} else {
			entity.getPersistentData().putBoolean("spellCancelled", true);
			if (entity instanceof LivingEntity _entity)
				_entity.removeEffect(EminentMagicModMobEffects.MANA_CHANNEL);
		}
	}
}