package it.thisone.iotter.ui.common.fields;

import com.vaadin.flow.component.combobox.ComboBox;

import it.thisone.iotter.enums.modbus.TypeVar;
import it.thisone.iotter.ui.common.UIUtils;

public class TypeVarSelect extends ComboBox<TypeVar> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2993122599439071404L;
	
	public TypeVarSelect() {
		super();
		setItems(TypeVar.values());
		setItemLabelGenerator(type -> getTranslation(type.getI18nKey()));

	}
}