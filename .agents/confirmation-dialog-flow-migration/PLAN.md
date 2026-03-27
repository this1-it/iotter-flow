## Metadata
- **Owner:** iotter-flow team
- **Created:** 2026-03-27
- **Last Updated:** 2026-03-27
- **Agent / Module:** `iotter-flow-ui-core`, `iotter-flow-ui`
- **Related Plans:** `.agents/vaadin14-ui-core-migration/PLAN.md`

---

## Replace custom `ConfirmationDialog` with Flow `ConfirmDialog`
Replace active usages of `it.thisone.iotter.ui.common.ConfirmationDialog` with `com.vaadin.flow.component.confirmdialog.ConfirmDialog`, removing the custom `Callback` interface from call sites while preserving the old component as deprecated compatibility code and keeping the current translated yes/no confirmation UX.

---

## Purpose / Big Picture
Stop carrying a custom confirmation component now that Flow provides a first-class confirmation dialog with the APIs this codebase needs, including component-backed body content. The finished state is that every confirmation flow uses the standard Flow dialog, all boolean callbacks are expressed via native listeners or narrow local lambdas, and the shared button labels still come from the existing `basic.editor.*` translations.

---

## Context and Orientation
- Current custom component: [iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/ConfirmationDialog.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/ConfirmationDialog.java).
- The custom class is a thin `Dialog` wrapper with two buttons:
  `basic.editor.yes` triggers `Callback#onDialogResult(true)` and `basic.editor.no` just closes the dialog.
- Existing translation keys already match the requested basic wording in [messages_en.properties](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/resources/messages_en.properties), with corresponding keys also present in `messages_it.properties`, `messages_de.properties`, `messages_fr.properties`, and `messages_es.properties`.
- Flow `ConfirmDialog` is available in the local dependency cache and supports both `setText(String)` and `setText(Component)`, so string- and component-backed confirmations can migrate directly.
- Current usages span 19 files:
  - Plain text confirmations: base editor cancel/lose changes, most remove/delete actions, provisioning confirmations.
  - Component-body confirmations: `ChannelListing`, `DevicesListing`, `NetworkGroupUsers`.
  - Rich formatted body content: `QuickCommandButton` builds HTML-like warning content with `MarkupsUtils`; this path needs the most careful conversion because `ConfirmDialog` text should be given as a Flow component when markup is intentional.
- Main caller inventory:
  - [BaseEditor.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/BaseEditor.java)
  - [ChannelListing.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/channels/ChannelListing.java)
  - [DeviceModelsListing.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/deviceconfigurations/DeviceModelsListing.java)
  - [MeasureSensorTypesListing.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/deviceconfigurations/MeasureSensorTypesListing.java)
  - [MeasureUnitTypesListing.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/deviceconfigurations/MeasureUnitTypesListing.java)
  - [DevicesListing.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/devices/DevicesListing.java)
  - [GraphicFeedListing.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/graphicfeeds/GraphicFeedListing.java)
  - [GraphicWidgetPlaceHolder.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/graphicwidgets/GraphicWidgetPlaceHolder.java)
  - [GroupWidgetListing.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/groupwidgets/GroupWidgetListing.java)
  - [GroupWidgetsCustomMap.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/maps/GroupWidgetsCustomMap.java)
  - [ModbusProfileListing.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/modbusprofiles/ModbusProfileListing.java)
  - [ModbusRegisterListing.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/modbusregisters/ModbusRegisterListing.java)
  - [NetworkGroupDevices.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/networkgroups/NetworkGroupDevices.java)
  - [NetworkGroupUsers.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/networkgroups/NetworkGroupUsers.java)
  - [NetworkDevices.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/networks/NetworkDevices.java)
  - [ModbusTemplatesStep.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/provisioning/ModbusTemplatesStep.java)
  - [ControlPanelBaseAdapter.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/visualizers/ControlPanelBaseAdapter.java)
  - [QuickAlarmInfo.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/visualizers/controlpanel/QuickAlarmInfo.java)
  - [QuickCommandButton.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/visualizers/controlpanel/QuickCommandButton.java)
- This is a practical refactor, not a TDD-first change. The plan should favor compile verification plus targeted manual smoke checks because many confirmation paths are UI-event driven and spread across listings/details screens.

---

## Plan of Work
1. Inventory and classify all `ConfirmationDialog` usages.
   - Confirm there are no remaining call sites outside `iotter-flow-ui-core` and `iotter-flow-ui`.
   - Group usages into plain text body, component body, and rich formatted body.
   - Record any public helper signatures that currently expose `ConfirmationDialog.Callback`.
2. Define the migration pattern for standard yes/no dialogs.
   - Instantiate `ConfirmDialog` directly at call sites or through a very small local helper if repeated setup becomes noisy.
   - Set header/body through `setHeader(...)` and `setText(...)`.
   - Map translations as:
     `basic.editor.yes` -> confirm button,
     `basic.editor.no` -> cancel button.
   - Explicitly enable cancelability and keep the current behavior that only confirm performs the action.
3. Refactor callback consumers away from `ConfirmationDialog.Callback`.
   - For inline one-off usages, replace the boolean callback with `addConfirmListener(...)` and keep the guarded action in the confirm handler.
   - For reusable helper methods such as reset-alarm flows in `QuickCommandButton`, replace `Callback` parameters with a narrower type suited to the remaining contract, likely `ComponentEventListener<ConfirmDialog.ConfirmEvent>` or `Runnable`/`SerializableRunnable` when only positive confirmation matters.
   - Remove now-unused imports and type aliases from all caller classes.
