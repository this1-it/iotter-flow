package it.thisone.iotter.cassandra;

import static it.thisone.iotter.cassandra.CassandraQueryBuilder.deleteKey;
import static it.thisone.iotter.cassandra.RollupQueryBuilder.fillMeasureStats;
import static it.thisone.iotter.cassandra.RollupQueryBuilder.prepareCountRollupStats;
import static it.thisone.iotter.cassandra.RollupQueryBuilder.prepareDeleteAllRollupStats;
import static it.thisone.iotter.cassandra.RollupQueryBuilder.prepareDeleteRollupStats;
import static it.thisone.iotter.cassandra.RollupQueryBuilder.prepareInsertQueueRollup;
import static it.thisone.iotter.cassandra.RollupQueryBuilder.prepareRollupLock;
import static it.thisone.iotter.cassandra.RollupQueryBuilder.prepareRollupUnlock;
import static it.thisone.iotter.cassandra.RollupQueryBuilder.prepareSelectAllRollupStats;
import static it.thisone.iotter.cassandra.RollupQueryBuilder.prepareSelectLock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.DriverException;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.google.common.collect.Range;

import it.thisone.iotter.cassandra.model.Feed;
import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.cassandra.model.MeasureAggregation;
import it.thisone.iotter.cassandra.model.MeasureDateComparator;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.cassandra.model.MeasureStats;
import it.thisone.iotter.cassandra.model.QueueRollup;
import it.thisone.iotter.config.Constants;

/**
 * Feature #194 : Data aggregation with rollup tables
 * http://btoddb-cass-storage.blogspot.it/
 *
 */
@Service
public class CassandraRollup implements CassandraConstants {

	private static final String UTC_TZ = "UTC";
	private static final int LOCK_TTL = 3600;

	@Autowired
	private MeasuresQueries measuresQueries;

	@Autowired
	private RollupQueries rollupQueries;

	@Autowired
	private CassandraClient client;

	@Autowired
	private RollupBounded rollupBounded;

	@Autowired
	private RollupSmart rollupSmart;
	
	@Autowired
	private SimpleDateFormat sdf;

	// private static Logger logger =
	// LoggerFactory.getLogger(CassandraRollup.class);
	private static Logger logger = LoggerFactory.getLogger(Constants.RollUp.ROLL_UP_LOG4J_CATEGORY);

	public Interpolation checkInterpolationAvailability(Interpolation interpolation, Range<Date> interval,
			Date firstMeasure, Date since) {
		List<Interpolation> values = rollupQueries.availableInterpolations(interval, firstMeasure, since);
		if (!values.contains(interpolation)) {
			for (Interpolation value : values) {
				if (value.greaterThan(interpolation)) {
					return value;
				}
			}
		}
		return interpolation;

	}

	public void resetRollUpStats(String sn) {
		try {
			Statement<?> stmt = prepareDeleteAllRollupStats(sn);
			client.executeWithRetry(stmt);
		} catch (Exception e) {
			logger.error("unrecoverable error, unable to delete rollup for sn " + sn, e);
		}
	}

	public void deleteRollUp(FeedKey feedKey) {
		List<String> rollup = new ArrayList<String>();
		rollup.add(ROLL_UP_MIN15_CF);
		rollup.add(ROLL_UP_H1_CF);
		rollup.add(ROLL_UP_D1_CF);
		rollup.add(ROLL_UP_W1_CF);
		rollup.add(ROLL_UP_M1_CF);
		try {
			Statement<?> stmt = prepareDeleteRollupStats(feedKey.getSerial(), feedKey.getKey());
			client.executeWithRetry(stmt);
			for (String columnFamily : rollup) {
				stmt = deleteKey(columnFamily, feedKey.getKey());
				client.executeWithRetry(stmt);
			}
		} catch (Exception e) {
			logger.error("unrecoverable error, unable to delete rollup for key " + feedKey.getKey(), e);
		}
	}

