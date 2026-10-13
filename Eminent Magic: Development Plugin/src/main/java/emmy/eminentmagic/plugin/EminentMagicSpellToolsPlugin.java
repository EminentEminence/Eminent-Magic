package emmy.eminentmagic.plugin;

import emmy.eminentmagic.plugin.element.SpellModElementType;
import emmy.eminentmagic.plugin.element.StaffModElementType;
import net.mcreator.element.ModElementType;
import net.mcreator.element.ModElementTypeLoader;
import net.mcreator.generator.GeneratorFlavor;
import net.mcreator.generator.GeneratorWrapper;
import net.mcreator.plugin.JavaPlugin;
import net.mcreator.plugin.Plugin;
import net.mcreator.plugin.events.ModifyTemplateResultEvent;
import net.mcreator.plugin.events.PreGeneratorsLoadingEvent;
import net.mcreator.workspace.Workspace;
import net.mcreator.workspace.elements.ModElement;

import java.util.Set;

public class EminentMagicSpellToolsPlugin extends JavaPlugin {
    private static final String CORE_MOD_ID = "eminent_magic_core";
    private static final String MANIFEST_TEMPLATE = "modbase/neoforge.mods.toml.ftl";

    public EminentMagicSpellToolsPlugin(Plugin plugin) {
        super(plugin);

        addListener(PreGeneratorsLoadingEvent.class, event -> {
            ModElementTypeLoader.register(new SpellModElementType().coveredOn(GeneratorFlavor.NEOFORGE));
            ModElementTypeLoader.register(new StaffModElementType().coveredOn(GeneratorFlavor.NEOFORGE));
        });

        addListener(ModifyTemplateResultEvent.class, event -> {
            if (!event.getTemplateName().endsWith(MANIFEST_TEMPLATE)) {
                return;
            }

            Object workspaceModel = event.getDataModel().get("w");
            if (!(workspaceModel instanceof GeneratorWrapper generatorWrapper)) {
                return;
            }

            Workspace workspace = generatorWrapper.getWorkspace();
            if (CORE_MOD_ID.equals(workspace.getWorkspaceSettings().getModID()) || !usesPluginElements(workspace)) {
                return;
            }

            String output = event.getTemplateOutput();
            String marker = "# Start of user code block dependencies configuration";
            if (!output.contains(marker) || output.contains("modId=\"" + CORE_MOD_ID + "\"")) {
                return;
            }

            String dependency = "[[dependencies." + workspace.getWorkspaceSettings().getModID() + "]]\n"
                    + "    modId=\"" + CORE_MOD_ID + "\"\n"
                    + "    type=\"required\"\n"
                    + "    versionRange=\"[1.0.0,)\"\n"
                    + "    ordering=\"AFTER\"\n"
                    + "    side=\"BOTH\"\n\n";
            event.setTemplateOutput(output.replace(marker, dependency + marker));
        });
    }

    private static boolean usesPluginElements(Workspace workspace) {
        Set<String> pluginElementTypes = Set.of("spell", "staff");
        return workspace.getModElements().stream()
                .map(ModElement::getTypeString)
                .anyMatch(pluginElementTypes::contains);
    }
}
