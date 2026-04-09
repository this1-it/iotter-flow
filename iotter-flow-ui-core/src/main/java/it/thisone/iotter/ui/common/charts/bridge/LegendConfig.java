package it.thisone.iotter.ui.common.charts.bridge;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LegendConfig {

    private Boolean display;
    private String position;

    public Boolean getDisplay() {
        return display;
    }

    public LegendConfig setDisplay(boolean display) {
        this.display = display;
        return this;
    }

    public String getPosition() {
        return position;
    }

    public LegendConfig setPosition(String position) {
        this.position = position;
        return this;
    }
}
