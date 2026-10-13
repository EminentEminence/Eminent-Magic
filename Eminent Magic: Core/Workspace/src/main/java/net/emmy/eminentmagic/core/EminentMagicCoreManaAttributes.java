package net.emmy.eminentmagic.core;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

public final class EminentMagicCoreManaAttributes {
	private static final ResourceLocation MANA_CONTROL = id("mana_control");
	private static final ResourceLocation MANA_CURRENT = id("mana_current");
	private static final ResourceLocation MANA_PERCEPTION = id("mana_perception");
	private static final ResourceLocation MANA_RESERVES = id("mana_reserves");
	private static final ResourceLocation MANA_STRENGTH = id("mana_strength");

	private EminentMagicCoreManaAttributes() {
	}

	private static ResourceLocation id(String name) {
		return ResourceLocation.fromNamespaceAndPath(EminentMagicCoreMod.MODID, name);
	}

	public static double getManaControl(LivingEntity entity) {
		return getValue(entity, MANA_CONTROL);
	}

	public static double getManaCurrent(LivingEntity entity) {
		return getValue(entity, MANA_CURRENT);
	}

	public static double getManaPerception(LivingEntity entity) {
		return getValue(entity, MANA_PERCEPTION);
	}

	public static double getManaReserves(LivingEntity entity) {
		return getValue(entity, MANA_RESERVES);
	}

	public static double getManaStrength(LivingEntity entity) {
		return getValue(entity, MANA_STRENGTH);
	}

	public static boolean setManaControl(LivingEntity entity, double value) {
		return setValue(entity, MANA_CONTROL, value);
	}

	public static boolean setManaCurrent(LivingEntity entity, double value) {
		return setValue(entity, MANA_CURRENT, value);
	}

	public static boolean setManaPerception(LivingEntity entity, double value) {
		return setValue(entity, MANA_PERCEPTION, value);
	}

	public static boolean setManaReserves(LivingEntity entity, double value) {
		return setValue(entity, MANA_RESERVES, value);
	}

	public static boolean setManaStrength(LivingEntity entity, double value) {
		return setValue(entity, MANA_STRENGTH, value);
	}

	private static double getValue(LivingEntity entity, ResourceLocation attributeId) {
		Holder<Attribute> attribute = BuiltInRegistries.ATTRIBUTE.get(attributeId).orElse(null);
		if (entity == null || attribute == null || !entity.getAttributes().hasAttribute(attribute)) {
			return 0;
		}
		return entity.getAttributeValue(attribute);
	}

	private static boolean setValue(LivingEntity entity, ResourceLocation attributeId, double value) {
		Holder<Attribute> attribute = BuiltInRegistries.ATTRIBUTE.get(attributeId).orElse(null);
		if (entity == null || entity.level().isClientSide() || !Double.isFinite(value) || value < 0 || attribute == null || !entity.getAttributes().hasAttribute(attribute)) {
			return false;
		}
		entity.getAttribute(attribute).setBaseValue(value);
		return true;
	}
}
