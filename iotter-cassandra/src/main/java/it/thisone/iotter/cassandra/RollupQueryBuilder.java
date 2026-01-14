package it.thisone.iotter.cassandra;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.cassandra.model.MeasureAggregation;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.cassandra.model.MeasureStats;
import it.thisone.iotter.cassandra.model.QueueRollup;
import it.thisone.iotter.cassandra.model.SummaryErrors;

public abstract class RollupQueryBuilder extends CassandraQueryBuilder {
	public static final String[] ROLLUP_FIELDS = new String[] { COUNT, RECORDS };

	public static final String UTC_TZ = "UTC";

	public static Calendar getCalendar() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		return calendar;
	}

	public static Statement<?> prepareSelectRollupStats(String sn, String key, ConsistencyLevel consistency) {
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ? AND %s = ?", CassandraClient.getKeySpace(),
				MEASURE_STATS_CF, SN, KEY);
		return simpleStatement(query, consistency, sn, key);
	}

	public static Statement<?> prepareSelectAllRollupStats(String sn, ConsistencyLevel consistency) {
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ?", CassandraClient.getKeySpace(),
				MEASURE_STATS_CF, SN);
		return simpleStatement(query, consistency, sn);
	}

	public static Statement<?> prepareInsertRollupStats(String sn, String key, boolean running, int qualifier,
			ConsistencyLevel consistency) {
		String query = String.format(
				"INSERT INTO %s.%s (%s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?)",
				CassandraClient.getKeySpace(), MEASURE_STATS_CF, QUAL, SN, KEY, CREATED, UPDATED, RUNNING);
		return simpleStatement(query, consistency, qualifier, sn, key, new Date(), new Date(), running);
	}

	public static Statement<?> prepareUpdateRollupStats(MeasureStats stats, ConsistencyLevel consistency) {
		String query = String.format(
				"UPDATE %s.%s SET %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ? WHERE %s = ? AND %s = ?",
				CassandraClient.getKeySpace(), MEASURE_STATS_CF, LBL, OWN, RECORDS, SINCE, CREATED, UPDATED, LAST,
				FIRST, FREQ, RUNNING, SN, KEY);
		return simpleStatement(query, consistency, stats.getLabel(), stats.getOwner(), stats.getRecords(),
				stats.getSince(), stats.getCreated(), stats.getUpdated(), stats.getLastMeasureDate(),
				stats.getFirstMeasureDate(), stats.getFrequency(), stats.isRunning(), stats.getSerial(),
				stats.getKey());
	}

	public static Statement<?> prepareUpdateStatsRunning(String sn, String key, ConsistencyLevel consistency) {
		String query = String.format("UPDATE %s.%s SET %s = ? WHERE %s = ? AND %s = ?",
				CassandraClient.getKeySpace(), MEASURE_STATS_CF, RUNNING, SN, KEY);
		return simpleStatement(query, consistency, true, sn, key);
	}

	public static Statement<?> prepareDeleteAllRollupStats(String sn) {
		String query = String.format("DELETE FROM %s.%s WHERE %s = ?", CassandraClient.getKeySpace(),
				MEASURE_STATS_CF, SN);
		return simpleStatement(query, DELETE_CONSISTENCY_LEVEL, sn);
	}

	public static Statement<?> prepareDeleteRollupStats(String sn, String key) {
		String query = String.format("DELETE FROM %s.%s WHERE %s = ? AND %s = ?", CassandraClient.getKeySpace(),
				MEASURE_STATS_CF, SN, KEY);
		return simpleStatement(query, DELETE_CONSISTENCY_LEVEL, sn, key);
	}

	public static Statement<?> prepareCountRollupStats(String serial, ConsistencyLevel consistency) {
		String query = String.format("SELECT %s FROM %s.%s WHERE %s = ?", RECORDS, CassandraClient.getKeySpace(),
				MEASURE_STATS_CF, SN);
		return simpleStatement(query, consistency, serial);
	}

	public static MeasureAggregation fillMeasureAggregation(Row row) {
		MeasureAggregation item = new MeasureAggregation();
		item.setKey(row.getString(KEY));
		item.setDate(getDate(row, TS));
		return fillRollupAggregation(row, item);
	}

	/**
	 * 
	 * @param row
	 * @param item has been initialize with interval
	 * @return
	 */
	public static MeasureAggregation fillRollupAggregation(Row row, MeasureAggregation item) {

		item.setValue(row.getFloat(VAL));
		item.setCount(row.getInt(COUNT));
		item.setError(row.getString(ERR));
		item.setRecords(row.getLong(RECORDS));
		item.setErrorDate(getDate(row, ERRTS));
		item.setMaxDate(getDate(row, MAXTS));
		item.setMaxValue(row.getFloat(MAXVAL));
		item.setMinDate(getDate(row, MINTS));
		item.setMinValue(row.getFloat(MINVAL));

		if (item.getValue().equals(Float.NaN)) {
			item.setValue(null);
		}
		if (item.getMaxValue().equals(Float.NaN)) {
			item.setMaxValue(null);
		}
		if (item.getMinValue().equals(Float.NaN)) {
			item.setMinValue(null);
		}
		return item;
	}

	public static MeasureStats fillMeasureStats(Row row) {
		MeasureStats item = new MeasureStats(row.getString(SN), row.getString(KEY));
		item.setQualifier(row.getInt(QUAL));
		item.setLabel(row.getString(LBL));
		item.setRecords(row.getLong(RECORDS));
		item.setSince(getDate(row, SINCE));
		item.setCreated(getDate(row, CREATED));
		item.setUpdated(getDate(row, UPDATED));
		item.setFirstMeasureDate(getDate(row, FIRST));
		item.setLastMeasureDate(getDate(row, LAST));
		item.setFrequency(row.getFloat(FREQ));
		item.setRunning(row.getBool(RUNNING));
		item.setOwner(row.getString(OWN));
		return item;
	}

	public static Statement<?> prepareSelectRollupTimestamps(List<String> keys, Date from, Date to,
			Interpolation interpolation, ConsistencyLevel consistency) {
		String query = String.format("SELECT %s FROM %s.%s WHERE %s IN (%s) AND %s >= ? AND %s <= ?", TS,
				CassandraClient.getKeySpace(), interpolation.getColumnFamily(), KEY, placeholders(keys.size()), TS, TS);
		List<Object> values = new ArrayList<Object>(keys);
		values.add(from);
		values.add(to);
		return simpleStatement(query, consistency, values.toArray());
	}

	public static Statement<?> prepareSelectRollupData(String[] fields, List<String> keys, Date from, Date to,
			Interpolation interpolation, ConsistencyLevel consistency) {
		String select = "*";
		if (fields.length > 0) {
			select = String.join(", ", fields);
		}
		String query = String.format("SELECT %s FROM %s.%s WHERE %s IN (%s) AND %s >= ? AND %s <= ?", select,
				CassandraClient.getKeySpace(), interpolation.getColumnFamily(), KEY, placeholders(keys.size()), TS, TS);
		List<Object> values = new ArrayList<Object>(keys);
		values.add(from);
		values.add(to);
		return simpleStatement(query, consistency, values.toArray());
	}

	public static Statement<?> prepareSelectRollup(MeasureAggregation measure, String[] fields,
			Interpolation interpolation, ConsistencyLevel consistency) {
		String select = String.join(", ", fields);
		String query = String.format("SELECT %s FROM %s.%s WHERE %s = ? AND %s = ?", select,
				CassandraClient.getKeySpace(), interpolation.getColumnFamily(), KEY, TS);
		return simpleStatement(query, consistency, measure.getKey(), measure.getDate());
	}

	public static Statement<?> prepareUpdateRollup(MeasureAggregation measure, Interpolation interpolation, int ttl,
			ConsistencyLevel consistency) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("UPDATE ").append(CassandraClient.getKeySpace()).append('.').append(interpolation.getColumnFamily());
		if (ttl > 0) {
			query.append(" USING TTL ?");
			values.add(ttl);
		}
		query.append(" SET ");
		StringBuilder setClause = new StringBuilder();
		appendAssignment(setClause, VAL, measure.getValue(), values);
		appendAssignment(setClause, MINVAL, measure.getMinValue(), values);
		appendAssignment(setClause, MAXVAL, measure.getMaxValue(), values);
		appendAssignment(setClause, MINTS, measure.getMinDate(), values);
		appendAssignment(setClause, MAXTS, measure.getMaxDate(), values);
		appendAssignment(setClause, ERRTS, measure.getErrorDate(), values);
		appendAssignment(setClause, ERR, measure.getError(), values);
		appendAssignment(setClause, COUNT, measure.getCount(), values);
		appendAssignment(setClause, RECORDS, measure.getRecords(), values);
		query.append(setClause);
		query.append(" WHERE ").append(KEY).append(" = ? AND ").append(TS).append(" = ?");
		values.add(measure.getKey());
		values.add(measure.getDate());
		return simpleStatement(query.toString(), consistency, values.toArray());
	}

	private static void appendAssignment(StringBuilder setClause, String column, Object value, List<Object> values) {
		if (setClause.length() > 0) {
			setClause.append(", ");
		}
		setClause.append(column).append(" = ");
		if (value == null) {
			setClause.append("null");
		} else {
			setClause.append("?");
			values.add(value);
		}
	}

	public static Statement<?> prepareSelectRollupData(String cf, String key, Range<Date> interval,
			ConsistencyLevel consistency) {

		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("SELECT * FROM ").append(CassandraClient.getKeySpace()).append('.').append(cf)
				.append(" WHERE ").append(KEY).append(" = ?");
		values.add(key);

		if (interval.upperBoundType().equals(BoundType.OPEN)) {
			query.append(" AND ").append(TS).append(" < ?");
		} else {
			query.append(" AND ").append(TS).append(" <= ?");
		}
		values.add(interval.upperEndpoint());

		if (interval.lowerBoundType().equals(BoundType.OPEN)) {
			query.append(" AND ").append(TS).append(" > ?");
		} else {
			query.append(" AND ").append(TS).append(" >= ?");
		}
		values.add(interval.lowerEndpoint());

		return simpleStatement(query.toString(), consistency, values.toArray());
	}

	public static QueueRollup fillQueueRollup(Row row) {
		FeedKey feed = new FeedKey(row.getString(SN), row.getString(KEY));
		Range<Date> interval = Range.closed(getDate(row, FIRST), getDate(row, LAST));
		QueueRollup item = new QueueRollup(feed, interval);
		return item;
	}

	public static Statement<?> prepareSelectQueueRollup(String sn, String key, ConsistencyLevel consistency) {
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ? AND %s = ?", CassandraClient.getKeySpace(),
				ROLL_UP_QUEUE, SN, KEY);
		return simpleStatement(query, consistency, sn, key);
	}

	public static Statement<?> prepareInsertQueueRollup(QueueRollup item, int ttl, ConsistencyLevel consistency) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("INSERT INTO ").append(CassandraClient.getKeySpace()).append('.').append(ROLL_UP_QUEUE)
				.append(" (").append(SN).append(", ").append(KEY).append(", ").append(FIRST).append(", ")
				.append(LAST).append(") VALUES (?, ?, ?, ?)");
		values.add(item.getSerial());
		values.add(item.getKey());
		values.add(item.getFirst());
		values.add(item.getLast());
		if (ttl > 0) {
			query.append(" USING TTL ?");
			values.add(ttl);
		}
		return simpleStatement(query.toString(), consistency, values.toArray());
	}

	public static Statement<?> prepareDeleteQueueRollup(QueueRollup item) {
		String query = String.format("DELETE FROM %s.%s WHERE %s = ? AND %s = ? AND %s = ?",
				CassandraClient.getKeySpace(), ROLL_UP_QUEUE, SN, KEY, FIRST);
		return simpleStatement(query, DELETE_CONSISTENCY_LEVEL, item.getSerial(), item.getKey(), item.getFirst());
	}

	public static List<MeasureRaw> fillRollupMeasures(Row row, boolean aggregation) {
		List<MeasureRaw> measures = new ArrayList<MeasureRaw>();
		String key = row.getString(KEY);
		// Bug #249 (In Progress): [CASSANDRA] wrong values in rollup tables
		// clean up !!
		Float value = row.getFloat(VAL); // aggregated value !
		Float minValue = row.getFloat(MINVAL);
		Float maxValue = row.getFloat(MAXVAL);

		if (value.equals(Float.NaN)) {
			value = null;
		}
		if (minValue.equals(Float.NaN)) {
			minValue = null;
		}
		if (maxValue.equals(Float.NaN)) {
			maxValue = null;
		}

		if (aggregation) {
			MeasureRaw aggregate = new MeasureRaw(getDate(row, TS), value, null);
			aggregate.setKey(key);
			measures.add(aggregate);
		} else {
			MeasureRaw min = new MeasureRaw(getDate(row, MINTS), minValue, null);
			MeasureRaw max = new MeasureRaw(getDate(row, MAXTS), maxValue, null);

			// Bug #332 [VAADIN] chart shows different error values with
			// different time intervals
			MeasureRaw err = new MeasureRaw(getDate(row, ERRTS), null, row.getString(ERR));
			min.setKey(key);
			max.setKey(key);
			err.setKey(key);
			measures = SummaryErrors.interval(min, max, err);
		}
		return measures;
	}

	public static Statement<?> prepareSelectFirstQueueRollup(String sn, String key, ConsistencyLevel consistency) {
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ? AND %s = ? LIMIT 1",
				CassandraClient.getKeySpace(), ROLL_UP_QUEUE, SN, KEY);
		return simpleStatement(query, consistency, sn, key);
	}

	public static Statement<?> prepareSelectFirstRollup(String cf, String key, Date date,
			ConsistencyLevel consistency) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("SELECT * FROM ").append(CassandraClient.getKeySpace()).append('.').append(cf)
				.append(" WHERE ").append(KEY).append(" = ?");
		values.add(key);
		if (date != null) {
			query.append(" AND ").append(TS).append(" >= ?");
			values.add(date);
		}
		query.append(" ORDER BY ").append(TS).append(" ASC LIMIT 1");
		return simpleStatement(query.toString(), consistency, values.toArray());
	}

	public static Statement<?> prepareRollupLock(String cf, String key, int ttl, ConsistencyLevel consistency) {
		String query = String.format("INSERT INTO %s.%s (%s) VALUES (?) IF NOT EXISTS USING TTL ?",
				CassandraClient.getKeySpace(), cf, CassandraConstants.KEY);
		return simpleStatement(query, consistency, key, ttl);
	}

	public static Statement<?> prepareRollupUnlock(String cf, String key) {
		String query = String.format("DELETE FROM %s.%s WHERE %s = ?", CassandraClient.getKeySpace(), cf,
				CassandraConstants.KEY);
		return simpleStatement(query, DELETE_CONSISTENCY_LEVEL, key);
	}

	public static Statement<?> prepareSelectLock(String cf, String key, ConsistencyLevel consistency) {
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ?", CassandraClient.getKeySpace(), cf,
				CassandraConstants.KEY);
		return simpleStatement(query, consistency, key);
	}

	public static Statement<?> prepareSelectRollupRecords(String cf, List<String> keys, Range<Date> interval,
			ConsistencyLevel consistency) {
		String query = String.format("SELECT %s FROM %s.%s WHERE %s IN (%s) AND %s <= ? AND %s >= ?", RECORDS,
				CassandraClient.getKeySpace(), cf, KEY, placeholders(keys.size()), TS, TS);
		List<Object> values = new ArrayList<Object>(keys);
		values.add(interval.upperEndpoint());
		values.add(interval.lowerEndpoint());
		return simpleStatement(query, consistency, values.toArray());
	}

}
