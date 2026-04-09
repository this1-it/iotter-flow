package it.thisone.iotter.ui.common.charts.bridge;

import java.util.ArrayList;
import java.util.List;

public class BarDataset extends ChartDataset {

    private List<Double> data = new ArrayList<>();
    private String stack;

    public List<Double> getData() {
        return data;
    }

    public BarDataset dataAsList(List<Double> data) {
        this.data = data != null ? new ArrayList<>(data) : new ArrayList<>();
        return this;
    }

    public String getStack() {
        return stack;
    }

    public BarDataset setStack(String stack) {
        this.stack = stack;
        return this;
    }
}
