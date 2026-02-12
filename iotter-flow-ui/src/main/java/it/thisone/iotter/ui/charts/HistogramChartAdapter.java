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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.shared.Registration;

import org.vaadin.addons.chartjs.ChartJs;
import org.vaadin.addons.chartjs.config.BarChartConfig;
import org.vaadin.addons.chartjs.data.BarDataset;
import org.vaadin.addons.chartjs.options.Position;
import org.vaadin.addons.chartjs.options.scale.Axis;
import org.vaadin.addons.chartjs.options.scale.CategoryScale;
import org.vaadin.addons.chartjs.options.scale.LinearScale;

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

public class HistogramChartAdapter extends AbstractChartAdapter {

	private ChartJs chart;
	private BarChartConfig chartConfig;
	private CategoryScale xScale;
	private LinearScale yScale;

	private static Logger logger = LoggerFactory.getLogger(HistogramChartAdapter.class);
	private List<String> categories;
	private List<Range<Date>> buckets = new ArrayList<>();

	private ComboBox<Interpolation> interpolationChoice;
	private Interpolation interpolation;
	private SimpleDateFormat sdf;
	private Registration applyInterpolationRegistration;

	private static final long serialVersionUID = -1958076735452737593L;

	public HistogramChartAdapter(GraphicWidget widget) {
		super(widget);
		optionsField.getRealTime().setVisible(false);
		optionsField.getScale().setVisible(false);
		optionsField.getAutoScale().setVisible(false);
		optionsField.getShowMarkers().setVisible(false);
		optionsField.getShowGrid().setVisible(false);
		sdf = new SimpleDateFormat(ChartUtils.DATE_FORMAT, UI.getCurrent().getLocale());
		sdf.setTimeZone(getNetworkTimeZone());

		interpolationChoice = new ComboBox<>();
		interpolationChoice.addClassName("small");
		interpolationChoice.setWidth("8em");
		interpolationChoice.setItemLabelGenerator(type -> getTranslation(type.getI18nKey()));
		interpolationChoice.setClearButtonVisible(false);

		getToolLayout().add(interpolationChoice);
	}

	@Override
	protected Component buildVisualization() {
		ChartJs chart = new ChartJs();
		chart.setId(String.valueOf(getWidget().getId()));
		setChart(chart);
		chartConfig = createEmptyConfiguration();
		chart.configure(chartConfig);
		return chart;
	}

	private BarChartConfig createEmptyConfiguration() {
		BarChartConfig configuration = new BarChartConfig();
		configuration.options().responsive(true).maintainAspectRatio(false);
		configuration.options().legend().position(Position.BOTTOM);
		configuration.options().legend().display(getGraphWidget().getOptions().getShowLegend());
		xScale = new CategoryScale();
		yScale = new LinearScale();
		applyGridVisibility(configuration, getGraphWidget().getOptions().getShowGrid());
		configuration.options().scales().add(Axis.X, xScale).add(Axis.Y, yScale);
		return configuration;
	}

	private BarChartConfig createConfiguration(GraphicFeed feed) {
		String feedColor = feed.getOptions().getFillColor();
		String feedLabel = ChartUtils.getFeedLabel(feed);
		BarChartConfig configuration = createEmptyConfiguration();
		configuration.options().title().display(true).text(feedLabel).fontColor(feedColor).fontSize(12);
		// TODO(flow-chartjs): Vaadin 8 column paddings and tooltip templates do not map 1:1.
		return configuration;
	}

	private void applyGridVisibility(BarChartConfig configuration, boolean showGrid) {
		int gridLineWidth = showGrid ? ((Number) ChartUtils.GRID_LINE_WIDTH).intValue() : 0;
		xScale.gridLines().display(showGrid).lineWidth(gridLineWidth);
		yScale.gridLines().display(showGrid).lineWidth(gridLineWidth);
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
		Range<Date> interval = Range.closed(period.getStartDate(), period.getEndDate());

		logger.debug("Setup {} {}", interpolation, rangeToString(interval));

		categories = new ArrayList<>();
		buckets = new ArrayList<>();
		Date from = new Date(period.getStartDate().getTime());
		Date to = period.getEndDate();
		while (to.after(from)) {
			calendar.setTime(from);
			interval = InterpolationUtils.currentPeriod(calendar, interpolation);
			String category = categoryName(interval, sdf, interpolation);
			categories.add(category);
			buckets.add(interval);
			logger.debug("Added interval {} {}", category, rangeToString(interval));
			from = new Date(interval.upperEndpoint().getTime() + 1000);
		}
	}

	private String rangeToString(Range<Date> interval) {
		sdf.applyPattern(ChartUtils.DATE_FORMAT);
		boolean uopen = interval.upperBoundType().equals(BoundType.OPEN);
		String udel = uopen ? ")" : "]";
		return String.format("[%s - %s%s", sdf.format(interval.lowerEndpoint()), sdf.format(interval.upperEndpoint()), udel);
	}

