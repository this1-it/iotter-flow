package it.thisone.iotter.cassandra;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import it.thisone.iotter.cassandra.model.ExportQuery;
import it.thisone.iotter.cassandra.model.Feed;
import it.thisone.iotter.cassandra.model.MeasureRaw;

public abstract class MeasuresQueryBuilder extends CassandraQueryBuilder {

	protected static final String[] RAW_FIELDS = new String[] { TS, TR, VAL, ERR };

	/*
	 * this query does not allow distinct results since ts is not a partition
	 * key
	 */
	public static Statement<?> prepareSelectMeasureTicks(List<String> keys, Date from, Date to,
			ConsistencyLevel consistency) {
		String query = String.format("SELECT %s FROM %s.%s WHERE %s IN (%s) AND %s >= ? AND %s <= ?", TS,
				CassandraClient.getKeySpace(), MEASURE_TICKS_CF, KEY, placeholders(keys.size()), TS, TS);
		List<Object> values = new ArrayList<Object>(keys);
		values.add(from);
		values.add(to);
		return simpleStatement(query, consistency, values.toArray());
	}

	public static Statement<?> prepareSelectMeasureTicks(String key, Date from, Date to, ConsistencyLevel consistency) {
		String query = String.format("SELECT %s FROM %s.%s WHERE %s = ? AND %s >= ? AND %s <= ?", TS,
				CassandraClient.getKeySpace(), MEASURE_TICKS_CF, KEY, TS, TS);
		return simpleStatement(query, consistency, key, from, to);
	}

