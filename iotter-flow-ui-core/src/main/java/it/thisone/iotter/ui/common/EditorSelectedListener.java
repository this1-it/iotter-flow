package it.thisone.iotter.ui.common;

import java.io.Serializable;

public interface EditorSelectedListener extends Serializable {
	public static final String EDITOR_SELECTED = "editorSelected";
	public void editorSelected(EditorSelectedEvent event);
}
