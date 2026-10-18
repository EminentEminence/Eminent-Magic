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

import emmy.eminentmagic.procedures.IsHoldingStaffProcedure;
import emmy.eminentmagic.procedures.GetSpellNameProcedure;
import emmy.eminentmagic.procedures.GetManaCostProcedure;
import emmy.eminentmagic.procedures.GetControlSpellStrengthProcedure;
import emmy.eminentmagic.procedures.GetControlSpellControlProcedure;

@EventBusSubscriber(Dist.CLIENT)
public class OverlaySpellInfoOverlay {
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
		if (IsHoldingStaffProcedure.execute(entity)) {
			event.getGuiGraphics().blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("eminent_magic:textures/screens/hundred_by_twentyfive.png"), 3, h - 28, 0, 0, 100, 25, 100, 25);

			event.getGuiGraphics().blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("eminent_magic:textures/screens/seventyfive_by_fifteen.png"), 3, h - 42, 0, 0, 75, 15, 75, 15);

			event.getGuiGraphics().blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.parse("eminent_magic:textures/screens/seventyfive_by_fifteen.png"), 3, h - 56, 0, 0, 75, 15, 75, 15);

			event.getGuiGraphics().drawString(Minecraft.getInstance().font,

					GetSpellNameProcedure.execute(entity), 6, h - 25, -13421773, false);
			event.getGuiGraphics().drawString(Minecraft.getInstance().font, Component.translatable("gui.eminent_magic.overlay_spell_info.label_mana"), 6, h - 16, -10066330, false);
			event.getGuiGraphics().drawString(Minecraft.getInstance().font,

					GetManaCostProcedure.execute(entity), 32, h - 16, -3342337, false);
			event.getGuiGraphics().drawString(Minecraft.getInstance().font, Component.translatable("gui.eminent_magic.overlay_spell_info.label_control"), 6, h - 39, -10066330, false);
			event.getGuiGraphics().drawString(Minecraft.getInstance().font, Component.translatable("gui.eminent_magic.overlay_spell_info.label_strength"), 6, h - 53, -10066330, false);
			event.getGuiGraphics().drawString(Minecraft.getInstance().font,

					GetControlSpellStrengthProcedure.execute(entity), 59, h - 53, -3342337, false);
			event.getGuiGraphics().drawString(Minecraft.getInstance().font,

					GetControlSpellControlProcedure.execute(entity), 59, h - 39, -3342337, false);
		}
	}
}