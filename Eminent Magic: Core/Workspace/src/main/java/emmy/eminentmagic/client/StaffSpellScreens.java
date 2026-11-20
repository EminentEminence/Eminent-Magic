package emmy.eminentmagic.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import emmy.eminentmagic.client.gui.StaffSpellSlotsScreen;
import emmy.eminentmagic.init.StaffSpellMenus;

@EventBusSubscriber(Dist.CLIENT)
public final class StaffSpellScreens {
	private StaffSpellScreens() {
	}

	@SubscribeEvent
	public static void register(RegisterMenuScreensEvent event) {
		event.register(StaffSpellMenus.STAFF_SPELL_SLOTS.get(), StaffSpellSlotsScreen::new);
	}
}
