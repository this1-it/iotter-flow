package it.thisone.iotter.ui.common;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.ui.main.UiConstants;

@CssImport(value = "./styles/side-drawer.css", themeFor = "vaadin-dialog-overlay")
public class SideDrawer extends Dialog {

    private static final long serialVersionUID = 1L;

    private final VerticalLayout contentArea;

    public SideDrawer(String caption) {
        addThemeName("side-drawer");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        setDraggable(false);

        // Disable transform transition on overlay part to prevent bounce on close
        getElement().executeJs(
                "this.$.overlay.$.overlay.style.transition = 'none';"
              + "this.$.overlay.$.overlay.style.transform = 'none';");
        addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                getElement().executeJs(
                        "this.$.overlay.$.overlay.style.transition = 'none';"
                      + "this.$.overlay.$.overlay.style.transform = 'none';");
            }
        });

        // HEADER
        Span title = new Span(caption);
        title.addClassName("side-drawer-title");

        Button close = new Button(VaadinIcon.CLOSE_SMALL.create(), e -> close());
        close.addClassName("side-drawer-close");
        close.getElement().setAttribute("aria-label", "Close");

        HorizontalLayout header = new HorizontalLayout(title, close);
        header.setWidthFull();
        header.setHeight(UiConstants.SIDE_DRAWER_HEADER_HEIGHT, Unit.PIXELS);
        header.setPadding(true);
        header.setSpacing(false);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.expand(title);
        header.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");
        header.getStyle().set("background-color", "var(--lumo-base-color)");
        header.getStyle().set("flex-shrink", "0");

        // CONTENT AREA - will hold the form set via setDrawerContent()
        contentArea = new VerticalLayout();
        contentArea.setSizeFull();
        //contentArea.setHeight("calc(100vh - 48px)");
        contentArea.setSpacing(false);
        contentArea.setPadding(false);
        //contentArea.getElement().getStyle().set("height", "100vh");
        contentArea.getElement().getStyle().set("min-height", "0");

        // WRAPPER - a VerticalLayout is natively display:flex + flex-direction:column
        // This bypasses the div.draggable (display:block) issue
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setSizeFull();
        wrapper.setSpacing(false);
        wrapper.setPadding(false);
        wrapper.add(header, contentArea);
        wrapper.setFlexGrow(1, contentArea);

        add(wrapper);
    }

    public void setDrawerContent(Component component) {
        contentArea.removeAll();
        if (component != null) {
            contentArea.add(component);
            contentArea.setFlexGrow(1, component);
        }
    }
}
