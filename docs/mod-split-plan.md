# Eminent Magic Mod Split Plan

**Status:** Initial MCreator workspace migration completed for Core and World of Magic. Cross-workspace API calls and compilation are intentionally deferred to the next stage.

## Goal

Split `Original Mod` into three independently maintainable deliverables while preserving existing gameplay behavior and world compatibility:

1. **Eminent Magic: Core**: the runtime foundation and stable API for player attributes, mana, spell execution, networking, and shared systems.
2. **Eminent Magic: Development Plugin**: MCreator-time authoring tools, including custom spell/staff elements and generated code that targets Core APIs. It should not be required on a player's runtime installation.
3. **Eminent Magic: World of Magic**: the main content mod: spells, staffs, items, effects, visuals, interfaces, and other content built on Core.

The `Original Mod` folder is the source of truth and remains untouched. Core and World of Magic are initialized as MCreator workspaces under their respective segment folders; Eminent Magic: Source remains deferred until World of Magic is finished.

## Current Baseline

- The source workspace is a NeoForge Gradle project with NeoForge `21.8.31` and Java toolchain 21.
- The workspace filename `mcreator20253.desktop` indicates MCreator 2025.3; confirm the exact installed MCreator build before plugin work.
- `eminent_magic.mcreator` declares 100 elements: 5 attributes, 56 procedures, 6 GUIs, 6 keybinds, 14 code elements, 1 gamerule, 1 damage type, 1 particle, 2 overlays, 3 items, 1 tab, 2 potion effects, and 2 commands.
- Existing element names indicate the project already mixes shared systems with concrete spell and staff content. Element type alone is not enough to decide ownership; procedure dependencies and generated-code references must be audited before extraction.
- `Original Mod/ReadMe.md` says: "ORIGINAL SOURCE CODE OF THE MOD DO NOT MODIFY AT ANY POINT". Keep it read-only and migrate by copying/exporting into the new workspaces.
- `Original Mod/mcreator.gradle` is empty; Gradle configuration is driven by `build.gradle` and MCreator-generated workspace files.

## Workspace Migration Status

- Initialized `Eminent Magic: Core/Workspace` with mod ID `eminent_magic`, package `emmy.eminentmagic`, the NeoForge 1.21.8/Java 21 scaffold, and 75 MCreator elements.
- Initialized `Eminent Magic: World of Magic/Workspace` with mod ID `eminent_magic_world`, package `emmy.eminentmagic.world`, and 16 MCreator elements. Its NeoForge metadata declares Core as a required dependency.
- Kept item-bound interfaces and adapters beside their content in World: `StaffGUI1`, `StaffGUI4`, `SpellBookGui`, `GetSpellbookSpellName`, `GetSpellbookSelfSpellName`, and `CastSelectedSpell`. Core retains the shared skill/mana interfaces and generic spell systems.
- Copied MCreator element JSON, project folder structures, code-element Java sources, and the selected runtime resources. Regenerated element Java and full generated runtime classes were not copied; MCreator/Gradle generation and compilation are deferred.
- Deferred seven Source elements: `GetSource`, `Ingall`, `Vesselless`, `VessellessEffectStartedapplied`, `VessellessEffectExpires`, `VessellessOnEffectActiveTick`, and `DeathReplacement`. Excluded the development-only `Development` and `TestingHotkey` elements.
- Removed Source-specific `sourceCost`/`GetSource` logic from retained spell and mana procedure definitions, including the Source gate from `ConsumeMana`. The generic mana eligibility/payment path remains in Core.
- The two workspace manifests, copied element JSON, Blockly XML, module IDs, element/file parity, local item-to-GUI/procedure bindings, and World-to-Core dependency metadata passed structural checks. Core's Gradle output goes to `.build/core-workspace` because `:` in the workspace directory name conflicts with Linux classpath separators. Seeding the matching MCreator Gradle cache allowed MCreator 2025.3 to open Core and regenerate 64 Java files. Core owns `CapeParticle` and `CapeRenderer`; the locked `BarrierShatterEffectProcedure.java` is tracked in its manifest; World no longer lists those moved elements; and `GetManaCost` uses its original entity-only dependency contract. The installed spell-tools plugin still logs a null-template-name exception during code generation, but Core-only Gradle `compileJava` and `build` both succeed. The packaged artifact is `.build/core-workspace/libs/eminent_magic-1.0.jar`; compiled particle registration, mana cost, and barrier shatter classes are present. No generated Java was edited by hand.
- Known cross-workspace procedure references remain for the next API/plugin stage: Core `SpellSelector` still names World spell procedures `FreezeCoffinSpell` and `ZoltraakSpell`; World `ZoltraakSpell` still names Core `CalculateManaCost`, `ConsumeMana`, `RenderCylinder`, and `RotateXYZ`; World `CastSelectedSpell` still names Core `SpellSelector`. Replace these references with the finalized Core/plugin boundary or correctly owned local helpers before compiling World or running combined Core + World tests. Core-only compilation is verified; these remain integration gaps, not evidence that the procedure APIs are implemented.

