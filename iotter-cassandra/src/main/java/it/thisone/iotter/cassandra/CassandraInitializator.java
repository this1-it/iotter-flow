package it.thisone.iotter.cassandra;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.DriverException;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;

import it.thisone.iotter.config.Constants;

/**
 * 
 * Time Series Data Modeling
 * http://planetcassandra.org/blog/getting-started-with
 * -time-series-data-modeling/
 * 
 * Composite Keys in Apache Cassandra
 * http://planetcassandra.org/blog/composite-keys-in-apache-cassandra/
 * 
 * 
 * PRIMARY KEY column "ts" cannot be restricted (preceding column "ts_rx" is
 * either not restricted or by a non-EQ relation)
 * 
 * http://stackoverflow.com/questions/22989708/bad-request-primary-key-part-to-
 * id-cannot-be-restricted-when-trying-to-select
 * 
 * @author tisone
 *
 *         I would say the best way to reset cassandra would be to delete the
 *         contents of the <data dir>/data/* <data dir>/commitlog/* <data
 *         dir>/saved_caches/*
 * 
 *         service cassandra stop
 *          rm -rf /var/lib/cassandra/data/* 
 *          rm -rf
 *         /var/lib/cassandra/commitlog/* 
 *         rm -rf
 *         /var/lib/cassandra/saved_caches/* service cassandra start
 * 
 * 
 *         TRUNCATE iotter.measures;
 * 
 * 
 *         ALTER TABLE iotter.measures ADD pos varchar; ALTER TABLE
 *         iotter.measures ADD qual int;
 *  
 *  Cassandra 3.5 like query
 *  CREATE CUSTOM INDEX  fn_prefix ON cyclist_name (firstname) USING 'org.apache.cassandra.index.sasi.SASIIndex';
 *  
 *  		// https://stackoverflow.com/questions/9905795/is-there-any-query-for-cassandra-as-same-as-sqllike-condition


		statement = "CREATE CUSTOM INDEX IF NOT EXISTS " + " idx_key_" + ROLL_UP_SINK_LOCK_CF + " on " + getCFName(ROLL_UP_SINK_LOCK_CF)
		+ " (key) USING 'org.apache.cassandra.index.sasi.SASIIndex';";
		executeQuery(statement);

		 CREATE CUSTOM INDEX IF NOT EXISTS idx_key_roll_up_sink_lock on aernet.roll_up_sink_lock (key) USING 'org.apache.cassandra.index.sasi.SASIIndex';
		

 */

public class CassandraInitializator implements InitializingBean, CassandraConstants {
	
	//private static final String COMPACTION_CLASS = " AND COMPACTION = {'class': 'DateTieredCompactionStrategy', 'max_sstable_age_days':'1'}";
	
	// http://cassandra.apache.org/doc/latest/operating/compaction.html
	
	/*
	 Ideally, operators should select a compaction_window_unit and compaction_window_size pair 
	 that produces approximately 20-30 windows - if writing with a 90 day TTL, 
	 for example, a 3 Day window would be a reasonable choice ('compaction_window_unit':'DAYS','compaction_window_size':3).
	 
	 2592000
	 
	 604800
	 
	 604860
	 
	 31452720
	 29030400
	 
	 315360000
	 
	 ALTER TABLE mykeyspace.mytable  WITH compaction = { 'class' :  'LeveledCompactionStrategy'  };
	 
	 



	 ALTER TABLE roll_up_m1  WITH compaction = {'class': 'SizeTieredCompactionStrategy'};
	 ALTER TABLE roll_up_w1  WITH compaction = {'class': 'SizeTieredCompactionStrategy'};
	 ALTER TABLE roll_up_d1  WITH compaction = {'class': 'SizeTieredCompactionStrategy'};
	 ALTER TABLE roll_up_h1  WITH compaction = {'class': 'SizeTieredCompactionStrategy'};



	 ALTER TABLE aernet.measures  WITH compaction = {'class': 'TimeWindowCompactionStrategy', 'compaction_window_unit':'DAYS', 'compaction_window_size':12, 'tombstone_compaction_interval': 86400, 'unchecked_tombstone_compaction': 'true' };

	 changed schema: read_repair_chance = 0.0, dclocal_read_repair_chance = 0.0, gc_grace_seconds = 3600
	 604800
	 */
	
