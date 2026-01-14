package it.thisone.iotter.ui.common;

import java.io.Serializable;

public interface EditorAddedListener extends Serializable {
	public static final String EDITOR_ADDED = "addedItem";
	public void addedItem(EditorAddedEvent event);
}
