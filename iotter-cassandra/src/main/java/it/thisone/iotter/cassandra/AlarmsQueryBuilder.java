package it.thisone.iotter.cassandra;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;

import it.thisone.iotter.cassandra.model.FeedAlarm;
import it.thisone.iotter.cassandra.model.FeedAlarmEvent;
import it.thisone.iotter.cassandra.model.FeedAlarmThresholds;

public class AlarmsQueryBuilder extends CassandraQueryBuilder {

	public static Statement<?> prepareUpdateAlarmThresholds(FeedAlarmThresholds item) {
		String query = String.format(
				"UPDATE %s.%s SET %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ? WHERE %s = ? AND %s = ?",
				CassandraClient.getKeySpace(), FEEDALARMTHRESHOLDS_CF_NAME, ARMED, NOTIFY, HIGH, HIGH_HIGH, LOW,
				LOW_LOW, PRIORITY, REPEAT_MINUTES, DELAY_MINUTES, SN, KEY);
		return simpleStatement(query, null, item.isArmed(), item.isNotify(), item.getHigh(), item.getHighHigh(),
				item.getLow(), item.getLowLow(), item.getPriority(), item.getRepeatMinutes(), item.getDelayMinutes(),
				item.getSerial(), item.getKey());
	}

	public static FeedAlarmThresholds fillAlarmThresholds(Row row) {
		FeedAlarmThresholds item = new FeedAlarmThresholds(row.getString(SN), row.getString(KEY));
		item.setArmed(row.getBool(ARMED));
		item.setNotify(row.getBool(NOTIFY));
		item.setHigh(row.getFloat(HIGH));
		item.setHighHigh(row.getFloat(HIGH_HIGH));
		item.setLow(row.getFloat(LOW));
		item.setLowLow(row.getFloat(LOW_LOW));
		item.setRepeatMinutes(row.getInt(REPEAT_MINUTES));
		item.setDelayMinutes(row.getInt(DELAY_MINUTES));
		item.setPriority(row.getString(PRIORITY));
		return item;
	}

