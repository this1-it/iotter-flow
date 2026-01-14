package it.thisone.iotter.ui.common;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

import it.thisone.iotter.persistence.model.BaseEntity;

public class EditorSelectedEvent<T extends BaseEntity> extends ComponentEvent<Component> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Set<T> selected;
	public EditorSelectedEvent(Component source, T item) {
		super(source, false);
		if (item != null) {
			selected = new HashSet<T>();
			selected.add(item);
		}
	}
	
	public EditorSelectedEvent(Component source, List<T> items) {
		super(source, false);
		if (items != null) {
			selected = new HashSet<T>(items);
		}
	}

	public Set<T> getSelected() {
		return selected;
	}
}
