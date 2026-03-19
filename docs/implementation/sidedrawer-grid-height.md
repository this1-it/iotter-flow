# SideDrawer Grid Height Issue

## Problem

A `vaadin-grid` inside a `SideDrawer` renders with zero height or only shows ~12 rows (intrinsic height) instead of filling the available drawer space.

## Root Cause

### 1. `[part="content"]` not filling the overlay

The app-level `side-drawer.css` (themeFor="vaadin-dialog-overlay") set `display:flex; flex-direction:column` on `[part="overlay"]` but had **no rule for `[part="content"]`**. Without `flex:1` on `[part="content"]`, it only sizes to its content, collapsing the entire chain below it.

**Fix** — added to `iotter-flow-ui/frontend/styles/side-drawer.css`:
```css
:host([theme~="side-drawer"]) [part="content"] {
  flex: 1 1 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0;
}

:host([theme~="side-drawer"]) ::slotted(.side-drawer-content) {
  flex: 1 1 0 !important;
  min-height: 0 !important;
  display: flex !important;
  flex-direction: column !important;
  overflow: auto !important;
  padding: var(--lumo-space-m) !important;
}
```

### 2. `height:100%` vs `flex-grow:1` conflict in nested layouts

`height:100%` on a flex child sets its **flex-basis** to the full parent height — not "remaining space after siblings". When multiple siblings exist (upload, header, registers), the item with `height:100%` overflows or collapses.

**The pattern that works:** `height:0 + flex-grow:1` (flex-basis:0, grows into remaining space).

**The pattern that breaks:** `height:100% + flex-grow:1` when siblings also occupy space.

### 3. Grid component nested too deep

Nesting `registers` (ModbusRegisterGrid) inside an intermediate `fieldsLayout` added an extra flex layer. Each `height:100%` in the chain resolved against the wrong parent, eventually reaching a container with 0 computed height.

**Fix:** add `registers` directly to `mainLayout` (the form's root VerticalLayout with `setSizeFull()`), not inside a nested `fieldsLayout`. This removes one `height:100%` resolution step.

## Current Solution (Partial)

`ModbusRegisterGrid` uses a fixed viewport-relative height on the grid:

```java
grid.setHeight("calc(100vh - 220px)");
```

This bypasses the flex chain entirely. `220px` accounts for drawer header + form header row + footer buttons + padding. Adjust if the visual result is wrong.

## Lessons Learned

1. **`[part="content"]` in vaadin-dialog-overlay needs explicit flex properties** when the overlay uses `display:flex`. The default has no `flex:1`.

2. **`::slotted()` is required** to style direct light-DOM children of a shadow DOM slot from inside themeFor CSS. Bare class selectors (`.side-drawer-content`) inside shadow DOM CSS do not reach slotted light-DOM elements.

3. **Global CSS (`@CssImport` without themeFor) can style light-DOM descendants** of slotted elements (e.g., `.side-drawer-content > div`) since they remain in the light DOM.

4. **`height:100%` in flex children is fragile.** Prefer `height:0 + flex-grow:1` when a flex item must fill remaining space alongside siblings. Only use `height:100%` when the item is the sole child or the parent has `overflow:hidden`.

5. **`vaadin-grid` without explicit height** defaults to showing its pageSize rows (~12 visible). Always set an explicit height on grids inside flex containers.

6. **`.draggable` global CSS rule is ineffective** when `setDraggable(false)` — the mixin uses `.draggable` only as a drag-handle marker, not as a wrapper element.

## Files Modified

- `iotter-flow-ui/frontend/styles/side-drawer.css` — added `[part="content"]` and `::slotted(.side-drawer-content)` rules
- `iotter-flow-ui/frontend/styles/side-drawer-global.css` — removed ineffective `.draggable` rule; kept `> div` rule for form root div
- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/modbusprofiles/ModbusProfileForm.java` — moved `registers` to be a direct child of `mainLayout`
- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/modbusregisters/ModbusRegisterGrid.java` — set `grid.setHeight("calc(100vh - 220px)")`
