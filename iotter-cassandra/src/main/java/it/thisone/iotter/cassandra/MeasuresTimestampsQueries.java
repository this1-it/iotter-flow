package it.thisone.iotter.cassandra;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.DriverException;
import com.google.common.collect.Range;

import it.thisone.iotter.cassandra.model.ExportTimestamps;
import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.config.Constants;

// /rest/v1/device/151uyq1801R2-3-61512/data?from=1592695092&to=1592716698&asc=true&interpolation=RAW

@Service
public class MeasuresTimestampsQueries implements CassandraConstants {

	private static Logger logger = LoggerFactory.getLogger(Constants.Exporter.LOG4J_CATEGORY);

	public static final int PREFETCH_LIMIT = 1000;

	@Autowired
	private RollupQueries rollupQueries;

	@Autowired
	private CassandraClient client;

	@Resource
	private MeasuresTimestampsQueries self;

	/*
	 * Feature #197 increase export performances
	 */
	public List<Date> writeTimeStamps(String qid, List<FeedKey> feeds, Date from, Date to, Interpolation interpolation,
			boolean ascending, Set<Range<Date>> validities) {
		List<String> keys = new ArrayList<String>();
		List<Date> dates = new ArrayList<>();
		Map<String, String> map = new HashMap<>();
		for (FeedKey feed : feeds) {
			map.put(feed.getSerial(), feed.getKey());
		}
		try {
			if (interpolation.equals(Interpolation.RAW)) {
				keys = new ArrayList<String>(map.keySet());
				dates = fetchRawUniqueTimestamps(qid, keys, from, to);
			} else {
				keys = new ArrayList<String>(map.values());
				dates = fetchRollupUniqueTimestamps(qid, keys, from, to, interpolation);
			}
			if (!ascending) {
				Collections.reverse(dates);
			}
			writeExportTimestamps(qid, dates);

		} catch (DriverException e) {
			logger.error("cassandra not available, unable to write timestamps from " + keys, e);
		}

		logger.debug("retrieved timestamp for qid {} found {} ", qid, dates.size());
		return dates;
	}

	@CachePut(value = Constants.Cache.TIMESTAMPS, key = "{#qid}", unless = "#result == null")
	public ExportTimestamps writeExportTimestamps(String qid, List<Date> dates) {
		// https://stackoverflow.com/questions/29927238/java-efficiently-converting-an-array-of-longs-to-an-array-of-bytes
		long[] timestamps = new long[dates.size()];
		for (int i = 0; i < timestamps.length; i++) {
			timestamps[i] = dates.get(i).getTime();
		}
		ByteBuffer bytes = ByteBuffer.allocate(timestamps.length * Long.BYTES);
		bytes.order(ByteOrder.nativeOrder()).asLongBuffer().put(timestamps);

		// https://stackoverflow.com/questions/25197685/how-can-i-store-objects-in-cassandra-using-the-blob-datatype

		String query = "INSERT INTO %s (qid, timestamps) VALUES (?,?)";
		String cf = client.getCFName(CassandraConstants.MEASURES_EXPORT_TS_CF);
		query = String.format(query, cf);
		BoundStatement stmt = client.prepare(query, client.writeConsistencyLevel()).bind(qid, bytes);
		client.executeWithRetry(stmt);
		ExportTimestamps wrapper = new ExportTimestamps();
		wrapper.setQid(qid);
		wrapper.setTimestamps(dates);
		return wrapper;
	}

	@Cacheable(value = Constants.Cache.TIMESTAMPS, key = "{#qid}", unless = "#result == null")
	public ExportTimestamps fetchExportTimestamps(String qid) {
		logger.debug("fetchExportTimestamps for qid", qid);

		ExportTimestamps wrapper = new ExportTimestamps();
		wrapper.setQid(qid);
		wrapper.setTimestamps(new ArrayList<>());
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ?", CassandraClient.getKeySpace(),
				MEASURES_EXPORT_TS_CF, QID);
		SimpleStatement stmt = CassandraQueryBuilder.simpleStatement(query, null, qid);
		ResultSet rs = client.executeWithRetry(stmt);
		Iterator<Row> iter = rs.iterator();
		if (iter.hasNext()) {
			Row row = rs.one();
			ByteBuffer bb = row.getByteBuffer("timestamps");
			long[] timestamps = new long[bb.remaining() / Long.BYTES];
			bb.order(ByteOrder.nativeOrder()).asLongBuffer().get(timestamps);
			for (int i = 0; i < timestamps.length; i++) {
				Date date = new Date(timestamps[i]);
				wrapper.getTimestamps().add(date);
			}
		}
		return wrapper;
	}

	public Date getTimeStamp(String qid, int id) {
		ExportTimestamps wrapper = self.fetchExportTimestamps(qid);
		return wrapper.get(id);
	}

	public List<Date> getTimeStamps(String qid, int start, int end) {
		ExportTimestamps wrapper = self.fetchExportTimestamps(qid);
		return wrapper.subList(start, end);
	}

	private List<Date> fetchRollupUniqueTimestamps(String uid, List<String> keys, Date from, Date to,
			Interpolation interpolation) {
		List<Date> found = new ArrayList<>();
		Date first = null;
		Range<Date> range = null;
		for (String key : keys) {
			first = rollupQueries.getFirstRollupDate(interpolation.getColumnFamily(), key, from);
			if (first == null) {
				range = InterpolationUtils.currentPeriod(from, interpolation);
				first = range.lowerEndpoint();
			}
		}
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.setTime(first);
		while (calendar.getTime().before(to)) {
			found.add(calendar.getTime());
			calendar.add(Calendar.SECOND, interpolation.getSeconds());
		}
		return found;
	}

	private List<Date> fetchRawUniqueTimestamps(String uid, List<String> keys, Date from, Date to) {
		Set<Date> found = new HashSet<Date>();
		Statement<?> stmt = MeasuresQueryBuilder.prepareSelectMeasureTicks(keys, from, to,
				client.readConsistencyLevel());
		stmt = stmt.setPageSize(PREFETCH_LIMIT);
		ResultSet rs = client.executeWithRetry(stmt);
		Iterator<Row> iter = rs.iterator();
		while (iter.hasNext()) {
			Row row = iter.next();
			Date ts = CassandraQueryBuilder.getDate(row, CassandraConstants.TS);
			found.add(ts);
		}
		List<Date> list = new ArrayList<>();
		list.addAll(found);
		Collections.sort(list);
		return list;
	}

}
