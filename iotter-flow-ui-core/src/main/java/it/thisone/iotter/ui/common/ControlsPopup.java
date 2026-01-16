package it.thisone.iotter.ui.common;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.vaadin.flow.components.PanelFlow;

public class ControlsPopup extends PanelFlow {

    private static final long serialVersionUID = -3868360389375898886L;

    private final Component content;

    public ControlsPopup(Component component) {
        super();
        this.content = component;

        // Trigger icon (COG)
        Icon cog = VaadinIcon.COG.create();
        cog.getStyle()
            .set("cursor", "pointer")
            .set("font-size", "20px");

        // Popup replacement
        ContextMenu popup = new ContextMenu(cog);
        popup.setOpenOnClick(true);
        popup.add(buildPopup());

        HorizontalLayout layout = new HorizontalLayout(cog);
        layout.setPadding(false);
        layout.setSpacing(false);

        setWidth("40px");
        setHeight("40px");
        setContent(layout);
    }

    private Component buildPopup() {
        return content;
    }
}
