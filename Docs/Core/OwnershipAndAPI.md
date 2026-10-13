# Ownership and API Contract

This document defines module boundaries for the migration and separates the interfaces that exist today from the target contract needed to port the original mod. The spell/staff field schema is intentionally delegated to [Spell+StaffDataStruct.md](../../Spell%2BStaffDataStruct.md). Source scoring and Ingall/resurrection are deferred to the future [Eminent Magic: Source design](../../Eminent%20Magic:%20Source/Source%20and%20Resurrection%20Design.md). Migration status and end-user acceptance checks are tracked in [MigrationProgress.md](../../MigrationProgress.md); individual legacy Procedure dispositions are in [ProcedureMigrationMap.md](../Migration/ProcedureMigrationMap.md).

## Module Ownership

| Module | Owns | Must not own |
|---|---|---|
| **Eminent Magic: Core** | Shared runtime independent of authored content: spell-definition loading and lookup; mana attributes and accounting; generic cast lifecycle and dispatch boundary; shared staff behavior, Grimoire stack and inventory support; common commands; reusable barrier state/collision/math APIs. | Concrete World of Magic spell effects, authored staff/spell balance/content, World-specific progression screens, keybinds, overlays, and cosmetics. Core must not import or directly branch on World of Magic element classes or spell IDs. |
| **Eminent Magic: Development Plugin** | MCreator authoring types, editors/validation, custom Blockly blocks, and generator templates that turn authored elements into resources and integrations. It owns generation-time adaptation, not runtime game state. | A second copy of mana/casting/staff runtime; hardcoded Eminent Magic spell effects; generated JSON as a replacement for authored Procedure behavior. |
| **Eminent Magic: World of Magic** | Concrete spells/effects, authored staff definitions, spell-specific barrier effects, player progression and stat workflows, keybinds, screens/overlays, and visual assets/rendering. | A private duplicate of Core's mana, spell registry, Grimoire, staff inventory, or generic barrier lifecycle. |
| **Eminent Magic: Source (future)** | Source-specific model and validation, Source eligibility, Vesselless/death lifecycle, and initially the Ingall rescue feature. Its planned boundary is documented separately. | Not part of the current Core/Plugin/World migration. Core must not depend on this mod; see the [Source design](../../Eminent%20Magic:%20Source/Source%20and%20Resurrection%20Design.md). |

World of Magic uses the Plugin's authoring elements and depends at runtime on Core. The Plugin adds a required `eminent_magic_core` dependency when a non-Core workspace contains Spell or Staff elements. Core itself is excluded from that generated dependency injection.

## Current Implemented Interfaces

These describe the checked-in foundation, not completion of the legacy migration.

