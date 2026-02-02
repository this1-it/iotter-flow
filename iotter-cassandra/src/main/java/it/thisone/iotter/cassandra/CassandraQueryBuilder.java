package it.thisone.iotter.cassandra;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;

import it.thisone.iotter.config.Constants;

public abstract class CassandraQueryBuilder implements CassandraConstants {

	public static final ConsistencyLevel DELETE_CONSISTENCY_LEVEL = ConsistencyLevel.LOCAL_QUORUM;
		private static Logger logger = LoggerFactory.getLogger(CassandraQueryBuilder.class);


//	public static Statement deleteIndexes(String uid) {
//		Delete stmt = QueryBuilder.delete().from(CassandraClient.getKeySpace(), MEASURES_IDX_TMP_CF);
//		stmt.where(eq(UID, uid));
//		return stmt;
//	}

//	public static Statement deleteTimestamps(String uid) {
//		Delete stmt = QueryBuilder.delete().from(CassandraClient.getKeySpace(), MEASURES_TS_TMP_CF);
//		stmt.where(eq(UID, uid));
//		return stmt;
//	}



//	public static Statement prepareInsertMeasureTimestamp(String uid, Date ts, int ttl) {
//		Insert stmt = QueryBuilder.insertInto(CassandraClient.getKeySpace(), MEASURES_TS_TMP_CF);
//		stmt.value(UID, uid);
//		stmt.value(TS, ts);
//		stmt.using(QueryBuilder.ttl(ttl));
//		return stmt;
//	}
//
//	public static Statement prepareInsertMeasureIndex(String uid, int id, Date ts) {
//		Insert stmt = QueryBuilder.insertInto(CassandraClient.getKeySpace(), MEASURES_IDX_TMP_CF);
//		stmt.value(UID, uid);
//		stmt.value(ID, id);
//		stmt.value(TS, ts);
//		return stmt;
//	}

	public static Statement<?> deleteKey(String columnFamily, String key) {
		String query = String.format("DELETE FROM %s.%s WHERE %s = ?", CassandraClient.getKeySpace(), columnFamily,
				KEY);
		return simpleStatement(query, DELETE_CONSISTENCY_LEVEL, key);
	}
	
	public static String getQueryString(Statement<?> statement) {
    	if (statement instanceof SimpleStatement) {
    		return ((SimpleStatement) statement).getQuery();
    	} else if (statement instanceof BoundStatement) {
    		return ((BoundStatement) statement).getPreparedStatement().getQuery();
    	} else if (statement instanceof BatchStatement) {
    		BatchStatement batch = (BatchStatement) statement;
    		if (batch.size() > 0) {
    			Statement<?> first = (Statement<?>) batch.iterator().next();
    			return String.format("[%d] %s", batch.size(), getQueryString(first));
    		}
    	}
    	return statement.getClass().getName();
    }

	public static Date getDate(Row row, String column) {
		Instant instant = row.getInstant(column);
		return instant == null ? null : Date.from(instant);
	}

	public static Date getDate(Row row, int index) {
		Instant instant = row.getInstant(index);
		return instant == null ? null : Date.from(instant);
	}

	public static String placeholders(int size) {
		if (size <= 0) {
			return "?";
		}
		StringJoiner joiner = new StringJoiner(", ");
		for (int i = 0; i < size; i++) {
			joiner.add("?");
		}
		return joiner.toString();
	}

	public static SimpleStatement simpleStatement(String query, ConsistencyLevel consistency, Object... values) {
		//logger.error(query);
		SimpleStatement stmt;
		if (values == null || values.length == 0) {
			stmt = SimpleStatement.newInstance(query);
		} else {
			stmt = SimpleStatement.newInstance(query, values);
		}
		if (consistency == null) {
			return stmt;
		}
		return stmt.setConsistencyLevel(consistency);
	}

}
