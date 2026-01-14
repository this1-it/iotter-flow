package it.thisone.iotter.ui.common;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

import it.thisone.iotter.persistence.model.BaseEntity;

public class EditorAddedEvent<T extends BaseEntity> extends ComponentEvent<Component> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final T item;
	public EditorAddedEvent(Component source, T item) {
		super(source, false);
		this.item = item;
	}

	public T getItem() {
		return item;
	}
}
