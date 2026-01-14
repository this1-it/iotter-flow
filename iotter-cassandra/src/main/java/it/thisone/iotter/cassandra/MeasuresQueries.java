package it.thisone.iotter.cassandra;

import static it.thisone.iotter.cassandra.RollupQueryBuilder.fillMeasureAggregation;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.DriverException;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.google.common.collect.Range;

import it.thisone.iotter.cassandra.model.Feed;
import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.MeasureAggregation;
import it.thisone.iotter.cassandra.model.MeasureInterval;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.cassandra.model.MeasureTick;
import it.thisone.iotter.common.SimpleQueue;
import it.thisone.iotter.config.Constants;

@Service
public class MeasuresQueries extends MeasuresQueryBuilder {
	private static Logger logger = LoggerFactory.getLogger(MeasuresQueries.class);

	protected static final String DATE_FORMAT = "dd/MM/yy HH:mm:ss.SSS ZZZ";

	private static final int MEASURES_PREFETCH_LIMIT = 250;

	private static final ConsistencyLevel READ_CONSISTENCY_LEVEL = ConsistencyLevel.LOCAL_QUORUM;

	@Resource
	private MeasuresQueries self;

	@Autowired
	protected CassandraClient client;

	public Date getEpoch() {
		return client.getEpoch();
	}


	public void insertMeasure(MeasureRaw measure) {
		Statement stmt = prepareInsertMeasure(measure, client.getMeasuresTTL(), client.writeConsistencyLevel());
		client.executeWithRetry(stmt);
	}

	public void insertTick(MeasureTick tick) {
		Statement stmt = prepareInsertTick(tick.getKey(), tick.getDate(), client.getMeasuresTTL(),
				client.writeConsistencyLevel());
		client.executeWithRetry(stmt);
	}

	/**
	 * An example how dataset can be reduced simply on the server side.
	 * 
	 * Using more datapoints than there are pixels on the devices makes usually no
	 * sense. Excess data just consumes bandwidth and client side CPU. This method
	 * reduces a large dataset with a very simple method.
	 * 
	 * @param series the series to be reduced
	 * @param pixels the pixel size for which the data should be adjusted
	 */
	protected List<MeasureRaw> simpleReduce(List<MeasureRaw> series, int points) {
		if (points <= 0)
			return series;
		if (series.isEmpty())
			return series;
		MeasureRaw first = series.get(0);
		MeasureRaw last = series.get(series.size() - 1);
		ArrayList<MeasureRaw> reduced = new ArrayList<MeasureRaw>();
		double startX = first.getDate().getTime();
		double endX = last.getDate().getTime();
		double minDistance = (endX - startX) / points;
		reduced.add(first);
		double lastPoint = first.getDate().getTime();
		for (int i = 0; i < series.size(); i++) {
			MeasureRaw item = series.get(i);
			if (item.getDate().getTime() - lastPoint > minDistance) {
				reduced.add(item);
				lastPoint = item.getDate().getTime();
			}
		}
		//logger.debug("Simple Reduce Series with original size {}, reduced size {}", series.size(), reduced.size());
		return reduced;
	}

	/*
	 * Se il numero di punti grezzi da rappresentare supera il numero di pixel a
	 * disposizione si deve procedere alla decimazione dei punti. La regola da
	 * seguire è la seguente: Dal rapporto tra punti grezzi e pixel si determina lo
	 * “step” = intervallo di punti in cui si deve eseguire la decimazione. Dato che
	 * devo prendere 2 punti per step determino uno step minimo multiplo di 2,
	 * strettamente superiore allo step trovato ?? non ha senso anche se sono
	 * dispari, trovo sempre un max e min Per ogni step si devono trovare 2 punti:
	 * il valore minimo e il valore massimo. Entrambi i punti devono essere
	 * rappresentati. ??? non è vero, nell'intervallo ci può essere sempre lo stesso
	 * valore
	 */