## Proposed Boundaries

### Eminent Magic: Core

Own runtime behavior that must be shared by the main content mod and future add-ons:

- Registration and lifecycle for the mana attributes and shared player state.
- Mana storage, synchronization, costs, consumption, regeneration rules, and validation.
- Generic spell contracts: definitions, identifiers, lookup/validation, casting context, execution, cooldown/failure handling, and extension points.
- Stable public accessors for player attributes and spell properties; avoid exposing implementation internals.
- Shared network messages and server-authoritative gameplay checks.
- The barrier manager and its runtime behavior: dimension-scoped storage, creation/removal, lookup, expiry, collision tests and responses, durability, and shattering.
- Generic staff/spellcasting interfaces and hooks, but not necessarily the concrete staff item registrations.
- Standard player-facing skill summary, mana displays, and spell-selection interfaces; keep their UI data and implementation independent of World-specific content.
- Shared configuration/data loading runtime, including schema validation and useful diagnostics.

Keep Core independent of World of Magic. It must not import World classes, textures, item IDs, or content-specific spell implementations.

### Eminent Magic: Development Plugin

Own MCreator authoring-time integration:

- Custom MCreator element types for spells and staffs, with editors for their properties.
- Custom procedure blocks for calling the documented Core spell, staff, mana, and barrier APIs from MCreator procedures.
- A plugin-defined Blockly `Spell` value type so procedures can pass spell data to typed getter, cost, and cast blocks without passing raw JSON between blocks.
- Validation, templates, previews, and generated registration/data/code that call documented Core APIs.
- Version checks and clear workspace diagnostics when the target Core API is incompatible.
- Plugin metadata, installation/build instructions, and compatibility tests against the target MCreator release.

Treat the plugin as a development dependency, not a runtime mod dependency. Generated projects may depend on Core; players should not need the plugin installed. Avoid placing gameplay implementations or the runtime config loader in this plugin.

### Eminent Magic: World of Magic

Own the concrete game content and user-facing presentation:

- Spell implementations and spell definitions such as Zoltraak, Freeze Coffin, and other in-scope named spells, including the spells that create barriers. These invoke Core barrier APIs rather than owning barrier storage or collision logic. Ingall is deferred to Eminent Magic: Source.
- Concrete staff and spellbook items, recipes, creative tabs, particles, and content-specific effects.
- Content assets, sounds, textures, models, localization, and content-specific GUI/overlay presentation; the standard skill/mana/spell-selection UI is owned by Core.
- Any content-specific compatibility integrations.

The World of Magic mod should require Core and use Core's public API; Core must not require World of Magic.

## Initial Element Ownership Map

This is a first-pass proposal, not an instruction to move files mechanically. Keep a per-element decision record during extraction and follow each procedure's callers, dependencies, and generated references.

