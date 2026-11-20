package com.eminentmagic.development;

import net.mcreator.element.ModElementTypeLoader;
import net.mcreator.plugin.JavaPlugin;
import net.mcreator.plugin.Plugin;
import net.mcreator.plugin.events.PreGeneratorsLoadingEvent;
import net.mcreator.plugin.events.workspace.MCreatorLoadedEvent;
import net.mcreator.workspace.Workspace;
import net.mcreator.workspace.settings.WorkspaceSettings;

import java.util.HashSet;
import java.util.Set;

public final class EminentMagicPlugin extends JavaPlugin {
	public EminentMagicPlugin(Plugin plugin) {
		super(plugin);
		addListener(PreGeneratorsLoadingEvent.class, event -> {
			ModElementTypeLoader.register(SpellModElementType.SPELL);
			ModElementTypeLoader.register(StaffModElementType.STAFF);
		});
		addListener(MCreatorLoadedEvent.class, event -> {
			Workspace workspace = event.getMCreator().getWorkspace();
			WorkspaceSettings settings = workspace.getWorkspaceSettings();
			if (!"eminent_magic_world".equals(settings.getModID())
					|| settings.requiredMods.contains("eminent_magic"))
				return;

			Set<String> requiredMods = new HashSet<>(settings.requiredMods);
			requiredMods.add("eminent_magic");
			settings.setRequiredMods(requiredMods);
			workspace.markDirty();
		});
	}
}