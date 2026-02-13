package it.thisone.iotter.ui.visualizers.controlpanel;

import java.text.MessageFormat;

import com.vaadin.flow.component.html.Span;

public class MeasureLabel extends Span {
    public static final String FORMAT1 =
            "<span style=\"font-size:14px; line-height: 1; color: {0}; font-weight: bold;\">{1}</span>" +
            "<span style=\"font-size:10px; color: {0};\">{2}</span>";
    public static final String FORMAT2 =
            "<span style=\"font-size:1.0em; line-height: 1; color: {0}; font-weight: bold;\">{1}</span>" +
            "<span style=\"font-size:0.8em; color: {0};\">{2}</span>";

    private static final long serialVersionUID = 1L;

    private String unit;
    private final String key;
    private String fillColor;
    private String format;

    public MeasureLabel(String key, String fillColor, String format) {
        super();
        this.key = key;
        this.fillColor = fillColor;
        this.format = format;
        this.unit = "";
        setValue("");
    }

    public void setMeasure(String value, String unit) {
        this.unit = unit;
        setValue(value);
    }

    public void setValue(String value) {
        if (format == null) {
            format = FORMAT1;
        }
        if (fillColor == null) {
            fillColor = "";
        }
        if (value == null) {
            value = "";
        }
        if (unit == null || "adim".equals(unit)) {
            unit = "";
        }
        getElement().setProperty("innerHTML", MessageFormat.format(format, fillColor, value, unit));
    }

    public String getKey() {
        return key;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
