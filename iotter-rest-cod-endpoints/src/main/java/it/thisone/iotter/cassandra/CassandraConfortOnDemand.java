package it.thisone.iotter.cassandra;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;

import it.thisone.iotter.config.CODConfig;
import it.thisone.iotter.rest.model.client.ConfortOnDemandEvent;

@Component
public class CassandraConfortOnDemand {
	private static final String COD_EVENTS_CF = "cod_events";
	private static Logger logger = LoggerFactory.getLogger(CODConfig.CONFORT_ON_DEMAND_LOG4J_CATEGORY);

	@Autowired
	private CassandraClient client;

	private static final String CREATE_TABLE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS ";

	private static final String WITH_CACHING_ALL = " WITH caching = {'keys': 'ALL', 'rows_per_partition': 'ALL'} ";

	public void createTable() {
		String statement = CREATE_TABLE_IF_NOT_EXISTS + client.getCFName(COD_EVENTS_CF)
				+ " (sn varchar, beacon varchar, userid varchar, ts timestamp, topic varchar, type varchar, "
				+ "priority int, delta1 int, delta2 int, "
				+ "set_wanted float, set_min float, set_max float, set_default float,   PRIMARY KEY ((sn, beacon), userid)) "
				+ WITH_CACHING_ALL;
		;

		try {
			client.executeQuery(statement);
		} catch (Exception e) {
			logger.error("unrecoverable error", e);
		}
	}

	public ConfortOnDemandEvent aggregate(String sn, String beacon) {
		return new ConfortOnDemandEvent();
	}
	
	public List<ConfortOnDemandEvent> selectPKs() {
		List<ConfortOnDemandEvent> items = new ArrayList<ConfortOnDemandEvent>();
		String[] pk = new String[] { "sn", "beacon" };
		try {
			Select stmt = QueryBuilder.select(pk).distinct().from(client.getCassandraOps().getSession().getLoggedKeyspace(), COD_EVENTS_CF);
			stmt.setConsistencyLevel(client.readConsistencyLevel());
			ResultSet rs = client.getCassandraOps().getSession().execute(stmt);
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
				ConfortOnDemandEvent event = new ConfortOnDemandEvent();
				event.setBeacon(row.getString("beacon"));
				event.setSerial(row.getString("sn"));
				items.add(event);
			}
			
		} catch (InvalidQueryException e) {
			logger.error("cassandra not available retrieving ConfortOnDemandEvent pks ", e);
		}
		return items;
	}
	
	public List<ConfortOnDemandEvent> select(String sn, String beacon) {
		List<ConfortOnDemandEvent> items = new ArrayList<ConfortOnDemandEvent>();
		try {
			Statement stmt = prepareSelect(sn, beacon, client.getCassandraOps().getSession().getLoggedKeyspace(),
					client.readConsistencyLevel());
			ResultSet rs = client.getCassandraOps().getSession().execute(stmt);
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
				items.add(fill(row));
			}
		} catch (InvalidQueryException e) {
			logger.error("cassandra not available retrieving ConfortOnDemandEvent for sn " + sn, e);
		}
		return items;
	}

	public void insert(ConfortOnDemandEvent item) {
		try {
			{
				Statement stmt = prepareInsert(item, client.writeConsistencyLevel());
				client.getCassandraOps().getSession().execute(stmt);
			}
		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to insert ConfortOnDemandEvent for sn " + item.getSerial(), e);
		}
	}

	public void delete(ConfortOnDemandEvent item) {
		try {
			Statement stmt = prepareDelete(item.getSerial(),item.getBeacon(),item.getUserid());
			client.executeWithRetry(stmt);
		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to delete ConfortOnDemandEvent for sn " + item.getSerial(), e);
		}
	}
	
	
	
	static Statement prepareDelete(String serial, String beacon, String userid) {
		Delete stmt = QueryBuilder.delete().from(CassandraClient.getKeySpace(),
				COD_EVENTS_CF);
		stmt.where(eq("sn", serial)).and(eq("beacon", beacon)).and(eq("userid", userid));
		stmt.setConsistencyLevel(ConsistencyLevel.ALL);
		return stmt;	}

	static Statement prepareInsert(ConfortOnDemandEvent item, ConsistencyLevel consistency) {
		Insert stmt = QueryBuilder.insertInto(CassandraClient.getKeySpace(), COD_EVENTS_CF);

		stmt.value("beacon", item.getBeacon());
		stmt.value("delta1", item.getDelta1());
		stmt.value("delta2", item.getDelta2());
		stmt.value("priority", item.getPriority());
		stmt.value("set_default", item.getSetpointDefault());
		stmt.value("set_max", item.getSetpointMax());
		stmt.value("set_min", item.getSetpointMin());
		stmt.value("set_wanted", item.getSetpointWanted());
		stmt.value("ts", item.getTimestamp());
		stmt.value("topic", item.getTopic());
		stmt.value("type", item.getType());
		stmt.value("sn", item.getSerial());
		stmt.value("userid", item.getUserid());

		stmt.setConsistencyLevel(consistency);
		return stmt;
	}

	static ConfortOnDemandEvent fill(Row row) {
		ConfortOnDemandEvent event = new ConfortOnDemandEvent();
		event.setBeacon(row.getString("beacon"));
		event.setDelta1(row.getInt("delta1"));
		event.setDelta2(row.getInt("delta2"));
		event.setPriority(row.getInt("priority"));
		event.setSerial(row.getString("sn"));
		event.setSetpointDefault(row.getFloat("set_default"));
		event.setSetpointMax(row.getFloat("set_max"));
		event.setSetpointMin(row.getFloat("set_min"));
		event.setSetpointWanted(row.getFloat("set_wanted"));
		event.setTimestamp(row.getTimestamp("ts"));
		event.setTopic(row.getString("topic"));
		event.setType(row.getString("type"));
		event.setUserid(row.getString("userid"));
		return event;
	}

	static Statement prepareSelect(String sn, String beacon, String keyspace, ConsistencyLevel consistency) {
		Select stmt = QueryBuilder.select().from(keyspace, COD_EVENTS_CF);
		stmt.where().and(eq("sn", sn));
		if (beacon != null) {
			stmt.where().and(eq("beacon", beacon));
		}
		stmt.setConsistencyLevel(consistency);
		return stmt;
	}



}
