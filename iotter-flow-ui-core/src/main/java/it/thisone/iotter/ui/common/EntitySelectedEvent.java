package it.thisone.iotter.ui.common;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

import it.thisone.iotter.persistence.model.BaseEntity;

public class EntitySelectedEvent<T extends BaseEntity> extends ComponentEvent<Component> {
	private static final long serialVersionUID = 1L;

	private final T entity;

	public EntitySelectedEvent(Component source, T entity) {
		super(source, false);
		this.entity = entity;
	}

	public T getEntity() {
		return entity;
	}
}
