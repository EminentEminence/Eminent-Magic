package emmy.eminentmagic.plugin.element;

import emmy.eminentmagic.plugin.ui.SpellGUI;
import net.mcreator.element.ModElementType;
import net.mcreator.ui.init.UIRES;

import javax.swing.ImageIcon;

public class SpellModElementType extends ModElementType<Spell> {
    public SpellModElementType() {
        super("spell", null, SpellGUI::new, Spell.class);
    }

    @Override
    public ImageIcon getIcon() {
        return UIRES.get("mod_types.procedure");
    }
}