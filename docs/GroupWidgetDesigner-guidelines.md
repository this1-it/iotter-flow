# GroupWidgetDesigner Refactoring Guidelines (Vaadin 8 → Vaadin Flow with Gridstack.js)


## Introduction


This document defines the architectural guidelines for refactoring `GroupWidgetDesigner` to replace the legacy Vaadin 8 layout engine (IParkingPlace, ResizePanel, absolute positioning) with a modern Gridstack.js-based Web Component integrated into Vaadin Flow.

The goal is to eliminate the server-driven layout engine and move to a client-driven layout model compatible with ThingsBoard-style dashboards.

The refactoring is not a 1:1 migration. It is a paradigm shift from server-calculated layout to client-managed grid layout.

The objective of this refactoring is to implement a dashboard configurator similar to the dashboards provided by ThingsBoard, using Gridstack.js as the underlying layout engine. The solution must be based on a Web Component that integrates Gridstack.js on the client side and is wrapped by a Vaadin Flow server-side component.

The dashboard must support a grid-based layout with draggable and resizable widgets in design mode, and a static read-only mode for visualization. Layout management must be fully client-driven, with the server responsible only for persisting and restoring the layout state as JSON.

By adopting Gridstack.js as a Web Component, the application moves away from legacy server-calculated absolute positioning and towards a modern, responsive, and maintainable dashboard architecture aligned with contemporary web standards.


---

## 1. Architectural Overview

The new architecture introduces three clearly separated layers:

1. Web Component layer (gridstack-designer.js)
2. Vaadin Flow wrapper (GridstackDesignerComponent.java)
3. Domain model (GroupWidget, GraphicWidget)

Gridstack.js must live entirely on the client side. The server must not calculate or manage widget positions.

GroupWidgetDesigner becomes a controller that:
- Instantiates the GridstackDesignerComponent
- Loads and saves layout JSON
- Manages widget creation and deletion
- Coordinates with services
- Does not calculate coordinates

---

## 2. Removal of Legacy Layout Engine

The following concepts must be completely removed from GroupWidgetDesigner:

- IParkingPlace
- ResizePanel
- Absolute positioning
- Pixel-based coordinate calculations
- Canonical width/height calculations
- Percent normalization (x / canvasWidth, y / canvasHeight)
- Manual placeholder stacking logic
- bottomPosition() logic
- changeCanvasSize logic
- Browser resize listeners related to layout
- Scroll management inside WidgetDesigner

These were necessary in Vaadin 8 due to server-driven layout. They are obsolete when using Gridstack.js.

---

## 3. Domain Model Changes

GroupWidget already contains:
@Column(name = "LAYOUT")
private String layout;


This field must become the single source of truth for layout state.

GraphicWidget fields:

- x
- y
- width
- height

These become legacy. They should not be used for layout rendering once Gridstack is adopted. They may remain for backward compatibility during migration, but layout rendering must use only GroupWidget.layout JSON.

---

## 4. Layout Storage Strategy

The layout is stored as Gridstack JSON.

Example conceptual structure:
[
{ "id": "w1", "x": 0, "y": 0, "w": 4, "h": 3 },
{ "id": "w2", "x": 4, "y": 0, "w": 4, "h": 6 }
]


Rules:

- Layout is fully managed client-side.
- Backend stores and retrieves raw JSON.
- Backend does not parse or compute grid coordinates.
- Save operation persists only the JSON string.

---

## 5. New Component: GridstackDesignerComponent

A new Vaadin Flow component must wrap the Gridstack Web Component.

Responsibilities:

- Load JS module via @JsModule
- Initialize Gridstack
- Enable drag and resize
- Provide Java API:
  - setLayout(String json)
  - getLayout()
  - addWidget(String id, Component content)
  - removeWidget(String id)
- Emit layout-changed event to server
- No coordinate calculations server-side

All drag/resize logic must remain in JavaScript.

---

## 6. Refactored GroupWidgetDesigner Responsibilities

GroupWidgetDesigner must:

1. Load the GroupWidget entity.
2. Instantiate GridstackDesignerComponent.
3. If layout JSON exists, pass it to gridstack.
4. If layout JSON is empty:
   - Create default grid layout (optional).
5. Provide toolbar actions:
   - Add widget
   - Modify group
   - Save
   - Cancel
6. On layout-changed event:
   - Update entity.setLayout(json)
7. On Save:
   - Persist entity via groupWidgetService.update(entity)
   - Do not compute or normalize coordinates

GroupWidgetDesigner must not:
- Add placeholder components manually
- Calculate x/y
- Resize components server-side
- Manage scroll offsets

---

## 7. Widget Creation Flow

Previous behavior:
- Calculate bottom position
- Create placeholder
- Add to parking place

New behavior:

1. User clicks Add.
2. Dialog selects GraphicWidgetType.
3. Server creates new GraphicWidget entity.
4. Server generates unique widget ID.
5. Server calls gridstack.addWidget(id, component).
6. Gridstack positions widget automatically.
7. Layout-changed event updates JSON.

No manual positioning logic is required.

---

## 8. Save Behavior

Old behavior:
- Convert pixel coordinates to normalized float
- Persist x, y, width, height

New behavior:
- Persist only:
  - entity.setLayout(json)
  - addedWidgets
  - removedWidgets

No normalization or resizing logic.

---

## 9. Resize Handling

All resize behavior must be handled by Gridstack.js.

Server must not:
- Listen to browser resize events
- Recalculate canvas width/height
- Adjust widget proportions

Gridstack handles responsiveness internally.

---

## 10. Event Handling

Designer must listen to:

- layout-changed
- widget-removed
- widget-added

Upon layout-changed:
- Update entity.layout
- Mark pending changes
- Highlight save button

---

## 11. Backward Compatibility Strategy

Migration plan:

Phase 1:
- Introduce GridstackDesignerComponent.
- Continue storing x/y but ignore for rendering.

Phase 2:
- Migrate existing GroupWidget records:
  - Convert x/y/width/height to grid JSON.
  - Populate layout column.
- Stop using x/y fields.

Phase 3:
- Remove x/y layout logic from Designer entirely.

---

## 12. Visual Differences from Vaadin 8

Old model:
- Server-driven layout
- Absolute positioning
- Pixel-based math
- Manual drag and resize handling

New model:
- Client-driven layout
- Grid-based positioning
- JSON persistence
- No server layout calculations

---

## 13. Final Structural Outcome

After refactoring:

- WidgetDesigner class becomes obsolete.
- IParkingPlace becomes obsolete.
- ResizePanel becomes obsolete.
- GroupWidgetDesigner becomes a pure orchestration layer.
- Layout is fully client-controlled.
- Backend only persists JSON state.
- Designer and Visualizer share the same layout infrastructure.

This architecture is aligned with modern dashboard editors such as ThingsBoard and ensures long-term maintainability within Vaadin Flow.
