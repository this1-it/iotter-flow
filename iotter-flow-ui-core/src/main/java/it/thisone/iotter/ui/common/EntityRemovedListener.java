package it.thisone.iotter.ui.common;

import java.io.Serializable;

public interface EntityRemovedListener extends Serializable {
	String ENTITY_REMOVED = "entityRemoved";

	void entityRemoved(EntityRemovedEvent<?> event);
}