| Interface | Current behavior | Current limitation |
|---|---|---|
| Spell resources | Plugin generation writes one resource per Spell element under `data/<modid>/spells/`. | Resource values currently follow the schema in [Spell+StaffDataStruct.md](../../Spell%2BStaffDataStruct.md) / Plugin README; casting does not yet consume them. |
| Spell registry | `findSpell(ResourceLocation)` and `findSpell(String)` return an optional immutable typed definition; `getAll()` and `getAll(String)` return sorted immutable values. `reload(MinecraftServer)` loads packaged spell JSON and overlays `config/<namespace>/Spells.json`. | Reload currently occurs at server start; broader data-pack reload handling and integration tests remain. Cast APIs are not present. |
| Cast runtime | `EminentMagicCoreSpellCasting` provides namespaced hook registration, cast-state snapshots, atomic upfront payment, server-ticked exact channel installments, control-paced timing, stop/failure cancellation, action-bar failure feedback, and active-duration dispatch. | Native cast/channel Procedure entry points, World-generated hook registration, cost/control resolution, and reserve-gamerule call-site wiring are not connected. Staff affinity metadata is still absent. |
| Spell validation/config | Runtime normalization accepts canonical `baseCost` and legacy `manaCost` alias, validates `pure`, `fire`, `water`, `earth`, `wind`, and `duration`, and applies defaults field-by-field. | The one-time `config/eminentmagic/Spells.json` backup/import path is implemented but awaits end-to-end verification with packaged World definitions. |
| Mana runtime | The five stats and `spellsConsumeReserve` are native MCreator Attribute/Gamerule elements in Core's `Core/Mana` folder, with numeric getter Procedures. `CalculateManaCost` preserves the legacy formula and final affinity divisor; `GetManaCost` resolves the spell and per-player settings before calling it. Per-spell Control/Strength get/set Procedures use the synchronized, clamped player attachment. `ConsumeMana` uses native attribute reads and writes for current-first payment and optional reserves. | `OverrideStats`, casting-owned 20% upfront/channel-time payment, reserve gamerule wiring, failure feedback, and in-game tests remain. `GetManaCost` currently accepts affinity match/scaling as explicit dependencies because Staff authoring does not yet provide them. |
| Grimoire | The native `Grimoire` Item and `EminentSpells` Tab elements register `eminent_magic_core:grimoire` and `eminent_magic_core:eminent_spells`. `EminentMagicCoreGrimoireContent` creates identity-bearing stacks and resolves their namespaced spell ID through the registry. Staff inventory slots accept only valid registered spell stacks. | Core exposes off-hand-first selected-spell lookup and per-staff slot cycling, but no input/UI callback uses them yet. Cast lifecycle is not connected. The legacy Plugin template still needs migration to the typed stack factory contract. |
| Staff runtime | `EminentMagicCoreStaffItem` uses the configured slot count, exposes item-handler capability backed by the staff stack, accepts only registered Core Grimoire spells, persists a selected slot on each stack, resolves off-hand-first selection, and opens its menu on sneak-use. | Plugin UI currently allows 0..12, while the target contract is 1..12. Affinity metadata, input hookup, casting, and legacy callbacks remain to be migrated. |
| Plugin elements | Spell and Staff elements are available for NeoForge 1.21.8. Staff generation creates per-workspace items and models; spell generation adds Grimoire stacks to the Core tab. | Plugin-generated spell content currently contributes display stacks only. Spell casting and World effect dispatch are not wired. |
| World integration | The World workspace has generated Spell/Staff examples and calls Core's Grimoire stack factory/staff base class. | Existing World content is test content, not migrated gameplay. |
| Spell list | Core registers `/spelllist`, permission level 4, listing loaded namespaced definitions. | Administrative inspection only; it is not a cast or player lookup API. |

Relevant implementation anchors: `EminentMagicCoreSpellRegistry`, `EminentMagicCoreGrimoireContent`, `EminentMagicCoreStaffItem`, `EminentMagicCoreStaffMenu`, and the Plugin `spellcontent.java.ftl` / `staffcontent.java.ftl` templates.

## Target Runtime Contract

The following contracts are required for migration. They are design requirements, not claims that these methods already exist. Final spell/staff value objects and fields must follow [Spell+StaffDataStruct.md](../../Spell%2BStaffDataStruct.md).

### Spell Definitions and Data Access

- Core is the runtime authority for spell definitions from all dependent mod namespaces, validation, effective defaults, and lookup by namespaced spell ID.
- Provide a typed lookup boundary (for example, `findSpell(ResourceLocation id)` returning an immutable definition or an explicit absent result). Do not make authored Procedures parse JSON, config paths, or arbitrary NBT themselves.
- Plugin Blockly blocks should compile to direct Core API calls for definition lookup and supported fields. Blocks are data accessors; existing Procedures continue to own sequencing, conditions, and effects.
- Grimoire stacks must carry a stable namespaced spell ID. Display text/lore is presentation and must not be the identity source. A Core reader should validate the ID and resolve current definition data rather than trusting stale copied fields.
- Core must define behavior for unknown IDs, missing definitions, malformed values, and removed content. Callers need an explicit absent/failure result and must not cast using an unrelated default spell.

