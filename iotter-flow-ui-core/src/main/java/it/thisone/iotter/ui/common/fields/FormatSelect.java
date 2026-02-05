package it.thisone.iotter.ui.common.fields;

import com.vaadin.flow.component.combobox.ComboBox;

import it.thisone.iotter.enums.modbus.Format;
import it.thisone.iotter.ui.common.UIUtils;

public class FormatSelect extends ComboBox<Format> {

	private static final long serialVersionUID = -2993122599439071404L;
	
	public FormatSelect() {
		super();
		setItems(Format.values());
		setItemLabelGenerator(type -> getTranslation(type.getI18nKey()));
	}

}
