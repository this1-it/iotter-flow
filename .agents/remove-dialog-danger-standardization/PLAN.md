## Metadata
- **Owner:** iotter-flow team
- **Created:** 2026-03-27
- **Last Updated:** 2026-03-27
- **Agent / Module:** `iotter-flow-ui-core`, `iotter-flow-ui`
- **Related Plans:** `.agents/confirmation-dialog-flow-migration/PLAN.md`

---

## Standardize destructive listing dialogs on `ConfirmationDialogs`
Refactor the listed listing components so their `openRemove(...)` methods use the same lightweight confirm-dialog pattern as `DeviceModelsListing`, while changing the confirm button text from the generic yes-label to `getI18nLabel("remove_action")` and styling that confirm button as dangerous.

---

## Purpose / Big Picture
Make destructive listing actions feel consistent across the application. When this plan is complete, remove actions in the targeted listings will no longer mix side-drawer delete flows and generic yes/no confirmations. Instead, they will all use a compact Flow confirm dialog with a domain-appropriate destructive action label and a danger-styled confirm button, which makes the user intent clearer and reduces accidental destructive clicks.

---

## Context and Orientation
- Current helper: [ConfirmationDialogs.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/ConfirmationDialogs.java).
- The helper currently centralizes overlay styling and yes/no translations, but it always uses:
  - confirm text = `basic.editor.yes`
  - cancel text = `basic.editor.no`
  - default confirm button styling
- Reference implementation the user wants these listings to resemble:
  [DeviceModelsListing.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/deviceconfigurations/DeviceModelsListing.java)
  `openRemove(DeviceModel item)` already uses the compact `ConfirmationDialogs.open(...)` pattern and refreshes the page after deletion.
- Listed scope:
  - [UsersListing.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/users/UsersListing.java)
  - [GroupWidgetListing.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/groupwidgets/GroupWidgetListing.java)
  - [DevicesListing.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/devices/DevicesListing.java)
  - [NetworkListing.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/networks/NetworkListing.java)
  - [ModbusProfileListing.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/modbusprofiles/ModbusProfileListing.java)
  - [MeasureSensorTypesListing.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/deviceconfigurations/MeasureSensorTypesListing.java)
  - [MeasureUnitTypesListing.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/ui/deviceconfigurations/MeasureUnitTypesListing.java)
- Current state split:
  - Already on compact helper, but with generic yes/no confirm semantics:
    `GroupWidgetListing`, `ModbusProfileListing`, `MeasureSensorTypesListing`, `MeasureUnitTypesListing`.
  - Still using side-drawer delete flows instead of the compact confirm pattern:
    `UsersListing`, `DevicesListing`, `NetworkListing`.
- The requested behavior change is not just caller refactoring. The helper must gain an API for:
  - confirm button text = `getI18nLabel("remove_action")` rather than `basic.editor.yes`
  - confirm button theme = danger/destructive
- `DevicesListing` is a special case because it already uses a compact confirm dialog for reset/delete in `openReset(...)`, while `openRemove(...)` still opens a side drawer with a delete handler. The plan must explicitly separate those two flows and only standardize `openRemove(...)`.

---

## Plan of Work
1. Extend `ConfirmationDialogs` for destructive confirmation flows.
   - Add a dedicated helper overload or named method for destructive actions so callers can specify:
     header text,
     body text or component,
     confirm button text,
     cancel button text,
     confirm button theme.
   - Keep the current yes/no helper behavior intact for non-destructive confirmations already migrated elsewhere.
2. Define the destructive dialog contract.
   - Use `getI18nLabel("remove_action")` as the text shown on the confirm button, not as the body text.
   - Keep the dialog title/body semantics aligned with each listing’s existing remove message strategy.
   - Apply a danger/destructive theme to the confirm button so destructive intent is visible before click.
3. Refactor listings that already use compact confirm dialogs.
   - Update:
     `GroupWidgetListing`,
     `ModbusProfileListing`,
     `MeasureSensorTypesListing`,
     `MeasureUnitTypesListing`.
   - Replace their current generic `ConfirmationDialogs.open(this, ..., getI18nLabel("remove_action"), ...)` calls with the new destructive helper variant so:
     the body remains the intended removal message,
     the confirm button label becomes `remove_action`,
     the confirm button is danger-themed.
4. Refactor listings that still use side-drawer delete flows.
   - Update:
     `UsersListing`,
     `DevicesListing`,
     `NetworkListing`.
   - Replace `openRemove(...)` side-drawer behavior with the same compact destructive confirm-dialog pattern, matching the `DeviceModelsListing` structure:
     null guard,
     destructive confirm,
     service delete/disconnect call,
     `refreshCurrentPage()`,
     error notification handling if needed.
   - Preserve any listing-specific delete service semantics:
     `userService.deleteById(...)`,
     `backendServices.getNetworkService().disconnect(...)`,
     device deletion flow as implemented in `DevicesListing`.
