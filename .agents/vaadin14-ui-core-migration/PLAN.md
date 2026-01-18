## Metadata
- **Owner:** iotter-flow team
- **Created:** 2026-01-13
- **Last Updated:** 2026-01-18
- **Agent / Module:** iotter-flow-ui-core/, iotter-flow-ui/
- **Related Plans:** none

---

## Migrate Vaadin 8 APIs to Vaadin Flow 14 for UI core + users module
Replace Vaadin 8 APIs with Vaadin Flow 14 equivalents so the shared UI core and Users/GroupWidget UIs compile and function on Flow without legacy UI access patterns.

---

## Purpose / Big Picture
Enable iotter-flow-ui-core and Users/GroupWidget UI screens to run on Vaadin Flow 14 so the UI module can modernize its component stack, align with Flow-based navigation, and support updated layouts, data providers, and component APIs.

---

## Context and Orientation
- Module scope: `iotter-flow-ui-core/` and `iotter-flow-ui/` (Vaadin 8 UI components targeted for Flow 14 migration).
- Code locations: Java sources under `iotter-flow-ui-core/src/main/java` and `iotter-flow-ui/src/main/java`.
- Migration guidance: `docs/vaadin_8_to_flow_search_replace.md` for mechanical search/replace and `docs/vaadin-platform.md` for component availability and API notes.
- Known gaps: Vaadin 8-only components (Panel, GridLayout, TabSheet, etc.) require manual refactor or add-ons; captions and icons are no longer universal.
- Targeted UI files for this phase:
  - `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/users/UsersView.java`
  - `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/users/UsersListing.java`
  - `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/users/UserForm.java`
  - `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/groupwidgets/GroupWidgetAdapterListing.java`
  - Base components to align: `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/AbstractBaseEntityForm.java`, `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/AbstractBaseEntityListing.java`, `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/AbstractBaseEntityDetails.java`

---

## Plan of Work
1. Inventory Vaadin 8 usage in `iotter-flow-ui-core` and the targeted `iotter-flow-ui` files.
   - Use `rg` to list imports and API calls (e.g., `com.vaadin.ui`, `setCaption`, `GridLayout`, `Panel`, `BeanItemContainer`, `TabSheet`, `Window`, `ValoTheme`).
   - Deliverable: checklist of files and component types to migrate, with Users/GroupWidget items highlighted.
2. Apply mechanical search/replace pass from `docs/vaadin_8_to_flow_search_replace.md` where safe.
   - Global package rename to `com.vaadin.flow.component.*` and targeted component subpackages.
   - Update layout classes and common API changes (`setExpandRatio` -> `setFlexGrow`, `addStyleName` -> `addClassName`).
3. Resolve component replacements using `docs/vaadin-platform.md`.
   - Replace direct equivalents (Button, Grid, TextField, etc.).
   - For missing components, decide on alternatives (e.g., Panel -> custom container, TabSheet -> Tabs with manual content handling).
4. Refactor removed APIs and patterns.
   - Replace containers/BeanItemContainer with `DataProvider` and `Grid.setItems`.
   - Replace renderers with `addColumn` / `addComponentColumn`.
   - Update selection APIs (`getSelectedRow` -> `getSelectedItems`).
5. Update custom components and layouts.
   - Convert `extends CustomComponent` to `extends Composite<Div>` (or appropriate root).
   - Replace `CustomLayout` with `Html` or `PolymerTemplate` and update usage sites.
6. Update UI/navigation glue.
   - Remove any `extends UI` patterns and align with Flow navigation patterns.
   - Validate `VaadinSession`/`VaadinService` package changes.
7. Adjust styling, icons, and error handling.
   - Update style names to class names and migrate icons to `VaadinIcon.*.create()`.
   - Replace `setComponentError` with binder validation or helper text.
