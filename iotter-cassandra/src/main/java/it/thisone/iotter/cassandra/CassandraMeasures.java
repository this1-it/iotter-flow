package it.thisone.iotter.cassandra;

import static it.thisone.iotter.cassandra.CassandraQueryBuilder.deleteKey;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BatchableStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.google.common.collect.Range;

import it.thisone.iotter.cassandra.model.CassandraExportContext;
import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.exporter.CDataPoint;
import it.thisone.iotter.exporter.RecordPrinter;

@Service
public class CassandraMeasures implements Serializable, CassandraConstants {
	private static Logger logger = LoggerFactory.getLogger(CassandraMeasures.class);

	private static final long serialVersionUID = -963150975938168570L;
	
	@Autowired
	protected CassandraClient client;

	@Autowired
	protected MeasuresQueries measuresQueries;
	
	@Autowired
	private MeasuresSetQueries setQueries;
	
	

	

	public void insertRawBatch(List<MeasureRaw> items) throws BackendServiceException {
		if (items.isEmpty()) {
			return;
		}
		try {
			for (MeasureRaw measure : items) {
				measuresQueries.insertMeasure(measure);
			}
		} catch (Throwable t) {
			String msg = "unable to insert measures";
			logger.error(msg, t);
			throw new BackendServiceException(msg, t);
		}

	}

