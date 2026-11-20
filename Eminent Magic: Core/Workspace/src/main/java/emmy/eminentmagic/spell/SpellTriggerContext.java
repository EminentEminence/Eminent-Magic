package emmy.eminentmagic.spell;

import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import emmy.eminentmagic.network.EminentMagicModVariables;

public final class SpellTriggerContext {
	private final Map<String, Object> dependencies;

	private SpellTriggerContext(Map<String, Object> dependencies) {
		this.dependencies = Map.copyOf(dependencies);
	}

	public static SpellTriggerContext create(Entity caster, ItemStack spellStack) {
		BlockPos position = caster.blockPosition();
		BlockState blockState = caster.level().getBlockState(position);
		double spellControl = 0;
		double spellStrength = 0;
		if (caster instanceof Player player) {
			var variables = player.getData(EminentMagicModVariables.PLAYER_VARIABLES);
			spellControl = variables.spellControl;
			spellStrength = variables.spellStrength;
		}
		return new SpellTriggerContext(Map.<String, Object>ofEntries(
				Map.entry("x", caster.getX()),
				Map.entry("y", caster.getY()),
				Map.entry("z", caster.getZ()),
				Map.entry("world", caster.level()),
				Map.entry("entity", caster),
				Map.entry("sourceentity", caster),
				Map.entry("itemstack", spellStack == null ? ItemStack.EMPTY : spellStack),
				Map.entry("blockstate", blockState),
				Map.entry("direction", caster.getDirection()),
				Map.entry("spellControl", spellControl),
				Map.entry("spellStrength", spellStrength)));
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String dependency) {
		return (T) dependencies.get(dependency);
	}
}