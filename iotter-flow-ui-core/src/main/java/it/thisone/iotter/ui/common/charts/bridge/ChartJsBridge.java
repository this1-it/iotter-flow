package it.thisone.iotter.ui.common.charts.bridge;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.xdev.vaadin.chartjs.ChartContainer;

/**
 * Bridge component wrapping {@link ChartContainer} with convenience methods
 * that match the old {@code org.vaadin.addons.chartjs.ChartJs} API style.
 */
public class ChartJsBridge extends ChartContainer {

    private static final Logger logger = LoggerFactory.getLogger(ChartJsBridge.class);
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private ChartConfig config;
    private String canvasId;

    public ChartJsBridge() {
        super();
    }

    /**
     * Renders (or re-renders) the chart with the given configuration.
     * This destroys any existing chart and creates a new one.
     */
    public void configure(ChartConfig config) {
        this.config = config;
        this.canvasId = getChartJSDivId() + "-canvas";
        String json = config.toJson();
        logger.debug("Chart configure: {}", json.length() > 200 ? json.substring(0, 200) + "..." : json);
        showChart(json);
    }

    /**
     * Full re-render with the current configuration.
     */
    public void update() {
        if (config != null) {
            showChart(config.toJson());
        }
    }

    /**
     * Add a data point to a dataset via JS interop (no destroy/recreate).
     * Use this for real-time streaming data.
     */
    public void addDataPoint(int datasetIndex, LocalDateTime x, Double y) {
        if (canvasId == null) {
            return;
        }
        String xStr = x.format(ISO);
        getElement().executeJs(
                "window.chartjsBridge_addDataPoint($0, $1, $2, $3)",
                canvasId, datasetIndex, xStr, y);
    }

    /**
     * Update scale min/max bounds via JS interop (no destroy/recreate).
     */
    public void updateScaleBounds(String scaleId, LocalDateTime min, LocalDateTime max) {
        if (canvasId == null) {
            return;
        }
        String minStr = min != null ? min.format(ISO) : "";
        String maxStr = max != null ? max.format(ISO) : "";
        getElement().executeJs(
                "window.chartjsBridge_updateScaleBounds($0, $1, $2, $3)",
                canvasId, scaleId, minStr, maxStr);
    }

    /**
     * Trigger a chart update via JS interop (re-render with current data).
     */
    public void jsUpdate() {
        if (canvasId == null) {
            return;
        }
        getElement().executeJs(
                "window.chartjsBridge_update($0)", canvasId);
    }

    public ChartConfig getConfig() {
        return config;
    }
}