### Casting and Effect Dispatch

- Core owns shared cast preconditions and lifecycle: resolve selected spell, validate actor/target/context, calculate and consume mana once, enforce server authority, then request effect execution.
- World of Magic owns the authored implementation of each spell effect and its visuals.
- **Required dependency rule:** Core cannot directly call or import World of Magic procedures/classes, and cannot contain a switch over World spell IDs. Use an explicit registration/dispatch boundary keyed by namespaced spell ID. World-generated integration registers its effects through Plugin output or an equivalent stable extension point.
- Preserve the legacy Procedure behavior and callback shape where practical. Migrate obsolete config/NBT reads inside those Procedures to Plugin-generated Core accessor blocks; keep effect logic in the Procedure or deliberately documented World implementation.
- Specify cast result states (at minimum success, unknown/unavailable spell, invalid context, and insufficient mana) so procedures/UI can give correct feedback and avoid applying effects after a failed payment.
- Core performs authoritative state changes on the logical server. Client-only rendering/sounds and UI updates consume synchronized outcomes; they must not cause a second cast or mana charge.

The dispatch mechanism and registration lifecycle must be settled before migrating `SpellSelector`, `CastSelectedSpell`, or `CastSpell`; see [ProcedureMigrationMap.md](../Migration/ProcedureMigrationMap.md).

### MCreator Organization and Procedure-First Rule

Keep authored behavior visible and editable as MCreator elements. Prefer Procedure elements for calculations, conditions, event sequencing, player workflows, and spell effects; preserve existing Procedures when replacing only their obsolete data access. Use Plugin custom Blockly blocks to make typed Core calls from Procedures. Locked Java Code elements are appropriate for narrow integration boundaries MCreator cannot express (registries, typed data adapters, item capabilities/menus, network hooks, client render registration, and generator glue), not as replacements for authored gameplay Procedures.

Use these MCreator workspace folders as content is migrated:

| Workspace | Folders |
|---|---|
| Core | `Core/Spells`, `Core/Mana`, `Core/Staffs and Grimoire`, `Core/Barriers`, `Core/Commands`, `Core/Utilities` |
| World of Magic | `World/Spells`, `World/Barriers`, `World/Staffs`, `World/Progression`, `World/Controls`, `World/Screens`, `World/Overlays`, `World/Rendering`, `World/Particles`, `World/Data` |
| Plugin source | Java source packages `element`, `ui`, `blockly`, `generator`, `templates`; the MCreator testing workspace uses `Examples/Spells` and `Examples/Staffs` |

Place fixtures under an `Examples` subfolder of the relevant category and migrated production content beside its category, not mixed with generated Java. The Plugin remains a development tool rather than a content-owning MCreator workspace. These folders are now present in the Core, World of Magic, and Plugin Testing MCreator workspaces; elements are added/moved during their owning migration stage so each Procedure is introduced alongside its dependencies and behavior test.

### Mana and Player Stats

- Core owns the five shared legacy stats: `ManaControl`, `ManaCurrent`, `ManaPerception`, `ManaReserves`, and `ManaStrength`, including defaults, persistence, synchronization, bounds, and server-side mutation.
- Core exposes typed read/update operations used by World progression/UI and cast logic. World controls how players earn or change stats; it does not duplicate attribute storage.
- `CalculateManaCost` is the single total-cost formula: `baseCost + baseCost * ((spellControl * controlScaling) / 10) * ((spellStrength * strengthScaling) / 10)`. It divides by affinity scaling only when affinity matches and the divisor is finite and positive, after calculating the complete legacy result. It does not consume mana or apply payment fractions.
- `ConsumeMana` performs one synchronous server-side Procedure call: it checks that the full amount is available before changing attributes, spends current mana first, and spends reserves only when the caller permits them. The cast lifecycle owns any 20% upfront/channel-time schedule by invoking this Procedure with the appropriate amount. Reserve gamerule wiring and `NotEnoughMana` feedback remain to be connected. Source eligibility and `sourceCost` are explicitly outside this Core contract and belong to the future Source module.
- Renderers and overlays may read synchronized values, but all expenditure and mutation is server-authoritative.