5. Audit menu labels and dialog body text.
   - Confirm action menu entries can remain `getI18nLabel("remove_action")`.
   - Ensure the dialog body text is not accidentally duplicated as both body and confirm button label unless that is explicitly desired per listing.
   - If some listings currently lack a specific remove body message, decide whether to reuse `remove_action` as temporary body copy or introduce a clearer existing i18n key already present in that module.
6. Validate the refactor.
   - Compile with `mvn -pl iotter-flow-ui -am compile`.
   - Manual smoke-check each targeted listing:
     open remove action,
     verify confirm button text reads the translated remove action,
     verify the confirm button is visually dangerous,
     verify the action completes and the listing refreshes correctly.

---

## Progress
- [x] (2026-03-27 10:35 Z) Inventory current `openRemove(...)` implementations and compare them against `DeviceModelsListing`.
- [x] (2026-03-27 10:35 Z) Confirm helper limitation: `ConfirmationDialogs` currently hardcodes yes/no semantics and no danger theme.
- [x] (2026-03-27 10:47 Z) Add destructive-confirm support to `ConfirmationDialogs` via a dedicated `openDanger(...)` helper.
- [x] (2026-03-27 10:47 Z) Refactor the requested listings from side-drawer or generic yes/no remove flows to the standardized destructive confirm pattern.
- [x] (2026-03-27 10:47 Z) Compile impacted modules with `mvn -pl iotter-flow-ui -am compile`.
- [ ] Run targeted manual remove-dialog smoke checks.

---

## Surprises & Discoveries
- Four of the requested listings are already close to the desired shape, but they currently misuse `getI18nLabel("remove_action")` as dialog body text while the button label still comes from `basic.editor.yes`.
- Three requested listings still use side-drawer delete flows, so the change is a UX standardization as much as a code cleanup.
- `DevicesListing` already has a separate compact confirmation flow for reset/delete in `openReset(...)`; that path should not be conflated with the dedicated `openRemove(...)` standardization.
- A dedicated `openDanger(...)` helper was enough; the generic yes/no helpers did not need to change, which keeps the earlier confirmation-dialog migration stable.
- `DeviceModelsListing` was updated alongside the requested scope so the reference pattern now actually matches the new destructive remove behavior.

---

## Decision Log
- **Decision:** Extend `ConfirmationDialogs` rather than inlining `ConfirmDialog` setup into each listing.
- **Rationale:** The button label and danger styling requirements are cross-cutting, and centralizing them prevents yet another round of near-duplicate confirm-dialog code.
- **Date/Author:** 2026-03-27 — Codex

- **Decision:** Keep the existing generic yes/no helper behavior for non-destructive confirmations.
- **Rationale:** Other already-migrated confirmations rely on yes/no semantics; this plan should standardize destructive remove flows without regressing neutral confirmations.
- **Date/Author:** 2026-03-27 — Codex

- **Decision:** Use `remove_action` as confirm button text for the targeted remove dialogs.
- **Rationale:** This matches the user request and makes the destructive action explicit on the confirm affordance instead of forcing the user to translate a generic “Yes” into a delete/removal action.
- **Date/Author:** 2026-03-27 — Codex

---

## Outcomes & Retrospective
- The requested listing remove flows now share a compact destructive confirmation path with explicit `remove_action` confirm text and danger styling.
- `UsersListing`, `DevicesListing`, and `NetworkListing` no longer rely on side-drawer delete UX inside `openRemove(...)`.
- Compile validation passed; manual visual verification is still pending.

---

## Risks / Open Questions
- Some listings may not have ideal remove body copy beyond `remove_action`. If the body becomes too terse once `remove_action` moves to the button, those modules may need clearer existing i18n keys or a follow-up wording pass.
- The exact danger theme token should match the theme already used in the project for destructive Vaadin buttons. If the repo does not already standardize this, the implementation should use the closest Vaadin/Lumo destructive theme supported by the current version.
- Replacing side-drawer delete flows in `UsersListing`, `DevicesListing`, and `NetworkListing` removes any implicit read-only review content that those drawers currently show before deletion. That UX tradeoff should be confirmed during manual review.

---

## Next Steps / Handoff Notes
- Implement the helper enhancement first, then refactor the already-compact listings, then convert the side-drawer remove flows.
- Manual-check the targeted listings and confirm the confirm button renders as destructive and uses the translated remove label instead of the generic yes label.
