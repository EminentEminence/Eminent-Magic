package emmy.eminentmagic.init;

import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

import emmy.eminentmagic.EminentMagicMod;
import emmy.eminentmagic.world.inventory.StaffSpellSlotsMenu;

public final class StaffSpellMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY =
			DeferredRegister.create(Registries.MENU, EminentMagicMod.MODID);
	public static final DeferredHolder<MenuType<?>, MenuType<StaffSpellSlotsMenu>> STAFF_SPELL_SLOTS =
			REGISTRY.register("staff_spell_slots", () -> IMenuTypeExtension.create(StaffSpellSlotsMenu::new));

	private StaffSpellMenus() {
	}
}
