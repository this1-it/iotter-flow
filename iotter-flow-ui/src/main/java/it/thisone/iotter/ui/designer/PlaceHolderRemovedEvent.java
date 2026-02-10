package it.thisone.iotter.ui.designer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

public class PlaceHolderRemovedEvent extends ComponentEvent<Component> {
	public PlaceHolderRemovedEvent(Component source) {
		super(source, false);
	}
}
