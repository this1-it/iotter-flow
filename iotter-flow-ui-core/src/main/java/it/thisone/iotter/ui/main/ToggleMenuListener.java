package it.thisone.iotter.ui.main;

import java.io.Serializable;

public interface ToggleMenuListener extends Serializable {
	public static final String TOGGLEMENU = "toggleMenu";
	public void toggleMenu(ToggleMenuEvent event);
}
