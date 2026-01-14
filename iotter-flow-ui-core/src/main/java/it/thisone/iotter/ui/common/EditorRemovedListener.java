package it.thisone.iotter.ui.common;

import java.io.Serializable;

public interface EditorRemovedListener extends Serializable {
	public static final String DETAILS_REMOVED = "detailsRemoved";
	public void detailsRemoved(EditorRemovedEvent event);
}
