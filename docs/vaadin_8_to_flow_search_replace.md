# Vaadin 8 → Vaadin Flow  
## Search & Replace Migration Guide

This document is a **practical search/replace checklist** to accelerate migration from **Vaadin 8** to **Vaadin Flow (Vaadin 10+)**.

It is intentionally **mechanical**:
- Class/package renames  
- API method renames  
- Layout API substitutions  
- Known removals that require manual refactor

Source reference: `vaadin-platform.md`

---

## 1. Package namespace migration

### Global replace (mandatory)

| Search | Replace |
|------|---------|
| `com.vaadin.ui.` | `com.vaadin.flow.component.` |

⚠️ **Important**  
This is a *first pass only*. Many components moved to **sub-packages** and will not compile until refined (see next sections).

---

## 2. Component class replacements (direct)

### Safe search & replace

| Vaadin 8 | Vaadin Flow |
|---------|-------------|
| `Button` | `button.Button` |
| `TextField` | `textfield.TextField` |
| `TextArea` | `textfield.TextArea` |
| `PasswordField` | `textfield.PasswordField` |
| `CheckBox` | `checkbox.Checkbox` |
| `ComboBox` | `combobox.ComboBox` |
| `Grid` | `grid.Grid` |
| `TreeGrid` | `treegrid.TreeGrid` |
| `Upload` | `upload.Upload` |
| `Notification` | `notification.Notification` |
| `ProgressBar` | `progressbar.ProgressBar` |
| `RadioButtonGroup` | `radiobutton.RadioButtonGroup` |
| `CheckBoxGroup` | `checkbox.CheckboxGroup` |
| `ListSelect` | `listbox.ListBox` |
| `Image` | `html.Image` |
| `Label` | `html.Span` *(or `Text`)* |
| `Link` | `html.Anchor` |
| `BrowserFrame` | `html.IFrame` |

Example:
```java
import com.vaadin.ui.Button;
```
→
```java
import com.vaadin.flow.component.button.Button;
```

---

## 3. Layout replacements

### Class-level replacements

| Vaadin 8 | Vaadin Flow |
|--------|-------------|
| `VerticalLayout` | `orderedlayout.VerticalLayout` |
| `HorizontalLayout` | `orderedlayout.HorizontalLayout` |
| `FormLayout` | `formlayout.FormLayout` |
| `CssLayout` | `html.Div` |
| `HorizontalSplitPanel` | `splitlayout.SplitLayout` |
| `VerticalSplitPanel` | `splitlayout.SplitLayout` |

---

## 4. Removed components (manual refactor required)

❌ **DO NOT search/replace blindly**

| Vaadin 8 | Strategy |
|--------|----------|
| `AbsoluteLayout` | `Div` + CSS `position:absolute` |
| `GridLayout` | `FormLayout`, `FlexLayout`, CSS Grid |
| `Panel` | Custom container or add-on |
| `PopupView` | `Button + ContextMenu` |
| `TabSheet` | `Tabs` (manual content handling) |
| `Tree` | Add-on |
| `TwinColSelect` | Custom composite |
| `Slider` | `<input type="range">` |
| `Video` / `Audio` | Native HTML via Element API |
| `Window` | `Dialog` |

---

## 5. UI & navigation

### UI class

| Search | Replace |
|------|---------|
| `extends UI` | *(remove entirely)* |

Flow does **not** require extending `UI`.

---

## 6. Caption & label migration

### Vaadin 8
```java
component.setCaption("Name");
```

### Vaadin Flow

| Search | Replace |
|------|---------|
| `.setCaption(` | `.setLabel(` *(if supported)* |

If unsupported, create explicit labels:
```java
new Span("Name");
```

---

## 7. Layout API method replacements

### Expand / sizing

| Vaadin 8 | Vaadin Flow |
|--------|-------------|
| `setExpandRatio(c, 1)` | `setFlexGrow(1, c)` |
| `expand(c)` | `setFlexGrow(1, c)` |
| `setMargin(true)` | `setPadding(true)` |
| `setSpacing(true)` | `setSpacing(true)` *(same name)* |

⚠️ **Behavior change**  
Do **not** rely on `setSizeFull()` on children. Use **flex-grow** instead.

---

## 8. Alignment API replacements

### HorizontalLayout

| Vaadin 8 | Vaadin Flow |
|--------|-------------|
| `setComponentAlignment(c, Alignment.MIDDLE_CENTER)` | `setVerticalComponentAlignment(Alignment.CENTER, c)` |

### VerticalLayout

| Vaadin 8 | Vaadin Flow |
|--------|-------------|
| `setComponentAlignment(c, Alignment.MIDDLE_CENTER)` | `setHorizontalComponentAlignment(Alignment.CENTER, c)` |

---

