package emmy.eminentmagic.client.screens;

import org.checkerframework.checker.units.qual.h;

import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.Minecraft;

import emmy.eminentmagic.procedures.GetCurrentManaProcedure;

@EventBusSubscriber(Dist.CLIENT)
public class OverlayManaStatsOverlay {
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void eventHandler(RenderGuiEvent.Pre event) {
		int w = event.getGuiGraphics().guiWidth();
		int h = event.getGuiGraphics().guiHeight();
		Level world = null;
		double x = 0;
		double y = 0;
		double z = 0;
		Player entity = Minecraft.getInstance().player;
		if (entity != null) {
			world = entity.level();
			x = entity.getX();
			y = entity.getY();
			z = entity.getZ();
		}
		if (true) {
			event.getGuiGraphics().blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("eminent_magic:textures/screens/hundred_by_twentyfive.png"), w - 103, h - 28, 0, 0, 100, 25, 100, 25);

			event.getGuiGraphics().drawString(Minecraft.getInstance().font, Component.translatable("gui.eminent_magic.overlay_mana_stats.label_mana"), w - 100, h - 25, -13421773, false);
			event.getGuiGraphics().drawString(Minecraft.getInstance().font,

					GetCurrentManaProcedure.execute(entity), w - 72, h - 25, -3342337, false);
			event.getGuiGraphics().drawString(Minecraft.getInstance().font, Component.translatable("gui.eminent_magic.overlay_mana_stats.label_suppression"), w - 100, h - 16, -10066330, false);
			event.getGuiGraphics().drawString(Minecraft.getInstance().font, Component.translatable("gui.eminent_magic.overlay_mana_stats.label_35"), w - 31, h - 16, -3342337, false);
		}
	}
}