	@Override
	protected void createChartConfiguration(TimeInterval time) {
		if (getGraphWidget().getFeeds().isEmpty()) {
			return;
		}
		setup(time);

		GraphicFeed feed = getGraphWidget().getFeeds().get(0);
		chartConfig = createConfiguration(feed);
		chartConfig.data().labelsAsList(categories);

		List<Double> values = new ArrayList<>();
		for (int i = 0; i < categories.size(); i++) {
			values.add(null);
		}
		if (feed.getChannel() != null) {
			List<Range<Date>> validities = getValidities().get(feed.getKey());
			FeedKey feedKey = new FeedKey(feed.getDevice().getSerial(), feed.getKey());
			feedKey.setQualifier(feed.getChannel().getConfiguration().getQualifier());
			List<MeasureRaw> measures = ChartUtils.getAggregationData(feedKey, time.getStartDate(), time.getEndDate(),
					interpolation, validities, getNetworkTimeZone());
			sdf.applyPattern(ChartUtils.DATE_FORMAT);
			boolean positive = true;
			Date ts = new Date();
			for (MeasureRaw measure : measures) {
				if (!measure.isValid()) {
					continue;
				}
				try {
					Number value = ChartUtils.calculateMeasure(measure.getValue(), feed.getMeasure());
					if (value.longValue() < 0) {
						positive = false;
					}
					ts = measure.getDate();
					int index = getCategoryIndex(ts);
					if (index > -1) {
						values.set(index, value.doubleValue());
						logger.debug("{}: {} {}", categoryName(ts), ts, value);
					} else {
						logger.error("Feed {} cannot categorize date {}", feed.getKey(), sdf.format(ts));
					}
				} catch (Exception e) {
					logger.debug("Feed {} date {} error {}", feed.getKey(), sdf.format(ts), e.getMessage());
				}
			}
			yScale.ticks().beginAtZero(positive);
		}
		String feedLabel = ChartUtils.getFeedLabel(feed);
		BarDataset dataset = new BarDataset().label(feedLabel).dataAsList(values)
				.backgroundColor(feed.getOptions().getFillColor()).stack("values");
		chartConfig.data().addDataset(dataset);
		getChart().configure(chartConfig);
	}

	@Override
	public void draw() {
		setUpInterpolationChoice();
		TimeInterval interval = intervalField.getValue();
		if (interval != null) {
			createChartConfiguration(interval);
		}
		if (chartConfig != null) {
			getChart().update();
		}
	}

	private void setUpInterpolationChoice() {
		Range<Date> interval = Range.closedOpen(intervalField.getValue().getStartDate(), intervalField.getValue().getEndDate());

		List<Interpolation> availableInterpolations = new ArrayList<>();
		for (Interpolation type : UIUtils.getCassandraService().getRollup().availableInterpolations(interval, null, null)) {
			if (acceptInterpolation(type, intervalField.getValue(), getMaxColumns())) {
				availableInterpolations.add(type);
			}
		}

		if (applyInterpolationRegistration != null) {
			applyInterpolationRegistration.remove();
		}
		interpolationChoice.setItems(availableInterpolations);
		if (availableInterpolations.contains(interpolation)) {
			interpolationChoice.setValue(interpolation);
		}

		applyInterpolationRegistration = interpolationChoice.addValueChangeListener(event -> {
			startDrawing();
			intervalField.updateValue();
		});
	}

	@Override
	protected void setGraphWidgetOptionsOnChange(GraphicWidgetOptions options) {
		boolean redraw = false;
		boolean changeShowGrid = getGraphWidget().getOptions().getShowGrid() != options.getShowGrid();
		if (changeShowGrid && chartConfig != null) {
			redraw = true;
			applyGridVisibility(chartConfig, options.getShowGrid());
			getGraphWidget().getOptions().setShowGrid(options.getShowGrid());
		}
		changedRealTime(options);

		changedLocalControls(options);
		if (redraw) {
			getChart().update();
		}
	}

	@Override
	protected boolean isRealTime() {
		return (getGraphWidget() != null) && getGraphWidget().getOptions().getRealTime();
	}

	@Override
	public boolean refresh() {
		if (((GraphicWidget) getWidget()).getFeeds().isEmpty()) {
			return false;
		}

		if (buckets.isEmpty()) {
			return false;
		}
		boolean redraw = false;
		Date endX = buckets.get(buckets.size() - 1).upperEndpoint();
		Date max = new Date(endX.getTime() + (interpolation.getSeconds() / 2) * 1000);
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
		Map<Integer, Interpolation> suggested = new HashMap<>();
		Range<Date> range = Range.closedOpen(interval.getStartDate(), interval.getEndDate());
		for (Interpolation interpolation : UIUtils.getCassandraService().getRollup().availableInterpolations(range, null, null)) {
			if (seconds > interpolation.getSeconds() && interpolation.getSeconds() > 0) {
				int count = (int) ((float) seconds / (float) interpolation.getSeconds());
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
			count = (int) ((float) seconds / (float) interpolation.getSeconds());
		}
		return (count > 1 && count <= maxCategories);
	}

	private int getMaxColumns() {
		return 72;
	}

	private String categoryName(Range<Date> interval, SimpleDateFormat sdf, Interpolation interpolation) {
		switch (interpolation) {
		case D1:
			sdf.applyPattern("d.MMM");
			break;
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
	public ChartJs getChart() {
		return chart;
	}

	@Override
	protected void setChart(Component chart) {
		this.chart = (ChartJs) chart;
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
