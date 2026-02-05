package it.thisone.iotter.ui.common.fields;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.combobox.ComboBox;

import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.ui.common.UIUtils;

public class GraphicWidgetTypeComboBox extends ComboBox<GraphicWidgetType> {

	private static final long serialVersionUID = 1039291023978438806L;

	public GraphicWidgetTypeComboBox() {
		super();
		List<GraphicWidgetType> sortedItems = Arrays.asList(GraphicWidgetType.values());
		sortedItems.sort(Comparator.comparing(type -> getTranslation(type.getI18nKey())));
		setItems(sortedItems);
		setItemLabelGenerator(type -> getTranslation(type.getI18nKey()));

	}

}
