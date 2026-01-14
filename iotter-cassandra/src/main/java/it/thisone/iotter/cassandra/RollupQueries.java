package it.thisone.iotter.cassandra;

import static it.thisone.iotter.cassandra.FeedsQueryBuilder.fillFeed;
import static it.thisone.iotter.cassandra.FeedsQueryBuilder.prepareSelectFeed;
import static it.thisone.iotter.cassandra.InterpolationUtils.currentMonth;
import static it.thisone.iotter.cassandra.InterpolationUtils.toServerRange;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.DriverException;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BatchableStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.google.common.collect.Range;

import it.thisone.iotter.cassandra.model.Feed;
import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.cassandra.model.MeasureAggregation;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.cassandra.model.MeasureStats;
import it.thisone.iotter.cassandra.model.QueueRollup;
import it.thisone.iotter.cassandra.model.SummaryMeasure;

/**
 * Feature #194 : Data aggregation with rollup tables
 * Cassandra Storage Sizing
 * http://btoddb-cass-storage.blogspot.it/
 *
 */
@Service
public class RollupQueries extends RollupQueryBuilder {

	private static final int AGGREGATION_UPDATE_BATCH_SIZE = 16;

	private static final ConsistencyLevel READ_CONSISTENCY_LEVEL = ConsistencyLevel.LOCAL_QUORUM;
	
	@Autowired
	protected MeasuresQueries measuresQueries;

	@Autowired
	protected SimpleDateFormat sdf;

	@Autowired
	private CassandraClient client;


	protected static final long serialVersionUID = 1L;
	//protected static final boolean ENABLE_LOG = true;
	protected static Logger logger = LoggerFactory.getLogger(RollupQueries.class);

	public Interpolation interpolationChoice(String sn, String key, Range<Date> interval, int displayPoints) {
		MeasureStats stats = getRollupStats(sn, key);
		if (stats == null) {
			logger.debug("feed key {} has no roll up", key);
			return Interpolation.RAW;
		}
		if (stats.getFrequency() == null || stats.getFrequency() == 0f) {
			logger.debug("feed key {} has no frequency", key);
			return Interpolation.RAW;
		}
		int seconds = (int) ((float) (interval.upperEndpoint().getTime() - interval.lowerEndpoint().getTime())
				/ (float) 1000);
		
		Interpolation interpolation = suitableInterpolation(seconds, displayPoints, stats.getFrequency());
		logger.debug("suitable interpolation {} feed key {} with points {}", interpolation, key, displayPoints);
		List<Interpolation> values = availableInterpolations(interval, stats.getFirstMeasureDate(), stats.getSince());
		if (!values.contains(interpolation)) {
			for (Interpolation value : values) {
				if (value.greaterThan(interpolation)) {
					logger.debug("feed key {} with available interpolation {} ", key, value);
					return value;
				}
			}
		}
		return interpolation;
	}

	protected List<Interpolation> availableInterpolations(Range<Date> interval, Date firstMeasure, Date since) {
		List<Interpolation> values = new ArrayList<Interpolation>();
		for (Interpolation value : Interpolation.values()) {
			if (isFullyAvailableInterpolation(value, interval, firstMeasure, since)) {
				values.add(value);
			}
		}
		return values;
	}

	protected boolean isFullyAvailableInterpolation(Interpolation type, Range<Date> interval, Date firstMeasure,
			Date since) {
		if (client.getMeasuresTTL() == 0)
			return true;
		if (type.greaterThan(client.getMinimalInterpolation()))
			return true;
		if (type.equals(client.getMinimalInterpolation()))
			return true;

		if (firstMeasure == null) {
			long millis = System.currentTimeMillis() - (client.getMeasuresTTL() * 1000l);
			firstMeasure = new Date(millis);
		}

		switch (type) {
		case RAW:
		case MIN1:
		case MIN5:
		case MIN15:
			return firstMeasure.before(interval.lowerEndpoint());
		default:
			return interval.contains(firstMeasure) || firstMeasure.before(interval.upperEndpoint());
		}

	}

