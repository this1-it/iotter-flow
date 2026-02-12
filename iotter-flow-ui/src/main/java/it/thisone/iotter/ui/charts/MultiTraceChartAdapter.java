package it.thisone.iotter.ui.charts;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.chartjs.ChartJs;
import org.vaadin.addons.chartjs.config.LineChartConfig;
import org.vaadin.addons.chartjs.data.TimeLineDataset;
import org.vaadin.addons.chartjs.options.Position;
import org.vaadin.addons.chartjs.options.scale.Axis;
import org.vaadin.addons.chartjs.options.scale.BaseScale;
import org.vaadin.addons.chartjs.options.scale.LinearScale;
import org.vaadin.addons.chartjs.options.scale.LogarithmicScale;
import org.vaadin.addons.chartjs.options.scale.TimeScale;
import org.vaadin.addons.chartjs.utils.Pair;

import com.vaadin.flow.component.Component;

import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.enums.ChartScaleType;
import it.thisone.iotter.exceptions.MeasureException;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GraphicWidgetOptions;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.model.TimeInterval;

public class MultiTraceChartAdapter extends AbstractChartAdapter {

    private static final long serialVersionUID = 5817286715447264313L;
    private static final Logger logger = LoggerFactory.getLogger(MultiTraceChartAdapter.class);

    private final Map<String, Date> refreshed = new HashMap<>();
    private final Map<String, TimeLineDataset> datasetsByFeed = new LinkedHashMap<>();

    private ChartJs chart;
    private LineChartConfig chartConfig;
    private TimeScale xScale;
    private BaseScale<?> yScale;
    private TimeInterval currentInterval;

    public MultiTraceChartAdapter(GraphicWidget widget) {
        super(widget);
        optionsField.getAutoScale().setVisible(widget.hasExtremes());
    }

    @Override
    protected Component buildVisualization() {
        ChartJs chart = new ChartJs();
        chart.setId(String.valueOf(getWidget().getId()));
        setChart(chart);
        chartConfig = createBaseConfiguration();
        chart.configure(chartConfig);
        return chart;
    }

    protected LineChartConfig createBaseConfiguration() {
        LineChartConfig config = new LineChartConfig();
        config.options().responsive(true).maintainAspectRatio(false);
        config.options().legend().display(getGraphWidget().getOptions().getShowLegend()).position(Position.BOTTOM);

        xScale = new TimeScale();
        xScale.time().tooltipFormat(ChartUtils.X_DATEFORMAT);
        applyGridVisibility(getGraphWidget().getOptions().getShowGrid());
        config.options().scales().add(Axis.X, xScale);

        yScale = buildYScale(getGraphWidget().getOptions().getScale());
        applyGridVisibility(getGraphWidget().getOptions().getShowGrid());
        config.options().scales().add(Axis.Y, yScale);

        return config;
    }

    private BaseScale<?> buildYScale(ChartScaleType scaleType) {
        if (scaleType == ChartScaleType.LOGARITHMIC) {
            LogarithmicScale log = new LogarithmicScale();
            log.ticks().beginAtZero(false);
            return log;
        }
        LinearScale linear = new LinearScale();
        linear.ticks().beginAtZero(false);
        return linear;
    }

    private void applyGridVisibility(boolean showGrid) {
        int width = showGrid ? ((Number) ChartUtils.GRID_LINE_WIDTH).intValue() : 0;
        if (xScale != null) {
            xScale.gridLines().display(showGrid).lineWidth(width);
        }
        if (yScale != null) {
            yScale.gridLines().display(showGrid).lineWidth(width);
        }
    }

    @Override
    public void createChartConfiguration(TimeInterval interval) {
        currentInterval = interval;
        chartConfig = createBaseConfiguration();
        datasetsByFeed.clear();

        for (GraphicFeed feed : getGraphWidget().getFeeds()) {
            TimeLineDataset dataset = createFeedDataset(feed, interval, null);
            datasetsByFeed.put(feed.getKey(), dataset);
            chartConfig.data().addDataset(dataset);
        }

        getChart().configure(chartConfig);
        applyExtremes(interval);
    }

    private TimeLineDataset createFeedDataset(GraphicFeed feed, TimeInterval interval, Float ratio) {
        TimeLineDataset dataset = new TimeLineDataset();
        dataset.label(ChartUtils.getFeedLabel(feed));
        dataset.borderColor(resolveColor(feed));
        dataset.backgroundColor(resolveColor(feed));
        dataset.borderWidth(ChartUtils.PLOT_LINE_WIDTH.intValue());
        dataset.fill(false);
        int markerRadius = ((Number) ChartUtils.MARKER_RADIUS).intValue();
        dataset.pointRadius(getGraphWidget().getOptions().getShowMarkers() ? markerRadius : 0);

        List<Pair<java.time.LocalDateTime, Double>> points = loadPoints(feed, interval, ratio);
        dataset.dataAsList(points);
        return dataset;
    }

    private String resolveColor(GraphicFeed feed) {
        if (feed.getOptions().getFillColor() == null) {
            feed.getOptions().setFillColor(ChartUtils.quiteRandomHexColor());
        }
        return feed.getOptions().getFillColor();
    }

