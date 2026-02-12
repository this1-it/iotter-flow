package it.thisone.iotter.ui.charts;

import java.text.ChoiceFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Range;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;

import org.vaadin.addons.chartjs.ChartJs;
import org.vaadin.addons.chartjs.config.BarChartConfig;
import org.vaadin.addons.chartjs.data.BarDataset;
import org.vaadin.addons.chartjs.options.Position;
import org.vaadin.addons.chartjs.options.scale.Axis;
import org.vaadin.addons.chartjs.options.scale.CategoryScale;
import org.vaadin.addons.chartjs.options.scale.LinearScale;

import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.cassandra.model.SummaryVariation;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GraphicWidgetOptions;
import it.thisone.iotter.ui.common.charts.ChannelUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.model.TimeInterval;

public class VariationChartAdapter extends AbstractChartAdapter {

	private ChartJs chart;
	private BarChartConfig chartConfig;
	private CategoryScale xScale;
	private LinearScale yScale;

	private List<String> categories;
	private List<Double> limits = new ArrayList<>();

	private SimpleDateFormat sdf;

	private static final long serialVersionUID = -1958076735452737593L;

	public VariationChartAdapter(GraphicWidget widget) {
		super(widget);
		optionsField.getRealTime().setVisible(false);
		optionsField.getScale().setVisible(false);
		optionsField.getAutoScale().setVisible(false);
		optionsField.getShowMarkers().setVisible(false);
		optionsField.getShowGrid().setVisible(false);
		sdf = new SimpleDateFormat(ChartUtils.DATE_FORMAT, UI.getCurrent().getLocale());
		sdf.setTimeZone(getNetworkTimeZone());
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

	@Override
	protected void createChartConfiguration(TimeInterval time) {
		if (getGraphWidget().getFeeds().isEmpty()) {
			return;
		}

		GraphicFeed feed = getGraphWidget().getFeeds().get(0);
		ChoiceFormat formatter = ChannelUtils.enumChoiceFormat(feed.getChannel());
		if (formatter == null) {
			return;
		}
		categories = new ArrayList<>();
		limits = new ArrayList<>();
		for (int i = 0; i < formatter.getLimits().length; i++) {
			limits.add(formatter.getLimits()[i]);
			categories.add((String) formatter.getFormats()[i]);
		}

		chartConfig = createConfiguration(feed);
		chartConfig.data().labelsAsList(categories);

		List<Double> values = new ArrayList<>();
		for (int i = 0; i < categories.size(); i++) {
			values.add(null);
		}

		if (feed.getChannel() != null) {
			int points = 0;
			int step = 0;

			List<Range<Date>> validities = getValidities().get(feed.getKey());
			FeedKey feedKey = new FeedKey(feed.getDevice().getSerial(), feed.getKey());
			feedKey.setQualifier(feed.getChannel().getConfiguration().getQualifier());
			List<MeasureRaw> measures = ChartUtils.getData(feedKey, time.getStartDate(), time.getEndDate(), points, step,
					validities, getNetworkTimeZone());
			sdf.applyPattern(ChartUtils.DATE_FORMAT);
			boolean positive = true;

			SummaryVariation summary = new SummaryVariation(limits);
			summary.addAll(measures);

			Map<Double, Integer> stats = summary.getVariations();
			for (int index = 0; index < limits.size(); index++) {
				Integer value = stats.get(limits.get(index));
				if (value != null && value > 0) {
					values.set(index, value.doubleValue());
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
		TimeInterval interval = intervalField.getValue();
		if (interval != null) {
			createChartConfiguration(interval);
		}
		if (chartConfig != null) {
			getChart().update();
		}
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
		return false;
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

}
