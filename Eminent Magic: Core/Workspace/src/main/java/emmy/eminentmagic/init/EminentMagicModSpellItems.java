package emmy.eminentmagic.init;

import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.Item;

import emmy.eminentmagic.EminentMagicMod;
import emmy.eminentmagic.item.SpellItem;
import emmy.eminentmagic.item.SpellStaffItem;

public final class EminentMagicModSpellItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(EminentMagicMod.MODID);
	public static final DeferredItem<SpellItem> SPELL = REGISTRY.registerItem("spell", SpellItem::new);
	public static final DeferredItem<SpellStaffItem> SPELL_STAFF = REGISTRY.registerItem("spell_staff", SpellStaffItem::new);

	private EminentMagicModSpellItems() {
	}
}