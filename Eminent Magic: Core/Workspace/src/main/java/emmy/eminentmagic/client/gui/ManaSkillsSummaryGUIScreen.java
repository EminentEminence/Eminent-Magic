package emmy.eminentmagic.client.gui;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;

import emmy.eminentmagic.world.inventory.ManaSkillsSummaryGUIMenu;
import emmy.eminentmagic.procedures.GetManaStrengthProcedure;
import emmy.eminentmagic.procedures.GetManaReservesProcedure;
import emmy.eminentmagic.procedures.GetManaPerceptionProcedure;
import emmy.eminentmagic.procedures.GetManaControlProcedure;
import emmy.eminentmagic.procedures.DoesPlayerHaveOpProcedure;
import emmy.eminentmagic.network.ManaSkillsSummaryGUIButtonMessage;
import emmy.eminentmagic.init.EminentMagicModScreens;

public class ManaSkillsSummaryGUIScreen extends AbstractContainerScreen<ManaSkillsSummaryGUIMenu> implements EminentMagicModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	private EditBox ManaReserves;
	private EditBox ManaStrength;
	private EditBox ManaControl;
	private EditBox ManaPerception;
	private Button button_override;

	public ManaSkillsSummaryGUIScreen(ManaSkillsSummaryGUIMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 176;
		this.imageHeight = 166;
	}

	@Override
	public void updateMenuState(int elementType, String name, Object elementState) {
		menuStateUpdateActive = true;
		if (elementType == 0 && elementState instanceof String stringState) {
			if (name.equals("ManaReserves"))
				ManaReserves.setValue(stringState);
			else if (name.equals("ManaStrength"))
				ManaStrength.setValue(stringState);
			else if (name.equals("ManaControl"))
				ManaControl.setValue(stringState);
			else if (name.equals("ManaPerception"))
				ManaPerception.setValue(stringState);
		}
		menuStateUpdateActive = false;
	}

	private static final ResourceLocation texture = ResourceLocation.parse("eminent_magic:textures/screens/mana_skills_summary_gui.png");

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		ManaReserves.render(guiGraphics, mouseX, mouseY, partialTicks);
		ManaStrength.render(guiGraphics, mouseX, mouseY, partialTicks);
		ManaControl.render(guiGraphics, mouseX, mouseY, partialTicks);
		ManaPerception.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		if (ManaReserves.isFocused())
			return ManaReserves.keyPressed(key, b, c);
		if (ManaStrength.isFocused())
			return ManaStrength.keyPressed(key, b, c);
		if (ManaControl.isFocused())
			return ManaControl.keyPressed(key, b, c);
		if (ManaPerception.isFocused())
			return ManaPerception.keyPressed(key, b, c);
		return super.keyPressed(key, b, c);
	}

	@Override
	public void resize(Minecraft minecraft, int width, int height) {
		String ManaReservesValue = ManaReserves.getValue();
		String ManaStrengthValue = ManaStrength.getValue();
		String ManaControlValue = ManaControl.getValue();
		String ManaPerceptionValue = ManaPerception.getValue();
		super.resize(minecraft, width, height);
		ManaReserves.setValue(ManaReservesValue);
		ManaStrength.setValue(ManaStrengthValue);
		ManaControl.setValue(ManaControlValue);
		ManaPerception.setValue(ManaPerceptionValue);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(this.font, Component.translatable("gui.eminent_magic.mana_skills_summary_gui.label_mana_skills_summary"), 5, 5, -12829636, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.eminent_magic.mana_skills_summary_gui.label_reserves"), 9, 24, -12829636, false);
		guiGraphics.drawString(this.font, GetManaReservesProcedure.execute(entity), 70, 24, -12829636, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.eminent_magic.mana_skills_summary_gui.label_strength"), 9, 36, -12829636, false);
		guiGraphics.drawString(this.font, GetManaStrengthProcedure.execute(entity), 70, 36, -12829636, false);
		guiGraphics.drawString(this.font, GetManaControlProcedure.execute(entity), 70, 48, -12829636, false);
		guiGraphics.drawString(this.font, GetManaPerceptionProcedure.execute(entity), 70, 60, -12829636, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.eminent_magic.mana_skills_summary_gui.label_control"), 9, 48, -12829636, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.eminent_magic.mana_skills_summary_gui.label_perception"), 9, 60, -12829636, false);
	}

	@Override
	public void init() {
		super.init();
		ManaReserves = new EditBox(this.font, this.leftPos + -118, this.topPos + 22, 118, 18, Component.translatable("gui.eminent_magic.mana_skills_summary_gui.ManaReserves"));
		ManaReserves.setMaxLength(8192);
		ManaReserves.setResponder(content -> {
			if (!menuStateUpdateActive)
				menu.sendMenuStateUpdate(entity, 0, "ManaReserves", content, false);
		});
		ManaReserves.setHint(Component.translatable("gui.eminent_magic.mana_skills_summary_gui.ManaReserves"));
		this.addWidget(this.ManaReserves);
		ManaStrength = new EditBox(this.font, this.leftPos + -118, this.topPos + 42, 118, 18, Component.translatable("gui.eminent_magic.mana_skills_summary_gui.ManaStrength"));
		ManaStrength.setMaxLength(8192);
		ManaStrength.setResponder(content -> {
			if (!menuStateUpdateActive)
				menu.sendMenuStateUpdate(entity, 0, "ManaStrength", content, false);
		});
		ManaStrength.setHint(Component.translatable("gui.eminent_magic.mana_skills_summary_gui.ManaStrength"));
		this.addWidget(this.ManaStrength);
		ManaControl = new EditBox(this.font, this.leftPos + -118, this.topPos + 62, 118, 18, Component.translatable("gui.eminent_magic.mana_skills_summary_gui.ManaControl"));
		ManaControl.setMaxLength(8192);
		ManaControl.setResponder(content -> {
			if (!menuStateUpdateActive)
				menu.sendMenuStateUpdate(entity, 0, "ManaControl", content, false);
		});
		ManaControl.setHint(Component.translatable("gui.eminent_magic.mana_skills_summary_gui.ManaControl"));
		this.addWidget(this.ManaControl);
		ManaPerception = new EditBox(this.font, this.leftPos + -118, this.topPos + 81, 118, 18, Component.translatable("gui.eminent_magic.mana_skills_summary_gui.ManaPerception"));
		ManaPerception.setMaxLength(8192);
		ManaPerception.setResponder(content -> {
			if (!menuStateUpdateActive)
				menu.sendMenuStateUpdate(entity, 0, "ManaPerception", content, false);
		});
		ManaPerception.setHint(Component.translatable("gui.eminent_magic.mana_skills_summary_gui.ManaPerception"));
		this.addWidget(this.ManaPerception);
		button_override = Button.builder(Component.translatable("gui.eminent_magic.mana_skills_summary_gui.button_override"), e -> {
			int x = ManaSkillsSummaryGUIScreen.this.x;
			int y = ManaSkillsSummaryGUIScreen.this.y;
			if (DoesPlayerHaveOpProcedure.execute(entity)) {
				ClientPacketDistributor.sendToServer(new ManaSkillsSummaryGUIButtonMessage(0, x, y, z));
				ManaSkillsSummaryGUIButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		}).bounds(this.leftPos + -92, this.topPos + 99, 67, 20).build();
		this.addRenderableWidget(button_override);
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		this.button_override.visible = DoesPlayerHaveOpProcedure.execute(entity);
	}
}