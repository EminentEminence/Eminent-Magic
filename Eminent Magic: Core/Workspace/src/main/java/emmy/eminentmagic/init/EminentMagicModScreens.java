/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package emmy.eminentmagic.init;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import emmy.eminentmagic.client.gui.SeventyfiveByFifteenScreen;
import emmy.eminentmagic.client.gui.ManaSkillsSummaryGUIScreen;
import emmy.eminentmagic.client.gui.HundredByTwentyfiveScreen;

@EventBusSubscriber(Dist.CLIENT)
public class EminentMagicModScreens {
	@SubscribeEvent
	public static void clientLoad(RegisterMenuScreensEvent event) {
		event.register(EminentMagicModMenus.MANA_SKILLS_SUMMARY_GUI.get(), ManaSkillsSummaryGUIScreen::new);
		event.register(EminentMagicModMenus.HUNDRED_BY_TWENTYFIVE.get(), HundredByTwentyfiveScreen::new);
		event.register(EminentMagicModMenus.SEVENTYFIVE_BY_FIFTEEN.get(), SeventyfiveByFifteenScreen::new);
	}

	public interface ScreenAccessor {
		void updateMenuState(int elementType, String name, Object elementState);
	}
}