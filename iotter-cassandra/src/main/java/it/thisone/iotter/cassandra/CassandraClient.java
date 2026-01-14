package it.thisone.iotter.cassandra;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DriverException;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.datastax.oss.driver.api.core.servererrors.QueryConsistencyException;
import com.datastax.oss.driver.api.core.servererrors.ReadFailureException;
import com.datastax.oss.driver.api.core.servererrors.WriteFailureException;
import com.google.common.collect.Range;

import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.cassandra.model.MeasureRaw;

@Component
public class CassandraClient implements InitializingBean, DisposableBean, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2588394790215972616L;

	private static Logger logger = LoggerFactory.getLogger(CassandraClient.class);
	
	public static String KEYSPACE_NAME;
	private final int ATTEMPTS = 3;
	private final int TTL_GRACE_TIME = 3600;
	private Interpolation minimalInterpolation;
	private Integer measuresTTL;
	private Date epoch;

	private ConsistencyLevel readConsistency;

	private PreparedStatement insertMeasureRaw;
	private PreparedStatement insertMeasureTick;
	private PreparedStatement selectMeasureRaw;
	private PreparedStatement selectMeasureTick;

	@Autowired
	private CqlSession session;

	@Autowired
	@Qualifier("cassandraExecutor")
	private ThreadPoolTaskExecutor taskExecutor;

	@Autowired
	@Qualifier("cassandraProperties")
	private Properties properties;

	public CassandraClient() {
	}

	public PreparedStatement prepare(String query, ConsistencyLevel consistency) {
		SimpleStatement stmt = SimpleStatement.builder(query).setConsistencyLevel(consistency).build();
		return session.prepare(stmt);
	}

	public String getReplication() {
		return properties.getProperty("cassandra.replication");
	}

	public Interpolation getMinimalInterpolation() {
		if (minimalInterpolation != null)
			return minimalInterpolation;
		minimalInterpolation = Interpolation.MIN15;
		try {
			String value = properties.getProperty("cassandra.interpolation.minimal", "MIN15");
			minimalInterpolation = Interpolation.valueOf(value);
		} catch (IllegalArgumentException e) {
		}
		return minimalInterpolation;
	}

	public int getMeasuresTTL() {
		if (measuresTTL != null)
			return measuresTTL;
		try {
			String value = properties.getProperty("cassandra.ttl.measures", "0");
			measuresTTL = Integer.parseInt(value);
			if (measuresTTL > 0) {
				measuresTTL = measuresTTL + TTL_GRACE_TIME;
			}
		} catch (NumberFormatException e) {
		}
		return measuresTTL;
	}

	public Date getEpoch() {
		if (epoch != null)
			return epoch;

		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		if (getMeasuresTTL() > 0) {
			calendar.add(Calendar.SECOND, -1 * getMeasuresTTL());
		} else {
			calendar.add(Calendar.YEAR, -1);
		}
		epoch = calendar.getTime();
		return epoch;
	}

	public int calculateMeasuresTTL(Date date) {
		return getMeasuresTTL();
	}

	public static String getKeySpace() {
		return KEYSPACE_NAME;
	}

	public ConsistencyLevel readConsistencyLevel() {
		if (readConsistency != null)
			return readConsistency;
		try {
			readConsistency = ConsistencyLevel.LOCAL_ONE;
			int value = Integer.parseInt(properties.getProperty("cassandra.replication_factor", "1"));
			if (value > 2) {
				readConsistency = ConsistencyLevel.LOCAL_QUORUM;
			}
		} catch (NumberFormatException e) {
			readConsistency = ConsistencyLevel.LOCAL_ONE;
		}
		return readConsistency;
	}

	public ConsistencyLevel writeConsistencyLevel() {
		return ConsistencyLevel.LOCAL_QUORUM;
	}

	public String getCFName(String name) {
		return String.format("%s.%s", getKeySpace(), name);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
//		InsertMeasureCQL = "INSERT INTO " + value + " (key, ts, tr, val, err) VALUES (?, ?, ?, ?, ?); ";
//		RetrieveMeasureCQL = "SELECT ts, val, err FROM " + value + " WHERE key=? AND ts >= ? AND ts <= ? ORDER BY ts ASC;";
//		FirstMeasureCQL = "SELECT ts, val, err FROM " + value + " WHERE key=? AND ts >= ?  ORDER BY ts ASC LIMIT 1 ;";
//		LastMeasureCQL = "SELECT ts, val, err FROM " + value + " WHERE key=? AND ts <= ? ORDER BY ts DESC LIMIT 1 ;";

		// https://docs.datastax.com/en/developer/java-driver/3.1/manual/statements/prepared/
		String ttl = "";

		if (getMeasuresTTL() > 0) {
			ttl = String.format(" USING TTL %d ", getMeasuresTTL());
		}

		/*
		 * https://docs.datastax.com/en/developer/java-driver/3.1/manual/statements/
		 * prepared/
		 */

		KEYSPACE_NAME = properties.getProperty("cassandra.keyspace");

		String cfRaw = getCFName(CassandraConstants.MEASURES_CF);
		String insertRaw = "INSERT INTO %s (key, ts, tr, val, err) VALUES (?, ?, ?, ?, ?) %s;";
		insertMeasureRaw = session.prepare(String.format(insertRaw, cfRaw, ttl));

		String selectRaw = "SELECT ts, tr, val, err FROM %s WHERE key=? AND ts >= ? AND ts <= ? ";
		selectMeasureRaw = session.prepare(String.format(selectRaw, cfRaw));

		String cfTick = getCFName(CassandraConstants.MEASURE_TICKS_CF);
		String insertTick = "INSERT INTO %s (key, ts) VALUES (?, ?) %s;";

		insertMeasureTick = session.prepare(String.format(insertTick, cfTick, ttl));

		String selectTick = "SELECT ts FROM %s WHERE key=? AND ts >= ? AND ts <= ? ";
		selectMeasureTick = session.prepare(String.format(selectTick, cfTick));

	}

	public CqlSession getSession() {
		return session;
	}

	@Override
	public void destroy() throws Exception {
	}

	/**
	 * Retries after a non-timeout error during a write/read query.
	 * <p/>
	 * This happens when some of the replicas that were contacted by the coordinator
	 * replied with an error.
	 */
	public ResultSet executeWithRetry(Statement<?> statement) {
		ResultSet result = null;
		QueryConsistencyException executionException = null;
		int rTime = 0;
		while (rTime < ATTEMPTS) {
			try {
				result = session.execute(statement);
				executionException = null;
				break;
			} catch (WriteFailureException | ReadFailureException e) {
				if (rTime == ATTEMPTS - 1) {
					statement = statement.setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);
				}
				rTime++;
				executionException = e;
				try {
					int millis = ThreadLocalRandom.current().nextInt(100 * rTime, 1000);
					Thread.sleep(millis);
				} catch (InterruptedException e1) {
				}
			}
		}
		if (executionException != null) {
			throw executionException;
		}
		return result;
	}

	public ResultSet execute(Statement<?> stmt) {
		ResultSet result = session.execute(stmt);
		return result;
	}

	public Statement<?> prepareInsertMeasureTickBoundStatement(String key, Date date) {
		BoundStatement stmt = insertMeasureTick.bind(key, date);
		return stmt.setConsistencyLevel(writeConsistencyLevel());
	}

	public Statement<?> prepareInsertMeasureRawBoundStatement(MeasureRaw measure) {
		BoundStatement stmt = insertMeasureRaw.bind(measure.getKey(), measure.getDate(), measure.getReceived(),
				measure.getValue(), measure.getError());
		return stmt.setConsistencyLevel(writeConsistencyLevel());
	}

	public Statement<?> prepareSelectMeasureTickBoundStatement(String key, Range<Date> interval) {
		BoundStatement stmt = selectMeasureTick.bind(key, interval.lowerEndpoint(), interval.upperEndpoint());
		return stmt.setConsistencyLevel(readConsistencyLevel());
	}

	public Statement<?> prepareSelectMeasureRawBoundStatement(String key, Range<Date> interval) {
		BoundStatement stmt = selectMeasureRaw.bind(key, interval.lowerEndpoint(), interval.upperEndpoint());
		return stmt.setConsistencyLevel(readConsistencyLevel());
	}

	public void executeQuery(String query) {
		if (session != null) {
			try {
				PreparedStatement stmt = session.prepare(query);
				session.execute(stmt.bind().setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM));
			} catch (DriverException e) {
			}
		}
	}

	/*
	 * https://dzone.com/articles/cassandra-batch-loading-without-the-batch-the-nuan
	 * https://stackoverflow.com/questions/41049753/how-to-throttle-writes-request-
	 * to-cassandra-when-working-with-executeasync
	 */
	public void executeAsyncBatch(List<Statement<?>> batch) {
		for (Statement<?> statement : batch) {
			executeAsync(statement);
		}
	}

	private void executeAsync(Statement<?> stmt) {
		try {
			CompletionStage<AsyncResultSet> future = session.executeAsync(stmt);
			future.whenCompleteAsync((result, throwable) -> {
				if (throwable != null) {
					logger.error("executeAsync", throwable);
				}
			}, taskExecutor);
		} catch (Exception ex) {
		}
	}

	public void clusterState() {
		logger.info("Cassandra Cluster Connection Status");
		for (Node node : session.getMetadata().getNodes().values()) {
			logger.info("{} {}", node.getEndPoint(), node.getState());
		}
	}

}
