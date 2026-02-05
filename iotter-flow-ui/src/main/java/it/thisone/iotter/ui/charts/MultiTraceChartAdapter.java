package it.thisone.iotter.ui.charts;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.addon.charts.Chart;
//import com.vaadin.addon.charts.ChartSelectionEvent;
//import com.vaadin.addon.charts.ChartSelectionListener;
//import com.vaadin.addon.charts.LegendItemClickEvent;
//import com.vaadin.addon.charts.LegendItemClickListener;
//import com.vaadin.addon.charts.XAxesExtremesChangeEvent;
//import com.vaadin.addon.charts.XAxesExtremesChangeListener;
import com.vaadin.addon.charts.model.AbstractPlotOptions;
import com.vaadin.addon.charts.model.AxisTitle;
import com.vaadin.addon.charts.model.AxisType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.DateTimeLabelFormats;
import com.vaadin.addon.charts.model.Label;
import com.vaadin.addon.charts.model.Labels;
import com.vaadin.addon.charts.model.LayoutDirection;
import com.vaadin.addon.charts.model.Legend;
import com.vaadin.addon.charts.model.PlotLine;
import com.vaadin.addon.charts.model.PointOptions;
import com.vaadin.addon.charts.model.Series;
import com.vaadin.addon.charts.model.Title;
import com.vaadin.addon.charts.model.VerticalAlign;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.charts.model.ZoomType;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.addon.charts.model.style.Style;
import com.vaadin.addon.charts.model.style.StylePosition;
import com.vaadin.flow.component.Component;

import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.persistence.model.ChartThreshold;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GraphicWidgetOptions;
import it.thisone.iotter.persistence.model.MeasureRange;
import it.thisone.iotter.ui.common.charts.ChannelUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.model.TimeInterval;

	// TODO(flow-migration): this class still contains Vaadin 8 APIs and needs manual Flow refactor.
