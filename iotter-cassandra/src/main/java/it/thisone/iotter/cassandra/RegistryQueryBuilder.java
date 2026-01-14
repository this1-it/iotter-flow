package it.thisone.iotter.cassandra;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;

import it.thisone.iotter.cassandra.model.ConfigurationRaw;
import it.thisone.iotter.cassandra.model.ConfigurationRegistry;

public class RegistryQueryBuilder extends CassandraQueryBuilder {

	@Deprecated
	public static ConfigurationRaw fillConfiguration(Row row) {
		ConfigurationRaw item = new ConfigurationRaw();
		item.setKey(row.getString(KEY));
		item.setOwner(row.getBool(OWN));
		item.setRevision(row.getInt(REV));
		item.setPayload(row.getString(PAYLOAD));
		item.setDate(getDate(row, TS));
		return item;
	}

	@Deprecated
	public static Statement<?> insertConfiguration(ConfigurationRaw item) {
		String query = String.format("INSERT INTO %s.%s (%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)",
				CassandraClient.getKeySpace(), CONFIGURATION_CF_NAME, KEY, OWN, REV, PAYLOAD, TS);
		return simpleStatement(query, null, item.getKey(), item.getOwner(), item.getRevision(), item.getPayload(),
				new Date());
	}

	@Deprecated
	public static Statement<?> updateConfiguration(ConfigurationRaw item) {
		String query = String.format("UPDATE %s.%s SET %s = ?, %s = ?, %s = ? WHERE %s = ? AND %s = ?",
				CassandraClient.getKeySpace(), CONFIGURATION_CF_NAME, PAYLOAD, REV, TS, KEY, OWN);
		return simpleStatement(query, null, item.getPayload(), item.getRevision(), new Date(), item.getKey(),
				item.getOwner());
	}

	@Deprecated
	public static Statement<?> deleteConfiguration(String key, boolean owner) {
		String query = String.format("DELETE FROM %s.%s WHERE %s = ? AND %s = ?", CassandraClient.getKeySpace(),
				CONFIGURATION_CF_NAME, KEY, OWN);
		return simpleStatement(query, null, key, owner);
	}

	@Deprecated
	public static Statement<?> selectConfiguration(String key, boolean owner) {
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ? AND %s = ?", CassandraClient.getKeySpace(),
				CONFIGURATION_CF_NAME, KEY, OWN);
		return simpleStatement(query, null, key, owner);
	}

	@Deprecated
	public static Statement<?> selectConfigurationRevision(String key, boolean owner) {
		String query = String.format("SELECT %s, %s FROM %s.%s WHERE %s = ? AND %s = ?", REV, TS,
				CassandraClient.getKeySpace(), CONFIGURATION_CF_NAME, KEY, OWN);
		return simpleStatement(query, null, key, owner);
	}

	public static Statement<?> prepareUpdateConfigurationRegistry(ConfigurationRegistry item) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("UPDATE ").append(CassandraClient.getKeySpace()).append('.').append(CONFIGURATION_REGISTRY_CF_NAME)
				.append(" SET ");
		boolean first = true;
		if (item.getTimestamp() != null) {
			query.append(TS).append(" = ?");
			values.add(item.getTimestamp());
			first = false;
		}
		if (item.getGroup() != null) {
			if (!first) {
				query.append(", ");
			}
			query.append(GROUP).append(" = ?");
			values.add(item.getGroup());
			first = false;
		}
		if (item.getLabel() != null) {
			if (!first) {
				query.append(", ");
			}
			query.append(LBL).append(" = ?");
			values.add(item.getLabel());
			first = false;
		}
		if (item.getPermission() != null) {
			if (!first) {
				query.append(", ");
			}
			query.append(PERMISSION).append(" = ?");
			values.add(item.getPermission());
			first = false;
		}
		if (item.getSection() != null) {
			if (!first) {
				query.append(", ");
			}
			query.append(SECTION).append(" = ?");
			values.add(item.getSection());
			first = false;
		}
		if (item.getTopic() != null) {
			if (!first) {
				query.append(", ");
			}
			query.append(TOPIC).append(" = ?");
			values.add(item.getTopic());
			first = false;
		}
		if (item.getValue() != null) {
			if (!first) {
				query.append(", ");
			}
			query.append(VAL).append(" = ?");
			values.add(item.getValue());
			first = false;
		}
		if (item.getMax() != null) {
			if (!first) {
				query.append(", ");
			}
			query.append(MAXVAL).append(" = ?");
			values.add(item.getMax());
			first = false;
		}
		if (item.getMin() != null) {
			if (!first) {
				query.append(", ");
			}
			query.append(MINVAL).append(" = ?");
			values.add(item.getMin());
			first = false;
		}
		if (!first) {
			query.append(", ");
		}
		query.append(REV).append(" = ?");
		values.add(item.getRevision());
		query.append(" WHERE ").append(SN).append(" = ? AND ").append(OWN).append(" = ? AND ").append(ID)
				.append(" = ?");
		values.add(item.getSerial());
		values.add(item.getOwner());
		values.add(item.getId());
		return simpleStatement(query.toString(), null, values.toArray());
	}

	public static Statement<?> prepareSelectConfigurationRegistry(String serial, boolean owner, String id) {
		StringBuilder query = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		query.append("SELECT * FROM ").append(CassandraClient.getKeySpace()).append('.').append(CONFIGURATION_REGISTRY_CF_NAME)
				.append(" WHERE ").append(SN).append(" = ? AND ").append(OWN).append(" = ?");
		values.add(serial);
		values.add(owner);
		if (id != null) {
			query.append(" AND ").append(ID).append(" = ?");
			values.add(id);
		}
		return simpleStatement(query.toString(), null, values.toArray());
	}

	public static Statement<?> prepareDeleteConfigurationRegistry(String serial, boolean owner) {
		String query = String.format("DELETE FROM %s.%s WHERE %s = ? AND %s = ?", CassandraClient.getKeySpace(),
				CONFIGURATION_REGISTRY_CF_NAME, SN, OWN);
		return simpleStatement(query, DELETE_CONSISTENCY_LEVEL, serial, owner);
	}

	public static Statement<?> prepareDeleteAllConfigurationRegistry(String serial) {
		String query = String.format("DELETE FROM %s.%s WHERE %s = ?", CassandraClient.getKeySpace(),
				CONFIGURATION_REGISTRY_CF_NAME, SN);
		return simpleStatement(query, DELETE_CONSISTENCY_LEVEL, serial);
	}

	public static Statement<?> prepareSelectConfigurationRevision(String serial, boolean owner) {
		String query = String.format("SELECT %s, %s FROM %s.%s WHERE %s = ? AND %s = ?", TS, REV,
				CassandraClient.getKeySpace(), CONFIGURATION_REGISTRY_CF_NAME, SN, OWN);
		return simpleStatement(query, null, serial, owner);
	}

	public static ConfigurationRegistry fillConfigurationRegistry(Row row) {
		String serial = row.getString(SN);
		boolean own = row.getBool(OWN);
		String id = row.getString(ID);
		ConfigurationRegistry item = new ConfigurationRegistry(serial, own, id);
		item.setRevision(row.getInt(REV));
		item.setTimestamp(getDate(row, TS));
		item.setGroup(row.getString(GROUP));
		item.setLabel(row.getString(LBL));
		item.setPermission(row.getString(PERMISSION));
		item.setSection(row.getString(SECTION));
		item.setTopic(row.getString(TOPIC));
		item.setValue(row.getFloat(VAL));
		item.setMax(row.getFloat(MAXVAL));
		item.setMin(row.getFloat(MINVAL));
		return item;
	}

}
