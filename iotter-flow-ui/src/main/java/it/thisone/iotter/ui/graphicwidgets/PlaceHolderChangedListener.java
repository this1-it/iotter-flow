package it.thisone.iotter.ui.graphicwidgets;


import com.vaadin.flow.component.ComponentEventListener;

public interface PlaceHolderChangedListener extends ComponentEventListener<PlaceHolderChangedEvent> {
	public static final String PLACE_HOLDER_CHANGED = "placeHolderChanged";

	@Override
	default void onComponentEvent(PlaceHolderChangedEvent event) {
		placeHolderChanged(event);
	}

	public void placeHolderChanged(PlaceHolderChangedEvent event);
}