public class MultiTraceChartAdapter extends AbstractChartAdapter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5817286715447264313L;
	private static Logger logger = LoggerFactory.getLogger(MultiTraceChartAdapter.class);
	private Map<String, Date> refreshed = new HashMap<String, Date>();
	private List<String> hidden = new ArrayList<String>();
	private boolean movedRealtime;
	private Chart chart;
	private String masterGrid;
	//private ChartSelectionListener chartSelectionListener;

	public MultiTraceChartAdapter(GraphicWidget widget) {
		super(widget);
		optionsField.getAutoScale().setVisible(widget.hasExtremes());
	}

	@Override
	protected Component buildVisualization() {

		Chart chart = new Chart();
		chart.setId(String.valueOf(getWidget().getId()));

		Configuration configuration = chart.getConfiguration();
		configuration.getChart().setShowAxes(true);
		configuration.setExporting(getGraphWidget().getOptions().getExporting());
		configuration.getExporting().setWidth(800);

		Title chartTitle = new Title(getWidget().getLabel());
		chartTitle = new Title("");
		chartTitle.setFloating(true);
		Style cstyle = new Style();
		cstyle.setPosition(StylePosition.ABSOLUTE);
		cstyle.setFontSize("0px");
		configuration.setTitle(chartTitle);

		XAxis xAxis = new XAxis();
		xAxis.setType(AxisType.DATETIME);
		xAxis.setDateTimeLabelFormats(
				new DateTimeLabelFormats(ChartUtils.MONTH_DATEFORMAT, ChartUtils.YEAR_DATEFORMAT));
		// xAxis.setMinRange(5 * 60 * 1000);

		float gridLineWidth = getGraphWidget().getOptions().getShowGrid() ? (Float) ChartUtils.GRID_LINE_WIDTH : 0f;
		xAxis.setGridLineWidth(gridLineWidth);
		xAxis.setMinorGridLineWidth((double) gridLineWidth / 2);
		configuration.addxAxis(xAxis);

		configureMultipleYAxis(configuration);

		Legend legend = new Legend();
		legend.setLayout(LayoutDirection.HORIZONTAL);
		legend.setVerticalAlign(VerticalAlign.BOTTOM);
		configuration.setLegend(legend);
		legend.setEnabled(getGraphWidget().getOptions().getShowLegend());

		configuration.getTooltip().setXDateFormat(ChartUtils.X_DATEFORMAT);
		configuration.getTooltip().setUseHTML(true);

		// Bug #1961
		ZoomType zoomType = getGraphWidget().getOptions().getRealTime() ? null : ZoomType.X;
		configuration.getChart().setZoomType(zoomType);
		
		//configuration.getChart().setZoomType(ZoomType.X);
		
//		setChartSelectionListener(createChartSelectionListener());
//		chart.addChartSelectionListener(getChartSelectionListener());
//
//		chart.addLegendItemClickListener(createLegendItemClickListener());
//		chart.setSeriesVisibilityTogglingDisabled(false);

		chart.drawChart(configuration);
		// chart.setImmediate(true);
		setChart(chart);
		return chart;
	}

	private void configureMultipleYAxis(Configuration configuration) {
		if (getGraphWidget().hasExtremes()) {
			configuration.getChart().setAlignTicks(false);
			for (GraphicFeed feed : getGraphWidget().getFeeds()) {
				MeasureRange extremes = feed.getOptions().getExtremes();
				if (extremes != null) {
					masterGrid = feed.getKey();
					break;
				}
			}
		}

		AxisType yAxisType = AxisType.LINEAR;
		switch (getGraphWidget().getOptions().getScale()) {
		case LOGARITHMIC:
			yAxisType = AxisType.LOGARITHMIC;
			break;
		default:
			break;
		}

		float gridLineWidth = (float) configuration.getxAxis().getGridLineWidth();

		for (int i = 0; i < getGraphWidget().getFeeds().size(); i++) {
			GraphicFeed feed = getGraphWidget().getFeeds().get(i);
			String feedColor = feed.getOptions().getFillColor();
			if (feedColor == null) {
				feedColor = ChartUtils.quiteRandomHexColor();
			}
			String feedLabel = ChartUtils.getFeedLabel(feed);
			YAxis yAxis = new YAxis();
			yAxis.setId(feed.getKey());
			yAxis.setType(yAxisType);
			if (yAxisType.equals(AxisType.LOGARITHMIC))
				yAxis.setMinorTickInterval("0.1");
			else {
				yAxis.setMinorTickInterval("auto");
			}

			ChartUtils.setAxisExtremes(feed, yAxis);
			// yAxis.setMasterGrid(masterGrid == i);

			yAxis.setGridLineWidth(gridLineWidth);
			yAxis.setMinorGridLineWidth((double) gridLineWidth / 2);

			if (masterGrid != null && !masterGrid.equals(yAxis.getId())) {
				yAxis.setGridLineWidth(0);
				yAxis.setMinorGridLineWidth(0);
			}
			/*
			
			*/

			Style style = new Style();
			style.setColor(new SolidColor(feedColor));
			
			if (!feed.getOptions().isAxisTitle()) {
				feedLabel = null;
			}

			AxisTitle title = new AxisTitle(feedLabel);
			title.setStyle(style);
			yAxis.setTitle(title);

			Labels labels = new Labels();
			labels.setStyle(style);

			yAxis.setLabels(labels);
			yAxis.setOpposite((i & 1) != 0);

			if (feed.getThresholds().size() > 0) {
				PlotLine[] plotLines = new PlotLine[feed.getThresholds().size()];
				int index = 0;
				for (ChartThreshold threshold : feed.getThresholds()) {
					Number value = ChartUtils.calculateThreshold(threshold.getValue(), feed);
					plotLines[index] = new PlotLine(value, ChartUtils.THRESHOLD_LINE_WIDTH,
							new SolidColor(threshold.getFillColor()));
					plotLines[index].setWidth(ChartUtils.THRESHOLD_LINE_WIDTH);
					style = new Style();
					style.setColor(new SolidColor(threshold.getFillColor()));
					Label label = new Label(threshold.getLabel());
					label.setStyle(style);
					plotLines[index].setLabel(label);
					index++;
				}
				yAxis.setPlotLines(plotLines);
			}

			/*
			 * For numeric values, a subset of C printf formatting specifiers is
			 * supported. For example, " {point.y:%02.2f} would display a
			 * floating-point value with two decimals and two leading zeroes,
			 * such as 02.30.
			 */
			// precision on decimal label should be the same of measure
			if (feed.getMeasure() != null) {
				String format = String.format("{value:%%.%df}", feed.getMeasure().getDecimals());
				yAxis.getLabels().setFormat(format);
				yAxis.setAllowDecimals(feed.getMeasure().getDecimals() > 0);
			}

			if (ChannelUtils.isTypeDigital(feed.getChannel())) {
				yAxis.getLabels().setFormat(null);
				yAxis.setAllowDecimals(false);
				yAxis.setMin(0f);
			}
			//yAxis.setVisible(!UIUtils.isMobile());
			configuration.addyAxis(yAxis);

		}
	}

	@Override
	public void createChartConfiguration(TimeInterval interval) {
		List<Series> series = new ArrayList<Series>();
		for (int i = 0; i < getGraphWidget().getFeeds().size(); i++) {
			GraphicFeed feed = getGraphWidget().getFeeds().get(i);
			DataSeries feedSeries = getTimeSeries(feed, interval, true, null);
			feedSeries.setId(feed.getKey());
			feedSeries.setVisible(!hidden.contains(feed.getKey()));
			series.add(feedSeries);
		}
		getChart().getConfiguration().setSeries(series);
	}

	@Override
	public void draw() {
		TimeInterval interval = intervalField.getValue();

		Long start = ChartUtils.toHighchartsTS(interval.getStartDate(), getNetworkTimeZone());
		Long end = ChartUtils.toHighchartsTS(interval.getEndDate(), getNetworkTimeZone());

		if (isRealTime()) {
			if (movedRealtime) {
				interval = intervalField.getHelper().movingPeriod(new Date(), periodField.getValue());
				changeRealTimeInterval(interval);
				start = ChartUtils.toHighchartsTS(interval.getStartDate(), getNetworkTimeZone());
				end = ChartUtils.toHighchartsTS(interval.getEndDate(), getNetworkTimeZone());
				movedRealtime = false;
			}
			long quarter = periodField.getValue().getTime() / 4;
			end = end + quarter;
		}

		getChart().getConfiguration().getxAxis().setExtremes(start, end);
		getChart().drawChart(getChart().getConfiguration());
	}

	@Override
	public boolean refresh() {
		if (((GraphicWidget) getWidget()).getFeeds().isEmpty())
			return false;
		if (refreshed.isEmpty()) {
			createRefreshed();
		}
		Long max = (Long) getChart().getConfiguration().getxAxis().getMax();
		long period = periodField.getValue().getTime() / 4;
		if (max == null)
			max = period;
		Date to = new Date();
		Long endX = 0l;
		boolean redraw = false;
		for (int i = 0; i < getGraphWidget().getFeeds().size(); i++) {
			GraphicFeed feed = getGraphWidget().getFeeds().get(i);
			List<Series> chartSeries = getChart().getConfiguration().getSeries();
			if (chartSeries.isEmpty() || i >= chartSeries.size()) {
				refreshed.put(feed.getKey(), new Date());
				continue;
			}
			Series series = chartSeries.get(i);
			Date from = refreshed.get(feed.getKey());
			if (from == null) {
				from = refreshedDate(feed);
				refreshed.put(feed.getKey(), from);
			}
			if (from.before(to)) {
				float ratio = (float) periodField.getValue().getTime() / (float) (to.getTime() - from.getTime());
				DataSeries feedSeries = getTimeSeries(feed, new TimeInterval(from, to), false, ratio);
				if (!feedSeries.getData().isEmpty()) {
					redraw = true;
					boolean updateImmediately = false;
					boolean shift = ((DataSeries) series).size() > 360;
					int size = ((DataSeries) series).size();
					if (size > 1) {
						DataSeriesItem end = ((DataSeries) series).get(size - 1);
						endX = (Long) end.getX();
					}
					for (DataSeriesItem item : feedSeries.getData()) {
						Long x = (Long) item.getX();
						if (x > endX) {
							endX = x;
							((DataSeries) series).add(item, updateImmediately, shift);
							to = ChartUtils.toNetworkDate(x, getNetworkTimeZone());
						}
					}
					refreshed.get(feed.getKey()).setTime(to.getTime() + 1);
				}
			}

		}

		if (endX > (max - period)) {
			movedRealtime = true;
			redraw = true;
		} else {
			movedRealtime = false;
		}

		return redraw;
	}

	@Override
	public void setGraphWidgetOptionsOnChange(GraphicWidgetOptions options) {
		Configuration configuration = getChart().getConfiguration();
		boolean redraw = false;
		boolean changeShowGrid = getGraphWidget().getOptions().getShowGrid() != options.getShowGrid();
		if (changeShowGrid) {
			redraw = true;
			float gridLineWidth = options.getShowGrid() ? (Float) ChartUtils.GRID_LINE_WIDTH : 0;
			configuration.getxAxis().setGridLineWidth(gridLineWidth);
			configuration.getxAxis().setMinorGridLineWidth((double) gridLineWidth / 2);
			for (int i = 0; i < configuration.getNumberOfyAxes(); i++) {
				YAxis yAxis = (YAxis) configuration.getyAxes().getAxis(i);
				yAxis.setGridLineWidth(gridLineWidth);
				yAxis.setMinorGridLineWidth((double) gridLineWidth / 2);
				if (masterGrid != null && masterGrid.equals(yAxis.getId())) {
					yAxis.setGridLineWidth(0);
					yAxis.setMinorGridLineWidth(0);
				}
			}
			getGraphWidget().getOptions().setShowGrid(options.getShowGrid());
		}

		boolean changeScale = options.getScale() != null
				&& getGraphWidget().getOptions().getScale() != options.getScale();

		if (changeScale) {
			redraw = true;
			getGraphWidget().getOptions().setScale(options.getScale());
			AxisType yAxisType = AxisType.LINEAR;
			switch (getGraphWidget().getOptions().getScale()) {
			case LOGARITHMIC:
				yAxisType = AxisType.LOGARITHMIC;
				break;
			default:
				break;
			}

			for (int i = 0; i < configuration.getNumberOfyAxes(); i++) {
				configuration.getyAxes().getAxis(i).setType(yAxisType);
				if (yAxisType.equals(AxisType.LOGARITHMIC)) {
					/*
					 * TODO Bug #126: [VAADIN] [HIGHCHART] logaritmic scale is
					 * not working
					 */
					options.setAutoScale(true);
					YAxis yAxis = (YAxis) configuration.getyAxes().getAxis(i);

					yAxis.setType(AxisType.LOGARITHMIC);
					try {
						// yAxis.setExtremes((Number) yAxis.getMax(), (Number)
						// yAxis.getMin());
						// yAxis.setMin((Number) null);
						// yAxis.setMax((Number) null);
					} catch (Exception e) {
						logger.error("TODO Bug #126: [VAADIN] [HIGHCHART] logaritmic scale is not working", e);
					}
					yAxis.setMinorTickInterval("0.1");
					// yAxis.setPlotLines((PlotLine[]) null);
					// yAxis.setTickPositions(null);
				} else {
					configuration.getyAxes().getAxis(i).setMinorTickInterval(null);
				}
			}
		}

		boolean changeAutoScale = getGraphWidget().getOptions().getAutoScale() != options.getAutoScale();
		if (changeAutoScale) {
			redraw = true;
			getGraphWidget().getOptions().setAutoScale(options.getAutoScale());
			for (int i = 0; i < configuration.getNumberOfyAxes(); i++) {
				if (options.getAutoScale()) {
					try {
						configuration.getyAxes().getAxis(i).setExtremes((Number) null, (Number) null);
					} catch (Exception e) {
					}
					GraphicFeed feed = getGraphWidget().getFeeds().get(i);
					if (feed.getOptions().getExtremes() != null) {
						((YAxis) configuration.getyAxes().getAxis(i)).setTickPositions(null);
					}
				} else {
					ChartUtils.setAxisExtremes(getGraphWidget().getFeeds().get(i),
							(YAxis) configuration.getyAxes().getAxis(i));
				}
			}
		}

		boolean changeShowMarkers = getGraphWidget().getOptions().getShowMarkers() != options.getShowMarkers();
		if (changeShowMarkers) {
			redraw = true;
			getGraphWidget().getOptions().setShowMarkers(options.getShowMarkers());
			for (Series series : configuration.getSeries()) {
				if (series instanceof DataSeries) {
					AbstractPlotOptions plotOptions = ((DataSeries) series).getPlotOptions();
					if (plotOptions instanceof PointOptions) {
						((PointOptions) plotOptions).getMarker().setEnabled(options.getShowMarkers());
					}
				}
			}
		}
		changedLocalControls(options);
		if (changedRealTime(options)) {
			redraw = true;
		}
		if (redraw) {
			getChart().drawChart(configuration);
		}

	}

	@Override
	public boolean isRealTime() {
		return (getGraphWidget() != null) && getGraphWidget().getOptions().getRealTime();
	}

	public boolean changedRealTime(GraphicWidgetOptions options) {
		boolean changeRealTime = getGraphWidget().getOptions().getRealTime() != options.getRealTime();
		if (changeRealTime) {
			refreshed.clear();
			boolean enabled = !options.getRealTime() && options.getLocalControls();
			exportButton.setEnabled(enabled);
			timeControl.setEnabled(enabled);
			timeControl.activeLocalControl(options.getLocalControls());
			createRefreshed();
			getGraphWidget().getOptions().setRealTime(options.getRealTime());
			if (options.getRealTime() && options.getLocalControls()) {
				intervalField.setValue(intervalField.getHelper().movingPeriod(new Date(), periodField.getValue()));
			}
			Configuration configuration = getChart().getConfiguration();
			if (getGraphWidget().getOptions().getRealTime()) {
				configuration.getChart().setZoomType(null);
			} else {
				configuration.getChart().setZoomType(ZoomType.X);
			}
		}
		return changeRealTime;
	}

	/**
	 * re-create refreshed list needed for real-time display
	 * 
	 * @return
	 */
	private void createRefreshed() {
		refreshed.clear();
		for (GraphicFeed feed : getGraphWidget().getFeeds()) {
			refreshed.put(feed.getKey(), refreshedDate(feed));
		}
	}

	private Date refreshedDate(GraphicFeed feed) {
		Date date = null;
		if (feed != null && feed.getChannel() != null) {
			date = ChartUtils.lastTick(feed.getChannel().getDevice().getSerial());
		}

		if (date == null) {
			date = new Date(System.currentTimeMillis() - 1);
		}

		return date;

	}



