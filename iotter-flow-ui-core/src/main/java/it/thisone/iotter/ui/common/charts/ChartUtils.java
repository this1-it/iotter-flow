package it.thisone.iotter.ui.common.charts;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;
//import com.vaadin.addon.charts.ChartOptions;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.Lang;
import com.vaadin.addon.charts.model.Marker;
import com.vaadin.addon.charts.model.YAxis;

import it.thisone.iotter.cassandra.model.CassandraExportFeed;
import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.IFeedKey;
import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.exceptions.MeasureException;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.MeasureRange;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.ui.common.MarkupsUtils;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.util.BacNet;

public class ChartUtils {
	public static final int REALTIME_SERIES = 30;
	public static final int FULL_SERIES = -1;
	public static final int CATEGORIZED_SERIES = 0;

	/*
	 * must be not zero !
	 */
	public static final double ZERO_DEGREES = 0.001;
	public static final Number THRESHOLD_LINE_WIDTH = 1.5f;
	public static final Number GRID_LINE_WIDTH = 1f;
	public static final Number PLOT_LINE_WIDTH = 1f;
	public static final Number MARKER_RADIUS = 2f;

	public static final String MONTH_DATEFORMAT = "%e. %b %y";
	public static final String YEAR_DATEFORMAT = "%b %y";
	// http://api.highcharts.com/highstock/xAxis.dateTimeLabelFormats
	public static final String X_DATEFORMAT = "%Y/%m/%d %H:%M:%S.%L";
	// Bug #208 (Resolved): [VAADIN] in export csv there are wrong timestamp
	// HH.mm,ss it should be HH:m.
	public static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss ZZZ";

	public static final String UTF_8 = "UTF-8";
	public static final String ARROW_MARKER_JS = "it.thisone.iotter/ui/js/arrow.js";
	public static final String ERROR_MARKER_URL = "./" + Constants.IMAGES_PATH + "/error-indicator.png";

	private static Logger logger = LoggerFactory.getLogger(ChartUtils.class);

	public static DateFormat getDateFormat() {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		return sdf;
	}

	public static String formatDate(long utc, TimeZone tz) {
		DateFormat df = getDateFormat();
		df.setTimeZone(tz);
		return df.format(new Date(utc));
	}

	public static String formatDate(Date date, TimeZone tz) {
		if (tz == null) tz = UIUtils.getBrowserTimeZone();
		DateFormat df = getDateFormat();
		df.setTimeZone(tz);
		return df.format(date);
	}

	public static void setErrorMarker(String key, DataSeriesItem item, String timestamp, String errors) {
		Marker marker = new Marker(true);
		// marker.setSymbol(new MarkerSymbolUrl(ERROR_MARKER_URL));
		//marker.setSymbol(MarkerSymbolEnum.TRIANGLE_DOWN);
		item.setMarker(marker);
		item.setName(timestamp + "<br/>" + MarkupsUtils.errorTooltip(errors, key));
	}

