package emmy.eminentmagic.staff;

import java.util.Optional;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;

import emmy.eminentmagic.item.SpellItem;
import emmy.eminentmagic.item.SpellStaffItem;
import emmy.eminentmagic.spell.SpellCatalog;
import emmy.eminentmagic.spell.SpellCastingService;
import emmy.eminentmagic.spell.SpellDefinition;

public final class StaffService {
	private static final String SELECTED_SLOT_TAG = "SpellSlot";

	private StaffService() {
	}

	public static boolean isHoldingStaff(Entity entity) {
		return findHeldStaff(entity) != null;
	}

	public static Optional<InteractionHand> getHeldStaffHand(Entity entity) {
		return Optional.ofNullable(findHeldStaff(entity)).map(HeldStaff::hand);
	}

	public static Optional<SpellDefinition> getSelectedSpell(Entity entity) {
		HeldStaff heldStaff = findHeldStaff(entity);
		return heldStaff == null ? Optional.empty() : getSelectedSpell(heldStaff.stack());
	}

	public static Optional<SpellDefinition> getSelectedSpell(ItemStack staff) {
		return getSpellInSlot(staff, getSelectedSpellSlot(staff));
	}

	public static Optional<SpellDefinition> getSpellInHeldStaffSlot(Entity entity, int slot) {
		HeldStaff heldStaff = findHeldStaff(entity);
		return heldStaff == null ? Optional.empty() : getSpellInSlot(heldStaff.stack(), slot);
	}

	public static Optional<SpellDefinition> getSpellInSlot(ItemStack staff, int slot) {
		SpellStaffItem staffItem = getStaffItem(staff);
		if (staffItem == null || slot < 0 || slot >= staffItem.getSlotCount()) {
			return Optional.empty();
		}

		ItemContainerContents contents = staff.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
		if (slot < contents.getSlots()) {
			ItemStack spellStack = contents.getStackInSlot(slot);
			if (!spellStack.isEmpty()) {
				if (!(spellStack.getItem() instanceof SpellItem)) {
					return Optional.empty();
				}
				return SpellItem.getSpellId(spellStack).flatMap(SpellCatalog::get);
			}
		}

		if (slot == 0) {
			return SpellItem.getSpellId(staff).flatMap(SpellCatalog::get);
		}
		return Optional.empty();
	}

	public static int getSelectedSpellSlot(ItemStack staff) {
		SpellStaffItem staffItem = getStaffItem(staff);
		if (staffItem == null) {
			return 0;
		}
		double storedSlot = staff.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
				.copyTag().getDoubleOr(SELECTED_SLOT_TAG, 0);
		if (!Double.isFinite(storedSlot)) {
			return 0;
		}
		int slot = (int) storedSlot;
		return slot >= 0 && slot < staffItem.getSlotCount() ? slot : 0;
	}

	public static boolean setSpellInHeldStaffSlot(Entity entity, int slot, SpellDefinition spell) {
		if (!(entity instanceof LivingEntity living) || living.level().isClientSide()) {
			return false;
		}
		HeldStaff heldStaff = findHeldStaff(living);
		if (heldStaff == null) {
			return false;
		}

		ItemStack updatedStaff = heldStaff.stack().copy();
		if (!writeSpellInSlot(updatedStaff, slot, spell)) {
			return false;
		}
		living.setItemInHand(heldStaff.hand(), updatedStaff);
		return true;
	}

	public static boolean selectHeldStaffSpellSlot(Entity entity, int slot) {
		if (!(entity instanceof LivingEntity living) || living.level().isClientSide()) {
			return false;
		}
		HeldStaff heldStaff = findHeldStaff(living);
		SpellStaffItem staffItem = heldStaff == null ? null : getStaffItem(heldStaff.stack());
		if (staffItem == null || slot < 0 || slot >= staffItem.getSlotCount()) {
			return false;
		}

		ItemStack updatedStaff = heldStaff.stack().copy();
		CustomData.update(DataComponents.CUSTOM_DATA, updatedStaff, tag -> tag.putDouble(SELECTED_SLOT_TAG, slot));
		living.setItemInHand(heldStaff.hand(), updatedStaff);
		return true;
	}

	public static boolean castSelectedSpell(Entity entity) {
		if (!(entity instanceof Player player) || player.level().isClientSide()) {
			return false;
		}
		HeldStaff heldStaff = findHeldStaff(player);
		if (heldStaff == null) {
			return false;
		}
		return getSelectedSpell(heldStaff.stack())
				.map(spell -> SpellCastingService.cast(player, heldStaff.stack(), spell))
				.orElse(false);
	}

	private static boolean writeSpellInSlot(ItemStack staff, int slot, SpellDefinition spell) {
		SpellStaffItem staffItem = getStaffItem(staff);
		if (staffItem == null || slot < 0 || slot >= staffItem.getSlotCount()) {
			return false;
		}
		if (spell != null && SpellCatalog.get(spell.id()).filter(spell::equals).isEmpty()) {
			return false;
		}

		NonNullList<ItemStack> slots = NonNullList.withSize(staffItem.getSlotCount(), ItemStack.EMPTY);
		ItemContainerContents currentContents = staff.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
		for (int index = 0; index < Math.min(currentContents.getSlots(), slots.size()); index++) {
			slots.set(index, currentContents.getStackInSlot(index));
		}
		slots.set(slot, spell == null ? ItemStack.EMPTY : SpellItem.createStack(spell));
		staff.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(slots));
		CustomData.update(DataComponents.CUSTOM_DATA, staff, tag -> tag.remove(SpellItem.SPELL_ID_TAG));
		return true;
	}

	private static SpellStaffItem getStaffItem(ItemStack stack) {
		return stack.getItem() instanceof SpellStaffItem staffItem ? staffItem : null;
	}

	private static HeldStaff findHeldStaff(Entity entity) {
		if (!(entity instanceof LivingEntity living)) {
			return null;
		}
		if (getStaffItem(living.getOffhandItem()) != null) {
			return new HeldStaff(InteractionHand.OFF_HAND, living.getOffhandItem());
		}
		if (getStaffItem(living.getMainHandItem()) != null) {
			return new HeldStaff(InteractionHand.MAIN_HAND, living.getMainHandItem());
		}
		return null;
	}

	private record HeldStaff(InteractionHand hand, ItemStack stack) {
	}
}
