package it.thisone.iotter.ui.charts;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.google.common.collect.Range;
import com.google.common.eventbus.Subscribe;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.AbstractPlotOptions;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.DashStyle;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.Marker;
import com.vaadin.addon.charts.model.PlotOptionsAreaspline;
import com.vaadin.addon.charts.model.PlotOptionsColumn;
import com.vaadin.addon.charts.model.PlotOptionsSpline;
import com.vaadin.addon.charts.model.PointOptions;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.shared.Registration;

import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.enums.ExportFileMode;
import it.thisone.iotter.exceptions.MeasureException;
import it.thisone.iotter.exporter.ExportConfig;
import it.thisone.iotter.exporter.ExportProperties;
import it.thisone.iotter.exporter.IExportable;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChartPlotOptions;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GraphicWidgetOptions;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.ui.charts.controls.GraphicWidgetOptionsField;
import it.thisone.iotter.ui.charts.controls.TimeControl;
import it.thisone.iotter.ui.charts.controls.TimeIntervalField;
import it.thisone.iotter.ui.charts.controls.TimeLastMeasureButton;
import it.thisone.iotter.ui.charts.controls.TimePeriodPopup;
import it.thisone.iotter.ui.common.AbstractWidgetVisualizer;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.WidgetRefreshUIRunnable;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.common.charts.CustomMarkerSymbolEnum;
import it.thisone.iotter.ui.common.export.ExportDialog;
import it.thisone.iotter.ui.eventbus.GraphWidgetOptionsEvent;
import it.thisone.iotter.ui.eventbus.TimeIntervalEvent;
import it.thisone.iotter.ui.eventbus.TimePeriodEvent;
import it.thisone.iotter.ui.eventbus.WidgetRefreshEvent;
import it.thisone.iotter.ui.ifc.IGroupWidgetUiFactory;
import it.thisone.iotter.ui.main.IMainUI;
import it.thisone.iotter.ui.model.TimeInterval;
import it.thisone.iotter.ui.model.TimePeriod;
import it.thisone.iotter.ui.uitask.UIRunnable;

/*
 * http://www.highcharts.com/demo/dynamic-master-detail/grid
 * http://www.highcharts.com/demo/combo-meteogram/grid
 * http://www.highcharts.com/demo/heatmap-canvas
 */


// Bug #1960
// AbstractChartAdapter : useless layout in createContentWrapper


public abstract class AbstractChartAdapter extends AbstractWidgetVisualizer implements IExportable {
	// TODO(flow-migration): manual refactor still needed here for IMainUI/UIUtils legacy access,
	// dialog/window handling, and Vaadin 8 layout APIs.

	private static Logger logger = LoggerFactory.getLogger(AbstractChartAdapter.class);

	public static final int DAY_IN_MILLIS = 24 * 3600 * 1000;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	private WidgetRefreshUIRunnable refreshRunnable;
	private ChartDrawIntervalUIRunnable drawRunnable;

	protected ProgressBar progressBar;
	protected Button controlButton;
	protected Button exportButton;
	protected Button expandButton;
	protected TimeControl timeControl;
	protected TimePeriodPopup periodField;

	protected TimeIntervalField intervalField;
	protected IGroupWidgetUiFactory config;

	protected GraphicWidgetOptionsField optionsField;
	private Map<String, List<Range<Date>>> validities;
	private HorizontalLayout controlsLayout;
	private HorizontalLayout toolLayout;
	private VerticalLayout contentLayout;
	private Future<?> future;
	private TimeZone networkTimeZone;

	private Registration intervalChangeRegistration;



