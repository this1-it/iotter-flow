package it.thisone.iotter.ui.visualizers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.chartjs.ChartJs;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.enums.ChartScaleType;
import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GraphicWidgetOptions;
import it.thisone.iotter.persistence.model.MeasureRange;
import it.thisone.iotter.ui.charts.MultiTraceChartAdapter;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChannelUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.eventbus.WidgetRefreshEvent;
import it.thisone.iotter.ui.model.TimeInterval;
import it.thisone.iotter.ui.model.TimePeriod;

public class TandemTraceChartAdapter extends MultiTraceChartAdapter {

    private static final long serialVersionUID = -1879789719298795301L;
    private static Logger logger = LoggerFactory.getLogger(TandemTraceChartAdapter.class);

    private MultiTraceChartAdapter analogChartAdapter;
    private MultiTraceChartAdapter digitalChartAdapter;
    private List<GraphicFeed> digital = new ArrayList<>();
    private List<GraphicFeed> analog = new ArrayList<>();
    private VerticalLayout content;

    public TandemTraceChartAdapter(GraphicWidget widget) {
        super(widget);
        Component visualization = buildVisualization();
        if (visualization instanceof VerticalLayout) {
            content = (VerticalLayout) visualization;
            content.setSizeFull();
        }
        setRootComposition(createContentWrapper(visualization));
    }

    private MultiTraceChartAdapter buildMultiTraceChart(List<GraphicFeed> feeds) {
        GraphicWidget widget = new GraphicWidget();
        widget.setGroupWidget(getGraphWidget().getGroupWidget());
        widget.setType(GraphicWidgetType.MULTI_TRACE);
        widget.getOptions().setExporting(false);
        widget.getOptions().setRealTime(getGraphWidget().getOptions().getRealTime());
        widget.setLabel(getGraphWidget().getLabel());
        widget.getOptions().setScale(ChartScaleType.LINEAR);
        widget.getOptions().setShowGrid(false);
        widget.getOptions().setShowLegend(true);
        widget.setFeeds(feeds);
        return new MultiTraceChartAdapter(widget);
    }

    @Override
    protected Component buildVisualization() {
        content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);

        buildFeeds();

        if (!analog.isEmpty()) {
            analogChartAdapter = buildMultiTraceChart(analog);
        } else {
            analogChartAdapter = buildMultiTraceChart(new ArrayList<>());
            analogChartAdapter.getChart().setVisible(false);
        }

        if (!digital.isEmpty()) {
            digitalChartAdapter = buildMultiTraceChart(digital);
        } else {
            digitalChartAdapter = buildMultiTraceChart(new ArrayList<>());
            digitalChartAdapter.getChart().setVisible(false);
        }

        layoutCharts();
        return content;
    }

    public void buildFeeds() {
        digital = new ArrayList<>();
        analog = new ArrayList<>();

        for (GraphicFeed feed : getGraphWidget().getFeeds()) {
            if (feed.getChannel() != null && feed.isChecked()) {
                if (ChannelUtils.isTypeDigital(feed.getChannel())) {
                    if (digital.isEmpty()) {
                        MeasureRange range = new MeasureRange();
                        range.setLower(0f);
                        range.setUpper(1f);
                        feed.getOptions().setExtremes(range);
                    }
                    digital.add(feed);
                } else {
                    analog.add(feed);
                }
            }
        }
    }

    public void layoutCharts() {
        float analogRatio = 0f;
        float digitalRatio = 0f;

        if (!analog.isEmpty() && !digital.isEmpty()) {
            analogRatio = 7f;
            digitalRatio = 3f;
        } else if (!analog.isEmpty() && digital.isEmpty()) {
            analogRatio = 1f;
        } else if (analog.isEmpty() && !digital.isEmpty()) {
            digitalRatio = 1f;
        }

        content.removeAll();
        Component analogChart = analogChartAdapter.getChart();
        Component digitalChart = digitalChartAdapter.getChart();
        content.add(analogChart, digitalChart);
        content.setFlexGrow(analogRatio, analogChart);
        content.setFlexGrow(digitalRatio, digitalChart);
    }

    @Override
    public boolean changedRealTime(GraphicWidgetOptions options) {
        boolean done = false;
        done = analogChartAdapter.changedRealTime(options);
        done = digitalChartAdapter.changedRealTime(options);
        return done;
    }

    @Override
    public void createChartConfiguration(TimeInterval interval) {
        analogChartAdapter.createChartConfiguration(interval);
        digitalChartAdapter.createChartConfiguration(interval);
    }

    @Override
    public void draw() {
        analogChartAdapter.draw();
        digitalChartAdapter.draw();
    }

    @Override
    public ChartJs getChart() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRealTime() {
        boolean done = false;
        done = analogChartAdapter.isRealTime();
        done = digitalChartAdapter.isRealTime();
        return done;
    }

    @Override
    public boolean refresh() {
        boolean done = false;
        done = analogChartAdapter.refresh();
        done = digitalChartAdapter.refresh();
        return done;
    }

    @Override
    // @Subscribe // avoid duplicate event handling
    public void refreshWithUiAccess(WidgetRefreshEvent event) {
        logger.debug("{} received WidgetRefreshEvent ", getWidget().getLabel());
        analogChartAdapter.refreshWithUiAccess(event);
        digitalChartAdapter.refreshWithUiAccess(event);
    }

    @Override
    protected void setChart(Component chart) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGraphWidgetOptionsOnChange(GraphicWidgetOptions options) {
        analogChartAdapter.setGraphWidgetOptionsOnChange(options);
        digitalChartAdapter.setGraphWidgetOptionsOnChange(options);
        // TODO(flow-chartjs): legacy Highcharts zoomType(X) behavior has no direct Chart.js API here.
    }

    @Override
    public void setTimeInterval(TimeInterval interval) {
        analogChartAdapter.setTimeInterval(interval);
        digitalChartAdapter.setTimeInterval(interval);
    }

    @Override
    public void setTimePeriod(TimePeriod period) {
        analogChartAdapter.setTimePeriod(period);
        digitalChartAdapter.setTimePeriod(period);
    }

    @Override
    public void register() {
        analogChartAdapter.register();
        digitalChartAdapter.register();
        super.register();
    }

    @Override
    public void unregister() {
        analogChartAdapter.unregister();
        digitalChartAdapter.unregister();
        super.unregister();
    }

    public void applyChecked(List<String> checked) {
        for (GraphicFeed feed : getGraphWidget().getFeeds()) {
            if (feed.getChannel() != null) {
                feed.setChecked(checked.contains(feed.getChannel().getKey()));
            }
        }

        buildFeeds();

        analogChartAdapter.getGraphWidget().setFeeds(analog);
        analogChartAdapter.setValidities(ChartUtils.getValidities(analogChartAdapter.getGraphWidget()));
        analogChartAdapter.getChart().setVisible(!analog.isEmpty());

        digitalChartAdapter.getGraphWidget().setFeeds(digital);
        digitalChartAdapter.setValidities(ChartUtils.getValidities(digitalChartAdapter.getGraphWidget()));
        digitalChartAdapter.getChart().setVisible(!digital.isEmpty());

        layoutCharts();

        createChartConfiguration(analogChartAdapter.getIntervalField().getValue());
        draw();

        UIUtils.getServiceFactory().getGroupWidgetService().applyRefresh(getGraphWidget(), checked);
    }
}
