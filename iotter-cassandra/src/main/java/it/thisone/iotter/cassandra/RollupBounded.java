package it.thisone.iotter.cassandra;

import static it.thisone.iotter.cassandra.InterpolationUtils.current15Minutes;
import static it.thisone.iotter.cassandra.InterpolationUtils.currentDay;
import static it.thisone.iotter.cassandra.InterpolationUtils.currentHour;
import static it.thisone.iotter.cassandra.InterpolationUtils.currentMonth;
import static it.thisone.iotter.cassandra.InterpolationUtils.currentWeek;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Range;

import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.cassandra.model.MeasureAggregation;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.cassandra.model.MeasureStats;

@Service
public class RollupBounded extends RollupQueryBuilder {

	@Autowired
	private RollupQueries rollupQueries;

	@Autowired
	private MeasuresQueries measuresQueries;

	//private static Logger logger = LoggerFactory.getLogger(Constants.RollUp.ROLL_UP_LOG4J_CATEGORY);
	private static Logger logger = LoggerFactory.getLogger(RollupBounded.class);

	
	protected void rollUp(MeasureStats feedStats, Range<Date> interval) {
		List<Range<Date>> buckets = InterpolationUtils.splitPeriod(TimeZone.getTimeZone("UTC"), Interpolation.D1, interval);
		for (Range<Date> bucket : buckets) {
			rollUpInterval(feedStats, bucket);
		}
		if (rollupQueries.countAgain(feedStats.getUpdated())) {
			long records = rollupQueries.countRecords(feedStats.getKey(), feedStats.getSince(), new Date());
			feedStats.setRecords(records);
		}		
	}
		
	
	private void rollUpInterval(MeasureStats feedStats, Range<Date> interval) {
		long duration = System.currentTimeMillis();

		String sn = feedStats.getSerial();
		String key = feedStats.getKey();
		int qualifier = feedStats.getQualifier();

		FeedKey feedKey = new FeedKey(sn, key);
		feedKey.setQualifier(qualifier);

		duration = System.currentTimeMillis();
		interval = min15Interval(interval.lowerEndpoint(), interval.upperEndpoint());
		List<MeasureAggregation> aggregations = rollUp15Minutes(feedStats, interval);
		logger.debug("rollUp15Minutes key: {}, written: {}, interval: {}, elapsed: {}", key, aggregations.size(),
				InterpolationUtils.elapsed(interval),
				InterpolationUtils.elapsed(System.currentTimeMillis() - duration));

		duration = System.currentTimeMillis();
		interval = hourInterval(aggregations);
		aggregations = rollUp1Hour(feedStats, interval);
		logger.debug("rollUp1Hour key: {}, written: {}, interval: {}, elapsed: {}", key, aggregations.size(),
				InterpolationUtils.elapsed(interval),
				InterpolationUtils.elapsed(System.currentTimeMillis() - duration));

		duration = System.currentTimeMillis();
		interval = dayInterval(aggregations);
		aggregations = rollUp1Day(feedStats, interval);
		logger.debug("rollUp1Day key: {}, written: {}, interval: {}, elapsed: {}", key, aggregations.size(),
				InterpolationUtils.elapsed(interval),
				InterpolationUtils.elapsed(System.currentTimeMillis() - duration));

		duration = System.currentTimeMillis();
		interval = weekInterval(aggregations);
		aggregations = rollUp1Week(feedStats, interval);
		logger.debug("rollUp1Week key: {}, written: {}, interval: {}, elapsed: {}", key, aggregations.size(),
				InterpolationUtils.elapsed(interval),
				InterpolationUtils.elapsed(System.currentTimeMillis() - duration));

		duration = System.currentTimeMillis();
		interval = monthInterval(aggregations);
		aggregations = rollUp1Month(feedStats, interval);
		logger.debug("rollUp1Month key: {}, written: {} interval: {} elapsed: {}", key, aggregations.size(),
				InterpolationUtils.elapsed(interval),
				InterpolationUtils.elapsed(System.currentTimeMillis() - duration));





	}

	private List<MeasureAggregation> rollUp1Month(MeasureStats feedStats, Range<Date> interval) {
		String sn = feedStats.getSerial();
		String key = feedStats.getKey();
		int qualifier = feedStats.getQualifier();

		FeedKey feedKey = new FeedKey(sn, key);
		feedKey.setQualifier(qualifier);
		List<MeasureAggregation> aggregations = rollupQueries.rollUpData(key, Interpolation.D1, interval);
		List<MeasureAggregation> result = new ArrayList<>();
		if (aggregations.isEmpty()) {
			return result;
		}
		Calendar calendar = getCalendar();
		calendar.setTime(interval.upperEndpoint());
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);

