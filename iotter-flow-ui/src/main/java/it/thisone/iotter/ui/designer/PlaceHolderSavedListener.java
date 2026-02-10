package it.thisone.iotter.ui.designer;

import java.io.Serializable;

import com.vaadin.flow.component.ComponentEventListener;

public interface PlaceHolderSavedListener extends ComponentEventListener<PlaceHolderSavedEvent>, Serializable {
	public static final String PLACE_HOLDER_SAVED = "placeHolderSaved";

	@Override
	default void onComponentEvent(PlaceHolderSavedEvent event) {
		placeHolderSaved(event);
	}

	public void placeHolderSaved(PlaceHolderSavedEvent event);
}
