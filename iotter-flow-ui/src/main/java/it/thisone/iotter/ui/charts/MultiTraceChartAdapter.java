package it.thisone.iotter.ui.charts;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;

import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.enums.ChartScaleType;
import it.thisone.iotter.exceptions.MeasureException;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GraphicWidgetOptions;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.common.charts.bridge.ChartConfig;
import it.thisone.iotter.ui.common.charts.bridge.ChartJsBridge;
import it.thisone.iotter.ui.common.charts.bridge.ScaleConfig;
import it.thisone.iotter.ui.common.charts.bridge.TimeLineDataset;
import it.thisone.iotter.ui.common.charts.bridge.TimePoint;
import it.thisone.iotter.ui.model.TimeInterval;
import it.thisone.iotter.ui.providers.BackendServices;


public class MultiTraceChartAdapter extends AbstractChartAdapter {

    private static final long serialVersionUID = 5817286715447264313L;
    private static final Logger logger = LoggerFactory.getLogger(MultiTraceChartAdapter.class);

    private final Map<String, Date> refreshed = new HashMap<>();
    private final Map<String, TimeLineDataset> datasetsByFeed = new LinkedHashMap<>();
    private final Map<String, Integer> datasetIndexByFeed = new LinkedHashMap<>();

    private ChartJsBridge chart;
    private ChartConfig chartConfig;
    private ScaleConfig xScale;
    private ScaleConfig yScale;
    private TimeInterval currentInterval;

    public MultiTraceChartAdapter(GraphicWidget widget, BackendServices backendServices) {
        super(widget, backendServices);
        optionsField.getAutoScale().setVisible(widget.hasExtremes());
    }

    @Override
    protected Component buildVisualization() {
        ChartJsBridge chart = new ChartJsBridge();
        chart.setId(String.valueOf(getWidget().getId()));
        setChart(chart);
        chartConfig = createBaseConfiguration();
        chart.configure(chartConfig);
        return chart;
    }

    protected ChartConfig createBaseConfiguration() {
        ChartConfig config = new ChartConfig("line");
        config.getData().setLabels(new ArrayList<>());
        config.getOptions().setResponsive(true).setMaintainAspectRatio(false);
        config.getOptions().getPlugins().getLegend()
                .setDisplay(getGraphWidget().getOptions().getShowLegend())
                .setPosition("bottom");

        xScale = ScaleConfig.time();
        xScale.getTime().setTooltipFormat(ChartUtils.X_DATEFORMAT);
        applyGridVisibility(getGraphWidget().getOptions().getShowGrid());
        config.getOptions().addScale("x", xScale);

        yScale = buildYScale(getGraphWidget().getOptions().getScale());
        applyGridVisibility(getGraphWidget().getOptions().getShowGrid());
        config.getOptions().addScale("y", yScale);

        return config;
    }

    private ScaleConfig buildYScale(ChartScaleType scaleType) {
        if (scaleType == ChartScaleType.LOGARITHMIC) {
            ScaleConfig log = ScaleConfig.logarithmic();
            log.getTicks().setBeginAtZero(false);
            return log;
        }
        ScaleConfig linear = ScaleConfig.linear();
        linear.getTicks().setBeginAtZero(false);
        return linear;
    }

    private void applyGridVisibility(boolean showGrid) {
        int width = showGrid ? ((Number) ChartUtils.GRID_LINE_WIDTH).intValue() : 0;
        if (xScale != null) {
            xScale.getGrid().setDisplay(showGrid).setLineWidth(width);
        }
        if (yScale != null) {
            yScale.getGrid().setDisplay(showGrid).setLineWidth(width);
        }
    }

    @Override
    public void createChartConfiguration(TimeInterval interval) {
        currentInterval = interval;
        chartConfig = createBaseConfiguration();
        datasetsByFeed.clear();
        datasetIndexByFeed.clear();

        int index = 0;
        for (GraphicFeed feed : getGraphWidget().getFeeds()) {
            TimeLineDataset dataset = createFeedDataset(feed, interval, null);
            datasetsByFeed.put(feed.getKey(), dataset);
            datasetIndexByFeed.put(feed.getKey(), index);
            chartConfig.getData().addDataset(dataset);
            index++;
        }

        getChart().configure(chartConfig);
        applyExtremes(interval);
    }

    private TimeLineDataset createFeedDataset(GraphicFeed feed, TimeInterval interval, Float ratio) {
        TimeLineDataset dataset = new TimeLineDataset();
        dataset.setLabel(ChartUtils.getFeedLabel(feed, backendServices.getDeviceService()));
        dataset.setBorderColor(resolveColor(feed));
        dataset.setBackgroundColor(resolveColor(feed));
        dataset.setBorderWidth(ChartUtils.PLOT_LINE_WIDTH.intValue());
        dataset.setFill(false);
        int markerRadius = ((Number) ChartUtils.MARKER_RADIUS).intValue();
        dataset.setPointRadius(getGraphWidget().getOptions().getShowMarkers() ? markerRadius : 0);

        List<TimePoint> points = loadPoints(feed, interval, ratio);
        dataset.dataAsList(points);
        return dataset;
    }

