package it.thisone.iotter.ui.designer;

import java.io.Serializable;

import com.vaadin.flow.component.ComponentEventListener;

public interface PlaceHolderRemovedListener extends ComponentEventListener<PlaceHolderRemovedEvent>, Serializable {
	public static final String PLACE_HOLDER_REMOVED = "placeHolderRemoved";

	@Override
	default void onComponentEvent(PlaceHolderRemovedEvent event) {
		placeHolderRemoved(event);
	}

	public void placeHolderRemoved(PlaceHolderRemovedEvent event);
}
