package it.thisone.iotter.ui.common;

import java.io.Serializable;

public interface EntitySelectedListener extends Serializable {
	String ENTITY_SELECTED = "entitySelected";

	void entitySelected(EntitySelectedEvent<?> event);
}
