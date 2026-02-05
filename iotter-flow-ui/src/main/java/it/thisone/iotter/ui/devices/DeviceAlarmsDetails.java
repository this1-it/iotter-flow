package it.thisone.iotter.ui.devices;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import it.thisone.iotter.cassandra.model.FeedAlarmEvent;
import it.thisone.iotter.cassandra.model.IFeedAlarm;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.ui.common.AbstractBaseEntityDetails;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.model.ChannelAdapter;
import it.thisone.iotter.ui.model.ChannelAdapterDataProvider;

@SuppressWarnings("serial")
public class DeviceAlarmsDetails extends AbstractBaseEntityDetails<Device> {
	
	public DeviceAlarmsDetails(Device item) {
		super(item, Device.class, DeviceForm.NAME, new String[]{}, false);
		getRemoveButton().setVisible(false);
		getSelectButton().setVisible(false);
		buildLayout(deviceAlarms(item));
	}


	public String getWindowStyle() {
		return "device-editor";
	}


	public float[] getWindowDimension() {
		return UIUtils.L_DIMENSION;
	}


	@Override
	protected void onRemove() throws EditorConstraintException {
	}

	private Component deviceAlarms(Device device) {
		
		List<FeedAlarmEvent> events = UIUtils.getCassandraService().getAlarms().getAlarmEvents(device.getSerial(), 30);
		ChannelAdapterDataProvider ccontainer = new ChannelAdapterDataProvider();
		ccontainer.addChannels(device.getChannels());
		List<IFeedAlarm> alarms = new ArrayList<>();
		alarms.addAll(events);
		List<ChannelAdapter> items = ccontainer.renderAlarms(alarms);
		
		if (items.isEmpty()) {
			VerticalLayout layout = new VerticalLayout();
			layout.setSizeFull();
			layout.setMargin(true);
			layout.addComponent(new Label(getI18nLabel("no_alarm_events")));
			return layout;
		}
		
		
		Grid<ChannelAdapter> grid = new Grid<>();
		grid.addStyleName(ValoTheme.TABLE_SMALL);
		grid.addStyleName(ValoTheme.TABLE_COMPACT);
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setSizeFull();

		grid.addColumn(ChannelAdapter::getAlarmStatus)
			.setCaption(getI18nLabel("alarmStatus"))
			.setExpandRatio(1);
		grid.addColumn(ChannelAdapter::getAlarmDate)
			.setCaption(getI18nLabel("alarmDate"))
			.setExpandRatio(1);
		grid.addColumn(ChannelAdapter::getDisplayName)
			.setCaption(getI18nLabel("displayName"))
			.setExpandRatio(2);
		grid.addColumn(ChannelAdapter::getAlarmValue)
			.setCaption(getI18nLabel("alarmValue"))
			.setExpandRatio(1);
		grid.addColumn(ChannelAdapter::getMeasureUnit)
			.setCaption(getI18nLabel("measureUnit"))
			.setExpandRatio(1);
		grid.addColumn(ChannelAdapter::getAlarmOperator)
			.setCaption(getI18nLabel("alarmOperator"))
			.setExpandRatio(1);
		grid.addColumn(ChannelAdapter::getAlarmMembers)
			.setCaption(getI18nLabel("alarmMembers"))
			.setExpandRatio(1);

		grid.setItems(items);
		//grid.sort(Grid.SortDirection.DESCENDING, grid.getColumns().get(1));
		return grid;

		
	}


}
