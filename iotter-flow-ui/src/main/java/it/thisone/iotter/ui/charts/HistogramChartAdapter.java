package it.thisone.iotter.ui.charts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.AbstractPlotOptions;
import com.vaadin.addon.charts.model.AxisTitle;
import com.vaadin.addon.charts.model.ChartModel;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.LayoutDirection;
import com.vaadin.addon.charts.model.Legend;
import com.vaadin.addon.charts.model.ListSeries;
import com.vaadin.addon.charts.model.PlotOptionsColumn;
import com.vaadin.addon.charts.model.Series;
import com.vaadin.addon.charts.model.Stacking;
import com.vaadin.addon.charts.model.TickmarkPlacement;
import com.vaadin.addon.charts.model.Title;
import com.vaadin.addon.charts.model.VerticalAlign;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.addon.charts.model.style.Style;
import com.vaadin.addon.charts.model.style.StylePosition;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.shared.Registration;

import it.thisone.iotter.cassandra.InterpolationUtils;
import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GraphicWidgetOptions;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.model.TimeInterval;

	// TODO(flow-migration): this class still contains Vaadin 8 APIs and needs manual Flow refactor.
public class HistogramChartAdapter extends AbstractChartAdapter {

	private Chart chart;

	private static Logger logger = LoggerFactory
			.getLogger(HistogramChartAdapter.class);
	private List<String> categories;
	private List<Range<Date>> buckets = new ArrayList<>();

	private ComboBox<Interpolation> interpolationChoice;
	private Interpolation interpolation;
	private SimpleDateFormat sdf;
	private Registration applyInterpolationRegistration;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1958076735452737593L;

	public HistogramChartAdapter(GraphicWidget widget) {
		super(widget);
		optionsField.getRealTime().setVisible(false);
		optionsField.getScale().setVisible(false);
		optionsField.getAutoScale().setVisible(false);
		optionsField.getShowMarkers().setVisible(false);
		optionsField.getShowGrid().setVisible(false);
		sdf = new SimpleDateFormat(ChartUtils.DATE_FORMAT, UI.getCurrent()
				.getLocale());
		sdf.setTimeZone(getNetworkTimeZone());
		
		interpolationChoice = new ComboBox<>();
		interpolationChoice.addClassName("small");
		interpolationChoice.setWidth("8em");
		//interpolationChoice.setItems(Interpolation.values());
		interpolationChoice.setItemLabelGenerator(type -> getTranslation(type.getI18nKey()));
		interpolationChoice.setClearButtonVisible(false);

		
		
		getToolLayout().add(interpolationChoice);
		
		// Value change listener will be registered in setUpInterpolationChoice
	}

	@Override
	protected Component buildVisualization() {
		Chart chart = new Chart(ChartType.COLUMN);
		chart.setId(String.valueOf(getWidget().getId()));
		chart.getConfiguration().setTitle(new Title(""));
		setChart(chart);
		return chart;
	}

	private Configuration createConfiguration(GraphicFeed feed) {
		String feedColor = feed.getOptions().getFillColor();
		String feedLabel = ChartUtils.getFeedLabel(feed);
		Configuration configuration = new Configuration();
		configuration.setExporting(getGraphWidget().getOptions().getExporting());
		configuration.getExporting().setWidth(800);
		Title chartTitle = new Title(feedLabel);
		
		Style tstyle = new Style();
		tstyle.setPosition(StylePosition.ABSOLUTE);
		tstyle.setColor(new SolidColor(feedColor));
		tstyle.setFontSize("0px");
		tstyle.setFontSize("12px");
		
		chartTitle.setStyle(tstyle);
		chartTitle.setFloating(false);
		configuration.setTitle(chartTitle);

		ChartModel chart = new ChartModel();
		chart.setType(ChartType.COLUMN);
		chart.setPlotShadow(false);

		configuration.setChart(chart);
		//
		configuration.getChart().setAnimation(false);

		configuration.getLegend().setReversed(false);
		configuration.getLegend().setLayout(LayoutDirection.HORIZONTAL);
		configuration.getLegend().setVerticalAlign(VerticalAlign.BOTTOM);
		configuration.getLegend().setFloating(false);
		
		configuration.getTitle().setFloating(false);

		XAxis xAxis = new XAxis();

		String[] array = new String[categories.size()];
		array = categories.toArray(array);
		xAxis.setCategories(array);
		xAxis.setTickmarkPlacement(TickmarkPlacement.ON);

		configuration.setPlotOptions(getPlotOptions(feed));

		YAxis yAxis = new YAxis();

		Style style = new Style();
		style.setColor(new SolidColor(feedColor));

		AxisTitle title = new AxisTitle("");
		title.setStyle(style);
		yAxis.setTitle(title);
		
		float gridLineWidth = getGraphWidget().getOptions().getShowGrid() ? (Float) ChartUtils.GRID_LINE_WIDTH
				: 0f;

		yAxis.setGridLineWidth(gridLineWidth);
		yAxis.setMinorGridLineWidth((double) gridLineWidth / 2);

		configuration.addxAxis(xAxis);
		configuration.addyAxis(yAxis);

		Legend legend = new Legend();
		legend.setLayout(LayoutDirection.HORIZONTAL);
		legend.setVerticalAlign(VerticalAlign.BOTTOM);
		configuration.setLegend(legend);
		
		legend.setEnabled(getGraphWidget().getOptions().getShowLegend());
		
		return configuration;
	}
	
