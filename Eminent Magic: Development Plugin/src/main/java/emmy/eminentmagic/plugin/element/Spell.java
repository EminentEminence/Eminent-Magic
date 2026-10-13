package emmy.eminentmagic.plugin.element;

import net.mcreator.element.GeneratableElement;
import net.mcreator.workspace.elements.ModElement;

public class Spell extends GeneratableElement {
    public String spellName = "";
    public double manaCost;
    public double controlScaling;
    public double strengthScaling;
    public String element = "fire";

    private Spell() {
        this(null);
    }

    public Spell(ModElement modElement) {
        super(modElement);
    }
}
