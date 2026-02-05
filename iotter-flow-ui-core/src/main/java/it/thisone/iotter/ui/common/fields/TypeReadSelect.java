
package it.thisone.iotter.ui.common.fields;



import com.vaadin.flow.component.combobox.ComboBox;

import it.thisone.iotter.enums.modbus.TypeRead;
import it.thisone.iotter.ui.common.UIUtils;
public class TypeReadSelect extends ComboBox<TypeRead> {
	private static final long serialVersionUID = -2993122599439071404L;
	public TypeReadSelect() {
		super();
		setItems(TypeRead.values());
		setItemLabelGenerator(type -> getTranslation(type.getI18nKey()));
	}

}
