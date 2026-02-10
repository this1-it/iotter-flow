package it.thisone.iotter.ui.designer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

public class PlaceHolderSavedEvent extends ComponentEvent<Component> {
	public PlaceHolderSavedEvent(Component source) {
		super(source, false);
	}
}