	/**
	 * change marker and item name showing cumulative errors
	 * 
	 * @param feedSeries
	 * @param errors
	 * @param timeZone
	 * @param timeZone
	 */
	public static void showErrors(String key, DataSeries feedSeries, Map<Number, String> errors,
			TimeZone networkTimeZone) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		sdf.setTimeZone(networkTimeZone);
		for (DataSeriesItem item : feedSeries.getData()) {
			if (errors.containsKey(item.getX())) {
				// need to restore original date
				Date date = toNetworkDate((Long) item.getX(), networkTimeZone);
				// date = new Date((Long) item.getX());
				String name = sdf.format(date);
				setErrorMarker(key, item, name, errors.get(item.getX()));
			}
		}

	}

	@Deprecated
	public static void setArrowMarker(DataSeriesItem item, Number angle) {
		Marker marker = new Marker(true);
		// marker.setStates(new MarkerStates(new State(false)));
		marker.setSymbol(CustomMarkerSymbolEnum.ARROW);
		marker.setRadius(angle);
		item.setMarker(marker);
	}

	/**
	 * Reduce data series with bit more sophisticated method.
	 * 
	 * @param series
	 *            with x y data pairs
	 * @param pixels
	 *            the pixel size for which the data should be adjusted
	 */
	public static void ramerDouglasPeuckerReduce(DataSeries series, int pixels) {
		// Calculate rough estimate for visual ratio on x-y, might be bad guess
		// if axis have been set manually
		if (series.getData().isEmpty())
			return;

		DataSeriesItem dataSeriesItem = series.get(0);
		double xMax = dataSeriesItem.getX().doubleValue();
		double xMin = xMax;
		double yMax = dataSeriesItem.getY().doubleValue();
		double yMin = yMax;
		for (int i = 1; i < series.size(); i++) {
			DataSeriesItem item = series.get(i);
			double x = item.getX().doubleValue();
			if (xMax < x) {
				xMax = x;
			}
			if (xMin > x) {
				xMin = x;
			}
			double y = item.getY().doubleValue();
			if (yMax < y) {
				yMax = y;
			}
			if (yMin > y) {
				yMin = y;
			}
		}
		double xyRatio = (xMax - xMin) / (yMax - yMin);

		// rough estimate for sane epsilon (1px)
		double epsilon = (xMax - xMin) / pixels;

		List<DataSeriesItem> rawData = series.getData();
		List<DataSeriesItem> reduced = ramerDouglasPeucker(rawData, epsilon, xyRatio);
		series.setData(reduced);

	}

	/**
	 * 
	 * @see http://en.wikipedia.org/wiki/Ramer–Douglas–Peucker_algorithm
	 * 
	 * @param points
	 * @param epsilon
	 * @param xyRatio
	 *            y values are multiplied with this to make distance calculation
	 *            in algorithm sane
	 * @return
	 */
	public static List<DataSeriesItem> ramerDouglasPeucker(List<DataSeriesItem> points, double epsilon,
			final double xyRatio) {
		// Find the point with the maximum distance
		double dmax = 0;
		int index = 0;
		DataSeriesItem start = points.get(0);
		DataSeriesItem end = points.get(points.size() - 1);
		for (int i = 1; i < points.size() - 1; i++) {
			DataSeriesItem point = points.get(i);
			double d = pointToLineDistance(start, end, point, xyRatio);
			if (d > dmax) {
				index = i;
				dmax = d;
			}
		}
		ArrayList<DataSeriesItem> reduced = new ArrayList<DataSeriesItem>();
		if (dmax >= epsilon) {
			// max distance is greater than epsilon, keep the most relevant
			// point and recursively simplify
			List<DataSeriesItem> startToRelevant = ramerDouglasPeucker(points.subList(0, index + 1), epsilon, xyRatio);
			reduced.addAll(startToRelevant);
			List<DataSeriesItem> relevantToEnd = ramerDouglasPeucker(points.subList(index, points.size() - 1), epsilon,
					xyRatio);
			reduced.addAll(relevantToEnd.subList(1, relevantToEnd.size()));
		} else {
			// no relevant points, drop all but ends
			reduced.add(start);
			reduced.add(end);
		}

		return reduced;
	}

	public static double pointToLineDistance(DataSeriesItem A, DataSeriesItem B, DataSeriesItem P,
			final double xyRatio) {
		double bY = B.getY().doubleValue() * xyRatio;
		double aY = A.getY().doubleValue() * xyRatio;
		double pY = P.getY().doubleValue() * xyRatio;
		double normalLength = Math.hypot(B.getX().doubleValue() - A.getX().doubleValue(), bY - aY);
		return Math.abs((P.getX().doubleValue() - A.getX().doubleValue()) * (bY - aY)
				- (pY - aY) * (B.getX().doubleValue() - A.getX().doubleValue())) / normalLength;
	}

	/*
	 * Bug #336 [HighCharts] axis extremes and thresholds should not be
	 * calculated
	 */
	public static Number calculateThreshold(Float threshold, GraphicFeed feed) {
		MeasureUnit thresholdUnit = feed.getMeasure();
		if (thresholdUnit == null) {
			return threshold.doubleValue();
		}
		BigDecimal value = new BigDecimal(threshold);
		value.setScale(thresholdUnit.getDecimals(), RoundingMode.HALF_UP);
		return value.doubleValue();
	}

	/**
	 * return a double
	 * 
	 * @param value
	 * @param measureUnit
	 * @return
	 * @throws MeasureException
	 */
	public static Number calculateMeasure(Float measure, MeasureUnit measureUnit) throws MeasureException {
		if (measure == null) {
			return null;
		}
		if (Float.isNaN(measure)) {
			return null;
		}
		if (Float.isInfinite(measure)) {
			return null;
		}
		if (measureUnit == null) {
			return measure.doubleValue();
		}

		return measureUnit.convert(measure).doubleValue();
	}

	public static String formatMeasure(Number value, MeasureUnit measureUnit) {
		DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(UIUtils.getLocale());
		if (measureUnit == null) {
			return decimalFormat.format(value);
		}
		if (measureUnit.getFormat() == null || measureUnit.getFormat().isEmpty()) {
			return decimalFormat.format(value);
		}
		/*
		 * Basically, a 0 means "always show the digit in this position", where
		 * a # means "show the digit in this position unless it's zero".
		 */
		String format = measureUnit.normalizedFormat();
		decimalFormat.applyPattern(format);
		decimalFormat.setDecimalSeparatorAlwaysShown(measureUnit.getDecimals() > 0);
		return decimalFormat.format(value);
	}

	/**
	 * return value
	 * 
	 * @param value
	 *            must be between 0 and 360
	 * @param markersMeausure
	 * @return 0.001 to 0.360
	 * @throws MeasureException
	 */
	public static Number calculateAngle(Float value, MeasureUnit measureUnit) throws MeasureException {
		Double angle = null;
		try {
			if (value == 0)
				value = 1f;
			if (value < 1f)
				value = (360f * value + 1);
			value = value / 1000f;
			measureUnit.setFormat("0.###");
			angle = (Double) calculateMeasure(value, measureUnit);
			if (angle > 0.360d || angle < 0.001d) {
				angle = null;
			}

		} catch (Exception e) {
			throw new MeasureException("Angle", e);
		}
		return angle;
	}

	/**
	 * An example how dataset can be reduced simply on the server side.
	 * 
	 * Using more datapoints than there are pixels on the devices makes usually
	 * no sense. Excess data just consumes bandwidth and client side CPU. This
	 * method reduces a large dataset with a very simple method.
	 * 
	 * @param series
	 *            the series to be reduced
	 * @param pixels
	 *            the pixel size for which the data should be adjusted
	 */
	public static void simpleReduce(DataSeries series, Map<Number, List<String>> errors, int pixels) {
		if (series.getData().isEmpty())
			return;
		DataSeriesItem first = series.get(0);
		DataSeriesItem last = series.get(series.size() - 1);
		ArrayList<DataSeriesItem> reducedDataSet = new ArrayList<DataSeriesItem>();
		if (first.getX() != null) {
			// x y pairs
			double startX = first.getX().doubleValue();
			double endX = last.getX().doubleValue();
			double minDistance = (endX - startX) / pixels;
			reducedDataSet.add(first);
			double lastPoint = first.getX().doubleValue();
			for (int i = 0; i < series.size(); i++) {
				DataSeriesItem item = series.get(i);
				if (item.getX().doubleValue() - lastPoint > minDistance) {
					reducedDataSet.add(item);
					lastPoint = item.getX().doubleValue();
				} else {
					errors.remove(item.getX());
				}
			}
			series.setData(reducedDataSet);
		} else {
			// interval data
			int k = series.size() / pixels;
			if (k > 1) {
				for (int i = 0; i < series.size(); i++) {
					if (i % k == 0) {
						DataSeriesItem item = series.get(i);
						reducedDataSet.add(item);
					}
				}
				series.setData(reducedDataSet);
			}
		}
	}

	/**
	 * http://www.highcharts.com/docs/chart-concepts/labels-and-string-
	 * formatting
	 * 
	 * @param unit
	 * @return
	 */
	public static String labelFormatter(String unit) {
		return String.format("this.value +' %s'", unit);
		// .0f no decimal places
		// return String.format("this.value:.5f");
	}

	public static String getUnitOfMeasure(GraphicFeed feed) {
		String feedUnit = UIUtils.getServiceFactory().getDeviceService()
				.getUnitOfMeasureName(feed.getMeasure().getType());
		return feedUnit;
	}

	public static void loadCustomMarkers() {
//		try {
//			String js = IOUtils.toString(ChartUtils.class.getResourceAsStream(ARROW_MARKER_JS), "UTF-8");
//			Page.getCurrent().getJavaScript().execute(js);
//		} catch (Exception e) {
//			logger.error(ARROW_MARKER_JS, e);
//		}
	}

	public static void setAxisExtremes(GraphicFeed feed, YAxis axis) {
		MeasureRange extremes = feed.getOptions().getExtremes();
		if (extremes != null) {
			
			if (ChannelUtils.isTypeDigital(feed.getChannel())) {
				Number[] tickPositions = new Number[2];
				tickPositions[0] = extremes.getLower();
				tickPositions[1] = extremes.getUpper();
				axis.setTickPositions(tickPositions);
				axis.setExtremes(extremes.getLower(), extremes.getUpper());
				axis.setMin(tickPositions[0]);
				axis.setMax(tickPositions[1]);
				axis.setStartOnTick(true);
				axis.setEndOnTick(true);
				return;
			}
			
			
			
			/*
			 * Number min = ChartUtils.calculateThreshold(extremes.getLower(),
			 * feed); Number max =
			 * ChartUtils.calculateThreshold(extremes.getUpper(), feed); float
			 * axis_min = extremes.getLower(); float axis_max =
			 * extremes.getUpper(); float tickInterval = Math.round((axis_max -
			 * axis_min) / 10f); while (axis_min % tickInterval != 0) {
			 * axis_min--; } while (axis_max % tickInterval != 0) { axis_max++;
			 * }
			 * 
			 * axis.setTickInterval(tickInterval);
			 */

			float axis_min = extremes.getLower();
			float axis_max = extremes.getUpper();
			float tickInterval = (axis_max - axis_min) / 5f;

			// Bug #122: [VAADIN] [HIGHCHART] extremes on Y axis are not
			// properly displayed
			Number[] tickPositions = new Number[6];

			tickPositions[0] = ChartUtils.calculateThreshold(extremes.getLower(), feed);
			tickPositions[tickPositions.length - 1] = ChartUtils.calculateThreshold(extremes.getUpper(), feed);

			float tickPosition = axis_min;
			for (int i = 1; i < tickPositions.length - 1; i++) {
				tickPosition = tickPosition + tickInterval;
				tickPositions[i] = ChartUtils.calculateThreshold(tickPosition, feed);
			}

			axis.setTickPositions(tickPositions);

			axis.setMin(tickPositions[0]);
			axis.setMax(tickPositions[tickPositions.length - 1]);
			axis.setStartOnTick(true);
			axis.setEndOnTick(true);

			// axis.setStartOnTick(false);
			// axis.setEndOnTick(false);
			// axis.setExtremes(min, max);
			// axis.setMin(min);
			// axis.setMax(max);

		}
		
	}


	/**
	 * #129: [PERSISTENCE][VAADIN][HIGHCHART] charts visualization shows
	 * deactivated channels as active Feature #195 Create device param with
	 * different id and same unique key
	 * 
	 * @param feed
	 * @param tz
	 * @return
	 */
	public static List<Range<Date>> getValidities(Channel channel) {
		if (channel == null) {
			return new ArrayList<Range<Date>>();
		}
		return channel.getValidityRanges();
	}

	/**
	 * Bug #129: [PERSISTENCE][VAADIN][HIGHCHART] charts visualization shows
	 * deactivated channels as active
	 * 
	 * @param widget
	 * @param tz
	 * @return
	 */
	public static Map<String, List<Range<Date>>> getValidities(GraphicWidget widget) {
		Map<String, List<Range<Date>>> validities = new HashMap<String, List<Range<Date>>>();
		for (GraphicFeed feed : widget.getFeeds()) {
			validities.put(feed.getKey(), ChartUtils.getValidities(feed.getChannel()));
		}
		return validities;
	}

	/**
	 * Bug #129: [PERSISTENCE][VAADIN][HIGHCHART] charts visualization shows
	 * deactivated channels as active
	 * 
	 * @param value
	 * @param ranges
	 * @return
	 */
	public static boolean inRange(Date value, List<Range<Date>> ranges) {
		if (ranges == null) {
			throw new IllegalArgumentException("missing validity date range for measure");
		}
		if (ranges.isEmpty()) {
			return true;
		}
		for (Range<Date> range : ranges) {
			if (range.contains(value)) {
				return true;
			}
		}
		return false;

	}


	public static MeasureRaw lastMeasure(IFeedKey feedKey) {
		if (feedKey == null) return null;
		
		
		MeasureRaw measure = UIUtils.getCassandraService().getFeeds().getLastMeasure(feedKey);
//		Feed feed = UIUtils.getCassandraService().getFeeds().getFeed(feedKey.getSerial(), feedKey.getKey());
//		if (feed != null && feed.getValue() != null) {
//			measure = new MeasureRaw(feed.getDate(), feed.getValue(), null);
//		}
		return measure;
	}

	public static Date lastTick(String key) {
		if (key == null) return null;
		return UIUtils.getCassandraService().getMeasures().getLastTick(key, new Date());
	}




	
	/**
	 * Bug #129: [PERSISTENCE][VAADIN][HIGHCHART] charts visualization shows
	 * deactivated channels as active
	 * 
	 * @param ranges
	 * @return
	 */
	public static Date latestDate(List<Range<Date>> ranges) {
		if (ranges == null) {
			throw new IllegalArgumentException("missing validity date range for last measure");
		}
		if (ranges.isEmpty()) {
			return new Date();
		}
		Date last = new Date(0);
		for (Range<Date> range : ranges) {
			if (range.hasUpperBound() && range.upperEndpoint().after(last)) {
				last = range.upperEndpoint();
			} else {
				if (!range.hasUpperBound()) {
					last = new Date();
					break;
				}
			}
		}
		return last;
	}

	
	/**
	 * 
	 * @param feedKey
	 * @param from
	 * @param to
	 * @param points
	 * @param ranges
	 * @return
	 */
	public static List<MeasureRaw> getData(FeedKey feed, Date from, Date to, int displayPoints, int step,
			List<Range<Date>> ranges, TimeZone tz) {
		List<MeasureRaw> result = new ArrayList<MeasureRaw>();
		List<MeasureRaw> measures = new ArrayList<MeasureRaw>();
		
		Date now = new Date();
		if (to.after(now)) {
			to = now;
		}
		
		if (to.before(from)) {
			return result;
		}

		String sn = feed.getSerial();
		String feedKey = feed.getKey();
		if (feed.getSerial().startsWith("_")) {
			return UIUtils.getCassandraService().getMeasures().getMockData(from, to, displayPoints);
		}

		if (ranges == null) {
			logger.error("missing validity date range for measure ", feed.getKey());
			return result;
		}
		
		
		Range<Date> interval = toUTCRange(from, to, tz);

		if (displayPoints == ChartUtils.FULL_SERIES) {
			return UIUtils.getCassandraService().getMeasures().getData(feed, interval, ChartUtils.FULL_SERIES, 0);
		}
		
		Interpolation interpolation = UIUtils.getCassandraService().getRollup().interpolationChoice(sn, feedKey, interval, displayPoints);
		
		switch (interpolation) {
		case RAW:
			measures = UIUtils.getCassandraService().getMeasures().getData(feed, interval, displayPoints, step);
			break;
		case MIN1:
			measures = UIUtils.getCassandraService().getMeasures().getData(feed, interval, displayPoints, 60*1000);
			break;
		case MIN5:
			measures = UIUtils.getCassandraService().getMeasures().getData(feed, interval, displayPoints, 5* 60*1000);
			break;
		default:
			measures = UIUtils.getCassandraService().getRollup().getData(feed, interval, interpolation);
			break;
		}

		/*
		 * Bug #129: [PERSISTENCE][VAADIN][HIGHCHART] charts visualization shows
		 * deactivated channels as active Feature #195 Create device param with
		 * different id and same unique key
		 */

		for (MeasureRaw measure : measures) {
			if (measure.getValue() == null || measure.getValue().equals(Float.NaN)) {
				continue;
			}
			if (ChartUtils.inRange(measure.getDate(), ranges)) {
				result.add(measure);
			}
		}

		// Collections.sort(result, new MeasureDateComparator());
		return result;
	}

	public static List<MeasureRaw> getAggregationData(FeedKey feedKey, Date from, Date to, Interpolation interpolation,
			List<Range<Date>> ranges, TimeZone zone) {

		List<MeasureRaw> measures = new ArrayList<MeasureRaw>();
		List<MeasureRaw> result = new ArrayList<MeasureRaw>();

		if (ranges == null) {
			logger.error("missing validity date range for measure ", feedKey.getKey());
			return result;
		}
		
		if (feedKey.getSerial().startsWith("_")) {
			return UIUtils.getCassandraService().getMeasures().getMockData(from, to, 1600);
		}

		
		Date now = new Date();
		if (to.after(now)) {
			to = now;
		}
		
		if (to.before(from)) {
			return measures;
		}

		Range<Date> interval = toUTCRange(from, to, zone);
		measures = UIUtils.getCassandraService().getRollup().getAggregationData(feedKey, interval, interpolation, zone);

		for (MeasureRaw measure : measures) {
			if (measure.getValue() == null || measure.getValue().equals(Float.NaN)) {
				continue;
			}
			if (ChartUtils.inRange(measure.getDate(), ranges)) {
				result.add(measure);
			}
		}
		return result;
	}

	/**
	 * Returns an epoch timestamp adjusted by timezone offset. All Date objects
	 * passed to Highcharts should be routed via this method as we want to
	 * maintain the Timezone used on the server (HC uses UTC time stamps
	 * internally)
	 * 
	 * @see com.vaadin.addon.charts.util.Util.toHighchartsTS
	 * @param date
	 * @return Bug #150 [VAADIN] [HIGHCHART] cannot display data serie with a
	 *         timezone different from default
	 */
	public static long toHighchartsTS(Date date, TimeZone tz) {
		return date.getTime() + tz.getOffset(date.getTime());
	}

	/**
	 * Converts UTC based raw date value from the client side rendering library
	 * to a Date value in JVM's default time zone.
	 * 
	 * @see com.vaadin.addon.charts.util.Util.toServerDate
	 * @param rawClientSideValue
	 *            the raw value from the client side
	 * @return a Date value in Network default time zone
	 */
	public static Date toNetworkDate(double rawClientSideValue, TimeZone networkTimeZone) {
		Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		instance.setTimeInMillis((long) rawClientSideValue);
		// fix one field to force calendar re-adjust the value
		instance.set(Calendar.MINUTE, instance.get(Calendar.MINUTE));
		instance.setTimeZone(networkTimeZone);
		return instance.getTime();
	}

	public static Date toUTCDate(double millis, TimeZone networkTimeZone) {
		// DateFormat df = getDateFormat();
		// df.setTimeZone(networkTimeZone);
		Calendar instance = Calendar.getInstance(networkTimeZone);
		instance.setTimeInMillis((long) millis);

		// String local = df.format(instance.getTime());
		instance.setTimeZone(TimeZone.getTimeZone("UTC"));
		// fix one field to force calendar re-adjust the value
		instance.set(Calendar.MINUTE, instance.get(Calendar.MINUTE));
		// df.setTimeZone(TimeZone.getTimeZone("UTC"));
		// String gmt = df.format(instance.getTime());
		// logger.info("Local {} GMT {}", local, gmt);
		return instance.getTime();
	}
	
	public static Range<Date> toUTCRange(Date from, Date to, TimeZone tz) {

		Date lower = toUTCDate(from.getTime(), tz);
		Date upper = toUTCDate(to.getTime(), tz);
//		
//		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
//		calendar.setTime(lower);
//		calendar.set(Calendar.SECOND, 0);
//		calendar.set(Calendar.MILLISECOND, 0);
//		lower = calendar.getTime();
//		
//		calendar.setTime(upper);
//		calendar.set(Calendar.SECOND, 59);
//		calendar.set(Calendar.MILLISECOND, 0);
//		upper = calendar.getTime();
		

		return Range.closedOpen(lower, upper);
	}



	public static String getFeedLabel(GraphicFeed feed) {
		String label = null;
		if (feed.getChannel() != null) {
			label = ChannelUtils.displayName(feed.getChannel());
			if (!feed.getChannel().getDefaultMeasure().getType().equals(BacNet.ADIM)) {
				String feedUnit = ChartUtils.getUnitOfMeasure(feed);
				label = String.format("%s [%s]", label, feedUnit);
			}
		} else if (feed.getMetaData() != null) {
			label = UIUtils.messageBundle(feed.getMetaData());
		}
		if (label == null) {
			label = feed.getLabel();
		}
		
		return label;
	}

	public static CassandraExportFeed createExportFeed(GraphicFeed feed) {
		if (feed.getChannel() == null) return null;
		return createExportFeed(feed.getChannel());
	}

	public static CassandraExportFeed createExportFeed(Channel channel) {
		if (channel == null) return null;
		return UIUtils.getServiceFactory().getExportService().createExportFeed(channel, ChannelUtils.displayName(channel));
	}

	
	public static List<CassandraExportFeed> createExportFeeds(List<GraphicFeed> feeds) {
		List<CassandraExportFeed> items = new ArrayList<CassandraExportFeed>();
		for (GraphicFeed feed : feeds) {
			CassandraExportFeed item = ChartUtils.createExportFeed(feed);
			if (item != null) items.add(item);
		}
		return items;
	}

