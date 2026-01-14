package it.thisone.iotter.cassandra;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.DriverException;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.Range;

import it.thisone.iotter.cassandra.model.CassandraExportContext;
import it.thisone.iotter.cassandra.model.CassandraExportFeed;
import it.thisone.iotter.cassandra.util.CassandraExportUtil;
import it.thisone.iotter.exporter.CDataPoint;
import it.thisone.iotter.exporter.CDataPointUtil;
import it.thisone.iotter.exporter.RecordPrinter;

// Feature #2162
@Service
public class MeasuresSetQueries implements CassandraConstants {
//	 private static Logger logger =
//	 LoggerFactory.getLogger(Constants.Exporter.LOG4J_CATEGORY);
	private static Logger logger = LoggerFactory.getLogger(MeasuresSetQueries.class);

	protected static final String DATE_FORMAT = "dd/MM/yy HH:mm:ss.SSS ZZZ";

	private static final int PREFETCH_LIMIT = 500;

	@Autowired
	private CassandraClient client;

	@Autowired
	@Qualifier("cborMapper")
	public ObjectMapper mapper;

	@Autowired
	@Qualifier("dataPointListObjectReader")
	public ObjectReader reader;

	public void delete(String sn) {

		try {
			String query = String.format("DELETE FROM %s.%s WHERE %s = ?", CassandraClient.getKeySpace(),
					MEASURES_SET_CF, SN);
			client.executeWithRetry(CassandraQueryBuilder.simpleStatement(query, null, sn));
		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to delete for sn " + sn, e);
		}
	}

	public void insert(String serial, Date timestamp, List<CDataPoint> values) {

		try {
			byte[] value = mapper.writeValueAsBytes(values);
			ByteBuffer blobBuffer = ByteBuffer.wrap(value);

			String query = String.format("INSERT INTO %s.%s (%s, %s, %s) VALUES (?, ?, ?)",
					CassandraClient.getKeySpace(), MEASURES_SET_CF, SN, TS, VAL);
			SimpleStatement stmt = CassandraQueryBuilder.simpleStatement(query, ConsistencyLevel.LOCAL_QUORUM, serial,
					timestamp, blobBuffer);
			client.executeWithRetry(stmt);
		} catch (DriverException | JsonProcessingException e) {
			logger.error("unrecoverable error, unable to insert MeasuresSet for id " + serial, e);
		}
	}

	public List<CDataPoint> getLastMeasuresSet(String serial, Date timestamp) {
		List<CDataPoint> dataPoints = new ArrayList<>();
		try {
			String query = String.format("SELECT %s FROM %s.%s WHERE %s = ? AND %s <= ? LIMIT 1", VAL,
					CassandraClient.getKeySpace(), MEASURES_SET_CF, SN, TS);
			SimpleStatement stmt = CassandraQueryBuilder.simpleStatement(query, ConsistencyLevel.LOCAL_QUORUM, serial,
					timestamp);
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
					ByteBuffer blobBuffer = row.getByteBuffer(VAL);
					byte[] blobBytes = new byte[blobBuffer.remaining()];
					blobBuffer.get(blobBytes);
					dataPoints = reader.readValue(blobBytes);
			}
		} catch (Throwable e) {
			logger.error("unrecoverable error, to get MeasuresSet for id " + serial, e);
		}

		if (dataPoints.isEmpty()) {
			logger.error("dataPoints isEmpty {} ", serial);
		}

