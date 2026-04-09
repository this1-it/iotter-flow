package it.thisone.iotter.ui.common.charts.bridge;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScaleConfig {

    private String type;
    private GridConfig grid;
    private TicksConfig ticks;
    private TimeConfig time;
    private String min;
    private String max;

    public ScaleConfig() {
    }

    public ScaleConfig(String type) {
        this.type = type;
    }

    public static ScaleConfig time() {
        return new ScaleConfig("time");
    }

    public static ScaleConfig linear() {
        return new ScaleConfig("linear");
    }

    public static ScaleConfig logarithmic() {
        return new ScaleConfig("logarithmic");
    }

    public static ScaleConfig category() {
        return new ScaleConfig("category");
    }

    public String getType() {
        return type;
    }

    public ScaleConfig setType(String type) {
        this.type = type;
        return this;
    }

    public GridConfig getGrid() {
        if (grid == null) {
            grid = new GridConfig();
        }
        return grid;
    }

    public TicksConfig getTicks() {
        if (ticks == null) {
            ticks = new TicksConfig();
        }
        return ticks;
    }

    public TimeConfig getTime() {
        if (time == null) {
            time = new TimeConfig();
        }
        return time;
    }

    public String getMin() {
        return min;
    }

    public ScaleConfig setMin(String min) {
        this.min = min;
        return this;
    }

    public String getMax() {
        return max;
    }

    public ScaleConfig setMax(String max) {
        this.max = max;
        return this;
    }
}
