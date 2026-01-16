```markdown
# Components in Vaadin platform (Vaadin 8 → Vaadin Flow )

This document summarizes how Vaadin 8 components map to their counterparts in Vaadin Flow (Vaadin 10+ / Flow), and highlights key component and layout API differences. 

---

## Component set

All components have been rebuilt based on Web Components, but some Vaadin 8 components do **not** have a direct server-side Java replacement (yet) and some replacements are **not** 1:1 feature-compatible. 

**Notes**
- The listed replacements are available starting from **Vaadin 10**, or from the specifically mentioned platform version. 
- If you’re missing a component, look for third-party add-ons in Vaadin Directory or alternatives there; you can also contact Vaadin for help. 

---

## Vaadin 8 component → Vaadin Flow replacement matrix

> Columns: **V8 component** → **Vaadin Flow replacement** → **Details / notes**

| Vaadin 8 component (full class name) | Vaadin Flow component (full class name) | Notes |
|-------------------------------------|-----------------------------------------|-------|
| com.vaadin.ui.AbsoluteLayout | — | Use com.vaadin.flow.component.html.Div with CSS absolute positioning |
| com.vaadin.ui.Accordion | com.vaadin.flow.component.accordion.Accordion | Available since Vaadin 14 |
| com.vaadin.ui.Audio | — | Use native HTML <audio> via Element API |
| com.vaadin.ui.Button | com.vaadin.flow.component.button.Button | Direct replacement |
| com.vaadin.ui.BrowserFrame | com.vaadin.flow.component.html.IFrame | Available since Vaadin 13 |
| com.vaadin.ui.CheckBox | com.vaadin.flow.component.checkbox.Checkbox | Direct replacement |
| com.vaadin.ui.CheckBoxGroup | com.vaadin.flow.component.checkbox.CheckboxGroup | Available since Vaadin 12 |
| com.vaadin.ui.ColorPicker | — | Use HTML <input type="color"> |
| com.vaadin.ui.ComboBox | com.vaadin.flow.component.combobox.ComboBox | Direct replacement |
| com.vaadin.ui.ContextMenu (add-on) | com.vaadin.flow.component.contextmenu.ContextMenu | Available since Vaadin 12 |
| com.vaadin.ui.CssLayout | com.vaadin.flow.component.html.Div | FlexLayout optional alternative |
| com.vaadin.ui.CustomComponent | com.vaadin.flow.component.Composite | Composition-based |
| com.vaadin.ui.CustomField | com.vaadin.flow.component.AbstractField / AbstractCompositeField / AbstractSinglePropertyField | Depends on value model |
| com.vaadin.ui.CustomLayout | com.vaadin.flow.component.Html / com.vaadin.flow.component.polymertemplate.PolymerTemplate | Static vs dynamic templates |
| com.vaadin.ui.DateField | com.vaadin.flow.component.datepicker.DatePicker | Direct conceptual replacement |
| com.vaadin.ui.DateTimeField | com.vaadin.flow.component.datetimepicker.DateTimePicker | Available since Vaadin 14.2 |
| com.vaadin.ui.Embedded | — | Use @Tag("object") with Element API |
| com.vaadin.ui.FormLayout | com.vaadin.flow.component.formlayout.FormLayout | Responsive, multi-column |
| com.vaadin.ui.Grid | com.vaadin.flow.component.grid.Grid | New data & rendering API |
| com.vaadin.ui.GridLayout | — | Use FormLayout, FlexLayout, or CSS Grid |
| com.vaadin.ui.HorizontalLayout | com.vaadin.flow.component.orderedlayout.HorizontalLayout | Flexbox-based |
| com.vaadin.ui.HorizontalSplitPanel | com.vaadin.flow.component.splitlayout.SplitLayout | Orientation configurable |
| com.vaadin.ui.Image | com.vaadin.flow.component.html.Image | Direct replacement |
| com.vaadin.ui.InlineDateField | — | No core replacement |
| com.vaadin.ui.InlineDateTimeField | — | No core replacement |
| com.vaadin.ui.Label | com.vaadin.flow.component.Text / com.vaadin.flow.component.html.Span | Label class only for form labels |
| com.vaadin.ui.Link | com.vaadin.flow.component.html.Anchor | Direct replacement |
| com.vaadin.ui.ListSelect | com.vaadin.flow.component.listbox.ListBox | Direct replacement |
| com.vaadin.ui.LoginForm | com.vaadin.flow.component.login.LoginForm / LoginOverlay | Available since Vaadin 14 |
| com.vaadin.ui.MenuBar | com.vaadin.flow.component.menubar.MenuBar | Available since Vaadin 14 |
| com.vaadin.ui.NativeButton | com.vaadin.flow.component.button.NativeButton | Direct replacement |
| com.vaadin.ui.NativeSelect | com.vaadin.flow.component.select.Select | Available since Vaadin 13 |
| com.vaadin.ui.Notification | com.vaadin.flow.component.notification.Notification | Direct replacement |
| com.vaadin.ui.Panel | — | Use add-ons (Card-like components) |
| com.vaadin.ui.PasswordField | com.vaadin.flow.component.textfield.PasswordField | Direct replacement |
| com.vaadin.ui.PopupView | — | Compose using Button + ContextMenu |
| com.vaadin.ui.ProgressBar | com.vaadin.flow.component.progressbar.ProgressBar | Direct replacement |
| com.vaadin.ui.RadioButtonGroup | com.vaadin.flow.component.radiobutton.RadioButtonGroup | Direct replacement |
| com.vaadin.ui.RichTextArea | com.vaadin.flow.component.richtexteditor.RichTextEditor | Available since Vaadin 13 |
| com.vaadin.ui.Slider | — | Use HTML <input type="range"> or add-ons |
| com.vaadin.ui.TabSheet | com.vaadin.flow.component.tabs.Tabs | Content managed manually |
| com.vaadin.ui.TextArea | com.vaadin.flow.component.textfield.TextArea | Direct replacement |
| com.vaadin.ui.TextField | com.vaadin.flow.component.textfield.TextField | Numeric fields are separate classes |
| com.vaadin.ui.Tree | — | Add-on required |
| com.vaadin.ui.TreeGrid | com.vaadin.flow.component.treegrid.TreeGrid | Available since Vaadin 12 |
| com.vaadin.ui.TwinColSelect | — | Implement as composite |
| com.vaadin.ui.Video | — | Use native HTML <video> |
| com.vaadin.ui.VerticalLayout | com.vaadin.flow.component.orderedlayout.VerticalLayout | Flexbox-based |
| com.vaadin.ui.VerticalSplitPanel | com.vaadin.flow.component.splitlayout.SplitLayout | Orientation configurable |
| com.vaadin.ui.UI | com.vaadin.flow.component.UI | No longer mandatory |
| com.vaadin.ui.Upload | com.vaadin.flow.component.upload.Upload | Direct replacement |
| com.vaadin.ui.Window | com.vaadin.flow.component.dialog.Dialog | Limited feature parity |


---

## Basic component features

### Component is lightweight and maps to an Element
In Flow, each component maps to a single **root element** in the server-side DOM. Components can contain nested components/elements. The **Element API** is the low-level way to manipulate DOM from server-side Java. 

Flow’s `Component` is an abstract class with minimal API. Additional capabilities are added through mixin interfaces (e.g., implement `HasSize` to get sizing API like `setWidth` / `setHeight`). 

### Captions and icons aren’t universal
Vaadin 8’s universal caption concept is gone. Many components use component-specific labels (often `setLabel(...)` rather than `setCaption(...)`). If needed, create your own `Span`/`Text` and place it next to the component. Icons are possible, but not universally supported by a single built-in caption+icon system. 

### `setEnabled(...)` is still a security feature
In Flow, `setEnabled` is available only for components implementing `HasEnabled`. When disabled, by default, client-side property changes and DOM events are ignored; whitelisting can allow specific properties/events. Disabled state cascades to children; client changes remain blocked even if the component doesn’t visually look disabled. 

### `setReadOnly(...)` is component-specific and behaves differently
`setReadOnly` applies to input components implementing `HasValue`. Client changes won’t affect `getValue()` and won’t fire `ValueChangeEvent`. Most components also update visuals to indicate read-only state. 

### Tooltips are component-specific
Vaadin 8 allowed tooltips broadly. In Vaadin Flow there’s no automatic, universal tooltip mechanism; it’s component-specific and can be achieved with CSS where applicable. 

---

## Layouts in Vaadin Flow

Vaadin 8 layouts relied on a client-side `LayoutManager` (historically to cope with browser differences and older IE). In Vaadin Flow, layouts rely on standard HTML5/CSS3: no LayoutManager calculations, faster native rendering, and responsive layouts via the DOM/Element API from server-side Java. 

### Core layouts API and creating custom layouts
You can create custom layouts with only server-side Java using mixin interfaces and the Element API. Key mixins include: 
- `HasComponents`:
  - `add(Component... components)`
  - `remove(Component... components)` and `removeAll()`
- `HasOrderedComponents` for index-based access

### Layout click listeners
Layouts don’t expose a direct API, but you can attach a DOM event listener to the underlying element for click events. 

### Available layouts and key API differences

#### HorizontalLayout & VerticalLayout
They now map more directly to CSS flexbox concepts (native browser rendering). Key API differences: 
- `setComponentAlignment` / `setDefaultComponentAlignment` →
  - `HorizontalLayout`: `setVerticalComponentAlignment` / `setDefaultVerticalComponentAlignment`
  - `VerticalLayout`: `setHorizontalComponentAlignment` / `setDefaultHorizontalComponentAlignment`
  - These correspond to CSS `align-self` and `align-items`.
- `setExpandRatio` → `setFlexGrow`
- `expand()` sets `flex-grow` to `1`
- `setMargin` → `setPadding`
- Spacing/Padding are on/off for all edges; fine-grained padding via CSS using `component.getElement().getStyle()...`
- Setting `setSizeFull()` / `setHeight("100%")` / `setWidth("100%")` on children doesn’t behave like before; instead, leave size undefined and use flex-grow for sizing.

#### FormLayout
Now responsive and supports multiple columns; it can partially replace old `GridLayout` use cases. 

#### FlexLayout
A server-side convenience wrapper for a `<div>` with `display: flex` (configure flexbox properties from Java). Useful for responsive layouts if you’re comfortable with flexbox. 

#### Div (aka CssLayout replacement)
Vaadin 8 `CssLayout` was essentially a `<div>`. In Vaadin Flow this is explicitly `Div`. V8 `getCss` is not available, but you can modify CSS from server side via `component.getElement().getStyle()` (for any component, not just `Div`). 

---

## Replacing existing layouts

### AbsoluteLayout
Replace with `Div` + CSS: set `position: absolute` and use top/right/bottom/left coordinates for children via Element API styling. 

### GridLayout
No direct replacement; depending on the use case, alternatives include: 
- `Board` (commercial, fully responsive)
- `FormLayout` (multi-column)
- `FlexLayout` (powerful, requires flexbox understanding)
- Nesting `HorizontalLayout` and `VerticalLayout`
- `Div` + modern CSS Grid (widely supported in modern browsers)

### CustomLayout
For static content, use an `Html` container. For dynamic content, use `PolymerTemplate` with `@Id` bindings. 

---

## Migrating your own components

Flow’s server-side DOM access makes many previous GWT-based reasons for custom components unnecessary, but **Vaadin 8 custom components must be rebuilt**. You can often reuse server-side API, but expect changes due to the updated component hierarchy and mixin-based approach. 

Guidance:
- Simple components: compose using existing components + Element API (see creating-components tutorials).
- More complex components (heavy client-side logic or complex DOM): implement as Web Components and provide a Java API wrapper. 
```


button.setDescription()
button.getElement().setProperty("title", "..."); // tooltip HTML nativo

button.setLabel(
button.setText(

setItemLabelGenerator -> setItemLabelGenerator


import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.component.html.Span;

        setRenderer(new ComponentRenderer<>(value ->
                new Span( UIUtils.localize(value.getI18nKey()) )
        ));


public class BooleanOptionGroup extends RadioButtonGroup<Boolean> {

    private static final long serialVersionUID = 1L;

    public BooleanOptionGroup() {
        super();

        // Ordered labels: false first, then true
        Map<Boolean, String> labels = new LinkedHashMap<>();
        labels.put(false, UIUtils.localize("enum.boolean.false"));
        labels.put(true, UIUtils.localize("enum.boolean.true"));

        setItems(labels.keySet());

        // Vaadin 14-compatible label rendering
        setRenderer(new ComponentRenderer<>(value ->
                new Span(labels.get(value))
        ));
    }
}
