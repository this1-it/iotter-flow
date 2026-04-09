package it.thisone.iotter.ui.common.charts.bridge;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TitleConfig {

    private Boolean display;
    private String text;
    private String color;
    private FontConfig font;

    public Boolean getDisplay() {
        return display;
    }

    public TitleConfig setDisplay(boolean display) {
        this.display = display;
        return this;
    }

    public String getText() {
        return text;
    }

    public TitleConfig setText(String text) {
        this.text = text;
        return this;
    }

    public String getColor() {
        return color;
    }

    public TitleConfig setColor(String color) {
        this.color = color;
        return this;
    }

    public FontConfig getFont() {
        if (font == null) {
            font = new FontConfig();
        }
        return font;
    }
}
