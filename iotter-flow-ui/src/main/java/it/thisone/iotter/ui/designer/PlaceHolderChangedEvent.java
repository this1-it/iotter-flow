package it.thisone.iotter.ui.designer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

public class PlaceHolderChangedEvent extends ComponentEvent<Component> {
	public PlaceHolderChangedEvent(Component source) {
		super(source, false);
	}
}