	public Range<Date> writeRollUp(Feed feed, String owner, final Range<Date> range, final boolean enableBreak) {
		String sn = feed.getSerial();
		String key = feed.getKey();
		
		FeedKey feedKey = new FeedKey(sn, key);
		feedKey.setQualifier(feed.getQualifier());

		Range<Date> interval = null;
		if (range != null) {
			interval = Range.closedOpen(range.lowerEndpoint(), range.upperEndpoint());
		}

		Date now = new Date();
		Date epoch = client.getEpoch();
		try {
			if (!lockFeed(key, LOCK_TTL)) {
				return null;
			}
			MeasureStats feedStats = rollupQueries.getRollupStats(sn, key);
			if (feedStats != null && feedStats.isValid()) {
				
				Date last = measuresQueries.getLastTick(sn, now);
				Date first = measuresQueries.getFirstTick(sn, feedStats.getSince());
				if (last == null || first == null) {
					if (last != null) {
						logger.error("feed key {} last == null", key);
					}
					if (first != null) {
						logger.error("feed key {} first == null", key);
					}
					return null;
				}

				rollupQueries.updateStatsRunning(sn, key);
				feedStats.setFirstMeasureDate(first);
				feedStats.setLastMeasureDate(last);

			} else {

				Date since = feed.getSince();
				if (since == null) {
					since = epoch;
				}
				Date last = measuresQueries.getLastTick(sn, now);
				Date first = measuresQueries.getFirstTick(sn, feed.getSince());
				if (last == null || first == null) {
					if (last == null) {
						logger.error("feed {} last tick == null", sn);
					}
					if (first == null) {
						logger.error("feed {} first tick == null", sn);
					}
					return null;
				}

				feedStats = new MeasureStats(sn, key);
				feedStats.setLabel(feed.getLabel());
				feedStats.setSince(since);
				feedStats.setQualifier(feed.getQualifier());
				feedStats.setOwner(owner);
				feedStats.setFirstMeasureDate(first);
				feedStats.setLastMeasureDate(last);
				feedStats.setCreated(new Date());
				feedStats.setFrequency(null);
				feedStats.setRunning(true);
				rollupQueries.updateStats(feedStats);
				interval = null;
			}

			Date begin = feedStats.getFirstMeasureDate();
			Date end = feedStats.getLastMeasureDate();

			if (begin.before(epoch)) {
				begin = epoch;
				feedStats.setFirstMeasureDate(epoch);
			}
			if (end.after(now)) {
				end = now;
			}
			
			feed.setAggregation(measuresQueries.getLastAggregationValue(key, begin));
			measuresQueries.updateFeedAggregation(feed);

			if (interval == null) {
				interval = Range.closed(begin, end);
			} else {
				Date lower = interval.lowerEndpoint();
				Date upper = interval.upperEndpoint();
				// change lower with feed last update date
				if (feedStats.getUpdated() != null && feedStats.getUpdated().before(lower)) {
					lower = feedStats.getUpdated();
				}
				if (lower.before(begin)) {
					begin = lower;
				}
				if (upper.after(end)) {
					end = upper;
				}
				interval = Range.closed(lower, upper);
			}
			String intervalString = sdf.format(interval.lowerEndpoint()) + " - " + sdf.format(interval.upperEndpoint());
			logger.debug("Rollup start feed key {} [{}), enableBreak {} ", key, intervalString, enableBreak);

			long duration = System.currentTimeMillis();

			if (enableBreak) {
				rollupSmart.rollUp(feedStats, interval, enableBreak);
			} else {
				rollupBounded.rollUp(feedStats, interval);
			}
			
			feedStats.setRunning(false);
			feedStats.setUpdated(new Date());
			rollupQueries.updateStats(feedStats);
			unlockFeed(key);
			duration = (System.currentTimeMillis() - duration);
			logger.debug("Rollup end feed key {}, elapsed {}", key, InterpolationUtils.elapsed(duration));

		} catch (Throwable e) {
			logger.error("unable to rollup key " + key, e);
			return null;
		}
		return interval;
	}

//	public MeasureStats createRollupStats(String sn, String key, String label, int qualifier) {
//		MeasureStats stats = new MeasureStats(sn, key);
//		try {
//			Statement stmt = prepareInsertRollupStats(sn, key, true, qualifier, client.writeConsistencyLevel());
//			client.executeWithRetry(stmt);
//		} catch (DriverException e) {
//			logger.error("cassandra not available creating RollUp " + stats.getKey(), e);
//		}
//		stats.setLabel(label);
//		stats.setFrequency(null);
//		stats.setCreated(new Date());
//		stats.setUpdated(new Date());
//		stats.setFirstMeasureDate(new Date());
//		stats.setLastMeasureDate(new Date());
//		return stats;
//	}

