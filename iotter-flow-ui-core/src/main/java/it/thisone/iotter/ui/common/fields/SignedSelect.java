package it.thisone.iotter.ui.common.fields;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.combobox.ComboBox;

import it.thisone.iotter.enums.modbus.Signed;
import it.thisone.iotter.ui.common.UIUtils;

public class SignedSelect extends ComboBox<Signed> {
	private static final long serialVersionUID = -2993122599439071404L;
	

	public SignedSelect() {
		super();
		setLabel(getTranslation("basic.combobox.signed"));
		setItems(Signed.values());
		setItemLabelGenerator(type -> getTranslation(type.getI18nKey()));
		setWidth(16, Unit.EM);
//		setEmptySelectionAllowed(false);
//		setTextInputAllowed(true);
//		setPageLength(10);
	}

}
