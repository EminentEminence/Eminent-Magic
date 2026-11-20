package com.eminentmagic.development;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSeparator;
import javax.swing.SpinnerNumberModel;

import net.mcreator.blockly.data.Dependency;
import net.mcreator.element.parts.procedure.Procedure;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.help.IHelpContext;
import net.mcreator.ui.modgui.ModElementGUI;
import net.mcreator.ui.procedure.ProcedureSelector;
import net.mcreator.ui.validation.Validator.ValidationResult;
import net.mcreator.ui.validation.Validator.ValidationResultType;
import net.mcreator.ui.validation.component.VComboBox;
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.workspace.elements.ModElement;

public final class SpellGUI extends ModElementGUI<Spell> {
	private static final String[] ELEMENTS = {
			"Pyra (Fire)", "Umbra (Dark)", "Glacies (Ice)", "Arcanum (Arcane)",
			"Nihil (Void)", "Aether (Air)", "Gaia (Earth)", "Viridia (Nature)",
			"Delphira (Water)", "Tempestris (Lighting)", "Helios (Light)", "Astraeus (Space)"
	};
	private static final Dependency[] SPELL_DEPENDENCIES = Dependency.fromString(
			"x:number/y:number/z:number/world:world/entity:entity/sourceentity:entity/itemstack:itemstack/blockstate:blockstate/direction:direction/spellControl:number/spellStrength:number");

	private final VTextField spellName = new VTextField(28).requireValue("Spell name is required");
	private final VTextField spellId = new VTextField(32).requireValue("Spell ID is required");
	private final JSpinner baseCost = numberField(0, 0, 1_000_000_000, 0.1);
	private final JSpinner strengthScaling = numberField(0, -1_000_000_000, 1_000_000_000, 0.1);
	private final JSpinner controlScaling = numberField(0, -1_000_000_000, 1_000_000_000, 0.1);
	private final VComboBox<String> element = new VComboBox<>(ELEMENTS);
	private final VComboBox<CreativeTabSelection.Option> creativeTab = new VComboBox<>(CreativeTabSelection.OPTIONS);
	private final ItemAppearanceControls appearance;
	private final JCheckBox channelled = new JCheckBox("Channelled spell");
	private final ProcedureSelector onCast;
	private final ProcedureSelector whileCharging;
	private final ProcedureSelector whileChannelling;
	private final String namespace;

	public SpellGUI(MCreator mcreator, ModElement modElement, boolean editingMode) {
		super(mcreator, modElement, editingMode);
		namespace = mcreator.getWorkspace().getWorkspaceSettings().getModID();
		appearance = new ItemAppearanceControls(mcreator, "Normal");
		IHelpContext help = withEntry("eminent_magic/spell");
		onCast = new ProcedureSelector(help, mcreator, "Run when cast", SPELL_DEPENDENCIES);
		whileCharging = new ProcedureSelector(help, mcreator, "Run while charging", SPELL_DEPENDENCIES);
		whileChannelling = new ProcedureSelector(help, mcreator, "Run while channelling", SPELL_DEPENDENCIES);
		onCast.refreshList(null);
		whileCharging.refreshList(null);
		whileChannelling.refreshList(null);
		initGUI();
		finalizeGUI();
	}

