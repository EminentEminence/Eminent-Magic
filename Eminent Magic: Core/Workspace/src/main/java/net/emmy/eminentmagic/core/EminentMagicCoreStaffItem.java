package net.emmy.eminentmagic.core;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.items.ComponentItemHandler;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;
import java.util.Optional;

public final class EminentMagicCoreStaffItem extends Item {
	private static final ResourceLocation GRIMOIRE_ID = ResourceLocation.fromNamespaceAndPath(EminentMagicCoreMod.MODID, "grimoire");
	private static final String SELECTED_SLOT_KEY = "selectedSpellSlot";
	public final int spellSlots;
	public final List<ResourceLocation> creativeTabIds;
	private final Component displayName;

	public EminentMagicCoreStaffItem(Properties properties, int spellSlots, Component displayName, List<ResourceLocation> creativeTabIds) {
		super(properties);
		if (spellSlots < 1 || spellSlots > 12) {
			throw new IllegalArgumentException("Staff spell slot count must be between 1 and 12");
		}
		this.spellSlots = spellSlots;
		this.displayName = displayName;
		this.creativeTabIds = List.copyOf(creativeTabIds);
	}

	@Override
	public Component getName(ItemStack stack) {
		return displayName;
	}

	@Override
	public InteractionResult use(net.minecraft.world.level.Level level, Player player, InteractionHand hand) {
		if (!player.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.openMenu(new MenuProvider() {
				@Override
				public Component getDisplayName() {
					return displayName;
				}

				@Override
				public AbstractContainerMenu createMenu(int id, Inventory inventory, Player menuPlayer) {
					return new EminentMagicCoreStaffMenu(id, inventory, hand == InteractionHand.MAIN_HAND ? 0 : 1);
				}
			}, buffer -> buffer.writeByte(hand == InteractionHand.MAIN_HAND ? 0 : 1));
		}
		return InteractionResult.SUCCESS;
	}

	public void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerItem(Capabilities.ItemHandler.ITEM,
				(stack, context) -> new StaffInventory(stack, spellSlots), this);
	}

	public static ItemStack getSelectedStaffStack(Player player) {
		if (player == null) {
			return ItemStack.EMPTY;
		}
		if (player.getOffhandItem().getItem() instanceof EminentMagicCoreStaffItem) {
			return player.getOffhandItem();
		}
		if (player.getMainHandItem().getItem() instanceof EminentMagicCoreStaffItem) {
			return player.getMainHandItem();
		}
		return ItemStack.EMPTY;
	}

	public static int getSelectedSpellSlot(ItemStack staffStack) {
		if (staffStack == null || !(staffStack.getItem() instanceof EminentMagicCoreStaffItem)) {
			return -1;
		}
		return staffStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr(SELECTED_SLOT_KEY, 0);
	}

	public static boolean setSelectedSpellSlot(ItemStack staffStack, int slot) {
		if (staffStack == null || !(staffStack.getItem() instanceof EminentMagicCoreStaffItem staffItem)
				|| slot < 0 || slot >= staffItem.spellSlots || !containsValidSpell(staffStack, slot)) {
			return false;
		}
		CustomData.update(DataComponents.CUSTOM_DATA, staffStack, tag -> tag.putInt(SELECTED_SLOT_KEY, slot));
		return true;
	}

	public static Optional<SelectedSpell> getSelectedSpell(Player player) {
		return getSelectedSpell(getSelectedStaffStack(player));
	}

	public static Optional<SelectedSpell> getSelectedSpell(ItemStack staffStack) {
		if (staffStack == null || !(staffStack.getItem() instanceof EminentMagicCoreStaffItem staffItem)) {
			return Optional.empty();
		}
		int slot = getSelectedSpellSlot(staffStack);
		if (slot < 0 || slot >= staffItem.spellSlots) {
			return Optional.empty();
		}
		IItemHandler inventory = staffStack.getCapability(Capabilities.ItemHandler.ITEM);
		if (inventory == null || slot >= inventory.getSlots()) {
			return Optional.empty();
		}
		return EminentMagicCoreGrimoireContent.getSpellDefinition(inventory.getStackInSlot(slot))
				.map(definition -> new SelectedSpell(staffStack, slot, definition));
	}

	public static Optional<SelectedSpell> cycleSelectedSpell(Player player, int direction) {
		if (player == null || player.level().isClientSide() || direction == 0) {
			return Optional.empty();
		}
		ItemStack staffStack = getSelectedStaffStack(player);
		if (!(staffStack.getItem() instanceof EminentMagicCoreStaffItem staffItem) || staffItem.spellSlots <= 0) {
			return Optional.empty();
		}
		int currentSlot = getSelectedSpellSlot(staffStack);
		int step = direction > 0 ? 1 : -1;
		for (int offset = 1; offset <= staffItem.spellSlots; offset++) {
			int slot = Math.floorMod(currentSlot + step * offset, staffItem.spellSlots);
			if (containsValidSpell(staffStack, slot) && setSelectedSpellSlot(staffStack, slot)) {
				return getSelectedSpell(staffStack);
			}
		}
		return Optional.empty();
	}

	private static boolean containsValidSpell(ItemStack staffStack, int slot) {
		IItemHandler inventory = staffStack.getCapability(Capabilities.ItemHandler.ITEM);
		return inventory != null && slot < inventory.getSlots()
				&& EminentMagicCoreGrimoireContent.getSpellDefinition(inventory.getStackInSlot(slot)).isPresent();
	}

	public record SelectedSpell(ItemStack staffStack, int slot, EminentMagicCoreSpellRegistry.SpellDefinition definition) {
	}

	private static final class StaffInventory extends ComponentItemHandler {
		private StaffInventory(MutableDataComponentHolder parent, int slots) {
			super(parent, DataComponents.CONTAINER, slots);
		}

		@Override
		public int getSlotLimit(int slot) {
			return 1;
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return stack.isEmpty() || EminentMagicCoreGrimoireContent.getSpellDefinition(stack).isPresent();
		}
	}
}