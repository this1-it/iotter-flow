package it.thisone.iotter.ui.channels;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.enums.ChartScaleType;
import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.enums.Period;
import it.thisone.iotter.enums.modbus.Permission;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.ui.charts.MultiTraceChartAdapter;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.common.charts.TimeIntervalHelper;
import it.thisone.iotter.ui.model.TimeInterval;
import it.thisone.iotter.ui.model.TimePeriod;
import it.thisone.iotter.ui.model.TimePeriod.TimePeriodEnum;

public class ChannelRemoteControlForm extends AbstractBaseEntityForm<Channel> {

	private static final long serialVersionUID = 5961351166441753740L;

	public ChannelRemoteControlForm(Channel item) {
		super(item, Channel.class, "channel.remote", null,null,false);
		getSaveButton().setVisible(false);
		getResetButton().setVisible(false);
		getDeleteButton().setVisible(false);
	}



	@Override
	public VerticalLayout getFieldsLayout() {
		Channel channel = getEntity();
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setPadding(true);
		mainLayout.setSizeFull();

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

		final MultiTraceChartAdapter chartAdapter = new MultiTraceChartAdapter(widget);
//		chartAdapter.setHeightFull();
//		chartAdapter.setWidthFull();

		TimeIntervalHelper helper = new TimeIntervalHelper(chartAdapter.getNetworkTimeZone());
		TimePeriod period = new TimePeriod(Period.HOUR, 1, TimePeriodEnum.LAST);
		TimeInterval interval = helper.period(new Date(), period);

		chartAdapter.setTimePeriod(period);
		chartAdapter.setTimeInterval(interval);
		chartAdapter.register();

		final ChannelRemoteControlField control = new ChannelRemoteControlField(channel);
		control.setLabel(getI18nLabel("change_setpoint"));

		control.addValueChangeListener(event -> {
			UI.getCurrent().access(() -> {
				control.setValidationError(null);
				chartAdapter.startDrawing();
			});
			ChannelRemoteUIRunnable runnable = new ChannelRemoteUIRunnable(chartAdapter, control,
					channel.getDefaultMeasure(), getPermission(channel));
			UI ui = UI.getCurrent();
			CompletableFuture.runAsync(() -> {
				Throwable error = null;
				try {
					runnable.runInBackground();
				} catch (Throwable ex) {
					error = ex;
				}
				Throwable uiError = error;
				ui.access(() -> runnable.runInUI(uiError));
			});
			channel.getRemote().setValue(control.getValue().getValue());
		});

		FormLayout layout = new FormLayout();
		layout.setWidthFull();
		layout.add(control);
		mainLayout.add(chartAdapter);
		mainLayout.setFlexGrow(1f, chartAdapter);
		mainLayout.add(layout);
		return mainLayout;
	}

	@Override
	protected void afterCommit() {
	}

	@Override
	protected void beforeCommit() throws EditorConstraintException {
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
	protected void initializeFields() {
		// TODO Auto-generated method stub
		
	}



	@Override
	protected void bindFields() {
		// TODO Auto-generated method stub
		
	}
}
