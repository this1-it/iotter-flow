package it.thisone.iotter.ui.common.fields;

import com.vaadin.flow.component.combobox.ComboBox;

import it.thisone.iotter.enums.ChartScaleType;
import it.thisone.iotter.ui.common.UIUtils;

public class ChartScaleTypeSelect extends ComboBox<ChartScaleType> {

	private static final long serialVersionUID = -2993122599439071404L;
	
	public ChartScaleTypeSelect() {
		super();
		setItems(ChartScaleType.values());
		setItemLabelGenerator(type -> getTranslation(type.getI18nKey()));
	}

}
