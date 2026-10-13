# Spell and Staff Data Structure

## Spell Definition

Each Spell is authored as an MCreator Spell element. Its identity is its owning workspace mod ID plus the element registry name, producing a stable namespaced ID (`<modid>:<registry_name>`). MCreator supplies both values; authors do not duplicate or independently edit them in the Spell fields. The namespaced ID is not the display name and is the only runtime identity.

| Field | Type | Meaning / rules |
|---|---|---|
| `id` | Generated namespaced identifier | `<workspace modid>:<element registry name>`, for example `eminent_magic_world:zoltraak`. Derived from the MCreator workspace and element; not an editable data field. |
| `spellName` | Display name string | Player-facing name; independent of the stable namespaced ID. |
| `baseCost` | Nonnegative numeric value | Base mana cost before scaling. Preserve fractional results if the original cost formula produces them; do not require an integer unless the implementation explicitly rounds. |
| `strengthScaling` | Nonnegative numeric value | Multiplier for the spell's per-spell strength setting in the cost calculation. Default `1`. |
| `controlScaling` | Nonnegative numeric value | Multiplier for the spell's per-spell control setting in the cost calculation. Default `1`. |
| `element` | Element identifier | One value from the centrally maintained, easily editable element list. Initial values: `pure`, `fire`, `water`, `earth`, and `wind`. Default `pure`. |
| `duration` | Nonnegative integer ticks | Active spell duration after channeling completes and `onCast` is triggered. Default `0`, meaning an instant spell with no continuing active-duration ticks. |
| `onCast` | Procedure trigger | Procedure invoked once when charging completes and the spell is dispatched. |
| `whileCasting` | Procedure trigger | Procedure invoked once per tick during the active spell duration. For `duration` greater than zero, it runs for that many ticks after `onCast`; for duration `0`, it does not run. |
| `whileChanneling` | Procedure trigger | Procedure invoked once per tick during the pre-cast channel/charge phase. |

`onCast`, `whileCasting`, and `whileChanneling` are trigger hooks on the Spell element, not values serialized into the spell's runtime JSON. In MCreator, each trigger should let the author select an existing Procedure or create a new one; its generated procedure dependencies should expose the parameters supported by that trigger. Define and document the exact available parameters for each trigger in the Plugin implementation.

The canonical packaged/config record uses these serialized fields: `spellName`, `baseCost`, `controlScaling`, `strengthScaling`, `element`, and `duration`. Its key/resource location supplies the namespaced ID. Trigger Procedure references, staff affinity, and player-specific settings are not part of this record. `manaCost` is not a canonical field; it is accepted only as a legacy Plugin/config alias during migration and maps to `baseCost`.

Example record (the resource location is `data/eminent_magic_world/spells/zoltraak.json`):

```json
{
	"spellName": "Zoltraak",
	"baseCost": 25.0,
	"controlScaling": 1.0,
	"strengthScaling": 1.0,
	"element": "pure",
	"duration": 0
}
```

An omitted optional field uses the corresponding packaged/default value. A malformed or out-of-range field uses that same field's packaged/default value and produces a clear warning; a malformed whole file is left untouched and packaged definitions remain active. A record with an invalid identity is not registered. Unknown config IDs are retained for inspection but never become runtime spells merely because they appear in config.

### Per-Spell Player Settings

Each player has separate strength and control settings for each spell rather than one shared pair applied to every spell:

- `spellStrength`: numeric setting for a particular spell, minimum `0`, maximum equal to that player's current maximum strength.
- `spellControl`: numeric setting for a particular spell, minimum `0`, maximum equal to that player's current maximum control.

Changing the player's maximum must not leave a stored per-spell setting above the new maximum; clamp it when read or when the maximum changes, and use the same rule in UI and runtime. The persistence key must include the full namespaced spell ID. These settings are distinct from the spell definition's `strengthScaling` and `controlScaling` multipliers.

### Cost Calculation

Create a Core MCreator Procedure for spell cost calculation and initially preserve the original mod's calculation logic. Keep it as a Procedure specifically so the formula can be changed later without changing every spell or Blockly caller. The Procedure takes a spell definition plus the caster's per-spell control and strength settings, applies the definition's scaling fields, and returns the effective cost.

The current legacy calculation is the compatibility starting point:

`baseCost + baseCost * ((spellControl * controlScaling) / 10) * ((spellStrength * strengthScaling) / 10)`

The Core Procedure must apply staff elemental affinity to that result: when the staff's affinity matches the spell's element, divide the calculated mana cost by the staff's `affinityScaling`; otherwise leave the cost unchanged. `affinityScaling` must be finite and greater than zero to avoid division by zero or invalid costs. A value of `1` gives no discount. The exact rounding/payment behavior must remain consistent between the displayed cost and the amount charged.

The legacy cost formula and the new payment schedule are separate concerns. The Core Procedure returns the final mana cost after spell scaling and any matching staff affinity discount; the cast lifecycle then splits that final value into the initial and channel payments below.

## Legacy Data and State Migration

