package emmy.eminentmagic.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import emmy.eminentmagic.world.inventory.StaffSpellSlotsMenu;

public final class StaffSpellSlotsScreen extends AbstractContainerScreen<StaffSpellSlotsMenu> {
	private final Component playerInventoryTitle;

	public StaffSpellSlotsScreen(StaffSpellSlotsMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth = 176;
		this.imageHeight = menu.getImageHeight();
		this.playerInventoryTitle = inventory.getDisplayName();
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);
		graphics.fill(leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xFF8B8B8B);
		for (int slot = 0; slot < menu.getStaffSlotCount(); slot++) {
			drawSlot(graphics, leftPos + 8 + (slot % 9) * 18, topPos + 18 + (slot / 9) * 18);
		}
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				drawSlot(graphics, leftPos + 8 + column * 18, topPos + menu.getInventoryTop() + row * 18);
			}
		}
		for (int column = 0; column < 9; column++) {
			drawSlot(graphics, leftPos + 8 + column * 18, topPos + menu.getInventoryTop() + 58);
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(font, title, 8, 6, 0xFF404040, false);
		graphics.drawString(font, playerInventoryTitle, 8, menu.getInventoryTop() - 10, 0xFF404040, false);
	}

	private static void drawSlot(GuiGraphics graphics, int x, int y) {
		graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF373737);
		graphics.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
	}
}
