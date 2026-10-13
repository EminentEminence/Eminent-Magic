package net.emmy.eminentmagic.core;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = EminentMagicCoreMod.MODID, value = Dist.CLIENT)
public final class EminentMagicCoreStaffScreen extends AbstractContainerScreen<EminentMagicCoreStaffMenu> {
	private static final ResourceLocation INVENTORY_SCREEN_BACKGROUND = ResourceLocation.parse("minecraft:textures/gui/container/inventory.png");
	private static final ResourceLocation CONTAINER_INVENTORY_BACKGROUND = ResourceLocation.parse("minecraft:textures/gui/container/generic_54.png");
	private static final ResourceLocation SLOT_BACKGROUND = ResourceLocation.parse("minecraft:container/slot");

	public EminentMagicCoreStaffScreen(EminentMagicCoreStaffMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth = 176;
		this.imageHeight = 244;
		this.inventoryLabelY = 152;
		this.titleLabelY = 0;
	}

	@SubscribeEvent
	public static void registerScreens(RegisterMenuScreensEvent event) {
		event.register(EminentMagicCoreStaffMenu.getMenuType(), EminentMagicCoreStaffScreen::new);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		graphics.fill(leftPos, topPos - 3, leftPos + imageWidth, topPos + imageHeight, 0xFF373737);
		graphics.fill(leftPos + 1, topPos - 2, leftPos + imageWidth - 1, topPos + imageHeight - 1, 0xFF8B8B8B);
		graphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY_SCREEN_BACKGROUND, leftPos + 3, topPos - 1, 80, 10, imageWidth - 6, imageHeight, 1, 1, 256, 256);
		graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_INVENTORY_BACKGROUND, leftPos, topPos + 150, 0, 126, imageWidth, 94, 256, 256);
		for (int slotIndex = 0; slotIndex < menu.getStaffSlotCount(); slotIndex++) {
			var slot = menu.slots.get(slotIndex);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_BACKGROUND, leftPos + slot.x - 1, topPos + slot.y - 1, 18, 18);
		}
	}
}