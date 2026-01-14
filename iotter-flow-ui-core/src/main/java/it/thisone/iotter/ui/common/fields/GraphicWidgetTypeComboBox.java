package it.thisone.iotter.ui.common.fields;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.flow.component.combobox.ComboBox;

import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.ui.common.UIUtils;

public class GraphicWidgetTypeComboBox extends ComboBox<GraphicWidgetType> {

	private static final long serialVersionUID = 1039291023978438806L;

	public GraphicWidgetTypeComboBox() {
		super();
		List<GraphicWidgetType> sortedItems = Arrays.asList(GraphicWidgetType.values());
		sortedItems.sort(Comparator.comparing(type -> UIUtils.localize(type.getI18nKey())));
		setItems(sortedItems);
		setLabelCaptionGenerator(type -> UIUtils.localize(type.getI18nKey()));
		setItemIconGenerator(type -> {
			switch (type) {
				case HISTOGRAM:
				case MULTI_TRACE:
				case WIND_ROSE:
					return VaadinIcons.BAR_CHART;
				case TABLE:
					return VaadinIcons.TABLE;
				case LABEL:
					return VaadinIcons.TAG;
				case LAST_MEASURE:
				case LAST_MEASURE_TABLE:
					return VaadinIcons.SIGNAL;
				case EMBEDDED:
					return VaadinIcons.PICTURE;
				case WEBPAGE:
					return VaadinIcons.LINK;
				case CUSTOM:
					return VaadinIcons.SUITCASE;
				default:
					return null;
			}
		});
	}

}
