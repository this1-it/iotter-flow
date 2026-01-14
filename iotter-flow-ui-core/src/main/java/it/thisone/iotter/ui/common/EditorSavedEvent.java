package it.thisone.iotter.ui.common;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

import it.thisone.iotter.persistence.model.BaseEntity;

public class EditorSavedEvent<T extends BaseEntity> extends ComponentEvent<Component> {
	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	private T savedItem;
	public EditorSavedEvent(Component source, T savedItem) {
		super(source, false);
		this.savedItem = savedItem;
	}

	public T getSavedItem() {
		return savedItem;
	}
}