4. Migrate simple string-based confirmations.
   - Update listings and editor flows that only pass string captions/messages.
   - Preserve the existing translated captions/messages, especially:
     `basic.editor.forget_changes`,
     `basic.editor.pending_changes`,
     `basic.editor.remove`,
     `basic.editor.are_you_sure`.
5. Migrate component-backed confirmations.
   - Replace constructor calls that pass `Span`, `FormLayout`, `VerticalLayout`, or other Flow components with `ConfirmDialog#setText(Component)`.
   - Re-check layout sizing and alignment in `ChannelListing`, `DevicesListing`, and `NetworkGroupUsers`.
6. Migrate rich formatted confirmation content.
   - Review `QuickCommandButton` and any other path that currently passes HTML-like strings assembled with `MarkupsUtils`.
   - Convert intentional markup into Flow components instead of relying on string HTML being rendered.
   - Verify the warning emphasis, alarm list readability, and button ordering remain acceptable after the move.
7. Deprecate the custom component.
   - Update [ConfirmationDialog.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/ConfirmationDialog.java) with `@Deprecated` and short Javadoc pointing new code to Flow `ConfirmDialog`.
   - Keep the class compiling for compatibility during the migration window, but ensure no production caller still instantiates it.
   - Run a final search for `new ConfirmationDialog`, `ConfirmationDialog.Callback`, and `onDialogResult(` to ensure the custom API is no longer used by active code paths.
8. Validate the refactor.
   - Compile at least the impacted UI modules with `mvn -pl iotter-flow-ui -am compile`.
   - Perform manual smoke checks for:
     editor cancel with pending changes,
     remove/delete actions from listings,
     reset alarms / quick command confirmation,
     component-based dialogs that show form-like content.

---

## Progress
- [x] (2026-03-27 08:44 Z) Inventory current `ConfirmationDialog` implementation, translations, and all direct call sites.
- [x] (2026-03-27 08:44 Z) Verify local Flow `ConfirmDialog` API supports both string and component body content.
- [x] (2026-03-27 09:06 Z) Replace all call sites with Flow `ConfirmDialog`/`ConfirmationDialogs` and remove `ConfirmationDialog.Callback` from active callers.
- [x] (2026-03-27 09:06 Z) Deprecate the custom `ConfirmationDialog` class and leave it compiling as compatibility code.
- [ ] Perform targeted manual confirmation-dialog smoke checks.

---

## Surprises & Discoveries
- The codebase already has the requested English base translations in `messages_en.properties`; no new English keys are needed for this migration.
- The risky path is not the standard remove dialogs but the alarm-reset dialog in `QuickCommandButton`, because it currently builds formatted content via HTML-like strings and will need explicit Flow components for safe rendering.
- `ConfirmDialog` in the locally cached Vaadin dependency supports `setText(Component)`, so component-body dialogs do not require a fallback wrapper just to host Flow components.
- A small `ConfirmationDialogs` helper keeps the Flow `ConfirmDialog` setup centralized, which made the 19 caller migration mechanical while preserving the requested `basic.editor.yes` / `basic.editor.no` translations.
- `mvn -pl iotter-flow-ui -am compile` succeeded after the refactor on 2026-03-27 09:06 Z.

---

## Decision Log
- **Decision:** Remove the custom boolean `Callback` contract from active call sites instead of recreating it around `ConfirmDialog`, but keep the legacy dialog class as deprecated compatibility code.
- **Rationale:** The only meaningful action is the positive confirm path; Flow already models confirm/cancel explicitly, and keeping a boolean callback would preserve the wrong abstraction. Deprecating rather than deleting lowers migration risk and leaves a temporary compatibility seam.
- **Date/Author:** 2026-03-27 — Codex

- **Decision:** Keep button texts on the existing `basic.editor.yes` / `basic.editor.no` translations unless a specific screen already uses domain-specific wording.
- **Rationale:** This preserves current behavior and matches the custom dialog’s semantics while standardizing implementation on Flow.
- **Date/Author:** 2026-03-27 — Codex

- **Decision:** Treat this as a refactor with compile + smoke verification, not full TDD.
- **Rationale:** The affected behavior is highly UI-event-oriented and spread across many screens; the fastest credible validation is a compile pass plus targeted manual interaction checks.
- **Date/Author:** 2026-03-27 — Codex

---

## Outcomes & Retrospective
- Active callers now use Flow `ConfirmDialog` through a shared helper, and the old custom component remains available only as deprecated compatibility code.
- Compile validation passed for `iotter-flow-ui-core` and `iotter-flow-ui`.
- Manual UI smoke checks are still pending.

---

## Risks / Open Questions
- Should destructive actions keep the current yes/no wording everywhere, or should some screens switch to `basic.editor.confirm` / `basic.editor.cancel` for stronger consistency with Flow defaults? The current request points to preserving the existing basic translation set, so the default plan keeps yes/no semantics.
- Are any tests or UI helpers outside the direct `rg` results instantiating the custom dialog reflectively or through factories?
- Do any component-body dialogs depend on the old custom dialog’s exact padding/sizing enough that small layout regressions need dedicated CSS adjustments?

---

## Next Steps / Handoff Notes
- Implement the migration in one batch, starting with the shared helper signatures in `QuickCommandButton` and `BaseEditor`, then sweep the simple listing call sites, then handle the component/rich-content cases.
- Once callers are migrated, mark the old dialog as deprecated rather than removing it, and leave final deletion for a later cleanup pass if the team wants one.
- Run manual smoke checks for pending-changes cancel, listing removals, device reset/delete, and reset alarms dialog rendering.
