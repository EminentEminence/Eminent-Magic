package emmy.eminentmagic.spell;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

import emmy.eminentmagic.init.EminentMagicModAttributes;
import emmy.eminentmagic.network.EminentMagicModVariables;
import emmy.eminentmagic.network.EminentMagicModVariables.PlayerVariables;

public final class SpellControlService {
	private SpellControlService() {
	}

	public static void adjustStrength(Player player, int direction) {
		adjust(player, direction, true);
	}

	public static void adjustControl(Player player, int direction) {
		adjust(player, direction, false);
	}

	private static void adjust(Player player, int direction, boolean strength) {
		if (player == null || player.level().isClientSide() || direction == 0) {
			return;
		}

		AttributeInstance limitAttribute = player.getAttribute(
				strength ? EminentMagicModAttributes.MANA_STRENGTH : EminentMagicModAttributes.MANA_CONTROL);
		if (limitAttribute == null || !Double.isFinite(limitAttribute.getValue())) {
			return;
		}
		double maximum = Math.max(1, limitAttribute.getValue());

		PlayerVariables variables = player.getData(EminentMagicModVariables.PLAYER_VARIABLES);
		double current = strength ? variables.spellStrength : variables.spellControl;
		if (!Double.isFinite(current)) {
			current = 1;
		}
		double updated = Math.max(1, Math.min(maximum, current + Integer.signum(direction)));
		if (Double.compare(current, updated) == 0) {
			return;
		}

		if (strength) {
			variables.spellStrength = updated;
		} else {
			variables.spellControl = updated;
		}
		variables.markSyncDirty();
	}
}