	public List<MeasureRaw> getData(FeedKey feedKey, Range<Date> interval, Interpolation interpolation) {
		String sn = feedKey.getSerial();
		String key = feedKey.getKey();
		List<MeasureRaw> measures = new ArrayList<MeasureRaw>();
		if (interpolation.equals(Interpolation.RAW)) {
			logger.error("unsuitable interpolation " + Interpolation.RAW);
			throw new IllegalArgumentException("unsuitable interpolation " + Interpolation.RAW);
		}

		TimeZone zone = TimeZone.getTimeZone(UTC_TZ);
		sdf.setTimeZone(zone);

		logger.debug("Retrieving Series {} from {} to {} with {}", key, sdf.format(interval.lowerEndpoint()),
				sdf.format(interval.upperEndpoint()), interpolation);

		MeasureStats stats = rollupQueries.getRollupStats(sn, key);
		feedKey.setQualifier(stats.getQualifier());
		Range<Date> rollupInterval = rollupQueries.availableRollupInterval(stats, interpolation);

		Range<Date> undoneInterval = null;
		boolean existRollUpData = rollupInterval != null && rollupInterval.isConnected(interval);

		boolean useRollUpData = true;
		if (existRollUpData) {
			if (interval.upperEndpoint().after(rollupInterval.upperEndpoint())) {
				undoneInterval = Range.closed(rollupInterval.upperEndpoint(), interval.upperEndpoint());
			}
		}

		String columnFamily = interpolation.getColumnFamily();
		if (columnFamily == null && existRollUpData) {
			Interpolation virtual = Interpolation.valueOf(interpolation.getVirtual());
			columnFamily = virtual.getColumnFamily();
			useRollUpData = false;
		} else if (!existRollUpData) {
			useRollUpData = false;
			columnFamily = MEASURES_CF;
		}

		boolean aggregation = false;
		
		if (useRollUpData) {
			measures.addAll(rollupQueries.rollUpData(key, interpolation.getColumnFamily(), interval, aggregation));
			logger.debug("Retrieved rollup for Series {} from {} to {} with {}  ", key, sdf.format(interval.lowerEndpoint()),
					sdf.format(interval.upperEndpoint()), interpolation.getColumnFamily());
			if (undoneInterval != null) {
				logger.debug("Undone rollup for Series {} from {} to {} with {} userollup {} ", feedKey.toString(), sdf.format(undoneInterval.lowerEndpoint()),
						sdf.format(undoneInterval.upperEndpoint()), interpolation, useRollUpData );

				
				measures.addAll(rollupQueries.rollUpVirtual(feedKey, stats.getQualifier(), interpolation, MEASURES_CF,
						undoneInterval, zone, aggregation));
				Collections.sort(measures, new MeasureDateComparator());
			}
		} else {

			logger.debug("Using virtual rollup with cf {} and interpolation {}", columnFamily, interpolation);
			measures.addAll(rollupQueries.rollUpVirtual(feedKey, feedKey.getQualifier(), interpolation, columnFamily,
					interval, zone, aggregation));
		}

		float value = 0f;
		for (MeasureRaw measureRaw : measures) {
			if (measureRaw.hasError() || measureRaw.getValue() == null) {
				measureRaw.setValue(value);
			} else {
				value = measureRaw.getValue();
			}
		}

		logger.debug("Retrieved Series {} size {}", key, measures.size());
		return measures;
	}

