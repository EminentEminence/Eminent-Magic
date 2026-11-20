package emmy.eminentmagic.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.client.Minecraft;

import emmy.eminentmagic.network.EminentMagicModVariables;
import emmy.eminentmagic.init.EminentMagicModMobEffects;
import emmy.eminentmagic.init.EminentMagicModAttributes;
import emmy.eminentmagic.EminentMagicMod;

public class SpellSelectorProcedure {
	public static void execute(Entity entity, double spellID) {
		if (entity == null)
			return;
		com.google.gson.JsonObject spellData = new com.google.gson.JsonObject();
		double chargeTime = 0;
		double ManaCost = 0;
		EminentMagicMod.LOGGER.info(("Casting Spell. ID: " + new java.text.DecimalFormat("##").format(spellID)));
		spellData = GetSpellDataProcedure.execute(spellID);
		ManaCost = CalculateManaCostProcedure.execute(spellData.get("baseCost").getAsDouble(), spellData.get("costControlScaling").getAsDouble(), Minecraft.getInstance().player.getData(EminentMagicModVariables.PLAYER_VARIABLES).spellControl,
				Minecraft.getInstance().player.getData(EminentMagicModVariables.PLAYER_VARIABLES).spellStrength, spellData.get("costStrengthScaling").getAsDouble());
		chargeTime = ManaCost
				/ (entity instanceof LivingEntity _livingEntity4 && _livingEntity4.getAttributes().hasAttribute(EminentMagicModAttributes.MANA_CONTROL) ? _livingEntity4.getAttribute(EminentMagicModAttributes.MANA_CONTROL).getValue() : 0);
		entity.getPersistentData().putDouble("manaChannelRate", ((ManaCost / chargeTime) / 20));
		entity.getPersistentData().putDouble("channelSpellID", spellID);
		entity.getPersistentData().putDouble("timeChannelling", 0);
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(EminentMagicModMobEffects.MANA_CHANNEL, (int) (chargeTime * 20), 1, false, false));
	}
}