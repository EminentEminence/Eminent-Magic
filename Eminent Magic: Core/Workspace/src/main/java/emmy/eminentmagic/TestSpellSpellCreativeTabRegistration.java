package emmy.eminentmagic;

import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;

import emmy.eminentmagic.item.SpellItem;

@EventBusSubscriber(modid = EminentMagicMod.MODID)
public final class TestSpellSpellCreativeTabRegistration {
	private static final ResourceKey<CreativeModeTab> CREATIVE_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.parse("minecraft:combat"));

	private TestSpellSpellCreativeTabRegistration() {
	}

	@SubscribeEvent
	public static void addSpellToCreativeTab(BuildCreativeModeTabContentsEvent event) {
		if (!CREATIVE_TAB.equals(event.getTabKey())) {
			return;
		}
		ItemStack spell = SpellItem.createStack(ResourceLocation.fromNamespaceAndPath(EminentMagicMod.MODID, "trigger_probe"), "Test Spell");
		spell.set(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(EminentMagicMod.MODID, "test_spell"));
		event.accept(spell);
	}
}