	public List<MeasureRaw> getAggregationData(FeedKey feedKey, Range<Date> interval, Interpolation interpolation,
			TimeZone zone) {

		String sn = feedKey.getSerial();
		String key = feedKey.getKey();
		List<MeasureRaw> measures = new ArrayList<MeasureRaw>();
		if (interpolation.equals(Interpolation.RAW)) {
			logger.error("unsuitable interpolation " + Interpolation.RAW);
			throw new IllegalArgumentException("unsuitable interpolation " + Interpolation.RAW);
		}

		sdf.setTimeZone(TimeZone.getTimeZone(UTC_TZ));

		logger.debug("Retrieving Aggregation {} from {} to {} with {}", key, sdf.format(interval.lowerEndpoint()),
				sdf.format(interval.upperEndpoint()), interpolation);

		Range<Date> dataInterval = null;
		Range<Date> undoneInterval = null;
		boolean useRollUpData = true;
		boolean aggregation = true;
		boolean useVirtual = false;

		MeasureStats stats = rollupQueries.getRollupStats(sn, key);

		if (stats != null) {
			dataInterval = rollupQueries.availableRollupInterval(stats,interpolation);
			feedKey.setQualifier(stats.getQualifier());
		}

		boolean existRollUpData = dataInterval != null && dataInterval.isConnected(interval);

		if (existRollUpData) {
			if (interval.upperEndpoint().after(dataInterval.upperEndpoint())) {
				undoneInterval = Range.closed(dataInterval.upperEndpoint(), interval.upperEndpoint());
				long undoneSecs = (interval.upperEndpoint().getTime() - dataInterval.upperEndpoint().getTime()) / 1000;
				useVirtual = interpolation.getSeconds() < undoneSecs;
			}
		}

		String columnFamily = interpolation.getColumnFamily();
		if (columnFamily == null && existRollUpData) {
			Interpolation virtual = Interpolation.valueOf(interpolation.getVirtual());
			columnFamily = virtual.getColumnFamily();
			useRollUpData = false;
		} else if (!existRollUpData) {
			columnFamily = MEASURES_CF;
			useRollUpData = false;
		}

		if (useRollUpData) {

			logger.debug("Using rollup with cf {}", interpolation.getColumnFamily());
			measures.addAll(rollupQueries.rollUpData(key, interpolation.getColumnFamily(), interval, aggregation));
			if (undoneInterval != null && useVirtual) {
				measures.addAll(rollupQueries.rollUpVirtual(feedKey, feedKey.getQualifier(), interpolation, MEASURES_CF,
						undoneInterval, zone, aggregation));
				Collections.sort(measures, new MeasureDateComparator());

			}
		} else {

			logger.debug("Using virtual rollup with cf {} and interpolation {}", columnFamily, interpolation);
			measures.addAll(rollupQueries.rollUpVirtual(feedKey, feedKey.getQualifier(), interpolation, columnFamily,
					interval, zone, aggregation));
		}

		logger.debug("Retrieved Aggregation {} size {}", key, measures.size());
		return measures;
	}

	@Deprecated
	public void delayRollup(Date lastRollup, List<MeasureAggregation> measures) {
		if (lastRollup == null) {
			return;
		}
		if (measures == null) {
			return;
		}
		for (MeasureAggregation measure : measures) {
			FeedKey feed = measure.getFeedKey();
			if (measure.getInterval().lowerEndpoint().before(lastRollup)) {
				delayRollup(feed, measure.getInterval());
			}
		}
	}

	/**
	 * Number of Rows
	 * 
	 * @return
	 */
	public Long countRollupStats(String serial) {
		long records = 0;
		try {
			Statement stmt = prepareCountRollupStats(serial, client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
				records = records + row.getLong(RECORDS);
			}
		} catch (DriverException e) {
			logger.error("cassandra not available for rollup ", e);
		} catch (Throwable re) {
			logger.error("unrecoverable error during rollup ", re);
		}
		return records;
	}

	private QueueRollup delayRollup(FeedKey feed, Range<Date> interval) {
		if (interval == null) {
			return null;
		}
		if (interval.isEmpty()) {
			return null;
		}
		QueueRollup item = new QueueRollup(feed, interval);
		try {
			Statement<?> stmt = prepareInsertQueueRollup(item, client.getMeasuresTTL(), client.writeConsistencyLevel());
			client.executeWithRetry(stmt);
		} catch (DriverException e) {
			logger.error("cassandra not available creating QueueRollup " + item.getKey(), e);
		}
		return item;
	}