	private void setup(TimeInterval period) {
		Map<Integer, Interpolation> suggested = suggestedInterpolation(period, getMaxColumns());
		try {
			interpolation = suggested.get(Collections.max(suggested.keySet()));
		} catch (Exception e) {
			interpolation = Interpolation.MIN5;
		}

		if (interpolationChoice.getValue() != null) {
			Interpolation choice = interpolationChoice.getValue();
			if (suggested.containsValue(choice)) {
				interpolation = choice;
			}
		}
		
		Calendar calendar = Calendar.getInstance(getNetworkTimeZone());
		Range<Date> interval = Range.closed(period.getStartDate(),
				period.getEndDate());
		
		logger.debug("Setup {} {}", interpolation, rangeToString(interval));
		
		categories = new ArrayList<String>();
		buckets = new ArrayList<Range<Date>>();
		Date from = new Date(period.getStartDate().getTime());
		Date to = period.getEndDate();
		while (to.after(from)) {
			calendar.setTime(from);
			interval = InterpolationUtils
					.currentPeriod(calendar, interpolation);
			String category = categoryName(interval, sdf,
					interpolation);
			categories.add(category);
			buckets.add(interval);
			logger.debug("Added interval {} {}", category, rangeToString(interval));
			from = new Date(interval.upperEndpoint().getTime() + 1000);
		}
	}

	private String rangeToString(Range<Date> interval) {
		sdf.applyPattern(ChartUtils.DATE_FORMAT);
		boolean uopen = interval.upperBoundType().equals(BoundType.OPEN);
		String udel = uopen?")":"]";
		return String.format("[%s - %s%s", sdf.format(interval.lowerEndpoint()),
				sdf.format(interval.upperEndpoint()),udel);
	}

	protected AbstractPlotOptions getPlotOptions(GraphicFeed feed) {
		String feedColor = feed.getOptions().getFillColor();
		PlotOptionsColumn plotOptions = new PlotOptionsColumn();
		plotOptions.setColor(new SolidColor(feedColor));
		plotOptions.setStacking(Stacking.NORMAL);
		plotOptions.setShadow(false);
	    /**
	     * Sets the pixel value specifying a fixed width for each column or bar.
	     * Default null. If not set, the width is calculated from
	     * pointPadding and groupPadding.
	     */
        plotOptions.setPointWidth(null);
        if (feed.getMeasure() != null) {
    		Number feedDecimals = feed.getMeasure().getDecimals();
    		plotOptions.getTooltip().setValueDecimals(feedDecimals);
        }
        String headerFormat = "<small>{point.key}</small><br/>";
		plotOptions.getTooltip().setHeaderFormat(headerFormat);
        /**
         * Sets the padding between each column or bar, in x axis units. 
         * Defaults to 0.1.
         */
        plotOptions.setPointPadding(0.05);
        /**
         * Sets the padding between each value group, in X-axis units. 
         * Defaults to 0.2.
         */
        plotOptions.setGroupPadding(0.1);
		return plotOptions;
	}


