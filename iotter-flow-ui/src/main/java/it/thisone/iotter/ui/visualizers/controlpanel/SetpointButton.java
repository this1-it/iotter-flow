package it.thisone.iotter.ui.visualizers.controlpanel;

import java.text.MessageFormat;

import com.vaadin.flow.component.button.Button;

import it.thisone.iotter.persistence.model.GraphicFeed;

public class SetpointButton extends Button {
    public static final String FORMAT2 =
            "<span style=\"font-size:1.0em; line-height: 1; font-weight: bold; color: {0};\">{1}</span>" +
            "<span style=\"font-size:0.8em; color: {0}; \">{2}</span>";

    private static final long serialVersionUID = 1L;

    private String unit;
    private final String key;
    private String fillColor;
    private final String format;

    public SetpointButton(GraphicFeed feed) {
        super("");
        this.key = feed.getKey();
        this.fillColor = feed.getOptions().getFillColor();
        this.format = FORMAT2;
        this.unit = "";
        setValue("");
        addClassName("controlpanel-setpoint");
    }

    public void setMeasure(String value, String unit) {
        this.unit = unit;
        setValue(value);
    }

    public void setValue(String value) {
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