	public List<MeasureStats> getRollupStats(String sn) {
		List<MeasureStats> stats = new ArrayList<>();
		try {
			Statement<?> stmt = prepareSelectAllRollupStats(sn, client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
				stats.add(fillMeasureStats(row));
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving stats " + sn, e);
		}
		return stats;
	}

	public void unlockFeed(String key) {
		try {
			Statement<?> stmt = prepareRollupUnlock(ROLL_UP_FEED_LOCK_CF, key);
			client.executeWithRetry(stmt);
			logger.debug("feed key {} unlocked  ", key);
		} catch (Throwable cause) {
			logger.error("unrecoverable error unlocking " + key, cause);
		}
	}

	public boolean lockFeed(String key, int ttl) {
		boolean applied = false;
		try {
			Statement<?> stmt = prepareRollupLock(ROLL_UP_FEED_LOCK_CF, key, ttl, client.writeConsistencyLevel());
			ResultSet rs = client.execute(stmt);
			applied = rs.one().getBool(0);
			logger.debug("feed key {} locked {} ", key, applied);
		} catch (Throwable cause) {
			logger.error("unrecoverable error locking " + key, cause);
		}
		return applied;
	}

	public void unlockSink(String key) {
		if (key == null) {
			return;
		}
		try {
			Statement<?> stmt = prepareRollupUnlock(ROLL_UP_SINK_LOCK_CF, key);
			client.executeWithRetry(stmt);
			logger.debug("sink key {} unlocked ", key);
		} catch (Throwable cause) {
			logger.error("unrecoverable error unlocking " + key, cause);
		}
	}

	public boolean existLockSink(String key) {
		boolean result = false;
		try {
			Statement<?> stmt = prepareSelectLock(ROLL_UP_SINK_LOCK_CF, key, client.readConsistencyLevel());
			// ResultSet rs = getSession().execute(stmt);
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			result = iter.hasNext();
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving  lock key " + key, e);
		}
		return result;
	}

	public boolean lockSink(String key, int ttl) {
		if (key == null) {
			return false;
		}
		if (ttl == 0) {
			return false;
		}

		boolean applied = existLockSink(key);
		if (applied)
			return false;

		try {
			Statement<?> stmt = prepareRollupLock(ROLL_UP_SINK_LOCK_CF, key, ttl, client.writeConsistencyLevel());
			ResultSet rs = client.execute(stmt);
			applied = rs.one().getBool(0);
			logger.debug("sink key {} locked {} ", key, applied);
		} catch (Throwable cause) {
			logger.error("unrecoverable error locking " + key, cause);
		}
		return applied;
	}

	public Interpolation interpolationChoice(String sn, String key, Range<Date> interval, int displayPoints) {
		return rollupQueries.interpolationChoice(sn, key, interval, displayPoints);
	}

	public List<Interpolation> availableInterpolations(Range<Date> interval, Date firstMeasure, Date since) {
		return rollupQueries.availableInterpolations(interval, firstMeasure, since);
	}

	public List<MeasureAggregation> rollUpData(String key, Interpolation interpolation, Range<Date> interval) {
		return rollupQueries.rollUpData(key, interpolation, interval);
	}

	public boolean isFullyAvailableInterpolation(Interpolation interpolation, Range<Date> interval, Date firstMeasure,
			Date since) {
		return rollupQueries.isFullyAvailableInterpolation(interpolation, interval, firstMeasure, since);
	}

	public boolean countAgain(Date last) {
		return rollupQueries.countAgain(last);
	}

	public void resetLockSink(String key) {
		StringBuffer sb = new StringBuffer();
		try {
			String query = String.format("SELECT * FROM %s.%s", CassandraClient.getKeySpace(), ROLL_UP_SINK_LOCK_CF);
			SimpleStatement stmt = SimpleStatement.builder(query)
					.setConsistencyLevel(client.readConsistencyLevel())
					.build();
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
				String lock = row.getString(KEY);
				if (lock.startsWith(key)) {
					this.unlockSink(lock);
					sb.append(lock);
				}
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving locks " + key, e);
		}
		// 683xih2102R2 292zyp2002R2 ab2ssy2002R2
		logger.info("cassandra resetLockSink {}: {}", key, sb.toString());

	}

}