	@Override
	protected void createChartConfiguration(TimeInterval time) {
		if (getGraphWidget().getFeeds().isEmpty()) {
			return;
		}
		setup(time);
		GraphicFeed feed = getGraphWidget().getFeeds().get(0);
		Configuration configuration = createConfiguration(feed);
		getChart().setConfiguration(configuration);
		List<Number> values = new ArrayList<Number>();
		for (int i = 0; i < categories.size(); i++) {
			values.add(null);
		}
		if (feed.getChannel() != null) {
			List<Range<Date>> validities = getValidities().get(feed.getKey());
			FeedKey feedKey = new FeedKey(feed.getDevice().getSerial(), feed.getKey());
			feedKey.setQualifier(feed.getChannel().getConfiguration().getQualifier());
			List<MeasureRaw> measures = ChartUtils.getAggregationData(
					feedKey, time.getStartDate(), time.getEndDate(),
					interpolation, validities, getNetworkTimeZone());
			sdf.applyPattern(ChartUtils.DATE_FORMAT);
			boolean positive = true;
			Date ts = new Date();
			for (MeasureRaw measure : measures) {
				if (!measure.isValid()) {
					continue;
				}
				try {
					Number value = ChartUtils.calculateMeasure(measure.getValue(),
							feed.getMeasure());
					if (value.longValue() < 0) {
						positive = false;
					}
					ts = measure.getDate();
					int index = getCategoryIndex(ts);
					if (index > -1) {
						values.set(index, value);
						logger.debug("{}: {} {}", categoryName(ts), ts, value);
					} else {
						logger.error("Feed {} cannot categorize date {}",
								feed.getKey(), sdf.format(ts));
					}
				} catch (Exception e) {
					logger.debug("Feed {} date {} error {}",
								feed.getKey(), sdf.format(ts),e.getMessage());
				}
			}
			if (positive) {
				YAxis yAxis = (YAxis) getChart().getConfiguration()
						.getyAxes().getAxis(0);
				yAxis.setMin(0);
			}
		}
		String feedLabel = ChartUtils.getFeedLabel(feed);
		ListSeries item = new ListSeries(feedLabel);
		item.setData(values);
		List<Series> items = new ArrayList<Series>();
		items.add(item);
		getChart().getConfiguration().setSeries(items);
	}

	@Override
	public void draw() {
		setUpInterpolationChoice();
		getChart().drawChart(getChart().getConfiguration());
	}

	private void setUpInterpolationChoice() {
		Range<Date> interval = Range.closedOpen(intervalField.getValue().getStartDate(), intervalField.getValue().getEndDate());
		
		java.util.List<Interpolation> availableInterpolations = new java.util.ArrayList<>();
		for (Interpolation type : UIUtils.getCassandraService().getRollup().availableInterpolations(interval,null,null)) {
			if (acceptInterpolation(type, intervalField.getValue(), getMaxColumns())) {
				availableInterpolations.add(type);
			}
		}
		
		if (applyInterpolationRegistration != null) {
			applyInterpolationRegistration.remove();
		}
		interpolationChoice.setItems(availableInterpolations);
		if (availableInterpolations.contains(interpolation)){
			interpolationChoice.setValue(interpolation);
		}
		
		applyInterpolationRegistration = interpolationChoice.addValueChangeListener(event -> {
			startDrawing();
			intervalField.updateValue();
		});
	}

	@Override
	protected void setGraphWidgetOptionsOnChange(GraphicWidgetOptions options) {
		Configuration configuration = getChart().getConfiguration();
		boolean redraw = false;
		boolean changeShowGrid = getGraphWidget().getOptions().getShowGrid() != options
				.getShowGrid();
		if (changeShowGrid) {
			redraw = true;
			float gridLineWidth = options.getShowGrid() ? (Float) ChartUtils.GRID_LINE_WIDTH
					: 0;
			configuration.getxAxis().setGridLineWidth(gridLineWidth);
			configuration.getxAxis().setMinorGridLineWidth(
					(double) gridLineWidth / 2);
			for (int i = 0; i < configuration.getNumberOfyAxes(); i++) {
				YAxis yAxis = (YAxis) configuration
						.getyAxes().getAxis(i);
					yAxis.setGridLineWidth(gridLineWidth);
					yAxis.setMinorGridLineWidth((double) gridLineWidth / 2);
			}
			getGraphWidget().getOptions().setShowGrid(options.getShowGrid());
		}
		changedRealTime(options);
		
		changedLocalControls(options);
		if (redraw) {
			getChart().drawChart(configuration);
		}
	}

	@Override
	protected boolean isRealTime() {
		return (getGraphWidget() != null)
				&& getGraphWidget().getOptions().getRealTime();
	}