	protected List<MeasureRaw> stepReduce(String key, List<MeasureRaw> series, int displayPoints) {
		if (displayPoints <= 0)
			return series;

		List<MeasureRaw> reduced = new ArrayList<MeasureRaw>();
		int step = (int) ((float) series.size() * 2 / displayPoints);

		if (step == 0) {
			return series;
		}
		int fromIndex = 0;
		MeasureInterval interval = new MeasureInterval();
		while (fromIndex < series.size()) {
			int toIndex = fromIndex + step;
			if (toIndex > series.size() - 1) {
				toIndex = series.size() - 1;
			}
			List<MeasureRaw> chunk = series.subList(fromIndex, toIndex);
			interval.setValues(chunk);
			reduced.addAll(interval.getValues());
			fromIndex = toIndex + 1;
		}

//		logger.debug("Step Reduce Series {} with original size {}, reduced size {}," + " display points {}, step {}",
//				key, series.size(), reduced.size(), displayPoints, step);

		return reduced;
	}

	protected List<MeasureRaw> fetchRaw(FeedKey feedKey, Range<Date> interval, List<Date> ticks) {
		List<MeasureRaw> measures = new ArrayList<MeasureRaw>();
		List<MeasureRaw> items = new ArrayList<MeasureRaw>();
		Float value = null;
		Date firstDate = new Date();

		items = fetchMeasureRaw(feedKey, interval);
		if (!items.isEmpty()) {
			firstDate = items.get(0).getDate();
		}

		if (ticks == null) {
			long lower = interval.lowerEndpoint().getTime() / 1000;
			long upper = interval.upperEndpoint().getTime() / 1000;
			String key = String.format("%s-%d-%d", feedKey.getSerial(), lower, upper);
			TicksWrapper wrapper = self.fetchTicksCacheable(key, feedKey, interval);
			if (wrapper != null) {
				ticks = wrapper.getTicks();
			}
		}
		SimpleQueue<Date> queue = new SimpleQueue<>(ticks);
		if (!queue.isEmpty() && queue.peek().before(firstDate)) {
			value = getLastMeasureValue(feedKey, queue.peek());
			logger.debug("First Tick {} {} {}", feedKey.getKey(), queue.peek(), value);
		}
		
		for (MeasureRaw item : items) {
			while (!queue.isEmpty()) {
				if (queue.peek().after(item.getDate())) {
					break;
				} else if (queue.peek().equals(item.getDate())) {
					queue.pop();
					break;
				} else if (queue.peek().before(item.getDate())) {
					Date date = queue.pop();
					if (value != null) {
						measures.add(new MeasureRaw(feedKey.getKey(), date, value));
					}
				}
			}
			if (item.getValue() != null) {
				value = item.getValue();
			} else {
				item.setValue(value);
			}
			measures.add(item);
		}

		while (!queue.isEmpty()) {
			Date date = queue.pop();
			if (value != null) {
				measures.add(new MeasureRaw(feedKey.getKey(), date, value));
			}
		}
		return measures;
	}