### Staff and Grimoire

- Core owns generic staff inventory/menu behavior and Grimoire stack identity/validation. Plugin-generated Staff elements supply per-item identity, presentation, configured slot count, and creative-tab placement.
- Spell-slot contents persist with the individual staff ItemStack; no global slot state should cause two staffs to share contents.
- Only valid Core Grimoire spell stacks are accepted in spell slots. Empty slots are valid. Define behavior for stale/unknown spell IDs at insertion and cast time.
- A generic staff cast reads the intended slot/selection through Core APIs and enters the same cast lifecycle as other spell casts. Item-specific callbacks may choose when to request a cast, but may not reimplement mana spending or effect dispatch.
- Preserve explicit main-hand/off-hand behavior, sneak-menu behavior, slot limits, and close/reopen persistence. Any departure from legacy staff behavior should be recorded in the Procedure map.

### Barriers and Shared Utilities

- Core owns dimension-scoped barrier state, lifecycle/duration, generic collision queries, and reusable geometry/math only where those utilities have cross-content callers.
- World owns spell-specific barrier parameters, creation triggers, and shatter visuals/effects.
- Barrier queries and mutations must be dimension-scoped and server-authoritative. Collision handling must define entity/projectile participation, expiry, removal, and shatter behavior.
- Rendering is client-only and should not be a prerequisite for authoritative collision or barrier-state updates.

The legacy source-only `TickBarrierDurationsProcedure` runs once per server tick, traverses the global `OverworldBarriers` list backwards, removes null/empty records, decrements `remainingDuration`, and calls the World `BarrierShatterEffectProcedure` before removing an expired record. The event level is used for shatter even though the list is named Overworld-only; dimension-scoped state and ticking are target behavior, not legacy behavior.

The source-only Java `RenderingMathUtil` is a helper, not an MCreator Procedure. It delegates local-vector rotation to `RotateXYZProcedure` after converting yaw/pitch/roll degrees to radians; computes a yaw/pitch look direction; samples floating-point ranges, integer ranges, and random signs; and samples points on or inside regular polygons (clamping side counts to at least three). Keep callable game behavior in Procedures where practical; Java is justified for narrow vector/rendering/random API adapters that Blockly cannot express cleanly.

## Change and Compatibility Rules

- Changes to Core APIs require updating Plugin-generated templates, World integrations, the API documentation, and test fixtures in the same migration slice.
- Generated code must compile after regeneration from MCreator; do not rely on edits to generated source as the integration contract.
- Plugin code generation should be deterministic and namespaced by the owning workspace. Core registrations must not duplicate content emitted by dependent mods.
- Data-schema changes belong in the spell/staff structure doc. Update this document only for ownership, runtime semantics, or cross-module API changes.
- For each legacy Procedure, record owner, trigger/callback, dependencies, behavior test, and any intentional replacement in [ProcedureMigrationMap.md](../Migration/ProcedureMigrationMap.md).
- Keep API methods/blocks narrow and typed. Do not expose Gson internals, config file paths, or implementation-specific item NBT as the long-term authoring contract.

## Remaining Contract Decisions

- Final non-Source spell/staff data fields: owned by [Spell+StaffDataStruct.md](../../Spell%2BStaffDataStruct.md). Source fields and semantics are deferred to the [Eminent Magic: Source design](../../Eminent%20Magic:%20Source/Source%20and%20Resurrection%20Design.md).
- Concrete spell-effect registration/dispatch API and how Plugin-generated World content registers handlers.
- Whether legacy staff tick/stop-use Procedures still have hosts or are intentionally retired; inspect generated references before deciding.
- Whether each legacy rendering utility Procedure is shared Core API or World-local; preserve behavior and choose ownership based on actual cross-module callers.