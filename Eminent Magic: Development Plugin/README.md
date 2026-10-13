# Eminent Magic Spell Tools

An MCreator 2025.3 Java plugin for authoring spell metadata as a custom `Spell` mod element. It currently supports the NeoForge 1.21.8 generator.

Spell and Staff runtime templates are generated only for non-Core workspaces that contain the matching elements. Workspaces using either element type receive a required NeoForge dependency on `eminent_magic_core` (Eminent Magic: Core). The Core workspace itself is excluded from plugin generation; Core-owned grimoire content is not duplicated into dependent mods.

Each Spell element stores `spellName`, `manaCost`, `controlScaling`, `strengthScaling`, and an `element` choice (`fire`, `water`, `earth`, or `wind`). Generating the workspace writes one JSON file per spell under `data/<modid>/spells/`. Core owns spell configuration, the workspace-wide `/spelllist` command, the `Eminent Spells` creative tab, and the Grimoire item. The plugin-generated SpellContent contributes one stack per Spell element to Core's tab by calling Core's stack factory; every stack uses `eminent_magic_core:grimoire` and carries that element's data in custom item data, with its spell name shown in purple lore. Spell casting is not yet connected to this runtime data.

Each Staff element stores a display name, an item texture, a spell slot count from 0 to 12, and optional creative tabs. It generates a separate staff item in the workspace namespace with an item model using the selected texture. Core owns staff behavior and the circular, item-bound inventory opened by sneak-right-clicking; slots persist with the staff and accept only `eminent_magic_core:grimoire` items. With no creative tab selected, the Staff appears in `Eminent Spells`.

The config maps each namespaced spell ID (`<modid>:<registryname>`) to its editable values:

```json
{
	"spells": {
		"plugin_testing:test": {
			"spellName": "Test",
			"manaCost": 1.0,
			"controlScaling": 2.0,
			"strengthScaling": 3.0,
			"element": "water"
		}
	}
}
```

Numeric values must be finite and nonnegative, `spellName` must be non-empty, and `element` must be one of the four supported choices. Invalid values use that field's packaged default. If the file as a whole is malformed, all packaged defaults are used and the malformed file is left untouched.

After installing or updating the plugin, restart MCreator and regenerate code in the target workspace to emit the command source.

## Build

Use Java 21 and point Gradle at the MCreator 2025.3 installation:

```sh
./gradlew -p mcreator-spell-tools pluginZip -Pmcreator_path="$HOME/Coding/Software/Mcreator/MCreator20253"
```

The unversioned plugin archive `eminent-magic-spell-tools.zip` is created in `build/libs/` by `pluginZip`. The standard `build` task also installs it into `~/.mcreator/plugins/`. Override the destination with `-Pplugin_install_dir=/path/to/plugins`. Restart MCreator after each build to load the updated plugin. Java plugins must be enabled in MCreator preferences.
