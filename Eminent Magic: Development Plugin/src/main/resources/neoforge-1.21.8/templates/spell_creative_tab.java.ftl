package ${package};

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import emmy.eminentmagic.item.SpellItem;

@EventBusSubscriber(modid = ${JavaModName}.MODID)
public final class ${name}SpellCreativeTabRegistration {
<#if data.creativeTab?has_content>
	private static final ResourceKey<CreativeModeTab> CREATIVE_TAB = ResourceKey.create(
			Registries.CREATIVE_MODE_TAB, ResourceLocation.parse("${data.creativeTab?json_string}"));
</#if>

	private ${name}SpellCreativeTabRegistration() {
	}

<#if data.creativeTab?has_content>
	@SubscribeEvent
	public static void addSpellToCreativeTab(BuildCreativeModeTabContentsEvent event) {
		if (!CREATIVE_TAB.equals(event.getTabKey())) {
			return;
		}
		ItemStack spell = SpellItem.createStack(
				ResourceLocation.fromNamespaceAndPath(${JavaModName}.MODID, "${data.spellId?json_string}"),
				"${data.spellName?json_string}");
		spell.set(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(
				${JavaModName}.MODID, "${registryname}"));
		event.accept(spell);
	}
</#if>
}
