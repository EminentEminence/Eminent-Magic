# Eminent Magic: Source and Resurrection Design

## Status and Scope

This document collects the legacy Source and resurrection mechanics for a future standalone `Eminent Magic: Source` mod. These mechanics are **deferred** from the current Core, Development Plugin, and World of Magic migration. Do not add Source-specific fields or behavior to the current spell/staff contract until this future module is designed.

The proposed future module groups together:

- Source score calculation and its relationship to mana attributes.
- Spell source requirements (`sourceCost`) and cast eligibility.
- The Vesselless state and the death-interception lifecycle.
- Ingall as a rescue/release spell tightly coupled to Vesselless.

This is a design/inventory document, not a finalized game design. Several legacy behaviors appear unused or questionable and are called out below for an explicit decision before implementation.

## Legacy Source Semantics

The original mod does **not** contain a separate Source attribute, Source item, or Vessel entity. `GetSource` is a Procedure that derives a score from the entity's **base** mana attributes. Missing attributes are treated as zero. The observed formula in `Original Mod/src/main/java/net/emmy/eminentmagic/procedures/GetSourceProcedure.java` is:

`round((strength + control + perception + reserves / 100) / 4)`

Keep this as a compatibility observation only. Confirm whether the parentheses and `/ 100` placement are intentional before adopting it as the future Source model.

`sourceCost` behaves like a **minimum source-score requirement**, not a consumable resource quantity. The original `ConsumeManaProcedure` compares a supplied source requirement with the caster's Source score as part of its eligibility check, alongside mana/reserve checks. This suggests Source eligibility and mana payment are coupled in the legacy helper, but does not establish that they must remain coupled in the new design.

Observed usage is incomplete:

- `Spells.json` has `sourceCost: 0` for all current entries, and `OnGameBootProcedure` also creates defaults with zero source cost.
- `FreezeCoffinSpell` and `Ingall` load `sourceCost` and call `GetSource`, but do not use the loaded requirement to gate their effects or payment.
- The inspected `ZoltraakSpell` calls `ConsumeMana` with `SourceRequirement` set to zero, so it imposes no meaningful source threshold there.
- Ingall reads a configured mana cost of 150 but does not charge it.
- Source score is based on base attribute values in the original implementation; whether temporary modifiers should affect Source is undecided.

The future Source design should decide whether Source is a derived score, a persistent attribute/resource, or a larger entity/lifecycle concept. Do not infer a consumable Source pool from the old field name.

## Legacy Ingall and Vesselless Behavior

### Cast Flow

The observed legacy route is:

`staff slot -> CastSelectedSpell -> SpellSelector -> ManaChannel expiry -> CastSpell -> Ingall (spell ID 3)`

The generated `CrudeStaffItem` and `CrystalStaffItem` call `CastSelectedSpell`; `ManaChannelEffectExpiresProcedure` reaches `CastSpell`. Ingall searches nearby for a living entity with the Vesselless effect, removes that effect, and emits Happy Villager particles at the target. Removing the effect invokes the expiry callback, which restores the entity and cancels the pending death path.

`IngallProcedure` also deals 4 points of Magic damage to the **caster**, not the rescued target. Whether this is intentional is unknown. Its configured 150 mana cost and loaded source requirement are not currently enforced by the inspected procedure.

### Death Interception

`DeathReplacementProcedure` is attached to living-entity death. If the entity is not already Vesselless, it:

1. Cancels the death event and sets health to 1.
2. Applies Vesselless for 110 ticks.
3. Schedules a follow-up check after 105 ticks.
4. If the effect is still present, removes Resistance and Invisibility, changes a `ServerPlayer` to Survival, and deals very large damage using the original damage source.

This is a delayed death/interception mechanic, not a conventional respawn routine. Its exact behavior for non-player entities, repeated lethal damage, disconnects, server shutdown, and effects removed by other systems needs to be decided and tested.

### Vesselless Lifecycle

`Vesselless.mod.json` wires three Procedure callbacks:

- `VessellessEffectStartedapplied`: saves supported player game mode and position in persistent data (`Vessel Gamemode`, `Vessel X/Y/Z`); applies Invisibility and Resistance; switches `ServerPlayer` to Spectator.
- `VessellessOnEffectActiveTick`: emits Soul Fire Flame particles, teleports the entity to the saved position, and decrements saved Y.
- `VessellessEffectExpires`: restores a saved supported game mode and removes the added effects. Generated registration runs this callback on both effect removal and expiry.