    private String resolveColor(GraphicFeed feed) {
        if (feed.getOptions().getFillColor() == null) {
            feed.getOptions().setFillColor(ChartUtils.quiteRandomHexColor());
        }
        return feed.getOptions().getFillColor();
    }

    private List<TimePoint> loadPoints(GraphicFeed feed, TimeInterval interval, Float ratio) {
        List<TimePoint> data = new ArrayList<>();
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

        int points = 1000;
        if (ratio != null) {
            points = (int) Math.ceil(points * ratio);
        }
        int step = points > 0 ? (int) (to.getTime() - from.getTime()) / points : 0;

        FeedKey feedKey = new FeedKey(feed.getDevice().getSerial(), feed.getKey());
        feedKey.setQualifier(feed.getChannel().getConfiguration().getQualifier());
        List<MeasureRaw> measures = ChartUtils.getData(feedKey, from, to, points, step,
                getValidities().get(feed.getKey()), getNetworkTimeZone(),
                backendServices.getCassandraMeasures(), backendServices.getCassandraRollup());

        for (MeasureRaw measure : measures) {
            if (measure == null || !measure.isValid() || measure.getValue() == null) {
                continue;
            }
            try {
                Number value = ChartUtils.calculateMeasure(measure.getValue(), feed.getMeasure());
                data.add(TimePoint.of(toLocalDateTime(measure.getDate()), value.doubleValue()));
            } catch (MeasureException e) {
                logger.debug("Invalid measure for feed {}: {}", feed.getKey(), e.getMessage());
            }
        }

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

        java.time.LocalDateTime minDt = toLocalDateTime(start);
        java.time.LocalDateTime maxDt = toLocalDateTime(end);
        xScale.getTime().setMin(minDt.toString()).setMax(maxDt.toString());

        // For live chart updates without full re-render
        getChart().updateScaleBounds("x", minDt, maxDt);
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
            Integer dsIndex = datasetIndexByFeed.get(feed.getKey());
            if (dataset == null || dsIndex == null) {
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
            List<TimePoint> points = loadPoints(feed, new TimeInterval(from, now), ratio);

            long lastMillis = getLastMillis(dataset);
            for (TimePoint point : points) {
                long millis = point.getDateTime().atZone(getNetworkTimeZone().toZoneId()).toInstant().toEpochMilli();
                if (millis > lastMillis) {
                    dataset.addData(point);
                    // Real-time append via JS interop — no chart destroy/recreate
                    chart.addDataPoint(dsIndex, point.getDateTime(), point.getY());
                    lastMillis = millis;
                    redraw = true;
                }
            }
            refreshed.put(feed.getKey(), new Date(now.getTime() + 1));
        }

        return redraw;
    }

    private long getLastMillis(TimeLineDataset dataset) {
        List<TimePoint> data = dataset.getData();
        if (data == null || data.isEmpty()) {
            return Long.MIN_VALUE;
        }
        TimePoint last = data.get(data.size() - 1);
        return last.getDateTime().atZone(getNetworkTimeZone().toZoneId()).toInstant().toEpochMilli();
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
            chartConfig.getOptions().getPlugins().getLegend().setDisplay(options.getShowLegend());
            redraw = true;
        }

        if (getGraphWidget().getOptions().getShowMarkers() != options.getShowMarkers()) {
            getGraphWidget().getOptions().setShowMarkers(options.getShowMarkers());
            int markerRadius = ((Number) ChartUtils.MARKER_RADIUS).intValue();
            for (TimeLineDataset ds : datasetsByFeed.values()) {
                ds.setPointRadius(options.getShowMarkers() ? markerRadius : 0);
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
            date = ChartUtils.lastTick(feed.getChannel().getDevice().getSerial(), backendServices.getCassandraMeasures());
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
        Integer dsIndex = datasetIndexByFeed.get(measure.getKey());
        if (dataset == null || dsIndex == null || measure.getValue() == null) {
            return;
        }
        java.time.LocalDateTime dt = toLocalDateTime(measure.getDate());
        dataset.addData(dt, measure.getValue().doubleValue());
        chart.addDataPoint(dsIndex, dt, measure.getValue().doubleValue());
    }

    @Override
    public ChartJsBridge getChart() {
        return chart;
    }

    @Override
    protected void setChart(Component chart) {
        this.chart = (ChartJsBridge) chart;
    }
}
