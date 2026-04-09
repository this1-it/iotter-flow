package it.thisone.iotter.ui.common.charts.bridge;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimeConfig {

    private String tooltipFormat;
    private String min;
    private String max;
    private String unit;

    public String getTooltipFormat() {
        return tooltipFormat;
    }

    public TimeConfig setTooltipFormat(String tooltipFormat) {
        this.tooltipFormat = tooltipFormat;
        return this;
    }

    public String getMin() {
        return min;
    }

    public TimeConfig setMin(String min) {
        this.min = min;
        return this;
    }

    public String getMax() {
        return max;
    }

    public TimeConfig setMax(String max) {
        this.max = max;
        return this;
    }

    public String getUnit() {
        return unit;
    }

    public TimeConfig setUnit(String unit) {
        this.unit = unit;
        return this;
    }
}