Ingall rescues by removing Vesselless, which triggers this cleanup. The effect manifest selects `manadraineffect` as its icon even though both `vesselless.png` and `manadraineffect.png` exist; verify the intended icon. No Ingall-specific particle or sound asset was found; its observed particles are vanilla Soul and Happy Villager particles.

## Proposed Future Ownership and Boundary

- **Eminent Magic: Source** should own the Source model/API, source requirements, Vesselless state, death interception and expiry/rescue lifecycle, and the initial Ingall implementation because Ingall's primary purpose is to interact with that lifecycle.
- **Eminent Magic: Core** should continue to own generic mana attributes, mana payment, spell lookup, staff selection, and cast lifecycle. Core must not hardcode Source scoring or directly depend on the Source mod. If Source requirements participate in casting, define an optional extension/validation boundary that Core can invoke without knowing Source-specific types.
- **Eminent Magic: Development Plugin** may later provide authoring fields or Blockly accessors for Source concepts, but only after the Source runtime API and data model are stable. The current Plugin migration should not add `sourceCost` fields as though they were settled.
- **Eminent Magic: World of Magic** may eventually author spells that call the Source API. Keep unrelated World spells and common runtime independent of Source. If Ingall is later moved back into World of Magic, World should consume a stable Source API rather than own Vesselless/death lifecycle internals.

The future Source module should depend on Core, not the reverse. World of Magic can optionally depend on Source when it contains spells or effects that use it. A workspace without the Source mod must still load and cast spells that have no Source requirements.

## Decisions Required Before Implementation

- Is Source a derived score, a mutable/persistent resource, a status, or a broader system? Which values contribute, and do modifiers count?
- Is the observed `GetSource` formula intentional? What are its units, bounds, rounding rules, and missing-stat defaults?
- Does `sourceCost` mean a minimum score, and where is it authored? Should a requirement be optional/absent rather than defaulting to zero?
- Should Source eligibility be checked before mana payment, and should failure consume any mana or produce feedback?
- Should Ingall cost 150 mana, enforce a Source requirement, damage the caster, or have different targeting/range rules?
- What entities can become Vesselless? What happens to non-player entities and players in unsupported game modes?
- Which event cancellation, timer, persistence, logout/restart, repeated death, and effect-removal behaviors are intended?
- Is rescue always performed by Ingall, or should Source expose a general rescue/release API for future content?
- Which texture, particles, sounds, and client/server effects are intended?

## Legacy Items to Defer as a Unit

The following original elements are inventoried in the current migration map but should not be implemented as ordinary Core or World-of-Magic migration tasks until this design is resolved:

- `GetSource` and its source-score behavior.
- The Source eligibility branch in `ConsumeMana` and all reads of legacy `sourceCost`.
- `Ingall` and its Vesselless target/rescue behavior.
- `DeathReplacement`.
- `Vesselless`, `VessellessEffectStartedapplied`, `VessellessOnEffectActiveTick`, and `VessellessEffectExpires`.

Keep their Procedure bodies and legacy assets available as references. The exported `getSpellProperties.ptpl` is compressed template material, has placeholder config/key values, and no authored/generated call site was found; do not port its old config lookup as the future API. `FreezeCoffinSpell` also calls `GetSource` and reads a source requirement without using either to gate its effect; remove or replace that incidental dependency when migrating Freeze Coffin, rather than making an otherwise unrelated spell require the future Source mod.

## Acceptance Tests for the Future Source Module

- [ ] Verify Source values at attribute boundaries, with missing attributes, and with temporary modifiers; confirm the selected formula and rounding in both single-player and multiplayer.
- [ ] Verify a spell with no Source requirement works when the Source mod is absent; a spell requiring Source fails clearly when the mod/API or required value is unavailable.
- [ ] Verify source-gate failure does not accidentally consume mana unless explicitly designed to do so; verify successful casts consume mana exactly once.
- [ ] Trigger lethal damage on a player and a non-player entity; confirm cancellation, Vesselless application, spectator/effect behavior, and timeout behavior match the decisions above.
- [ ] Rescue a Vesselless target with Ingall; confirm the correct target is restored, pending death is cancelled, game mode/effects/position are restored correctly, and the caster receives only intended costs/damage.
- [ ] Test manual effect removal, dispel, disconnect/reconnect, save/reload, dimension change, repeated lethal damage, and server restart during the Vesselless interval.
- [ ] Verify server-authoritative behavior with multiple clients and confirm particles, sounds, and effect icons render without client-only crashes or duplicate gameplay effects.
