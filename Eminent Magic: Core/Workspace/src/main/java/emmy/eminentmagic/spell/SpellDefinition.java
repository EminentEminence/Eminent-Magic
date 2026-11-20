package emmy.eminentmagic.spell;

import java.util.Objects;

import net.minecraft.resources.ResourceLocation;

public record SpellDefinition(ResourceLocation id, String displayName, double baseCost, double strengthScaling, double controlScaling,
		String element, boolean channelled, String onCast, String whileCharging, String whileChannelling) {
	public SpellDefinition {
		Objects.requireNonNull(id);
		if (displayName == null || displayName.isBlank() || displayName.length() > 256
				|| element == null || element.isBlank() || element.length() > 256
				|| !Double.isFinite(baseCost) || baseCost < 0
				|| !Double.isFinite(strengthScaling) || !Double.isFinite(controlScaling)
				|| !validHook(onCast) || !validHook(whileCharging) || !validHook(whileChannelling)) {
			throw new IllegalArgumentException("Invalid spell definition " + id);
		}
	}

	private static boolean validHook(String hook) {
		return hook == null || hook.length() <= 256;
	}
}