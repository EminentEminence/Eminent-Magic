# Eminent Magic Migration Checklist

## Scope and Ordering

This checklist tracks migration from `Original Mod/` into `Eminent Magic: Core/`, `Eminent Magic: Development Plugin/`, and `Eminent Magic: World of Magic/`. Work is ordered by dependency: data contracts, shared runtime, authoring/generation, authored game content, then integration and polish.

The legacy inventory is 86 root element manifests, 10 nested rendering-utility manifests, two exported procedure templates, and associated generated Java/resources. Existing Core, Plugin, and World of Magic implementations are foundations only unless a task below explicitly records full legacy parity.

**Procedure markers:** `Legacy Procedures: ...` identifies original MCreator elements whose `_type` is `procedure`; preserve their behavior by migrating/updating those procedures, not by dropping them when the data format changes. `Host elements: ...` identifies non-Procedure elements that may invoke procedures through callbacks. New spell-data JSON/config may be replaced with Plugin-provided custom Blockly code blocks that call Core APIs directly; that is a data-access replacement, not a reason to remove the spell, staff, UI, or event procedures that use it.

## 0. Inventory and Data Contracts

- [x] Inventory the legacy manifests, exported templates, generated source, and resources.
- [x] Identify existing foundations: Core spell registry/list command, Grimoire and spell tab, and staff inventory/menu; Plugin Spell/Staff elements and generators; World of Magic test Spell/Staff elements.
- [x] Define the canonical spell schema across Plugin output, Core runtime/config, and World of Magic content. Map `baseCost`, `costControlScaling`, and `costStrengthScaling` to `baseCost`, `controlScaling`, and `strengthScaling`; define namespaced ID derivation and defaults in [Spell+StaffDataStruct.md](Spell%2BStaffDataStruct.md). Defer Source and `sourceCost` semantics to the future [Eminent Magic: Source design](Eminent%20Magic:%20Source/Source%20and%20Resurrection%20Design.md). **Legacy Procedures:** `getSpellProperties.ptpl` remains a data-access template to replace with Plugin Blockly accessors, not a Procedure to drop.
- [x] Choose a one-time importer for legacy `config/eminentmagic/Spells.json`; document destination, backup, ID mapping, malformed/invalid fallback, and Source-field handling in [Spell+StaffDataStruct.md](Spell%2BStaffDataStruct.md). **Implementation remains in the Core stage. Legacy Procedures:** data migration itself is not a Procedure; preserved Procedures must consume the new typed API.
- [x] Define the mapping for legacy numeric spell IDs, selected-spell state, Spellbook stacks, staff slots, and Grimoire stack IDs in [Spell+StaffDataStruct.md](Spell%2BStaffDataStruct.md). **Legacy Procedures:** `getSpellIDFromItem.ptpl`, `GetCurrentSpellID`, `SpellSelector`, `GetSpellbookSelfSpellName`, and `GetSpellbookSpellName` remain behavior/call-site migration tasks; only obsolete data reads are replaced.
- [x] Define ownership/API boundaries and a Procedure-first MCreator folder convention in [Ownership and API Contract](Docs/Core/OwnershipAndAPI.md). **Legacy Procedures:** ownership retains the original procedure inventory below; Java is limited to integration/API code where MCreator cannot express the boundary.
- [x] Inspect and record source-only `TickBarrierDurationsProcedure.java` and `RenderingMathUtil.java` behavior in [Ownership and API Contract](Docs/Core/OwnershipAndAPI.md) and the stage notes below. `TickBarrierDurationsProcedure.java` is generated Procedure logic without a root manifest; `RenderingMathUtil.java` is Java helper code, not an MCreator Procedure.

**Stage 0 implementation notes**

