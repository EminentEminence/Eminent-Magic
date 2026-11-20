package com.eminentmagic.development;

import net.mcreator.element.GeneratableElement;
import net.mcreator.element.parts.TextureHolder;
import net.mcreator.element.parts.procedure.Procedure;
import net.mcreator.workspace.elements.ModElement;

public final class Spell extends GeneratableElement {
	public String spellName = "";
	public String spellId = "";
	public double baseCost;
	public double strengthScaling;
	public double controlScaling;
	public String element = "Pyra (Fire)";
	public boolean channelled;
	public String creativeTab = CreativeTabSelection.SPELL_DEFAULT;
	public TextureHolder texture;
	public TextureHolder guiTexture;
	public String modelType = "Normal";
	public Procedure onCast;
	public Procedure whileCharging;
	public Procedure whileChannelling;

	public Spell(ModElement element) {
		super(element);
	}

	public boolean hasGUITexture() {
		return guiTexture != null && !guiTexture.isEmpty();
	}
}