| Existing element group | Proposed owner | Notes / items to audit |
| --- | --- | --- |
| `ManaReserves`, `ManaStrength`, `ManaControl`, `ManaPerception`, `ManaCurrent` attributes | Core | Confirm whether `ManaCurrent` should be a registered attribute or synchronized resource/capability-like state. Preserve attribute registry IDs if worlds already use them. |
| Mana getters/cost/consumption and attribute/control access procedures | Core | Includes `GetMana*`, `GetCurrentMana`, `CalculateManaCost`, `GetManaCost`, the mana-only part of `ConsumeMana`, `GetControlSpell*`, and `OverrideStats`. Keep `GetSource`, source-score logic, source requirements, and the source-check branch of `ConsumeMana` deferred to Eminent Magic: Source. |
| Casting and spell lookup procedures | Core framework plus World implementations | `CastSpell`, `CastSelectedSpell`, `GetSpellData`, `GetSpellExists`, `GetSpellName`, and `GetCurrentSpellID` appear shared, but audit any hard-coded spell/item IDs and move those implementations to World or data definitions. |
| Named spell procedures | World of Magic, except Source content | Initial candidates include `ZoltraakSpell`, `FreezeCoffinSpell`, and other in-scope spells. Barrier-creating spells call Core APIs. Defer `Ingall` to Eminent Magic: Source because it is coupled to Vesselless/resurrection behavior. |
| Barrier manager, lifecycle, collision, damage/durability, and shatter behavior | Core runtime; MCreator procedure blocks in Development Plugin; barrier-creating spells in World | Move storage, expiry, collision math/response, and generic barrier operations to Core. The plugin supplies procedure-call blocks; it does not own runtime state. World spells call Core to create barriers. Replace the current global Overworld JSON array with typed, dimension-scoped state. |
| Shared rendering/math/code elements | Core or World, after reference audit | Generic geometry utilities and the moved mana particle/cape renderer may be Core; content-specific spell visuals and effects belong in World. Core currently owns `CapeParticle` and `CapeRenderer`; confirm remaining code elements and callers before assigning. |
| Standard skill summary, mana displays, and generic spell-selection interfaces | Core | Confirmed baseline Core UI. Item-bound book/staff GUIs and their item adapter procedures are in World of Magic. |
| `StaffGUI1`, `StaffGUI4`, `SpellBookGui`, and item-bound book/staff procedures | World of Magic | These screens are bound to World items or opened by World item procedures. Generic spell data and casting behavior remain Core responsibilities. |
| `Spellbook`, `CrudeStaff`, `CrystalStaff`, `Spells` tab | World of Magic | The generic staff contract belongs in Core; concrete item registrations, recipes, and tab are content. |
| `ManaChannel` and `NotEnoughMana` | Core candidate | These look like shared mechanics/feedback. Check whether their current visuals/text are content-specific before moving. |
| `GetSource`, legacy `sourceCost`, `Ingall`, `Vesselless`, and death-interception procedures/effects | Eminent Magic: Source (future mod; do not migrate now) | Explicitly deferred as a group. This includes source-score/requirement logic and `Ingall`'s rescue behavior; do not place them in Core, the Development Plugin, or World of Magic during this split. See [Source and Resurrection Design](../Eminent%20Magic:%20Source/Source%20and%20Resurrection%20Design.md). |
| `OverlaySpellInfo`, `OverlayManaStats` and their generic UI support | Core | Standard spell/mana presentation belongs with the Core UI decision. Keep spell-specific art and effects in World. |
| `CapeParticle` and `CapeRenderer` | Core | Shared mana/cape rendering elements moved to Core. Any World callers must compile against the supported Core API; other content-specific visuals remain in World. |
| `SpellsConsumeReserve` gamerule | Core | Shared casting rule; preserve gamerule behavior and default. |
| `GetBarriers`, `ClearBarriers` commands | Unresolved | Decide whether these are player/admin features, development diagnostics, or test commands. Keep debug-only commands out of public releases unless intended. |
| Remaining procedures, GUIs, keybinds, and code elements | Unresolved until call graph review | Specific names include `ActivePerception`, `ToggleActivePerception*`, `SpellSelector`, `SpellbookRightclicked`, and staff tick/stop-use hooks. Assign by reusable contract versus named content, not by element category. |

## Spell Data and Configuration Recommendation

