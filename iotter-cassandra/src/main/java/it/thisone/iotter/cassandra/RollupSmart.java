package it.thisone.iotter.cassandra;

import static it.thisone.iotter.cassandra.InterpolationUtils.current15Minutes;
import static it.thisone.iotter.cassandra.InterpolationUtils.currentDay;
import static it.thisone.iotter.cassandra.InterpolationUtils.currentHour;
import static it.thisone.iotter.cassandra.InterpolationUtils.currentMonth;
import static it.thisone.iotter.cassandra.InterpolationUtils.currentWeek;
import static it.thisone.iotter.cassandra.InterpolationUtils.getCalendarUTC;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Range;

import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.cassandra.model.MeasureAggregation;
import it.thisone.iotter.cassandra.model.MeasureStats;

@Service
public class RollupSmart extends RollupQueryBuilder {

	@Autowired
	private RollupQueries rollupQueries;

	@Autowired
	private SimpleDateFormat sdf;
	
	//private static Logger logger = LoggerFactory.getLogger(Constants.RollUp.ROLL_UP_LOG4J_CATEGORY);
	private static Logger logger = LoggerFactory.getLogger(RollupBounded.class);

	protected Range<Date> rollUp(MeasureStats feedStats, Range<Date> interval, boolean enableBreak) {
		String sn = feedStats.getSerial();
		String key = feedStats.getKey();
		int qualifier = feedStats.getQualifier();

		FeedKey feedKey = new FeedKey(sn, key);
		feedKey.setQualifier(qualifier);

		Float frequency = feedStats.getFrequency();
		Long records = feedStats.getRecords();
		MeasureAggregation aggregation = rollUp15Minutes(feedKey, interval, enableBreak);

		Range<Date> range = aggregation.getInterval();

		// frequency on raw data
		frequency = aggregation.getFrequency();

		aggregation = rollUp1Hour(key, interval, qualifier, enableBreak);
		if (frequency == null)
			frequency = aggregation.getFrequency();

		aggregation = rollUp1Day(key, interval, qualifier, enableBreak);
		if (frequency == null)
			frequency = aggregation.getFrequency();

		aggregation = rollUp1Week(key, interval, qualifier, enableBreak);
		if (frequency == null)
			frequency = aggregation.getFrequency();

		aggregation = rollUp1Month(key, interval, qualifier, enableBreak);
		if (frequency == null)
			frequency = aggregation.getFrequency();

		/*
		 * on change of monthly aggregation, update record count
		 */
//		if (aggregation.isChanged() || !enableBreak) {
//			records = countRecords(key, feedStats.getSince(), end);
//		}
		records = rollupQueries.countRecords(key, feedStats.getSince(), new Date());


		if (frequency == null || frequency == 0f) {
			frequency = 15 * 60f;
		}

		feedStats.setRecords(records);
		feedStats.setFrequency(frequency);

		return range;
	}


	private MeasureAggregation rollUp15Minutes(FeedKey feed, Range<Date> interval, boolean enableBreak) {
		Calendar calendar = getCalendar();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.setTime(interval.upperEndpoint());
		MeasureAggregation aggregation = new MeasureAggregation();
		while (calendar.getTime().after(interval.lowerEndpoint())) {
			Range<Date> range = current15Minutes(calendar.getTime());
			aggregation = aggregateRawTo15minutes(feed, range);
			if (!aggregation.isChanged() && enableBreak)
				break;
			calendar.add(Calendar.MINUTE, -15);
		}
		return aggregation;
	}

	private MeasureAggregation rollUp1Hour(String key, Range<Date> interval, int qualifier, boolean enableBreak) {
		Calendar calendar = getCalendar();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.setTime(interval.upperEndpoint());
		MeasureAggregation aggregation = new MeasureAggregation();
		while (calendar.getTime().after(interval.lowerEndpoint())) {
			Range<Date> hour = currentHour(calendar.getTime());
			aggregation = aggregate15minutesTo1hour(key, hour, qualifier);
			if (!aggregation.isChanged() && enableBreak)
				break;
			calendar.add(Calendar.HOUR_OF_DAY, -1);
		}
		return aggregation;
	}

	private MeasureAggregation rollUp1Day(String key, Range<Date> interval, int qualifier, boolean enableBreak) {
		Calendar calendar = getCalendar();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.setTime(interval.upperEndpoint());
		MeasureAggregation aggregation = new MeasureAggregation();
		while (calendar.getTime().after(interval.lowerEndpoint())) {
			Range<Date> day = currentDay(calendar.getTime());
			aggregation = aggregate1hourTo1day(key, day, qualifier);
			if (!aggregation.isChanged() && enableBreak)
				break;
			calendar.add(Calendar.DATE, -1);
		}
		return aggregation;
	}

