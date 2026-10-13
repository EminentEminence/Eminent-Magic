package emmy.eminentmagic.plugin.element;

import emmy.eminentmagic.plugin.ui.StaffGUI;
import net.mcreator.element.ModElementType;
import net.mcreator.ui.init.UIRES;

import javax.swing.ImageIcon;

public class StaffModElementType extends ModElementType<Staff> {
    public StaffModElementType() {
        super("staff", null, StaffGUI::new, Staff.class);
    }

    @Override
    public ImageIcon getIcon() {
        return UIRES.get("mod_types.item");
    }
}