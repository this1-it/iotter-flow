package it.thisone.iotter.ui.charts;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.chartjs.ChartJs;
import org.vaadin.addons.chartjs.config.RadarChartConfig;
import org.vaadin.addons.chartjs.data.RadarDataset;

import com.google.common.collect.Range;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.shared.Registration;

import it.thisone.iotter.cassandra.InterpolationUtils;
import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GraphicWidgetOptions;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.common.fields.ChannelAcceptor;
import it.thisone.iotter.ui.model.TimeInterval;

public class WindRoseChartAdapter extends AbstractChartAdapter {

    private static final long serialVersionUID = -1958076735452737593L;
    private static final Logger logger = LoggerFactory.getLogger(WindRoseChartAdapter.class);

    private static final String[] ALL_DEGREE_CATEGORIES = {
            "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
            "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"
    };
    private static final Integer[] DEGREE_PETALS = { 4, 8, 16 };

    private ChartJs chart;
    private RadarChartConfig chartConfig;

    private List<String> measureCategories = new ArrayList<>();
    private List<String> measureColors = new ArrayList<>();
    private List<Range<Double>> measureRanges = new ArrayList<>();
    private List<Range<Double>> degreeRanges = new ArrayList<>();

    private int petals = ALL_DEGREE_CATEGORIES.length / 2;
    private List<String> degreeCategories = new ArrayList<>();

    private ComboBox<Integer> petalsChoice;
    private Registration applyPetalsChoiceRegistration;

    public WindRoseChartAdapter(GraphicWidget widget) {
        super(widget);
        optionsField.getRealTime().setVisible(false);
        optionsField.getScale().setVisible(false);
        optionsField.getAutoScale().setVisible(false);
        optionsField.getShowMarkers().setVisible(false);
        optionsField.getShowGrid().setVisible(false);
        petalsChoice = createDegreeChoice();
        getToolLayout().add(petalsChoice);
    }

    @Override
    protected Component buildVisualization() {
        ChartJs chart = new ChartJs();
        chart.setId(String.valueOf(getWidget().getId()));
        setChart(chart);
        createMeasureCategories();
        createDegreeCategories();
        chartConfig = new RadarChartConfig();
        chartConfig.options().responsive(true).maintainAspectRatio(false);
        chartConfig.options().legend().display(getGraphWidget().getOptions().getShowLegend());
        chart.configure(chartConfig);
        return chart;
    }

    @Override
    public void draw() {
        TimeInterval interval = intervalField.getValue();
        if (interval == null) {
            return;
        }
        createChartConfiguration(interval);
        getChart().update();
    }

    private void createMeasureCategories() {
        measureRanges = new ArrayList<>();
        measureCategories = new ArrayList<>();
        measureColors = new ArrayList<>();

        GraphicFeed speed = getSpeedFeed();
        GraphicFeed direction = getDirectionFeed();
        if (speed == null || direction == null) {
            return;
        }

        String feedUnit = ChartUtils.getUnitOfMeasure(speed);
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(UIUtils.getLocale());
        df.applyPattern(speed.getMeasure().getFormat());

        if (speed.getThresholds().isEmpty()) {
            measureCategories.add(String.format("%s [%s]", speed.getChannel().toString(), feedUnit));
            measureRanges.add(Range.all());
            measureColors.add(speed.getOptions().getFillColor() == null ? ChartUtils.quiteRandomHexColor() : speed.getOptions().getFillColor());
            return;
        }

        int size = speed.getThresholds().size();
        for (int i = 1; i < size; i++) {
            Number lower = ChartUtils.calculateThreshold(speed.getThresholds().get(i - 1).getValue(), speed);
            Number upper = ChartUtils.calculateThreshold(speed.getThresholds().get(i).getValue(), speed);
            measureCategories.add(String.format("%s - %s [%s]", df.format(lower), df.format(upper), feedUnit));
            measureRanges.add(Range.closedOpen((Double) lower, (Double) upper));
            measureColors.add(speed.getThresholds().get(i - 1).getFillColor());
        }

        Number upper = ChartUtils.calculateThreshold(speed.getThresholds().get(size - 1).getValue(), speed);
        measureCategories.add(String.format("> %s [%s]", df.format(upper), feedUnit));
        measureRanges.add(Range.atLeast((Double) upper));
        measureColors.add(speed.getThresholds().get(size - 1).getFillColor());
    }

