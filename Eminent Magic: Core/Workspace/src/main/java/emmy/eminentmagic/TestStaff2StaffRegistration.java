package emmy.eminentmagic;

import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;

import emmy.eminentmagic.item.SpellStaffItem;

@EventBusSubscriber(modid = EminentMagicMod.MODID)
public final class TestStaff2StaffRegistration {
	private static final ResourceLocation STAFF_ID = ResourceLocation.fromNamespaceAndPath(EminentMagicMod.MODID, "teststaff2");
	private static final ResourceKey<CreativeModeTab> CREATIVE_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.parse("minecraft:tools_and_utilities"));

	private TestStaff2StaffRegistration() {
	}

	@SubscribeEvent
	public static void registerStaff(RegisterEvent event) {
		if (!Registries.ITEM.equals(event.getRegistryKey())) {
			return;
		}
		event.register(Registries.ITEM, STAFF_ID, () -> new SpellStaffItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, STAFF_ID)), 8, "Test Staff 2"));
	}

	@SubscribeEvent
	public static void addStaffToCreativeTab(BuildCreativeModeTabContentsEvent event) {
		if (!CREATIVE_TAB.equals(event.getTabKey())) {
			return;
		}
		ItemStack staff = new ItemStack(BuiltInRegistries.ITEM.get(STAFF_ID).orElseThrow(() -> new IllegalStateException("Registered staff item is missing: " + STAFF_ID)));
		staff.set(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(EminentMagicMod.MODID, "test_staff_2"));
		event.accept(staff);
	}
}