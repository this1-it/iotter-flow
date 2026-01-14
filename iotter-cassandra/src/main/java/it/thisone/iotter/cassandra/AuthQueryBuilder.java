package it.thisone.iotter.cassandra;

import java.util.ArrayList;
import java.util.List;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;

import it.thisone.iotter.cassandra.model.SessionAuthentication;
import it.thisone.iotter.cassandra.model.SessionAuthorization;

public class AuthQueryBuilder extends CassandraQueryBuilder {
	
	public static Statement<?> prepareInsertAuthentication(SessionAuthentication item, String keyspace, int ttl,
			ConsistencyLevel consistency) {
		StringBuilder query = new StringBuilder();
		query.append("INSERT INTO ").append(keyspace).append('.').append(SESSION_AUTHENTICATION)
				.append(" (").append(USER).append(", ").append(TOKEN).append(", ").append(EXPIRY)
				.append(", ").append(ROLE).append(") VALUES (?, ?, ?, ?)");
		List<Object> values = new ArrayList<Object>();
		values.add(item.getUsername());
		values.add(item.getToken());
		values.add(item.getExpiryDate());
		values.add(item.getRole());
		if (ttl > 0) {
			query.append(" USING TTL ?");
			values.add(ttl);
		}
		return simpleStatement(query.toString(), consistency, values.toArray());
	}

	public static SessionAuthentication fillAuthentication(Row row) {
		SessionAuthentication item = new SessionAuthentication();
		item.setUsername(row.getString(USER));
		item.setExpiryDate(getDate(row, EXPIRY));
		item.setRole(row.getString(ROLE));
		item.setToken(row.getString(TOKEN));
		return item;
	}

	public static Statement<?> prepareSelectAuthentication(String username, String keyspace,
			ConsistencyLevel consistency) {
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ? LIMIT 1", keyspace,
				SESSION_AUTHENTICATION, USER);
		return simpleStatement(query, consistency, username);
	}

	public static Statement<?> prepareSelectAuthenticatedUser(String token, String keyspace,
			ConsistencyLevel consistency) {
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ? LIMIT 1 ALLOW FILTERING",
				keyspace, SESSION_AUTHENTICATION, TOKEN);
		return simpleStatement(query, consistency, token);
	}
	
	public static Statement<?> prepareInsertAuthorization(SessionAuthorization item, String keyspace, int ttl,
			ConsistencyLevel consistency) {
		StringBuilder query = new StringBuilder();
		query.append("INSERT INTO ").append(keyspace).append('.').append(SESSION_AUTHORIZATION)
				.append(" (").append(TOKEN).append(", ").append(GRANT).append(") VALUES (?, ?)");
		List<Object> values = new ArrayList<Object>();
		values.add(item.getToken());
		values.add(item.getGrant());
		query.append(" IF NOT EXISTS");
		if (ttl > 0) {
			query.append(" USING TTL ?");
			values.add(ttl);
		}
		return simpleStatement(query.toString(), consistency, values.toArray());
	}

	public static SessionAuthorization fillAuthorization(Row row) {
		SessionAuthorization item = new SessionAuthorization();
		item.setGrant(row.getString(GRANT));
		item.setToken(row.getString(TOKEN));
		return item;
	}

	public static Statement<?> prepareSelectAuthorization(String token, String grant, String keyspace,
			ConsistencyLevel consistency) {
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ? AND %s = ? LIMIT 1", keyspace,
				SESSION_AUTHORIZATION, TOKEN, GRANT);
		return simpleStatement(query, consistency, token, grant);
	}

	
	public static Statement<?> prepareSelectAuthorizations(String token, String keyspace,
			ConsistencyLevel consistency) {
		String query = String.format("SELECT * FROM %s.%s WHERE %s = ?", keyspace,
				SESSION_AUTHORIZATION, TOKEN);
		return simpleStatement(query, consistency, token);
	}
	
	public static Statement<?> prepareDeleteAuthorization(SessionAuthorization item, String keyspace,
			ConsistencyLevel consistency) {
		String query = String.format("DELETE FROM %s.%s WHERE %s = ? AND %s = ?", keyspace,
				SESSION_AUTHORIZATION, TOKEN, GRANT);
		return simpleStatement(query, consistency, item.getToken(), item.getGrant());
	}
	
}