## 9. ReadOnly & Enabled audit (no replace)

| API | Notes |
|----|------|
| `setEnabled(false)` | Blocks client-side events |
| `setReadOnly(true)` | No value propagation |

🔎 Audit logic relying on client-side changes while disabled/read-only.

---

## 9bis. Data binding & Containers (REMOVED APIs)

### Removed in Vaadin Flow

| Vaadin 8 API | Status |
|-------------|--------|
| `BeanItemContainer` | ❌ removed |
| `Container` | ❌ removed |
| `BeanFieldGroup` | ❌ removed |

### Search markers (manual refactor required)

```text
BeanItemContainer
Container
BeanFieldGroup
```

Replace with:
- `Grid.setItems(...)`
- `DataProvider`
- `Binder`

---

## 9ter. Selection API changes

### Search & audit

```text
addSelectionListener(
getSelectedRow(
```

Notes:
- `getSelectedRow()` → `getSelectedItems()`
- Selection event types moved to Flow packages

---

## 9quater. Grid Renderers (REMOVED)

### Removed APIs

```text
Renderer
ButtonRenderer
HtmlRenderer
DateRenderer
```

Replace with:
- `addColumn(item -> ...)`
- `addComponentColumn(...)`

---

## 9quinquies. Generated properties

### Vaadin 8
```java
addGeneratedProperty(...)
```

### Flow replacement
```java
grid.addColumn(item -> ...)
```

Search marker:
```text
addGeneratedProperty(
```

---

## 9sexies. Styles & themes

### Mandatory search & replace

| Search | Replace |
|------|---------|
| `addStyleName(` | `addClassName(` |
| `removeStyleName(` | `removeClassName(` |

---

## 9septies. Responsive API (REMOVED)

### Vaadin 8
```java
Responsive.makeResponsive(component);
```

### Flow
❌ removed – use CSS media queries

Search marker:
```text
Responsive.makeResponsive
```

---

## 9octies. Server & Session package migration

### Mandatory search & replace

| Search | Replace |
|------|---------|
| `com.vaadin.server.VaadinSession` | `com.vaadin.flow.server.VaadinSession` |
| `com.vaadin.server.VaadinService` | `com.vaadin.flow.server.VaadinService` |

---

## 9nonies. Icons migration

### Search markers

```text
FontAwesome.
VaadinIcons.
```

Replace with:
```java
VaadinIcon.XYZ.create()
```

---

## 9decies. Component error handling (REMOVED)

### Vaadin 8
```java
setComponentError(new UserError("msg"))
```

### Flow
❌ removed – use `Binder` validation or helper text

Search marker:
```text
setComponentError(
```

---

## 10. Event listeners

Most `addValueChangeListener(...)` calls remain valid.

Ensure imports are from:
```
com.vaadin.flow.component
```

---

## 10bis. DataProvider migration (Backend)

### AbstractBackEndDataProvider correspondence

| Vaadin 8 | Vaadin Flow |
|--------|-------------|
| `com.vaadin.data.provider.AbstractBackEndDataProvider` | `com.vaadin.flow.data.provider.AbstractBackEndDataProvider` |

### Mandatory search & replace

| Search | Replace |
|------|---------|
| `com.vaadin.data.provider.AbstractBackEndDataProvider` | `com.vaadin.flow.data.provider.AbstractBackEndDataProvider` |
| `com.vaadin.data.provider.Query` | `com.vaadin.flow.data.provider.Query` |
| `com.vaadin.data.provider.QuerySortOrder` | `com.vaadin.flow.data.provider.QuerySortOrder` |

### Notes (no automatic replace)

- Method signatures `fetchFromBackEnd(Query<T,F>)` and `sizeInBackEnd(Query<T,F>)` remain the same
- Providers **must be stateless** (no open JPA sessions, no live JDBC connections)
- Refresh methods `refreshAll()` / `refreshItem()` still exist but are UI-thread safe

---

## 11. CustomComponent migration

### Search
```java
extends CustomComponent
```

### Replace with
```java
extends Composite<Div>
```
(or appropriate root component)

---

## 12. CustomLayout migration

| Vaadin 8 | Vaadin Flow |
|--------|-------------|
| `CustomLayout` | `Html` or `PolymerTemplate` |

Manual refactor required.

---

## 13. Recommended migration order

1. Global package replace  
2. Component import fixes  
3. Layout class fixes  
4. API method replacements  
5. Manual refactor of removed components  
6. Visual & behavior audit

---

## 14. Tooling tips

- IntelliJ IDEA: Structural Search & Replace (SSR)  
- CLI: `sed`, `ripgrep`, `perl -pi`  
- Git: commit after each migration phase

---

## Status

Covers **~80% mechanical migration**.  
Remaining effort is **layout semantics and UX parity**.

