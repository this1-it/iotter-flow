package it.thisone.iotter.ui.charts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Range;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.cassandra.model.MeasureAggregation;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.cassandra.CassandraRollup;
import it.thisone.iotter.persistence.model.GraphicWidgetOptions;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.common.charts.bridge.ChartConfig;
import it.thisone.iotter.ui.common.charts.bridge.ChartJsBridge;
import it.thisone.iotter.ui.common.charts.bridge.ScaleConfig;
import it.thisone.iotter.ui.common.charts.bridge.TimeLineDataset;
import it.thisone.iotter.ui.common.charts.bridge.TimePoint;
import it.thisone.iotter.ui.model.TimeInterval;
import it.thisone.iotter.ui.providers.BackendServices;

public class RollupActivityChartAdapter extends AbstractChartAdapter {

    private static final long serialVersionUID = -1879789719298795301L;

    private ChartJsBridge chart;
    private ChartConfig chartConfig;
    private ScaleConfig xScale;
    private final CassandraRollup rollup;

    public RollupActivityChartAdapter(GraphicWidget widget, BackendServices backendServices) {
        super(widget, backendServices);
        this.rollup = backendServices.getCassandraRollup();
        optionsField.getScale().setVisible(false);
        optionsField.getShowMarkers().setVisible(false);
        optionsField.getAutoScale().setVisible(false);
    }

    @Override
    protected Component buildVisualization() {
        if (getGraphWidget().getFeeds().isEmpty()) {
            return new VerticalLayout();
        }
        ChartJsBridge chart = new ChartJsBridge();
        chart.setId("rollup-" + getWidget().getId());
        setChart(chart);
        chartConfig = createBaseConfiguration();
        chart.configure(chartConfig);
        return chart;
    }

    private ChartConfig createBaseConfiguration() {
        ChartConfig config = new ChartConfig("line");
        config.getData().setLabels(new ArrayList<>());
        config.getOptions().setResponsive(true).setMaintainAspectRatio(false);
        config.getOptions().getPlugins().getLegend().setDisplay(false).setPosition("bottom");

        xScale = ScaleConfig.time();
        xScale.getTime().setTooltipFormat(ChartUtils.X_DATEFORMAT);
        ScaleConfig yScaleConfig = ScaleConfig.linear();
        yScaleConfig.getTicks().setBeginAtZero(true);

        int grid = getGraphWidget().getOptions().getShowGrid() ? ((Number) ChartUtils.GRID_LINE_WIDTH).intValue() : 0;
        xScale.getGrid().setDisplay(getGraphWidget().getOptions().getShowGrid()).setLineWidth(grid);
        yScaleConfig.getGrid().setDisplay(getGraphWidget().getOptions().getShowGrid()).setLineWidth(grid);

        config.getOptions().addScale("x", xScale);
        config.getOptions().addScale("y", yScaleConfig);
        return config;
    }

    @Override
    protected void createChartConfiguration(TimeInterval interval) {
        if (getGraphWidget().getFeeds().isEmpty() || chart == null) {
            return;
        }

        GraphicFeed feed = getGraphWidget().getFeeds().get(0);
        chartConfig = createBaseConfiguration();
        chartConfig.getOptions().getPlugins().getTitle().setDisplay(true).setText(getWidget().getLabel());

        TimeLineDataset dataset = new TimeLineDataset();
        dataset.setLabel(ChartUtils.getFeedLabel(feed, backendServices.getDeviceService()));
        dataset.setBorderColor(feed.getOptions().getFillColor() == null ? ChartUtils.quiteRandomHexColor() : feed.getOptions().getFillColor());
        dataset.setBackgroundColor(feed.getOptions().getFillColor() == null ? ChartUtils.quiteRandomHexColor() : feed.getOptions().getFillColor());
        dataset.setFill(false);
        dataset.setPointRadius(0);

        Range<Date> range = Range.closedOpen(interval.getStartDate(), interval.getEndDate());
        List<MeasureAggregation> measures = rollup.rollUpData(feed.getKey(), Interpolation.MIN15, range);
        List<TimePoint> data = new ArrayList<>();
        for (MeasureAggregation measure : measures) {
            data.add(TimePoint.of(toLocalDateTime(measure.getDate()), (double) measure.getRecords()));
        }
        dataset.dataAsList(data);

        chartConfig.getData().addDataset(dataset);
        xScale.getTime()
                .setMin(toLocalDateTime(interval.getStartDate()).toString())
                .setMax(toLocalDateTime(interval.getEndDate()).toString());
        chart.configure(chartConfig);
    }

    @Override
    public void draw() {
        TimeInterval interval = intervalField.getValue();
        if (interval == null) {
            return;
        }
        createChartConfiguration(interval);
        if (chart != null) {
            chart.update();
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

    @Override
    protected boolean changedRealTime(GraphicWidgetOptions options) {
        return false;
    }

    @Override
    public Component getChart() {
        return chart;
    }

    @Override
    protected void setChart(Component chart) {
        this.chart = (ChartJsBridge) chart;
    }
}
