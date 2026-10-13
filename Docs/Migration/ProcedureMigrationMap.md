# Legacy Procedure Migration Map

This is the per-element disposition map for MCreator Procedure elements in `Original Mod/`. Preserve each Procedure's behavior, triggers, dependencies, and callback wiring unless its row records an intentional replacement or retirement. Replacing old spell JSON/NBT lookup with Plugin Blockly blocks that call Core is a data-access change; it does not by itself replace the Procedure.

The map is based on the original `.mod.json` manifests, exported procedure templates, and generated-source inventory. A missing manifest callback/caller is not proof that a Procedure is unused; investigate it before retiring it. The current API boundary is described in [Ownership and API Contract](../Core/OwnershipAndAPI.md), and spell/staff field decisions belong in [Spell+StaffDataStruct.md](../../Spell%2BStaffDataStruct.md).

## Ownership Key

- **Core**: shared runtime/API, independent of authored World of Magic effects.
- **World**: World of Magic authored effects, player-facing workflows, and presentation.
- **Source (future)**: Source-specific model/requirements and the coupled Vesselless, death-interception, and Ingall rescue feature. Deferred from the current migration; see [Source and Resurrection Design](../../Eminent%20Magic:%20Source/Source%20and%20Resurrection%20Design.md).
- **Plugin block**: reusable MCreator Blockly block or code-generation support. The block may call Core; it is not a substitute for a legacy Procedure.
- **Split**: keep generic mechanics in Core and authored effect dispatch/behavior in World.
- **Confirm**: inspect generated code and in-game behavior before assigning final ownership or considering retirement.

## Core Runtime Procedures

| Legacy Procedure | Target | Calls / dependencies | Known host or use | Migration note |
|---|---|---|---|---|
| `GetSpellData` | Core | Legacy spell config/data lookup | Used by `GetManaCost` and `SpellSelector` | Preserve lookup semantics through the finalized typed spell API; replace the old storage access, not its consumers' behavior. |
| `GetSpellExists` | Core | Legacy spell ID/data lookup | No host found in manifest scan | Preserve missing/unknown-spell behavior; determine if callers exist in generated source. |
| `GetSpellName` | Core | `GetCurrentSpellID` (twice) | `OverlaySpellInfo` | Preserve selected-spell name behavior; overlay remains World presentation. |
| `GetCurrentSpellID` | Core | Selected spell / spellbook data | Used by `GetSpellName`; selected spell lookup | Map legacy item/slot selection to the canonical ID contract. |
| `GetSource` | Source (future) | Source API | Legacy consumers; see the Source design | Deferred. The future Source document owns its semantics and call-site decisions. |
| `CalculateManaCost` | Core | Spell definition, control/strength scaling | `GetManaCost`, `SpellSelector`, `ZoltraakSpell` | Keep one authoritative calculation path; specify rounding and invalid-value behavior. |
| `GetManaCost` | Core | `CalculateManaCost`, `GetSpellData` | `OverlaySpellInfo` | Retain returned-cost semantics while switching to the canonical spell API. |
| `ConsumeMana` | Core | Mana state, `NotEnoughMana` damage type | `ActivePerception`, `ManaChannelActiveTickCondition` | Port generic mana/reserve payment only. Source eligibility is deferred; Core must not depend on Source. |
| `GetCurrentMana` | Core | Player mana state | `OverlayManaStats` | Shared read API; presentation stays in World. |
| `GetManaControl` | Core | Player attribute | `ManaSkillsSummaryGUI`, control/cost behavior | Preserve default and absent-attribute handling. |
| `GetManaPerception` | Core | Player attribute | `ManaSkillsSummaryGUI` | Preserve default and absent-attribute handling. |
| `GetManaReserves` | Core | Player attribute | `ManaSkillsSummaryGUI`, mana expenditure | Preserve reserve bounds and expenditure semantics. |
| `GetManaStrength` | Core | Player attribute | `ManaSkillsSummaryGUI`, spell scaling | Preserve default and absent-attribute handling. |
| `GetControlSpellControl` | Core | Player control setting | `OverlaySpellInfo` | Preserve selected-spell/control association. |
| `GetControlSpellStrength` | Core | Player strength setting | `OverlaySpellInfo` | Preserve selected-spell/strength association. |
| `SetControlSpell` | Core | Player control setting, selected spell | Control workflow; no host found | Preserve mutation/validation behavior; identify its user-facing input workflow. |
| `SetStrengthSpell` | Core | Player strength setting, selected spell | Control workflow; no host found | Preserve mutation/validation behavior; identify its user-facing input workflow. |
| `OverrideStats` | Core | Player mana attributes; permission guard via `DoesPlayerHaveOp` | `ManaSkillsSummaryGUI` button callback | Keep server-authoritative and permission-gated. |
| `SpellSelector` | Split | `CalculateManaCost`, `FreezeCoffinSpell`, `GetSpellData`, `ZoltraakSpell` | `CastSelectedSpell`; legacy selected-spell data flow | Keep generic selection/validation/cost flow in Core; remove direct Core-to-World effect references and route through a registered effect boundary. |
| `CastSelectedSpell` | Split | `SpellSelector` | `CrudeStaff` and `CrystalStaff` right-click callbacks | Keep generic cast entry point in Core; preserve item callback behavior and dispatch authored effects through the World integration. |
| `CastSpell` | Split | `FreezeCoffinSpell` and legacy effect dispatch | `ManaChannelEffectExpires`; called from cast flow | Keep generic cast lifecycle in Core and move spell-ID-to-effect mapping out of Core. Defer the Ingall route to Source. |
| `IsHoldingStaff` | Core | Held item and staff identity | `ManaChannelActiveTickCondition`, `OverlaySpellInfo` condition | Replace legacy item checks with the Core staff contract; preserve main/off-hand expectations. |
| `StaffEveryTickWhileUsingItem` | Core / Confirm | Staff use state | No manifest host found | Search generated source and workspace references; preserve if used, otherwise document an explicit retirement. |
| `StaffOnPlayerStoppedUsing` | Core / Confirm | Staff use state, cast lifecycle | No manifest host found | Search generated source and workspace references; preserve if used, otherwise document an explicit retirement. |
| `ActivePerception` | Core | `ConsumeMana`, `RenderMana` | `player_ticks` trigger | Preserve tick timing and mana drain; delegate rendering to the World presentation layer. |
| `ManaChannelActiveTickCondition` | Core | `ConsumeMana`, `IsHoldingStaff` | `ManaChannel` active-tick condition | Preserve eligibility and cost checks. |
| `ManaChannelEffectExpires` | Core | `CastSpell` | `ManaChannel` expiry callback | Preserve expiry cast behavior through the shared dispatcher. |
| `OnEntityChannelingTick` | Core | Channel state | `entity_ticks` trigger | Preserve tick semantics and multiplayer authority. |
| `OnGameBoot` | Core | Shared initialization | `mod_load` trigger | Port only shared initialization; World-specific setup belongs in World. |

