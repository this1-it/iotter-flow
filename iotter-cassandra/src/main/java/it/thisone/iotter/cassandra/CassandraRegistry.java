package it.thisone.iotter.cassandra;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DriverException;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BatchableStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;

import it.thisone.iotter.cassandra.model.ConfigurationRaw;
import it.thisone.iotter.cassandra.model.ConfigurationRegistry;
import it.thisone.iotter.cassandra.model.ConfigurationRevision;

@Service
public class CassandraRegistry extends RegistryQueryBuilder implements Serializable{

	private static Logger logger = LoggerFactory
			.getLogger(CassandraRegistry.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1019058202995574938L;

	private static final int UPDATE_BATCH_SIZE = 16;

	@Autowired
	private CassandraClient client;

	public void updateConfigurationRegistry(ConfigurationRegistry item) {
		try {
			{
				item.setTimestamp(new Date());
				Statement stmt = prepareUpdateConfigurationRegistry(item);
				getSession().execute(stmt);
			}
		} catch (DriverException e) {
			logger.error(
					"unrecoverable error, unable to delete update Configuration Registry for id "
							+ item.getId(), e);
		}

	}


	public List<ConfigurationRegistry> getConfigurations(String serial,
			boolean owner) {
		List<ConfigurationRegistry> items = new ArrayList<ConfigurationRegistry>();
		try {
			Statement stmt = prepareSelectConfigurationRegistry(serial, owner, null)
					.setPageSize(PREFETCH_LIMIT);
			ResultSet rs = getSession().execute(stmt);
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
				items.add(fillConfigurationRegistry(row));
			}
		} catch (DriverException e) {
			logger.error(
					"cassandra not available retrieving Configuration Registry",
					e);
		}
		return items;
	}

	public ConfigurationRegistry getConfiguration(String serial,
			boolean owner, String id) {
		ConfigurationRegistry item =null;
		try {
			Statement stmt = prepareSelectConfigurationRegistry(serial, owner, id)
					.setPageSize(PREFETCH_LIMIT);
			ResultSet rs = getSession().execute(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = iter.next();
				item =fillConfigurationRegistry(row);
			}
		} catch (DriverException e) {
			logger.error(
					"cassandra not available retrieving Configuration Registry",
					e);
		}
		return item;
	}


	public ConfigurationRevision getConfigurationRevision(String serial,
			boolean owner) {
		Statement stmt = prepareSelectConfigurationRevision(serial, owner);
		ResultSet rs = getSession().execute(stmt);
		Iterator<Row> iter = rs.iterator();
		Date ts = new Date(0);
		int rev = -1;
		
		while (iter.hasNext()) {
			Row row = iter.next();
			Date date = CassandraQueryBuilder.getDate(row, TS);
			if (date.after(ts)) {
				ts = date;
				rev = row.getInt(REV);
			}
		}
		
		if (rev >=0 ) {
			return new ConfigurationRevision(ts, rev);
		}
		return null;
	}


	public void deleteConfigurationRegistry(String serial, boolean owner) {
		Statement stmt = prepareDeleteConfigurationRegistry(serial, owner);
		getSession().execute(stmt);
	}



	public boolean checkPendingConfiguration(String serial) {
		ConfigurationRevision pending = getConfigurationRevision(serial, false);
		return (pending != null);
	}

	public ConfigurationRevision deviceConfigurationRevision(String serial) {
		return getConfigurationRevision(serial, true);
	}
	
	@Deprecated
	public void insertConfigurationRaw(ConfigurationRaw item) {
		Statement stmt = insertConfiguration(item);
		getSession().execute(stmt);
	}

	@Deprecated
	public void updateConfigurationRaw(ConfigurationRaw item) {
		Statement stmt = updateConfiguration(item);
		getSession().execute(stmt);
	}
	
	@Deprecated
	public ConfigurationRaw getConfiguration(String serial, boolean owner) {
		Statement stmt = selectConfiguration(serial, owner);
		ResultSet rs = getSession().execute(stmt);
		Iterator<Row> iter = rs.iterator();
		if (iter.hasNext()) {
			Row row = rs.one();
			return fillConfiguration(row);
		}
		return null;		
	}

	
	
	@Deprecated
	public void deleteConfigurationRaw(String serial, boolean owner) {
		Statement stmt = deleteConfiguration(serial, owner);
		getSession().execute(stmt);
	}
	

	private CqlSession getSession() {
		return client.getSession();
	}


	public void deleteAllConfigurationRegistry(String sn) {
		Statement stmt = prepareDeleteAllConfigurationRegistry(sn);
		getSession().execute(stmt);
		
	}


	public void updateRegistryBatch(List<ConfigurationRegistry> items) {
		if (items.isEmpty()) return;
		BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED)
				.setConsistencyLevel(client.writeConsistencyLevel());
		for (ConfigurationRegistry item : items) {
			BatchableStatement<?> stmt = (BatchableStatement<?>) prepareUpdateConfigurationRegistry(item);
			batch = batch.add(stmt);
			if (batch.size() > UPDATE_BATCH_SIZE) {
				client.executeWithRetry(batch);
				batch = BatchStatement.newInstance(BatchType.UNLOGGED)
						.setConsistencyLevel(client.writeConsistencyLevel());
			}
		}
		if (batch.size() > 0) {
			client.executeWithRetry(batch);
		}		
	}


}