Use two layers unless the project has a reason to make spell balance server-config-only:

- **Spell definitions**: versioned data files shipped by World of Magic (and future add-ons), validated and loaded by Core. This keeps definitions namespaced, distributable, and compatible with data-pack/resource workflows.
- **Server/admin overrides**: a documented config file for values that operators should tune globally. Core owns parsing, validation, reload policy, and synchronization of effective values. It should report invalid entries clearly and define safe defaults.

The MCreator plugin should edit/generate the definition data and use the same schema as Core. It should not own the only copy of schema rules or the runtime parser. Avoid arbitrary code in config files; define a typed, versioned schema and decide which properties are safe to override.

## Dependency and Compatibility Rules

- Allowed runtime direction: `World of Magic -> Core`. Never `Core -> World of Magic`.
- Development direction: `Development Plugin -> documented Core API/schema`; plugin installation is only needed by mod authors.
- Any future third-party addon should depend on Core and should be able to ship without World of Magic.
- Keep mod IDs, registry namespaces, package names, and artifact coordinates explicit and stable. Changing the original namespace can break existing saves, configs, datapacks, and integrations.
- Use one compatible Minecraft/NeoForge/Java baseline across the runtime mods initially. Version the Core API and declare the minimum compatible Core version for World of Magic.
- Build each runtime mod independently, then run a combined game test with Core + World of Magic. Test Core by itself and test World-of-Magic absence/presence behavior.

## Core APIs and Development Plugin Procedure Blocks

The plugin should expose MCreator-facing blocks that generate calls into Core. Runtime state, validation, server/client handling, and gameplay logic stay in Core. Prefer typed inputs/outputs and stable namespaced spell IDs; do not make third-party procedures depend on Core's internal JSON objects, collections, or generated implementation classes.

| API family | First-pass procedure blocks | Contract |
| --- | --- | --- |
| Barriers | `Create barrier` (world, origin, normal, radius, duration, owner, initial integrity; returns stable ID), `Remove barrier by ID`, `Remove barrier at index`, `Get barrier count`, `Get barrier ID at index`, and typed getters for barrier origin, normal, size, remaining duration, integrity, and owner | **Confirmed:** typed count/index/ID/property reads, removal by both stable ID and current index, and explicit initial integrity supplied by the creating spell; creation returns a stable ID. Scope each operation to a world/dimension. IDs are preferred for retained references because indices can shift after removal. Core owns persistence, expiry, collision response, durability, and shattering. |
| Barrier collision | `Does vector collide with a barrier?` (boolean) and `Get collided barrier index` (number; `-1` when there is no hit) | Both blocks call the same Core query so their results cannot disagree. **Confirmed:** world, start position, and motion/displacement vector; test the finite circular disk over the segment from start to start + motion. Barriers block crossings from either side; the normal defines plane orientation and response. If multiple barriers intersect, return the nearest hit along the segment. |
| Spells | `Get spell by ID` (returns the custom `Spell` value), `Is spell valid?`, named property getters that accept a `Spell`, `Calculate spell mana cost`, and `Cast spell` | **Confirmed interface requirements:** pass a spell data type between blocks, use namespaced string IDs as canonical identifiers, and expose named property getters. Begin with the in-scope cost fields `baseCost`, `costStrengthScaling`, and `costControlScaling`; exclude `sourceCost` and all Source-specific properties until Eminent Magic: Source is designed. Add display-name and other in-scope getters according to the new schema. Core resolves definitions through its validated registry/loader; the plugin must verify MCreator 2025.3 supports this custom Blockly type. Representation (Core reference versus snapshot) is an implementation choice; keep it opaque to procedure authors. Casting returns success/failure and executes authoritatively on the server. |
| Staffs | `Is entity holding a staff?`, `Get selected spell from held staff` (returns `Spell`), `Get spell in held staff slot` (returns `Spell`), `Set spell in held staff slot`, `Select held staff spell slot`, and `Cast selected spell` | **Confirmed:** procedures can read, write, select, and cast spells on the held staff. Blocks resolve an entity's staff (not arbitrary inventory `ItemStack`s); if both hands hold staffs, use offhand first, then main hand. Core validates slot indices and owns the staff contract. Concrete staff item creation remains a Development Plugin element that generates content targeting Core. |
| Mana and attributes | Numeric getters and controlled set/add blocks for current mana, reserves, strength, control, and perception; `Can pay mana?`; `Try consume mana` (boolean); and `Calculate mana cost` | **Confirmed:** expose both read and write access plus safe spending. Make entity and world context explicit. Core validates and synchronizes mutations; generated procedures must not edit backing attributes directly. The consume operation centralizes mana/reserve rules, creative handling, and failure behavior. Source eligibility and requirements are deferred to Eminent Magic: Source. |

