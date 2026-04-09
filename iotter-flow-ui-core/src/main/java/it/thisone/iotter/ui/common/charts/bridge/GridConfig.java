package it.thisone.iotter.ui.common.charts.bridge;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GridConfig {

    private Boolean display;
    private Integer lineWidth;

    public Boolean getDisplay() {
        return display;
    }

    public GridConfig setDisplay(boolean display) {
        this.display = display;
        return this;
    }

    public Integer getLineWidth() {
        return lineWidth;
    }

    public GridConfig setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
        return this;
    }
}
