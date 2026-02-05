package it.thisone.iotter.ui.charts;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.AbstractPlotOptions;
import com.vaadin.addon.charts.model.ChartModel;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.LayoutDirection;
import com.vaadin.addon.charts.model.ListSeries;
import com.vaadin.addon.charts.model.PlotOptionsSeries;
import com.vaadin.addon.charts.model.PointPlacement;
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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.shared.Registration;

import it.thisone.iotter.cassandra.InterpolationUtils;
import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GraphicWidgetOptions;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.common.charts.PlotOptionsColumn;
import it.thisone.iotter.ui.common.fields.ChannelAcceptor;
import it.thisone.iotter.ui.model.TimeInterval;

/**
 * <p>
 * See the Guava User Guide article on <a
 * href="http://code.google.com/p/guava-libraries/wiki/RangesExplained">
 * {@code Range}</a>.
 * 
 * 
 * 
 * (a..b) open(C, C) [a..b] closed(C, C) [a..b) closedOpen(C, C) (a..b]
 * openClosed(C, C) (a..+∞) greaterThan(C) [a..+∞) atLeast(C) (-∞..b)
 * lessThan(C) (-∞..b] atMost(C) (-∞..+∞) all()
 * 
 * @author tisone
 * 
 */
	// TODO(flow-migration): this class still contains Vaadin 8 APIs and needs manual Flow refactor.
public class WindRoseChartAdapter extends AbstractChartAdapter {

	/*
	 * the names of the winds were commonly known throughout the Mediterranean
	 * countries as tramontana (N), greco (NE), levante (E), siroco (SE), ostro
	 * (S), libeccio (SW), ponente (W) and maestro (NW). On portolan charts you
	 * can see the initials of these winds labeled around the edge as T, G, L,
	 * S, O, L, P, and M.
	 */
	private static Logger logger = LoggerFactory
			.getLogger(WindRoseChartAdapter.class);
	private List<String> measureCategories;
	private List<Range<Double>> measureRanges;
	private List<AbstractPlotOptions> options;
	private List<Range<Double>> degreeRanges;
	private static final String[] allDegreeCategories = { "N", "NNE", "NE",
			"ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W",
			"WNW", "NW", "NNW" };
	
	private Chart chart;

	private static final Integer[] degreePetals = { 4, 8, 16 };

	private ComboBox<Integer> petalsChoice;
	private Registration applyPetalsChoiceRegistration;

	private int petals = allDegreeCategories.length / 2;
	private List<String> degreeCategories;

	/**
	 * 
	 */
	private static final long serialVersionUID = -1958076735452737593L;

	public WindRoseChartAdapter(GraphicWidget widget) {
		super(widget);
		optionsField.getRealTime().setVisible(false);
		optionsField.getScale().setVisible(false);
		optionsField.getAutoScale().setVisible(false);
		optionsField.getShowMarkers().setVisible(false);
		optionsField.getShowGrid().setVisible(false);
		petalsChoice = createDegreeChoice();
		getToolLayout().add(petalsChoice);
	}

	@Override
	public void draw() {
		getChart().drawChart(getChart().getConfiguration());
	}

	@Override
	protected Component buildVisualization() {
		Chart chart = new Chart(ChartType.COLUMN);
		chart.setId(String.valueOf(getWidget().getId()));
		setChart(chart);
		createMeasureCategories();
		createDegreeCategories();
		return chart;
	}