	public static Statement<?> prepareSelectAlarmThresholds(String sn, String key) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("SELECT * FROM ").append(CassandraClient.getKeySpace()).append('.').append(FEEDALARMTHRESHOLDS_CF_NAME)
				.append(" WHERE ").append(SN).append(" = ?");
		values.add(sn);
		if (key != null) {
			query.append(" AND ").append(KEY).append(" = ?");
			values.add(key);
		}
		return simpleStatement(query.toString(), null, values.toArray());
	}

	public static Statement<?> prepareSelectAlarm(String sn, String key) {
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ? AND %s = ?", CassandraClient.getKeySpace(),
				FEEDALARMS_CF_NAME, SN, KEY);
		return simpleStatement(query, null, sn, key);
	}

	public static FeedAlarm fillAlarm(Row row) {
		FeedAlarm item = new FeedAlarm(row.getString(SN), row.getString(KEY));
		item.setActive(row.getBoolean(ACTIVE));
		item.setDelayed(row.getBoolean(DELAY));
		item.setRepeated(row.getBoolean(REPEAT));
		item.setValue(row.getFloat(VAL));
		item.setThreshold(row.getFloat(THRESHOLD));
		item.setStatus(row.getString(STATUS));
		item.setTimestamp(getDate(row, TS));
		item.setUpdated(getDate(row, UPDATED));
		return item;
	}

	public static Statement<?> prepareUpdateAlarm(FeedAlarm item) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("UPDATE ").append(CassandraClient.getKeySpace()).append('.').append(FEEDALARMS_CF_NAME)
				.append(" SET ").append(ACTIVE).append(" = ?, ").append(STATUS).append(" = ?, ").append(VAL)
				.append(" = ?, ").append(THRESHOLD).append(" = ?, ").append(DELAY).append(" = ?, ").append(REPEAT)
				.append(" = ?");
		values.add(item.isActive());
		values.add(item.getStatus());
		values.add(item.getValue());
		values.add(item.getThreshold());
		values.add(item.isDelayed());
		values.add(item.isRepeated());

		// Capture values once to avoid race condition between null check and add
		Date timestamp = item.getTimestamp();
		Date updated = item.getUpdated();

		if (timestamp != null) {
			query.append(", ").append(TS).append(" = ?");
			values.add(timestamp.toInstant());
		}
		if (updated != null) {
			query.append(", ").append(UPDATED).append(" = ?");
			values.add(updated.toInstant());
		}

		query.append(" WHERE ").append(SN).append(" = ? AND ").append(KEY).append(" = ?");
		values.add(item.getSerial());
		values.add(item.getKey());
		return simpleStatement(query.toString(), null, values.toArray());
	}

	public static Statement<?> prepareSelectDelayedAlarms() {
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ? AND %s = ? ALLOW FILTERING",
				CassandraClient.getKeySpace(), FEEDALARMS_CF_NAME, DELAY, ACTIVE);
		return simpleStatement(query, null, true, true);
	}

	public static Statement<?> prepareSelectRepeatedAlarms() {
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ? AND %s = ? ALLOW FILTERING",
				CassandraClient.getKeySpace(), FEEDALARMS_CF_NAME, REPEAT, ACTIVE);
		return simpleStatement(query, null, true, true);
	}

	public static Statement<?> prepareSelectAlarms(String serial) {
		if (serial == null) {
			String query = String.format("SELECT * FROM %s.%s", CassandraClient.getKeySpace(), FEEDALARMS_CF_NAME);
			return simpleStatement(query, null);
		}
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ?", CassandraClient.getKeySpace(),
				FEEDALARMS_CF_NAME, SN);
		return simpleStatement(query, null, serial);
	}

	public static Statement<?> prepareCountActiveAlarms(String serial) {
		String query = String.format("SELECT %s, %s, %s FROM %s.%s WHERE %s = ?", ACTIVE, SN, KEY,
				CassandraClient.getKeySpace(), FEEDALARMS_CF_NAME, SN);
		return simpleStatement(query, null, serial);
	}

	public static FeedAlarmEvent fillAlarmEvent(Row row) {
		FeedAlarmEvent item = new FeedAlarmEvent(row.getString(SN), row.getString(KEY), getDate(row, TS),
				getDate(row, CREATED));
		item.setNotify(row.getBool(NOTIFY));
		item.setValue(row.getFloat(VAL));
		item.setThreshold(row.getFloat(THRESHOLD));
		item.setStatus(row.getString(STATUS));
		item.setOperator(row.getString(OPERATOR));
		item.setMembers(row.getString(MEMBERS));
		return item;
	}

	public static Statement<?> prepareUpdateAlarmEvent(FeedAlarmEvent item, int ttl, ConsistencyLevel consistency) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("UPDATE ").append(CassandraClient.getKeySpace()).append('.').append(FEEDALARMEVENTS_CF_NAME)
				.append(" SET ").append(NOTIFY).append(" = ?, ").append(THRESHOLD).append(" = ?, ")
				.append(STATUS).append(" = ?, ").append(VAL).append(" = ?, ").append(OPERATOR).append(" = ?, ")
				.append(MEMBERS).append(" = ?, ").append(TS).append(" = ? WHERE ").append(SN).append(" = ? AND ")
				.append(KEY).append(" = ? AND ").append(CREATED).append(" = ?");
		values.add(item.isNotify());
		values.add(item.getThreshold());
		values.add(item.getStatus());
		values.add(item.getValue());
		values.add(item.getOperator());
		values.add(item.getMembers());
		values.add(item.getTimestamp().toInstant());
		values.add(item.getSerial());
		values.add(item.getKey());
		values.add(item.getCreated().toInstant());
		if (ttl > 0) {
			query.append(" USING TTL ?");
			values.add(ttl);
		}
		return simpleStatement(query.toString(), consistency, values.toArray());
	}

	public static Statement<?> prepareSelectAlarmEvents(String sn, int limit) {
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ? ORDER BY %s DESC LIMIT ?",
				CassandraClient.getKeySpace(), FEEDALARMEVENTS_CF_NAME, SN, CREATED);
		return simpleStatement(query, null, sn, limit);
	}

	public static Statement<?> prepareSelectAlarmEvent(String sn, Date created, String key, ConsistencyLevel consistency) {
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ? AND %s = ? AND %s = ?",
				CassandraClient.getKeySpace(), FEEDALARMEVENTS_CF_NAME, SN, CREATED, KEY);
		return simpleStatement(query, consistency, sn, created, key);
	}

	public static Statement<?> prepareDeleteAlarms(String sn) {
		String query = String.format("DELETE FROM %s.%s WHERE %s = ?", CassandraClient.getKeySpace(),
				FEEDALARMS_CF_NAME, SN);
		return simpleStatement(query, null, sn);
	}

	public static Statement<?> prepareDeleteThresholds(String sn) {
		String query = String.format("DELETE FROM %s.%s WHERE %s = ?", CassandraClient.getKeySpace(),
				FEEDALARMTHRESHOLDS_CF_NAME, SN);
		return simpleStatement(query, null, sn);
	}

	public static Statement<?> prepareDeleteEvents(String sn) {
		String query = String.format("DELETE FROM %s.%s WHERE %s = ?", CassandraClient.getKeySpace(),
				FEEDALARMEVENTS_CF_NAME, SN);
		return simpleStatement(query, null, sn);
	}

}