	@Override
	protected void initGUI() {
		spellName.enableRealtimeValidation();
		setToolTip(spellName, "Human-readable name for identifying this spell. It is separate from the spell ID.");
		setToolTip(spellId, "<html>Enter only the path portion; the workspace namespace is added automatically (namespace:path).<br>Use lowercase letters, digits, '.', '_', '-', or '/'. Keep the ID stable once other content references it.</html>");
		setToolTip(baseCost, "<html>Base mana cost before scaling. The cost formula is:<br>base + base * (spell control * control scaling / 10) * (spell strength * strength scaling / 10).<br>If either scaling value is zero, the cost is the base cost.</html>");
		setToolTip(strengthScaling, "<html>Coefficient for the caster's spell strength in the mana-cost formula. It combines multiplicatively with control scaling.<br>Zero disables the scaling term; negative values can produce a lower or negative final cost.</html>");
		setToolTip(controlScaling, "<html>Coefficient for the caster's spell control in the mana-cost formula. It combines multiplicatively with strength scaling.<br>Zero disables the scaling term; negative values can produce a lower or negative final cost.</html>");
		setToolTip(element, "<html>Choose the affinity that best describes this spell. This classifies the spell;<br>the procedures define what the spell actually does.</html>");
		setToolTip(channelled, "<html>Select for a sustained spell that continues while the caster channels it.<br>Use the While channelling procedure for its repeated effect; mana is drained until it stops or runs out.</html>");
		setToolTip(creativeTab, "Choose the creative tab where this spell item should appear.");
		setToolTip(onCast, "<html>Runs once after the spell finishes casting, not once per tick. Use for one-time effects.<br>Dependencies include position, world, entity, item, block state, direction, spell control, and spell strength.</html>");
		setToolTip(whileCharging, "<html>Runs every tick during charge-up, before the cast completes. Its logic repeats, so guard one-time actions.<br>Dependencies include position, world, entity, item, block state, direction, spell control, and spell strength.</html>");
		setToolTip(whileChannelling, "<html>Runs every tick while a channelled spell remains active. Use for ongoing effects, not unguarded one-time actions.<br>Dependencies include position, world, entity, item, block state, direction, spell control, and spell strength.</html>");
		makeControlLarger(spellName);
		makeControlLarger(spellId);
		makeControlLarger(baseCost);
		makeControlLarger(strengthScaling);
		makeControlLarger(controlScaling);
		makeControlLarger(element);
		makeControlLarger(creativeTab);
		makeControlLarger(channelled);
		spellId.setValidator(() -> {
			if (spellId.getText().trim().matches("[a-z0-9_./-]+")) {
				return new ValidationResult(ValidationResultType.PASSED, "");
			}
			return new ValidationResult(ValidationResultType.ERROR,
					"Use lowercase letters, digits, '.', '_', '-', or '/' without a namespace.");
		});
		spellId.enableRealtimeValidation();

		JPanel properties = new JPanel(new GridBagLayout());
		properties.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
		JPanel namespacedId = new JPanel(new BorderLayout(8, 0));
		JLabel namespaceLabel = new JLabel(namespace + ":");
		namespaceLabel.setFont(spellId.getFont());
		namespacedId.setToolTipText(spellId.getToolTipText());
		namespaceLabel.setToolTipText(spellId.getToolTipText());
		namespacedId.add(namespaceLabel, BorderLayout.WEST);
		namespacedId.add(spellId, BorderLayout.CENTER);
		int row = 0;
		row = addField(properties, row, "Spell name", spellName);
		row = addField(properties, row, "Spell ID", namespacedId);
		row = addField(properties, row, "Base cost", baseCost);
		row = addField(properties, row, "Strength scaling", strengthScaling);
		row = addField(properties, row, "Control scaling", controlScaling);
		row = addField(properties, row, "Element", element);
		row = addField(properties, row, "Creative tab", creativeTab);
		addField(properties, row, "Channelled", channelled);
		addPage("Properties", properties);
		addPage("Appearance", appearance.createPanel());

		JPanel triggers = new JPanel(new GridBagLayout());
		triggers.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		int triggerRow = 0;
		triggerRow = addTriggerSection(triggers, triggerRow, "On cast", "Runs once after the spell finishes casting.", onCast);
		triggerRow = addTriggerSection(triggers, triggerRow, "While charging", "Runs every tick while the spell is charging.", whileCharging);
		addTriggerSection(triggers, triggerRow, "While channelling", "Runs every tick while the channelled spell is active.", whileChannelling);
		addPage("Triggers", triggers);
	}

