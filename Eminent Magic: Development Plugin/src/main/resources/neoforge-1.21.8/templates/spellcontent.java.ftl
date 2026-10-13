package ${package};

import net.emmy.eminentmagic.core.EminentMagicCoreGrimoireContent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = "${modid}")
public final class ${JavaModName}SpellContent {
	private ${JavaModName}SpellContent() {
	}

	@SubscribeEvent
	public static void addSpellsToGrimoireTab(BuildCreativeModeTabContentsEvent event) {
		if (!event.getTabKey().location().equals(EminentMagicCoreGrimoireContent.SPELLS_TAB_ID)) {
			return;
		}
<#list w.getGElementsOfType("spell") as spell>
		event.accept(EminentMagicCoreGrimoireContent.createSpellStack(
				"${modid}:${spell.getModElement().getRegistryName()}",
				"${spell.spellName?json_string}",
				${spell.manaCost?c},
				${spell.controlScaling?c},
				${spell.strengthScaling?c},
				"${spell.element?json_string}"
		));
</#list>
	}
}