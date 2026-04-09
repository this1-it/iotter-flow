package it.thisone.iotter.ui.common.charts.bridge;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChartOptions {

    private Boolean responsive;
    private Boolean maintainAspectRatio;
    private PluginsConfig plugins;
    private Map<String, ScaleConfig> scales;

    public Boolean getResponsive() {
        return responsive;
    }

    public ChartOptions setResponsive(boolean responsive) {
        this.responsive = responsive;
        return this;
    }

    public Boolean getMaintainAspectRatio() {
        return maintainAspectRatio;
    }

    public ChartOptions setMaintainAspectRatio(boolean maintainAspectRatio) {
        this.maintainAspectRatio = maintainAspectRatio;
        return this;
    }

    public PluginsConfig getPlugins() {
        if (plugins == null) {
            plugins = new PluginsConfig();
        }
        return plugins;
    }

    public Map<String, ScaleConfig> getScales() {
        if (scales == null) {
            scales = new LinkedHashMap<>();
        }
        return scales;
    }

    public void addScale(String id, ScaleConfig scale) {
        getScales().put(id, scale);
    }
}