### Existing API Migration Notes

- The current barrier system stores data in a global `OverworldBarriers` JSON array. Its query/debug procedure prints JSON to chat, and the storage is not a suitable public cross-dimension API.
- Existing barrier collision and duration code includes generic mechanics that should move to Core. World-owned spell procedures should call the new Core create operation; Core should own generic collision callbacks and shattering behavior.
- The current barrier JSON `mana` field is used as collision durability/integrity. Rename it to an explicit integrity field in Core; migrate the old value without conflating it with the caster's mana resource.
- Current mana getters format values as strings; `GetManaCost` reads `Minecraft.getInstance().player`, and spell lookup reads `Spells.json` directly from config. Replace these with typed Core methods backed by the validated spell data/config loader and server-authoritative player state.
- Replace current numeric spell IDs with namespaced string IDs; include an old-number-to-new-ID mapping in the best-effort migration plan for existing configs and saved staff/spell selections.

### Eminent Magic: Source Deferral

The workspace already has a separate `Eminent Magic: Source` directory and a [Source and Resurrection Design](../Eminent%20Magic:%20Source/Source%20and%20Resurrection%20Design.md) note for a future standalone mod. That mod is planned only after World of Magic is finished. **Do not migrate or implement Source-related content as part of the current Core, Development Plugin, or World of Magic work.**

Defer `GetSource` and source-score calculations; source-specific attributes or derived-attribute behavior; the `sourceCost` spell property, getters, config fields, and cast-validation/payment branch; `Ingall`; `Vesselless`; `DeathReplacement`; and the Vesselless lifecycle procedures/effects. Keep their original procedures and assets in `Original Mod` as references and mark their migration-ledger entries deferred. Do not add a Source dependency to Core or make current World spells require the future Source mod.

This deferral applies to Source-specific attributes and logic, not the generic mana attributes already assigned to Core (`ManaReserves`, `ManaStrength`, `ManaControl`, `ManaPerception`, and `ManaCurrent`). Keep those mana systems in Core. If an in-scope spell such as Freeze Coffin has an incidental `GetSource` or `sourceCost` reference, remove that dependency for this migration rather than importing the deferred Source system.

After World of Magic is complete, design Eminent Magic: Source as a separate mod that depends on Core. Any later Source-aware spell integration or Development Plugin fields should be added only after that mod's API and data model are defined.

### Interface Decisions

1. **Confirmed:** Treat the input as a finite movement segment: start position plus displacement.
2. **Confirmed:** Barriers block movement from both sides; the normal defines plane orientation and collision response.
3. **Confirmed:** Retain finite circular disks and return the nearest hit when a segment intersects multiple barriers.
4. **Confirmed:** Read barriers through count/index/ID and typed property getter blocks, not a raw JSON result.
5. **Confirmed:** Provide both remove-by-stable-ID and remove-by-current-index blocks; creation returns a stable ID.
6. **Confirmed interface requirement:** Represent spells as a dedicated Blockly data type passed to property and spell-operation blocks; verify exact custom-type support in MCreator 2025.3.
7. **Implementation choice:** User has no preference between a Core definition reference and snapshot; choose whichever is reliable in MCreator 2025.3 while keeping the custom `Spell` value opaque.
8. **Confirmed:** Use namespaced string IDs as canonical spell identifiers.
9. **Confirmed:** Staff procedure blocks operate on an entity's currently held staff, not arbitrary staff item stacks; prefer offhand, then main hand.
10. **Confirmed:** Expose mana and attribute read/write blocks, plus affordability and safe-consumption operations; Core owns validation and synchronization.
11. **Confirmed:** Use named getter blocks for standard properties on the `Spell` data type; define the exact block set from the new schema.
12. **Confirmed:** Held-staff procedure blocks support reading/writing spell slots, selecting a slot, and casting the selected spell.
13. **Confirmed:** A barrier-creating spell supplies initial barrier integrity explicitly to `Create barrier`.