	public AbstractChartAdapter(GraphicWidget widget) {
		super(widget);
		
		//config = UIUtils.getUiFactory().getGroupWidgetUiFactory();
		// ChartUtils.loadCustomMarkers();
		optionsField = new GraphicWidgetOptionsField();

		if (widget.getGroupWidget() != null) {
			networkTimeZone = widget.getGroupWidget().getTimeZone();
		}

		if (networkTimeZone == null) {
			networkTimeZone = UIUtils.getBrowserTimeZone();
		}

		int width = (int) (getCanonicalWindowWidth() * 0.8f);
		boolean compact = Math.round(getWidget().getWidth() * getCanonicalWindowWidth()) < width;

		intervalField = new TimeIntervalField(networkTimeZone, config.getPeriods());
		intervalField.setCompact(compact);
		periodField = new TimePeriodPopup();
		periodField.setCompact(compact);

		String lastMeasureCaption = getI18nLabel("lastMeasure");
		timeControl = new TimeControl(intervalField, periodField, new TimeLastMeasureButton(lastMeasureCaption, this));
		validities = ChartUtils.getValidities(widget);
		Component visualization = buildVisualization();
		if (visualization instanceof HasSize) {
			((HasSize) visualization).setSizeFull();
		}

		if (widget.hasExtremes()) {
			getGraphWidget().getOptions().setAutoScale(false);
		}

		optionsField.setValue(getGraphWidget().getOptions());
		optionsField.addValueChangeListener(event->{
			setGraphWidgetOptionsOnChange(event.getValue());
		});


		intervalChangeRegistration = intervalField.addValueChangeListener(event -> {
		    drawInterval(event.getValue());
		});
		
		periodField.addValueChangeListener(event->{
			TimePeriod period = event.getValue();
			if (optionsField.getValue().getRealTime()) {
				intervalField.setValue(intervalField.getHelper().lastTimePeriod(period));
			}			
		});



		timeControl.setEnabled(widget.getOptions().getLocalControls());
		contentLayout = createContentWrapper(visualization);
		getContent().removeAll();
		getContent().add(contentLayout);
		refreshRunnable = new WidgetRefreshUIRunnable(AbstractChartAdapter.this);
		drawRunnable = new ChartDrawIntervalUIRunnable(AbstractChartAdapter.this);
		register();

	}
	
	protected void changeRealTimeInterval(TimeInterval interval) {
		intervalChangeRegistration.remove();
		intervalField.setValue(interval);
		intervalChangeRegistration = intervalField.addValueChangeListener(event -> {
		    drawInterval(event.getValue());
		});
	}

	abstract protected void createChartConfiguration(TimeInterval interval);

	abstract protected void setGraphWidgetOptionsOnChange(GraphicWidgetOptions options);

	abstract protected boolean isRealTime();

	abstract protected boolean changedRealTime(GraphicWidgetOptions options);

	public abstract Chart getChart();

	abstract protected void setChart(Chart chart);

	
	protected VerticalLayout createContentWrapper(Component visualization) {
//		HorizontalLayout titleLayout = new HorizontalLayout();
//		titleLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		Span title = new Span(getGraphWidget().getLabel());
		title.addClassName("responsive-title");
//		titleLayout.addComponent(title);
//		titleLayout.setExpandRatio(title, 1f);

		progressBar = new ProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);
//		titleLayout.addComponent(progressBar);

		toolLayout = buildTools();

		HorizontalLayout header = new HorizontalLayout();
		header.setWidthFull();
		header.setAlignItems(Alignment.CENTER);
		header.add(title, progressBar, toolLayout);
		header.setFlexGrow(1f, title);

		controlsLayout = buildTimeControls();
		controlsLayout.setVisible(false);
		VerticalLayout topLayout = new VerticalLayout();
		topLayout.setSpacing(true);
		topLayout.setPadding(false);
		topLayout.setWidthFull();
		topLayout.setAlignItems(Alignment.CENTER);
		topLayout.add(header, controlsLayout);

//		VerticalLayout contentLayout = new VerticalLayout();
//		contentLayout.setSizeFull();
//		contentLayout.setMargin(false);
//		contentLayout.addComponent(visualization);
//		contentLayout.setExpandRatio(visualization, 1);

