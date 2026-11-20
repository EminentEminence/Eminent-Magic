package com.eminentmagic.development;

import net.mcreator.element.ModElementType;

public final class ModElementTypeLoader {
	public static final ModElementType<Spell> SPELL = new ModElementType<>(
			"eminent_magic_spell", 'S', SpellGUI::new, Spell.class);

	private ModElementTypeLoader() {
	}
}