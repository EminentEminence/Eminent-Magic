package emmy.eminentmagic.spell;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import emmy.eminentmagic.item.SpellItem;
import emmy.eminentmagic.mana.ManaSpendingService;
import emmy.eminentmagic.network.EminentMagicModVariables;

public final class SpellCastingService {
	private SpellCastingService() {
	}

	public static double calculateManaCost(SpellDefinition spell, double spellControl, double spellStrength) {
		double controlFactor = spellControl * spell.controlScaling() / 10;
		double strengthFactor = spellStrength * spell.strengthScaling() / 10;
		return spell.baseCost() * controlFactor * strengthFactor + spell.baseCost();
	}

	public static boolean cast(Entity caster, ItemStack spellStack) {
		if (!(caster instanceof Player player) || player.level().isClientSide()) {
			return false;
		}
		return SpellItem.getSpellId(spellStack).flatMap(SpellCatalog::get)
				.map(spell -> cast(player, spellStack, spell)).orElse(false);
	}

	public static boolean cast(Entity caster, ItemStack spellStack, SpellDefinition spell) {
		if (!(caster instanceof Player player) || player.level().isClientSide() || spell == null
				|| SpellCatalog.get(spell.id()).filter(spell::equals).isEmpty()) {
			return false;
		}
		var variables = player.getData(EminentMagicModVariables.PLAYER_VARIABLES);
		double manaCost = calculateManaCost(spell, variables.spellControl, variables.spellStrength);
		double costToSpend = Math.max(0, manaCost);
		if (!Double.isFinite(manaCost) || !ManaSpendingService.tryConsume(player.level(), player, costToSpend)) {
			return false;
		}
		return SpellTriggerDispatcher.fire(player, spellStack, spell, SpellTrigger.ON_CAST, costToSpend);
	}
}