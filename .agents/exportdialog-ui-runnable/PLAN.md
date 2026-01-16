# ExportDialog UIRunnable Migration

## Metadata
- **Owner:** bedinsky
- **Created:** 2026-01-16
- **Last Updated:** 2026-01-16
- **Agent / Module:** `iotter-flow-ui-core` export UI
- **Related Plans:** `.agents/vaadin14-ui-core-migration/PLAN.md`

## Replace UIRunnable-based export kickoff with Flow async
Migrate `ExportDialog#createExportListener` and its background export flow to Vaadin Flow concurrency using `CompletableFuture` + `UI.access`, removing `UIRunnable` usage. Use an `Executor` provided via constructor (from Spring context).

## Purpose / Big Picture
Stop using legacy Vaadin 8 `UIRunnable`/`UIRunnableFuture` patterns and align export execution with Flow idioms, while keeping the same UX: export runs in background, completion opens a dialog with a download button and errors are shown as notifications.

## Context and Orientation
- **Current flow:** `ExportDialog#createExportListener` creates `ExportStartEvent`, instantiates `ExportUIRunnable`, and executes via `IMainUI.getUIExecutor().executeAndAccess(...)`.
- **Legacy class:** `ExportUIRunnable` implements `UIRunnable` with `runInBackground()` and `runInUI(Throwable ex)`; uses Vaadin 8 `FileDownloader` and `StreamResource` API.
- **Migration guide:** `docs/uirunnable_future_to_vaadin_flow_migration.md` is a drop-in recipe for replacing `UIRunnable` with `CompletableFuture` + `UI.access`.
- **Key files:**
  - `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/export/ExportDialog.java`
  - `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/export/ExportUIRunnable.java`
  - `docs/uirunnable_future_to_vaadin_flow_migration.md`
  - `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/export/EnhancedFileDownloader.java` (Flow download helper)
  - `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/export/Exporter.java` (Flow resource usage pattern)

## Plan of Work
1. **Audit current executor + UI access**
   - Locate `IMainUI` and any `getUIExecutor()` contract to ensure we can replace it with a constructor-injected `Executor` from Spring.
   - Confirm that `ExportUIRunnable` is only used by `ExportDialog` and can be removed or replaced.
2. **Replace `UIRunnable` kickoff in `createExportListener`**
   - Use `CompletableFuture.runAsync(...)` on the constructor-injected `Executor` and `whenComplete((v, ex) -> ui.access(...))` per the migration doc.
   - Move the background export logic from `ExportUIRunnable#runInBackground` into the async lambda.
   - Move UI logic from `ExportUIRunnable#runInUI` into the `ui.access` block, including download dialog creation and error handling.
3. **Update download UI to Flow APIs**
   - Replace Vaadin 8 `FileDownloader` usage with Flow-compatible download (e.g., `EnhancedFileDownloader` or direct `StreamResource` with click handler).
   - Ensure StreamResource headers and content type are set as in existing Flow helpers.
4. **Remove or repurpose `ExportUIRunnable`**
   - Delete the class if unused after migration; remove related imports and `UIRunnable` dependencies.
   - If reusable logic remains, extract minimal helper methods in `ExportDialog` or a small utility class (keep surface area small).
5. **Validate behavior and update UI strings**
   - Verify validation errors, locked export handling, and failure notifications remain consistent.
   - Ensure dialog still displays download button with correct filename.

## Progress
- [x] (2026-01-16 16:41 Z) Locate `IMainUI` executor usage and current export flow dependencies.
- [x] (2026-01-16 16:41 Z) Replace `UIRunnable` path with `CompletableFuture` + `UI.access` in `ExportDialog`.
- [x] (2026-01-16 16:41 Z) Update download dialog logic to Flow-native APIs.
- [ ] (2026-01-16 00:00 Z) Remove `ExportUIRunnable` and clean imports/usages.
- [ ] (2026-01-16 00:00 Z) Manual smoke check: start export, see completion dialog, and download file.

## Surprises & Discoveries
- None yet.

## Decision Log
- **Decision:** Use the repository’s migration recipe (`docs/uirunnable_future_to_vaadin_flow_migration.md`) as the authoritative pattern for Flow async + UI synchronization.
- **Rationale:** Ensures consistent migration with other features and removes legacy Vaadin 8 concurrency constructs.
- **Date/Author:** 2026-01-16 — Codex

## Risks / Open Questions
- Confirm the constructor-injected `Executor` lifetime and error handling expectations from Spring (e.g., shared pool vs. dedicated export executor).
- Are there other callers of `ExportUIRunnable` outside `ExportDialog` that must be migrated simultaneously?

## Next Steps / Handoff Notes
- Once the executor strategy is confirmed, proceed with the concrete code changes and run a module compile.
