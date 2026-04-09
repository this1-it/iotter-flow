package it.thisone.iotter.ui.common.charts.bridge;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonSubTypes({
    @JsonSubTypes.Type(TimeLineDataset.class),
    @JsonSubTypes.Type(BarDataset.class),
    @JsonSubTypes.Type(RadarDataset.class)
})
public abstract class ChartDataset {

    private String label;
    private String borderColor;
    private String backgroundColor;
    private Integer borderWidth;
    private Boolean fill;
    private Integer pointRadius;

    public String getLabel() {
        return label;
    }

    public ChartDataset setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public ChartDataset setBorderColor(String borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public ChartDataset setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public Integer getBorderWidth() {
        return borderWidth;
    }

    public ChartDataset setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        return this;
    }

    public Boolean getFill() {
        return fill;
    }

    public ChartDataset setFill(boolean fill) {
        this.fill = fill;
        return this;
    }

    public Integer getPointRadius() {
        return pointRadius;
    }

    public ChartDataset setPointRadius(int pointRadius) {
        this.pointRadius = pointRadius;
        return this;
    }
}