8. Migrate Users/GroupWidget UI components (Flow + DI + backend grids).
   - `UsersView`: replace `UI.getCurrent()`/`IMainUI` access with injected services and Flow navigation patterns.
   - `UsersListing`: keep backend paging/filtering via `LazyQueryDataProvider` and Flow Grid APIs; replace modal `Window` with a right-side sliding panel for editor/details.
   - `UserForm`: replace Vaadin 8 `TabSheet`/`ValoTheme` usage with Flow equivalents (Tabs or existing Flow add-on) and DI for service access.
   - `GroupWidgetAdapterListing`: replace Vaadin 8 grid/filtering APIs and icons with Flow-compatible equivalents; keep collection-backed display but no UI-context access.
9. Introduce a reusable right-side sliding panel component for edit/view/remove flows.
   - Implement a Flow component named `SideDrawer` that hosts form/details content, with open/close APIs.
   - Ensure it behaves like a modal dialog (modal overlay, close-on-escape, focus trapping).
   - Ensure it supports sizing (width/height).
   - Replace dialog open/close calls in listing/detail flows with `SideDrawer` open/close calls.
10. Verify compilation and minimal runtime behavior.
   - Run module build (or full build) to surface remaining compile errors.
   - Update plan with follow-up fixes or test adjustments needed.

---

## Progress
- [x] (2026-01-13) Inventory Vaadin 8 usages in `iotter-flow-ui-core`.
- [x] (2026-01-13) Apply mechanical search/replace pass per `docs/vaadin_8_to_flow_search_replace.md`.
- [x] (2026-01-13) Replace `Window` usage with a Flow-compatible overlay pattern in `iotter-flow-ui-core`.
- [x] (2026-01-18) Inventory Vaadin 8 usage in Users/GroupWidget files and shared base components.
- [x] (2026-01-18) Update Users/GroupWidget UI classes to Flow APIs and DI (no `UI.getCurrent()` access).
- [x] (2026-01-18) Keep UsersListing backend grids on `LazyQueryDataProvider` with Flow sort/filter integration.
- [ ] (2026-01-13) Resolve component replacements per `docs/vaadin-platform.md`.
- [ ] (2026-01-13) Refactor removed APIs (containers, renderers, selection).
- [ ] (2026-01-13) Migrate custom components/layouts and update navigation glue.
- [ ] (2026-01-13) Update styling/icons/error handling and verify build.

---

## Surprises & Discoveries
- None yet.

---

## Decision Log
- None yet.

---

## Drop-in Replacements
- **Panel:** use `org.vaadin.flow.components.PanelFlow` from `iotter-flow-ui-shim/src/main/java/org/vaadin/flow/components/PanelFlow.java` as a drop-in replacement for Vaadin 8 `Panel` usage in `iotter-flow-ui-core`.
- **GridLayout:** use `org.vaadin.flow.components.GridLayout` from `iotter-flow-ui-shim/src/main/java/org/vaadin/flow/components/GridLayout.java` as a drop-in replacement for Vaadin 8 `GridLayout` usage in `iotter-flow-ui-core`.
- **TabSheet:** use `org.vaadin.flow.components.TabSheet` from `iotter-flow-ui-shim/src/main/java/org/vaadin/flow/components/TabSheet.java` as a drop-in replacement for Vaadin 8 `TabSheet` usage in `iotter-flow-ui-core`.
- **TwinColSelect:** use `org.vaadin.flow.components.TwinColSelectFlow` from `iotter-flow-ui-shim/src/main/java/org/vaadin/flow/components/TwinColSelectFlow.java` as a drop-in replacement for Vaadin 8 `TwinColSelect` usage in `iotter-flow-ui-core`.

---

## Migration Search/Replace Notes
- **Component events:** replace `extends Component.Event` with `extends ComponentEvent<Component>`, add `import com.vaadin.flow.component.ComponentEvent;`, and update constructors from `super(source);` to `super(source, false);`.

---

## Outcomes & Retrospective
- Pending.

---

## Risks / Open Questions
- Which Vaadin 8-only components are used in `iotter-flow-ui-core`, and do we need add-ons vs. custom replacements?
- Are there any UI behaviors relying on LayoutManager or caption/icon patterns that need UX redesign?
- Does the module rely on Vaadin 8 add-ons that lack Flow 14 equivalents?

---

## Next Steps / Handoff Notes
- After inventory, create a per-file migration checklist and start with the simplest views/components first.
