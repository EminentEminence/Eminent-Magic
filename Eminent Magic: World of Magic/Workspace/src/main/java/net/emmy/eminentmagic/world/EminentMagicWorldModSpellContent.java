package net.emmy.eminentmagic.world;

import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.emmy.eminentmagic.core.EminentMagicCoreGrimoireContent;

@EventBusSubscriber(modid = "eminent_magic_world")
public final class EminentMagicWorldModSpellContent {
	private EminentMagicWorldModSpellContent() {
	}

	@SubscribeEvent
	public static void addSpellsToGrimoireTab(BuildCreativeModeTabContentsEvent event) {
		if (!event.getTabKey().location().equals(EminentMagicCoreGrimoireContent.SPELLS_TAB_ID)) {
			return;
		}
		event.accept(EminentMagicCoreGrimoireContent.createSpellStack("eminent_magic_world:test", "Test", 1, 2, 3, "fire"));
	}
}