- The old config is a `spellInfo` array; import only when the namespaced destination config is absent, preserve the source as a legacy backup, and never import `sourceCost` into Core. IDs `0` and `3` are excluded as empty and Source-deferred; IDs `1` and `2` map to World of Magic Zoltraak and Freeze Coffin.
- Legacy staff selection uses a zero-based `SpellSlot` on the staff ItemStack, reads that stack's item-handler slot, and resolves numeric `spellID`; off-hand staff wins. A Spellbook ItemStack stores one numeric `spellID`. The target uses a per-staff selected slot plus a Grimoire stack's namespaced spell ID; the Spellbook interaction remains a World Procedure workflow until a later stage deliberately replaces it.
- `TickBarrierDurationsProcedure` executes once per server tick, decrements `remainingDuration` in the legacy Overworld barrier list, removes null/empty entries, and invokes `BarrierShatterEffectProcedure` before removing expired barriers. Dimension-scoped handling is a target correction, not an observed legacy behavior.
- `RenderingMathUtil` wraps `RotateXYZProcedure` with degree-to-radian conversion, computes a yaw/pitch look vector, samples uniform ranges/signs, and computes points along/inside regular polygon triangles. Preserve those callable behaviors as Procedures when practical; retain a Java adapter only where rendering or random/vector APIs require it.
- Stage 0 establishes the contracts and folder layout, and assigns existing MCreator elements/fixtures to those folders. It does not port legacy gameplay elements into MCreator yet. The Stage 0 end-user verification below remains open until the authoring UI/importer is implemented and exercised in MCreator.

**End-user verification**
- [ ] Using the documented legacy-to-new field mapping, enter or import a representative spell and confirm its name, element, mana cost, scaling values, and namespaced ID are preserved in the new format.
- [ ] Verify the documented behavior for a missing optional field and an invalid value; confirm the user receives a clear fallback or migration instruction rather than silently losing data. Source-specific field behavior is deferred to the Source module.
- [ ] Confirm the written ownership map lets a user identify where to edit a spell, where shared runtime behavior lives, and which existing Procedures remain part of the workflow.

## 1. Core: Shared Runtime and API

Core must expose the APIs required by generated Plugin content and World of Magic before those features are ported.

**Stage 1 implementation progress**

- Implemented foundations: typed immutable spell lookup/listing, canonical field normalization with the `manaCost` alias, legacy World config backup/import attempt at startup, five native MCreator mana Attribute elements and numeric getter Procedures, a native default-false `spellsConsumeReserve` Gamerule element, native Grimoire Item and Eminent Spells Tab elements, registered `CalculateManaCost`, `GetManaCost`, and `ConsumeMana` Procedures, namespaced per-spell setting getter/setter Procedures, namespaced spell existence/name/current-selection Procedures, `IsHoldingStaff`, `DotProduct`, the `NotEnoughMana` damage type, server-side mana expenditure through editable attribute blocks, per-staff selected-slot persistence/resolution/cycling APIs, and a server-owned cast-state/trigger-dispatch boundary.
- These foundations still need in-editor and in-game verification. The one-time import needs an end-to-end run against packaged Zoltraak/Freeze Coffin definitions; cast/channel Procedures, reserve-rule and failure-feedback wiring, staff input hookups, and barriers are not implemented.
- The old cost Procedure's `source` eligibility and partial-payment side effects are intentionally not carried into generic Core payment. Any Source eligibility belongs behind the future Source boundary; failed generic mana payment is atomic and does not consume either pool.
- `CalculateManaCost` preserves `baseCost + baseCost * ((spellControl * controlScaling) / 10) * ((spellStrength * strengthScaling) / 10)` and divides by a matching staff's finite positive affinity scaling only at the end. It calculates a total only; the casting process owns the 20% upfront and channel-time payment schedule.
- `EminentMagicCoreSpellCasting` provides namespaced trigger registration and a server-owned prepared/upfront/channel/active state machine. `startCast` charges the upfront fifth through Core's atomic payment service; its server tick driver charges each exact channel installment before dispatching `WHILE_CHANNELING`. It cancels on payment failure, stopped use, disconnect, or dimension change. Channel pacing uses `ceil(16 * totalCost / spellControl)`; native cast/channel Procedure entry points, reserve-gamerule lookup, failure feedback, cost/control resolution, and World hook registration remain unwired.

