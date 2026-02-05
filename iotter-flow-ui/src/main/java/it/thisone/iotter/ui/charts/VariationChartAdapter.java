package it.thisone.iotter.ui.charts;

import java.text.ChoiceFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.cassandra.model.SummaryVariation;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GraphicWidgetOptions;
import it.thisone.iotter.ui.common.charts.ChannelUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.model.TimeInterval;

	// TODO(flow-migration): this class still contains Vaadin 8 APIs and needs manual Flow refactor.
public class VariationChartAdapter extends AbstractChartAdapter {

	private Chart chart;

	private List<String> categories;
	private List<Double> limits = new ArrayList<>();

	private SimpleDateFormat sdf;

	/**
	 * 
	 */
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

		float gridLineWidth = getGraphWidget().getOptions().getShowGrid() ? (Float) ChartUtils.GRID_LINE_WIDTH : 0f;

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



	protected AbstractPlotOptions getPlotOptions(GraphicFeed feed) {
		String feedColor = feed.getOptions().getFillColor();
		PlotOptionsColumn plotOptions = new PlotOptionsColumn();
		plotOptions.setColor(new SolidColor(feedColor));
		plotOptions.setStacking(Stacking.NORMAL);
		plotOptions.setShadow(false);
		/**
		 * Sets the pixel value specifying a fixed width for each column or bar.
		 * Default null. If not set, the width is calculated from pointPadding
		 * and groupPadding.
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
		 * Sets the padding between each value group, in X-axis units. Defaults
		 * to 0.2.
		 */
		plotOptions.setGroupPadding(0.1);
		return plotOptions;
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
		categories = new ArrayList<String>();
		limits = new ArrayList<Double>();
		for (int i = 0; i < formatter.getLimits().length; i++) {
			limits.add(formatter.getLimits()[i]);
			categories.add((String)formatter.getFormats()[i]);
		}

		Configuration configuration = createConfiguration(feed);
		getChart().setConfiguration(configuration);
		List<Number> values = new ArrayList<Number>();
		for (int i = 0; i < categories.size(); i++) {
			values.add(null);
		}
		
		if (feed.getChannel() != null) {
			//int points = (int) getWidth();
			int points = 0;
			int step = 0;
			
			List<Range<Date>> validities = getValidities().get(feed.getKey());
			FeedKey feedKey = new FeedKey(feed.getDevice().getSerial(), feed.getKey());
			feedKey.setQualifier(feed.getChannel().getConfiguration().getQualifier());
			List<MeasureRaw> measures = ChartUtils.getData(
					feedKey, time.getStartDate(), time.getEndDate(),
					points, step , validities, getNetworkTimeZone());
			sdf.applyPattern(ChartUtils.DATE_FORMAT);
			boolean positive = true;
			
			SummaryVariation summary = new SummaryVariation(limits);
			summary.addAll(measures);
			
			Map<Double, Integer> stats = summary.getVariations();
			for (int index = 0; index < limits.size(); index++) {
				Integer value = stats.get(limits.get(index));
				if (value!=null && value > 0) {
					values.set(index, value);
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
		getChart().drawChart(getChart().getConfiguration());
	}

	@Override
	protected void setGraphWidgetOptionsOnChange(GraphicWidgetOptions options) {
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
		return (getGraphWidget() != null) && getGraphWidget().getOptions().getRealTime();
	}

	@Override
	public boolean refresh() {
		if (((GraphicWidget) getWidget()).getFeeds().isEmpty())
			return false;
		boolean redraw = false;
		return redraw;
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
	public Chart getChart() {
		return chart;
	}

	@Override
	protected void setChart(Chart chart) {
		this.chart = chart;
	}

}
