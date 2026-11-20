package ${package};

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import emmy.eminentmagic.item.SpellStaffItem;

@EventBusSubscriber(modid = ${JavaModName}.MODID)
public final class ${name}StaffRegistration {
	private static final ResourceLocation STAFF_ID =
			ResourceLocation.fromNamespaceAndPath(${JavaModName}.MODID, "${data.staffId}");
<#if data.creativeTab?has_content>
	private static final ResourceKey<CreativeModeTab> CREATIVE_TAB = ResourceKey.create(
			Registries.CREATIVE_MODE_TAB, ResourceLocation.parse("${data.creativeTab?json_string}"));
</#if>

	private ${name}StaffRegistration() {
	}

	@SubscribeEvent
	public static void registerStaff(RegisterEvent event) {
		if (!Registries.ITEM.equals(event.getRegistryKey())) {
			return;
		}
		event.register(Registries.ITEM, STAFF_ID, () -> new SpellStaffItem(
				new Item.Properties().setId(ResourceKey.create(Registries.ITEM, STAFF_ID)),
				${data.slotCount}, "${data.staffName?json_string}"));
	}

<#if data.creativeTab?has_content>
	@SubscribeEvent
	public static void addStaffToCreativeTab(BuildCreativeModeTabContentsEvent event) {
		if (!CREATIVE_TAB.equals(event.getTabKey())) {
			return;
		}
		ItemStack staff = new ItemStack(BuiltInRegistries.ITEM.get(STAFF_ID).orElseThrow(
				() -> new IllegalStateException("Registered staff item is missing: " + STAFF_ID)));
		staff.set(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(
				${JavaModName}.MODID, "${registryname}"));
		event.accept(staff);
	}
</#if>
}
