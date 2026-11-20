package emmy.eminentmagic.worldofmagic;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import emmy.eminentmagic.init.EminentMagicModSpellItems;
import emmy.eminentmagic.item.SpellStaffItem;
import emmy.eminentmagic.spell.SpellCatalog;

public final class EminentMagicWorldSpellTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EminentMagicWorldMod.MODID);
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SPELLS = REGISTRY.register("spells", () -> CreativeModeTab.builder()
			.title(Component.literal("Eminent Magic Spells"))
			.icon(() -> new ItemStack(EminentMagicModSpellItems.SPELL_STAFF.get()))
			.displayItems((parameters, output) -> SpellCatalog.all().forEach(spell -> output.accept(SpellStaffItem.createStack(spell))))
			.build());

	private EminentMagicWorldSpellTabs() {
	}
}