## Barrier and Geometry Procedures

| Legacy Procedure | Target | Calls / dependencies | Known host or use | Migration note |
|---|---|---|---|---|
| `GetLocalDimensionBarriers` | Core | Barrier state keyed by dimension | Barrier service; no host found | Preserve dimension isolation and returned shape/ordering if callers rely on it. |
| `RemoveLocalDimensionBarriers` | Core | `GetLocalDimensionBarriers`, barrier state | Barrier service; no host found | Preserve removal scope and cleanup behavior. |
| `BarrierCollisionDetection` | Core | `DotProduct` (six calls), barrier state | `world_ticks` trigger | Preserve collision timing, entity/projectile rules, and server authority. |
| `OnBarrierCollision` | Core | `DotProduct`, `ShatterBarrier` | Barrier collision workflow; no host found | Preserve collision response and shatter conditions. |
| `ShatterBarrier` | Core | Barrier state and shatter effect | `OnBarrierCollision`; no host found | Keep generic barrier destruction in Core; move spell-specific audiovisual effects to World. |
| `DotProduct` | Core | Vector inputs | `BarrierCollisionDetection`, `OnBarrierCollision` | Preserve math behavior and boundary cases. |
| `RotateXYZ` | Core / Confirm | Coordinate/vector inputs | `ZoltraakSpell` | Keep in Core only if it is a stable shared geometry utility; otherwise move beside its World caller. |
| `RenderCylinder` | Core / Confirm | Geometry and render inputs; `RotateXYZ` | `ZoltraakSpell` | Rendering side effects may require World ownership; inspect client-only constraints before placing in Core. |

## World of Magic Effects, UI, and Presentation Procedures