	private Configuration createConfiguration() {
		Configuration configuration = new Configuration();
		Title chartTitle = new Title(getWidget().getLabel());
		chartTitle = new Title("");
		chartTitle.setFloating(true);
		Style cstyle = new Style();
		cstyle.setPosition(StylePosition.ABSOLUTE);
		cstyle.setFontSize("0px");
		configuration.setTitle(chartTitle);

		configuration.setExporting(true);
		configuration.getExporting().setWidth(800);
		configuration.setTitle(chartTitle);

		ChartModel chart = new ChartModel();
		chart.setType(ChartType.COLUMN);
		chart.setPolar(true);
		chart.setInverted(false);
		chart.setPlotShadow(false);
		chart.setMargin(0);
		chart.setMarginBottom(70);
		chart.setBorderWidth(0);
		chart.setBorderRadius(0);
		chart.setPlotBorderWidth(0);

		configuration.setChart(chart);
		//
		configuration.getChart().setAnimation(false);
		configuration.getLegend().setReversed(false);
		configuration.getLegend().setLayout(LayoutDirection.HORIZONTAL);
		//configuration.getLegend().setHorizontalAlign(HorizontalAlign.CENTER);
		configuration.getLegend().setVerticalAlign(VerticalAlign.BOTTOM);
		configuration.getLegend().setFloating(false);

		XAxis xAxis = new XAxis();

		PlotOptionsSeries plotOptions = new PlotOptionsSeries();
		plotOptions.setStacking(Stacking.NORMAL);
		plotOptions.setShadow(false);
		//plotOptions.setGroupPadding(0);

		plotOptions.setPointPlacement(PointPlacement.ON);
		configuration.setPlotOptions(plotOptions);

		String[] array = new String[measureCategories.size()];
		array = measureCategories.toArray(array);
		xAxis.setCategories(array);
		xAxis.setTickmarkPlacement(TickmarkPlacement.ON);

		YAxis yAxis = new YAxis();
		yAxis.setMin(0d);
		// yAxis.setShowFirstLabel(false);
		// yAxis.setShowLastLabel(false);
		// yAxis.setStartOnTick(false);
		yAxis.setEndOnTick(false);
		// yAxis.setTitle(getI18nLabel("frequency") + " (%)");
		yAxis.setTitle("");
		yAxis.getLabels().setFormatter("function() {return this.value + '%';}");

		// Bug #323 Rosa dei venti: i petali della rosa dei venti devono essere
		// invertiti come posizione
		yAxis.setReversedStacks(false);

		configuration.addxAxis(xAxis);
		configuration.addyAxis(yAxis);
		configuration.getTooltip().setValueSuffix("%");

		return configuration;
	}

	/**
	 * create degree and threshold ranges
	 */
	private void createMeasureCategories() {
		options = new ArrayList<AbstractPlotOptions>();
		measureRanges = new ArrayList<Range<Double>>();
		measureCategories = new ArrayList<String>();


		GraphicFeed speed = getSpeedFeed();
		GraphicFeed direction = getDirectionFeed();
		if (speed == null || direction == null) {
			return;
		}

		String feedColor = speed.getOptions().getFillColor();
		String feedUnit = ChartUtils.getUnitOfMeasure(speed);
		String feedLabel = String.format("%s [%s]", speed.getChannel()
				.toString(), feedUnit);

		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(UIUtils
				.getLocale());
		df.applyPattern(speed.getMeasure().getFormat());

		Range<Double> range;
		if (speed.getThresholds().isEmpty()) {
			measureCategories.add(feedLabel);
			range = Range.all();
			measureRanges.add(range);
			options.add(getPlotOptions(feedColor));
		} else {
			int size = speed.getThresholds().size();
			Number lower = ChartUtils.calculateThreshold(speed.getThresholds()
					.get(0).getValue(), speed);
			Number upper = lower;

			if (size > 1) {
				for (int i = 1; i < size; i++) {
					lower = ChartUtils.calculateThreshold(speed.getThresholds()
							.get(i - 1).getValue(), speed);
					upper = ChartUtils.calculateThreshold(speed.getThresholds()
							.get(i).getValue(), speed);
					measureCategories.add(String.format("%s - %s [%s]",
							df.format(lower), df.format(upper), feedUnit));
					range = Range.closedOpen((Double) lower, (Double) upper);
					measureRanges.add(range);
					options.add(getPlotOptions(speed.getThresholds().get(i - 1)
							.getFillColor()));
				}
			}

			upper = ChartUtils.calculateThreshold(
					speed.getThresholds().get(size - 1).getValue(), speed);

			measureCategories.add(String.format(" > %s [%s]", df.format(upper),
					feedUnit));
			range = Range.atLeast((Double) upper);
			measureRanges.add(range);
			options.add(getPlotOptions(speed.getThresholds().get(size - 1)
					.getFillColor()));

		}
		for (Range<Double> mrange : measureRanges) {
			logger.debug("Added measure range {} ", mrange);
		}
	}


	private void createDegreeCategories() {

		if (petals == 0)
			petals = allDegreeCategories.length;
		if (degreeCategories == null) {
			// degreeCategories = Arrays.asList(allDegreeCategories);
			degreeCategories = new ArrayList<String>();
		}

		if (petals == degreeCategories.size()) {
			return;
		}

		degreeCategories = new ArrayList<String>();

		int span = allDegreeCategories.length / petals;

		for (int i = 0; i < allDegreeCategories.length; i = i + span) {
			degreeCategories.add(allDegreeCategories[i]);
		}
		degreeRanges = new ArrayList<Range<Double>>();
		double step = 360d / petals;

		/*
		 * create two ranges for N position
		 */
		double current = step / 2d;
		Range<Double> range = Range.closedOpen(0d, current / 1000d);
		degreeRanges.add(range);

		for (int i = 1; i < petals; i++) {
			double lower = current;
			double upper = current + step;
			range = Range.closedOpen(lower / 1000d, upper / 1000d);
			degreeRanges.add(range);
			current = upper;
		}
		range = Range.closed(current / 1000d, (current + (step / 2d)) / 1000d);
		degreeRanges.add(range);
		for (Range<Double> drange : degreeRanges) {
			logger.debug("Added degree range {} ", drange);
		}
	}

