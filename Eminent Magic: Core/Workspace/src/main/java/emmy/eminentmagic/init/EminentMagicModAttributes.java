/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package emmy.eminentmagic.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.stream.Collectors;
import java.util.List;

import emmy.eminentmagic.EminentMagicMod;

@EventBusSubscriber
public class EminentMagicModAttributes {
	public static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, EminentMagicMod.MODID);
	public static final DeferredHolder<Attribute, Attribute> MANA_RESERVES = REGISTRY.register("mana_reserves", () -> new RangedAttribute("attribute.eminent_magic.mana_reserves", 0, 0, 9999).setSyncable(true));
	public static final DeferredHolder<Attribute, Attribute> MANA_STRENGTH = REGISTRY.register("mana_strength", () -> new RangedAttribute("attribute.eminent_magic.mana_strength", 0, 0, 99).setSyncable(true));
	public static final DeferredHolder<Attribute, Attribute> MANA_CONTROL = REGISTRY.register("mana_control", () -> new RangedAttribute("attribute.eminent_magic.mana_control", 0, 0, 99).setSyncable(true));
	public static final DeferredHolder<Attribute, Attribute> MANA_PERCEPTION = REGISTRY.register("mana_perception", () -> new RangedAttribute("attribute.eminent_magic.mana_perception", 0, 0, 99).setSyncable(true));
	public static final DeferredHolder<Attribute, Attribute> MANA_CURRENT = REGISTRY.register("mana_current", () -> new RangedAttribute("attribute.eminent_magic.mana_current", 0, 0, 9999).setSyncable(true));

	@SubscribeEvent
	public static void addAttributes(EntityAttributeModificationEvent event) {
		event.add(EntityType.PLAYER, MANA_RESERVES);
		event.add(EntityType.PLAYER, MANA_STRENGTH);
		event.add(EntityType.PLAYER, MANA_CONTROL);
		event.add(EntityType.PLAYER, MANA_PERCEPTION);
		List.of(EntityType.ZOMBIE).stream().filter(DefaultAttributes::hasSupplier).map(entityType -> (EntityType<? extends LivingEntity>) entityType).collect(Collectors.toList()).forEach(entity -> event.add(entity, MANA_CURRENT));
		event.add(EntityType.PLAYER, MANA_CURRENT);
	}
}