		HorizontalLayout bottomLayout = new HorizontalLayout();
		bottomLayout.setSizeFull();

		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setPadding(false);
		layout.add(topLayout, visualization, bottomLayout);

		layout.setFlexGrow(1f, visualization);
		layout.setHorizontalComponentAlignment(Alignment.CENTER, topLayout);
		layout.setHorizontalComponentAlignment(Alignment.CENTER, visualization);

		layout.addClassName("graph-widget");
		topLayout.addClassName("graph-widget-header");
		//contentLayout.addStyleName("graph-widget-content");
		//visualization.addClassName("graph-widget-content");
		bottomLayout.addClassName("graph-widget-footer");
		//contentLayout.setResponsive(true);

		VerticalLayout outer = new VerticalLayout();
		outer.setSizeFull();
		outer.addClassName("graph-widget-outer");
		outer.add(layout);
		return outer;
	}

	private HorizontalLayout buildTools() {

		exportButton = createExportButton();
		expandButton = createExpandButton();
		controlButton = createControlButton();

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setAlignItems(Alignment.CENTER);

		if (getGraphWidget().getOptions().getShowControls()) {
			timeControl.setControlButton(controlButton);
			buttons.add(controlButton, exportButton);
		}

		return buttons;
	}

	private Button createControlButton() {
		Button controlButton = new Button();
		controlButton.addClassName("small");
		controlButton.setIcon(VaadinIcon.COG.create());
		controlButton.addClickListener(event -> controlsLayout.setVisible(!controlsLayout.isVisible()));
		return controlButton;
	}

	private Button createExpandButton() {
		final Button expandButton = new Button();
		expandButton.addClassName("small");
		expandButton.setIcon(VaadinIcon.EXPAND.create());
//		expandButton.addClickListener(event -> {
//			if (!getClassNames().contains("max")) {
//				expandButton.setIcon(VaadinIcon.COMPRESS.create());
//				toggleMaximized(true);
//			} else {
//				removeClassName("max");
//				expandButton.setIcon(VaadinIcon.EXPAND.create());
//				toggleMaximized(false);
//			}
//		});
		return expandButton;
	}

	private Button createExportButton() {
		Button exportButton = new Button();
		exportButton.addClassName("small");
		exportButton.setIcon(VaadinIcon.DOWNLOAD.create());
		exportButton.setEnabled(getGraphWidget().getOptions().getLocalControls());
		// exportButton.addStyleName(UIUtils.DISPLAY_1024PX_STYLE);
		// Responsive.makeResponsive(exportButton);
		exportButton.addClickListener(event -> {
			ExportDialog dialog = new ExportDialog(
					createExportConfig(),
					createExportProperties(),
					null,
					getBackgroundExecutor());
			UI.getCurrent().add(dialog);
			dialog.open();
		});
		return exportButton;
	}

	protected void toggleMaximized(boolean maximized) {
		// TODO(flow-migration): replace this legacy maximize behavior with a dedicated
		// Flow layout strategy (no AbsoluteLayout/css-position based relocation).
		Optional<Component> parent = getParent();
		parent.ifPresent(p -> p.getChildren().forEach(c -> {
			if (c instanceof AbstractWidgetVisualizer && c != this) {
				c.setVisible(!maximized);
			}
		}));
//		if (maximized) {
//			addClassName("max");
//			setWidthFull();
//		} else {
//			removeClassName("max");
//			calculateCssPosition();
//		}
	}

	private HorizontalLayout buildTimeControls() {
		HorizontalLayout controls = new HorizontalLayout();
//		controls.addClassName(UIUtils.TIME_BUTTONS_STYLE);
//		controls.addClassName(UIUtils.TIME_CONTROLS_STYLE);
		timeControl.getButtonsLayout().add(optionsField);
		controls.add(timeControl);
		return controls;
	}

	/*
	 * initializes timeinterval and fire event for drawing charts
	 */
	protected void initTimeInterval() {
		if (intervalField.getValue() == null) {
			intervalField.setValue(intervalField.getHelper().currentDay(new Date()));
		}
	}

	/**
	 * managed by value change listener
	 * 
	 * @param interval
	 */
	public void setTimeInterval(TimeInterval interval) {
		intervalField.setValue(interval);
	}

	/**
	 * managed by TimePeriodEvent
	 * 
	 * @param period
	 */
	public void setTimePeriod(TimePeriod period) {
		periodField.setValue(period);
	}

	/**
	 * managed by GraphWidgetOptionsEvent
	 * 
	 * @param options
	 */
	public void setGraphWidgetOptions(GraphicWidgetOptions value) {
		optionsField.setValue(value);
	}

	public GraphicWidgetOptions getGraphWidgetOptions() {
		return optionsField.getValue();
	}

	
	public GraphicWidget getGraphWidget() {
		return (GraphicWidget) getWidget();
	}


	protected DataSeries getTimeSeries(GraphicFeed feed, TimeInterval interval, boolean configure, Float ratio) {
		DataSeries series = new DataSeries();
		series.setId(feed.getKey());

		if (feed.getChannel() == null) {
			return series;
		}
		
		Date from = interval.getStartDate();
		Date to = interval.getEndDate();
		Date now = new Date();
		if (to.after(now)) {
			to = now;
		}
		
		if (to.before(from)) {
			return series;
		}
		
		
		Map<Number, String> errors = new HashMap<Number, String>();
		int points = Math.max(1, (int) Math.ceil(getWidget().getWidth() * 1000f));
		if (ratio != null) {
			// real time
			points = (int) Math.ceil(points * ratio);
		}
		int step = 0;
		
		if (points > 0) {
			step = (int) (to.getTime() -from.getTime()) / points;
		}
		
		List<MeasureRaw> degrees = null;
		MeasureUnit degreesMeausure = null;
		List<Range<Date>> ranges = getValidities().get(feed.getKey());
		FeedKey feedKey = new FeedKey(feed.getDevice().getSerial(), feed.getKey());
		feedKey.setQualifier(feed.getChannel().getConfiguration().getQualifier());
		List<MeasureRaw> measures = ChartUtils.getData(feedKey, from, to, points,step,
				ranges, getNetworkTimeZone());

		if (feed.getOptions().hasMarkerReference()) {
			feedKey = new FeedKey(feed.getDevice().getSerial(), feed.getOptions().getFeedReference());
			degrees = ChartUtils.getData(feedKey, interval.getStartDate(), interval.getEndDate(), points, step, ranges,
					getNetworkTimeZone());
			degreesMeausure = findMeasureUnit(feed.getOptions().getFeedReference(), feed);
			// TODO handle different list size as error
			if (degrees.size() < measures.size()) {
				int diff = measures.size() - degrees.size();
				for (int i = 0; i < diff; i++) {
					degrees.add(new MeasureRaw(new Date(), 0f, "missing degrees"));
				}
			}
		}
		if (configure) {
			String feedLabel = ChartUtils.getFeedLabel(feed);
			AbstractPlotOptions plotOptions = getPlotOptions(feed);
			series.setPlotOptions(plotOptions);
			series.setName(feedLabel);
		}
		int index = 0;
		Number timestamp = null;
		for (MeasureRaw measure : measures) {
			DataSeriesItem item = new DataSeriesItem(measure.getDate(), measure.getValue());
			timestamp = ChartUtils.toHighchartsTS(measure.getDate(), getNetworkTimeZone());
			item.setX(timestamp);

			series.add(item);
			if (measure.hasError()) {
				errors.put(item.getX(), measure.getError());
			}
			try {
				item.setY(ChartUtils.calculateMeasure((Float) item.getY(), feed.getMeasure()));
				if (degrees != null) {
					Number angle = ChartUtils.calculateAngle(degrees.get(index).getValue(), degreesMeausure);
					if (angle != null) {
						ChartUtils.setArrowMarker(item, angle);
					}
				}
			} catch (MeasureException e) {
				if (errors.containsKey(timestamp)) {
					errors.put(item.getX(), e.getMessage());
				}
			}

			index++;
		}

		ChartUtils.showErrors(AbstractWidgetVisualizer.NAME, series, errors, getNetworkTimeZone());

		return series;
	}

	/**
	 * default spline plot option
	 * 
	 * @param options
	 * @return
	 */
	protected AbstractPlotOptions getPlotOptions(GraphicFeed feed) {
		ChartPlotOptions options = feed.getOptions();
		boolean enableMarkers = feed.getWidget().getOptions().getShowMarkers();
		if (options.getChartType() == null) {
			options.setChartType(ChartType.LINE.toString());
		}
		if (options.getFillColor() == null) {
			options.setFillColor(ChartUtils.quiteRandomHexColor());
		}
		if (options.getDashStyle() == null) {
			options.setDashStyle(DashStyle.SOLID.name());
		}
		AbstractPlotOptions plotOptions = null;
		if (options.getChartType().equals(ChartType.COLUMN.toString())) {
			PlotOptionsColumn plotColumn = new PlotOptionsColumn();
			plotColumn.setColor(new SolidColor(options.getFillColor()));
			plotOptions = plotColumn;
		} else {
			if (options.getChartType().equals(ChartType.AREASPLINE.toString())) {
				PlotOptionsAreaspline plotAreaspline = new PlotOptionsAreaspline();
				plotAreaspline.setColor(new SolidColor(options.getFillColor()));
				plotAreaspline.setLineWidth(ChartUtils.PLOT_LINE_WIDTH);
				plotAreaspline.setShadow(false);
				
				if (options.getMarkerSymbol() != null) {
					Marker marker = new Marker(enableMarkers);
					marker.setSymbol(CustomMarkerSymbolEnum.valueOf(options.getMarkerSymbol()));
					plotAreaspline.setMarker(marker);
				}
				plotAreaspline.getTooltip().setValueDecimals(feed.getMeasure().getDecimals());
				String xDateFormat = ChartUtils.X_DATEFORMAT + " - " + feed.getChannel().getDevice().getLabel();
				plotAreaspline.getTooltip().setXDateFormat(xDateFormat);

				plotOptions = plotAreaspline;
			} else {
				PointOptions plotSpline = new PlotOptionsSpline();
				plotSpline.setLineWidth(ChartUtils.PLOT_LINE_WIDTH);
				plotSpline.setShadow(false);
				if (options.getFillColor() != null) {
					plotSpline.setColor(new SolidColor(options.getFillColor()));
				}
				plotSpline.setDashStyle(DashStyle.valueOf(options.getDashStyle()));
				if (options.getMarkerSymbol() != null) {
					Marker marker = new Marker(enableMarkers);
					marker.setSymbol(CustomMarkerSymbolEnum.valueOf(options.getMarkerSymbol()));
					if (!marker.getSymbol().equals(CustomMarkerSymbolEnum.ARROW)) {
						marker.setRadius(ChartUtils.MARKER_RADIUS);
					}
					plotSpline.setMarker(marker);
				}

				plotSpline.setAnimation(false);
				plotSpline.getTooltip().setValueDecimals(feed.getMeasure().getDecimals());
				String xDateFormat = ChartUtils.X_DATEFORMAT + " - " + feed.getChannel().getDevice().getLabel();
				plotSpline.getTooltip().setXDateFormat(xDateFormat);

				// linePlotOptions.getTooltip().setUseHTML(true);

				plotOptions = plotSpline;
			}

		}

		// String pointFormat =
		// "<span
		// style='color:{series.color}'>{series.name}</span>:<b>{point.y}</b><br/>";
		// plotOptions.getTooltip().setPointFormat(pointFormat);

		return plotOptions;
	}

	protected void optimizePlotOptions(AbstractPlotOptions plotOptions) {
		// if (plotOptions instanceof AbstractLinePlotOptions) {
		// // Showing points with thousands of data items looks odd (on top of
		// // each other),
		// // also renders faster without markers
		// ((AbstractLinePlotOptions) plotOptions)
		// .setMarker(new Marker(false));
		// // To render shadow, library must create additional element, without
		// // it
		// // performance will be better
		// ((AbstractLinePlotOptions) plotOptions).setShadow(false);
		// }
		/*
		 * If developers need to use large data sets and point specific
		 * settings, they can override the default turbo threshold. Here we set
		 * it to 200000 (default 1000). Turbo threshold is Highcharts related
		 * configuration that works as a "sanity threshold" so that old browsers
		 * wont drop to their knees under load. Without this Highcharts might
		 * not render chart if data items has e.g. name set.
		 */
		// if (plotOptions instanceof AbstractPointPlotOptions) {
		// ((AbstractPointPlotOptions) plotOptions).setTurboThreshold(200000);
		// }
		// plotOptions.setAnimation(false);
	}

	protected MeasureUnit findMeasureUnit(String feedKey, GraphicFeed feed) {
		Channel chnl = feed.getChannel();
		if (chnl == null)
			return null;
		Device device = chnl.getDevice();
		for (Channel channel : device.getChannels()) {
			if (feedKey.equals(channel.getKey())) {
				return channel.getDefaultMeasure();
			}
		}
		return null;
	}

	/**
	 * executes before change real time
	 * 
	 * @param options
	 */
	protected void changedLocalControls(GraphicWidgetOptions options) {
		boolean changeLocalControls = getGraphWidget().getOptions().getLocalControls() != options.getLocalControls();
		if (changeLocalControls) {
			exportButton.setEnabled(options.getLocalControls() && !options.getRealTime());
			timeControl.setEnabled(options.getLocalControls() && !options.getRealTime());
			timeControl.activeLocalControl(options.getLocalControls() && !options.getRealTime());
			getGraphWidget().getOptions().setLocalControls(options.getLocalControls());
			// disable real time if local control is false
			if (getGraphWidget().getOptions().getRealTime() && !options.getLocalControls()) {
				options.setRealTime(false);
			}
		}
	}

	public Map<String, List<Range<Date>>> getValidities() {
		return validities;
	}

	public TimeZone getNetworkTimeZone() {
		return networkTimeZone;
	}

	private ExportProperties createExportProperties() {
		ExportProperties props = new ExportProperties();
		props.setTimeZone(networkTimeZone);
		props.setFileMode(ExportFileMode.SINGLE);
		props.setLocale(UIUtils.getLocale());
		return props;
	}

	public ExportConfig createExportConfig() {
		ExportConfig config = new ExportConfig();
		Range<Date> range = Range.closed(intervalField.getValue().getStartDate(),
				intervalField.getValue().getEndDate());
		config.setInterval(range);
		config.setName(getWidget().getLabel());
		List<GraphicFeed> feeds = ((GraphicWidget) getWidget()).getFeeds();
		config.setFeeds(ChartUtils.createExportFeeds(feeds));
		config.setInterpolation(Interpolation.RAW);
		return config;
	}

	@Subscribe
	public void changeTimeInterval(final TimeIntervalEvent event) {
		if (getGraphWidget().getGroupWidget().getId().toString().equals(event.getSource())) {
			logger.debug("{} received TimeIntervalEvent ", getWidget().getLabel());
//			UI.getCurrent().access(new Runnable() {
//				@Override
//				public void run() {
//					setTimeInterval(event.getInterval());
//				}
//			});
		}
	}

	@Subscribe
	public void changedPeriod(final TimePeriodEvent event) {
		if (getGraphWidget().getGroupWidget().getId().toString().equals(event.getSource())) {
			logger.debug("{} received TimePeriodEvent ", getWidget().getLabel());
			if (!getGraphWidgetOptions().getLocalControls()) {
//				UI.getCurrent().access(new Runnable() {
//					@Override
//					public void run() {
//						setTimePeriod(event.getPeriod());
//					}
//				});
			}
		}
	}

	@Subscribe
	public void changedOptions(final GraphWidgetOptionsEvent event) {
		if (getGraphWidget().getGroupWidget().getId().toString().equals(event.getSource())) {
			logger.debug("{} received GraphWidgetOptionsEvent ", getWidget().getLabel());
			if (!getGraphWidgetOptions().getLocalControls()) {
				try {
//					UI.getCurrent().access(new Runnable() {
//						@Override
//						public void run() {
//							GraphicWidgetOptions value = new GraphicWidgetOptions();
//							BeanUtils.copyProperties(optionsField.getValue(), value);
//							if (event.getRealTime() != null) {
//								value.setRealTime(event.getRealTime());
//							}
//							if (event.getAutoScale() != null) {
//								value.setAutoScale(event.getAutoScale());
//							}
//							if (event.getScale() != null) {
//								value.setScale(event.getScale());
//							}
//							if (event.getShowGrid() != null) {
//								value.setShowGrid(event.getShowGrid());
//							}
//							if (event.getShowMarkers() != null) {
//								value.setShowMarkers(event.getShowMarkers());
//							}
//							setGraphWidgetOptions(value);
//						}
//					});
				} catch (Throwable e) {
				}
			}
		}
	}

	public void startDrawing() {
		progressBar.setVisible(true);
		//addClassName("draw-chart");
	}

	public void endDrawing() {
		progressBar.setVisible(false);
		//removeClassName("draw-chart");
	}

	// Feature #201 (In Progress): Chart real time managed with asynchronous
	// event better then refresher
	@Override
	@Subscribe
	public void refreshWithUiAccess(final WidgetRefreshEvent event) {
		if (isRealTime()) {
			if (future != null) {
				future.cancel(true);
			}
			logger.debug("{} received WidgetRefreshEvent ", getWidget().getLabel());
			future = executeAndAccess(refreshRunnable);
		}
	}

	private void drawInterval(TimeInterval interval) {
		if (future != null) {
			future.cancel(true);
		}
		drawRunnable.setInterval(interval);
		future = executeAndAccess(drawRunnable);
	}

	private int getCanonicalWindowWidth() {
		UI ui = UI.getCurrent();
		if (ui instanceof IMainUI) {
			return ((IMainUI) ui).getCanonicalWindowWidth();
		}
		return 1200;
	}

	private Executor getBackgroundExecutor() {
//		UI ui = UI.getCurrent();
//		if (ui instanceof IMainUI) {
//			return ((IMainUI) ui).getThreadPoolExecutor();
//		}
		return Runnable::run;
	}

	private Future<?> executeAndAccess(UIRunnable runnable) {
		UI ui = UI.getCurrent();
		if (ui == null) {
			return CompletableFuture.completedFuture(null);
		}
		return CompletableFuture
				.runAsync(runnable::runInBackground)
				.whenComplete((unused, throwable) -> ui.access(() -> runnable.runInUI(throwable)));
	}

	

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		logger.debug("attach {}", getGraphWidget().getLabel());
		super.onAttach(attachEvent);
	}

	@Override
	protected void onDetach(DetachEvent detachEvent) {
		logger.debug("detach {}", getGraphWidget().getLabel());
		super.onDetach(detachEvent);
		if (future != null) {
			future.cancel(true);
			future = null;
		}
	}

	public HorizontalLayout getToolLayout() {
		return toolLayout;
	}

	public VerticalLayout getContentLayout() {
		return contentLayout;
	}

	public Date lastMeasureDate() {
		return new Date();
	}

	public void setValidities(Map<String, List<Range<Date>>> validities) {
		this.validities = validities;
	}

	public TimeIntervalField getIntervalField() {
		return intervalField;
	}

}