	protected AbstractPlotOptions getPlotOptions(String feedColor) {
		PlotOptionsColumn plotOption = new PlotOptionsColumn();
		plotOption.setStacking(Stacking.NORMAL);
		plotOption.setColor(new SolidColor(feedColor));
		plotOption.setShadow(false);
		//plotOption.setGroupPadding(0);
		plotOption.setPointPlacement(PointPlacement.ON);
		return plotOption;
	}


	@Override
	protected void createChartConfiguration(TimeInterval time) {
		getChart().setConfiguration(createConfiguration());
		if (getGraphWidget().getFeeds().isEmpty()) {
			return;
		}
		createDegreeCategories();

		Map<Number, List<Number>> seriesContainer = new HashMap<Number, List<Number>>();

		GraphicFeed speed = getSpeedFeed();;
		GraphicFeed direction = getDirectionFeed();;
		int size = speed.getThresholds().size();

		for (int i = 0; i < degreeCategories.size(); i++) {
			List<Number> values = new ArrayList<Number>();
			for (int j = 0; j < size; j++) {
				values.add(0d);
			}
			seriesContainer.put(i, values);
		}

		List<MeasureRaw> measures = getData(speed, time);
		List<MeasureRaw> degrees = getData(direction, time);

		// size of measures / degrees must the same
		/*
		Bug #341 [Highcharts] Wind Rose does not show some values: max or min values are not reported in categories
		 */
		if (measures.size() != degrees.size()) {
			Map<Date, MeasureRaw> map = new HashMap<>();
			for (MeasureRaw measureRaw : degrees) {
				if (!measureRaw.hasError()) {
					map.put(measureRaw.getDate(), measureRaw);
				}
			}
			degrees.clear();
			List<MeasureRaw> temp = new ArrayList<>();
			for (MeasureRaw measure : measures) {
				if (!measure.hasError()) {
					MeasureRaw degree = map.get(measure.getDate());
					if (degree != null) {
						temp.add(measure);
						degrees.add(degree);
					}
				}
			}
			measures = temp;
		}

		int total = measures.size();
		MeasureUnit measureUnit = speed.getMeasure();
		MeasureUnit degreesMeausure = findMeasureUnit(speed.getOptions()
				.getFeedReference(), speed);

		logger.debug("Creating series container  {} x {} for data points {} ",
				degreeCategories.size(), size, total);

		int count = 0;
		for (int index = 0; index < total; index++) {
			try {
				MeasureRaw measure = measures.get(index);
				if (measure.getValue() == null) {
					logger.debug("Missing measure at index {}", index);
					continue;
				}

				MeasureRaw degree = degrees.get(index);
				if (degree.getValue() == null) {
					logger.debug("Missing degree at index {}", index);
					continue;
				}

				Double value = (Double) ChartUtils.calculateMeasure(
						measure.getValue(), measureUnit);
				Double degreeVal = (Double) ChartUtils.calculateAngle(
						degree.getValue(), degreesMeausure);

				int valuePos = findRange(value, measureRanges);
				int degreePos = findRange(degreeVal, degreeRanges);
				if (degreePos != -1) {
					if (degreePos > degreeCategories.size()) {
						degreePos = 0;
					}
					List<Number> values = seriesContainer.get(degreePos);
					if (values != null && valuePos != -1) {
						Double cnt = (Double) values.get(valuePos);
						cnt++;
						values.set(valuePos, cnt);
						count++;
					}
				}
			} catch (Exception e) {
				logger.error("Wind Rose drawChartTimeInterval", e);
			}
			index++;
		}

		List<Series> items = new ArrayList<Series>();
		for (int degreePos = 0; degreePos < degreeCategories.size(); degreePos++) {
			String name = degreeCategories.get(degreePos);
			ListSeries item = new ListSeries(name);
			List<Number> values = seriesContainer.get(degreePos);
			item.setData(values);
			items.add(item);
		}

		if (count > 0) {
			items.clear();
			DecimalFormat decimalFormat = (DecimalFormat) NumberFormat
					.getInstance(UIUtils.getLocale());
			decimalFormat.applyPattern("0.##");
			for (int degreePos = 0; degreePos < degreeCategories.size(); degreePos++) {
				String name = degreeCategories.get(degreePos);
				ListSeries item = new ListSeries(name);
				List<Number> values = seriesContainer.get(degreePos);
				for (int valuePos = 0; valuePos < values.size(); valuePos++) {
					Double value = (((Double) values.get(valuePos) * 100d) / count);
					try {
						values.set(valuePos,
								decimalFormat
										.parse(decimalFormat.format(value))
										.doubleValue());
					} catch (ParseException e) {
						logger.error("Wind Rose format percent", e);
					}
				}
				item.setData(values);
				items.add(item);
			}
		}

		getChart().getConfiguration().setSeries(items);
		// transpose data "matrix"
		getChart().getConfiguration().reverseListSeries();
		for (int i = 0; i < getChart().getConfiguration().getSeries().size(); i++) {
			((ListSeries) getChart().getConfiguration().getSeries().get(i))
					.setPlotOptions(options.get(i));
		}

	}

