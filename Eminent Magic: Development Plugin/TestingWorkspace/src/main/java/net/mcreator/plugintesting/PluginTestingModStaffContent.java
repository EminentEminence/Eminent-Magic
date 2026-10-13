package net.mcreator.plugintesting;

import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import net.emmy.eminentmagic.core.EminentMagicCoreStaffItem;

import java.util.List;
import java.util.ArrayList;

@EventBusSubscriber(modid = "plugin_testing")
public final class PluginTestingModStaffContent {
	private static final List<EminentMagicCoreStaffItem> STAFF_ITEMS = new ArrayList<>();

	private PluginTestingModStaffContent() {
	}

	@SubscribeEvent
	public static void registerContent(RegisterEvent event) {
		if (event.getRegistryKey().equals(Registries.ITEM)) {
			ResourceLocation staffId0 = ResourceLocation.fromNamespaceAndPath("plugin_testing", "simple_wooden_staff");
			ResourceKey<Item> staffKey0 = ResourceKey.create(Registries.ITEM, staffId0);
			event.register(Registries.ITEM, staffId0, () -> {
				EminentMagicCoreStaffItem staffItem = new EminentMagicCoreStaffItem(new Item.Properties().setId(staffKey0).stacksTo(1), 1, Component.literal("Simple Wooden Staff"), List.of(CreativeModeTabs.COMBAT.location()));
				STAFF_ITEMS.add(staffItem);
				return staffItem;
			});
			ResourceLocation staffId1 = ResourceLocation.fromNamespaceAndPath("plugin_testing", "ultimate_staff");
			ResourceKey<Item> staffKey1 = ResourceKey.create(Registries.ITEM, staffId1);
			event.register(Registries.ITEM, staffId1, () -> {
				EminentMagicCoreStaffItem staffItem = new EminentMagicCoreStaffItem(new Item.Properties().setId(staffKey1).stacksTo(1), 12, Component.literal("Ultimate Staff"), List.of(CreativeModeTabs.COMBAT.location()));
				STAFF_ITEMS.add(staffItem);
				return staffItem;
			});
			ResourceLocation staffId2 = ResourceLocation.fromNamespaceAndPath("plugin_testing", "intermediate_staff");
			ResourceKey<Item> staffKey2 = ResourceKey.create(Registries.ITEM, staffId2);
			event.register(Registries.ITEM, staffId2, () -> {
				EminentMagicCoreStaffItem staffItem = new EminentMagicCoreStaffItem(new Item.Properties().setId(staffKey2).stacksTo(1), 11, Component.literal("Intermediate Staff"), List.of(CreativeModeTabs.COMBAT.location()));
				STAFF_ITEMS.add(staffItem);
				return staffItem;
			});
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