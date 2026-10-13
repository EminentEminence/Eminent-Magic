package emmy.eminentmagic.plugin.element;

import net.mcreator.element.GeneratableElement;
import net.mcreator.element.parts.TabEntry;
import net.mcreator.element.parts.TextureHolder;
import net.mcreator.workspace.elements.ModElement;

import java.util.ArrayList;
import java.util.List;

public class Staff extends GeneratableElement {
    public String staffName = "";
    public TextureHolder texture;
    public int spellSlots;
    public List<TabEntry> creativeTabs = new ArrayList<>();

    private Staff() {
        this(null);
    }

    public Staff(ModElement modElement) {
        super(modElement);
        if (modElement != null) {
            texture = new TextureHolder(modElement.getWorkspace(), "");
        }
    }
}