    private List<Pair<java.time.LocalDateTime, Double>> loadPoints(GraphicFeed feed, TimeInterval interval, Float ratio) {
        List<Pair<java.time.LocalDateTime, Double>> data = new ArrayList<>();
        if (feed.getChannel() == null) {
            return data;
        }

        Date from = interval.getStartDate();
        Date to = interval.getEndDate();
        Date now = new Date();
        if (to.after(now)) {
            to = now;
        }
        if (to.before(from)) {
            return data;
        }

        int points = Math.max(1, (int) Math.ceil(getWidget().getWidth() * 1000f));
        if (ratio != null) {
            points = (int) Math.ceil(points * ratio);
        }
        int step = points > 0 ? (int) (to.getTime() - from.getTime()) / points : 0;

        FeedKey feedKey = new FeedKey(feed.getDevice().getSerial(), feed.getKey());
        feedKey.setQualifier(feed.getChannel().getConfiguration().getQualifier());
        List<MeasureRaw> measures = ChartUtils.getData(feedKey, from, to, points, step,
                getValidities().get(feed.getKey()), getNetworkTimeZone());

        for (MeasureRaw measure : measures) {
            if (measure == null || !measure.isValid() || measure.getValue() == null) {
                continue;
            }
            try {
                Number value = ChartUtils.calculateMeasure(measure.getValue(), feed.getMeasure());
                data.add(Pair.of(toLocalDateTime(measure.getDate()), value.doubleValue()));
            } catch (MeasureException e) {
                logger.debug("Invalid measure for feed {}: {}", feed.getKey(), e.getMessage());
            }
        }

        // TODO(flow-chartjs): marker-reference arrow symbols are not available in Chart.js addon.
        return data;
    }

    @Override
    public void draw() {
        TimeInterval interval = intervalField.getValue();
        if (interval == null) {
            return;
        }

        if (currentInterval == null || !currentInterval.equals(interval)) {
            createChartConfiguration(interval);
        }

        applyExtremes(interval);
        getChart().update();
    }

    private void applyExtremes(TimeInterval interval) {
        if (xScale == null) {
            return;
        }
        Date start = interval.getStartDate();
        Date end = interval.getEndDate();

        if (isRealTime()) {
            long quarter = periodField.getValue().getTime() / 4;
            end = new Date(end.getTime() + quarter);
        }

        xScale.time().min(toLocalDateTime(start)).max(toLocalDateTime(end));
    }

    @Override
    public boolean refresh() {
        if (((GraphicWidget) getWidget()).getFeeds().isEmpty()) {
            return false;
        }

        if (refreshed.isEmpty()) {
            createRefreshed();
        }

        boolean redraw = false;
        Date now = new Date();

        for (GraphicFeed feed : getGraphWidget().getFeeds()) {
            TimeLineDataset dataset = datasetsByFeed.get(feed.getKey());
            if (dataset == null) {
                continue;
            }

            Date from = refreshed.get(feed.getKey());
            if (from == null) {
                from = refreshedDate(feed);
                refreshed.put(feed.getKey(), from);
            }
            if (!from.before(now)) {
                continue;
            }

            float ratio = (float) periodField.getValue().getTime() / (float) Math.max(1L, (now.getTime() - from.getTime()));
            List<Pair<java.time.LocalDateTime, Double>> points = loadPoints(feed, new TimeInterval(from, now), ratio);

            long lastMillis = getLastMillis(dataset);
            for (Pair<java.time.LocalDateTime, Double> point : points) {
                long millis = point.getFirst().atZone(getNetworkTimeZone().toZoneId()).toInstant().toEpochMilli();
                if (millis > lastMillis) {
                    dataset.addData(point);
                    lastMillis = millis;
                    redraw = true;
                }
            }
            refreshed.put(feed.getKey(), new Date(now.getTime() + 1));
        }

        return redraw;
    }

    private long getLastMillis(TimeLineDataset dataset) {
        List<Pair<java.time.LocalDateTime, Double>> data = dataset.getData();
        if (data == null || data.isEmpty()) {
            return Long.MIN_VALUE;
        }
        Pair<java.time.LocalDateTime, Double> last = data.get(data.size() - 1);
        return last.getFirst().atZone(getNetworkTimeZone().toZoneId()).toInstant().toEpochMilli();
    }

    @Override
    public void setGraphWidgetOptionsOnChange(GraphicWidgetOptions options) {
        boolean redraw = false;

        if (getGraphWidget().getOptions().getShowGrid() != options.getShowGrid()) {
            getGraphWidget().getOptions().setShowGrid(options.getShowGrid());
            applyGridVisibility(options.getShowGrid());
            redraw = true;
        }

        if (options.getScale() != null && getGraphWidget().getOptions().getScale() != options.getScale()) {
            getGraphWidget().getOptions().setScale(options.getScale());
            if (intervalField.getValue() != null) {
                createChartConfiguration(intervalField.getValue());
            }
            redraw = true;
        }

        if (getGraphWidget().getOptions().getShowLegend() != options.getShowLegend()) {
            getGraphWidget().getOptions().setShowLegend(options.getShowLegend());
            chartConfig.options().legend().display(options.getShowLegend());
            redraw = true;
        }

        if (getGraphWidget().getOptions().getShowMarkers() != options.getShowMarkers()) {
            getGraphWidget().getOptions().setShowMarkers(options.getShowMarkers());
            int markerRadius = ((Number) ChartUtils.MARKER_RADIUS).intValue();
            for (TimeLineDataset ds : datasetsByFeed.values()) {
                ds.pointRadius(options.getShowMarkers() ? markerRadius : 0);
            }
            redraw = true;
        }

        changedLocalControls(options);
        if (changedRealTime(options)) {
            redraw = true;
        }

        if (redraw) {
            getChart().update();
        }
    }

    @Override
    public boolean isRealTime() {
        return (getGraphWidget() != null) && getGraphWidget().getOptions().getRealTime();
    }

    @Override
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
        }
        return changeRealTime;
    }

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

    public void addMeasure(MeasureRaw measure) {
        if (measure == null) {
            return;
        }
        TimeLineDataset dataset = datasetsByFeed.get(measure.getKey());
        if (dataset == null || measure.getValue() == null) {
            return;
        }
        dataset.addData(toLocalDateTime(measure.getDate()), measure.getValue().doubleValue());
        getChart().update();
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