	@Override
	public boolean refresh() {
		if (((GraphicWidget) getWidget()).getFeeds().isEmpty()) return false;

		if (buckets.isEmpty()) {
			return false;
		}
		boolean redraw = false;
		Date endX = buckets.get(buckets.size() - 1).upperEndpoint();
		Date max = new Date(endX.getTime() + (interpolation.getSeconds()/2) * 1000);
		Date now = new Date();
		if (now.after(max)) {
			TimeInterval interval = intervalField.getHelper().movingPeriod(now, periodField.getValue());
			
			changeRealTimeInterval(interval);
			createChartConfiguration(interval);
			redraw = true;
		}
		return redraw;
	}

	private int getCategoryIndex(Date date) {
		for (int index = 0; index < buckets.size(); index++) {
			Range<Date> interval = buckets.get(index);
			if (interval.contains(date)) {
				return index;
			}
		}
		return -1;
	}



	
	
	private Map<Integer, Interpolation> suggestedInterpolation(TimeInterval interval, int maxCategories) {
		Date from = interval.getStartDate();
		Date to = new Date(interval.getEndDate().getTime() + 1000);
		int seconds = (int) ((float) (to.getTime() - from.getTime()) / (float) 1000);
		Map<Integer, Interpolation> suggested = new HashMap<Integer, Interpolation>();
		Range<Date> range = Range.closedOpen(interval.getStartDate(), interval.getEndDate());
		for (Interpolation interpolation : UIUtils.getCassandraService().getRollup().availableInterpolations(range,null,null)) {
		    if (seconds > interpolation.getSeconds() && interpolation.getSeconds() > 0) {
		    	int count = (int)((float)seconds / (float)interpolation.getSeconds());
		    	if (count > 1 && count <= maxCategories) {
		    		suggested.put(count, interpolation);
		    	}
		    }
		}
		return suggested;
	}
	
	private boolean acceptInterpolation(Interpolation interpolation, TimeInterval interval, int maxCategories) {
		Date from = interval.getStartDate();
		Date to = new Date(interval.getEndDate().getTime() + 1000);
		int seconds = (int) ((float) (to.getTime() - from.getTime()) / (float) 1000);
		int count = seconds;
	    if (seconds > interpolation.getSeconds() && interpolation.getSeconds() > 0) {
	    	count = (int)((float)seconds/(float)interpolation.getSeconds());
	    }
		return (count > 1 && count <= maxCategories);
	}
	
	
	private int getMaxColumns() {
//		int pixels = (int) getWidth();
//		if (pixels <= 320) {
//			return 12;
//		} else if (320 < pixels && pixels <= 480) {
//			return 24;
//		} else if (480 < pixels && pixels <= 768) {
//			return 36;
//		}
		return 72;
	}

	private String categoryName(Range<Date> interval, SimpleDateFormat sdf,
			Interpolation interpolation) {
		switch (interpolation) {
		case D1:
			sdf.applyPattern("d.MMM");
			break;
		//case H3:
		case H6:
			sdf.applyPattern("HH:mm");
			return String.format("[%s:%s)", sdf.format(interval.lowerEndpoint()), sdf.format(interval.upperEndpoint()));
		case H1:
			sdf.applyPattern("HH:mm");
			break;
		case M1:
			sdf.applyPattern("MMM");
			break;
		case MIN15:
			sdf.applyPattern("HH:mm");
			break;
		case MIN5:
			sdf.applyPattern("HH:mm");
			break;
		case MIN1:
			sdf.applyPattern("mm");
			break;
		case W1:
			sdf.applyPattern("d.MMM");
			break;
		case RAW:
		default:
			break;
		}
		return sdf.format(interval.lowerEndpoint());
	}

	@Override
	protected boolean changedRealTime(GraphicWidgetOptions options) {
		boolean changeRealTime = getGraphWidget().getOptions().getRealTime() != options.getRealTime();
		if (changeRealTime) {
			boolean enabled = !options.getRealTime() && options.getLocalControls();
			exportButton.setEnabled(enabled);
			timeControl.setEnabled(enabled);
			timeControl.activeLocalControl(options.getLocalControls());
			getGraphWidget().getOptions().setRealTime(options.getRealTime());
			interpolationChoice.setEnabled(!getGraphWidget().getOptions().getRealTime());
		}
		return changeRealTime;
	}

	@Override
	public Chart getChart() {
		return chart;
	}

	@Override
	protected void setChart(Chart chart) {
		this.chart = chart;
	}

	private String categoryName(Date date) {
		for (int index = 0; index < buckets.size(); index++) {
			Range<Date> interval = buckets.get(index);
			if (interval.contains(date)) {
				return categoryName(interval, sdf, interpolation);
			}
		}
		
		return "missing";
	}
	

}
