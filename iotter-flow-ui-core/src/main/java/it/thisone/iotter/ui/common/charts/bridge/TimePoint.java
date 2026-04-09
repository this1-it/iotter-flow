package it.thisone.iotter.ui.common.charts.bridge;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TimePoint {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final LocalDateTime dateTime;
    private final Double y;

    public TimePoint(LocalDateTime dateTime, Double y) {
        this.dateTime = dateTime;
        this.y = y;
    }

    public static TimePoint of(LocalDateTime dateTime, Double y) {
        return new TimePoint(dateTime, y);
    }

    public String getX() {
        return dateTime != null ? dateTime.format(ISO) : null;
    }

    public Double getY() {
        return y;
    }

    @JsonIgnore
    public LocalDateTime getDateTime() {
        return dateTime;
    }
}
