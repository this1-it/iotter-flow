package it.thisone.iotter.ui.common;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

import it.thisone.iotter.persistence.model.BaseEntity;

public class ItemSelectedEvent<T extends BaseEntity> extends ComponentEvent<Component> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	T selected;
	public ItemSelectedEvent(Component source, T item) {
		super(source, false);
		if (item != null) {
			selected = item;
		}
	}
	

	public T getSelected() {
		return selected;
	}
}