    private void createDegreeCategories() {
        if (petals == 0) {
            petals = ALL_DEGREE_CATEGORIES.length;
        }

        degreeCategories = new ArrayList<>();
        int span = ALL_DEGREE_CATEGORIES.length / petals;
        for (int i = 0; i < ALL_DEGREE_CATEGORIES.length; i += span) {
            degreeCategories.add(ALL_DEGREE_CATEGORIES[i]);
        }

        degreeRanges = new ArrayList<>();
        double step = 360d / petals;
        double current = step / 2d;

        degreeRanges.add(Range.closedOpen(0d, current / 1000d));
        for (int i = 1; i < petals; i++) {
            double lower = current;
            double upper = current + step;
            degreeRanges.add(Range.closedOpen(lower / 1000d, upper / 1000d));
            current = upper;
        }
        degreeRanges.add(Range.closed(current / 1000d, (current + (step / 2d)) / 1000d));
    }

    @Override
    protected void createChartConfiguration(TimeInterval time) {
        if (getGraphWidget().getFeeds().isEmpty()) {
            return;
        }

        createMeasureCategories();
        createDegreeCategories();

        GraphicFeed speed = getSpeedFeed();
        GraphicFeed direction = getDirectionFeed();
        if (speed == null || direction == null || measureRanges.isEmpty() || degreeCategories.isEmpty()) {
            return;
        }

        int rows = measureRanges.size();
        int cols = degreeCategories.size();
        double[][] matrix = new double[rows][cols];

        List<MeasureRaw> measures = getData(speed, time);
        List<MeasureRaw> degrees = getData(direction, time);

        if (measures.size() != degrees.size()) {
            Map<Date, MeasureRaw> map = new HashMap<>();
            for (MeasureRaw degree : degrees) {
                if (!degree.hasError()) {
                    map.put(degree.getDate(), degree);
                }
            }
            List<MeasureRaw> filtered = new ArrayList<>();
            List<MeasureRaw> alignedDegrees = new ArrayList<>();
            for (MeasureRaw measure : measures) {
                if (measure.hasError()) {
                    continue;
                }
                MeasureRaw d = map.get(measure.getDate());
                if (d != null) {
                    filtered.add(measure);
                    alignedDegrees.add(d);
                }
            }
            measures = filtered;
            degrees = alignedDegrees;
        }

        MeasureUnit measureUnit = speed.getMeasure();
        MeasureUnit degreesMeasure = findMeasureUnit(speed.getOptions().getFeedReference(), speed);

        int total = 0;
        for (int i = 0; i < measures.size(); i++) {
            try {
                MeasureRaw measure = measures.get(i);
                MeasureRaw degree = degrees.get(i);
                if (measure.getValue() == null || degree.getValue() == null) {
                    continue;
                }

                Double value = (Double) ChartUtils.calculateMeasure(measure.getValue(), measureUnit);
                Double angle = (Double) ChartUtils.calculateAngle(degree.getValue(), degreesMeasure);
                int valuePos = findRange(value, measureRanges);
                int degreePos = findRange(angle, degreeRanges);
                if (degreePos > degreeCategories.size()) {
                    degreePos = 0;
                }
                if (valuePos != -1 && degreePos != -1 && degreePos < cols) {
                    matrix[valuePos][degreePos] = matrix[valuePos][degreePos] + 1d;
                    total++;
                }
            } catch (Exception e) {
                logger.debug("Wind rose point skipped: {}", e.getMessage());
            }
        }

        chartConfig = new RadarChartConfig();
        chartConfig.options().responsive(true).maintainAspectRatio(false);
        chartConfig.options().legend().display(getGraphWidget().getOptions().getShowLegend());
        chartConfig.data().labelsAsList(degreeCategories);

        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(UIUtils.getLocale());
        decimalFormat.applyPattern("0.##");

        for (int row = 0; row < rows; row++) {
            List<Double> values = new ArrayList<>();
            for (int col = 0; col < cols; col++) {
                double pct = total > 0 ? (matrix[row][col] * 100d) / total : 0d;
                try {
                    values.add(decimalFormat.parse(decimalFormat.format(pct)).doubleValue());
                } catch (ParseException e) {
                    values.add(pct);
                }
            }

            String color = measureColors.get(row) == null ? ChartUtils.quiteRandomHexColor() : measureColors.get(row);
            RadarDataset ds = new RadarDataset()
                    .label(measureCategories.get(row))
                    .dataAsList(values)
                    .borderColor(color)
                    .backgroundColor(color)
                    .fill(false)
                    .pointRadius(0);
            chartConfig.data().addDataset(ds);
        }

        // TODO(flow-chartjs): polar stacked wind-rose petals are approximated with radar datasets.
        getChart().configure(chartConfig);
    }