	public static Statement<?> prepareSelectFirstTick(String sn, Date date, ConsistencyLevel consistency) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("SELECT * FROM ").append(CassandraClient.getKeySpace()).append('.').append(MEASURE_TICKS_CF)
				.append(" WHERE ").append(KEY).append(" = ?");
		values.add(sn);
		if (date != null) {
			query.append(" AND ").append(TS).append(" >= ?");
			values.add(date);
		}
		query.append(" ORDER BY ").append(TS).append(" ASC LIMIT 1");
		return simpleStatement(query.toString(), consistency, values.toArray());
	}

	public static Statement<?> prepareSelectLastTick(String sn, Date date, ConsistencyLevel consistency) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("SELECT * FROM ").append(CassandraClient.getKeySpace()).append('.').append(MEASURE_TICKS_CF)
				.append(" WHERE ").append(KEY).append(" = ?");
		values.add(sn);
		if (date != null) {
			query.append(" AND ").append(TS).append(" <= ?");
			values.add(date);
		}
		query.append(" ORDER BY ").append(TS).append(" DESC LIMIT 1");
		return simpleStatement(query.toString(), consistency, values.toArray());
	}

	public static Statement<?> prepareSelectValidMeasures(String key, Date date, int limit,
			ConsistencyLevel consistency) {
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ? AND %s <= ? ORDER BY %s DESC LIMIT ?",
				CassandraClient.getKeySpace(), MEASURES_CF, KEY, TS, TS);
		return simpleStatement(query, consistency, key, date, limit);
	}

	public static Statement<?> prepareSelectLastMeasure(String key, Date date, ConsistencyLevel consistency) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("SELECT * FROM ").append(CassandraClient.getKeySpace()).append('.').append(MEASURES_CF)
				.append(" WHERE ").append(KEY).append(" = ?");
		values.add(key);
		if (date != null) {
			query.append(" AND ").append(TS).append(" <= ?");
			values.add(date);
		}
		query.append(" ORDER BY ").append(TS).append(" DESC LIMIT 1");
		return simpleStatement(query.toString(), consistency, values.toArray());
	}

	public static Statement<?> prepareInsertTick(String key, Date timestamp, int ttl, ConsistencyLevel consistency) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("INSERT INTO ").append(CassandraClient.getKeySpace()).append('.').append(MEASURE_TICKS_CF)
				.append(" (").append(KEY).append(", ").append(TS).append(", ").append(TR).append(") VALUES (?, ?, ?)");
		values.add(key);
		values.add(timestamp);
		values.add(new Date());
		if (ttl > 0) {
			query.append(" USING TTL ?");
			values.add(ttl);
		}
		SimpleStatement stmt = simpleStatement(query.toString(), consistency, values.toArray());
		long microsecs = timestamp.getTime() * 1000;
		return stmt.setQueryTimestamp(microsecs);
	}

	public static Statement<?> prepareInsertMeasure(MeasureRaw measure, int ttl, ConsistencyLevel consistency) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("INSERT INTO ").append(CassandraClient.getKeySpace()).append('.').append(MEASURES_CF)
				.append(" (").append(KEY).append(", ").append(TS).append(", ").append(TR).append(", ")
				.append(VAL).append(", ").append(ERR).append(") VALUES (?, ?, ?, ?, ?)");
		values.add(measure.getKey());
		values.add(measure.getDate());
		values.add(measure.getReceived());
		values.add(measure.getValue());
		values.add(measure.getError());
		if (ttl > 0) {
			query.append(" USING TTL ?");
			values.add(ttl);
		}
		SimpleStatement stmt = simpleStatement(query.toString(), consistency, values.toArray());
		if (ttl > 0) {
			long microsecs = measure.getDate().getTime() * 1000;
			return stmt.setQueryTimestamp(microsecs);
		}
		return stmt;
	}

	public static Statement<?> prepareDeleteMeasuresInterval(String key, Date ts) {
		String query = String.format("DELETE FROM %s.%s WHERE %s = ? AND %s < ?", CassandraClient.getKeySpace(),
				MEASURES_CF, KEY, TS);
		return simpleStatement(query, DELETE_CONSISTENCY_LEVEL, key, ts);
	}

	public static MeasureRaw fillMeasure(Row row) {
		Date dt = getDate(row, TS);
		float value = row.getFloat(VAL);
		String error = row.getString(ERR);
		MeasureRaw item = new MeasureRaw(dt, value, error);
		item.setReceived(getDate(row, TR));
		return item;
	}

	public static Statement<?> prepareSelectFirstMeasure(String key, Date date, ConsistencyLevel consistency) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("SELECT * FROM ").append(CassandraClient.getKeySpace()).append('.').append(MEASURES_CF)
				.append(" WHERE ").append(KEY).append(" = ?");
		values.add(key);
		if (date != null) {
			query.append(" AND ").append(TS).append(" >= ?");
			values.add(date);
		}
		query.append(" ORDER BY ").append(TS).append(" ASC LIMIT 1");
		return simpleStatement(query.toString(), consistency, values.toArray());
	}

	public static Statement<?> findValidMeasure(String key, Date date, int limit, ConsistencyLevel consistency) {
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ? AND %s <= ? ORDER BY %s DESC LIMIT ?",
				CassandraClient.getKeySpace(), MEASURES_CF, KEY, TS, TS);
		return simpleStatement(query, consistency, key, date, limit);
	}

	public static Statement<?> prepareSelectMeasureRaw(String[] fields, List<String> keys, Date from, Date to,
			ConsistencyLevel consistency) {
		StringBuilder select = new StringBuilder();
		for (int i = 0; i < fields.length; i++) {
			if (i > 0) {
				select.append(", ");
			}
			select.append(fields[i]);
		}
		String query = String.format("SELECT %s FROM %s.%s WHERE %s IN (%s) AND %s >= ? AND %s <= ?",
				select, CassandraClient.getKeySpace(), MEASURES_CF, KEY, placeholders(keys.size()), TS, TS);
		List<Object> values = new ArrayList<Object>(keys);
		values.add(from);
		values.add(to);
		return simpleStatement(query, consistency, values.toArray());
	}

	public static Statement<?> prepareSelectMeasureRaw(String key, Range<Date> interval,
			ConsistencyLevel consistency) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("SELECT ").append(String.join(", ", RAW_FIELDS)).append(" FROM ")
				.append(CassandraClient.getKeySpace()).append('.').append(MEASURES_CF)
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

	public static Statement<?> countRawStatement(String key, Range<Date> interval, ConsistencyLevel consistency) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("SELECT COUNT(*) FROM ").append(CassandraClient.getKeySpace()).append('.').append(MEASURES_CF)
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

	static Statement<?> prepareSelectFeedValue(String sn, String key, ConsistencyLevel consistency) {
		String query = String.format("SELECT %s, %s, %s FROM %s.%s WHERE %s = ? AND %s = ?", VAL, AVAL, TS,
				CassandraClient.getKeySpace(), FEEDS_CF, SN, KEY);
		return simpleStatement(query, consistency, sn, key);
	}

	static Statement<?> prepareUpdateFeedAggregation(Feed item, ConsistencyLevel consistency) {
		String query = String.format("UPDATE %s.%s SET %s = ? WHERE %s = ? AND %s = ? IF EXISTS",
				CassandraClient.getKeySpace(), FEEDS_CF, AVAL, SN, KEY);
		return simpleStatement(query, consistency, item.getAggregation(), item.getSerial(), item.getKey());
	}

	public static Statement<?> prepareUpdateExportQuery(ExportQuery query, int ttl, ConsistencyLevel consistency) {
		StringBuilder cql = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		cql.append("UPDATE ").append(CassandraClient.getKeySpace()).append('.').append(MEASURES_EXPORT_TMP_CF);
		if (ttl > 0) {
			cql.append(" USING TTL ?");
			values.add(ttl);
		}
		cql.append(" SET ").append(START).append(" = ?, ").append(TOTAL).append(" = ?, ")
				.append(ASCENDING).append(" = ?, ").append(INTERPOLATION).append(" = ?, ").append(LOWER)
				.append(" = ?, ").append(UPPER).append(" = ?, ").append(SN).append(" = ?, ").append(EXPIRY)
				.append(" = ?, ").append(BATCH_SIZE).append(" = ?, ").append(KEYS).append(" = ")
				.append(KEYS).append(" + ?");
		values.add(query.getStart());
		values.add(query.getTotal());
		values.add(query.isAscending());
		values.add(query.getInterpolation());
		values.add(query.getFrom());
		values.add(query.getTo());
		values.add(query.getSerial());
		values.add(query.getExpires());
		values.add(query.getBatchSize());
		values.add(query.getKeys());
		cql.append(" WHERE ").append(QID).append(" = ?");
		values.add(query.getQid());
		return simpleStatement(cql.toString(), consistency, values.toArray());
	}

	public static Statement<?> prepareUpdateExportQuery(String qid, Long start, ConsistencyLevel consistency) {
		String query = String.format("UPDATE %s.%s SET %s = ? WHERE %s = ? IF EXISTS",
				CassandraClient.getKeySpace(), MEASURES_EXPORT_TMP_CF, START, QID);
		return simpleStatement(query, consistency, start, qid);
	}

	public static Statement<?> prepareSelectExportQuery(String qid, ConsistencyLevel consistency) {
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ?", CassandraClient.getKeySpace(),
				MEASURES_EXPORT_TMP_CF, QID);
		return simpleStatement(query, consistency, qid);
	}

	public static ExportQuery fillExportQuery(Row row) {
		ExportQuery item = new ExportQuery(row.getString(QID));
		item.setAscending(row.getBool(ASCENDING));
		item.setInterpolation(row.getString(INTERPOLATION));
		item.setSerial(row.getString(SN));
		item.setStart(row.getLong(START));
		item.setTotal(row.getLong(TOTAL));
		item.setBatchSize(row.getInt(BATCH_SIZE));
		item.setFrom(getDate(row, LOWER));
		item.setTo(getDate(row, UPPER));
		item.setExpires(getDate(row, EXPIRY));
		item.setKeys(row.getList(KEYS, String.class));
		return item;
	}

	public static Statement<?> prepareSelectLastRollupValue(String cf, String key, Date date,
			ConsistencyLevel consistency) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("SELECT * FROM ").append(CassandraClient.getKeySpace()).append('.').append(cf)
				.append(" WHERE ").append(KEY).append(" = ?");
		values.add(key);
		if (date != null) {
			query.append(" AND ").append(TS).append(" <= ?");
			values.add(date);
		}
		query.append(" ORDER BY ").append(TS).append(" DESC LIMIT 1");
		return simpleStatement(query.toString(), consistency, values.toArray());
	}

}
