package org.vaadin.flow.components;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.shared.Registration;

/**
 * Basic drop-in replacement for com.vaadin.ui.Panel.
 *
 * - Single component container
 * - Scrollable
 * - Optional caption
 *
 * NOT implemented (by design):
 * - ActionManager
 * - Legacy RPC scrolling sync
 * - Declarative design
 */
public class PanelFlow extends Composite<Div>
        implements HasSize, Focusable<PanelFlow>, ClickNotifier<PanelFlow> {

    private final Div content = new Div();
    private final Span caption = new Span();

    public PanelFlow() {
        init();
    }

    public PanelFlow(Component content) {
        init();
        setContent(content);
    }

    public PanelFlow(String caption) {
        init();
        setCaption(caption);
    }

    public PanelFlow(String caption, Component content) {
        init();
        setCaption(caption);
        setContent(content);
    }

    private void init() {
        Div root = getContent();

        root.addClassName("panel-flow");
        root.getStyle().set("overflow", "auto");
        root.getStyle().set("display", "flex");
        root.getStyle().set("flex-direction", "column");

        caption.addClassName("panel-flow-caption");
        caption.setVisible(false);

        content.addClassName("panel-flow-content");
        content.getStyle().set("flex", "1");

        root.add(caption, content);
        setWidthFull();
        getElement().setAttribute("tabindex", "-1");
    }

    /* ---------------- Content ---------------- */

    public void setContent(Component component) {
        content.removeAll();
        if (component != null) {
            content.add(component);
        }
    }

    public Component getContentComponent() {
        return content.getComponentCount() == 0 ? null : content.getComponentAt(0);
    }

    /* ---------------- Caption ---------------- */

    public void setCaption(String text) {
        if (text == null || text.isEmpty()) {
            caption.setVisible(false);
            caption.setText("");
        } else {
            caption.setText(text);
            caption.setVisible(true);
        }
    }

    public String getCaption() {
        return caption.getText();
    }

    /* ---------------- Focus ---------------- */

    @Override
    public void focus() {
        getElement().callJsFunction("focus");
    }

    /* ---------------- Click ---------------- */

    @Override
    public Registration addClickListener(ComponentEventListener<ClickEvent<PanelFlow>> listener) {
        //return addListener(ClickEvent.class, listener);
        return null;
    }
}
