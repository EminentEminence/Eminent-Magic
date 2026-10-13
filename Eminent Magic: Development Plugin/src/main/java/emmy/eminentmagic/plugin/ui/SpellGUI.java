package emmy.eminentmagic.plugin.ui;

import emmy.eminentmagic.plugin.element.Spell;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.modgui.ModElementGUI;
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.workspace.elements.ModElement;
import net.mcreator.ui.help.HelpUtils;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.GridLayout;
import java.net.URI;
import java.net.URISyntaxException;

public class SpellGUI extends ModElementGUI<Spell> {
    private final VTextField spellName;
    private final JSpinner manaCost;
    private final JSpinner controlScaling;
    private final JSpinner strengthScaling;
    private final JComboBox<String> element;

    public SpellGUI(MCreator mcreator, ModElement modElement, boolean editingMode) {
        super(mcreator, modElement, editingMode);

        spellName = new VTextField(28)
            .requireValue("elementgui.spell.error_empty_spell_name")
            .enableRealtimeValidation();
        spellName.setText(modElement.getName());
        manaCost = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1_000_000.0, 0.1));
        controlScaling = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1_000_000.0, 0.1));
        strengthScaling = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1_000_000.0, 0.1));
        element = new JComboBox<>(new String[] { "fire", "water", "earth", "wind" });

        initGUI();
        finalizeGUI();
    }

    @Override
    protected void initGUI() {
        JPanel fields = new JPanel(new GridLayout(5, 2, 5, 2));
        fields.setOpaque(false);

        addProperty(fields, "spell_name", "elementgui.spell.spell_name", spellName);
        addProperty(fields, "mana_cost", "elementgui.spell.mana_cost", manaCost);
        addProperty(fields, "control_scaling", "elementgui.spell.control_scaling", controlScaling);
        addProperty(fields, "strength_scaling", "elementgui.spell.strength_scaling", strengthScaling);
        addProperty(fields, "element", "elementgui.spell.element", element);

        addPage(PanelUtils.totalCenterInPanel(fields)).validate(spellName);
    }

    @Override
    protected void openInEditingMode(Spell spell) {
        spellName.setText(spell.spellName);
        manaCost.setValue(spell.manaCost);
        controlScaling.setValue(spell.controlScaling);
        strengthScaling.setValue(spell.strengthScaling);
        element.setSelectedItem(spell.element);
    }

    @Override
    public Spell getElementFromGUI() {
        Spell spell = new Spell(modElement);
        spell.spellName = spellName.getText().trim();
        spell.manaCost = ((Number) manaCost.getValue()).doubleValue();
        spell.controlScaling = ((Number) controlScaling.getValue()).doubleValue();
        spell.strengthScaling = ((Number) strengthScaling.getValue()).doubleValue();
        spell.element = (String) element.getSelectedItem();
        return spell;
    }

    @Override
    public URI contextURL() throws URISyntaxException {
        return null;
    }

    private void addProperty(JPanel panel, String property, String labelKey, java.awt.Component input) {
        panel.add(HelpUtils.wrapWithHelpButton(withEntry("spell/" + property), L10N.label(labelKey)));
        panel.add(input);
    }
}
