package it.thisone.iotter.ui.common;

import com.vaadin.flow.component.Component;

import it.thisone.iotter.persistence.model.BaseEntity;

public class EditorRemovedEvent<T extends BaseEntity> extends Component.Event {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private T removedItem;
	public EditorRemovedEvent(Component source, T removedItem) {
		super(source);
		this.removedItem = removedItem;
	}

	public T getRemovedItem() {
		return removedItem;
	}
	
	
	
	
}