//	public static com.vaadin.addon.charts.model.style.Theme getTheme() {
//		return ChartOptions.get().getTheme();
//	}

	public static Lang getLang() {
		ChartLang language = new ChartLang();
		DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance(UIUtils.getLocale());
		DateFormatSymbols dateSymbols = DateFormatSymbols.getInstance(UIUtils.getLocale());
//		language.setMonths(dateSymbols.getMonths());
//		language.setShortMonths(dateSymbols.getShortMonths());
//		language.setWeekdays(dateSymbols.getWeekdays());
//		language.setDecimalPoint(String.valueOf(decimalSymbols.getDecimalSeparator()));
//		language.setThousandsSep(String.valueOf(decimalSymbols.getGroupingSeparator()));
//		language.setLoading(getTranslation("highcharts.loading", null, "Loading ..."));
//		language.setContextButtonTitle(getTranslation("highcharts.contextButtonTitle", null, "Chart context menu"));
//		language.setPrintChart(getTranslation("highcharts.printChart", null, "Print Chart"));
//		language.setNoData(getTranslation("highcharts.noData", null, "No Data to display"));
//		language.setNoData("");
//		language.setLoading(getTranslation("highcharts.loading", null, "Loading ..."));
		return language;
	}

	public static Number getDecimals(String format) {
		Integer decimals = null;
		String[] ff = format.split("\\.");
		if (ff != null && ff.length == 2) {
			decimals = ff[1].length();
		}
		return decimals;
	}

	// http://www.color-hex.com/216-web-safe-colors/
	private static String[] values = new String[] { "0000FF", // blue
			"8B008B", // magenta 4
			"FF0000", // red
			"00FF00", // green
			"00FFFF", "FF00FF", "FF9900", "32CD32", // lime green
			"3399CC", "9370DB", "FF6600", "006400", // dark green
			"3366CC", "CC33FF", "F08080", // light coral
			"339900", "FFFF00", // yellow
			"CC00FF" };

	public static String quiteRandomHexColor() {
		return "#" + values[(int) (Math.random() * values.length)];
//		Color[] colors = ChartUtils.getTheme().getColors();
//		return ((SolidColor) colors[(int) (Math.random() * colors.length)]).toString();
	}

	/**
	 * http://matplotlib.org/examples/color/colormaps_reference.html
	 * @param pos
	 * @return
	 */
	public static String hexColor(int pos) {
		 int i = pos % values.length;
		 return "#" + values[i];
//		Color[] colors = ChartUtils.getTheme().getColors();
//		int i = pos % colors.length;
//		return ((SolidColor) colors[i]).toString();
	}


}
