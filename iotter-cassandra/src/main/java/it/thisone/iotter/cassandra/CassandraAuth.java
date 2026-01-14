package it.thisone.iotter.cassandra;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DriverException;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;

import it.thisone.iotter.cassandra.model.SessionAuthentication;
import it.thisone.iotter.cassandra.model.SessionAuthorization;
import it.thisone.iotter.config.Constants;

/*
 * Authentication is the process of ascertaining that somebody really is who he claims to be.
 * Authorization refers to rules that determine who is allowed to do what. 
 */
@Service
public class CassandraAuth extends AuthQueryBuilder implements Serializable {
	private static Logger logger = LoggerFactory
			.getLogger(CassandraAuth.class);

	private static final long serialVersionUID = 1L;

	@Autowired
	private CassandraClient client;

	protected CqlSession getSession() {
		return client.getSession();
	}

	protected String getKeyspace() {
		return client.getSession().getKeyspace()
				.map(id -> id.asInternal())
				.orElse(CassandraClient.getKeySpace());
	}
	
	public SessionAuthentication findAuthenticationToken(String username) {
		SessionAuthentication item =null;
		try {
			Statement<?> stmt = prepareSelectAuthentication(username, getKeyspace(), client.readConsistencyLevel());
			ResultSet rs = getSession().execute(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = iter.next();
				item =fillAuthentication(row);
			}
		} catch (DriverException e) {
			logger.error(
					"cassandra not available retrieving Session Authenticate",
					e);
		}
		return item;
	}

	public SessionAuthentication findAuthenticatedUser(String token) {
		SessionAuthentication item =null;
		try {
			Statement<?> stmt = prepareSelectAuthenticatedUser(token, getKeyspace(), client.readConsistencyLevel());
			ResultSet rs = getSession().execute(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = iter.next();
				item =fillAuthentication(row);
			}
		} catch (DriverException e) {
			logger.error(
					"cassandra not available retrieving Session Authenticate",
					e);
		}
		return item;
	}
	
	public SessionAuthorization createAuthorizationGrant(String token, String grant, int ttl) {
		SessionAuthorization item =null;
		try {
			item = new SessionAuthorization();
			item.setGrant(grant);;
			item.setToken(token);
			Statement<?> stmt = prepareInsertAuthorization(item, getKeyspace(), ttl, client.writeConsistencyLevel());
			getSession().execute(stmt);

		} catch (DriverException e) {
			item =null;
			logger.error(
					"cassandra not available retrieving Session Authenticate",
					e);
		}
		return item;

	}
	public SessionAuthentication createAuthenticationToken(String username, String role, int ttl) {
		SessionAuthentication item =null;
		try {
			item = new SessionAuthentication();
			item.setUsername(username);
			String token = UUID.randomUUID().toString();
			if (role.equalsIgnoreCase(Constants.ROLE_SUPERVISOR)) {
				token = String.format("[%s]", token);
			}
			item.setToken(token);
			item.setRole(role);
			if (ttl > 0) {
				item.setExpiryDate(DateUtils.addSeconds(new Date(), ttl));
			}
			Statement<?> stmt = prepareInsertAuthentication(item, getKeyspace(), ttl, client.writeConsistencyLevel());
			getSession().execute(stmt);

		} catch (DriverException e) {
			item =null;
			logger.error(
					"cassandra not available retrieving Session Authenticate",
					e);
		}
		return item;

	}
	
	public boolean checkAuthorization(String token, String grant) {
		SessionAuthorization item =null;
		try {
			Statement<?> stmt = prepareSelectAuthorization(token, grant, getKeyspace(), client.readConsistencyLevel());
			ResultSet rs = getSession().execute(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = iter.next();
				item = fillAuthorization(row);
			}
		} catch (DriverException e) {
			logger.error(
					"cassandra not available retrieving SessionAuthorization",
					e);
		}
		return (item != null);
	}


	public List<SessionAuthorization> findAuthorizations(String token) {
		List<SessionAuthorization> items =new ArrayList<>();
		try {
			Statement<?> stmt = prepareSelectAuthorizations(token, getKeyspace(), client.readConsistencyLevel());
			ResultSet rs = getSession().execute(stmt);
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
				items.add(fillAuthorization(row)) ;
			}
		} catch (DriverException e) {
			logger.error(
					"cassandra not available retrieving SessionAuthorization",
					e);
		}
		return items;
	}

	public void deleteAuthorizationGrant(String token, String grant) {
		SessionAuthorization item =null;
		try {
			item = new SessionAuthorization();
			item.setGrant(grant);;
			item.setToken(token);
			Statement<?> stmt = prepareDeleteAuthorization(item, getKeyspace(), client.writeConsistencyLevel());
			getSession().execute(stmt);

		} catch (DriverException e) {
			item =null;
			logger.error(
					"cassandra not available retrieving Session Authenticate",
					e);
		}
		
	}


	
}
