package it.thisone.iotter.cassandra;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;

import it.thisone.iotter.cassandra.model.DataSink;
import it.thisone.iotter.cassandra.model.Feed;

public class FeedsQueryBuilder extends CassandraQueryBuilder {

	private static final String[] VALUE_FIELDS = new String[] { KEY, VAL, TS };

	// https://datastax-oss.atlassian.net/browse/JAVA-672
	public static Statement<?> prepareInsertDataSink(DataSink item, ConsistencyLevel consistency) {
		String query = String.format(
				"INSERT INTO %s.%s (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				CassandraClient.getKeySpace(), DATASINKS_CF, SN, OWN, W_API_KEY, LBL, PROTOCOL, STATUS, LASTCONTACT,
				INACTIVITY, BLEVEL, ALARMED);
		return simpleStatement(query, consistency, item.getSerial(), item.getOwner(), item.getWriteApiKey(),
				item.getLabel(), item.getProtocol(), item.getStatus(), item.getLastContact(),
				item.getInactivityMinutes(), item.getBatteryLevel(), item.isAlarmed());
	}

	public static Statement<?> prepareUpdateDataSink(DataSink item, ConsistencyLevel consistency) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("UPDATE ").append(CassandraClient.getKeySpace()).append('.').append(DATASINKS_CF).append(" SET ");
		query.append(OWN).append(" = ?");
		values.add(item.getOwner());
		query.append(", ").append(W_API_KEY).append(" = ?");
		values.add(item.getWriteApiKey());
		query.append(", ").append(LBL).append(" = ?");
		values.add(item.getLabel());
		query.append(", ").append(PROTOCOL).append(" = ?");
		values.add(item.getProtocol());
		query.append(", ").append(STATUS).append(" = ?");
		values.add(item.getStatus());
		if (item.getLastContact() != null) {
			query.append(", ").append(LASTCONTACT).append(" = ?");
			values.add(item.getLastContact());
		}
		query.append(", ").append(INACTIVITY).append(" = ?");
		values.add(item.getInactivityMinutes());
		query.append(", ").append(BLEVEL).append(" = ?");
		values.add(item.getBatteryLevel());

