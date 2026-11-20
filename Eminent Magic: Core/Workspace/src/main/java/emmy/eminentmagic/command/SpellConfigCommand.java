package emmy.eminentmagic.command;

import java.io.IOException;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import emmy.eminentmagic.EminentMagicMod;
import emmy.eminentmagic.spell.SpellCatalog;

@EventBusSubscriber
public final class SpellConfigCommand {
	private SpellConfigCommand() {
	}

	@SubscribeEvent
	public static void register(RegisterCommandsEvent event) {
		LiteralArgumentBuilder<CommandSourceStack> spells = Commands.literal("spells")
				.then(Commands.literal("reload").executes(context -> reload(context.getSource(), false)))
				.then(Commands.literal("reset").executes(context -> reload(context.getSource(), true)));
		event.getDispatcher().register(
				Commands.literal("eminentmagic")
						.requires(source -> source.hasPermission(2))
						.then(spells));
	}

	private static int reload(CommandSourceStack source, boolean reset) {
		if (reset) {
			try {
				SpellCatalog.resetConfig();
			} catch (IOException | RuntimeException exception) {
				EminentMagicMod.LOGGER.error("Could not reset spell config", exception);
				source.sendFailure(Component.literal("Could not reset spell config; see the server log."));
				return 0;
			}
		}

		var server = source.getServer();
		source.sendSuccess(() -> Component.literal(reset ? "Resetting and reloading spell data..." : "Reloading spell data..."), true);
		server.reloadResources(server.getPackRepository().getSelectedIds()).whenComplete((ignored, exception) ->
				server.execute(() -> {
					if (exception != null) {
						EminentMagicMod.LOGGER.error("Could not reload spell data", exception);
						source.sendFailure(Component.literal("Could not reload spell data; see the server log."));
					} else {
						source.sendSuccess(() -> Component.literal("Spell data reloaded and synchronized."), true);
					}
				}));
		return 1;
	}
}
