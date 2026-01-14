package it.thisone.iotter.ui.common.fields;

import java.util.Collection;
import java.util.Collections;

import org.vaadin.flow.components.TwinColSelectFlow;

import it.thisone.iotter.persistence.model.DeviceWidget;

public class DeviceWidgetSelect extends TwinColSelectFlow<DeviceWidget> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DeviceWidgetSelect() {
		super();
		setItems(Collections.emptyList());
		setLabelCaptionGenerator(DeviceWidget::toString);
	}

	public void setOptions(Collection<DeviceWidget> options) {
		setItems(options == null ? Collections.emptyList() : options);
	}

}