- [ ] Complete and test the spell registry/configuration contract, namespaced lookup, validation, defaults, and reload/startup behavior. Preserve or deliberately replace behavior from `GetSpellData`, `GetSpellExists`, `GetSpellName`, and `GetCurrentSpellID`. **Legacy Procedures:** all four named elements are Procedures. `GetSource` is deferred to [Eminent Magic: Source](Eminent%20Magic:%20Source/Source%20and%20Resurrection%20Design.md).
- [x] Register the five synced mana attributes with legacy bounds/defaults in Core. **Still required:** verify persistence/synchronization in-game and migrate their access/progression Procedures. **Legacy Procedures:** No; these five manifests are MCreator `attribute` elements. Their consumers are covered in the procedure tasks below.
- [ ] Finish mana/stat access and progression hooks: the five numeric attribute getter Procedures and per-spell Control/Strength get/set Procedures are registered. `OverrideStats` and permission-guarded progression remain. **Legacy Procedures:** all named behaviors are Procedures.
- [x] Add native `CalculateManaCost` and `ConsumeMana` Procedures under `Core/Mana`. Cost retains the legacy control/strength formula and applies affinity division only after that result; payment prefers current mana, uses reserves only when allowed and sufficient, and contains no cast-timing fractions. **Legacy Procedures:** both are MCreator Procedures; `SpellsConsumeReserve` is a native `gamerule` element.
- [x] Add `GetManaCost` as a native Procedure that resolves the namespaced spell and per-spell settings, then calls `CalculateManaCost`; affinity match/scaling remain explicit inputs until Staff authoring supplies those values. **Legacy Procedures:** `GetManaCost` is a Procedure.
- [ ] Connect native cast Procedures to `GetManaCost` and `ConsumeMana`; resolve `SpellsConsumeReserve` at the call site. The Java cast service now charges the 20% upfront and server-ticked exact channel installments through atomic Core payment and shows action-bar feedback on payment failure. Keep generic payment independent of Source; defer Source eligibility to [Eminent Magic: Source](Eminent%20Magic:%20Source/Source%20and%20Resurrection%20Design.md).
- [ ] Wire the cast-state service to native MCreator Procedures and migrate `SpellSelector`, `CastSelectedSpell`, and `CastSpell`. `GetCurrentSpellID` is now a namespaced-string Procedure and Core selection/cycling APIs exist. Keep spell effects in World of Magic and register trigger handlers through the Core dispatcher. **Legacy Procedures:** all named elements are Procedures; spell metadata itself is data.
- [ ] Port channeling lifecycle and requirements: `ManaChannel`, `ManaChannelActiveTickCondition`, `ManaChannelEffectExpires`, and `OnEntityChannelingTick`. **Legacy Procedures:** the last three are Procedures; `ManaChannel` is a MCreator `potioneffect` element whose lifecycle invokes procedures.
- [ ] Port shared initialization and perception behavior: `OnGameBoot` and `ActivePerception`. **Legacy Procedures:** Yes; both are Procedures.
- [ ] Finish shared staff callbacks: `IsHoldingStaff` is a native Core Procedure, and selected-slot persistence/resolution/cycling APIs exist. Migrate `StaffEveryTickWhileUsingItem` and `StaffOnPlayerStoppedUsing` and reconcile them with Core's item-bound staff inventory/menu. **Legacy Procedures:** all named elements are Procedures. **Host elements:** `CrudeStaff` and `CrystalStaff` are `item` elements with procedure callbacks.
- [x] Add insufficient-mana action-bar feedback to the cast payment path. `NotEnoughMana` also exists as a native Core `damagetype`, but is not used to restore legacy partial-payment self-damage.
- [ ] Implement the reusable barrier service: `GetBarriers`, `ClearBarriers`, `GetLocalDimensionBarriers`, `RemoveLocalDimensionBarriers`, `BarrierCollisionDetection`, `OnBarrierCollision`, and barrier duration/tick handling. **Legacy Procedures:** `GetLocalDimensionBarriers`, `RemoveLocalDimensionBarriers`, `BarrierCollisionDetection`, and `OnBarrierCollision`; `GetBarriers` and `ClearBarriers` are MCreator `command` elements. Also preserve source-only `TickBarrierDurationsProcedure.java` behavior.
- [ ] Port barrier math and assign reusable rendering helpers: `DotProduct` is now an editable Core Procedure; `RotateXYZ`, `RenderCylinder`, and nested `renderingUtilities` ownership/porting remain. Keep helpers in Core only where they are genuinely shared; otherwise keep them with their World of Magic caller. **Legacy Procedures:** all named root and nested utility elements are Procedures.
- [ ] Port shared barrier shatter behavior represented by `ShatterBarrier`, or split its generic service from spell-specific effects as appropriate. **Legacy Procedures:** Yes; `ShatterBarrier` is a Procedure.
- [ ] Verify the existing Core Grimoire, `Eminent Spells` creative tab, spell-list command, staff item/menu, and stack factory remain compatible with the completed runtime. These are existing foundations, not evidence of full feature parity. **Legacy Procedures:** no direct original Core implementation; preserve callbacks from `SpellbookRightclicked` and staff Procedures where those workflows are retained.

