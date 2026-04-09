package it.thisone.iotter.ui.common.charts.bridge;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FontConfig {

    private Integer size;

    public Integer getSize() {
        return size;
    }

    public FontConfig setSize(int size) {
        this.size = size;
        return this;
    }
}
