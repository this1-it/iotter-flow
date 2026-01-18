package it.thisone.iotter.ui.common;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;

@CssImport(value = "./styles/side-drawer.css", themeFor = "vaadin-dialog-overlay")
public class SideDrawer extends Dialog {

    private static final long serialVersionUID = 1L;

    private final Div content;

    public SideDrawer() {
        addThemeName("side-drawer");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        setDraggable(false);

        content = new Div();
        content.addClassName("side-drawer-content");
        add(content);
    }

    public void setDrawerContent(Component component) {
        content.removeAll();
        if (component != null) {
            content.add(component);
        }
    }

    public void applyDimension(float[] dimension) {
        if (dimension == null || dimension.length < 2) {
            setWidth("40vw");
            setHeight("100vh");
            return;
        }
        setWidth((dimension[0] * 100f) + "vw");
        setHeight((dimension[1] * 100f) + "vh");
    }
}
