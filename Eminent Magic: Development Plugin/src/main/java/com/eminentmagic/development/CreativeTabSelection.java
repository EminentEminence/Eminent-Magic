package com.eminentmagic.development;

final class CreativeTabSelection {
	static final String STAFF_DEFAULT = "minecraft:tools_and_utilities";
	static final String SPELL_DEFAULT = "minecraft:combat";

	record Option(String id, String label) {
		@Override
		public String toString() {
			return label;
		}
	}

	static final Option[] OPTIONS = {
			new Option("", "Do not add to a creative tab"),
			new Option("minecraft:building_blocks", "Building Blocks"),
			new Option("minecraft:colored_blocks", "Colored Blocks"),
			new Option("minecraft:natural_blocks", "Natural Blocks"),
			new Option("minecraft:functional_blocks", "Functional Blocks"),
			new Option("minecraft:redstone_blocks", "Redstone Blocks"),
			new Option("minecraft:tools_and_utilities", "Tools & Utilities"),
			new Option("minecraft:combat", "Combat"),
			new Option("minecraft:food_and_drinks", "Food & Drinks"),
			new Option("minecraft:ingredients", "Ingredients"),
			new Option("minecraft:spawn_eggs", "Spawn Eggs"),
			new Option("minecraft:op_blocks", "Operator Utilities")
	};

	private CreativeTabSelection() {
	}

	static Option optionFor(String id, String defaultId) {
		String selectedId = id == null ? defaultId : id;
		for (Option option : OPTIONS) {
			if (option.id().equals(selectedId)) {
				return option;
			}
		}
		return OPTIONS[0];
	}

	static String idFor(Option option) {
		return option == null ? "" : option.id();
	}
}
