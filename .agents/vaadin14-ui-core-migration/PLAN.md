## Metadata
- **Owner:** iotter-flow team
- **Created:** 2026-01-13
- **Last Updated:** 2026-01-13
- **Agent / Module:** iotter-flow-ui-core/
- **Related Plans:** none

---

## Migrate iotter-flow-ui-core from Vaadin 8 to Vaadin Flow 14
Replace Vaadin 8 APIs with Vaadin Flow 14 equivalents so iotter-flow-ui-core compiles and functions on Flow without relying on Vaadin 8 classes.

---

## Purpose / Big Picture
Enable iotter-flow-ui-core to run on Vaadin Flow 14 so the UI module can modernize its component stack, align with Flow-based navigation, and support updated layouts, data providers, and component APIs.

---

## Context and Orientation
- Module scope: `iotter-flow-ui-core/` (Vaadin 8 UI components targeted for Flow 14 migration).
- Code locations: Java sources under `iotter-flow-ui-core/src/main/java`, resources under `iotter-flow-ui-core/src/main/resources` (confirm actual structure during inventory).
- Migration guidance: `docs/vaadin_8_to_flow_search_replace.md` for mechanical search/replace and `docs/vaadin-platform.md` for component availability and API notes.
- Known gaps: Vaadin 8-only components (Panel, GridLayout, TabSheet, etc.) require manual refactor or add-ons; captions and icons are no longer universal.

---

## Plan of Work
1. Inventory Vaadin 8 usage in `iotter-flow-ui-core`.
   - Use `rg` to list imports and API calls (e.g., `com.vaadin.ui`, `setCaption`, `GridLayout`, `Panel`, `BeanItemContainer`).
   - Deliverable: checklist of files and component types to migrate.
2. Apply mechanical search/replace pass from `docs/vaadin_8_to_flow_search_replace.md`.
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
8. Verify compilation and minimal runtime behavior.
   - Run module build (or full build) to surface remaining compile errors.
   - Update plan with follow-up fixes or test adjustments needed.

---

## Progress
- [x] (2026-01-13) Inventory Vaadin 8 usages in `iotter-flow-ui-core`.
- [x] (2026-01-13) Apply mechanical search/replace pass per `docs/vaadin_8_to_flow_search_replace.md`.
- [x] (2026-01-13) Replace `Window` usage with Flow `Dialog` APIs in `iotter-flow-ui-core`.
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
