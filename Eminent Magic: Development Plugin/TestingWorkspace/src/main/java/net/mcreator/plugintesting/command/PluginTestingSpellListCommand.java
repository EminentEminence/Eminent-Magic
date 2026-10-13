package net.mcreator.plugintesting.command;

import net.mcreator.plugintesting.PluginTestingSpellConfig;
import com.google.gson.JsonObject;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PluginTestingSpellListCommand {
	private PluginTestingSpellListCommand() {
	}

	@SubscribeEvent
	public static void registerCommand(RegisterCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("spelllist").requires(source -> source.hasPermission(4)).executes(context -> {
			List<String> lines = new ArrayList<>();
			lines.add("Spell definitions:");
			var spells = PluginTestingSpellConfig.getAll();
			for (var entry : spells) {
				JsonObject spell = entry.getValue();
				lines.add(String.format(Locale.ROOT, "%s (%s): manaCost=%s, controlScaling=%s, strengthScaling=%s, element=%s",
						spell.get("spellName").getAsString(), entry.getKey(), spell.get("manaCost").getAsString(),
						spell.get("controlScaling").getAsString(), spell.get("strengthScaling").getAsString(), spell.get("element").getAsString()));
			}
			if (spells.isEmpty()) {
				lines.add("No generated spell JSON resources found under data/plugin_testing/spells. Regenerate code after adding Spell elements.");
			}
			context.getSource().sendSuccess(() -> Component.literal(String.join("\n", lines)), false);
			return spells.size();
		}));
	}
}