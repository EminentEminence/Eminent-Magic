package net.emmy.eminentmagic.core;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = EminentMagicCoreMod.MODID)
public final class EminentMagicCoreStaffMenu extends AbstractContainerMenu {
	private static final ResourceLocation MENU_ID = ResourceLocation.fromNamespaceAndPath(EminentMagicCoreMod.MODID, "staff_spell_slots");
	private static MenuType<EminentMagicCoreStaffMenu> menuType;
	private final ItemStack staffStack;
	private final IItemHandler staffInventory;
	private final int staffSlotCount;
	private final int hand;

	public EminentMagicCoreStaffMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
		this(id, inventory, buffer.readUnsignedByte());
	}

	public EminentMagicCoreStaffMenu(int id, Inventory inventory, int hand) {
		super(menuType, id);
		Player player = inventory.player;
		this.hand = hand;
		this.staffStack = hand == 0 ? player.getMainHandItem() : player.getOffhandItem();
		this.staffSlotCount = staffStack.getItem() instanceof EminentMagicCoreStaffItem staffItem ? staffItem.spellSlots : 0;
		IItemHandler itemHandler = staffStack.getCapability(Capabilities.ItemHandler.ITEM);
		this.staffInventory = itemHandler != null ? itemHandler : new ItemStackHandler(0);

		for (int slot = 0; slot < staffSlotCount; slot++) {
			double angle = -Math.PI / 2 + Math.PI * 2 * slot / staffSlotCount;
			int x = (int) Math.round(88 + Math.cos(angle) * 52) - 9;
			int y = (int) Math.round(76 + Math.sin(angle) * 52) - 9;
			this.addSlot(new SlotItemHandler(staffInventory, slot, x, y));
		}
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				this.addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, 164 + row * 18));
			}
		}
		for (int column = 0; column < 9; column++) {
			this.addSlot(new Slot(inventory, column, 8 + column * 18, 222));
		}
	}

	@SubscribeEvent
	public static void registerMenu(RegisterEvent event) {
		if (event.getRegistryKey().equals(Registries.MENU)) {
			event.register(Registries.MENU, MENU_ID, () -> {
				menuType = IMenuTypeExtension.create(EminentMagicCoreStaffMenu::new);
				return menuType;
			});
		}
	}

	public static MenuType<EminentMagicCoreStaffMenu> getMenuType() {
		return menuType;
	}

	@Override
	public boolean stillValid(Player player) {
		ItemStack heldStack = hand == 0 ? player.getMainHandItem() : player.getOffhandItem();
		return player.isAlive() && heldStack == staffStack;
	}

	public int getStaffSlotCount() {
		return staffSlotCount;
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
		ItemStack original = source.copy();
		if (index < staffSlotCount) {
			if (!moveItemStackTo(source, staffSlotCount, slots.size(), true)) {
				return ItemStack.EMPTY;
			}
		} else if (!moveItemStackTo(source, 0, staffSlotCount, false)) {
			return ItemStack.EMPTY;
		}

		if (source.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		return original;
	}
}