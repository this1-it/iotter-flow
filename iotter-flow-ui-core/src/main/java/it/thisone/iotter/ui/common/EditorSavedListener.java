package it.thisone.iotter.ui.common;

import java.io.Serializable;

public interface EditorSavedListener extends Serializable {
	public static final String EDITOR_SAVED = "editorSaved";
	public void editorSaved(EditorSavedEvent event);
}