	protected Interpolation suitableInterpolation(int seconds, int displayPoints, float frequency) {
		if (displayPoints <= 0) {
			displayPoints = 600;
		}
		int raw = (int) (seconds / frequency);
		int min1 = (int) ((float) seconds / (float) Interpolation.MIN1.getSeconds());
		int min5 = (int) ((float) seconds / (float) Interpolation.MIN5.getSeconds());
		int min15 = (int) ((float) seconds / (float) Interpolation.MIN15.getSeconds()) * 2;
		int h1 = (int) ((float) seconds / (float) Interpolation.H1.getSeconds()) * 2;
		int h6 = (int) ((float) seconds / (float) Interpolation.H6.getSeconds()) * 2;
		int d1 = (int) ((float) seconds / (float) Interpolation.D1.getSeconds()) * 2;
		int w1 = (int) ((float) seconds / (float) Interpolation.W1.getSeconds()) * 2;
		int m1 = (int) ((float) seconds / (float) Interpolation.M1.getSeconds()) * 2;

		if (seconds < Interpolation.MIN15.getSeconds()) {
			return Interpolation.RAW;
		}

		if (raw <= min15) {
			return Interpolation.RAW;
		}

		Map<Integer, String> map = new HashMap<Integer, String>();

		map.put(raw, Interpolation.RAW.name());
		map.put(min1, Interpolation.MIN1.name());
		map.put(min5, Interpolation.MIN5.name());
		map.put(min15, Interpolation.MIN15.name());
		map.put(h1, Interpolation.H1.name());
		map.put(h6, Interpolation.H6.name());
		map.put(d1, Interpolation.D1.name());
		map.put(w1, Interpolation.W1.name());
		List<Integer> suitable = new ArrayList<Integer>();
		suitable.add(raw);
		suitable.add(min1);
		suitable.add(min5);
		suitable.add(min15);
		suitable.add(h1);
		suitable.add(h6);
		suitable.add(d1);
		suitable.add(w1);
		suitable.add(m1);
		Integer value = -1;
		for (Integer key : suitable) {
			if (displayPoints > key) {
				value = key;
				break;
			}
		}

		String name = map.get(value) != null ? map.get(value) : Interpolation.W1.name();
		Interpolation interpolation = Interpolation.valueOf(name);
		return interpolation;
	}

