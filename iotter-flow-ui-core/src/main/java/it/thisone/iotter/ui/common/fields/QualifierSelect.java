package it.thisone.iotter.ui.common.fields;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.combobox.ComboBox;

import it.thisone.iotter.enums.modbus.Qualifier;
import it.thisone.iotter.ui.common.UIUtils;

public class QualifierSelect extends ComboBox<Qualifier> {

	private static final long serialVersionUID = -2993122599439071404L;
	
	public QualifierSelect() {
		super();
		setLabel(UIUtils.localize("basic.combobox.qualifier"));
		setItems(Qualifier.values());
		setItemLabelGenerator(type -> type.getDisplayName());
		setWidth(16, Unit.EM);
//		setEmptySelectionAllowed(false);
//		setTextInputAllowed(true);
//		setPageLength(10);
	}

}
