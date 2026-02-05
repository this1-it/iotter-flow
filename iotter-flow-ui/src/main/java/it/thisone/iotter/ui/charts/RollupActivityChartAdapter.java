package it.thisone.iotter.ui.charts;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Range;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.AxisTitle;
import com.vaadin.addon.charts.model.AxisType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.PlotOptionsSpline;
import com.vaadin.addon.charts.model.PointOptions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.cassandra.model.MeasureAggregation;
import it.thisone.iotter.enums.Period;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.model.TimeInterval;
import it.thisone.iotter.ui.model.TimePeriod;
import it.thisone.iotter.ui.model.TimePeriod.TimePeriodEnum;

	// TODO(flow-migration): this class still contains Vaadin 8 APIs and needs manual Flow refactor.
public class RollupActivityChartAdapter extends MultiTraceChartAdapter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1879789719298795301L;

	public RollupActivityChartAdapter(GraphicWidget widget) {
		super(widget);
	}

	@Override
	protected Component buildVisualization() {
		if (getGraphWidget().getFeeds().isEmpty()) {
			return new VerticalLayout();
		}
		Chart chart = getRollupActivityChart();
		setChart(chart);
		return chart;
	}

	private Chart getRollupActivityChart() {
		GraphicFeed feed = getGraphWidget().getFeeds().get(0);
		
		TimePeriod period = new TimePeriod(Period.WEEK, 1, TimePeriodEnum.LAST);
		TimeInterval interval = intervalField.getHelper().period(new Date(), period);
		Chart chart = new Chart();
		chart.setId("detail-chart");
//		chart.setHeight("100%");
//		chart.setWidth("100%");
		Configuration configuration = chart.getConfiguration();
		configuration.getCredits().setEnabled(false);
		configuration.setTitle("");
		configuration.setSubTitle("");
		configuration.getxAxis().setType(AxisType.DATETIME);
		configuration.getyAxis().setTitle(new AxisTitle((String) null));
		configuration.getyAxis().setMinRange(0.1);
		configuration.getTooltip().setXDateFormat(ChartUtils.X_DATEFORMAT);
		configuration.getTooltip().setShared(true);
		configuration.getLegend().setEnabled(false);
		configuration.setExporting(false);
		DataSeries series = getRollupSeries(feed, interval);
		configuration.addSeries(series);
		chart.drawChart(configuration);
		return chart;
	}

	protected DataSeries getRollupSeries(GraphicFeed feed, TimeInterval interval) {
		DataSeries series = new DataSeries();
		PointOptions linePlotOptions = new PlotOptionsSpline();
		linePlotOptions.setLineWidth(ChartUtils.PLOT_LINE_WIDTH);
		linePlotOptions.setShadow(false);
		linePlotOptions.setAnimation(false);
		series.setPlotOptions(linePlotOptions);
		
		series.setId(feed.getKey());
		Range<Date> range = Range.closedOpen(interval.getStartDate(), interval.getEndDate());
		List<MeasureAggregation> measures = UIUtils.getCassandraService().getRollup().rollUpData(feed.getKey(),
				Interpolation.MIN15, range);
		for (MeasureAggregation measure : measures) {
			DataSeriesItem item = new DataSeriesItem(measure.getDate(), measure.getRecords());
			long timestamp = ChartUtils.toHighchartsTS(measure.getDate(), getNetworkTimeZone());
			item.setX(timestamp);
			series.add(item);
		}

		return series;
	}

}