## Migration Sequence

1. **Apply the confirmed decisions below.** Resolve the remaining implementation follow-ups before extracting elements or changing registry identities.
2. **Inventory dependencies.** For all 100 elements, record callers, dependencies, registry IDs, hard-coded references, assets, and whether the behavior is shared or content-specific. Inspect generated Java/resources as well as MCreator metadata.
3. **Establish Core first.** Create the Core workspace, move/copy only shared runtime systems, define API boundaries, and preserve registry identities where possible. Build and test Core alone.
4. **Define spell data/API contract.** Specify a versioned schema, validation rules, identifiers/namespaces, config override policy, and server-authoritative behavior before implementing plugin generation.
5. **Build the Development Plugin.** Add custom spell/staff elements that emit code/data for the selected MCreator version. Test generation against a clean Core + World of Magic workspace.
6. **Extract World of Magic.** Move concrete content, assets, and presentations; replace direct references to old generated classes with Core APIs. Declare the required Core version.
7. **Verify compatibility.** Test clean builds, dedicated server and client, multiplayer synchronization, existing-world loading, spell casting, missing/invalid config, reloads, and packaging/install instructions.
8. **Retain a migration ledger.** Mark each original element as moved, replaced, intentionally removed, or deferred. Do not delete or edit the source workspace as part of this split.

### Future Minecraft 26.3 Upgrade

Treat the Minecraft 26.3 move as a separate coordinated upgrade after MCreator supports that Minecraft version. The exact toolchain and code changes depend on the generator and loader versions MCreator releases, so verify them at that time rather than baking assumptions into the split.

- Confirm MCreator, its NeoForge generator, and the plugin API support Minecraft 26.3; identify the required Java, Gradle, and NeoForge versions.
- Upgrade Core first and resolve platform/API changes, then update the Development Plugin's element definitions and generated templates, and finally upgrade World of Magic against the new Core API.
- Review registry/data/resource format changes, configuration and spell-definition compatibility, networking, and any breaking API changes.
- Rebuild each workspace from a clean checkout; test Core alone and the combined client, dedicated server, multiplayer, existing-world loading, and plugin-generated elements.
- Record any save/data migration, version constraints, and updated install/build instructions before releasing the new baseline.

## Decisions and Questions

Only decisions explicitly marked as confirmed below have been settled. Other rows remain proposed defaults and open questions.

