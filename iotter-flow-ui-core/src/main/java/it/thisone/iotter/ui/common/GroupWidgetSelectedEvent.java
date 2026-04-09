package it.thisone.iotter.ui.common;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import it.thisone.iotter.persistence.model.GroupWidget;

public class GroupWidgetSelectedEvent extends ComponentEvent<Component> {
    private static final long serialVersionUID = 1L;
    private final GroupWidget groupWidget;

    public GroupWidgetSelectedEvent(Component source, GroupWidget groupWidget) {
        super(source, false);
        this.groupWidget = groupWidget;
    }

    public GroupWidget getGroupWidget() {
        return groupWidget;
    }
}
