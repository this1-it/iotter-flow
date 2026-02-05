## Metadata
- **Owner:** iotter-flow team
- **Created:** 2026-02-04
- **Last Updated:** 2026-02-04
- **Agent / Module:** iotter-flow-ui/src/main/java/it/thisone/iotter/ui/channels
- **Related Plans:** .agents/users-ui-flow-migration/PLAN.md

---

## Migrate channels package to Vaadin Flow APIs and DI-safe patterns
Replace Vaadin 8 UI APIs and legacy UIUtils/IMainUI access in channels package with Flow-compatible component APIs and explicit wiring markers.

---

## Purpose / Big Picture
Move `ui/channels` classes to Flow-style components and remove direct dependency on legacy UI access patterns that are invalid in Flow.

---

## Context and Orientation
- Target package currently mixes Vaadin 8 APIs (`Window`, `setCaption`, `ValoTheme`, `HtmlRenderer`) and removed patterns (`UIUtils.localize/getServiceFactory/getUserDetails`, `IMainUI`).
- Several referenced classes are currently missing from this worktree snapshot (e.g. chart adapter/details classes), so migration is done as a mechanical pass with explicit manual-refactor boundaries.

---

## Plan of Work
1. Convert UI imports and common APIs to Flow equivalents in `ui/channels`.
2. Replace legacy style/layout APIs (`addStyleName`, `setMargin`, `setExpandRatio`, `addComponent`) with Flow calls.
3. Replace legacy dialog usage (`Window`, `UI.addWindow`) with `Dialog` and `open()`.
4. Flag manual boundaries for removed patterns (`UIUtils.getServiceFactory`, `UIUtils.localize`, `UIUtils.getUserDetails`, `IMainUI`).
5. Attempt module compile to surface residual blockers and report unresolved items.

---

## Progress
- [x] (2026-02-04 11:30 Z) Applied mechanical import/API/layout/dialog migration pass across `ui/channels`.
- [x] (2026-02-04 11:36 Z) Recorded unresolved Flow migration boundaries and anti-patterns in report.
- [x] (2026-02-04 11:40 Z) Ran `mvn -pl iotter-flow-ui -DskipTests compile` to collect current compile blockers.

---

## Surprises & Discoveries
- Compile currently fails broadly outside `ui/channels` due many remaining Vaadin 8 classes in `ui/devices` and `ui/charts`.
- `ui/channels` references classes/services absent in this snapshot, requiring follow-up refactor before successful compilation.

---

## Decision Log
- **Decision:** Continue with mechanical migration despite untracked package state.
- **Rationale:** User explicitly approved migrating untracked files in place.
- **Date/Author:** 2026-02-04 — Codex

---

## Risks / Open Questions
- `UIUtils` and `IMainUI` usage cannot remain; these require service injection and view/component wiring decisions.
- Missing/legacy dependencies in neighboring packages block compile validation for channels-only work.

---

## Next Steps / Handoff Notes
- Refactor channels constructors to inject needed services (`DeviceService`, `MqttService`, event bus, auth/user context).
- Migrate dependent `ui/devices` and `ui/charts` classes to Flow to unblock compile/test cycle.
