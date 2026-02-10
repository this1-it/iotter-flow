package it.thisone.iotter.ui.groupwidgets;

import java.util.ArrayList;
import java.util.Collection;

import com.vaadin.flow.component.icon.VaadinIcon;

import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.enums.Period;
import it.thisone.iotter.exporter.ExportProperties;
import it.thisone.iotter.ui.common.WidgetTypeInstance;
import it.thisone.iotter.ui.ifc.IGroupWidgetUiFactory;
import it.thisone.iotter.ui.model.TimePeriod;
import it.thisone.iotter.ui.model.TimePeriod.TimePeriodEnum;
//import it.thisone.iotter.ui.providers.TypeVarMultiTraceProvider;

public class GroupWidgetUiFactory implements IGroupWidgetUiFactory {

    public TimePeriod getDefaultPeriod() {
        return new TimePeriod(Period.HOUR, 1, TimePeriodEnum.LAST);
    }

    @Override
    public Collection<TimePeriod> getPeriods() {
        Collection<TimePeriod> items = new ArrayList<>();
        items.add(new TimePeriod(Period.HOUR, 1, TimePeriodEnum.LAST, "Last hour"));
        items.add(new TimePeriod(Period.HOUR, 6, TimePeriodEnum.LAST, "Last 6 hours"));
        items.add(new TimePeriod(Period.DAY, 1, TimePeriodEnum.LAST, "Last day"));
        items.add(new TimePeriod(Period.WEEK, 1, TimePeriodEnum.LAST, "Last week"));
        items.add(new TimePeriod(Period.MONTH, 1, TimePeriodEnum.LAST, "Last month"));
        items.add(new TimePeriod(Period.MONTH, 3, TimePeriodEnum.LAST, "Last 3 months"));
        return items;
    }

    @Override
    public Collection<WidgetTypeInstance> getWidgetTypes() {
        Collection<WidgetTypeInstance> items = new ArrayList<>();

        items.add(new WidgetTypeInstance(GraphicWidgetType.MULTI_TRACE, null, VaadinIcon.BAR_CHART.create()));
        items.add(new WidgetTypeInstance(GraphicWidgetType.HISTOGRAM, null, VaadinIcon.BAR_CHART.create()));
        items.add(new WidgetTypeInstance(GraphicWidgetType.WIND_ROSE, null, VaadinIcon.BAR_CHART.create()));
        items.add(new WidgetTypeInstance(GraphicWidgetType.TABLE, null, VaadinIcon.TABLE.create()));
        items.add(new WidgetTypeInstance(GraphicWidgetType.LAST_MEASURE, null, VaadinIcon.SIGNAL.create()));
        items.add(new WidgetTypeInstance(GraphicWidgetType.LAST_MEASURE_TABLE, null, VaadinIcon.SIGNAL.create()));
        items.add(new WidgetTypeInstance(GraphicWidgetType.LABEL, null, VaadinIcon.TAG.create()));
        items.add(new WidgetTypeInstance(GraphicWidgetType.EMBEDDED, null, VaadinIcon.PICTURE.create()));
        items.add(new WidgetTypeInstance(GraphicWidgetType.WEBPAGE, null, VaadinIcon.PICTURE.create()));

//        items.add(new WidgetTypeInstance(GraphicWidgetType.CUSTOM, TypeVarMultiTraceProvider.TYPEVAR_MULTI_TRACE,
//                VaadinIcon.SUITCASE.create()));

        return items;
    }

    @Override
    public ExportProperties getExportProperties() {
        return new ExportProperties();
    }
}
