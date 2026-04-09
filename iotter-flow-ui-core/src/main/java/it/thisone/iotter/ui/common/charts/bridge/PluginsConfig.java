package it.thisone.iotter.ui.common.charts.bridge;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PluginsConfig {

    private LegendConfig legend;
    private TitleConfig title;

    public LegendConfig getLegend() {
        if (legend == null) {
            legend = new LegendConfig();
        }
        return legend;
    }

    public TitleConfig getTitle() {
        if (title == null) {
            title = new TitleConfig();
        }
        return title;
    }
}
