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
import net.minecraft.client.Minecraft;

import java.io.File;

import emmy.eminentmagic.network.EminentMagicModVariables;

public class GetManaCostProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		File SpellInfoFile = new File("");
		com.google.gson.JsonObject SpellObject = new com.google.gson.JsonObject();
		com.google.gson.JsonArray SpellArray = new com.google.gson.JsonArray();
		double ControlScaling = 0;
		double ManaCost = 0;
		double StrengthScaling = 0;
		double spellID = 0;
		if ((entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).is(ItemTags.create(ResourceLocation.parse("eminentmagic:magicstaff")))) {
			spellID = (getItemStackFromItemStackSlot((int) ((entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDoubleOr("SpellSlot", 0)),
					(entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY))).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDoubleOr("spellID", 0);
		} else if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).is(ItemTags.create(ResourceLocation.parse("eminentmagic:magicstaff")))) {
			spellID = (getItemStackFromItemStackSlot((int) ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDoubleOr("SpellSlot", 0)),
					(entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY))).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDoubleOr("spellID", 0);
		}
		assert Boolean.TRUE; //#dbg:GetManaCost:ParameterFetching
		SpellObject = GetSpellDataProcedure.execute(spellID);
		return new java.text.DecimalFormat("####")
				.format(CalculateManaCostProcedure.execute(SpellObject.get("baseCost").getAsDouble(), SpellObject.get("costControlScaling").getAsDouble(), Minecraft.getInstance().player.getData(EminentMagicModVariables.PLAYER_VARIABLES).spellControl,
						Minecraft.getInstance().player.getData(EminentMagicModVariables.PLAYER_VARIABLES).spellStrength, SpellObject.get("costStrengthScaling").getAsDouble()));
	}

	private static ItemStack getItemStackFromItemStackSlot(int slotID, ItemStack itemStack) {
		IItemHandler itemHandler = itemStack.getCapability(Capabilities.ItemHandler.ITEM, null);
		if (itemHandler != null)
			return itemHandler.getStackInSlot(slotID).copy();
		return ItemStack.EMPTY;
	}
}