**End-user verification**
- [ ] In a clean test world with Core installed, open the spell tab and Grimoire, run `/spelllist`, and confirm valid registered spells are listed without duplicates or missing names.
- [ ] Equip a staff, open its inventory, place and remove Grimoire spell stacks, close/reopen the menu, and confirm only valid spell stacks are accepted and contents persist.
- [ ] Cast representative spells with sufficient mana, insufficient mana, and reserve consumption enabled/disabled; confirm costs, scaling, feedback, and selected-spell behavior match the documented rules.
- [ ] Create and shatter a barrier, then test collision, duration expiry, clearing, and dimension separation through normal gameplay or the intended commands.

## 2. Development Plugin: Authoring and Generation

Plugin work depends on the finalized Core contract. Keep per-mod content in World of Magic rather than embedding it in the shared Plugin.

- [ ] Extend the existing Spell element to represent the agreed non-Source spell fields and any effect metadata selected in section 0. Do not add `sourceCost` or other Source-specific fields until the future Source API/schema is designed. **Legacy Procedures:** Spell definitions/data are not Procedures; preserve the separate spell-effect Procedures listed in the World of Magic tasks.
- [ ] Update Spell validation, defaults, UI, serialization, generated spell JSON, and generated Core integration together; preserve compatibility for existing test elements where feasible. **Legacy Procedures:** JSON/config generation is not a Procedure; call sites in the preserved Procedures must be migrated to the new contract.
- [ ] Add Plugin custom Blockly code blocks for direct access to Core spell data/API, replacing obsolete lookup behavior from `getSpellIDFromItem.ptpl` and `getSpellProperties.ptpl`. Update preserved Procedures to use these blocks; do not replace the Procedure elements with generated JSON or Java. **Legacy Procedures:** Yes; the two `.ptpl` files are exported procedure templates, and their useful logic/call sites must be retained or deliberately superseded.
- [ ] Extend the existing Staff element for legacy staff behavior/metadata where required, including slot count, selected spell behavior, texture, and creative-tab placement. **Legacy Procedures:** staff definitions are not Procedures; retain item callback Procedures such as `CastSelectedSpell`, `StaffEveryTickWhileUsingItem`, and `StaffOnPlayerStoppedUsing` where applicable.
- [ ] Update Staff validation, UI, models/assets generation, and runtime templates to target the finalized Core staff API. **Legacy Procedures:** generation is not itself a legacy Procedure; generated integrations must keep the legacy staff callback behavior.
- [ ] Ensure generated procedures/content call Core APIs rather than duplicating shared mana, spell, or staff runtime logic. **Legacy Procedures:** Yes; preserve/port the original Procedure elements and replace only their obsolete spell-data access with direct API custom blocks where appropriate.
- [ ] Add generator coverage for edge cases: empty/invalid metadata, zero slots, maximum slots, missing textures, namespaced IDs, and multiple content elements in one workspace. **Legacy Procedures:** No; these are new Plugin tests, with procedure call sites covered separately.
- [ ] Regenerate and test the Plugin TestingWorkspace against Core. Existing `Test`/`Test2` spells and sample staffs (`SimpleWoodenStaff`, `IntermediateStaff`, `UltimateStaff`) are fixtures only; they do not replace legacy content.
- [ ] Document Plugin/Core version and regeneration requirements for dependent workspaces.

**End-user verification**
- [ ] In MCreator, create a Spell and Staff element using the Plugin UI, save, close/reopen the workspace, and confirm all entered fields remain intact.
- [ ] Regenerate code and confirm the spell appears in Core's spell list/tab and the staff appears with its selected texture, slot count, and tab placement; confirm no manual generated-source edits are needed.
- [ ] Use the new spell-data Blockly blocks in a Procedure, regenerate, and confirm the generated Procedure compiles and reads the intended spell values at runtime.
- [ ] Regenerate after changing and deleting elements; confirm stale spell/staff registrations do not remain and existing Procedures still retain their behavior.

