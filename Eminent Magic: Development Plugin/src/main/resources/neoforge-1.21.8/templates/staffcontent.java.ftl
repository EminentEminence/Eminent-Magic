package ${package};

import net.emmy.eminentmagic.core.EminentMagicCoreStaffItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = "${modid}")
public final class ${JavaModName}StaffContent {
	private static final List<EminentMagicCoreStaffItem> STAFF_ITEMS = new ArrayList<>();

	private ${JavaModName}StaffContent() {
	}

	@SubscribeEvent
	public static void registerContent(RegisterEvent event) {
		if (event.getRegistryKey().equals(Registries.ITEM)) {
<#list w.getGElementsOfType("staff") as staff>
			ResourceLocation staffId${staff?index} = ResourceLocation.fromNamespaceAndPath("${modid}", "${staff.getModElement().getRegistryName()}");
			ResourceKey<Item> staffKey${staff?index} = ResourceKey.create(Registries.ITEM, staffId${staff?index});
			event.register(Registries.ITEM, staffId${staff?index}, () -> {
				EminentMagicCoreStaffItem staffItem = new EminentMagicCoreStaffItem(new Item.Properties().setId(staffKey${staff?index}).stacksTo(1), ${staff.spellSlots}, Component.literal("${staff.staffName?json_string}"), List.of(
<#if staff.creativeTabs?has_content>
<#list staff.creativeTabs as tab>
<#assign tabName = tab.getUnmappedValue()>
<#if tabName?starts_with("CUSTOM:")>
						ResourceLocation.fromNamespaceAndPath("${modid}", "${w.getWorkspace().getModElementByName(tabName.replace("CUSTOM:", "")).getRegistryName()}")<#if tab?has_next>,</#if>
<#else>
						${generator.map(tabName, "tabs")}.location()<#if tab?has_next>,</#if>
</#if>
</#list>
<#else>
	 					ResourceLocation.fromNamespaceAndPath("eminent_magic_core", "eminent_spells")
</#if>
				));
				STAFF_ITEMS.add(staffItem);
				return staffItem;
			});
</#list>
		}
	}

	@SubscribeEvent
	public static void addStaffItemsToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
		ResourceLocation tabId = event.getTabKey().location();
		for (EminentMagicCoreStaffItem staffItem : STAFF_ITEMS) {
			if (staffItem.creativeTabIds.contains(tabId)) {
				event.accept(staffItem);
			}
		}
	}

	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		for (EminentMagicCoreStaffItem staffItem : STAFF_ITEMS) {
			staffItem.registerCapabilities(event);
		}
	}
}
