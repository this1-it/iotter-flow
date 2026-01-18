## Metadata
- **Owner:** iotter-flow team
- **Created:** 2026-01-18
- **Last Updated:** 2026-01-18
- **Agent / Module:** iotter-flow-ui/, iotter-flow-ui-core/
- **Related Plans:** .agents/vaadin14-ui-core-migration/PLAN.md

---

## Migrate Users + GroupWidget listings to Vaadin Flow with DI and LazyQueryDataProvider
Refactor the Users UI and shared base listing/form/detail components to Flow patterns (DI, UI-scoped state) and keep server-backed grids via LazyQueryDataProvider.

---

## Purpose / Big Picture
Eliminate Vaadin 8 APIs and UI/Spring-context access patterns in the users/group widgets area, while preserving backend-driven listing behavior and making the code compile and behave correctly under Vaadin Flow.

---

## Context and Orientation
- Target files:
  - `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/users/UsersView.java`
  - `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/users/UsersListing.java`
  - `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/users/UserForm.java`
  - `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/groupwidgets/GroupWidgetAdapterListing.java`
- Shared base classes with the same migration needs:
  - `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/AbstractBaseEntityForm.java`
  - `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/AbstractBaseEntityListing.java`
  - `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/AbstractBaseEntityDetails.java`
- Current issues:
  - Vaadin 8 imports (`com.vaadin.ui.*`, `com.vaadin.navigator.*`, `com.vaadin.icons.*`, `ValoTheme`, `Window`, `TabSheet`).
  - Legacy UI access via `UI.getCurrent()` and `IMainUI`/`UIUtils` helpers.
  - Dialogs and component APIs need Flow equivalents.
  - Listing must remain backend-driven (no in-memory data provider); use `LazyQueryDataProvider` for grids.
- Constraints:
  - No manual Spring context access in components; use constructor injection or `@Autowired` + UI-scoped state.
  - Avoid `((IMainUI) UI.getCurrent())` patterns; replace with injected services or Flow component `getTranslation()` where available.

---

## Plan of Work
1. Inventory and map Vaadin 8 usage in the target files.
   - Identify `com.vaadin.ui.*` imports, `Window`, `TabSheet`, `ValoTheme`, and `UI.getCurrent()` usage.
   - List where `UIUtils`/`IMainUI` is used to fetch services and user details.
2. Define DI surface and state handoff for Users UI.
   - Decide injected services (UserService, UserRepository, NetworkService, GroupWidgetService, etc.).
   - Decide where user context comes from (e.g., injected `UserDetailsAdapter` or a session-scoped provider).
   - Confirm how `UsersView` obtains `UsersListing` (Flow navigation + Spring @Route + injection, not UI lookup).
3. Migrate `AbstractBaseEntityListing` and `AbstractBaseEntityForm` to Flow-safe patterns.
   - Ensure dialogs are Flow `Dialog` and are opened via `open()`; no `UI.addWindow`.
   - Keep `LazyQueryDataProvider` usage for backend listings and ensure filters are passed via `ConfigurableFilterDataProvider`.
   - Confirm event/listener wiring uses Flow APIs.
4. Migrate `UsersListing` to Flow grid and backend data provider.
   - Replace Vaadin 8 Grid APIs with Flow equivalents.
   - Keep `LazyQueryDataProvider` for backend paging/sorting; ensure `QuerySortOrder` mapping uses Flow classes.
   - Replace filtering UI with Flow components (header row, value change mode) without in-memory provider usage.
5. Migrate `UserForm` to Flow components and DI.
   - Replace `TabSheet` with Flow `Tabs` + `Tab` + content panels, or Flow TabSheet add-on if already used elsewhere.
   - Replace `ValoTheme` styles with Flow equivalents or CSS classes.
   - Replace service lookups with injected services; avoid `UIUtils.getServiceFactory()`.
6. Migrate `GroupWidgetAdapterListing` to Flow-only components.
   - Replace Vaadin 8 GridUtil/filters with Flow-compatible filtering (custom header filters or existing add-on).
   - Replace `VaadinIcons` with `VaadinIcon` (Flow) or icon components.
7. Validate interactions and update tests (if any).
   - Ensure add/modify/view/remove actions open dialogs and persist via services.
   - Check filtering and sorting with backend queries.

---

## Progress
- [ ] (2026-01-18) Inventory Vaadin 8 usage in target files and base classes.
- [ ] (2026-01-18) Define DI surface for UsersView/UsersListing/UserForm and replace UI access patterns.
- [ ] (2026-01-18) Update AbstractBaseEntity* base classes to Flow dialog/open patterns.
- [ ] (2026-01-18) Migrate UsersListing grid to Flow APIs with LazyQueryDataProvider.
- [ ] (2026-01-18) Migrate UserForm tabs/layouts to Flow equivalents and injected services.
- [ ] (2026-01-18) Migrate GroupWidgetAdapterListing grid/filtering to Flow equivalents.
- [ ] (2026-01-18) Compile check for iotter-flow-ui and iotter-flow-ui-core.

---

## Surprises & Discoveries
- None yet.

---

## Decision Log
- **Decision:** Keep backend-driven listing via `LazyQueryDataProvider` (no in-memory providers).
- **Rationale:** Preserves server-side paging/filtering behavior and existing repository queries.
- **Date/Author:** 2026-01-18 — Codex

---

## Outcomes & Retrospective
- Pending.

---

## Risks / Open Questions
- Confirm preferred mechanism for user/session context in Flow (injectable provider vs. UI-scoped bean).
- Confirm whether a Flow TabSheet add-on is already in use or if Tabs + content panels are desired.
- Identify a Flow-compatible grid filtering utility to replace `org.vaadin.gridutil` or implement custom header filters.

---

## Next Steps / Handoff Notes
- After plan approval, implement migrations in the order listed, keeping UI behaviors intact.
- Run `mvn -pl iotter-flow-ui -DskipTests compile` (or preferred build command) to surface remaining Flow errors.