	protected Feed getFeed(String serial, String key) {
		Feed item = null;
		try {
			Statement stmt = prepareSelectFeed(serial, key, client.readConsistencyLevel());
			ResultSet rs = getSession().execute(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = rs.one();
				item = fillFeed(row);
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving  Feed for key " + key, e);
		}
		return item;
	}

	protected Float calculateFrequency(Long records, Date begin, Date end) {
		Float frequency = 0f;
		if (records != null && records > 1 && end.after(begin)) {
			float seconds = (end.getTime() - begin.getTime()) / 1000;
			frequency = seconds / (float) (records - 1);
		}
		if (frequency == 0f) {
			frequency = 10f;
		}
		
		return frequency;
	}




	protected MeasureAggregation calculateRollupRaw(FeedKey feedKey, Range<Date> interval) {
		List<MeasureRaw> items = measuresQueries.fetchRaw(feedKey, interval, null);
		//List<MeasureRaw> items = null;
		return aggregateRaw(feedKey.getKey(), feedKey.getQualifier(),interval, items);
	}

	protected MeasureAggregation aggregateRaw(String key, int qualifier, Range<Date> range,
			List<MeasureRaw> items) {
		MeasureAggregation measure = new MeasureAggregation(key, range);
		measure.setQualifier(qualifier);
		
		SummaryMeasure stats = new SummaryMeasure(measure.getQualifier());
		Date lower = measure.getInterval().upperEndpoint();
		Date upper = measure.getInterval().lowerEndpoint();
		Float value = null;
		for (MeasureRaw raw : items) {
			if (raw.getDate().after(upper)) {
				upper = raw.getDate();
			}
			if (raw.getDate().before(lower)) {
				lower = raw.getDate();
			}
			value = raw.getValue();
			stats.add(raw);
		}
		int count = items.size();
		if (count > 0) {
			// change interval for frequency calculation
			measure.setInterval(Range.closedOpen(lower, upper));
		}
		if (count > 0 && !stats.isValid()) {
			// all errors !!!
			measure.setValue(value);
		}
		return setStatistics(measure, count, count, stats);
	}

	protected MeasureAggregation calculateAggregation(String key, int qualifier, Range<Date> interval, String columnFamily) {
		Statement<?> stmt = prepareSelectRollupData(columnFamily, key, interval, READ_CONSISTENCY_LEVEL);
		List<MeasureAggregation> items = new ArrayList<MeasureAggregation>();
		ResultSet rs = client.executeWithRetry(stmt);
		Iterator<Row> iter = rs.iterator();
		while (iter.hasNext()) {
			Row row = iter.next();
			items.add(fillMeasureAggregation(row));
		}
		return aggregate(key, qualifier, interval, items);
	}

	protected MeasureAggregation aggregate(String key, int qualifier, Range<Date> interval,
			List<MeasureAggregation> items) {
		MeasureAggregation aggregation = new MeasureAggregation(key, interval);
		aggregation.setQualifier(qualifier);
		SummaryMeasure stats = new SummaryMeasure(aggregation.getQualifier());
		int count = items.size();
		long records = 0;
		for (MeasureAggregation item : items) {
			records = records + item.getRecords();
			if (qualifier > -1) {
				stats.add(item);
			}
		}
		return setStatistics(aggregation, count, records, stats);
	}

	protected MeasureAggregation getRollUp(String key, Range<Date> interval, Interpolation interpolation) {
		MeasureAggregation measure = new MeasureAggregation(key, interval);
		Statement<?> stmt = prepareSelectRollup(measure, ROLLUP_FIELDS, interpolation, READ_CONSISTENCY_LEVEL);
		// ResultSet rs = getSession().execute(stmt);
		ResultSet rs = client.executeWithRetry(stmt);
		Iterator<Row> iter = rs.iterator();
		if (iter.hasNext()) {
			Row row = rs.one();
			measure.setCount(row.getInt(COUNT));
			measure.setRecords(row.getLong(RECORDS));
			return measure;
			// return fillRollupAggregation(row, measure);
		}
		return null;

		// return retrieveRollUp(measure, stmt);
	}

	/**
	 * 
	 * @param measure
	 * @param count   how many records has been collected in aggregation
	 * @param records how many raw has been collected
	 * @param errors
	 * @param stats
	 * @return
	 */
	protected MeasureAggregation setStatistics(MeasureAggregation measure, int count, long records, SummaryMeasure stats) {

		measure.setCount(count);
		measure.setRecords(records);

		if (records > 0 && count > 0) {
			MeasureRaw firstError = stats.getError();
			if (firstError != null) {
				measure.setError(firstError.getError());
				measure.setErrorDate(firstError.getDate());
			}

			if (stats.isValid()) {
				measure.setValue(stats.getValue());
				measure.setMaxValue(stats.getMaxValue());
				measure.setMinValue(stats.getMinValue());
				measure.setMaxDate(stats.getMaxDate());
				measure.setMinDate(stats.getMinDate());
				if (measure.getMinValue() != null) {
					if (measure.getMinDate() == null) {
						measure.setMinDate(measure.getDate());
					}
				}
				if (measure.getMaxValue() != null) {
					if (measure.getMaxDate() == null) {
						measure.setMaxDate(measure.getDate());
					}
				}
			} else {
				// all errors !!!
				//
			}
		} else {
			// clean up !!
			measure.setValue(Float.NaN);
			measure.setMaxValue(Float.NaN);
			measure.setMinValue(Float.NaN);

			measure.setMaxDate(null);
			measure.setMinDate(null);
			measure.setError(null);
			measure.setErrorDate(null);
		}

		return measure;
	}

//	protected MeasureAggregation retrieveRollUp(MeasureAggregation measure, Statement stmt) {
//		ResultSet rs = getSession().execute(stmt);
//		Iterator<Row> iter = rs.iterator();
//		if (iter.hasNext()) {
//			Row row = rs.one();
//			return fillRollupAggregation(row, measure);
//		}
//		return null;
//	}

	protected void updateStats(MeasureStats stats) {
		Statement<?> stmt = prepareUpdateRollupStats(stats, client.writeConsistencyLevel());
		getSession().execute(stmt);
	}

	protected void updateStatsRunning(String sn, String key) {
		Statement<?> stmt = prepareUpdateStatsRunning(sn, key, client.writeConsistencyLevel());
		getSession().execute(stmt);
	}

	protected MeasureAggregation updateAggregation(MeasureAggregation current, MeasureAggregation calculated,
			Interpolation interpolation) {
		if (current != null && current.equals(calculated)) {
			// nothing todo
			current.setChanged(false);
			return current;
		}
		if (current != null && current.getCount() >= calculated.getCount()) {
			// same records has been deleted, keep current
			current.setChanged(false);
			return current;
		}
		int ttl = 0;
		if (client.getMinimalInterpolation().greaterThan(interpolation)) {
			ttl = client.getMeasuresTTL();
		}
		Statement<?> stmt = prepareUpdateRollup(calculated, interpolation, ttl, client.writeConsistencyLevel());
		client.executeWithRetry(stmt);
		calculated.setChanged(true);
		return calculated;
	}

	protected MeasureStats getRollupStats(String sn, String key) {
		MeasureStats stats = null;
		try {
			Statement<?> stmt = prepareSelectRollupStats(sn, key, client.readConsistencyLevel());
			ResultSet rs = getSession().execute(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = rs.one();
				stats = fillMeasureStats(row);
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving stats " + key, e);
		}
		return stats;
	}

	protected Range<Date> availableRollupInterval(MeasureStats stats, Interpolation interpolation) {
		if (stats == null) {
			return null;
		}
		if (!stats.isValid()) {
			return null;
		}

		// Bug #2023
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		
		switch (interpolation) {
		case D1:
		case M1:
		case H1:
			calendar.add(Calendar.MONTH, -3);
			break;

		default:
			calendar.add(Calendar.DATE, -7);
			break;
			
		}
		Date sinceDate = calendar.getTime().after(stats.getSince()) ? calendar.getTime() : stats.getSince();
		Range<Date> interval = null;
		
		try {
			// Bug #2091
			interval = Range.closed(sinceDate, stats.getLastMeasureDate());
			logger.debug("Available rollup interval {} from {} to {} ", stats.getKey(),
					sdf.format(interval.lowerEndpoint()), sdf.format(interval.upperEndpoint()));

		} catch (IllegalArgumentException e) {
			logger.error(stats.getKey(), e);
		}

		return interval;
	}

	protected List<MeasureRaw> rollUpVirtual(FeedKey feedKey, int qualifier, Interpolation interpolation, String cf,
			Range<Date> range, TimeZone zone, boolean aggregation) {
		feedKey.setQualifier(qualifier);
		MeasureAggregation measure = new MeasureAggregation();
		measure.setKey(feedKey.getKey());

		
		List<MeasureRaw> measures = new ArrayList<MeasureRaw>();
		if (!zone.getID().equals(UTC_TZ)) {
			range = toServerRange(range, zone);
		}
		List<Range<Date>> buckets = InterpolationUtils.splitPeriod(zone, interpolation, range);
		for (Range<Date> bucket : buckets) {
			logger.debug("rollUpVirtual {} bucket {}", feedKey.getKey(), bucket.toString());
			if (cf.equals(MEASURES_CF)) {
				measure = calculateRollupRaw(feedKey, bucket);
			} else {
				measure = calculateAggregation(feedKey.getKey(), qualifier, bucket, cf);
			}
			if (aggregation) {
				MeasureRaw raw = new MeasureRaw(measure.getDate(), measure.getValue(), null);
				raw.setKey(feedKey.getKey());
				measures.add(raw);
			} else {
				measures.addAll(InterpolationUtils.deaggregate(measure));
			}
		}
		return measures;
	}

	protected List<MeasureRaw> rollUpData(String key, String columnFamily, Range<Date> interval, boolean aggregation) {
		List<MeasureRaw> items = new ArrayList<MeasureRaw>();
		try {
			Statement<?> stmt = prepareSelectRollupData(columnFamily, key, interval, READ_CONSISTENCY_LEVEL);
			ResultSet rs = getSession().execute(stmt);
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
				items.addAll(fillRollupMeasures(row, aggregation));
			}
		} catch (DriverException e) {
			logger.error("cassandra not available for aggregation " + key, e);
		} catch (Throwable re) {
			logger.error("unrecoverable error during aggregation " + key, re);
		}
		return items;
	}

	protected com.datastax.oss.driver.api.core.CqlSession getSession() {
		return client.getSession();
	}



	protected void delete(QueueRollup item) {
		try {
			Statement<?> stmt = prepareDeleteQueueRollup(item).setConsistencyLevel(DELETE_CONSISTENCY_LEVEL);
			client.executeWithRetry(stmt);
		} catch (DriverException e) {
			logger.error("cassandra not available deleting QueueRollup " + item.getKey(), e);
		}
	}

	protected List<QueueRollup> delayedIntervals(String sn, String key) {
		List<QueueRollup> items = new ArrayList<QueueRollup>();
		try {
			Statement stmt = prepareSelectQueueRollup(sn, key, client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
				items.add(fillQueueRollup(row));
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving  QueueRollup for sn " + sn, e);
		}

		return items;
	}

	protected Range<Date> checkDelayedIntervals(List<QueueRollup> items, Range<Date> range) {
		List<Date> list = new ArrayList<>();
		if (items.isEmpty()) {
			return range;
		}
		if (range != null) {
			list.add(range.lowerEndpoint());
			list.add(range.upperEndpoint());
		}
		for (QueueRollup item : items) {
			list.add(item.getFirst());
			list.add(item.getLast());
		}
		Range<Date> interval = Range.encloseAll(list);
		return interval;
	}

	protected Date getFirstQueueRollupDate(String sn, String key) {
		Date date = null;
		Statement<?> stmt = prepareSelectFirstQueueRollup(sn, key, client.readConsistencyLevel());
		ResultSet rs = getSession().execute(stmt);
		Iterator<Row> iter = rs.iterator();
		if (iter.hasNext()) {
			Row row = rs.one();
			date = CassandraQueryBuilder.getDate(row, FIRST);
		}
		return date;
	}

	protected Date getFirstRollupDate(String cf, String key, Date ts) {
		Date date = null;
		Statement<?> stmt = prepareSelectFirstRollup(cf, key, ts, client.readConsistencyLevel());
		ResultSet rs = client.executeWithRetry(stmt);
		Iterator<Row> iter = rs.iterator();
		if (iter.hasNext()) {
			Row row = rs.one();
			date = CassandraQueryBuilder.getDate(row, TS);
		}
		return date;
	}

	protected long countRecords(List<String> keys, Date from, Date to) {
		Range<Date> begin = currentMonth(from);
		Range<Date> end = currentMonth(to);
		Range<Date> interval = Range.closedOpen(begin.lowerEndpoint(), end.upperEndpoint());
		long records = 0;
		Statement<?> stmt = prepareSelectRollupRecords(Interpolation.M1.getColumnFamily(), keys, interval,
				client.readConsistencyLevel());
		ResultSet rs = client.executeWithRetry(stmt);
		Iterator<Row> iter = rs.iterator();
		while (iter.hasNext()) {
			Row row = iter.next();
			records = records + row.getLong(RECORDS);
		}
		return records;
	}
	
	public List<MeasureAggregation> rollUpData(String key, Interpolation interpolation, Range<Date> interval) {
		List<MeasureAggregation> measures = new ArrayList<MeasureAggregation>();
		try {
			Statement<?> stmt = prepareSelectRollupData(interpolation.getColumnFamily(), key, interval, READ_CONSISTENCY_LEVEL);
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
				MeasureAggregation item = fillMeasureAggregation(row);
				measures.add(item);
			}
		} catch (Throwable re) {
			logger.error("unrecoverable error retrieving rollup " + key, re);
		}
		return measures;
	}
	
	public void updateAggregationsBatch(List<MeasureAggregation> items, Interpolation interpolation)  {
		BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED)
				.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
		int ttl = 0;
		if (client.getMinimalInterpolation().greaterThan(interpolation)) {
			ttl = client.getMeasuresTTL();
		}
		for (MeasureAggregation aggregation : items) {
			BatchableStatement<?> stmt = (BatchableStatement<?>) prepareUpdateRollup(aggregation, interpolation, ttl,
					ConsistencyLevel.LOCAL_ONE);
			batch = batch.add(stmt);
			if (batch.size() > AGGREGATION_UPDATE_BATCH_SIZE) {
				client.executeWithRetry(batch);
				batch = BatchStatement.newInstance(BatchType.UNLOGGED)
						.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
			}
		}
		if (batch.size() > 0) {
			client.executeWithRetry(batch);
		}
		
	}
	