//	protected ChartSelectionListener createChartSelectionListener() {
//		ChartSelectionListener chartSelectionListener = new ChartSelectionListener() {
//			private static final long serialVersionUID = 325958356424593206L;
//
//			@Override
//			public void onSelection(ChartSelectionEvent event) {
//				long start = event.getSelectionStart().longValue();
//				long end = event.getSelectionEnd().longValue();
//				Date startZoom = ChartUtils.toNetworkDate(start, getNetworkTimeZone());
//				Date endZoom = ChartUtils.toNetworkDate(end, getNetworkTimeZone());
//				intervalField.setValue(new TimeInterval(startZoom, endZoom));
//			}
//		};
//		return chartSelectionListener;
//	}
//
//	protected LegendItemClickListener createLegendItemClickListener() {
//		return new LegendItemClickListener() {
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public void onClick(LegendItemClickEvent event) {
//				String seriesId = event.getSeries().getId();
//				DataSeries series = (DataSeries) event.getSeries();
//				if (!hidden.contains(seriesId)) {
//					series.setVisible(false);
//					hidden.add(seriesId);
//				} else {
//					series.setVisible(true);
//					hidden.remove(seriesId);
//				}
//
//			}
//		};
//
//	}

	public void addMeasure(MeasureRaw measure) {
		for (Series series : getChart().getConfiguration().getSeries()) {
			if (series.getId().equals(measure.getKey())) {
				DataSeriesItem item = new DataSeriesItem(measure.getDate(), measure.getValue());
				item.setX(ChartUtils.toHighchartsTS(measure.getDate(), getNetworkTimeZone()));
				((DataSeries) series).add(item, false, false);
				break;
			}
		}
	}

	@Override
	public Chart getChart() {
		return chart;
	}

	@Override
	protected void setChart(Chart chart) {
		this.chart = chart;
	}

//	public ChartSelectionListener getChartSelectionListener() {
//		return chartSelectionListener;
//	}
//
//	public void setChartSelectionListener(ChartSelectionListener chartSelectionListener) {
//		this.chartSelectionListener = chartSelectionListener;
//	}



}