	private List<MeasureRaw> fetchMeasureRaw(FeedKey feedKey, Range<Date> interval) {
		List<MeasureRaw> items = new ArrayList<MeasureRaw>();
		try {
			Statement<?> stmt = client.prepareSelectMeasureRawBoundStatement(feedKey.getKey(), interval)
					.setPageSize(MEASURES_PREFETCH_LIMIT)
					.setConsistencyLevel(client.readConsistencyLevel());
			
			ResultSet rs = client.executeWithRetry(stmt);
			logger.debug("Fetching Series {} {}", feedKey.getKey(), interval.toString());
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
				MeasureRaw item = fillMeasure(row);
				item.setKey(feedKey.getKey());
				items.add(item);
			}
		} catch (DriverException e) {
			logger.error("cassandra not available, unable to retrieve measure from " + feedKey.getKey(), e);
		}
		return items;
	}

	/*
	 * retrieve parameter value before given tick Date
	 */
	protected Float getLastMeasureValue(FeedKey feedKey, Date tick) {
		Float value = null;
		MeasureRaw measure = getLastMeasure(feedKey.getKey(), tick);
		if (measure != null && measure.getValue() != null) {
			logger.debug("Fetched Last Measure Value {} before tick {}", feedKey.getKey(), tick);
			value = measure.getValue();
		} else {
			Feed feed = getFeedValue(feedKey);
			if (feed == null) return null;
			if (!feed.hasLastValue()) return null;
			if (feed.getDate().compareTo(tick) <= 0) {
				logger.debug("Fetched Feed Value {} before tick {}", feedKey.getKey(), tick);
				value = feed.getValue();
			} else {
				// raw measure is not available at tick date and last measure date is after tick
				// there is not raw values between tick date and last measure date
				// value = getLastAggregationValue(feedKey.getKey(), tick);
				value = feed.getAggregation();
				if (value != null) {
					logger.debug("Fetched Feed Aggregation {} before tick {}", feedKey.getKey(), tick);
				} else {
					logger.debug("Missing Feed Aggregation {} before tick {}", feedKey.getKey(), tick);
				}
			}
		}
		return value;
	}

	public MeasureRaw getValidMeasure(String key, Date date) {
		if (key.startsWith("_")) {
			return new MeasureRaw(date, (float) Math.random(), null);
		}
		try {
			Statement stmt = prepareSelectValidMeasures(key, date, 5, client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
				MeasureRaw item = fillMeasure(row);
				if (!item.hasError()) {
					return item;
				}
			}
		} catch (DriverException e) {
			logger.error("cassandra not available, unable to retrieve measure from " + key, e);
		}
		return null;
	}

	public MeasureRaw getLastMeasure(String key, Date date) {
		if (key.startsWith("_")) {
			return new MeasureRaw(date, (float) Math.random(), null);
		}
		MeasureRaw item = null;
		try {
			Statement stmt = prepareSelectLastMeasure(key, date, client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = rs.one();
				item = fillMeasure(row);
				item.setReceived(CassandraQueryBuilder.getDate(row, TR));
			}
		} catch (DriverException e) {
			logger.error("cassandra not available, unable to retrieve last measure from " + key, e);
		}
		return item;
	}

	/*
	 * FirstMeasureCQL = "SELECT ts, val, err FROM " + value +
	 * " WHERE key=? AND ts >= ?  ORDER BY ts ASC LIMIT 1 ;";
	 */
	public MeasureRaw getFirstMeasure(String key, Date date) {
		MeasureRaw item = null;
		if (key.startsWith("_")) {
			return new MeasureRaw(date, (float) Math.random(), null);
		}
		try {
			Statement stmt = prepareSelectFirstMeasure(key, date, client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = rs.one();
				item = fillMeasure(row);
			}
		} catch (DriverException e) {
			logger.error("cassandra not available, unable to retrieve measure from " + key, e);
		}
		return item;
	}

	public Date getLastTick(String key, Date date) {
		Date timestamp = null;
		try {
			Statement stmt = prepareSelectLastTick(key, date, client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = rs.one();
				timestamp = CassandraQueryBuilder.getDate(row, TS);
			}
		} catch (DriverException e) {
			logger.error("cassandra not available, unable to retrieve measure from " + key, e);
		}
		return timestamp;
	}

	public Date getFirstTick(String key, Date date) {
		Date timestamp = null;
		try {
			Statement stmt = prepareSelectFirstTick(key, date, client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = rs.one();
				timestamp = CassandraQueryBuilder.getDate(row, TS);
			}
		} catch (DriverException e) {
			logger.error("cassandra not available, unable to retrieve measure from " + key, e);
		}
		return timestamp;
	}

	public void deleteMeasuresInterval(String key, Date ts) {
		try {
			Statement stmt = prepareDeleteMeasuresInterval(key, ts);
			client.executeWithRetry(stmt);
		} catch (DriverException e) {
			logger.error("cassandra not available, unable to retrieve measure from " + key, e);
		}

	}

	private Feed getFeedValue(FeedKey feedKey) {

		Feed feed = null;
		try {
			Statement stmt = prepareSelectFeedValue(feedKey.getSerial(), feedKey.getKey(),
					client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = rs.one();
				feed = new Feed(feedKey.getSerial(),feedKey.getKey());
				feed.setDate(CassandraQueryBuilder.getDate(row, TS));
				feed.setValue(row.getFloat(VAL));
				feed.setAggregation(row.getFloat(AVAL));
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving Feed value for key " + feedKey.getKey(), e);
		}
		return feed;
	}



	@Cacheable(value = Constants.Cache.TICKS, key = "{#key}", unless = "#result == null")
	public TicksWrapper fetchTicksCacheable(String key, FeedKey feedKey, Range<Date> interval) {
		List<Date> buffer = fetchTicks(feedKey, interval);
		return new TicksWrapper(buffer);
	}


	private List<Date> fetchTicks(FeedKey feedKey, Range<Date> interval) {
		Statement<?> stmt = client.prepareSelectMeasureTickBoundStatement(feedKey.getSerial(), interval)
				.setPageSize(PREFETCH_LIMIT)
				.setConsistencyLevel(READ_CONSISTENCY_LEVEL);
		List<Date> buffer = new ArrayList<Date>();
		logger.debug("Fetching Ticks {} ", stmt.toString());
		ResultSet rs = client.executeWithRetry(stmt);
		Iterator<Row> iter = rs.iterator();
		while (iter.hasNext()) {
			Row row = iter.next();
			buffer.add(CassandraQueryBuilder.getDate(row, TS));
		}
		logger.debug("Fetched {} Ticks {} {}", feedKey.getSerial(), buffer.size(), interval.toString());
		return buffer;
	}

	public Float getLastAggregationValue(String key, Date date) {
		Float value = null;
		try {
			Statement stmt = prepareSelectLastRollupValue(ROLL_UP_H1_CF, key, date, client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = rs.one();
				MeasureAggregation aggregation = fillMeasureAggregation(row);
				value = row.getFloat(VAL);
				value = aggregation.getLastValue();
			}
		} catch (DriverException e) {
			logger.error("cassandra not available, unable to retrieve last Aggregation value from " + key, e);
		}
		return value;
	}
	
	public void updateFeedAggregation(Feed feed) {
		if (feed.getAggregation() == null) return;
		try {
			Statement stmt = prepareUpdateFeedAggregation(feed, client.writeConsistencyLevel());
			client.executeWithRetry(stmt);
		} catch (DriverException e) {
			logger.error("cassandra not available, unable to update Feed Aggregation value for " + feed.getKey(), e);
		}
		
	}

	protected long countRaw(String key, Range<Date> interval) {
		Statement stmt = countRawStatement(key, interval, client.readConsistencyLevel());
		ResultSet rs = client.executeWithRetry(stmt);
		long count = rs.one().getLong(0);
		return count;
	}

	/**
	 * intentionally returning a not null measure
	 * 
	 * @param key
	 * @param date
	 * @return
	 */
	protected MeasureRaw findLastValidMeasure(String key, Date date) {
		MeasureRaw last = null;
		MeasureRaw measure = new MeasureRaw(date, 0f, null);
		int limit = 100;
		Statement stmt = findValidMeasure(key, date, limit, client.readConsistencyLevel());
		ResultSet rs = client.executeWithRetry(stmt);
		Iterator<Row> iter = rs.iterator();
		// int count = 0;
		while (iter.hasNext()) {
			Row row = iter.next();
			measure = fillMeasure(row);
			if (last == null)
				last = measure;
			if (!measure.hasError()) {
				break;
			}
			// count++;
		}

		return measure;
	}




}
