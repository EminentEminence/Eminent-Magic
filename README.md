# Eminent Magic

Eminent Magic is a Minecraft magic system for NeoForge 1.21.8. The project is
developed with MCreator and extended with Java services, custom MCreator
elements, data-driven spell definitions, and automated Core GameTests.

The repository is split into independently understandable parts:

- **Core** provides the reusable runtime: mana attributes and spending, spell
  catalogs and casting, staffs and spell slots, barriers, networking, client
  screens, and shared procedures.
- **World of Magic** contains world/content-specific spells and items that
  consume the Core API.
- **Development Plugin** adds custom Spell and Staff element types, GUIs,
  Blockly procedures, variables, and NeoForge templates to MCreator.
- **Source** contains design notes for a future standalone Source module. Its
  mechanics are intentionally not part of the current Core/World of Magic
  contract.

## Current version

The canonical project version is stored in [`VERSION`](./VERSION). The current
version is `0.1.2`.

## Requirements

The supported development stack is:

- Minecraft **1.21.8**
- NeoForge **21.8.31**
- Java **21**
- MCreator **2025.3**
- Gradle **8.14.2** or a compatible Gradle installation

The repository includes Gradle wrappers in the individual workspaces. The
coordinated root build also needs a working MCreator installation because it
compiles and installs the Development Plugin.

## Repository layout

```text
.
├── VERSION
├── build.sh
├── Eminent Magic: Core/
│   └── Workspace/
│       ├── eminent_magic.mcreator
│       ├── elements/
│       └── src/main/
├── Eminent Magic: Development Plugin/
│   ├── src/main/java/
│   ├── src/main/resources/
│   └── TestingWorkspace/
├── Eminent Magic: World of Magic/
│   └── workspace/
│       ├── eminent_magic_world.mcreator
│       ├── elements/
│       └── src/main/
├── Eminent Magic: Source/
│   └── Source and Resurrection Design.md
```

Build products are written to `.build/`. Generated workspaces, run data, logs,
and MCreator backups are not source-of-truth project files.

## Building everything

From the repository root:

```bash
./build.sh
```

The script:

1. Reads and validates the version from `VERSION`.
2. Builds the Core mod.
3. Copies the Core JAR into the World of Magic development runtime.
4. Builds the Development Plugin.
5. Installs the plugin into the configured MCreator plugin directory.

By default, the script expects the following local paths:

```text
MCreator:       ~/Coding/Software/Mcreator/MCreator20253
Java:           $MCREATOR_HOME/jdk
MCreator plugins: ~/.mcreator/plugins
```

Override them when necessary:

```bash
MCREATOR_HOME=/path/to/MCreator20253 \
MCREATOR_PLUGIN_DIR=/path/to/.mcreator/plugins \
JAVA_HOME=/path/to/jdk-21 \
./build.sh
```

`GRADLE` may also be set to an explicit Gradle executable. The build script
fails with a diagnostic if Java 21, Gradle, MCreator, either MCreator
workspace, or the expected build artifacts cannot be found.

## Building individual workspaces

### Core

```bash
cd "Eminent Magic: Core/Workspace"
./gradlew build
```

The Core workspace produces the reusable `eminent_magic` NeoForge mod. Its
version defaults to the root `VERSION` file, or can be supplied explicitly:

```bash
./gradlew build -PprojectVersion=0.1.2
```

### World of Magic

The World of Magic workspace expects a Core JAR in
`../../.build/core-workspace/libs`. Build Core first, then run:

```bash
cd "Eminent Magic: World of Magic/workspace"
./gradlew build
```

For development runs, the coordinated root build places the Core JAR in
`Eminent Magic: World of Magic/workspace/run/mods`.

### Development Plugin

The plugin requires the MCreator installation directory:

```bash
cd "Eminent Magic: Development Plugin"
./gradlew build -PmcreatorHome=/path/to/MCreator20253
```

After building, copy the resulting
`.build/development-plugin/libs/eminent-magic-development-plugin-<version>.jar`
to the MCreator plugins directory and restart MCreator.

## Running and testing

The Gradle workspaces provide the usual NeoForge development run tasks. From
the relevant workspace, use:

```bash
./gradlew runClient
./gradlew runServer
```

Core also contains manual Minecraft GameTests. In a development world with
commands enabled, run:

```text
/test runall
```

Or run an individual test:

```text
/test run eminent_magic:mana_spending
```

The current suite covers spell catalog loading and lookup, mana-cost
calculation, spell/staff item round-trips, mana attribute registration,
successful mana spending, and invalid or non-player mana requests. See
[`docs/core-gametests.md`](./docs/core-gametests.md) for the current scope.

## Architecture

### Core runtime

Core owns the stable runtime contracts used by other modules:

- `SpellCatalog` and `SpellDefinition` load and expose spell data.
- `SpellCastingService` calculates costs, spends mana, and dispatches cast
  triggers.
- `StaffService` finds held staffs, manages selected slots, reads and writes
  spells, and starts casts.
- `ManaSpendingService` validates and applies mana payments.
- Barrier services manage persistent, server-side barrier state.
- Network messages, menus, overlays, and client handlers provide the player
  experience around those systems.

Spell and staff behavior should use these services rather than duplicating
item-data or casting logic in generated procedures.

### World of Magic content

World of Magic is a content layer over Core. Its data currently includes
spells such as Freeze Coffin and Zoltraak and a world-specific staff item.
Content in this module should depend on public Core behavior and should not
move Source-specific lifecycle rules into Core.

### MCreator Development Plugin

The plugin keeps custom authoring available inside MCreator. It supplies:

- Spell and Staff mod element types.
- Custom Spell and Staff configuration GUIs.
- Blockly procedures for spell, staff, mana, and barrier operations.
- NeoForge 1.21.8 Java/resource templates.
- Custom variables, mappings, themes, and localization.

When runtime behavior changes, update the corresponding MCreator element
definitions, Blockly JSON, templates, and generated-code integration so the
procedures remain editable in MCreator.

## Versioning

Update `VERSION` first, then synchronize MCreator workspace metadata when
needed:

```bash
python3 scripts/sync_mcreator_version.py <version> \
  "Eminent Magic: Core/Workspace/eminent_magic.mcreator" \
  "Eminent Magic: World of Magic/workspace/eminent_magic_world.mcreator"
```

The build script passes the canonical version into Gradle and the plugin
resource processing step. Keep generated mod metadata and plugin metadata
aligned with the same version.

## Working with MCreator files

The `.mcreator` workspaces, `elements/` files, procedure JSON, Blockly
definitions, and generated-code templates are part of the editable project.
Prefer changing the MCreator representation and its templates over patching
generated Java output. Generated output can be regenerated and should not be
treated as the durable implementation.
