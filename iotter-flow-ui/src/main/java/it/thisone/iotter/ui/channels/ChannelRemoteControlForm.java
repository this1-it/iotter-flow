package it.thisone.iotter.ui.channels;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.enums.ChartScaleType;
import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.enums.Period;
import it.thisone.iotter.enums.modbus.Permission;
import it.thisone.iotter.mqtt.MqttServiceException;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.ui.charts.MultiTraceChartAdapter;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.common.charts.TimeIntervalHelper;
import it.thisone.iotter.ui.eventbus.DeviceChangedEvent;
import it.thisone.iotter.ui.eventbus.UIEventBus;
import it.thisone.iotter.ui.eventbus.WidgetRefreshEvent;
import it.thisone.iotter.ui.model.TimeInterval;
import it.thisone.iotter.ui.model.TimePeriod;
import it.thisone.iotter.ui.model.TimePeriod.TimePeriodEnum;
import it.thisone.iotter.ui.providers.BackendServices;


public class ChannelRemoteControlForm extends AbstractBaseEntityForm<Channel> {

	private static final long serialVersionUID = 5961351166441753740L;
	private static final Logger logger = LoggerFactory.getLogger(ChannelRemoteControlForm.class);

	private static final int LOOP_COUNT = 6;
	private static final int LOOP_PAUSE = 5;

	private final BackendServices backendServices;
	private final UIEventBus uiEventBus;

	private VerticalLayout chartContainer;
	private FormLayout controlContainer;

	public ChannelRemoteControlForm(Channel item, BackendServices backendServices, UIEventBus uiEventBus) {
		super(item, Channel.class, "channel.remote", null, null, false);
		this.backendServices = backendServices;
		this.uiEventBus = uiEventBus;
		getSaveButton().setVisible(false);
		getResetButton().setVisible(false);
		getDeleteButton().setVisible(false);
	}

	@Override
	public VerticalLayout getFieldsLayout() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setPadding(true);
		mainLayout.setSizeFull();

		chartContainer = new VerticalLayout();
		chartContainer.setSizeFull();

		controlContainer = new FormLayout();
		controlContainer.setWidthFull();

		mainLayout.add(chartContainer);
		mainLayout.setFlexGrow(1f, chartContainer);
		mainLayout.add(controlContainer);

		return mainLayout;
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		buildChartAndControls();
	}

	private void buildChartAndControls() {
		Channel channel = getEntity();

		GraphicWidget widget = new GraphicWidget();
		widget.setType(GraphicWidgetType.MULTI_TRACE);
		widget.setLabel(channel.getConfiguration().getDisplayName());
		widget.getOptions().setScale(ChartScaleType.LINEAR);
		widget.getOptions().setShowGrid(false);
		widget.getOptions().setExporting(false);
		widget.getOptions().setRealTime(true);

		GraphicFeed feed = new GraphicFeed();
		feed.getOptions().setFillColor(ChartUtils.hexColor(0));
		feed.setChannel(channel);
		feed.setMeasure(channel.getDefaultMeasure());
		widget.addFeed(feed);

		final MultiTraceChartAdapter chartAdapter = new MultiTraceChartAdapter(widget, backendServices);

		TimeIntervalHelper helper = new TimeIntervalHelper(chartAdapter.getNetworkTimeZone());
		TimePeriod period = new TimePeriod(Period.HOUR, 1, TimePeriodEnum.LAST);
		TimeInterval interval = helper.period(new Date(), period);

		chartAdapter.setTimePeriod(period);
		chartAdapter.setTimeInterval(interval);
		chartAdapter.register();

		chartContainer.add(chartAdapter);

		final ChannelRemoteControlField control = new ChannelRemoteControlField(channel);
		control.initLastMeasure(backendServices.getCassandraFeeds());
		control.setLabel(getI18nLabel("change_setpoint"));

		control.addValueChangeListener(event -> {
			UI ui = UI.getCurrent();
			ui.access(() -> {
				control.setValidationError(null);
				control.setEnabled(false);
				chartAdapter.startDrawing();
			});
			CompletableFuture.runAsync(() -> {
				String errorMessage = executeRemoteControl(channel, control, chartAdapter);
				ui.access(() -> {
					control.setEnabled(true);
					control.setValidationError(errorMessage);
				});
			});
			channel.getRemote().setValue(control.getValue().getValue());
		});

		controlContainer.add(control);
	}

	private String executeRemoteControl(Channel channel, ChannelRemoteControlField remote,
			MultiTraceChartAdapter chartAdapter) {
		if (remote.getValue() == null || remote.getValue().getValue() == null) {
			return "remote value is empty";
		}

		Permission permission = getPermission(channel);
		if (permission == Permission.READ) {
			return remote.getTranslation("mqtt.setvalue.remote_control_not_issued");
		}

		MeasureUnit unit = channel.getDefaultMeasure();
		BigDecimal value = unit.calculateRaw(remote.getValue().getValue().floatValue());

		if (remote.getValue().getTopic() == null || remote.getValue().getTopic().isEmpty()) {
			return remote.getTranslation("remote_control_not_issued");
		}

		try {
			backendServices.getMqttService().setValue(remote.getValue().getTopic(), value);

			boolean done = false;
			if (permission.equals(Permission.READ_WRITE)) {
				for (int i = 0; i <= LOOP_COUNT; i++) {
					try {
						Thread.sleep(LOOP_PAUSE * 1000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
					FeedKey feedKey = new FeedKey(remote.getSerial(), remote.getKey());
					MeasureRaw measure = ChartUtils.lastMeasure(feedKey, backendServices.getCassandraFeeds());
					if (measure != null && measure.getValue() != null
							&& measure.getValue().equals(value.floatValue())) {
						done = true;
						break;
					}
				}
			} else {
				done = true;
			}

			if (!done) {
				return remote.getTranslation("remote_control_accepted_but_unsuccessfull");
			}

			chartAdapter.startDrawing();
			uiEventBus.post(new WidgetRefreshEvent());
			uiEventBus.post(new DeviceChangedEvent(remote.getSerial()));
			return null;

		} catch (MqttServiceException e) {
			logger.error("MQTT remote control failed for channel {}", channel.getKey(), e);
			return remote.getTranslation("remote_control_not_accepted");
		}
	}

	private Permission getPermission(Channel channel) {
		Permission permission = Permission.READ_WRITE;
		if (channel.getRemote().getPermission() != null) {
			String literal = channel.getRemote().getPermission().toUpperCase();
			for (Permission value : Permission.values()) {
				if (value.getShortName().equals(literal)) {
					return value;
				}
			}
		}
		return permission;
	}

	@Override
	protected void afterCommit() {
	}

	@Override
	protected void beforeCommit() throws EditorConstraintException {
	}

	@Override
	protected void initializeFields() {
	}

	@Override
	protected void bindFields() {
	}
}
