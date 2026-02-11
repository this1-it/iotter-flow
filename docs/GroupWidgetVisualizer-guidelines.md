# GroupWidgetVisualizer Refactoring Guidelines (Vaadin 8 → Vaadin Flow with Gridstack.js)


This document defines the architectural guidelines for refactoring GroupWidgetVisualizer to replace the legacy absolute-positioning rendering engine with a Gridstack.js-based layout, aligned with the new GroupWidgetDesigner architecture. The objective is to eliminate server-side layout calculations and use the layout JSON stored in GroupWidget as the single source of truth for widget positioning.

The refactoring is not incremental. It is a structural change from server-driven absolute positioning to client-driven grid rendering.

## Introduction

The objective of this refactoring is to implement a dashboard renderer similar to the dashboards provided by ThingsBoard, using Gridstack.js as the underlying layout engine. The solution must be based on a Web Component that integrates Gridstack.js on the client side and is wrapped by a Vaadin Flow server-side component.

The dashboard must support a grid-based layout with draggable and resizable widgets in design mode, and a static read-only mode for visualization. Layout management must be fully client-driven, with the server responsible only for persisting and restoring the layout state as JSON.

By adopting Gridstack.js as a Web Component, the application moves away from legacy server-calculated absolute positioning and towards a modern, responsive, and maintainable dashboard architecture aligned with contemporary web standards.



## Current Implementation Issues

The current implementation renders widgets using absolute positioning inside a Div container, manually calculating top and left pixel values based on normalized float coordinates stored in GraphicWidget (x, y, width, height). It also computes canvas width and height dynamically and recalculates layout during resize events.

This logic must be completely removed. It tightly couples layout rendering to server-side calculations and makes responsiveness and maintainability difficult.

## Target Architecture

In the new architecture, GroupWidgetVisualizer must use a Gridstack-based viewer component in static mode. The layout JSON stored in the GroupWidget layout column must be passed directly to the client-side Gridstack instance, which will handle rendering and positioning of widgets.

The server must not calculate or normalize pixel coordinates. It must not compute canvas height based on widget positions. It must not sort widgets by Y position for rendering. It must not create absolute-positioned Div wrappers. It must not manage scroll offsets for layout positioning. All of these responsibilities move to Gridstack.

## New Component: GridstackViewerComponent

A new component, GridstackViewerComponent, must be introduced. This component wraps a Gridstack Web Component configured in static mode. Static mode means drag and resize are disabled, and the layout is read-only.

GridstackViewerComponent must provide a minimal Java API that allows:
- Setting the layout JSON
- Registering widget components by identifier
- Clearing and reinitializing the grid if needed

The server side does not need to understand or parse the layout structure; it simply passes the JSON string to the client.

## Rendering Flow

GroupWidgetVisualizer must follow this rendering flow:

During construction or attach, it loads the GroupWidget entity and retrieves the layout JSON from entity.getLayout().

It instantiates GridstackViewerComponent and calls setLayout with the JSON string.

It then iterates over the GraphicWidget entities and creates the corresponding AbstractWidgetVisualizer instances using the existing GraphicWidgetFactory.

Each widget visualizer component is registered inside GridstackViewerComponent using its identifier.

The grid layout determines position and size. No server-side positioning is performed.

## Handling of GraphicWidget Coordinates

GraphicWidget fields x, y, width, and height become legacy for rendering purposes. They must not be used for layout calculation in the Visualizer.

They may remain temporarily for backward compatibility, but rendering must rely exclusively on the layout JSON stored in GroupWidget.

## Toolbar and Business Logic

The toolbar logic in GroupWidgetVisualizer, including:
- Time controls
- Export functionality
- Real-time option handling
- Event bus integration

remains unchanged.

These behaviors are independent of layout and must continue to function as before. Only the layout rendering section is refactored.

## Resize and Responsiveness

The current implementation recalculates canvas size when the browser window changes. This logic must be removed.

Gridstack internally handles width recalculation and responsive behavior. The server does not need to track canonical width or height.

There must be:
- No changeCanvasSize logic
- No redrawMainLayout method
- No manual height recalculation

Rendering becomes declarative: load layout JSON, register widget components, and let Gridstack render the grid.

## Removal of Legacy Dependencies

GroupWidgetVisualizer must not depend on:
- IParkingPlace
- ResizePanel
- Any legacy designer component

These concepts belong to the old layout engine and are incompatible with the new architecture.

## Migration Strategy

During migration, a transitional strategy may be used. If GroupWidget.layout is null or empty, the system may generate a default grid layout based on legacy x and y values.

However, once layout JSON is saved, only JSON must be used.

A one-time migration script can convert existing x, y, width, and height values into Gridstack JSON and store them in the layout column.

After migration, legacy coordinate fields should be ignored for rendering.

## Final Architecture Outcome

After refactoring, the Visualizer becomes significantly simpler. It becomes a composition of:
- A toolbar
- A GridstackViewerComponent

It no longer manages layout mathematics, pixel conversions, normalization factors, or resizing logic.

Designer and Visualizer share the same layout infrastructure:
- Designer operates in editable mode with drag and resize enabled.
- Visualizer operates in static mode with drag and resize disabled.

Both rely on the same layout JSON and the same Gridstack-based rendering engine.

This architecture removes custom layout complexity, eliminates duplicated positioning logic, aligns the application with modern dashboard editors, and ensures long-term maintainability within Vaadin Flow.
