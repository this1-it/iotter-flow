package it.thisone.iotter.ui.model;

import java.io.Serializable;
import java.text.ChoiceFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.data.provider.ListDataProvider;

import it.thisone.iotter.cassandra.model.FeedAlarmEvent;
import it.thisone.iotter.cassandra.model.IFeedAlarm;
import it.thisone.iotter.cassandra.model.IFeedKey;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.enums.AlarmStatus;
import it.thisone.iotter.enums.modbus.TypeVar;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChannelRemoteControl;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChannelUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.util.BacNet;
import it.thisone.iotter.util.EncryptUtils;

public class ChannelAdapterDataProvider extends ListDataProvider<ChannelAdapter> implements Serializable {
	public static final String EMPTY_VALUE = "...";
	public static Logger logger = LoggerFactory.getLogger(ChannelAdapterDataProvider.class);
	/**
	 * https://vaadin.com/forum#!/thread/3193456
	 */
	private static final long serialVersionUID = 1L;

	private Date lastDate;

	private boolean hideUnits;

	public ChannelAdapterDataProvider() throws IllegalArgumentException {
		super(new ArrayList<>());
		lastDate = new Date(0);
	}

	public void addChannels(Collection<Channel> channels) {
		Collection<ChannelAdapter> collection = new ArrayList<>();
		String pattern = UIUtils.getMeasureUnitPattern();
		ChoiceFormat measureRenderer = new ChoiceFormat(pattern);
		for (Channel channel : channels) {
			ChannelAdapter adapter = new ChannelAdapter(channel);
			adapter.setDisplayName(ChannelUtils.displayName(channel));
			adapt(adapter, channel, measureRenderer);
			collection.add(adapter);
		}
		
		getItems().addAll(collection);
		// sort(new Object[] { "metaData" }, new boolean[] { true });
		lastDate = new Date(0);
	}

	private void adapt(ChannelAdapter adapter, Channel channel, ChoiceFormat measureRenderer) {
		adapter.setSelected(channel.getConfiguration().isSelected());
		adapter.setMeasureUnit(measureRenderer.format((double) channel.getDefaultMeasure().getType()));
		ChoiceFormat cf = ChannelUtils.enumChoiceFormat(channel);
		adapter.setRenderer(cf);
		
		if (cf != null && channel.getDefaultMeasure().getType().equals(BacNet.ADIM)) {
			adapter.setMeasureUnit("");
		}
		
		if (channel.getDefaultMeasure().getType().equals(BacNet.ADIM)) {
			adapter.setMeasureUnit("");
		}
		
		adapter.setMetaData(channel.getMetaData());
		adapter.setKey(channel.getKey());
		adapter.setNumber(channel.getNumber());
		adapter.setAlarmed(!channel.getAlarm().isEmpty());
		adapter.setControlled(channel.getRemote().isValid());
		adapter.setSerial(channel.getDevice().getSerial());
		// adapter.setThresholds(thresholds(channel));

		try {
			String typeVar = ChannelUtils.getTypeVar(channel.getMetaData());
			adapter.setTypeVar(TypeVar.valueOf(typeVar.toUpperCase()));
		} catch (Exception e) {
		}
	}

	public void addFeeds(List<GraphicFeed> feeds) {
		if (feeds == null) return;
		hideUnits = true;
		Collection<ChannelAdapter> collection = new ArrayList<>();
		String pattern = UIUtils.getMeasureUnitPattern();
		ChoiceFormat measureRenderer = new ChoiceFormat(pattern);
		for (GraphicFeed feed : feeds) {
			if (feed.getChannel() != null) {
				ChannelAdapter adapter = new ChannelAdapter(feed.getChannel());
				adapter.setDisplayName(ChannelUtils.displayName(feed.getChannel()));
				if (feed.getLabel() != null && !feed.getLabel().trim().isEmpty()) {
					adapter.setLabel(feed.getLabel());
				} else {
					adapter.setLabel(adapter.getDisplayName());
				}
				adapter.setLastMeasureValue(EMPTY_VALUE);
				adapter.setLastMeasure(null);
				adapter.setFillColor(feed.getOptions().getFillColor());
				adapter.setChecked(feed.isChecked());				
				adapt(adapter, feed.getChannel(), measureRenderer);
				collection.add(adapter);
			}
		}
		getItems().addAll(collection);
		lastDate = new Date(0);
	}

	/*
	 * refresh visible items
	 */
	public void refresh() {
		List<IFeedKey> feeds = new ArrayList<>();
		for (ChannelAdapter adapter : getItems()) {
			if (adapter.getItem().getConfiguration().isActive()) {
				feeds.add(adapter);
			}
		}
		Map<IFeedKey, MeasureRaw> measures = UIUtils.getCassandraService().getFeeds().lastMeasures(feeds);
		for (ChannelAdapter adapter : getItems()) {
			MeasureRaw measure = measures.get(adapter);
			try {
				refresh(adapter, measure);
			} catch (Throwable e) {
				logger.error("refresh visible items " + adapter.getKey(), e);
			}
		}
	}

