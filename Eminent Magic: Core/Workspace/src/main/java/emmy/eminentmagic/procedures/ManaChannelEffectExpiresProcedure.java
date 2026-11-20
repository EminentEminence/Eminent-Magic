package emmy.eminentmagic.procedures;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import emmy.eminentmagic.init.EminentMagicModMobEffects;

public class ManaChannelEffectExpiresProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(EminentMagicModMobEffects.MANA_CHANNEL) && !entity.getPersistentData().getBooleanOr("spellCancelled", false)) {
			if (entity instanceof Player _player)
				_player.getCooldowns().addCooldown((entity instanceof LivingEntity _entUseItem2 ? _entUseItem2.getUseItem() : ItemStack.EMPTY), 10);
			if (entity instanceof LivingEntity living) {
				living.stopUsingItem();
			}
			CastSpellProcedure.execute();
		}
	}
}