| ID | Decision needed | Proposed default | Question |
| --- | --- | --- | --- |
| D1 | Minecraft/loader baseline | **Confirmed:** Keep NeoForge `21.8.31` and Java 21 for the split; plan a separate Minecraft 26.3 upgrade once MCreator supports it. | At upgrade time, what NeoForge, Java, Gradle, and plugin API versions does MCreator's 26.3 generator require? |
| D2 | MCreator target | **Confirmed:** Target MCreator 2025.3 for the initial Development Plugin; verify the exact installed build during setup. | Which exact 2025.3 build is installed in the development environment? |
| D3 | Runtime IDs/namespaces | **Confirmed:** Core uses `eminent_magic`; World of Magic uses `eminent_magic_world`. Review existing registry IDs and migration needs because content is moving to a new namespace. | Build a best-effort mapping for old registry/data IDs and identify content that cannot be migrated. |
| D4 | Core dependency | **Confirmed:** World of Magic requires Core at runtime; the Development Plugin is authoring-time only and is not a runtime dependency. | Declare and test the minimum compatible Core version for each World of Magic release. |
| D5 | Spell property storage | **Confirmed:** Use both: World/add-ons ship versioned spell-definition data files, and server config overrides selected operator-tunable properties. Define the overrideable property list with the schema. | Which individual properties should be overrideable will be decided during schema design. Reload behavior is covered by D6. |
| D6 | Config authority/sync | **Confirmed:** The server remains authoritative and synchronizes all resolved spell properties to clients. Spell-definition and config changes require a restart. | Specify the network schema/versioning and ensure clients cannot authoritatively apply gameplay effects. |
| D7 | Core UI scope | **Confirmed:** Core owns the standard skill summary, mana displays, and spell-selection UI. World owns content-specific interfaces and presentation. | Audit current GUIs, overlays, and keybinds to classify standard screens versus staff/content-specific interfaces. |
| D8 | Spell/staff authoring | **Confirmed:** Custom spell and staff elements generate both data files and Java code. Define the data/code boundary and ensure generated Java targets stable Core APIs. | Specify which properties are represented in data versus generated code during plugin design. |
| D9 | Current debug/dev elements | **Confirmed:** Exclude `TestingHotkey` and `Development` from public builds; include `GetBarriers` and `ClearBarriers` as admin commands. Verify permission checks before release. | Confirm the intended permission level and behavior for the two barrier commands during implementation. |
| D10 | Distribution/build flow | **Confirmed:** Distribute Core and World of Magic as separate mod JARs, with World declaring its Core dependency. The Development Plugin is distributed separately to MCreator users. | Choose release hosting/platform and define versioned artifact publishing for development and public releases. |
| D11 | Save/API compatibility | **Confirmed:** Use a best-effort migration for existing worlds and integrations; test supported content and document unavoidable breaks. | Define the supported registry/data migration list and test fixtures before release. |

### Decision Log