	public void _insertRawBatch(List<MeasureRaw> items) throws BackendServiceException {
		if (items.isEmpty()) {
			return;
		}
		if (client.getSession().getMetadata().getNodes().isEmpty()) {
			throw new BackendServiceException("No Connected Hosts");
		}
		List<Statement<?>> batch = new ArrayList<Statement<?>>();
		for (MeasureRaw measure : items) {
			Statement<?> stmt = MeasuresQueries.prepareInsertMeasure(measure, client.getMeasuresTTL(),
					client.writeConsistencyLevel());
			batch.add(stmt);
		}
		client.executeAsyncBatch(batch);

	}

	
	public void insertTickBatch(String key, List<Date> dates) throws BackendServiceException {
		if (dates.isEmpty()) {
			return;
		}
		try {
			BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED)
					.setConsistencyLevel(client.writeConsistencyLevel());
			for (Date date : dates) {
				BatchableStatement<?> stmt = (BatchableStatement<?>) client.prepareInsertMeasureTickBoundStatement(key,
						date);
				batch = batch.add(stmt);
			}
			client.executeWithRetry(batch);
		} catch (Throwable t) {
			String msg = "unable to insert ticks";
			logger.error(msg, t);
			throw new BackendServiceException(msg, t);
		}

	}

	public List<MeasureRaw> getMockData(Date from, Date to, int points) {
		List<MeasureRaw> items = new ArrayList<MeasureRaw>();
		if (to.after(new Date())) {
			to = new Date();
		}
		Random random = new Random();
		long interval = to.getTime() - from.getTime();
		// data point / sec
		float rate = 0.001f;
		// float rate = 0.1f;
		long step = (long) (1000 / rate);
		if (step > interval) {
			return items;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal.setTime(from);
		cal.add(Calendar.MILLISECOND, random.nextInt((int) step));
		while (cal.getTime().before(to)) {
			float value = random.nextFloat();
			String error = null;
			if (value > 0.9f)
				error = "err";
			items.add(new MeasureRaw(cal.getTime(), value, error));
			cal.add(Calendar.MILLISECOND, (int) step);
		}
		return measuresQueries.simpleReduce(items, points);
	}

	/**
	 * 
	 * @param key    feed id
	 * @param from
	 * @param to
	 * @param points number of pixel of x axis
	 * @return
	 */
	public List<MeasureRaw> getData(FeedKey feedKey, Range<Date> interval, int points, int step) {
		List<MeasureRaw> items = new ArrayList<MeasureRaw>();
		boolean full = (points <= 0);
		items = measuresQueries.fetchRaw(feedKey, interval, null);
		SimpleDateFormat sdf = new SimpleDateFormat(MeasuresQueries.DATE_FORMAT);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		logger.debug("Retrieved Series {} from {} to {} with size {}, points {}", feedKey.getKey(),
				sdf.format(interval.lowerEndpoint()), sdf.format(interval.upperEndpoint()), items.size(), points);

		if (items.isEmpty()) {
			return items;
		}

		// real time or categorized series
		if (full) {
			return items;
		}

		int count = items.size();
		MeasureRaw first = items.get(0);
		MeasureRaw last = items.get(count - 1);
		float ratio = (float) (last.getDate().getTime() - first.getDate().getTime())
				/ (float) (interval.upperEndpoint().getTime() - interval.lowerEndpoint().getTime());
		int availablePoints = (int) Math.ceil(points * ratio);

		if (count <= availablePoints) {
			return items;
		}

		logger.debug(
				"Reducing Series {} with first date {} last date {}, "
						+ "reduce ratio {}, size {}, available points {} ",
				feedKey.getKey(), sdf.format(first.getDate()), sdf.format(last.getDate()), ratio, count,
				availablePoints);
		return measuresQueries.stepReduce(feedKey.getKey(), items, availablePoints);
	}

	public static Date toUTCDate(long millis) {
		Calendar calendar = getCalendarUTC();
		calendar.setTimeInMillis(millis);
		return calendar.getTime();
	}

	public static Calendar getCalendarUTC() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		return calendar;
	}

	public void deleteMeasures(String key) {
		try {
			Statement stmt = deleteKey(MEASURES_CF, key);
			client.executeWithRetry(stmt);
		} catch (Exception e) {
			logger.error("unrecoverable error, unable to delete rollup for key " + key, e);
		}
	}

	public void deleteMeasureTicks(String key) {
		try {
			Statement stmt = deleteKey(MEASURE_TICKS_CF, key);
			client.executeWithRetry(stmt);
		} catch (Exception e) {
			logger.error("unrecoverable error, unable to delete rollup for key " + key, e);
		}
	}
	


	public long currentTimeMillis(String timeZoneId) {
		return currentTimeMillis(getTimeZone(timeZoneId));
	}

	public TimeZone getTimeZone(String timeZoneId) {
		if (timeZoneId == null) {
			return null;
		}
		try {
			return TimeZone.getTimeZone(timeZoneId);
		} catch (Throwable e) {
		}
		return null;
	}

	public int timeZoneOffset(TimeZone tz, long millis) {
		if (tz == null) {
			return 0;
		}
		return tz.getOffset(millis);
	}

	public long currentTimeMillis(TimeZone tz) {
		long ts = System.currentTimeMillis();
		return ts + timeZoneOffset(tz, ts);
	}

	public Date toUTCDate(long ts, TimeZone tz) {
		ts = ts - timeZoneOffset(tz, ts);
		return new Date(ts);
	}

	public Interpolation minimalInterpolation() {
		return client.getMinimalInterpolation();
	}

	public int measuresTTL() {
		return client.getMeasuresTTL();
	}

	public Date getLastTick(String key, Date date) {
		return measuresQueries.getLastTick(key, date);
	}

	public Date getEpoch() {
		return measuresQueries.getEpoch();
	}

	public Date getFirstTick(String serial, Date date) {
		return measuresQueries.getFirstTick(serial, date);
	}

	public void insertMeasure(MeasureRaw measure) {
		measuresQueries.insertMeasure(measure);
	}

	public void insertMeasureSet(String serial, Date timestamp, List<CDataPoint> values) {
		setQueries.insert(serial, timestamp, values);
	}
	
	public List<CDataPoint> getLastMeasuresSet(String serial,Date timestamp)  {
		return setQueries.getLastMeasuresSet(serial,timestamp);
	}

	public void exportMeasuresSet(CassandraExportContext ctx, RecordPrinter printer) {
		setQueries.export(ctx,  printer);
	}
	
	
	public void deleteMeasuresSet(String serial) {
		setQueries.delete(serial);
	}
	
	public Range<Date> getMeasuresSetRange(String serial) {
		return setQueries.getMeasuresSetRange(serial);
	}
	
}
