package net.emmy.eminentmagic.core;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;

import java.util.List;
import java.util.Optional;

public final class EminentMagicCoreGrimoireContent {
	private static final ResourceLocation GRIMOIRE_ID = ResourceLocation.fromNamespaceAndPath("eminent_magic_core", "grimoire");
	public static final ResourceLocation SPELLS_TAB_ID = ResourceLocation.fromNamespaceAndPath("eminent_magic_core", "eminent_spells");

	private EminentMagicCoreGrimoireContent() {
	}

	public static ItemStack createSpellStack(String spellId, String spellName, double manaCost, double controlScaling, double strengthScaling, String element) {
		ResourceLocation id = parseNamespacedId(spellId);
		if (id == null) {
			return ItemStack.EMPTY;
		}
		return createSpellStack(id, spellName);
	}

	public static ItemStack createSpellStack(ResourceLocation spellId) {
		return EminentMagicCoreSpellRegistry.findSpell(spellId)
				.map(definition -> createSpellStack(spellId, definition.spellName()))
				.orElse(ItemStack.EMPTY);
	}

	public static Optional<ResourceLocation> getSpellId(ItemStack stack) {
		if (stack == null || stack.isEmpty() || !BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(GRIMOIRE_ID)) {
			return Optional.empty();
		}
		String encodedId = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getStringOr("spellId", "");
		ResourceLocation spellId = parseNamespacedId(encodedId);
		return spellId != null && EminentMagicCoreSpellRegistry.findSpell(spellId).isPresent() ? Optional.of(spellId) : Optional.empty();
	}

	public static Optional<EminentMagicCoreSpellRegistry.SpellDefinition> getSpellDefinition(ItemStack stack) {
		return getSpellId(stack).flatMap(EminentMagicCoreSpellRegistry::findSpell);
	}

	private static ItemStack createSpellStack(ResourceLocation spellId, String spellName) {
		ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(GRIMOIRE_ID).orElseThrow().value());
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
			tag.putString("spellId", spellId.toString());
			tag.putString("spellName", spellName);
		});
		stack.set(DataComponents.LORE, new ItemLore(List.of(
				Component.literal(spellName).withStyle(style -> style.withColor(0xAA00FF))
		)));
		return stack;
	}

	private static ResourceLocation parseNamespacedId(String spellId) {
		return spellId == null || !spellId.contains(":") ? null : ResourceLocation.tryParse(spellId);
	}

}