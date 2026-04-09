package it.thisone.iotter.ui.common.charts.bridge;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChartData {

    private List<String> labels;
    private List<ChartDataset> datasets = new ArrayList<>();

    public List<String> getLabels() {
        return labels;
    }

    public ChartData setLabels(List<String> labels) {
        this.labels = labels;
        return this;
    }

    public List<ChartDataset> getDatasets() {
        return datasets;
    }

    public void addDataset(ChartDataset dataset) {
        datasets.add(dataset);
    }
}
