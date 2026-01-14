package it.thisone.iotter.cassandra;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;

import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.cassandra.model.MeasureAggregation;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.cassandra.model.SummaryErrors;

public class InterpolationUtils {

	protected static Logger logger = LoggerFactory.getLogger(InterpolationUtils.class);

	public static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss ZZZ";

	
	// Added static final DateFormat using UTC timezone
	public static final DateFormat UTC_DATE_FORMAT;
	
	static {
		UTC_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		UTC_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public static String intervalString(Range<Date> interval) {
		return String.format("[%s‥%s]",UTC_DATE_FORMAT.format(interval.lowerEndpoint()),UTC_DATE_FORMAT.format(interval.upperEndpoint()));
	}
	
	public static Interpolation suggestedInterpolation(int seconds) {
		if (seconds <= Interpolation.H1.getSeconds()) {
			return Interpolation.MIN5;
		} else if (Interpolation.H1.getSeconds() < seconds && seconds <= Interpolation.D1.getSeconds()) {
			return Interpolation.MIN15;
		} else if (Interpolation.D1.getSeconds() < seconds && seconds <= Interpolation.W1.getSeconds() * 2) {
			return Interpolation.H1;
		} else if (Interpolation.W1.getSeconds() * 2 < seconds && seconds <= Interpolation.M1.getSeconds() * 3) {
			return Interpolation.D1;
		}
		return Interpolation.W1;
	}

	public static Interpolation suggestedInterpolation(Date from, Date to) {
		int seconds = (int) ((float) (to.getTime() - from.getTime()) / (float) 1000);
		return suggestedInterpolation(seconds);
	}

	public static Range<Date> currentPeriod(Date date, Interpolation interpolation) {
		Calendar calendar = getCalendarUTC();
		calendar.setTime(date);
		return currentPeriod(calendar, interpolation);
	}

	public static Range<Date> currentPeriod(Calendar calendar, Interpolation interpolation) {
		if (interpolation == null)
			throw new IllegalArgumentException("interpolation null not valid");
		Range<Date> interval = null;
		switch (interpolation) {
		case D1:
			interval = currentDay(calendar);
			break;
		case H1:
			interval = currentHour(calendar);
			break;
		case H6:
			interval = currentHoursStep(calendar, 6);
			break;
		case M1:
			interval = currentMonth(calendar);
			break;
		case MIN1:
			interval = currentMinutesStep(calendar, 1);
			break;
		case MIN15:
			interval = currentMinutesStep(calendar, 15);
			break;
		case MIN5:
			interval = currentMinutesStep(calendar, 5);
			break;
		case W1:
			interval = currentWeek(calendar);
			break;
		case RAW:
		default:
			throw new IllegalArgumentException("unsuitable interpolation " + Interpolation.RAW);
		}
		return interval;
	}

	/**
	 * using UTC calendar
	 * 
	 * @param date
	 * @return
	 */
	public static Range<Date> currentWeek(Date date) {
		Calendar calendar = getCalendarUTC();
		calendar.setTime(date);
		return currentWeek(calendar);
	}

	/**
	 * using UTC calendar
	 * 
	 * @param date
	 * @return
	 */
	public static Range<Date> current5Minutes(Date date) {
		Calendar calendar = getCalendarUTC();
		calendar.setTime(date);
		return currentMinutesStep(calendar, 5);
	}

	/**
	 * using UTC calendar
	 * 
	 * @param date
	 * @return
	 */
	public static Range<Date> current15Minutes(Date date) {
		Calendar calendar = getCalendarUTC();
		calendar.setTime(date);
		return currentMinutesStep(calendar, 15);
	}

	/**
	 * using UTC calendar
	 * 
	 * @param date
	 * @return
	 */
	public static Range<Date> currentMonth(Date date) {
		Calendar calendar = getCalendarUTC();
		calendar.setTime(date);
		return currentMonth(calendar);
	}

	/**
	 * using UTC calendar
	 * 
	 * @param date
	 * @return
	 */
	public static Range<Date> currentHour(Date date) {
		Calendar calendar = getCalendarUTC();
		calendar.setTime(date);
		return currentHour(calendar);
	}

	/**
	 * using UTC calendar
	 * 
	 * @param date
	 * @return
	 */
	public static Range<Date> currentDay(Date date) {
		Calendar calendar = getCalendarUTC();
		calendar.setTime(date);
		return currentDay(calendar);
	}

	/**
	 * using UTC calendar
	 * 
	 * @param date
	 * @return
	 */
	public static Range<Date> currentYear(Date date) {
		Calendar calendar = getCalendarUTC();
		calendar.setTime(date);
		return currentYear(calendar);
	}

	private static Range<Date> currentYear(Calendar calendar) {
		calendar.set(Calendar.DAY_OF_YEAR, 1); // first day of the year.
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar.getTime();
		// add step
		calendar.add(Calendar.YEAR, 1);
		Date endDate = calendar.getTime();
		return Range.closedOpen(startDate, endDate);
	}

	private static Range<Date> currentMonth(Calendar calendar) {
		// first day of the month.
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar.getTime();

		// add step
		calendar.add(Calendar.MONTH, 1);
		Date endDate = calendar.getTime();
		return Range.closedOpen(startDate, endDate);
	}

	private static Range<Date> currentWeek(Calendar calendar) {
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar.getTime();
		// add step
		calendar.add(Calendar.DATE, 7);
		Date endDate = calendar.getTime();
		return Range.closedOpen(startDate, endDate);
	}

	/*
	 * MUST be upper close interval !
	 */
	private static Range<Date> currentDay(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar.getTime();
		// add step
		calendar.add(Calendar.DATE, 1);
		Date endDate = calendar.getTime();
		return Range.closedOpen(startDate, endDate);
	}

	private static Range<Date> currentHour(Calendar calendar) {
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date lower = calendar.getTime();

		// add step
		calendar.add(Calendar.HOUR_OF_DAY, 1);
		Date upper = calendar.getTime();
		return Range.closedOpen(lower, upper);
	}

	private static Range<Date> currentHoursStep(Calendar calendar, int step) {
		int start = calendar.get(Calendar.HOUR_OF_DAY) / step;
		calendar.set(Calendar.HOUR, start * step);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date lower = calendar.getTime();
		// add step
		calendar.add(Calendar.HOUR_OF_DAY, step);
		Date upper = calendar.getTime();
		return Range.closedOpen(lower, upper);
	}

	/*
	 * MUST be upper open interval !
	 */
	private static Range<Date> currentMinutesStep(Calendar calendar, int step) {
		int start = calendar.get(Calendar.MINUTE) / step;
		calendar.set(Calendar.MINUTE, start * step);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date lower = calendar.getTime();
		// add step
		calendar.add(Calendar.MINUTE, step);
		Date upper = calendar.getTime();
		return Range.closedOpen(lower, upper);
	}

	public static Calendar getCalendarUTC() {
		return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * using UTC calendar
	 * 
	 * @param interpolation
	 * @param period
	 * @return
	 */
	public static List<Range<Date>> splitPeriod(Interpolation interpolation, Range<Date> period) {
		return splitPeriod(TimeZone.getTimeZone("UTC"), interpolation, period);
	}

	public static List<Range<Date>> splitPeriod(TimeZone zone, Interpolation interpolation, Range<Date> period) {
		List<Range<Date>> intervals = new ArrayList<Range<Date>>();
		
		Date from = new Date(period.lowerEndpoint().getTime());
		Calendar calendar = Calendar.getInstance(zone);
		calendar.setTime(period.upperEndpoint());
		
		Range<Date> interval = currentPeriod(calendar, interpolation);
		Date to = new Date(interval.upperEndpoint().getTime());

		while (to.after(from)) {
			calendar.setTime(from);
			interval = currentPeriod(calendar, interpolation);
			Date lower = interval.lowerEndpoint().before(period.lowerEndpoint()) ? period.lowerEndpoint() : interval.lowerEndpoint();
			Date upper = interval.upperEndpoint().after(period.upperEndpoint()) ? period.upperEndpoint() : interval.upperEndpoint();
			if (lower.before(upper)) {
				intervals.add(Range.closedOpen(lower, upper));
			}
			
			from = new Date(interval.upperEndpoint().getTime() + 1000);
		}

		return intervals;
	}

	public static Date toServerDate(Date date, TimeZone zone) {
		Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		instance.setTimeInMillis(date.getTime());
		instance.setTimeZone(zone);
		// fix one field to force calendar re-adjust the value
		instance.set(Calendar.MINUTE, instance.get(Calendar.MINUTE));
		return instance.getTime();
	}

	public static Date toUTCDate(Date date, TimeZone zone) {
		Calendar instance = Calendar.getInstance(zone);
		instance.setTimeInMillis(date.getTime());
		instance.setTimeZone(TimeZone.getTimeZone("UTC"));
		// fix one field to force calendar re-adjust the value
		instance.set(Calendar.MINUTE, instance.get(Calendar.MINUTE));
		return instance.getTime();
	}

	public static Range<Date> toServerRange(Range<Date> period, TimeZone zone) {
		return Range.openClosed(toServerDate(period.lowerEndpoint(), zone), toServerDate(period.upperEndpoint(), zone));
	}

	public static Range<Date> toUTCRange(Range<Date> period, TimeZone zone) {
		return Range.openClosed(toUTCDate(period.lowerEndpoint(), zone), toUTCDate(period.upperEndpoint(), zone));
	}

	public static long currentTimeMillis(String timeZoneId) {
		return currentTimeMillis(getTimeZone(timeZoneId));
	}

	public static TimeZone getTimeZone(String timeZoneId) {
		if (timeZoneId == null) {
			return null;
		}
		try {
			return TimeZone.getTimeZone(timeZoneId);
		} catch (Throwable e) {
		}
		return null;
	}

	public static int timeZoneOffset(TimeZone tz, long millis) {
		if (tz == null) {
			return 0;
		}
		return tz.getOffset(millis);
	}

	public static long currentTimeMillis(TimeZone tz) {
		long ts = System.currentTimeMillis();
		return ts + timeZoneOffset(tz, ts);
	}

	public static Date removeTimeZoneOffset(long ts, TimeZone tz) {
		ts = ts - timeZoneOffset(tz, ts);
		return new Date(ts);
	}

	/*
	 * @see MeasureUnit.convert
	 */
	@Deprecated
	public static Float calculateMeasure(Float measure, Float scale, Float offset) {
		if (measure == null) {
			return measure;
		}
		if (measure.isNaN()) {
			return measure;
		}

		boolean zeroOffset = Float.compare(offset, 0f) == 0;
		boolean oneScale = Float.compare(scale, 1f) == 0;

		if (zeroOffset && oneScale) {
			return measure;
		}

		BigDecimal value = new BigDecimal(measure);
		value.setScale(2, RoundingMode.HALF_UP);
		if (scale != null) {
			value = value.multiply(new BigDecimal(scale));
		}
		if (offset != null) {
			value = value.add(new BigDecimal(offset));
		}
		return value.floatValue();
	}

	@Deprecated
	public static BigDecimal calculateRaw(Float set, Float scale, Float offset, int precision) {
		BigDecimal value = new BigDecimal(set);
		value.setScale(precision, RoundingMode.HALF_UP);
		if (offset != null) {
			value = value.add(new BigDecimal(offset * -1));
		}
		if (scale != null) {
			value = value.divide(new BigDecimal(scale), precision, RoundingMode.HALF_UP);
		}
		return value;
	}

	public static List<MeasureRaw> deaggregate(MeasureAggregation measure) {
		MeasureRaw min = new MeasureRaw(measure.getMinDate(), measure.getMinValue(), null);
		min.setKey(measure.getKey());
		MeasureRaw max = new MeasureRaw(measure.getMaxDate(), measure.getMaxValue(), null);
		max.setKey(measure.getKey());
		MeasureRaw err = new MeasureRaw(measure.getErrorDate(), measure.getValue(), measure.getError());
		err.setKey(measure.getKey());
		return SummaryErrors.interval(min, max, err);

	}
	
	public static String elapsed(Range<Date> range) {
		return elapsed(range.upperEndpoint(), range.lowerEndpoint());
	}

	public static String elapsed(Date endDate, Date startDate) {
		return elapsed(endDate.getTime() - startDate.getTime());
	}
	
	public static String elapsed(long elapsed) {
		long secondsInMilli = 1000;
		long minutesInMilli = secondsInMilli * 60;
		long hoursInMilli = minutesInMilli * 60;
		long daysInMilli = hoursInMilli * 24;

		long elapsedDays = elapsed / daysInMilli;
		elapsed = elapsed % daysInMilli;

		long elapsedHours = elapsed / hoursInMilli;
		elapsed = elapsed % hoursInMilli;

		long elapsedMinutes = elapsed / minutesInMilli;
		elapsed = elapsed % minutesInMilli;

		long elapsedSeconds = elapsed / secondsInMilli;
		elapsed = elapsed % secondsInMilli;
		return String.format("%d %02d:%02d:%02d.%03d [d hh:mm:ss.SSS]", elapsedDays, elapsedHours, elapsedMinutes,
				elapsedSeconds, elapsed);
	}

}
