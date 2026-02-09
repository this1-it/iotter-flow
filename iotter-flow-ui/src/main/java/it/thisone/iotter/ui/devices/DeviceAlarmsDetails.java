package it.thisone.iotter.ui.devices;

import java.text.ChoiceFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.cassandra.model.FeedAlarmEvent;
import it.thisone.iotter.cassandra.model.IFeedAlarm;
import it.thisone.iotter.integration.CassandraService;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.service.MeasureUnitTypeService;
import it.thisone.iotter.ui.model.ChannelAdapter;
import it.thisone.iotter.ui.model.ChannelAdapterDataProvider;


public class DeviceAlarmsDetails extends Composite<VerticalLayout> {



	public DeviceAlarmsDetails(Device device, List<FeedAlarmEvent> events,ChoiceFormat cf ) {
		super();

		getContent().add(buildContent(device, events,cf));
	}

	public String getI18nLabel(String key) {
		return getTranslation(DeviceForm.NAME + "."+ key);
	}


	private Component buildContent(Device device, List<FeedAlarmEvent> events,ChoiceFormat cf) {
		
		ChannelAdapterDataProvider ccontainer = new ChannelAdapterDataProvider();
		ccontainer.setMeasureRenderer(cf);
		ccontainer.addChannels(device.getChannels());
		List<IFeedAlarm> alarms = new ArrayList<>();
		alarms.addAll(events);
		List<ChannelAdapter> items = ccontainer.renderAlarms(alarms);
		
		if (items.isEmpty()) {
			VerticalLayout layout = new VerticalLayout();
			layout.setSizeFull();
			layout.setPadding(true);
			layout.add(new Label(getI18nLabel("no_alarm_events")));
			return layout;
		}
		
		
		Grid<ChannelAdapter> grid = new Grid<>();

		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setSizeFull();

		grid.addColumn(ChannelAdapter::getAlarmStatus)
			.setHeader(getI18nLabel("alarmStatus"));
		grid.addColumn(ChannelAdapter::getAlarmDate)
			.setHeader(getI18nLabel("alarmDate"));
		grid.addColumn(ChannelAdapter::getDisplayName)
			.setHeader(getI18nLabel("displayName"));
		grid.addColumn(ChannelAdapter::getAlarmValue)
			.setHeader(getI18nLabel("alarmValue"));
		grid.addColumn(ChannelAdapter::getMeasureUnit)
			.setHeader(getI18nLabel("measureUnit"));
		grid.addColumn(ChannelAdapter::getAlarmOperator)
			.setHeader(getI18nLabel("alarmOperator"));
		grid.addColumn(ChannelAdapter::getAlarmMembers)
			.setHeader(getI18nLabel("alarmMembers"));

		grid.setItems(items);
		//grid.sort(Grid.SortDirection.DESCENDING, grid.getColumns().get(1));
		return grid;

		
	}


}
