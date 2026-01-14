## Metadata
- **Owner:** iotter-flow team
- **Created:** 2026-01-13
- **Last Updated:** 2026-01-13
- **Agent / Module:** iotter-flow-ui-core/
- **Related Plans:** .agents/vaadin14-ui-core-migration/PLAN.md

---

## Migrate CustomField-based components to AbstractCompositeField
Replace Vaadin 8/Flow CustomField usage with `AbstractCompositeField` for UI field components that still extend `CustomField`, aligning with Flow 14 APIs and value handling requirements.

---

## Purpose / Big Picture
Enable Flow 14 compatibility by moving CustomField-based components to `AbstractCompositeField`, ensuring proper value propagation and component composition without relying on deprecated `CustomField` APIs.

---

## Context and Orientation
- Target module: `iotter-flow-ui-core/` custom field components under `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/fields`.
- Migration rule: replace `com.vaadin.flow.component.CustomField` with `com.vaadin.flow.component.AbstractCompositeField`.
- Already migrated: `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/fields/GeoLocationField.java` (do not rework).
- Needs migration: `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/fields/ChartPlotOptionsField.java`.
- Constraint: do not apply this migration to `CustomField<String>` (or other primitive/boxed base types). These require separate handling or a different field strategy.

---

## Plan of Work
1. Inventory remaining `extends CustomField` classes in `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/fields`.
   - Confirm `GeoLocationField` is excluded and `ChartPlotOptionsField` is in scope.
2. Refactor `ChartPlotOptionsField` to `AbstractCompositeField`.
   - Update class signature, constructor, and value handling to match `AbstractCompositeField` expectations.
   - change super() -> super(null)
   - change doSetValue -> setPresentationValue
   - change VerticalLayout -> org.vaadin.flow.components.GridLayout
   - Ensure value type is not a primitive/boxed type per constraint.


MeasureRangeField
EditableImage
EmbeddedImageData
ChannelSelect
ChannelTreeSelect
FtpAccessField,
EditableResourceData



3. Review dependent code paths.
   - Update any callers or value change listeners that rely on `CustomField` APIs.
4. Validate compilation for the UI core module.
   - Run `mvn -pl iotter-flow-ui-core -am compile` once dependencies are resolvable.

---

## Progress
- [x] (2026-01-13) Inventory remaining `CustomField` usages and confirm target list.
- [x] (2026-01-13) Migrate `ChartPlotOptionsField` to `AbstractCompositeField`.
- [ ] (2026-01-13) Adjust dependent code paths and compile-check.

---

## Surprises & Discoveries
- None yet.

---

## Decision Log
- **Decision:** Skip migration for `CustomField<String>` and other primitive/boxed types; handle separately.
- **Rationale:** `AbstractCompositeField` usage is not valid for those cases and requires a different strategy.
- **Date/Author:** 2026-01-13 — Codex

---

## Outcomes & Retrospective
- Pending.

---

## Risks / Open Questions
- Does `ChartPlotOptionsField` embed value handling that depends on `CustomField` internals or Vaadin 8 Binder behavior?
- Are there additional `CustomField` subclasses outside `common/fields` that need migration?

---

## Next Steps / Handoff Notes
- After refactor, confirm binder/value change behavior in UI flows that use chart plot options.
