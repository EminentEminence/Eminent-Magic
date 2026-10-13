package net.emmy.eminentmagic.core;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@EventBusSubscriber(modid = EminentMagicCoreMod.MODID)
public final class EminentMagicCoreSpellListCommand {
	private EminentMagicCoreSpellListCommand() {
	}

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("spelllist").requires(source -> source.hasPermission(4)).executes(context -> {
			List<String> lines = new ArrayList<>();
			lines.add("Spell definitions:");
			var spells = EminentMagicCoreSpellRegistry.getAll();
			for (var spell : spells) {
				lines.add(String.format(Locale.ROOT, "%s (%s): baseCost=%s, controlScaling=%s, strengthScaling=%s, element=%s",
						spell.spellName(), spell.id(), spell.baseCost(), spell.controlScaling(), spell.strengthScaling(), spell.element()));
			}
			if (spells.isEmpty()) {
				lines.add("No packaged spell definitions found.");
			}
			context.getSource().sendSuccess(() -> Component.literal(String.join("\n", lines)), false);
			return spells.size();
		}));
	}
}