	@Override
	protected void setGraphWidgetOptionsOnChange(GraphicWidgetOptions options) {
		changedLocalControls(options);
	}

	@Override
	protected boolean isRealTime() {
		return false;
	}

	@Override
	public boolean refresh() {
		return false;
	}

	private int findRange(Double value, List<Range<Double>> ranges) {
		if (value == null) {
			return -1;
		}
		for (int i = 0; i < ranges.size(); i++) {
			if (ranges.get(i).contains(value)) {
				logger.debug("Value {} in Range {} {}", value, ranges.get(i), i);
				return i;
			}
		}
		logger.debug("Value {} range not found", value);
		return -1;
	}

	private ComboBox<Integer> createDegreeChoice() {
		ComboBox<Integer> combo = new ComboBox<>();
		combo.addClassName("small");
		combo.setWidth("8em");

		java.util.List<Integer> petalsList = java.util.Arrays.asList(degreePetals);
		combo.setItems(petalsList);
		combo.setItemLabelGenerator(value -> value + " " + getI18nLabel("petals"));
		combo.setValue(petals);

		applyPetalsChoiceRegistration = combo.addValueChangeListener(event -> {
			Integer selectedValue = event.getValue();
			if (selectedValue != null) {
				petals = selectedValue;
				startDrawing();
				intervalField.updateValue();
			}
		});
		return combo;
	}

	private Interpolation suggestedInterpolation(TimeInterval interval) {
		int seconds = (int) ((float) (interval.getEndDate().getTime() - interval.getStartDate().getTime()) / (float) 1000);
		Interpolation interpolation = InterpolationUtils.suggestedInterpolation(seconds);
		if (seconds <= Interpolation.D1.getSeconds()) {
			interpolation = Interpolation.RAW;
		}
		Range<Date> range = Range.closedOpen(interval.getStartDate(), interval.getEndDate());
		return UIUtils.getCassandraService().getRollup().checkInterpolationAvailability(interpolation, range, null, null);
	}

	/*
	Bug #341 [Highcharts] Wind Rose does not show some values: max or min values are not reported in categories
	 */
	private List<MeasureRaw> getData(GraphicFeed feed, TimeInterval interval) {
		List<Range<Date>> ranges = getValidities().get(feed.getKey());
		List<MeasureRaw> measures = new ArrayList<>();
		Interpolation interpolation = suggestedInterpolation(interval);

		FeedKey feedKey = new FeedKey(feed.getDevice().getSerial(), feed.getKey());
		feedKey.setQualifier(feed.getChannel().getConfiguration().getQualifier());
		
		if (interpolation.equals(Interpolation.RAW)) {
			measures = ChartUtils.getData(feedKey, interval.getStartDate(),
					interval.getEndDate(), -1, 0,ranges, getNetworkTimeZone());
		} else {
			
			/*
			 * daily aggregation must be shown using H1
			 */
			
			if (interpolation.equals(Interpolation.D1)) {
				interpolation = Interpolation.H1;
			}
			
			measures = ChartUtils.getAggregationData(feedKey,
					interval.getStartDate(), interval.getEndDate(), interpolation,
					ranges, getNetworkTimeZone());
		}

		if (measures.isEmpty()) {
			logger.debug("Missing measures {} ", feed.getKey());
		}
		return measures;
	}

	

	private GraphicFeed getDirectionFeed() {
		for (GraphicFeed feed : getGraphWidget().getFeeds()) {
			if (ChannelAcceptor.isDirection(feed.getChannel())) {
				return feed;
			}
		}
		return null;
	}

	private GraphicFeed getSpeedFeed() {
		for (GraphicFeed feed : getGraphWidget().getFeeds()) {
			if (ChannelAcceptor.isSpeed(feed.getChannel())) {
				return feed;
			}
		}
		return null;	
	}

	@Override
	protected boolean changedRealTime(GraphicWidgetOptions options) {
		return false;
		
	}
	@Override
	public Chart getChart() {
		return chart;
	}

	@Override
	protected void setChart(Chart chart) {
		this.chart = chart;
	}


}