| Date | Decision | Status |
| --- | --- | --- |
| 2026-10-18 | Keep `Original Mod` as an untouched reference/source workspace during the split. | Recorded from the source workspace README and user request. |
| 2026-10-18 | Keep NeoForge `21.8.31` and Java 21 for the initial split; treat Minecraft 26.3 as a later coordinated upgrade after MCreator supports it. | Confirmed by user. |
| 2026-10-18 | Target MCreator 2025.3 for the initial Development Plugin. | Confirmed by user; exact installed build to verify. |
| 2026-10-18 | Use `eminent_magic` for Core and `eminent_magic_world` for World of Magic. | Confirmed by user; build the registry/save migration map under the confirmed best-effort D11 policy. |
| 2026-10-18 | Make Core a required runtime dependency of World of Magic; keep the Development Plugin out of runtime dependencies. | Confirmed by user. |
| 2026-10-18 | Store spell definitions in versioned data files and allow selected properties to be overridden by server config. | Confirmed by user; exact property allowlist remains for schema design. |
| 2026-10-18 | Keep the server authoritative, synchronize all resolved spell properties to clients, and apply definition/config changes only after restart. | Confirmed by user. |
| 2026-10-18 | Include standard skill summary, mana displays, and spell-selection interfaces in Core. | Confirmed by user; audit individual current UI elements for content-specific ownership. |
| 2026-10-18 | Have custom spell/staff MCreator elements generate both data files and Java code. | Confirmed by user; exact division of properties and logic remains for plugin design. |
| 2026-10-18 | Exclude `TestingHotkey` and `Development` from public builds; ship `GetBarriers` and `ClearBarriers` as admin commands. | Confirmed by user; permission gate to verify. |
| 2026-10-18 | Distribute Core and World of Magic as separate mod JARs with an explicit dependency; distribute the MCreator plugin separately. | Confirmed by user; hosting/platform remains open. |
| 2026-10-18 | Use a best-effort migration for existing worlds and integrations, testing supported content and documenting unavoidable breaks. | Confirmed by user. |
| 2026-10-18 | Put barrier runtime/storage/collision behavior in Core, expose create/remove/get/collision operations through Development Plugin procedure blocks, and keep barrier-creating spells in World of Magic. | Confirmed by user. Collision contract is a two-sided finite circular disk tested along a segment, returning the nearest hit. |
| 2026-10-18 | Define barrier collision input as a finite segment from start position to start plus displacement. | Confirmed by user. |
| 2026-10-18 | Make barrier collision two-sided; use the normal for plane orientation and response. | Confirmed by user. |
| 2026-10-18 | Use finite circular disks and return the nearest barrier hit along a segment. | Confirmed by user. |
| 2026-10-18 | Expose barrier reads through typed count/index/ID/property getter blocks rather than raw JSON. | Confirmed by user. |
| 2026-10-18 | Provide remove-barrier blocks for both stable ID and current index; creation returns a stable ID. | Confirmed by user. |
| 2026-10-18 | Represent spells as a dedicated data type in MCreator procedures, passed to blocks that read spell properties. | Confirmed by user; verify custom Blockly type support and implementation details against MCreator 2025.3. |
| 2026-10-18 | Leave the internal representation of the custom `Spell` value to implementation; keep it opaque to procedures. | User has no preference; choose the reliable MCreator 2025.3-compatible approach. |
| 2026-10-18 | Use namespaced string IDs as canonical spell identifiers. | Confirmed by user; map old numeric IDs during best-effort migration. |
| 2026-10-18 | Defer all Source-specific systems, source requirements/attributes, `sourceCost`, and Ingall/Vesselless/death-interception content from the current split to the future Eminent Magic: Source mod, planned after World of Magic. | Confirmed by user. Generic mana attributes remain in Core. |
| 2026-10-18 | Make staff procedure blocks target the staff currently held by an entity rather than arbitrary inventory stacks; prefer offhand if both hands hold staffs. | Confirmed by user. |
| 2026-10-18 | Expose held-staff procedure operations to read/write spell slots, select a slot, and cast the selected spell. | Confirmed by user; Core validates slot indices and staff state. |
| 2026-10-18 | Expose mana/attribute getters and controlled setters/adders, affordability checks, and safe mana consumption through plugin procedure blocks. | Confirmed by user; Core owns validation and synchronization. |
| 2026-10-18 | Use named property getter blocks that accept the custom `Spell` data type. | Confirmed by user; exact getter set follows the new spell schema. |
| 2026-10-18 | Require barrier-creating spells to provide initial integrity explicitly to the Core-backed Create barrier block. | Confirmed by user; migrate the old JSON `mana` value as integrity, not caster mana. |
| 2026-10-18 | Plan three deliverables with Core as shared runtime, Development Plugin as authoring-time tooling, and World of Magic as concrete content. | Working architecture for this plan; detailed ownership follows the confirmed decisions and implementation audit. |
| 2026-10-18 | Use a one-way runtime dependency from World of Magic to Core. | Confirmed by user under D4. |
| 2026-10-18 | Audit by behavior and call graph, not by MCreator element type alone. | Recommended migration rule based on the mixed current element inventory. |

## Implementation Follow-Ups

All 11 top-level split decisions and the listed interface decisions have been discussed. Before implementation, resolve these detailed follow-ups:

- Verify the exact installed MCreator 2025.3 build and its plugin API; revisit the Java/Gradle/NeoForge baseline when MCreator supports Minecraft 26.3.
- Create the best-effort migration map for registry IDs, saved data, configs, and integrations affected by the `eminent_magic` to `eminent_magic_world` content namespace change.
- Include old numeric spell IDs in the migration map and resolve them to namespaced IDs.
- Define the spell schema, including which fields server config may override, the full client-sync payload/versioning, and generated-data versus generated-Java responsibilities.
- Define bounds, clamping, and authorization behavior for procedure blocks that mutate mana and player attributes.
- Verify MCreator 2025.3 supports the custom `Spell` Blockly value type, then define its named getter set from the spell schema.
- Audit individual GUIs, overlays, keybinds, staff elements, and procedures to apply the agreed Core-versus-World ownership.
- Verify operator permission checks for `GetBarriers` and `ClearBarriers`.
- Choose release hosting and artifact publishing, and set a minimum compatible Core version for each World of Magic release.