		if (item.hasActiveAlarms() != null) {
			query.append(", ").append(ACTIVE_ALARMS).append(" = ?");
			values.add(item.hasActiveAlarms());
			query.append(", ").append(ALARMED).append(" = ?");
			values.add(true);
		} else {
			query.append(", ").append(ALARMED).append(" = ?");
			values.add(item.isAlarmed());
		}
		query.append(", ").append(TRACING).append(" = ?");
		values.add(item.isTracing());
		query.append(", ").append(PUBLISHING).append(" = ?");
		values.add(item.isPublishing());
		query.append(", ").append(CHECKSUM).append(" = ?");
		values.add(item.getCheckSum());
		query.append(", ").append(MASTER).append(" = ?");
		values.add(item.getMaster());
		query.append(" WHERE ").append(SN).append(" = ?");
		values.add(item.getSerial());
		return simpleStatement(query.toString(), consistency, values.toArray());
	}

	public static Statement<?> prepareUpdateDataSinkOnLastContact(DataSink item, ConsistencyLevel consistency) {
		String query = String.format("UPDATE %s.%s SET %s = ?, %s = ? WHERE %s = ?", CassandraClient.getKeySpace(),
				DATASINKS_CF, LASTCONTACT, BLEVEL, SN);
		return simpleStatement(query, consistency, item.getLastContact(), item.getBatteryLevel(), item.getSerial());
	}

	public static Statement<?> prepareUpdateLastContact(String serial, Date now, ConsistencyLevel consistency) {
		String query = String.format("UPDATE %s.%s SET %s = ? WHERE %s = ?", CassandraClient.getKeySpace(),
				DATASINKS_CF, LASTCONTACT, SN);
		return simpleStatement(query, consistency, now, serial);
	}

	public static Statement<?> prepareUpdateActiveAlarms(String serial, boolean activeAlarms,
			ConsistencyLevel consistency) {
		String query = String.format("UPDATE %s.%s SET %s = ?, %s = ? WHERE %s = ?", CassandraClient.getKeySpace(),
				DATASINKS_CF, ALARMED, ACTIVE_ALARMS, SN);
		return simpleStatement(query, consistency, true, activeAlarms, serial);
	}

	public static Statement<?> prepareDeleteDataSink(String id) {
		String query = String.format("DELETE FROM %s.%s WHERE %s = ?", CassandraClient.getKeySpace(), DATASINKS_CF,
				SN);
		return simpleStatement(query, DELETE_CONSISTENCY_LEVEL, id);
	}

	public static Statement<?> prepareSelectDataSink(String sn, ConsistencyLevel consistency) {
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ?", CassandraClient.getKeySpace(), DATASINKS_CF,
				SN);
		return simpleStatement(query, consistency, sn);
	}

	public static Statement<?> prepareSelectDataSinkLastRollup(String sn, ConsistencyLevel consistency) {
		String query = String.format("SELECT %s FROM %s.%s WHERE %s = ?", LASTROLLUP, CassandraClient.getKeySpace(),
				DATASINKS_CF, SN);
		return simpleStatement(query, consistency, sn);
	}

	public static Statement<?> prepareSelectDataSinkLastContact(String sn, ConsistencyLevel consistency) {
		String query = String.format("SELECT %s FROM %s.%s WHERE %s = ?", LASTCONTACT, CassandraClient.getKeySpace(),
				DATASINKS_CF, SN);
		return simpleStatement(query, consistency, sn);
	}

	public static Statement<?> prepareSelectDataSinkCheckSum(String sn, ConsistencyLevel consistency) {
		String query = String.format("SELECT %s FROM %s.%s WHERE %s = ?", CHECKSUM, CassandraClient.getKeySpace(),
				DATASINKS_CF, SN);
		return simpleStatement(query, consistency, sn);
	}

	public static Statement<?> prepareSelectDataSinkRecords(String sn, ConsistencyLevel consistency) {
		String query = String.format("SELECT %s FROM %s.%s WHERE %s = ?", RECORDS, CassandraClient.getKeySpace(),
				DATASINKS_CF, SN);
		return simpleStatement(query, consistency, sn);
	}

	public static Statement<?> prepareUpdateFeed(Feed item, ConsistencyLevel consistency) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("UPDATE ").append(CassandraClient.getKeySpace()).append('.').append(FEEDS_CF).append(" SET ");
		query.append(ALARMED).append(" = ?");
		values.add(item.isAlarmed());
		query.append(", ").append(ACTIVE).append(" = ?");
		values.add(item.isActive());
		query.append(", ").append(PID).append(" = ?");
		values.add(item.getIdentifier());
		query.append(", ").append(LBL).append(" = ?");
		values.add(item.getLabel());
		query.append(", ").append(QUAL).append(" = ?");
		values.add(item.getQualifier());
		query.append(", ").append(UNIT).append(" = ?");
		values.add(item.getUnit());
		query.append(", ").append(OFFSET).append(" = ?");
		values.add(item.getOffset());
		query.append(", ").append(SCALE).append(" = ?");
		values.add(item.getScale());
		query.append(", ").append(TTL).append(" = ?");
		values.add(item.getTtl());
		if (item.getTypeVar() != null) {
			query.append(", ").append(TYPEVAR).append(" = ?");
			values.add(item.getTypeVar());
		}
		if (item.getDate() != null) {
			query.append(", ").append(TS).append(" = ?");
			values.add(item.getDate());
		}
		if (item.getValue() != null) {
			query.append(", ").append(VAL).append(" = ?");
			values.add(item.getValue());
		}
		if (item.getAggregation() != null) {
			query.append(", ").append(AVAL).append(" = ?");
			values.add(item.getAggregation());
		}
		if (item.getError() != null) {
			query.append(", ").append(ERR).append(" = ?");
			values.add(item.getError());
		}
		if (item.getSince() != null) {
			query.append(", ").append(SINCE).append(" = ?");
			values.add(item.getSince());
		}
		if (item.getInterpolation() != null) {
			query.append(", ").append(MININTERPL).append(" = ?");
			values.add(item.getInterpolation());
		}
		query.append(" WHERE ").append(SN).append(" = ? AND ").append(KEY).append(" = ?");
		values.add(item.getSerial());
		values.add(item.getKey());
		return simpleStatement(query.toString(), consistency, values.toArray());
	}

	public static Statement<?> prepareUpdateFeedLastValue(Feed item, ConsistencyLevel consistency) {
		String query = String.format("UPDATE %s.%s SET %s = ?, %s = ?, %s = ? WHERE %s = ? AND %s = ?",
				CassandraClient.getKeySpace(), FEEDS_CF, TS, VAL, ERR, SN, KEY);
		return simpleStatement(query, consistency, item.getDate(), item.getValue(), item.getError(), item.getSerial(),
				item.getKey());
	}

	public static Statement<?> prepareUpdateFeedAlarmed(Feed item, ConsistencyLevel consistency) {
		String query = String.format("UPDATE %s.%s SET %s = ? WHERE %s = ? AND %s = ?", CassandraClient.getKeySpace(),
				FEEDS_CF, ALARMED, SN, KEY);
		return simpleStatement(query, consistency, item.isAlarmed(), item.getSerial(), item.getKey());
	}

	public static Statement<?> prepareUpdateFeedSelected(Feed item, ConsistencyLevel consistency) {
		String query = String.format(
				"UPDATE %s.%s SET %s = ? WHERE %s = ? AND %s = ? IF EXISTS",
				CassandraClient.getKeySpace(), FEEDS_CF, SELECTED, SN, KEY);
		return simpleStatement(query, consistency, item.isSelected(), item.getSerial(), item.getKey());
	}

	public static Statement<?> prepareUpdateFeedActive(Feed item, ConsistencyLevel consistency) {
		String query = String.format(
				"UPDATE %s.%s SET %s = ? WHERE %s = ? AND %s = ? IF EXISTS",
				CassandraClient.getKeySpace(), FEEDS_CF, ACTIVE, SN, KEY);
		return simpleStatement(query, consistency, item.isSelected(), item.getSerial(), item.getKey());
	}

	public static Statement<?> prepareSelectFeed(String sn, String key, ConsistencyLevel consistency) {
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ? AND %s = ?", CassandraClient.getKeySpace(),
				FEEDS_CF, SN, KEY);
		return simpleStatement(query, consistency, sn, key);
	}

	public static Statement<?> prepareSelectFeedValue(String sn, String key, ConsistencyLevel consistency) {
		String query = String.format("SELECT %s, %s, %s FROM %s.%s WHERE %s = ? AND %s = ?",
				KEY, VAL, TS, CassandraClient.getKeySpace(), FEEDS_CF, SN, KEY);
		return simpleStatement(query, consistency, sn, key);
	}

	public static Statement<?> prepareSelectFeedsValues(String sn, List<String> keys, ConsistencyLevel consistency) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("SELECT ").append(KEY).append(", ").append(VAL).append(", ").append(TS)
				.append(" FROM ").append(CassandraClient.getKeySpace()).append('.').append(FEEDS_CF)
				.append(" WHERE ").append(SN).append(" = ?");
		values.add(sn);
		if (keys != null) {
			query.append(" AND ").append(KEY).append(" IN (").append(placeholders(keys.size())).append(")");
			values.addAll(keys);
		}
		return simpleStatement(query.toString(), consistency, values.toArray());
	}

	public static Statement<?> prepareSelectFeeds(String sn, List<String> keys, ConsistencyLevel consistency) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("SELECT * FROM ").append(CassandraClient.getKeySpace()).append('.').append(FEEDS_CF)
				.append(" WHERE ").append(SN).append(" = ?");
		values.add(sn);
		if (keys != null) {
			query.append(" AND ").append(KEY).append(" IN (").append(placeholders(keys.size())).append(")");
			values.addAll(keys);
		}
		return simpleStatement(query.toString(), consistency, values.toArray());
	}

	public static Statement<?> prepareDeleteFeeds(String sn) {
		String query = String.format("DELETE FROM %s.%s WHERE %s = ?", CassandraClient.getKeySpace(), FEEDS_CF, SN);
		return simpleStatement(query, DELETE_CONSISTENCY_LEVEL, sn);
	}

	public static Feed fillFeedValue(String sn, Row row) {
		Feed item = new Feed(sn, row.getString(KEY));
		item.setDate(getDate(row, TS));
		item.setValue(row.getFloat(VAL));
		return item;
	}

	public static Feed fillFeed(Row row) {
		Feed item = new Feed(row.getString(SN), row.getString(KEY));
		item.setAlarmed(row.getBool(ALARMED));
		item.setActive(row.getBool(ACTIVE));
		item.setSelected(row.getBool(SELECTED));
		item.setIdentifier(row.getString(PID));
		item.setLabel(row.getString(LBL));
		item.setQualifier(row.getInt(QUAL));
		item.setUnit(row.getString(UNIT));
		item.setOffset(row.getFloat(OFFSET));
		item.setScale(row.getFloat(SCALE));
		item.setDate(getDate(row, TS));
		item.setValue(row.getFloat(VAL));
		item.setSince(getDate(row, SINCE));
		item.setTtl(row.getInt(TTL));
		item.setInterpolation(row.getString(MININTERPL));
		item.setAggregation(row.getFloat(AVAL));
		item.setTypeVar(row.getString(TYPEVAR));
		return item;
	}

	public static Statement<?> prepareSelectFeedLabel(String sn, String key, ConsistencyLevel consistency) {
		String query = String.format("SELECT %s FROM %s.%s WHERE %s = ? AND %s = ?", LBL,
				CassandraClient.getKeySpace(), FEEDS_CF, SN, KEY);
		return simpleStatement(query, consistency, sn, key);
	}

	static DataSink fillDataSink(Row row) {
		DataSink item = new DataSink(row.getString(SN));
		item.setOwner(row.getString(OWN));
		item.setWriteApiKey(row.getString(W_API_KEY));
		item.setLabel(row.getString(W_API_KEY));
		item.setProtocol(row.getString(PROTOCOL));
		item.setStatus(row.getString(STATUS));
		item.setTimeZone(row.getString(TIME_ZONE));
		item.setLastContact(getDate(row, LASTCONTACT));
		item.setLastRollup(getDate(row, LASTROLLUP));
		item.setRecords(row.getLong(RECORDS));
		item.setMaster(row.getString(MASTER));
		item.setAlarmed(row.getBool(ALARMED));
		item.setTracing(row.getBool(TRACING));
		item.setPublishing(row.getBool(PUBLISHING));
		item.setCheckSum(row.getString(CHECKSUM));
		item.setActiveAlarms(row.getBool(ACTIVE_ALARMS));
		return item;
	}

	public static Statement<?> prepareUpdateDataSinkOnLastRollup(DataSink item, ConsistencyLevel consistency) {
		String query = String.format("UPDATE %s.%s SET %s = ?, %s = ? WHERE %s = ? IF EXISTS",
				CassandraClient.getKeySpace(), DATASINKS_CF, LASTROLLUP, RECORDS, SN);
		return simpleStatement(query, consistency, item.getLastRollup(), item.getRecords(), item.getSerial());
	}

	public static Statement<?> prepareSelectDataSinks(ConsistencyLevel consistency) {
		String query = String.format("SELECT * FROM %s.%s", CassandraClient.getKeySpace(), DATASINKS_CF);
		return simpleStatement(query, consistency);
	}

	public static Statement<?> prepareSelectDataSinkIds(ConsistencyLevel consistency) {
		String query = String.format("SELECT %s FROM %s.%s", SN, CassandraClient.getKeySpace(), DATASINKS_CF);
		return simpleStatement(query, consistency);
	}

	public static Statement<?> prepareSelectLastMeasure(String sn, String key, String keyspace,
			ConsistencyLevel consistency) {
		String query = String.format("SELECT %s, %s FROM %s.%s WHERE %s = ? AND %s = ?", TS, VAL,
				keyspace, FEEDS_CF, SN, KEY);
		return simpleStatement(query, consistency, sn, key);
	}

	public static Statement<?> prepareSelectLastMeasures(String sn, Set<String> keys, String keyspace,
			ConsistencyLevel consistency) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("SELECT ").append(KEY).append(", ").append(TS).append(", ").append(VAL)
				.append(" FROM ").append(keyspace).append('.').append(FEEDS_CF)
				.append(" WHERE ").append(SN).append(" = ?");
		values.add(sn);
		if (keys != null && keys.size() < 50) {
			query.append(" AND ").append(KEY).append(" IN (").append(placeholders(keys.size())).append(")");
			values.addAll(keys);
		}
		return simpleStatement(query.toString(), consistency, values.toArray());
	}

}
