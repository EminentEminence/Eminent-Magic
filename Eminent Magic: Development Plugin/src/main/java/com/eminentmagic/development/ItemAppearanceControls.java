package com.eminentmagic.development;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.mcreator.element.parts.TextureHolder;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.SearchableComboBox;
import net.mcreator.ui.dialogs.TypedTextureSelectorDialog;
import net.mcreator.ui.minecraft.TextureSelectionButton;
import net.mcreator.ui.modgui.ItemGUI;
import net.mcreator.ui.workspace.resources.TextureType;
import net.mcreator.workspace.Workspace;
import net.mcreator.workspace.resources.Model;

final class ItemAppearanceControls {
	private final Workspace workspace;
	private final SearchableComboBox<Model> model = new SearchableComboBox<>(ItemGUI.builtinitemmodels);
	private final TextureSelectionButton texture;
	private final TextureSelectionButton guiTexture;

	ItemAppearanceControls(MCreator mcreator, String defaultModel) {
		workspace = mcreator.getWorkspace();
		texture = new TextureSelectionButton(
				new TypedTextureSelectorDialog(mcreator, TextureType.ITEM));
		guiTexture = new TextureSelectionButton(
				new TypedTextureSelectorDialog(mcreator, TextureType.ITEM), 32);
		texture.setToolTipText("Select an item texture from this workspace. If empty, the default texture is used.");
		guiTexture.setToolTipText("Optional texture used when this item is displayed in a GUI, in an item frame, or on the ground.");
		model.setToolTipText("Choose the built-in item model style.");
		model.setSelectedItem(findModel(defaultModel));
	}

	JPanel createPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
		int row = 0;
		row = addField(panel, row, "Item model", model);
		row = addField(panel, row, "Item texture", texture);
		addField(panel, row, "GUI texture (optional)", guiTexture);
		return panel;
	}

	void load(TextureHolder itemTexture, TextureHolder itemGUITexture, String modelType) {
		setTexture(texture, itemTexture);
		setTexture(guiTexture, itemGUITexture);
		model.setSelectedItem(findModel(modelType));
	}

	TextureHolder getTexture() {
		return texture.hasTexture() ? texture.getTextureHolder() : null;
	}

	TextureHolder getGUITexture() {
		return guiTexture.hasTexture() ? guiTexture.getTextureHolder() : null;
	}

	String getModelType() {
		Model selected = model.getSelectedItem();
		return selected == null ? defaultModel().getReadableName() : selected.getReadableName();
	}

	private void setTexture(TextureSelectionButton button, TextureHolder texture) {
		if (texture != null && !texture.isEmpty()) {
			texture.setWorkspace(workspace);
			button.setTexture(texture);
		}
	}

	private static Model findModel(String modelType) {
		for (Model option : ItemGUI.builtinitemmodels) {
			if (option.getReadableName().equals(modelType)) {
				return option;
			}
		}
		return defaultModel();
	}

	private static Model defaultModel() {
		for (Model option : ItemGUI.builtinitemmodels) {
			if ("Normal".equals(option.getReadableName())) {
				return option;
			}
		}
		return ItemGUI.builtinitemmodels[0];
	}

	private static int addField(JPanel panel, int row, String label, java.awt.Component component) {
		JLabel fieldLabel = new JLabel(label);
		fieldLabel.setFont(fieldLabel.getFont().deriveFont(Font.BOLD, 14f));
		if (component instanceof javax.swing.JComponent swingComponent) {
			fieldLabel.setToolTipText(swingComponent.getToolTipText());
		}
		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.gridx = 0;
		labelConstraints.gridy = row;
		labelConstraints.anchor = GridBagConstraints.LINE_START;
		labelConstraints.insets.set(9, 8, 9, 16);
		panel.add(fieldLabel, labelConstraints);

		GridBagConstraints fieldConstraints = new GridBagConstraints();
		fieldConstraints.gridx = 1;
		fieldConstraints.gridy = row;
		fieldConstraints.weightx = 1;
		fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		fieldConstraints.insets.set(7, 0, 7, 12);
		panel.add(component, fieldConstraints);
		return row + 1;
	}
}
