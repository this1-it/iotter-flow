package it.thisone.iotter.ui.gridstack;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

@DomEvent("layout-changed")
public class LayoutChangedEvent extends ComponentEvent<GridstackBoard> {

    private static final long serialVersionUID = 1L;
    private final String layout;

    public LayoutChangedEvent(GridstackBoard source, boolean fromClient,
            @EventData("event.detail.layout") String layout) {
        super(source, fromClient);
        this.layout = layout;
    }

    public String getLayout() {
        return layout;
    }
}
