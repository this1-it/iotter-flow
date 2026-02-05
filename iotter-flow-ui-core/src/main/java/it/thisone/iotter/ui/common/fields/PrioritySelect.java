package it.thisone.iotter.ui.common.fields;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.combobox.ComboBox;

import it.thisone.iotter.enums.Priority;
import it.thisone.iotter.ui.common.UIUtils;

public class PrioritySelect extends ComboBox<Priority> {

	private static final long serialVersionUID = -2993122599439071404L;
	
	public PrioritySelect() {
		super();
		setLabel(getTranslation("basic.combobox.priority"));
		setItems(Priority.values());
		setItemLabelGenerator(type -> getTranslation(type.getI18nKey()));
		setWidth(16, Unit.EM);
//		setEmptySelectionAllowed(false);
//		setTextInputAllowed(true);
//		setPageLength(10);
	}

}
