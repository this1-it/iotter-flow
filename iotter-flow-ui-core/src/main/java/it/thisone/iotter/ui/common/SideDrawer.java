package it.thisone.iotter.ui.common;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;

@CssImport(value = "./styles/side-drawer.css", themeFor = "vaadin-dialog-overlay")
@CssImport("./styles/side-drawer-global.css")
public class SideDrawer extends Dialog {

    private static final long serialVersionUID = 1L;

    private final Div content;

    public SideDrawer(String caption) {
        addThemeName("side-drawer");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        setDraggable(false);

        // HEADER
        Span title = new Span(caption);
        title.addClassName("side-drawer-title");

        Button close = new Button(VaadinIcon.CLOSE_SMALL.create(), e -> close());
        close.addClassName("side-drawer-close");
        close.getElement().setAttribute("aria-label", "Close");

        Div header = new Div(title, close);
        header.addClassName("side-drawer-header");

        // CONTENT
        content = new Div();
        content.addClassName("side-drawer-content");
        content.getStyle().set("display", "flex");
        content.getStyle().set("flex-direction", "column");
        content.getStyle().set("flex", "1");
        content.getStyle().set("min-height", "0");
        content.getStyle().set("overflow", "auto");
        content.getStyle().set("padding", "var(--lumo-space-m)");

        add(header, content);
    }



    public void setDrawerContent(Component component) {
        content.removeAll();
        if (component != null) {
            component.getElement().getStyle().set("height", "100%");
            component.getElement().getStyle().set("min-height", "0");
            content.add(component);
        }
    }

    // public void applyDimension(float[] dimension) {
    //     if (dimension == null || dimension.length < 2) {
    //         setWidth("40vw");
    //         setHeight("100vh");
    //         return;
    //     }
    //     setWidth((dimension[0] * 100f) + "vw");
    //     setHeight((dimension[1] * 100f) + "vh");
    // }
}
