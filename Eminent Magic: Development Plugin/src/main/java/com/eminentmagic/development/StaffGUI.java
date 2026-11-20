package com.eminentmagic.development;

import java.awt.BorderLayout;
import java.awt.Font;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import net.mcreator.ui.MCreator;
import net.mcreator.ui.modgui.ModElementGUI;
import net.mcreator.ui.validation.Validator.ValidationResult;
import net.mcreator.ui.validation.Validator.ValidationResultType;
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.ui.validation.component.VComboBox;
import net.mcreator.workspace.elements.ModElement;

public final class StaffGUI extends ModElementGUI<Staff> {
	private final VTextField staffName = new VTextField(28).requireValue("Staff name is required");
	private final VTextField staffId = new VTextField(32).requireValue("Staff ID is required");
	private final JSpinner slotCount = new JSpinner(new SpinnerNumberModel(1, 1, Staff.MAX_SPELL_SLOTS, 1));
	private final VComboBox<CreativeTabSelection.Option> creativeTab = new VComboBox<>(CreativeTabSelection.OPTIONS);
	private final ItemAppearanceControls appearance;
	private final String namespace;

	public StaffGUI(MCreator mcreator, ModElement modElement, boolean editingMode) {
		super(mcreator, modElement, editingMode);
		namespace = mcreator.getWorkspace().getWorkspaceSettings().getModID();
		appearance = new ItemAppearanceControls(mcreator, "Tool");
		staffName.setToolTipText("Human-readable name shown for this staff item.");
		staffId.setToolTipText("<html>Enter only the path portion; the workspace namespace is added automatically (namespace:path).<br>Use lowercase letters, digits, '.', '_', '-', or '/'. Keep the ID stable once other content references it.</html>");
		slotCount.setToolTipText("Number of spell slots this staff stores. Valid values are 1 to "
				+ Staff.MAX_SPELL_SLOTS + ".");
		creativeTab.setToolTipText("Choose the creative tab where this staff should appear.");
		staffId.setValidator(() -> {
			if (staffId.getText().trim().matches("[a-z0-9_./-]+")) {
				return new ValidationResult(ValidationResultType.PASSED, "");
			}
			return new ValidationResult(ValidationResultType.ERROR,
					"Use lowercase letters, digits, '.', '_', '-', or '/' without a namespace.");
		});
		staffName.enableRealtimeValidation();
		staffId.enableRealtimeValidation();
		initGUI();
		finalizeGUI();
	}

	@Override
	protected void initGUI() {
		JPanel properties = new JPanel(new java.awt.GridBagLayout());
		properties.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
		JPanel namespacedId = new JPanel(new BorderLayout(8, 0));
		JLabel namespaceLabel = new JLabel(namespace + ":");
		namespaceLabel.setFont(staffId.getFont());
		namespacedId.add(namespaceLabel, BorderLayout.WEST);
		namespacedId.add(staffId, BorderLayout.CENTER);

		int row = 0;
		row = addField(properties, row, "Staff name", staffName);
		row = addField(properties, row, "Staff ID", namespacedId);
		row = addField(properties, row, "Spell slots", slotCount);
		addField(properties, row, "Creative tab", creativeTab);
		addPage("Properties", properties);
		addPage("Appearance", appearance.createPanel());
	}

	@Override
	protected void openInEditingMode(Staff staff) {
		staffName.setText(staff.staffName);
		staffId.setText(staff.staffId);
		slotCount.setValue(staff.slotCount);
		creativeTab.setSelectedItem(CreativeTabSelection.optionFor(staff.creativeTab,
				CreativeTabSelection.STAFF_DEFAULT));
		appearance.load(staff.texture, staff.guiTexture, staff.modelType);
	}

	@Override
	public Staff getElementFromGUI() {
		Staff staff = new Staff(modElement);
		staff.staffName = staffName.getText().trim();
		staff.staffId = staffId.getText().trim();
		staff.slotCount = ((Number) slotCount.getValue()).intValue();
		staff.creativeTab = CreativeTabSelection.idFor(creativeTab.getSelectedItem());
		staff.texture = appearance.getTexture();
		staff.guiTexture = appearance.getGUITexture();
		staff.modelType = appearance.getModelType();
		return staff;
	}

	@Override
	public String contextName() {
		return "Staff";
	}

	@Override
	public URI contextURL() {
		return URI.create("https://mcreator.net/wiki");
	}

	private static int addField(JPanel panel, int row, String label, java.awt.Component component) {
		JLabel fieldLabel = new JLabel(label);
		fieldLabel.setFont(fieldLabel.getFont().deriveFont(Font.BOLD, 14f));
		fieldLabel.setToolTipText(component instanceof javax.swing.JComponent swingComponent
				? swingComponent.getToolTipText() : null);
		java.awt.GridBagConstraints labelConstraints = new java.awt.GridBagConstraints();
		labelConstraints.gridx = 0;
		labelConstraints.gridy = row;
		labelConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
		labelConstraints.insets.set(9, 8, 9, 16);
		panel.add(fieldLabel, labelConstraints);

		java.awt.GridBagConstraints fieldConstraints = new java.awt.GridBagConstraints();
		fieldConstraints.gridx = 1;
		fieldConstraints.gridy = row;
		fieldConstraints.weightx = 1;
		fieldConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		fieldConstraints.insets.set(7, 0, 7, 12);
		panel.add(component, fieldConstraints);
		return row + 1;
	}
}