## 3. World of Magic: Gameplay Content

Begin content migration only after the relevant Core APIs and Plugin output are stable.

- [ ] Author and test the current World of Magic spell content: `AoeSpellTemplate`, `FreezeCoffinSpell`, and `ZoltraakSpell`. Remove Freeze Coffin's unused legacy Source lookup rather than adding a Source dependency. **Legacy Procedures:** Yes; all three are MCreator Procedures. Preserve their effect logic while updating data/API access. Defer `Ingall` to [Eminent Magic: Source](Eminent%20Magic:%20Source/Source%20and%20Resurrection%20Design.md).
- [ ] Port the barrier spell/content behavior: `SimpleBarrier` and `BarrierShatterEffect`; connect it to Core barrier creation, collision, duration, and shatter APIs. **Legacy Procedures:** Yes; both are Procedures.
- [ ] Migrate staff variants `CrudeStaff` and `CrystalStaff` as Plugin Staff elements; verify textures, slot counts, item behavior, and spell compatibility. **Legacy Procedures:** the staff manifests are MCreator `item` elements, not Procedures; preserve their Procedure callbacks, including `CastSelectedSpell` and applicable staff tick/stop-use Procedures.
- [ ] Decide whether the legacy `Spellbook` item and its `SpellbookRightclicked` behavior become the Core Grimoire workflow or remain distinct World of Magic content. Migrate `GetSpellbookSelfSpellName` and `GetSpellbookSpellName` to the chosen design. **Legacy Procedures:** `SpellbookRightclicked`, `GetSpellbookSelfSpellName`, and `GetSpellbookSpellName` are Procedures; `Spellbook` is an `item` element whose callback must be retained or intentionally replaced.
- [ ] Defer `DeathReplacement` and the complete Vesselless feature (`Vesselless`, `VessellessEffectExpires`, `VessellessEffectStartedapplied`, and `VessellessOnEffectActiveTick`) as a single lifecycle to [Eminent Magic: Source](Eminent%20Magic:%20Source/Source%20and%20Resurrection%20Design.md). **Legacy Procedures:** `DeathReplacement` and the three `Vesselless...` lifecycle elements are Procedures; `Vesselless` is a `potioneffect` host element.
- [ ] Port `DoesPlayerHaveOp` where required by World of Magic commands or admin-only workflows. **Legacy Procedures:** Yes; it is a Procedure.
- [ ] Migrate the original `Spells` tab content into the shared Core spell tab; verify World-authored spells and staffs appear there without duplicating Core-owned tab/item registrations. **Legacy Procedures:** No; `Spells` is a MCreator `tab` element. Spell Procedures remain separate.
- [ ] Port the original `magicstaff` item tag and any other gameplay data tags referenced by migrated features. **Legacy Procedures:** No; these are data tags, not Procedures.

**End-user verification**
- [ ] In a fresh world, obtain each migrated spell and staff through its intended recipe, command, or creative-tab path; confirm names, icons, textures, and descriptions are correct.
- [ ] Cast each migrated spell through its normal staff/Grimoire workflow and verify its visible effect, target rules, mana cost, and failure behavior.
- [ ] Exercise barrier creation and shattering and spellbook/Grimoire actions; confirm the replacement workflow is understandable and no legacy item callback is lost. Vesselless/death and Ingall rescue tests are deferred to the Source module.
- [ ] Save and reload while carrying a configured staff/Grimoire and while relevant effects are active; confirm inventory and effect state behave as designed.

## 4. World of Magic: Controls, Progression, UI, and Presentation

These features depend on Core mana/stat/spell APIs and the content decisions above.

