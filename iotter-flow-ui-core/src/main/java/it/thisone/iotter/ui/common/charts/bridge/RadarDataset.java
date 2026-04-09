package it.thisone.iotter.ui.common.charts.bridge;

import java.util.ArrayList;
import java.util.List;

public class RadarDataset extends ChartDataset {

    private List<Double> data = new ArrayList<>();

    public List<Double> getData() {
        return data;
    }

    public RadarDataset dataAsList(List<Double> data) {
        this.data = data != null ? new ArrayList<>(data) : new ArrayList<>();
        return this;
    }
}
