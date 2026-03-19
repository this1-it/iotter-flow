# Documentation Index

This index is the canonical map for repository documentation after the Vaadin Flow migration. Use `docs/implementation/` for current guidance. Treat `docs/archive/` as historical material only.

## Status Definitions

- `Aligned with implementation`: current code exists and matches the document at a high level.
- `Needs update`: related code exists, but the document is broader, older, or partially stale.
- `Totally discordant`: historical migration or scratch material; kept only for reference in `docs/archive/`.
- `Not yet implemented`: design documentation without matching code.

## Summary

| Status | Count |
|---|---:|
| Aligned with implementation | 6 |
| Needs update | 1 |
| Totally discordant | 7 |
| Not yet implemented | 0 |

## Aligned With Implementation

| File | Notes |
|---|---|
| `docs/implementation/AbstractBaseEntityForm.md` | Matches `AbstractBaseEntityForm` in `iotter-flow-ui-core` and documents the current Binder-based form pattern. |
| `docs/implementation/AbstractBaseEntityListing.md` | Matches the current listing base class in `iotter-flow-ui-core`. |
| `docs/implementation/GroupWidgetDesigner-guidelines.md` | Gridstack-based designer exists in `it.thisone.iotter.ui.groupwidgets.GroupWidgetDesigner`. |
| `docs/implementation/GroupWidgetVisualizer-guidelines.md` | Gridstack-based visualizer exists in `it.thisone.iotter.ui.groupwidgets.GroupWidgetVisualizer`. |
| `docs/implementation/SPRING_EVENTS_TO_VAADIN_FLOW_UI.md` | Current Spring-to-UI bridge matches `SpringToUIEventBridge`, `UIEventBusInitializer`, and `UIEventBusRegistry`. |
| `docs/implementation/sidedrawer-grid-height.md` | The described SideDrawer and CSS files exist and still document the active layout pattern. |

## Needs Update

| File | Notes |
|---|---|
| `docs/implementation/eventbus-usage.md` | Mostly accurate for the current event bus, but still includes broader migration-era guidance and should be trimmed to current usage examples. |

## Totally Discordant

| File | Notes |
|---|---|
| `docs/archive/chartjs-vaadin14-fixes.md` | References webpack-era Vaadin 14 behavior; the repo now targets Vaadin 24 and uses Vite-era frontend tooling. |
| `docs/archive/component_sizing.md` | Scratch note without a maintained structure; superseded by the SideDrawer height troubleshooting doc. |
| `docs/archive/eventbus_vaadin_init_listener.md` | Ad hoc explainer note, not a maintained repository document. |
| `docs/archive/uirunnable_future_to_vaadin_flow_migration.md` | Historical migration guidance for removed Vaadin 8 patterns. |
| `docs/archive/vaadin-platform.md` | Generic Vaadin 8 to Flow mapping reference, not current repo documentation. |
| `docs/archive/vaadin8-to-flow-data-compatibility.md` | Historical compatibility matrix for migration work, not current implementation guidance. |
| `docs/archive/vaadin_8_to_flow_search_replace.md` | Mechanical migration checklist from Vaadin 8, preserved only as history. |
