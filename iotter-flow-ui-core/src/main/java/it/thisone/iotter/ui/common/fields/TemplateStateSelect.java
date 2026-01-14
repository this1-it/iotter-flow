package it.thisone.iotter.ui.common.fields;

import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.component.combobox.ComboBox;

import it.thisone.iotter.enums.modbus.TemplateState;
import it.thisone.iotter.ui.common.UIUtils;

public class TemplateStateSelect extends ComboBox<TemplateState> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2993122599439071404L;

	public TemplateStateSelect() {
		super();
		setItems(TemplateState.values());
		setLabelCaptionGenerator(type -> UIUtils.localize(type.getI18nKey()));
	}

	
	@SuppressWarnings("unchecked")
	public void removeItem(TemplateState state) {
		// We assume the data provider is a ListDataProvider, as set by setItems()
		ListDataProvider<TemplateState> dataProvider = (ListDataProvider<TemplateState>) getDataProvider();
		dataProvider.getItems().remove(state);
		dataProvider.refreshAll();
	}
}
