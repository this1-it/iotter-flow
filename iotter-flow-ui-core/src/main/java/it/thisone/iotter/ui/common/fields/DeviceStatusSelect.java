package it.thisone.iotter.ui.common.fields;

import com.vaadin.flow.component.combobox.ComboBox;

import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.ui.common.UIUtils;

public class DeviceStatusSelect extends ComboBox<DeviceStatus> {

	private static final long serialVersionUID = -2993122599439071404L;
	
	public DeviceStatusSelect() {
		super();
		setItems(DeviceStatus.values());
		setItemLabelGenerator(type -> UIUtils.localize(type.getI18nKey()));
	}

}