- [ ] Port controls and keybind workflows: `ControlSpellControl`, `ControlSpellStrength`, `CycleSpell`, `OpenSkillsSummary`, `TestingHotkey`, and `ToggleActivePerception`; include callback Procedures `OpenSkillsSummaryOnKeyPressed` and `ToggleActivePerceptionOnKeyPressed` and preserve intended permissions/conditions. **Legacy Procedures:** the two named callbacks are Procedures; the six control entries are MCreator `keybind` elements.
- [ ] Port skill/progression screens and workflows: `ManaSkillsSummaryGUI`, `HundredByTwentyfive`, and `SeventyfiveByFifteen`; connect edits and displays to Core attributes and stat APIs. **Legacy Procedures:** these are MCreator `gui` elements; preserve and reconnect any procedure callbacks in their UI workflows.
- [ ] Port spellbook and staff screens/workflows: `SpellBookGui`, `StaffGUI1`, and `StaffGUI4`, unless their responsibilities are deliberately replaced by the Core Grimoire/staff menu. Record any retired screens and their replacement. **Legacy Procedures:** these are MCreator `gui` elements; retain `SpellbookRightclicked` and staff callback Procedures as applicable.
- [ ] Port overlays `OverlayManaStats` and `OverlaySpellInfo` using the Core runtime data contract. **Legacy Procedures:** the overlays are MCreator `overlay` elements, not Procedures; use the new custom API blocks/data access and retain any separate rendering Procedures they call.
- [ ] Port mana and visual rendering behavior `RenderMana`; keep `DefaultRenderer` and `CapeRenderer` with World presentation unless another module demonstrably needs them. **Legacy Procedures:** Yes; all three are Procedures.
- [ ] Port the cape particle feature `CapeParticle`, its particle definition, and its textures. **Legacy Procedures:** `CapeParticle` is a MCreator `particle` element, not a Procedure; preserve `CapeRenderer` as a separate Procedure.
- [ ] Migrate all remaining original GUI textures, staff/book item models and textures, language entries, potion-effect textures, particle assets, and damage-type data. Remove obsolete assets only after confirming no migrated content references them.

**End-user verification**
- [ ] Use each keybind with its default and a remapped key; confirm spell cycling, control/strength changes, active perception, and skills-summary opening work without firing while the relevant screen is closed or player is in an invalid state.
- [ ] Open each retained or replacement screen and verify values are readable, controls update the intended stats, and changes persist after closing/reopening.
- [ ] Check mana and spell overlays during gameplay, including low/empty mana and no selected spell; verify text/icons are legible and values update.
- [ ] Observe cape and mana particles, staff/book models, effect icons, and localized names in-game; confirm resources render correctly and no missing-texture placeholders appear.

## 5. Integration, Regression, and Completion

- [ ] Compare every legacy root element manifest against the migrated replacement or a documented intentional retirement; no legacy element should disappear without an explicit disposition. **Legacy Procedures:** explicitly verify all 56 root `_type: procedure` elements and every procedure callback referenced by non-Procedure host elements.
- [ ] Compare all 10 nested rendering-utility manifests against their replacements or retirement decisions. **Legacy Procedures:** Yes; all 10 nested manifests are `_type: procedure` and must be preserved or explicitly retired.
- [ ] Verify all original behaviors in-game across the dependency chain: Plugin-generated spell/staff content, Core mana and cast lifecycle, barriers, progression, UI, and World of Magic effects.
- [ ] Test multiplayer/server authority, save/reload persistence, invalid or missing spell data, insufficient mana, staff inventory persistence, and interactions between multiple spells/barriers.
- [ ] Build Core, build/install the Plugin, regenerate World of Magic, and build/run the World of Magic workspace from a clean state.
- [ ] Update `Docs/Core/DataFlows.md`, `Docs/Core/FunctionalRequirements.md`, Plugin documentation, and World of Magic spell documentation to match the shipped interfaces and behavior.
- [ ] Record the final migration commit/version and list any intentionally deferred or retired legacy features.

**End-user verification**
- [ ] Install the documented release artifacts into a fresh MCreator setup, create/regenerate the World of Magic workspace, and launch a new game without relying on developer-only files or manual source patches.
- [ ] Complete a representative player loop: acquire a staff and spell, inspect it in the Grimoire, cast it, spend/refill mana, change spell controls, use a barrier, and revisit progression/UI screens. Do not treat Source or Ingall resurrection as part of this migration release.
- [ ] Repeat the loop in multiplayer with one client and a dedicated/integrated server; confirm server-authoritative effects, synchronized values, and no duplicate or client-only casts.
- [ ] Save, quit, and reload; confirm player stats, selected spell, staff contents, and any designed persistent effects survive correctly.
- [ ] Compare the shipped behavior against the migration checklist and documented intentional retirements; confirm no user-facing legacy feature is missing without an explicit replacement or disposition.