The original `/config/eminentmagic/Spells.json` format is a root `spellInfo` array. Each row uses numeric `spellID`, `baseCost`, `costControlScaling`, `costStrengthScaling`, and `sourceCost`. The new Core config is per namespace at `config/<namespace>/Spells.json`, with a `spells` object keyed by the full namespaced ID. The compatibility policy is a one-time importer, not a permanent runtime compatibility layer:

- Run only when the legacy file exists and the destination namespace config does not. Never overwrite an existing destination config.
- Preserve the legacy file byte-for-byte as `Spells.json.legacy-backup` before attempting conversion. If a backup already exists, leave it untouched and do not overwrite the source file.
- Map known legacy IDs to explicit namespaced spell IDs and map `baseCost` unchanged, `costControlScaling` to `controlScaling`, and `costStrengthScaling` to `strengthScaling`. The authored packaged Spell record supplies `spellName`, `element`, and `duration`, which the old config did not contain.
- Do not import legacy ID `0`; it is the empty-spell sentinel, not a castable definition. IDs `1` and `2` map to `eminent_magic_world:zoltraak` and `eminent_magic_world:freeze_coffin`. ID `3` is Ingall and remains deferred to the future Source module; do not create or register an Ingall spell in Core or World of Magic as part of this migration.
- `sourceCost` is Source-specific and is not copied into the Core schema. Keep it in the untouched backup and include it in the migration report so nonzero requirements cannot be silently discarded. Unknown numeric IDs are likewise preserved in the backup and reported for an explicit mapping decision.
- If the legacy JSON is malformed, the importer leaves it untouched, uses packaged defaults, and reports the expected file path and recovery action. Missing fields in a valid row use the packaged value; invalid numeric values use the packaged value with a warning. A destination file already present takes precedence and is never merged destructively.

The original staff-selection flow reads the zero-based `SpellSlot` index from the staff ItemStack custom data, reads that slot from the staff's item-handler inventory, then reads numeric `spellID` from the contained spell stack. Off-hand staff takes priority over main hand. The target stores the selected zero-based slot and inventory on that same staff ItemStack, while each contained Grimoire stack stores a namespaced spell ID. Core APIs perform the lookup; Procedures do not parse arbitrary custom data. Empty/invalid/removed spell IDs resolve to an explicit absent result and cannot be cast.

The original Spellbook is a single-spell ItemStack with numeric `spellID` custom data. Its content maps to one Core Grimoire stack containing the namespaced ID. The existing right-click/GUI interaction is a separate workflow decision: it remains a World Procedure/callback until the later World stage either migrates it or explicitly replaces it with a documented Grimoire workflow. Spellbook UI slot indexes are not staff slot indexes and must not be conflated.

| Legacy numeric spell ID | Legacy meaning | Stage 0 disposition |
|---|---|---|
| `0` | Empty spell sentinel | No spell definition or Grimoire stack. |
| `1` | Zoltraak | `eminent_magic_world:zoltraak`. |
| `2` | Freeze Coffin | `eminent_magic_world:freeze_coffin`. |
| `3` | Ingall | Deferred to Source; no Core/World mapping or import in this migration. |

This mapping preserves the legacy spell lookup behavior while changing identity. The original `GetCurrentSpellID`, `SpellSelector`, `GetSpellbookSelfSpellName`, `GetSpellbookSpellName`, and exported `getSpellIDFromItem.ptpl` remain Procedure-level behavior/call sites to be migrated; Plugin Blockly accessors replace only their obsolete data/NBT reads.

## Staff Definition

| Field | Type | Meaning / rules |
|---|---|---|
| `staffName` | Display name string | Player-facing name for the staff. |
| `staffTexture` | Texture | Item texture used by the generated staff model. |
| `elementalAffinity` | Element identifier | Element for which this staff discounts spell mana cost. Default `pure`. |
| `affinityScaling` | Positive numeric value | When the staff affinity matches the spell element, divide the calculated cost by this value. Default `1`; must be finite and greater than zero. |
| `spellSlots` | Integer | Number of spell slots; minimum `1`, maximum `12`. Zero-slot staffs are invalid. Slot count is fixed for a registered staff and must not change dynamically. The current Plugin's allowed range `0..12` is a known mismatch and must become `1..12` when the Staff editor is updated. |
| `creativeTabs` | List of creative-tab references | One or more tabs in which the staff is displayed. |

Staff contents and selected-slot state are stored on the individual staff ItemStack. Different staff stacks must not share slot contents or selection state.

## Staff Selection and Spell UI

The original mod already has spell representation, a custom UI, and scroll-wheel input for switching spells. Preserve that user-facing workflow while adapting its storage and rendering to the new Grimoire-based representation.

- When a player holds staffs in both hands, selection prioritizes the off-hand staff; otherwise use the main-hand staff.
- Cycling selection skips empty slots and selects the next slot containing a valid Grimoire spell, wrapping around the staff's fixed slot list.
- If no slot contains a valid spell, the UI displays `No spells` in the location where the selected spell name would normally appear. Casting with no selected spell must do nothing and must not consume mana.
- Invalid/removed spell IDs in a slot are not valid spells for selection; skip them while cycling and define a clear UI fallback if every populated slot is invalid.
- The selected slot is per staff stack and must remain valid when the stack is moved between inventory slots or hands.

