package it.thisone.iotter.ui.charts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.vaadin.addons.chartjs.ChartJs;
import org.vaadin.addons.chartjs.config.LineChartConfig;
import org.vaadin.addons.chartjs.data.TimeLineDataset;
import org.vaadin.addons.chartjs.options.Position;
import org.vaadin.addons.chartjs.options.scale.Axis;
import org.vaadin.addons.chartjs.options.scale.LinearScale;
import org.vaadin.addons.chartjs.options.scale.TimeScale;
import org.vaadin.addons.chartjs.utils.Pair;

import com.google.common.collect.Range;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.cassandra.model.MeasureAggregation;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GraphicWidgetOptions;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.model.TimeInterval;

public class RollupActivityChartAdapter extends AbstractChartAdapter {

    private static final long serialVersionUID = -1879789719298795301L;

    private ChartJs chart;
    private LineChartConfig chartConfig;
    private TimeScale xScale;

    public RollupActivityChartAdapter(GraphicWidget widget) {
        super(widget);
        optionsField.getScale().setVisible(false);
        optionsField.getShowMarkers().setVisible(false);
        optionsField.getAutoScale().setVisible(false);
    }

    @Override
    protected Component buildVisualization() {
        if (getGraphWidget().getFeeds().isEmpty()) {
            return new VerticalLayout();
        }
        ChartJs chart = new ChartJs();
        chart.setId("rollup-" + getWidget().getId());
        setChart(chart);
        chartConfig = createBaseConfiguration();
        chart.configure(chartConfig);
        return chart;
    }

    private LineChartConfig createBaseConfiguration() {
        LineChartConfig config = new LineChartConfig();
        config.options().responsive(true).maintainAspectRatio(false);
        config.options().legend().display(false).position(Position.BOTTOM);

        xScale = new TimeScale();
        xScale.time().tooltipFormat(ChartUtils.X_DATEFORMAT);
        LinearScale yScale = new LinearScale();
        yScale.ticks().beginAtZero(true);

        int grid = getGraphWidget().getOptions().getShowGrid() ? ((Number) ChartUtils.GRID_LINE_WIDTH).intValue() : 0;
        xScale.gridLines().display(getGraphWidget().getOptions().getShowGrid()).lineWidth(grid);
        yScale.gridLines().display(getGraphWidget().getOptions().getShowGrid()).lineWidth(grid);

        config.options().scales().add(Axis.X, xScale).add(Axis.Y, yScale);
        return config;
    }

    @Override
    protected void createChartConfiguration(TimeInterval interval) {
        if (getGraphWidget().getFeeds().isEmpty() || chart == null) {
            return;
        }

        GraphicFeed feed = getGraphWidget().getFeeds().get(0);
        chartConfig = createBaseConfiguration();
        chartConfig.options().title().display(true).text(getWidget().getLabel());

        TimeLineDataset dataset = new TimeLineDataset();
        dataset.label(ChartUtils.getFeedLabel(feed));
        dataset.borderColor(feed.getOptions().getFillColor() == null ? ChartUtils.quiteRandomHexColor() : feed.getOptions().getFillColor());
        dataset.backgroundColor(feed.getOptions().getFillColor() == null ? ChartUtils.quiteRandomHexColor() : feed.getOptions().getFillColor());
        dataset.fill(false);
        dataset.pointRadius(0);

        Range<Date> range = Range.closedOpen(interval.getStartDate(), interval.getEndDate());
        List<MeasureAggregation> measures = UIUtils.getCassandraService().getRollup().rollUpData(feed.getKey(), Interpolation.MIN15, range);
        List<Pair<java.time.LocalDateTime, Double>> data = new ArrayList<>();
        for (MeasureAggregation measure : measures) {
            data.add(Pair.of(toLocalDateTime(measure.getDate()), (double) measure.getRecords()));
        }
        dataset.dataAsList(data);

        chartConfig.data().addDataset(dataset);
        xScale.time().min(toLocalDateTime(interval.getStartDate())).max(toLocalDateTime(interval.getEndDate()));
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
        this.chart = (ChartJs) chart;
    }
}
