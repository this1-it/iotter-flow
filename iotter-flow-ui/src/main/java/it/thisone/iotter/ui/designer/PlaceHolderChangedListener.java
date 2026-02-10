package it.thisone.iotter.ui.designer;

import java.io.Serializable;

import com.vaadin.flow.component.ComponentEventListener;

public interface PlaceHolderChangedListener extends ComponentEventListener<PlaceHolderChangedEvent>, Serializable {
	public static final String PLACE_HOLDER_CHANGED = "placeHolderChanged";

	@Override
	default void onComponentEvent(PlaceHolderChangedEvent event) {
		placeHolderChanged(event);
	}

	public void placeHolderChanged(PlaceHolderChangedEvent event);
}
