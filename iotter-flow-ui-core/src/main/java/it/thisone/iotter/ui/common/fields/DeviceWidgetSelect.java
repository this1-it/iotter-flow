package it.thisone.iotter.ui.common.fields;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.vaadin.flow.components.TwinColSelectFlow;

import it.thisone.iotter.persistence.model.DeviceWidget;

public class DeviceWidgetSelect extends TwinColSelectFlow<DeviceWidget> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DeviceWidgetSelect() {
		super();
		setItems(Collections.emptySet());
	}

	public void setOptions(Collection<DeviceWidget> options) {
		Set<DeviceWidget> items = options == null ? Collections.emptySet() : new LinkedHashSet<>(options);
		setItems(items);
	}

}