	public List<MeasureRaw> fetchRollup(List<FeedKey> feeds, Date from, Date to, Interpolation interpolation,
			boolean aggregation) {
		List<String> keys = new ArrayList<String>();
		for (FeedKey feed : feeds) {
			keys.add(feed.getKey());
		}
		List<MeasureRaw> items = new ArrayList<MeasureRaw>();
		String[] fields = new String[] { KEY, TS, VAL, ERR };
		if (!aggregation) {
			fields = new String[] {};
		}
		int fetchSize = PREFETCH_LIMIT / ((feeds.size() / 50) + 1);
		Statement<?> stmt = prepareSelectRollupData(fields, keys, from, to, interpolation, READ_CONSISTENCY_LEVEL)
				.setPageSize(fetchSize)
				.setConsistencyLevel(client.readConsistencyLevel());
		try {
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
				if (aggregation) {
					String key = row.getString(KEY);
					Date dt = CassandraQueryBuilder.getDate(row, TS);
					float value = row.getFloat(VAL);
					String error = row.getString(ERR);
					MeasureRaw item = new MeasureRaw(dt, value, error);
					item.setKey(key);
					items.add(item);
				} else {
					MeasureAggregation item = fillMeasureAggregation(row);
					items.addAll(InterpolationUtils.deaggregate(item));
				}
			}

		} catch (DriverException e) {
			logger.error("cassandra not available, unable to retrieve measure from " + keys, e);
		}
		Range<Date> interval = Range.closedOpen(from, to);
		logger.debug("fetchRollup keys:{}, interval:{}, found:{}", feeds.size(), interval, items.size());
		return items;
	}

	protected boolean countAgain(Date updated) {
		if (updated == null) return true;
		long elapsed = (System.currentTimeMillis() - updated.getTime()) / 1000;
		return elapsed > (86400 / 6);
	}

	protected long countRecords(String key, Date from, Date to) {
		Range<Date> begin = currentMonth(from);
		Range<Date> end = currentMonth(to);
		Range<Date> interval = Range.closedOpen(begin.lowerEndpoint(), end.upperEndpoint());
		long records = 0;
		MeasureAggregation measure = calculateAggregation(key, -1, interval, Interpolation.M1.getColumnFamily());
		if (measure != null) {
			records = measure.getRecords();
		}
			logger.debug("count Records {} [{} - {}) records: {}", key, sdf.format(interval.lowerEndpoint()),
					sdf.format(interval.upperEndpoint()), records);

		return records;
	}

}