	@Override
	protected void openInEditingMode(Spell spell) {
		spellName.setText(spell.spellName);
		spellId.setText(spell.spellId);
		baseCost.setValue(spell.baseCost);
		strengthScaling.setValue(spell.strengthScaling);
		controlScaling.setValue(spell.controlScaling);
		element.setSelectedItem(spell.element);
		creativeTab.setSelectedItem(CreativeTabSelection.optionFor(spell.creativeTab,
				CreativeTabSelection.SPELL_DEFAULT));
		channelled.setSelected(spell.channelled);
		appearance.load(spell.texture, spell.guiTexture, spell.modelType);
		onCast.setSelectedProcedure(spell.onCast);
		whileCharging.setSelectedProcedure(spell.whileCharging);
		whileChannelling.setSelectedProcedure(spell.whileChannelling);
	}

	@Override
	public Spell getElementFromGUI() {
		Spell spell = new Spell(modElement);
		spell.spellName = spellName.getText().trim();
		spell.spellId = spellId.getText().trim();
		spell.baseCost = ((Number) baseCost.getValue()).doubleValue();
		spell.strengthScaling = ((Number) strengthScaling.getValue()).doubleValue();
		spell.controlScaling = ((Number) controlScaling.getValue()).doubleValue();
		spell.element = (String) element.getSelectedItem();
		spell.creativeTab = CreativeTabSelection.idFor(creativeTab.getSelectedItem());
		spell.channelled = channelled.isSelected();
		spell.texture = appearance.getTexture();
		spell.guiTexture = appearance.getGUITexture();
		spell.modelType = appearance.getModelType();
		spell.onCast = onCast.getSelectedProcedure();
		spell.whileCharging = whileCharging.getSelectedProcedure();
		spell.whileChannelling = whileChannelling.getSelectedProcedure();
		return spell;
	}

	@Override
	public String contextName() {
		return "Spell";
	}

	@Override
	public URI contextURL() {
		return URI.create("https://mcreator.net/wiki");
	}

	private static JSpinner numberField(double value, double minimum, double maximum, double step) {
		return new JSpinner(new SpinnerNumberModel(value, minimum, maximum, step));
	}

	private static void makeControlLarger(JComponent component) {
		Font font = component.getFont().deriveFont(Math.max(14f, component.getFont().getSize2D() + 1f));
		component.setFont(font);
		Dimension preferredSize = component.getPreferredSize();
		component.setPreferredSize(new Dimension(Math.max(220, preferredSize.width), Math.max(36, preferredSize.height)));
		if (component instanceof JSpinner spinner && spinner.getEditor() instanceof JSpinner.DefaultEditor editor) {
			editor.getTextField().setFont(font);
		}
	}

	private static void setToolTip(JComponent component, String text) {
		component.setToolTipText(text);
		for (Component child : component.getComponents()) {
			if (child instanceof JComponent childComponent) {
				setToolTip(childComponent, text);
			}
		}
	}

	private static int addField(JPanel panel, int row, String label, JComponent component) {
		JLabel fieldLabel = new JLabel(label);
		fieldLabel.setFont(fieldLabel.getFont().deriveFont(Font.BOLD, 14f));
		fieldLabel.setToolTipText(component.getToolTipText());
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

	private static int addTriggerSection(JPanel panel, int row, String title, String description,
			ProcedureSelector selector) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = row++;
		constraints.weightx = 1;
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets.set(12, 8, 4, 8);
		JLabel heading = new JLabel(title);
		heading.setFont(heading.getFont().deriveFont(Font.BOLD, 16f));
		panel.add(heading, constraints);

		constraints.gridy = row++;
		constraints.insets.set(0, 8, 8, 8);
		JLabel detail = new JLabel(description);
		detail.setFont(detail.getFont().deriveFont(13f));
		panel.add(detail, constraints);

		constraints.gridy = row++;
		constraints.insets.set(4, 8, 10, 8);
		panel.add(selector, constraints);
		constraints.gridy = row++;
		constraints.insets.set(2, 8, 2, 8);
		panel.add(new JSeparator(), constraints);
		return row;
	}
}