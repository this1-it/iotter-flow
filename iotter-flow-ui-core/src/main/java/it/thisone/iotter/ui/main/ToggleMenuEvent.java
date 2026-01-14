package it.thisone.iotter.ui.main;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

public class ToggleMenuEvent extends ComponentEvent<Component> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ToggleMenuEvent(Component source) {
		super(source, false);
	}
}