    @Override
    protected void setGraphWidgetOptionsOnChange(GraphicWidgetOptions options) {
        changedLocalControls(options);
    }

    @Override
    protected boolean isRealTime() {
        return false;
    }

    @Override
    public boolean refresh() {
        return false;
    }

    private int findRange(Double value, List<Range<Double>> ranges) {
        if (value == null) {
            return -1;
        }
        for (int i = 0; i < ranges.size(); i++) {
            if (ranges.get(i).contains(value)) {
                return i;
            }
        }
        return -1;
    }

    private ComboBox<Integer> createDegreeChoice() {
        ComboBox<Integer> combo = new ComboBox<>();
        combo.addClassName("small");
        combo.setWidth("8em");
        combo.setItems(java.util.Arrays.asList(DEGREE_PETALS));
        combo.setItemLabelGenerator(value -> value + " " + getI18nLabel("petals"));
        combo.setValue(petals);

        applyPetalsChoiceRegistration = combo.addValueChangeListener(event -> {
            Integer selectedValue = event.getValue();
            if (selectedValue != null) {
                petals = selectedValue;
                startDrawing();
                intervalField.updateValue();
            }
        });
        return combo;
    }

    private Interpolation suggestedInterpolation(TimeInterval interval) {
        int seconds = (int) ((float) (interval.getEndDate().getTime() - interval.getStartDate().getTime()) / 1000f);
        Interpolation interpolation = InterpolationUtils.suggestedInterpolation(seconds);
        if (seconds <= Interpolation.D1.getSeconds()) {
            interpolation = Interpolation.RAW;
        }
        Range<Date> range = Range.closedOpen(interval.getStartDate(), interval.getEndDate());
        return UIUtils.getCassandraService().getRollup().checkInterpolationAvailability(interpolation, range, null, null);
    }

    private List<MeasureRaw> getData(GraphicFeed feed, TimeInterval interval) {
        List<MeasureRaw> measures = new ArrayList<>();
        List<Range<Date>> ranges = getValidities().get(feed.getKey());
        Interpolation interpolation = suggestedInterpolation(interval);

        FeedKey feedKey = new FeedKey(feed.getDevice().getSerial(), feed.getKey());
        feedKey.setQualifier(feed.getChannel().getConfiguration().getQualifier());

        if (interpolation.equals(Interpolation.RAW)) {
            measures = ChartUtils.getData(feedKey, interval.getStartDate(), interval.getEndDate(), -1, 0, ranges, getNetworkTimeZone());
        } else {
            if (interpolation.equals(Interpolation.D1)) {
                interpolation = Interpolation.H1;
            }
            measures = ChartUtils.getAggregationData(feedKey, interval.getStartDate(), interval.getEndDate(), interpolation, ranges,
                    getNetworkTimeZone());
        }
        return measures;
    }

    private GraphicFeed getDirectionFeed() {
        for (GraphicFeed feed : getGraphWidget().getFeeds()) {
            if (ChannelAcceptor.isDirection(feed.getChannel())) {
                return feed;
            }
        }
        return null;
    }

    private GraphicFeed getSpeedFeed() {
        for (GraphicFeed feed : getGraphWidget().getFeeds()) {
            if (ChannelAcceptor.isSpeed(feed.getChannel())) {
                return feed;
            }
        }
        return null;
    }

    @Override
    protected boolean changedRealTime(GraphicWidgetOptions options) {
        return false;
    }

    @Override
    public ChartJs getChart() {
        return chart;
    }

    @Override
    protected void setChart(Component chart) {
        this.chart = (ChartJs) chart;
    }
}
