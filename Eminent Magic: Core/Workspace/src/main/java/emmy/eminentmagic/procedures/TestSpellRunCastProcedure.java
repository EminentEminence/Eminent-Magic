package emmy.eminentmagic.procedures;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

public class TestSpellRunCastProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		emmy.eminentmagic.spell.SpellDefinition test = null;
		emmy.eminentmagic.spell.SpellDefinition test2 = null;
		test = emmy.eminentmagic.spell.SpellCatalog.getById("trigger_probe").orElse(null);
		if (entity instanceof Player _player && !_player.level().isClientSide())
			_player.displayClientMessage(Component.literal(("" + (test != null))), false);
		if (entity instanceof Player _player && !_player.level().isClientSide())
			_player.displayClientMessage(Component.literal(("" + java.util.Optional.ofNullable(test).map(emmy.eminentmagic.spell.SpellDefinition::displayName).orElse(""))), false);
		if (entity instanceof Player _player && !_player.level().isClientSide())
			_player.displayClientMessage(Component.literal(("" + java.util.Optional.ofNullable(test).map(emmy.eminentmagic.spell.SpellDefinition::element).orElse(""))), false);
		if (entity instanceof Player _player && !_player.level().isClientSide())
			_player.displayClientMessage(Component.literal(("" + java.util.Optional.ofNullable(test).map(emmy.eminentmagic.spell.SpellDefinition::baseCost).orElse(0.0))), false);
		if (entity instanceof Player _player && !_player.level().isClientSide())
			_player.displayClientMessage(Component.literal(("" + java.util.Optional.ofNullable(test).map(emmy.eminentmagic.spell.SpellDefinition::strengthScaling).orElse(0.0))), false);
		if (entity instanceof Player _player && !_player.level().isClientSide())
			_player.displayClientMessage(Component.literal(("" + java.util.Optional.ofNullable(test).map(emmy.eminentmagic.spell.SpellDefinition::controlScaling).orElse(0.0))), false);
		if (entity instanceof Player _player && !_player.level().isClientSide())
			_player.displayClientMessage(Component.literal(("" + java.util.Optional.ofNullable(test).map(emmy.eminentmagic.spell.SpellDefinition::channelled).orElse(false))), false);
		test2 = emmy.eminentmagic.spell.SpellCatalog.getById("eminent_magic:missing_spell").orElse(null);
		if (entity instanceof Player _player && !_player.level().isClientSide())
			_player.displayClientMessage(Component.literal(("" + (test2 != null))), false);
		if (entity instanceof Player _player && !_player.level().isClientSide())
			_player.displayClientMessage(Component.literal(("" + java.util.Optional.ofNullable(test2).map(emmy.eminentmagic.spell.SpellDefinition::displayName).orElse(""))), false);
		if (entity instanceof Player _player && !_player.level().isClientSide())
			_player.displayClientMessage(Component.literal(("" + java.util.Optional.ofNullable(test2).map(emmy.eminentmagic.spell.SpellDefinition::element).orElse(""))), false);
		if (entity instanceof Player _player && !_player.level().isClientSide())
			_player.displayClientMessage(Component.literal(("" + java.util.Optional.ofNullable(test2).map(emmy.eminentmagic.spell.SpellDefinition::baseCost).orElse(0.0))), false);
		if (entity instanceof Player _player && !_player.level().isClientSide())
			_player.displayClientMessage(Component.literal(("" + java.util.Optional.ofNullable(test2).map(emmy.eminentmagic.spell.SpellDefinition::strengthScaling).orElse(0.0))), false);
		if (entity instanceof Player _player && !_player.level().isClientSide())
			_player.displayClientMessage(Component.literal(("" + java.util.Optional.ofNullable(test2).map(emmy.eminentmagic.spell.SpellDefinition::controlScaling).orElse(0.0))), false);
		if (entity instanceof Player _player && !_player.level().isClientSide())
			_player.displayClientMessage(Component.literal(("" + java.util.Optional.ofNullable(test2).map(emmy.eminentmagic.spell.SpellDefinition::channelled).orElse(false))), false);
	}
}