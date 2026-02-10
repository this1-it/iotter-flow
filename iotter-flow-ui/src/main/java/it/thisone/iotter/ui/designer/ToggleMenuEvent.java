package it.thisone.iotter.ui.designer;


import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.html.Div;


public class ToggleMenuEvent extends ComponentEvent<Div> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ToggleMenuEvent(Div source) {
		super(null, false);
	}
}
