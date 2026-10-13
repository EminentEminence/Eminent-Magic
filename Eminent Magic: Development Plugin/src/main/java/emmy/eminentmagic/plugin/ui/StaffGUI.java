package emmy.eminentmagic.plugin.ui;

import emmy.eminentmagic.plugin.element.Staff;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.dialogs.TypedTextureSelectorDialog;
import net.mcreator.ui.help.HelpUtils;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.minecraft.TabListField;
import net.mcreator.ui.minecraft.TextureSelectionButton;
import net.mcreator.ui.modgui.ModElementGUI;
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.ui.workspace.resources.TextureType;
import net.mcreator.workspace.elements.ModElement;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.GridLayout;
import java.net.URI;
import java.net.URISyntaxException;

public class StaffGUI extends ModElementGUI<Staff> {
    private final VTextField staffName;
    private final TextureSelectionButton texture;
    private final JSpinner spellSlots;
    private final TabListField creativeTabs;

    public StaffGUI(MCreator mcreator, ModElement modElement, boolean editingMode) {
        super(mcreator, modElement, editingMode);

        staffName = new VTextField(28)
                .requireValue("elementgui.staff.error_empty_name")
                .enableRealtimeValidation();
        staffName.setText(modElement.getName());
        texture = new TextureSelectionButton(new TypedTextureSelectorDialog(mcreator, TextureType.ITEM))
                .requireValue("elementgui.staff.error_texture");
        spellSlots = new JSpinner(new SpinnerNumberModel(0, 0, 12, 1));
        creativeTabs = new TabListField(mcreator);

        initGUI();
        finalizeGUI();
    }

    @Override
    protected void initGUI() {
        JPanel fields = new JPanel(new GridLayout(4, 2, 5, 2));
        fields.setOpaque(false);

        addProperty(fields, "staff_name", "elementgui.staff.name", staffName);
        addProperty(fields, "texture", "elementgui.staff.texture", texture);
        addProperty(fields, "spell_slots", "elementgui.staff.spell_slots", spellSlots);
        addProperty(fields, "creative_tabs", "elementgui.staff.creative_tabs", creativeTabs);

        addPage(PanelUtils.totalCenterInPanel(fields)).validate(staffName).validate(texture);
    }

    @Override
    protected void openInEditingMode(Staff staff) {
        staffName.setText(staff.staffName);
        if (staff.texture != null) {
            texture.setTexture(staff.texture);
        }
        spellSlots.setValue(staff.spellSlots);
        creativeTabs.setListElements(staff.creativeTabs);
    }

    @Override
    public Staff getElementFromGUI() {
        Staff staff = new Staff(modElement);
        staff.staffName = staffName.getText().trim();
        staff.texture = texture.getTextureHolder();
        staff.spellSlots = ((Number) spellSlots.getValue()).intValue();
        staff.creativeTabs = creativeTabs.getListElements();
        return staff;
    }

    @Override
    public URI contextURL() throws URISyntaxException {
        return null;
    }

    private void addProperty(JPanel panel, String property, String labelKey, java.awt.Component input) {
        panel.add(HelpUtils.wrapWithHelpButton(withEntry("staff/" + property), L10N.label(labelKey)));
        panel.add(input);
    }
}