		while (calendar.getTime().compareTo(interval.lowerEndpoint()) >= 0) {
			Range<Date> range = currentMonth(calendar.getTime());
			List<MeasureAggregation> items = aggregations.stream().filter(o -> contains(range, o.getDate()))
					.collect(Collectors.toList());
			if (!items.isEmpty()) {
				MeasureAggregation aggregation = rollupQueries.aggregate(key, qualifier, range, items);
				result.add(aggregation);
				// logger.debug("rollUp1Month {} {} {}", aggregation.getKey(),
				// aggregation.getDate(), range.toString());

			}
			calendar.add(Calendar.MONTH, -1);
		}
		rollupQueries.updateAggregationsBatch(result, Interpolation.M1);
		return result;

	}

	private List<MeasureAggregation> rollUp1Week(MeasureStats feedStats, Range<Date> interval) {
		String sn = feedStats.getSerial();
		String key = feedStats.getKey();
		int qualifier = feedStats.getQualifier();

		FeedKey feedKey = new FeedKey(sn, key);
		feedKey.setQualifier(qualifier);
		List<MeasureAggregation> aggregations = rollupQueries.rollUpData(key, Interpolation.D1, interval);
		List<MeasureAggregation> result = new ArrayList<>();
		if (aggregations.isEmpty()) {
			return result;
		}
		Calendar calendar = getCalendar();
		calendar.setTime(interval.upperEndpoint());
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);

		while (calendar.getTime().compareTo(interval.lowerEndpoint()) >= 0) {
			Range<Date> range = currentWeek(calendar.getTime());
			List<MeasureAggregation> items = aggregations.stream().filter(o -> contains(range, o.getDate()))
					.collect(Collectors.toList());
			if (!items.isEmpty()) {
				MeasureAggregation aggregation = rollupQueries.aggregate(key, qualifier, range, items);
				result.add(aggregation);
				// logger.debug("rollUp1Week {} {} {}", aggregation.getKey(),
				// aggregation.getDate(), range.toString());

			}
			calendar.add(Calendar.WEEK_OF_YEAR, -1);
		}
		rollupQueries.updateAggregationsBatch(result, Interpolation.W1);
		return result;
	}

	private List<MeasureAggregation> rollUp1Day(MeasureStats feedStats, Range<Date> interval) {
		String sn = feedStats.getSerial();
		String key = feedStats.getKey();
		int qualifier = feedStats.getQualifier();

		FeedKey feedKey = new FeedKey(sn, key);
		feedKey.setQualifier(qualifier);
		List<MeasureAggregation> aggregations = rollupQueries.rollUpData(key, Interpolation.H1, interval);
		List<MeasureAggregation> result = new ArrayList<>();
		if (aggregations.isEmpty()) {
			return result;
		}
		Calendar calendar = getCalendar();
		calendar.setTime(interval.upperEndpoint());
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);

		while (calendar.getTime().compareTo(interval.lowerEndpoint()) >= 0) {
			Range<Date> range = currentDay(calendar.getTime());
			List<MeasureAggregation> items = aggregations.stream().filter(o -> contains(range, o.getDate()))
					.collect(Collectors.toList());
			if (!items.isEmpty()) {
				MeasureAggregation aggregation = rollupQueries.aggregate(key, qualifier, range, items);
				result.add(aggregation);
				// logger.debug("rollUp1Day {} {} {}", aggregation.getKey(),
				// aggregation.getDate(), range.toString());

			}
			calendar.add(Calendar.DATE, -1);
		}
		rollupQueries.updateAggregationsBatch(result, Interpolation.D1);
		return result;

	}

	private List<MeasureAggregation> rollUp1Hour(MeasureStats feedStats, Range<Date> interval) {
		String sn = feedStats.getSerial();
		String key = feedStats.getKey();
		int qualifier = feedStats.getQualifier();

		FeedKey feedKey = new FeedKey(sn, key);
		feedKey.setQualifier(qualifier);

		List<MeasureAggregation> aggregations = rollupQueries.rollUpData(key, Interpolation.MIN15, interval);
		List<MeasureAggregation> result = new ArrayList<>();
		
		if (aggregations.isEmpty()) {
			logger.debug("rollUp1Hour {} data not found in {}", feedKey.getKey(), interval );
			return result;
		}

		
		Calendar calendar = getCalendar();
		calendar.setTime(interval.upperEndpoint());
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);

		while (calendar.getTime().compareTo(interval.lowerEndpoint()) >= 0) {
			Range<Date> range = currentHour(calendar.getTime());
			List<MeasureAggregation> items = aggregations.stream().filter(o -> contains(range, o.getDate()))
					.collect(Collectors.toList());
			if (!items.isEmpty()) {
				MeasureAggregation aggregation = rollupQueries.aggregate(key, qualifier, range, items);
				result.add(aggregation);
				// logger.debug("rollUp1Hour {} {} {}", aggregation.getKey(),
				// aggregation.getDate(), range.toString());
			}
			calendar.add(Calendar.HOUR, -1);
		}
		rollupQueries.updateAggregationsBatch(result, Interpolation.H1);
		return result;

	}

	private List<MeasureAggregation> rollUp15Minutes(MeasureStats feedStats, Range<Date> interval) {
		// frequency on raw data
		// frequency = aggregation.getFrequency();

		String sn = feedStats.getSerial();
		String key = feedStats.getKey();
		int qualifier = feedStats.getQualifier();
		FeedKey feedKey = new FeedKey(sn, key);
		feedKey.setQualifier(qualifier);

		List<MeasureAggregation> result = new ArrayList<>();
		List<MeasureRaw> raw = measuresQueries.fetchRaw(feedKey, interval, null);
		if (raw.isEmpty()) {
//			String format = "dd/MM/yy HH:mm:ss.SSS";
//			SimpleDateFormat sdf = new SimpleDateFormat(format);
//			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//			Feed feed = rollupQueries.getFeed(sn, key);
//			logger.debug("rollUp15Minutes {} last: {} raw data not found in [{}, {}]", feedKey.getKey(), sdf.format(feed.getDate()), sdf.format(interval.lowerEndpoint()), sdf.format(interval.upperEndpoint()) );
			return result;
		}
		Calendar calendar = getCalendar();
		calendar.setTime(interval.upperEndpoint());
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		while (calendar.getTime().compareTo(interval.lowerEndpoint()) >= 0) {
			Range<Date> range = current15Minutes(calendar.getTime());
			List<MeasureRaw> items = raw.stream().filter(o -> contains(range, o.getDate()))
					.collect(Collectors.toList());
			if (!items.isEmpty()) {
				MeasureAggregation aggregation = rollupQueries.aggregateRaw(key, qualifier, range, items);
				// logger.debug("rollUp15Minutes {} {} {} found {}/{}",
				// aggregation.getKey(), aggregation.getDate(),
				// range.toString(), items.size(), raw.size());
				result.add(aggregation);
			}
			calendar.add(Calendar.MINUTE, -15);
		}
		//Float frequency = rollupQueries.calculateFrequency((long) raw.size(), interval.lowerEndpoint(), interval.upperEndpoint());
		//feedStats.setFrequency(frequency);
		if (result.size() > 0) {
			Float frequency = result.get(result.size() -1).getFrequency();
			feedStats.setFrequency(frequency);		
		}
		rollupQueries.updateAggregationsBatch(result, Interpolation.MIN15);
		return result;
	}

	/*
	 * Excludes upper endpoint
	 */
	private static boolean contains(Range<Date> range, Date date) {
		return (range.lowerEndpoint().compareTo(date) <= 0) && (range.upperEndpoint().compareTo(date) > 0);
	}

	private static Range<Date> min15Interval(Date lower, Date upper) {
		return Range.closedOpen(current15Minutes(lower).lowerEndpoint(), current15Minutes(upper).upperEndpoint());
	}

	private Range<Date> monthInterval(List<MeasureAggregation> aggregations) {
		if (aggregations.isEmpty())
			return Range.singleton(new Date());
		Date upper = aggregations.get(0).getDate();
		Date lower = aggregations.get(aggregations.size() - 1).getDate();
		return Range.closedOpen(currentMonth(lower).lowerEndpoint(), currentMonth(upper).upperEndpoint());
	}

	private Range<Date> weekInterval(List<MeasureAggregation> aggregations) {
		if (aggregations.isEmpty())
			return Range.singleton(new Date());
		Date upper = aggregations.get(0).getDate();
		Date lower = aggregations.get(aggregations.size() - 1).getDate();
		return Range.closedOpen(currentWeek(lower).lowerEndpoint(), currentWeek(upper).upperEndpoint());
	}

	private Range<Date> dayInterval(List<MeasureAggregation> aggregations) {
		if (aggregations.isEmpty())
			return Range.singleton(new Date());
		Date upper = aggregations.get(0).getDate();
		Date lower = aggregations.get(aggregations.size() - 1).getDate();
		return Range.closedOpen(currentDay(lower).lowerEndpoint(), currentDay(upper).upperEndpoint());
	}

	private Range<Date> hourInterval(List<MeasureAggregation> aggregations) {
		if (aggregations.isEmpty())
			return Range.singleton(new Date());
		Date upper = aggregations.get(0).getDate();
		Date lower = aggregations.get(aggregations.size() - 1).getDate();
		return Range.closedOpen(currentHour(lower).lowerEndpoint(), currentHour(upper).upperEndpoint());
	}

}