	// set unit and size to produce approximately 20-30 windows covering ttl period
	
	private static final String WEEK_TW_COMPACTION_CLASS = " AND COMPACTION = {'class': 'TimeWindowCompactionStrategy', 'compaction_window_unit':'HOURS', 'compaction_window_size':6, 'tombstone_compaction_interval': '86400', 'unchecked_tombstone_compaction': 'true' }";
	private static final String YEAR_TW_COMPACTION_CLASS = " AND COMPACTION = {'class': 'TimeWindowCompactionStrategy', 'compaction_window_unit':'DAYS', 'compaction_window_size':12, 'tombstone_compaction_interval': '2592000', 'unchecked_tombstone_compaction': 'true' }";
	
	//private static final int MAX_TTL = 365 * 24 * 60 * 60; // 1 year

	private static final int MAX_TTL = 3 * 30 * 24 * 60 * 60; // 3 months 7776000

	private static final String CREATE_INDEX_IF_NOT_EXISTS = "CREATE INDEX IF NOT EXISTS ";
	private static final String CREATE_TABLE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS ";
	private static final String WITH_CACHING_ALL = " WITH caching = {'keys': 'ALL', 'rows_per_partition': 'ALL'} ";

	
	@Autowired
    private CqlSession session;
	
	private static Logger logger = LoggerFactory
			.getLogger(CassandraInitializator.class);

	@Autowired
    @Qualifier("cassandraProperties")
	private Properties properties;

	public String getReplication() {
		return properties.getProperty("cassandra.replication");
	}

	public String getKeySpace() {
		return properties.getProperty("cassandra.keyspace");
	}

	public String getCFName(String name) {
		return String.format("%s.%s", getKeySpace(), name);
	}

	
	private void createCFs() {
		createMeasureCFs();
		createRollupCFs();
		createFeedCFs();
		createFeedAlarmCFs();
		createConfigurationCFs();
		createAuthCFs();
	}

	private void createMeasureCFs() {

		// 7 days
		String default_time_to_live = time_to_live("604800");
		String statement = CREATE_TABLE_IF_NOT_EXISTS + getCFName(MEASURES_CF)
		+ " (key varchar, ts timestamp, tr timestamp, val float, err varchar, PRIMARY KEY ((key), ts)) WITH CLUSTERING ORDER BY (ts ASC) AND GC_GRACE_SECONDS = 3600 "
		+ default_time_to_live
		+ WEEK_TW_COMPACTION_CLASS;
		executeQuery(statement);

		statement = CREATE_TABLE_IF_NOT_EXISTS + getCFName(MEASURE_TICKS_CF)
		+ " (key varchar, ts timestamp, tr timestamp, PRIMARY KEY ((key), ts)) WITH CLUSTERING ORDER BY (ts ASC) AND GC_GRACE_SECONDS = 3600 "
		+ default_time_to_live;
		executeQuery(statement);

		statement = CREATE_TABLE_IF_NOT_EXISTS + getCFName(MEASURES_IDX_TMP_CF)
				+ " (uid varchar, id int, ts timestamp, PRIMARY KEY ((uid), id)) WITH DEFAULT_TIME_TO_LIVE = 3600 AND GC_GRACE_SECONDS = 3600";
		executeQuery(statement);
		
		statement = CREATE_TABLE_IF_NOT_EXISTS + getCFName(MEASURES_TS_TMP_CF)
				+ " (uid varchar, ts timestamp, PRIMARY KEY ((uid), ts)) WITH DEFAULT_TIME_TO_LIVE = 3600 AND GC_GRACE_SECONDS = 3600";
		executeQuery(statement);

	
		statement = CREATE_TABLE_IF_NOT_EXISTS + getCFName(MEASURES_EXPORT_TMP_CF)
		+ " (qid varchar,sn varchar,exp timestamp,lower timestamp,upper timestamp,start bigint,total bigint,batch_size int, ascending boolean, interpolation varchar, keys list<varchar>, PRIMARY KEY (qid) ) WITH DEFAULT_TIME_TO_LIVE = 3600 AND GC_GRACE_SECONDS = 3600";
        executeQuery(statement);

		statement = CREATE_TABLE_IF_NOT_EXISTS + getCFName(MEASURES_EXPORT_TS_CF)
		+ " (qid varchar, timestamps blob, PRIMARY KEY (qid) ) WITH DEFAULT_TIME_TO_LIVE = 86400 AND GC_GRACE_SECONDS = 3600";
        executeQuery(statement);
        
        
		statement = CREATE_TABLE_IF_NOT_EXISTS + getCFName(MEASURES_SET_CF)
		+ " (sn varchar, ts timestamp, val blob, PRIMARY KEY ((sn), ts)) WITH CLUSTERING ORDER BY (ts DESC) AND GC_GRACE_SECONDS = 3600 "
		+ default_time_to_live
		+ WEEK_TW_COMPACTION_CLASS;
		executeQuery(statement);
        
	
	}

