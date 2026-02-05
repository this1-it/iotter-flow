package it.thisone.iotter.ui.common.fields;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.combobox.ComboBox;

import it.thisone.iotter.enums.modbus.Permission;
import it.thisone.iotter.ui.common.UIUtils;

public class PermissionSelect extends ComboBox<Permission> {

	private static final long serialVersionUID = -2993122599439071404L;
	
	public PermissionSelect() {
		super();
		setLabel(getTranslation("basic.combobox.permission"));
		setItems(Permission.values());
		setItemLabelGenerator(type -> type.getDisplayName());
		setWidth(16, Unit.EM);

	}

}
