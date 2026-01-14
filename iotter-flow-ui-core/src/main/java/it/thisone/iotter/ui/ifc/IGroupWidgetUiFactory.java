package it.thisone.iotter.ui.ifc;


import java.util.Collection;

import com.vaadin.flow.component.CustomField;

import it.thisone.iotter.exporter.ExportProperties;
import it.thisone.iotter.persistence.model.GraphicWidgetOptions;
import it.thisone.iotter.ui.common.WidgetTypeInstance;
import it.thisone.iotter.ui.model.TimePeriod;

public interface IGroupWidgetUiFactory {

	CustomField<GraphicWidgetOptions> getGraphicWidgetOptionsField();

	TimePeriod getDefaultPeriod();

	Collection<TimePeriod> getPeriods();

	Collection<WidgetTypeInstance> getWidgetTypes();

	ExportProperties getExportProperties(); 
	
}