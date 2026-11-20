package emmy.eminentmagic.world.inventory;

import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import emmy.eminentmagic.init.StaffSpellMenus;
import emmy.eminentmagic.item.SpellItem;
import emmy.eminentmagic.item.SpellStaffItem;

public final class StaffSpellSlotsMenu extends AbstractContainerMenu {
	private static final int COLUMNS = 9;
	private final Player player;
	private final InteractionHand hand;
	private final ItemStack staffStack;
	private final int staffSlotCount;
	private final int staffRows;
	private final int inventoryTop;
	private final ItemStackHandler spellSlots;
	private boolean loading = true;

	public StaffSpellSlotsMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
		this(id, inventory, extraData.readEnum(InteractionHand.class));
	}

	public StaffSpellSlotsMenu(int id, Inventory inventory, InteractionHand hand) {
		super(StaffSpellMenus.STAFF_SPELL_SLOTS.get(), id);
		this.player = inventory.player;
		this.hand = hand;
		this.staffStack = player.getItemInHand(hand);
		this.staffSlotCount = staffStack.getItem() instanceof SpellStaffItem staff ? staff.getSlotCount() : 0;
		this.staffRows = Math.max(1, (staffSlotCount + COLUMNS - 1) / COLUMNS);
		this.inventoryTop = 18 + staffRows * 18 + 14;
		this.spellSlots = new ItemStackHandler(staffSlotCount) {
			@Override
			public boolean isItemValid(int slot, ItemStack stack) {
				return stack.getItem() instanceof SpellItem;
			}

			@Override
			protected void onContentsChanged(int slot) {
				if (!loading && staffStack.getItem() instanceof SpellStaffItem) {
					saveSpellSlots();
				}
			}
		};

		ItemContainerContents contents = staffStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
		for (int slot = 0; slot < Math.min(staffSlotCount, contents.getSlots()); slot++) {
			ItemStack spell = contents.getStackInSlot(slot);
			if (!spell.isEmpty()) {
				spellSlots.setStackInSlot(slot, spell.copy());
			}
		}
		loading = false;

		for (int slot = 0; slot < staffSlotCount; slot++) {
			addSlot(new SlotItemHandler(spellSlots, slot,
					8 + (slot % COLUMNS) * 18, 18 + (slot / COLUMNS) * 18));
		}
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < COLUMNS; column++) {
				addSlot(new Slot(inventory, column + row * COLUMNS + 9,
						8 + column * 18, inventoryTop + row * 18));
			}
		}
		for (int column = 0; column < COLUMNS; column++) {
			addSlot(new Slot(inventory, column, 8 + column * 18, inventoryTop + 58));
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return player == this.player && player.getItemInHand(hand) == staffStack
				&& player.getItemInHand(hand).getItem() instanceof SpellStaffItem;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		if (index < 0 || index >= slots.size()) {
			return ItemStack.EMPTY;
		}
		Slot slot = slots.get(index);
		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}

		ItemStack source = slot.getItem();
		ItemStack moved = source.copy();
		if (index < staffSlotCount) {
			if (!moveItemStackTo(source, staffSlotCount, slots.size(), true)) {
				return ItemStack.EMPTY;
			}
		} else {
			if (!(source.getItem() instanceof SpellItem)
					|| !moveItemStackTo(source, 0, staffSlotCount, false)) {
				return ItemStack.EMPTY;
			}
		}

		if (source.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		return moved;
	}

	public int getStaffRows() {
		return staffRows;
	}

	public int getStaffSlotCount() {
		return staffSlotCount;
	}

	public int getInventoryTop() {
		return inventoryTop;
	}

	public int getImageHeight() {
		return 114 + staffRows * 18;
	}

	private void saveSpellSlots() {
		NonNullList<ItemStack> contents = NonNullList.withSize(staffSlotCount, ItemStack.EMPTY);
		for (int slot = 0; slot < staffSlotCount; slot++) {
			contents.set(slot, spellSlots.getStackInSlot(slot));
		}
		staffStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
	}
}