	public String time_to_live(String default_time_to_live) {
		try {
			String value = properties.getProperty("cassandra.ttl.measures", default_time_to_live);
			int measuresTTL = Integer.parseInt(value);
			if (measuresTTL > 0) {
				measuresTTL = measuresTTL + 3600;
				default_time_to_live = String.format( "AND default_time_to_live = %d ", measuresTTL);
			}
		} catch (NumberFormatException e) {
			return "";
		}
		return default_time_to_live;
	}

	private void createRollupCFs() {

		String statement = CREATE_TABLE_IF_NOT_EXISTS + getCFName(MEASURE_STATS_CF)
				+ " (sn varchar, key varchar, own varchar, freq float, first timestamp, last timestamp, crt timestamp, upd timestamp, run boolean, rec bigint, lbl varchar, qual int, since timestamp, PRIMARY KEY ((sn), key) ) " + WITH_CACHING_ALL; 
		executeQuery(statement);
		
		statement = CREATE_INDEX_IF_NOT_EXISTS + " idx_own_" + MEASURE_STATS_CF + " on " + getCFName(MEASURE_STATS_CF)
				+ " (own);";
		executeQuery(statement);

		statement = CREATE_TABLE_IF_NOT_EXISTS
				+ getCFName(ROLL_UP_QUEUE)
				+ " (sn varchar, key varchar, first timestamp, last timestamp, PRIMARY KEY ((sn), key, first) ) WITH DEFAULT_TIME_TO_LIVE = 86400 AND GC_GRACE_SECONDS = 3600";
		executeQuery(statement);

		statement = CREATE_TABLE_IF_NOT_EXISTS
				+ getCFName(ROLL_UP_FEED_LOCK_CF)
				+ " (key varchar, PRIMARY KEY (key) ) WITH DEFAULT_TIME_TO_LIVE = 3600 AND GC_GRACE_SECONDS = 3600";
		executeQuery(statement);

		statement = CREATE_TABLE_IF_NOT_EXISTS
				+ getCFName(ROLL_UP_SINK_LOCK_CF)
				+ " (key varchar, PRIMARY KEY (key) ) WITH DEFAULT_TIME_TO_LIVE = 3600 AND GC_GRACE_SECONDS = 3600";
		executeQuery(statement);
		

		
		
		createRollupCF(ROLL_UP_MIN15_CF, MAX_TTL, WEEK_TW_COMPACTION_CLASS);
		createRollupCF(ROLL_UP_H1_CF, MAX_TTL, "");
		createRollupCF(ROLL_UP_D1_CF, MAX_TTL,"");
		createRollupCF(ROLL_UP_W1_CF, MAX_TTL,"");
		createRollupCF(ROLL_UP_M1_CF, MAX_TTL,"");
	}