		return dataPoints;

	}

	public Range<Date> getMeasuresSetRange(String serial) {
		Date lower = lowerBoundTimestamp(serial);
		Date upper = upperBoundTimestamp(serial);

		if (lower != null && upper != null) {
			if (lower.equals(upper)) {
				return Range.singleton(lower);
			} else {
				return Range.open(lower, upper);
			}

		}

		return null;
	}

	private Date lowerBoundTimestamp(String serial) {
		String query = String.format("SELECT %s FROM %s.%s WHERE %s = ? ORDER BY %s ASC LIMIT 1", TS,
				CassandraClient.getKeySpace(), MEASURES_SET_CF, SN, TS);
		SimpleStatement stmt = CassandraQueryBuilder.simpleStatement(query, ConsistencyLevel.LOCAL_ONE, serial);
		ResultSet rs = client.executeWithRetry(stmt);
		Iterator<Row> iter = rs.iterator();
		Date timestamp = null;
		while (iter.hasNext()) {
			Row row = iter.next();
			timestamp = CassandraQueryBuilder.getDate(row, TS);
		}
		return timestamp;
	}

	private Date upperBoundTimestamp(String serial) {
		String query = String.format("SELECT %s FROM %s.%s WHERE %s = ? ORDER BY %s DESC LIMIT 1", TS,
				CassandraClient.getKeySpace(), MEASURES_SET_CF, SN, TS);
		SimpleStatement stmt = CassandraQueryBuilder.simpleStatement(query, ConsistencyLevel.LOCAL_ONE, serial);
		ResultSet rs = client.executeWithRetry(stmt);
		Iterator<Row> iter = rs.iterator();
		Date timestamp = null;
		while (iter.hasNext()) {
			Row row = iter.next();
			timestamp = CassandraQueryBuilder.getDate(row, TS);
		}
		return timestamp;
	}

	public void export(CassandraExportContext ctx, RecordPrinter printer) {
		String serial = ctx.getSerial();
		Range<Date> interval = ctx.getInterval();

		try {
			String query = String.format("SELECT %s, %s FROM %s.%s WHERE %s = ? AND %s >= ? AND %s <= ?",
					TS, VAL, CassandraClient.getKeySpace(), MEASURES_SET_CF, SN, TS, TS);
			if (ctx.isAscending()) {
				query += String.format(" ORDER BY %s ASC", TS);
			} else {
				query += String.format(" ORDER BY %s DESC", TS);
			}

			SimpleStatement stmt = SimpleStatement.builder(query)
					.addPositionalValues(serial, interval.lowerEndpoint(), interval.upperEndpoint())
					.setPageSize(PREFETCH_LIMIT)
					.setConsistencyLevel(ConsistencyLevel.LOCAL_ONE)
					.build();

			ResultSet rs = client.executeWithRetry(stmt);
			logger.info("Started fetching Series {} for range {}", serial, interval);

			int totalCount = 0;
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
				try {
					Date timestamp = CassandraQueryBuilder.getDate(row, TS);
					ByteBuffer buffer = row.getByteBuffer(VAL);
					byte[] blobBytes = new byte[buffer.remaining()];
					buffer.get(blobBytes);
					List<CDataPoint> dataPoints = reader.readValue(blobBytes);
					Object[] record = createRecord(ctx, timestamp, dataPoints);
					printer.printRecord(record);
					totalCount += dataPoints.size();
				} catch (IOException e) {
					logger.error("unrecoverable error, exporting csv for " + serial, e);
				}
			}
			printer.close();
			logger.info("Finished fetching {} datapoints for {}", totalCount, serial);
		} catch (DriverException | IOException e) {
			logger.error("Cassandra not available, unable to retrieve measures from " + serial, e);
		}
	}

	public Object[] createRecord(CassandraExportContext ctx, Date timestamp, List<CDataPoint> dataPoints) {
		List<String> addresses = ctx.getAddresses();
		Map<String, CassandraExportFeed> feeds = ctx.getFeeds();
		DecimalFormat nf = ctx.getNumberFormat();
		DateFormat df = ctx.getDateFormat();
		Object[] record = new Object[addresses.size() + 1];
		if (ctx.isTyped()) {
			record[0] = timestamp;
		} else {
			record[0] = df.format(timestamp);
		}
		Float[] values = CDataPointUtil.extractValues(dataPoints, addresses);
		for (int i = 0; i < values.length; i++) {
			String address = addresses.get(i);
			record[i + 1] = CassandraExportUtil.convertFormat(values[i], feeds.get(address), nf, ctx.isTyped());
		}
		return record;
	}

}
