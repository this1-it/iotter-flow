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

import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.cassandra.model.SummaryVariation;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GraphicWidgetOptions;
import it.thisone.iotter.ui.common.charts.ChannelUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.common.charts.bridge.BarDataset;
import it.thisone.iotter.ui.common.charts.bridge.ChartConfig;
import it.thisone.iotter.ui.common.charts.bridge.ChartJsBridge;
import it.thisone.iotter.ui.common.charts.bridge.ScaleConfig;
import it.thisone.iotter.ui.model.TimeInterval;
import it.thisone.iotter.ui.providers.BackendServices;

public class VariationChartAdapter extends AbstractChartAdapter {

    private ChartJsBridge chart;
    private ChartConfig chartConfig;
    private ScaleConfig xScale;
    private ScaleConfig yScale;

    private List<String> categories;
    private List<Double> limits = new ArrayList<>();

    private SimpleDateFormat sdf;

    private static final long serialVersionUID = -1958076735452737593L;

    public VariationChartAdapter(GraphicWidget widget, BackendServices backendServices) {
        super(widget, backendServices);
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
        ChartJsBridge chart = new ChartJsBridge();
        chart.setId(String.valueOf(getWidget().getId()));
        setChart(chart);
        chartConfig = createEmptyConfiguration();
        chart.configure(chartConfig);
        return chart;
    }

    private ChartConfig createEmptyConfiguration() {
        ChartConfig configuration = new ChartConfig("bar");
        configuration.getOptions().setResponsive(true).setMaintainAspectRatio(false);
        configuration.getOptions().getPlugins().getLegend().setPosition("bottom");
        configuration.getOptions().getPlugins().getLegend().setDisplay(getGraphWidget().getOptions().getShowLegend());
        xScale = ScaleConfig.category();
        yScale = ScaleConfig.linear();
        applyGridVisibility(getGraphWidget().getOptions().getShowGrid());
        configuration.getOptions().addScale("x", xScale);
        configuration.getOptions().addScale("y", yScale);
        return configuration;
    }

    private ChartConfig createConfiguration(GraphicFeed feed) {
        String feedColor = feed.getOptions().getFillColor();
        String feedLabel = ChartUtils.getFeedLabel(feed, backendServices.getDeviceService());
        ChartConfig configuration = createEmptyConfiguration();
        configuration.getOptions().getPlugins().getTitle().setDisplay(true).setText(feedLabel).setColor(feedColor);
        configuration.getOptions().getPlugins().getTitle().getFont().setSize(12);
        return configuration;
    }

    private void applyGridVisibility(boolean showGrid) {
        int gridLineWidth = showGrid ? ((Number) ChartUtils.GRID_LINE_WIDTH).intValue() : 0;
        xScale.getGrid().setDisplay(showGrid).setLineWidth(gridLineWidth);
        yScale.getGrid().setDisplay(showGrid).setLineWidth(gridLineWidth);
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
        chartConfig.getData().setLabels(categories);

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
                    validities, getNetworkTimeZone(),
                    backendServices.getCassandraMeasures(), backendServices.getCassandraRollup());
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
            yScale.getTicks().setBeginAtZero(positive);
        }

        String feedLabel = ChartUtils.getFeedLabel(feed, backendServices.getDeviceService());
        BarDataset dataset = new BarDataset();
        dataset.setLabel(feedLabel);
        dataset.dataAsList(values);
        dataset.setBackgroundColor(feed.getOptions().getFillColor());
        dataset.setStack("values");
        chartConfig.getData().addDataset(dataset);
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
            applyGridVisibility(options.getShowGrid());
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
    public ChartJsBridge getChart() {
        return chart;
    }

    @Override
    protected void setChart(Component chart) {
        this.chart = (ChartJsBridge) chart;
    }

}