## Spell Lifecycle

Let `C` be the final cost returned by the Core cost Procedure, after spell scaling and staff affinity. The intended payment schedule is:

1. Immediately attempt to consume `0.20 * C` mana. Start channeling only if this initial payment succeeds.
2. During channeling, invoke the spell's `whileChanneling` Procedure once per tick and consume the remaining `0.80 * C` over time.
3. Use the legacy control-based channel pacing as the starting reference for the over-time payment. In the original, `MANA_CONTROL` determines charge time from cost, and the resulting rate is consumed once per effect tick. Adapt this pacing to the new per-spell `spellControl` setting and the 80% remainder; do not reuse the original full-cost payment unchanged.
4. When the full 80% remainder has been successfully consumed, end channeling and trigger `onCast` exactly once.
5. Begin the spell's active duration after `onCast`. Invoke `whileCasting` once per active tick until `duration` ticks have elapsed or the spell is explicitly stopped. A `duration` of `0` means `onCast` runs once and there is no continuing active phase.

The 20%/80% schedule is the intended new contract; it is **not** what the original mod currently implements. The original calculates a channel duration from total cost and the player's `MANA_CONTROL` attribute, then drains mana over channel ticks; it does not charge an upfront 20%, and its channel tick ignores the mana-consumption result. The new implementation must track successful payment so that the cast cannot fire until exactly the initial 20% and remaining 80% have been paid, and must cancel safely if a tick payment fails, the player stops using the staff, or the channel is otherwise interrupted. Reconcile legacy `SpellSlot`, `spellCancelled`, staff-use, and cooldown behavior with the new state machine.

Channel duration and spell `duration` are distinct: channel duration is the time needed to pay the 80% remainder; spell duration is the active period after casting. The Core cost Procedure is the single source of truth for both displayed and charged total cost, including affinity discounts. Define the exact per-tick payment precision/remainder handling so fractional costs are neither lost nor charged twice.

### Channel Pacing Decision

For total cost `C > 0` and finite positive per-spell control `S`, use `ceil(16 * C / S)` channel ticks. Divide the unpaid portion of `0.80 * C` by the ticks remaining on each payment request; the final request is the exact remainder. Reject a positive-cost cast before charging when `S` is zero or invalid. A zero-cost cast has no channel phase.

## Blockly Interface

The Plugin adds a custom Spell data type and a dedicated Spell interaction category in the Blockly block menu. Spell-typed values reference the stable namespaced spell identity and are resolved through Core, rather than copying player-facing text or relying on a staff slot index after selection.

**Blocks to implement**

| Block | Behavior |
|---|---|
| `currentlySelectedSpell` | Returns the Spell value for the selected valid spell on the held staff, prioritizing off hand over main hand. Define an explicit empty/invalid result when no staff or spell is available. |
| `getSpellStrength` | Returns the player's per-spell strength setting, clamped to the player's current maximum. Takes a Spell input and the relevant player context. |
| `getSpellControl` | Returns the player's per-spell control setting, clamped to the player's current maximum. Takes a Spell input and the relevant player context. |
| `getSpellCost` | Calls the Core cost Procedure using the spell, caster's per-spell settings, and relevant staff affinity; must match the amount actually charged. |
| `getSpellElement` | Returns the element identifier for the Spell input. |
| `castSpell` | Starts or requests the selected spell's cast lifecycle. It must not bypass selection, channeling, mana payment, affinity, or server-authoritative dispatch. Define whether it returns a success/result value for callers. |

The custom Spell data type should also support safe equality/identity checks by namespaced ID. Unknown or removed spell IDs must produce an explicit empty/invalid value rather than silently resolving to another spell.

## Editable Element List

Keep the available elements in one centralized, easily edited definition used by Spell/Staff authoring choices, validation, generated data, Blockly/tooltips, and runtime validation. Adding an element should require updating this one list and any element-specific assets or behavior, not editing several unrelated hard-coded enums. Initial list: `pure`, `fire`, `water`, `earth`, `wind`.

## Deferred Implementation Details

- Define the exact MCreator dependency parameters available to each spell trigger (`onCast`, `whileCasting`, `whileChanneling`) and generate the Procedure references/registration.
- Wire and test native channel Procedures against the legacy staff-use stop conditions. Core's cast-state API now uses the new 20% upfront / 80% remainder schedule and the documented control-paced tick count.
- Confirm the duration clock starts after `onCast`, that `duration: 0` is instant, and that `whileCasting` executes exactly once per active tick for positive durations.
- Apply off-hand priority consistently to selected-spell lookup, UI, cast, and staff menu selection when both hands contain staffs.
- Show `No spells` when all staff slots are empty; show a distinct unavailable-spell fallback when slots contain only unknown/removed IDs.
- Decide the UI and persistence policy when a player's maximum control/strength is reduced below stored per-spell values; the proposed behavior is to clamp without losing unrelated spell settings.
- Implement and test the one-time legacy config importer and its backup/report behavior before enabling it in Core.

