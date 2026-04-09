package it.thisone.iotter.ui.common.charts.bridge;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicksConfig {

    private Boolean beginAtZero;

    public Boolean getBeginAtZero() {
        return beginAtZero;
    }

    public TicksConfig setBeginAtZero(boolean beginAtZero) {
        this.beginAtZero = beginAtZero;
        return this;
    }
}
