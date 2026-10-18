package emmy.eminentmagic.procedures;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.capabilities.Capabilities;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponents;

public class GetCurrentSpellIDProcedure {
	public static double execute(Entity entity) {
		if (entity == null)
			return 0;
		if ((entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).is(ItemTags.create(ResourceLocation.parse("eminentmagic:magicstaff")))) {
			return (getItemStackFromItemStackSlot((int) ((entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDoubleOr("SpellSlot", 0)),
					(entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY))).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDoubleOr("spellID", 0);
		} else if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).is(ItemTags.create(ResourceLocation.parse("eminentmagic:magicstaff")))) {
			return (getItemStackFromItemStackSlot((int) ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDoubleOr("SpellSlot", 0)),
					(entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY))).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDoubleOr("spellID", 0);
		}
		return 0;
	}

	private static ItemStack getItemStackFromItemStackSlot(int slotID, ItemStack itemStack) {
		IItemHandler itemHandler = itemStack.getCapability(Capabilities.ItemHandler.ITEM, null);
		if (itemHandler != null)
			return itemHandler.getStackInSlot(slotID).copy();
		return ItemStack.EMPTY;
	}
}