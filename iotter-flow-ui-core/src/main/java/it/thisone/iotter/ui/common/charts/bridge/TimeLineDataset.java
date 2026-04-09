package it.thisone.iotter.ui.common.charts.bridge;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TimeLineDataset extends ChartDataset {

    private List<TimePoint> data = new ArrayList<>();

    public List<TimePoint> getData() {
        return data;
    }

    public TimeLineDataset setData(List<TimePoint> data) {
        this.data = data != null ? data : new ArrayList<>();
        return this;
    }

    public void addData(LocalDateTime x, Double y) {
        data.add(TimePoint.of(x, y));
    }

    public void addData(TimePoint point) {
        data.add(point);
    }

    public void dataAsList(List<TimePoint> points) {
        this.data = points != null ? new ArrayList<>(points) : new ArrayList<>();
    }
}