	public void refresh(ChannelAdapter adapter, MeasureRaw measure) {
		if (adapter.getItem().getConfiguration().isActive()) {
			if (measure != null && measure.getValue() != null && measure.getDate() != null) {
				if (lastDate == null) {
					lastDate = new Date(0);
				}
				Double number = calculateValue(measure.getValue(), adapter.getItem().getDefaultMeasure());
				adapter.setLastMeasure(number.floatValue());
				adapter.setLastMeasureDate(measure.getDate());
				if (lastDate.before(measure.getDate())) {
					lastDate = measure.getDate();
				}
				if (measure.hasError()) {
					adapter.setLastMeasureValue(EMPTY_VALUE);
					adapter.setLastMeasure(null);
				} else {
					adapter.setLastMeasureValue(
							renderValue(number, adapter.getRenderer(), adapter.getItem().getDefaultMeasure()));
				}
			}


		} else {
			adapter.setLastMeasureValue(EMPTY_VALUE);
			adapter.setLastMeasure(null);
		}
		
		adapter.setLastMeasureValueUnit(String.format("%s %s", adapter.getLastMeasureValue(), adapter.getMeasureUnit()));
//		if (hideUnits && adapter.getRenderer() != null) {
//			adapter.setMeasureUnit("");
//		}

	}

	public Date getLastDate() {
		return lastDate;
	}

	private void filterKey(String key) {
		clearFilters();
		if (!getItems().isEmpty() && key != null) {
			setFilter(item -> key.equals(item.getKey()));
		}
	}

	public ChannelAdapter getAdapter(String key) {
		if (key == null) {
			return null;
		}
		ChannelAdapter adapter = null;
		filterKey(key);
		if (getItems().size() > 0) {
			adapter = ((ArrayList<ChannelAdapter>) getItems()).get(0);
		}
		clearFilters();
		return adapter;
	}

	public String renderValue(Double number, ChoiceFormat cf, MeasureUnit mu) {
		String value = null;
		if (cf != null) {
			try {
				value = cf.format(number);
			} catch (Throwable e) {
			}
			if (value == null) {
				value = String.format("[%f]", number);
			}

		} else {
			value = ChartUtils.formatMeasure(number, mu);
		}
		return value;

	}

	public Double calculateValue(Float value, MeasureUnit mu) {
		Double number = 0d;
		try {
			number = (Double) ChartUtils.calculateMeasure(value, mu);
		} catch (Exception e1) {
		}
		return number;
	}

	public String thresholds(Channel channel) {
		ChannelRemoteControl remote = channel.getRemote();

		if (!remote.isValid())
			return null;

		String[] args = null;
		String format = "";
		if (ChannelUtils.isTypeDigital(channel)) {
			format = "%s";
			args = new String[1];
			args[0] = getTranslation("channel.remote.digital");
		} else {
			DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance();
			format = "%s:%s, %s:%s";
			args = new String[8];
			args[0] = getTranslation("channel.remote.min");
			args[1] = decimalFormat.format(remote.getMin());
			args[2] = getTranslation("channel.remote.max");
			args[3] = decimalFormat.format(remote.getMax());
		}
		return String.format(format, (Object[]) args);
	}

	private String getTranslation(String key) {
		// TODO Auto-generated method stub
		return key;
	}

	public boolean isHideUnits() {
		return hideUnits;
	}

	public void setHideUnits(boolean hideUnits) {
		this.hideUnits = hideUnits;
	}

	public List<ChannelAdapter> renderAlarms(List<IFeedAlarm> alarms) {
		List<ChannelAdapter> items = new ArrayList<ChannelAdapter>();
		for (IFeedAlarm event : alarms) {
			ChannelAdapter adapter = getAdapter(event.getKey());
			if (adapter != null) {
				ChannelAdapter item = new ChannelAdapter();
				Double number = new Double(event.getValue());
				String alarmValue = renderValue(number, adapter.getRenderer(), adapter.getItem().getDefaultMeasure());
				AlarmStatus status = AlarmStatus.valueOf(event.getStatus());
				switch (status) {
				case FIRE_DOWN:
				case FIRE_UP:
					status = AlarmStatus.ON;
					item.setAlarmFired(true);
					item.setAlarmed(true);
					break;

				default:
					item.setAlarmFired(false);
					break;
				}
				item.setKey(EncryptUtils.getUniqueId());
				item.setDisplayName(adapter.getDisplayName());
				item.setAlarmStatus(status);
				item.setAlarmValue(alarmValue);

				if (event instanceof FeedAlarmEvent) {
					item.setAlarmDate(((FeedAlarmEvent) event).getCreated());
					item.setAlarmOperator(((FeedAlarmEvent) event).getOperator());
					item.setAlarmMembers(((FeedAlarmEvent) event).getMembers());
				} else {
					item.setAlarmDate(event.getTimestamp());
				}

				if (adapter.getRenderer() == null) {
					item.setMeasureUnit(adapter.getMeasureUnit());
				}
				items.add(item);
			} else {
				ChannelAdapter item = new ChannelAdapter();
				AlarmStatus status = AlarmStatus.valueOf(event.getStatus());
				String message = "";
				switch (status) {
				case ON:
					item.setAlarmFired(true);
					message = AlarmStatus.ONLINE.name() + "->" + AlarmStatus.OFFLINE.name();
					break;
				case REENTER:
					item.setAlarmFired(false);
					message = AlarmStatus.OFFLINE.name() + "->" + AlarmStatus.ONLINE.name();
					break;
				default:
					break;
				}
				item.setAlarmStatus(status);
				item.setDisplayName(message);
				item.setKey(EncryptUtils.getUniqueId());
				if (event instanceof FeedAlarmEvent) {
					item.setAlarmDate(((FeedAlarmEvent) event).getCreated());
					item.setAlarmMembers(((FeedAlarmEvent) event).getMembers());
				} else {
					item.setAlarmDate(event.getTimestamp());
				}
				items.add(item);

			}
		}
		return items;

	}

}
