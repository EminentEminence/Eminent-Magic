package emmy.eminentmagic;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.resources.ResourceLocation;

import emmy.eminentmagic.spell.SpellTriggerEvent;
import emmy.eminentmagic.spell.SpellTrigger;
import emmy.eminentmagic.procedures.TestSpellRunWhileChannellingProcedure;
import emmy.eminentmagic.procedures.TestSpellRunCastProcedure;
import emmy.eminentmagic.procedures.OnEntityChannelingTickProcedure;

@EventBusSubscriber(modid = EminentMagicMod.MODID)
public final class TestSpellSpellHooks {
	private TestSpellSpellHooks() {
	}

	@SubscribeEvent
	public static void onSpellTrigger(SpellTriggerEvent event) {
		if (!event.spell().id().equals(ResourceLocation.fromNamespaceAndPath(EminentMagicMod.MODID, "trigger_probe"))) {
			return;
		}
		if (event.trigger() == SpellTrigger.ON_CAST) {
			TestSpellRunCastProcedure.execute(event.context().get("world"), event.context().get("entity"));
		}
		if (event.trigger() == SpellTrigger.WHILE_CHARGING) {
			OnEntityChannelingTickProcedure.execute(event.context().get("world"), event.context().get("entity"));
		}
		if (event.trigger() == SpellTrigger.WHILE_CHANNELLING) {
			TestSpellRunWhileChannellingProcedure.execute(event.context().get("entity"));
		}
	}
}