	private MeasureAggregation rollUp1Week(String key, Range<Date> interval, int qualifier, boolean enableBreak) {
		Calendar calendar = getCalendarUTC();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.setTime(interval.upperEndpoint());
		MeasureAggregation aggregation = new MeasureAggregation();
		while (calendar.getTime().after(interval.lowerEndpoint())) {
			Range<Date> week = currentWeek(calendar.getTime());
			aggregation = aggregate1dayTo1week(key, week, qualifier);
			if (!aggregation.isChanged() && enableBreak)
				break;
			calendar.add(Calendar.DATE, -7);
		}
		return aggregation;
	}

	private MeasureAggregation rollUp1Month(String key, Range<Date> interval, int qualifier, boolean enableBreak) {
		Calendar calendar = getCalendarUTC();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.setTime(interval.upperEndpoint());
		MeasureAggregation aggregation = new MeasureAggregation();
		boolean monthlyChanged = false;
		while (calendar.getTime().after(interval.lowerEndpoint())) {
			Range<Date> month = currentMonth(calendar.getTime());
			aggregation = aggregate1dayTo1month(key, month, qualifier);
			if (aggregation.isChanged()) {
				monthlyChanged = true;
			}
			if (!aggregation.isChanged() && enableBreak)
				break;
			calendar.add(Calendar.MONTH, -1);
		}
		// on monthly aggregation change count records
		aggregation.setChanged(monthlyChanged);
		return aggregation;
	}

	private MeasureAggregation aggregateRawTo15minutes(FeedKey feed, Range<Date> interval) {
		Interpolation interpolation = Interpolation.MIN15;
		MeasureAggregation current = rollupQueries.getRollUp(feed.getKey(), interval, interpolation);
		MeasureAggregation measure = rollupQueries.calculateRollupRaw(feed, interval);

			logger.debug("Aggregate 15 Minutes {} [{} - {}) current: {} calculated: {}", feed.getKey(),
					sdf.format(interval.lowerEndpoint()), sdf.format(interval.upperEndpoint()), current, measure);
		return rollupQueries.updateAggregation(current, measure, interpolation);
	}

	private MeasureAggregation aggregate15minutesTo1hour(String key, Range<Date> interval, int qualifier) {
		Interpolation interpolation = Interpolation.H1;
		MeasureAggregation current = rollupQueries.getRollUp(key, interval, interpolation);
		MeasureAggregation measure = rollupQueries.calculateAggregation(key, qualifier, interval,
				Interpolation.MIN15.getColumnFamily());

			logger.debug("Aggregate 1 Hour {} [{} - {}) current: {} calculated: {}", key,
					sdf.format(interval.lowerEndpoint()), sdf.format(interval.upperEndpoint()), current, measure);
		return rollupQueries.updateAggregation(current, measure, interpolation);
	}

	private MeasureAggregation aggregate1hourTo1day(String key, Range<Date> interval, int qualifier) {
		Interpolation interpolation = Interpolation.D1;
		MeasureAggregation current = rollupQueries.getRollUp(key, interval, interpolation);

		MeasureAggregation measure = rollupQueries.calculateAggregation(key, qualifier, interval, Interpolation.H1.getColumnFamily());

			logger.debug("Aggregate 1 Day {} [{} - {}) current: {} calculated: {}", key,
					sdf.format(interval.lowerEndpoint()), sdf.format(interval.upperEndpoint()), current, measure);
		return rollupQueries.updateAggregation(current, measure, interpolation);
	}

	private MeasureAggregation aggregate1dayTo1week(String key, Range<Date> interval, int qualifier) {
		Interpolation interpolation = Interpolation.W1;

		MeasureAggregation current = rollupQueries.getRollUp(key, interval, interpolation);
		MeasureAggregation measure = rollupQueries.calculateAggregation(key, qualifier, interval, Interpolation.D1.getColumnFamily());

			logger.debug("Aggregate 1 Week {} [{} - {}) current: {} calculated: {} ", key,
					sdf.format(interval.lowerEndpoint()), sdf.format(interval.upperEndpoint()), current, measure);
		return rollupQueries.updateAggregation(current, measure, interpolation);

	}

	private MeasureAggregation aggregate1dayTo1month(String key, Range<Date> interval, int qualifier) {
		Interpolation interpolation = Interpolation.M1;
		MeasureAggregation current = rollupQueries.getRollUp(key, interval, interpolation);
		MeasureAggregation measure = rollupQueries.calculateAggregation(key, qualifier, interval, Interpolation.D1.getColumnFamily());

			logger.debug("Aggregate 1 Month {} [{} - {}) current: {} calculated: {}", key,
					sdf.format(interval.lowerEndpoint()), sdf.format(interval.upperEndpoint()), current, measure);
		return rollupQueries.updateAggregation(current, measure, interpolation);
	}

}