	private void createRollupCF(String columnFamily, int ttl, String compaction) {
		String statement = CREATE_TABLE_IF_NOT_EXISTS + getCFName(columnFamily)
				+ " (key varchar, ts timestamp, errts timestamp, mints timestamp, maxts timestamp, val float, minval float, maxval float, err varchar, rec bigint, count int, PRIMARY KEY ((key), ts)) "
				+ " WITH DEFAULT_TIME_TO_LIVE = %d %s";
		statement = String.format(statement, ttl, compaction);
		executeQuery(statement);
	}

	private void createFeedCFs() {
		String statement = CREATE_TABLE_IF_NOT_EXISTS + getCFName(DATASINKS_CF)
				+ " (sn varchar, msn varchar, lbl varchar, own varchar, wak varchar, st varchar, tz varchar, prt varchar, bl int, im int, al boolean, lc timestamp, lr timestamp, rec bigint, cks varchar, pub boolean, trc boolean, aca boolean, PRIMARY KEY (sn)) " + WITH_CACHING_ALL;
		executeQuery(statement);
		statement = CREATE_TABLE_IF_NOT_EXISTS + getCFName(FEEDS_CF)
				+ " (sn varchar, key varchar, pid varchar, mu varchar, lbl varchar, qual int, act boolean, sel boolean, offset float, scale float, val float, aval float, ts timestamp, al boolean, since timestamp, ttl int, mini varchar, err varchar, typ varchar,  PRIMARY KEY ((sn), key)) " + WITH_CACHING_ALL;;
		executeQuery(statement);
	}

	private void createAuthCFs() {
		int ttl = 24 * 60 * 60;
		String statement = CREATE_TABLE_IF_NOT_EXISTS + getCFName(SESSION_AUTHENTICATION)
		+ " (user varchar, tkn varchar, role varchar, exp timestamp, PRIMARY KEY ((user), tkn)) WITH DEFAULT_TIME_TO_LIVE = %d ";
		statement = String.format(statement, ttl);
		executeQuery(statement);
		statement = CREATE_TABLE_IF_NOT_EXISTS + getCFName(SESSION_AUTHORIZATION)
		+ " (tkn varchar, grnt varchar, PRIMARY KEY ((tkn), grnt)) WITH DEFAULT_TIME_TO_LIVE = %d ";
		statement = String.format(statement, ttl);
		executeQuery(statement);
	}

	
	private void createFeedAlarmCFs() {
		String statement = CREATE_TABLE_IF_NOT_EXISTS + getCFName(FEEDALARMS_CF_NAME)
				+ " (sn varchar, key varchar, act boolean, st varchar, val float, ts timestamp, upd timestamp, dly boolean,  rpt boolean, ths float, PRIMARY KEY ((sn), key)) " + WITH_CACHING_ALL;;
		executeQuery(statement);

		statement = CREATE_TABLE_IF_NOT_EXISTS + getCFName(FEEDALARMTHRESHOLDS_CF_NAME)
		+ " (sn varchar,  key varchar,  armed boolean,  ntf boolean,  prt varchar,  lowlow float,  low float,  high float,  highhigh float,  dlymins int,  rptmins int,  PRIMARY KEY ((sn), key)) " + WITH_CACHING_ALL; ;
		executeQuery(statement);
		
		statement = CREATE_TABLE_IF_NOT_EXISTS + getCFName(FEEDALARMEVENTS_CF_NAME)
		+ " (sn varchar, key varchar, ts timestamp, crt timestamp, ntf boolean, st varchar, val float, ths float, op varchar, mbrs varchar, PRIMARY KEY ((sn), crt, key)) WITH CLUSTERING ORDER BY (crt DESC) AND DEFAULT_TIME_TO_LIVE = %d ";

		statement = String.format(statement, MAX_TTL);
		executeQuery(statement);
		
		statement = CREATE_INDEX_IF_NOT_EXISTS + " idx_armed_" + FEEDALARMTHRESHOLDS_CF_NAME + " on " + getCFName(FEEDALARMTHRESHOLDS_CF_NAME)
		+ " (armed);";
		executeQuery(statement);

		statement = CREATE_INDEX_IF_NOT_EXISTS + " idx_act_" + FEEDALARMS_CF_NAME + " on " + getCFName(FEEDALARMS_CF_NAME)
		+ " (act);";
		executeQuery(statement);

		statement = CREATE_INDEX_IF_NOT_EXISTS + " idx_dly_" + FEEDALARMS_CF_NAME + " on " + getCFName(FEEDALARMS_CF_NAME)
		+ " (dly);";
		
		executeQuery(statement);
		statement = CREATE_INDEX_IF_NOT_EXISTS + " idx_rpt_" + FEEDALARMS_CF_NAME + " on " + getCFName(FEEDALARMS_CF_NAME)
		+ " (rpt);";
		executeQuery(statement);
		
	}
	
