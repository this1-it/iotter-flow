package it.thisone.iotter.ui.common.charts.bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChartConfig {

    private static final Logger logger = LoggerFactory.getLogger(ChartConfig.class);
    private static final ObjectMapper MAPPER;
    static {
        MAPPER = new ObjectMapper();
        MAPPER.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    private String type;
    private ChartData data;
    private ChartOptions options;

    public ChartConfig() {
    }

    public ChartConfig(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public ChartConfig setType(String type) {
        this.type = type;
        return this;
    }

    public ChartData getData() {
        if (data == null) {
            data = new ChartData();
        }
        return data;
    }

    public ChartOptions getOptions() {
        if (options == null) {
            options = new ChartOptions();
        }
        return options;
    }

    public String toJson() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (Exception e) {
            logger.error("Failed to serialize chart config", e);
            return "{}";
        }
    }
}