| Legacy Procedure | Target | Calls / dependencies | Known host or use | Migration note |
|---|---|---|---|---|
| `AoeSpellTemplate` | World | Spell effect inputs | `TestingHotkey` key press | Preserve as an authored/test effect or explicitly retire the test hotkey and its procedure together. |
| `FreezeCoffinSpell` | World | Generic cost/cast data; legacy unused `GetSource` lookup | `SpellSelector`, `CastSpell` | Preserve effect behavior; read spell data via the new custom blocks/Core API and remove unused Source lookup so this spell does not depend on the future Source mod. |
| `Ingall` | Source (future) | Future Source API | Legacy cast dispatch | Deferred with the Source feature; see [Source and Resurrection Design](../../Eminent%20Magic:%20Source/Source%20and%20Resurrection%20Design.md). |
| `ZoltraakSpell` | World | `CalculateManaCost`, `ConsumeMana`, geometry helpers | `SpellSelector` | Preserve targeting, generic mana cost, geometry, and visuals using Core APIs. Remove legacy Source lookup from the World migration. |
| `SimpleBarrier` | World | Core barrier creation API | Spell effect; no manifest host found | Preserve authored barrier parameters and effect behavior. |
| `BarrierShatterEffect` | World | Core barrier shatter event/API | No host found | Preserve authored shatter presentation/response; keep generic barrier state changes in Core. |
| `DeathReplacement` | Source (future) | Vesselless state and delayed death | `entity_dies` trigger | Defer with the complete Vesselless lifecycle; this is death interception, not ordinary respawn. |
| `VessellessEffectStartedapplied` | Source (future) | Vesselless state and saved player data | `Vesselless` started callback | Defer with the complete lifecycle; preserve callback behavior for the future Source design. |
| `VessellessOnEffectActiveTick` | Source (future) | Vesselless state and saved position | `Vesselless` active-tick callback | Defer with the complete lifecycle; preserve tick cadence and multiplayer behavior for later design. |
| `VessellessEffectExpires` | Source (future) | Vesselless state and saved player data | `Vesselless` expiry callback | Defer with the complete lifecycle; preserve expiry/removal cleanup for later design. |
| `DoesPlayerHaveOp` | World | Player permission/operator state | `ManaSkillsSummaryGUI` override-button condition | Keep permission checks server-authoritative; replace only if the new permission policy is equivalent. |
| `GetSpellbookSelfSpellName` | World / Confirm | Legacy spellbook item data | `Spellbook`, `SpellBookGui` | Preserve behavior if Spellbook remains; if Grimoire replaces it, document the replacement and retain any still-used lookup behavior. |
| `GetSpellbookSpellName` | World / Confirm | Legacy spellbook slot/item data | `Spellbook`, `SpellBookGui` | Same disposition as the Spellbook workflow; replace old NBT access without silently dropping slot behavior. |
| `SpellbookRightclicked` | World / Confirm | Spellbook/Grimoire and GUI behavior | `Spellbook` right-click callback | Preserve the interaction or record its Grimoire replacement. |
| `OpenSkillsSummaryOnKeyPressed` | World | Skills summary screen | `OpenSkillsSummary` keybind callback | Preserve keybind callback and screen-opening conditions. |
| `ToggleActivePerceptionOnKeyPressed` | World | Active-perception state | `ToggleActivePerception` keybind callback | Preserve keybind callback and toggle behavior. |
| `RenderMana` | World | `DefaultRenderer` | `ActivePerception` | Keep rendering client-side and preserve the mana visualization. |
| `DefaultRenderer` | World / Confirm | Renderer inputs | `RenderMana` | Preserve helper behavior; verify if other callers exist before simplifying. |
| `CapeRenderer` | World / Confirm | Cape/player rendering state | No host/caller found in scanned manifests | Find the renderer registration or generated-code integration before migration; do not treat no manifest caller as proof it is unused. |

## Nested Rendering Utility Procedures

All entries below are `_type: procedure` elements under `Original Mod/elements/renderingUtilities/`. No direct calls or callback hosts were found in the manifest scan, so validate generated-source and runtime references before deciding their owner. Keep them in Core only if at least one cross-content caller needs them; otherwise keep them with World presentation or explicitly retire them.

| Legacy Procedure | Target | Migration note |
|---|---|---|
| `RenderCircleFillPoint` | Core / World / Confirm | Determine callers; preserve fill geometry and render-layer behavior. |
| `RenderCircleOutlinePoint` | Core / World / Confirm | Determine callers; preserve outline geometry and render-layer behavior. |
| `RenderCubeFillPoint` | Core / World / Confirm | Determine callers; preserve fill geometry and render-layer behavior. |
| `RenderCubeOutlinePoint` | Core / World / Confirm | Determine callers; preserve outline geometry and render-layer behavior. |
| `RenderCylinderFillPoint` | Core / World / Confirm | Determine callers; preserve fill geometry and render-layer behavior. |
| `RenderCylinderOutlinePoint` | Core / World / Confirm | Determine callers; preserve outline geometry and render-layer behavior. |
| `RenderDirectionFromYawPitch` | Core / World / Confirm | Determine callers; preserve angle units and direction convention. |
| `RenderNgonPrismFillPoint` | Core / World / Confirm | Determine callers; preserve polygon count and fill geometry. |
| `RenderNgonPrismOutlinePoint` | Core / World / Confirm | Determine callers; preserve polygon count and outline geometry. |
| `RenderRotateByDirection` | Core / World / Confirm | Determine callers; preserve coordinate and direction conventions. |

## Exported Procedure Templates

These `.ptpl` files are Blockly procedure templates, not `.mod.json` Procedure elements. Their useful data-access behavior should migrate into Plugin Blockly blocks; existing call sites and authored Procedures should remain.

| Template | Target | Migration requirement |
|---|---|---|
| `getSpellIDFromItem.ptpl` | Plugin block + Core API | Replace legacy spell-ID/slot NBT lookup with the canonical Grimoire/staff slot API. Preserve sneak/GUI-close behavior where still required. Keep `SpellSelector` or its accepted replacement as a Procedure-level caller. |
| `getSpellProperties.ptpl` | Plugin block + Core API | Replace `/config/eminentmagic/Spells.json` parsing with direct typed access to the finalized spell definition. Preserve supported property semantics and explicitly disposition legacy-only fields. |

## Completion Record

Update this map as each Procedure is implemented. Do not mark a row complete solely because code compiles: confirm the original behavior, trigger/callback wiring, and the applicable player-facing test in [MigrationProgress.md](../../MigrationProgress.md). For intentional retirement, record the replacement or reason and the end-user workflow that supersedes it.