	private void createConfigurationCFs() {
		String statement = CREATE_TABLE_IF_NOT_EXISTS + getCFName(CONFIGURATION_REGISTRY_CF_NAME)
				+ " (sn varchar, own boolean, rev int, id varchar, sect varchar, grp varchar, lbl varchar, perm varchar, tpc varchar, val float, maxval float, minval float, ts timestamp, PRIMARY KEY ((sn), own, id)) " + WITH_CACHING_ALL;;
		executeQuery(statement);

	}
	


	private void executeQuery(String query) {
		if (session != null) {
			try {
				PreparedStatement stmt = session.prepare(query);
				session.execute(stmt.bind().setConsistencyLevel(ConsistencyLevel.ALL));
				logger.debug(query);
			} catch (DriverException e) {
				logger.error(query, e);
			}
		}
	}

//	private void createKS() {
//		String query = "CREATE KEYSPACE IF NOT EXISTS %s WITH REPLICATION = %s ";
//		query = String.format(query, getKeySpace(), getReplication());
//		PreparedStatement stmt = client.getSession().prepare(query);
//		client.getSession().execute(stmt.bind().setConsistencyLevel(ConsistencyLevel.ALL));
//		logger.debug(query);
//	}
	



//	private String getCassandraVersion() {
//		String version = "offline";
//		try {
//			String query = "select release_version from system.local where key = 'local'";
//			PreparedStatement ps = client.getSession().prepare(query);
//			ResultSet result = client.getSession().execute(ps.bind());
//			Row row = result.one();
//			if (row != null) {
//				version = row.getString("release_version");
//			}
//		} catch (Exception ex) {
//		} finally {
//		}
//		return version;
//	}

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			if (session == null) {
				logger.error("session is null, unable to initialize keyspace " + getKeySpace());
				return;
			}
			
			/*
			 log4j.logger.com.datastax.driver.core.QueryLogger.SLOW=DEBUG
log4j.logger.com.datastax.driver.core.QueryLogger.NORMAL=DEBUG
			QueryLogger queryLogger = QueryLogger.builder().withConstantThreshold(1000)
				    .withMaxQueryStringLength(512)
				.build();
			session.getCluster().register(queryLogger);
			 */
			
			
			
			String clusterRole = System.getProperty(Constants.CLUSTER_ROLE, "master");
			boolean isMaster = clusterRole.trim().equalsIgnoreCase("master");

			if (isMaster) {
				logger.info("Initializing Cassandra Column Families ");
				createCFs();
			}
		} catch (Exception e) {
			logger.error("afterPropertiesSet", e);
		}
	}

}
