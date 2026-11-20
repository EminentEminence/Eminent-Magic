package com.eminentmagic.development;

import net.mcreator.element.ModElementType;

public final class StaffModElementType {
	public static final ModElementType<Staff> STAFF = new ModElementType<>(
			"eminent_magic_staff", 'T', StaffGUI::new, Staff.class);

	private StaffModElementType() {
	}
}
