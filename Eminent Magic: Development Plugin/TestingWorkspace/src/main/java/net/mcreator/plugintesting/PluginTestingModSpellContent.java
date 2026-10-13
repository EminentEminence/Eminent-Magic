package net.mcreator.plugintesting;

import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.emmy.eminentmagic.core.EminentMagicCoreGrimoireContent;

@EventBusSubscriber(modid = "plugin_testing")
public final class PluginTestingModSpellContent {
	private PluginTestingModSpellContent() {
	}

	@SubscribeEvent
	public static void addSpellsToGrimoireTab(BuildCreativeModeTabContentsEvent event) {
		if (!event.getTabKey().location().equals(EminentMagicCoreGrimoireContent.SPELLS_TAB_ID)) {
			return;
		}
		event.accept(EminentMagicCoreGrimoireContent.createSpellStack("plugin_testing:test", "Test", 1, 2, 3, "water"));
		event.accept(EminentMagicCoreGrimoireContent.createSpellStack("plugin_testing:test_2", "Test2", 1, 2, 3, "wind"));
	}
}