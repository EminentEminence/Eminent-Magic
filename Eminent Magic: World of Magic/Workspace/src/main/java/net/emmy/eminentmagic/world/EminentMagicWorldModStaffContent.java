package net.emmy.eminentmagic.world;

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

@EventBusSubscriber(modid = "eminent_magic_world")
public final class EminentMagicWorldModStaffContent {
	private static final List<EminentMagicCoreStaffItem> STAFF_ITEMS = new ArrayList<>();

	private EminentMagicWorldModStaffContent() {
	}

	@SubscribeEvent
	public static void registerContent(RegisterEvent event) {
		if (event.getRegistryKey().equals(Registries.ITEM)) {
			ResourceLocation staffId0 = ResourceLocation.fromNamespaceAndPath("eminent_magic_world", "test_staff");
			ResourceKey<Item> staffKey0 = ResourceKey.create(Registries.ITEM, staffId0);
			event.register(Registries.ITEM, staffId0, () -> {
				EminentMagicCoreStaffItem staffItem = new EminentMagicCoreStaffItem(new Item.Properties().setId(staffKey0).stacksTo(1), 5, Component.literal("TestStaff"), List.of(CreativeModeTabs.COMBAT.location()));
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