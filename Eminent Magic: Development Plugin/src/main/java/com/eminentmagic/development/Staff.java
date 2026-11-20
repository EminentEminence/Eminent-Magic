package com.eminentmagic.development;

import net.mcreator.element.GeneratableElement;
import net.mcreator.element.parts.TextureHolder;
import net.mcreator.workspace.elements.ModElement;

public final class Staff extends GeneratableElement {
	public static final int MAX_SPELL_SLOTS = 54;
	public String staffName = "";
	public String staffId = "";
	public int slotCount = 1;
	public String creativeTab = CreativeTabSelection.STAFF_DEFAULT;
	public TextureHolder texture;
	public TextureHolder guiTexture;
	public String modelType = "Tool";

	public Staff(ModElement element) {
		super(element);
	}

	public boolean hasGUITexture() {
		return guiTexture != null && !guiTexture.isEmpty();
	}
}
