package it.thisone.iotter.cassandra;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.DriverException;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BatchableStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.servererrors.InvalidQueryException;

import it.thisone.iotter.cassandra.model.DataSink;
import it.thisone.iotter.cassandra.model.Feed;
import it.thisone.iotter.cassandra.model.FeedsWrapper;
import it.thisone.iotter.cassandra.model.IFeedKey;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.config.Constants;

@Service
public class CassandraFeeds extends FeedsQueryBuilder implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7303200826188585920L;
	private static final int FEED_UPDATE_BATCH_SIZE = 16;
	private static final int FEED_READ_CHUNK_SIZE = 16;

	private static Logger logger = LoggerFactory.getLogger(CassandraFeeds.class);

	@Autowired
	private CassandraClient client;



	/**
	 * remove datasink and feeds
	 * 
	 * @param item
	 */
	public void delete(String sn) {
		try {
			Statement stmt = prepareDeleteDataSink(sn);
			client.executeWithRetry(stmt);
			stmt = prepareDeleteFeeds(sn);
			client.executeWithRetry(stmt);
		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to delete datasink for id " + sn, e);
		}
	}

	@Deprecated
	public void insert(DataSink item) {
		try {
			{
				Statement stmt = prepareInsertDataSink(item, client.writeConsistencyLevel());
				client.executeWithRetry(stmt);
			}
		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to insert DataSink for id " + item.getSerial(), e);
		}
	}

	public void upsert(DataSink item) {
		Statement stmt = prepareUpdateDataSink(item, client.writeConsistencyLevel());
		client.executeWithRetry(stmt);
	}

	public void updateOnLastContact(DataSink item) {
		try {
			Statement stmt = prepareUpdateDataSinkOnLastContact(item, client.writeConsistencyLevel());
			client.executeWithRetry(stmt);

		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to update DataSink for id " + item.getSerial(), e);
		}
	}
	
	public void updateLastContact(String serial, Date now) {
		try {
			Statement stmt = prepareUpdateLastContact(serial, now, client.writeConsistencyLevel());
			client.executeWithRetry(stmt);
			//logger.debug("updateLastContact DataSink {} {}",serial,now);
		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to update DataSink for id " + serial, e);
		}
	}

	public DataSink getDataSink(String serial) {
		DataSink item = null;
		try {
			Statement stmt = prepareSelectDataSink(serial, client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = rs.one();
				item = fillDataSink(row);
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving DataSink " + serial, e);
		}
		return item;
	}
	


	public void update(Feed item) {
		try {
			Statement stmt = prepareUpdateFeed(item, client.writeConsistencyLevel());
			client.executeWithRetry(stmt);
		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to insert Feed for key " + item.getKey(), e);
		}
	}

	public Feed getFeed(String serial, String key) {
		Feed item = null;
		try {
			Statement stmt = prepareSelectFeed(serial, key, client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = rs.one();
				item = fillFeed(row);
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving  Feed for key " + key, e);
		}
		return item;
	}
	public Feed getFeedValue(String serial, String key) {
		Feed item = null;
		try {
			Statement stmt = prepareSelectFeedValue(serial, key, client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = rs.one();
				item = fillFeedValue(serial, row);
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving  Feed for key " + key, e);
		}
		return item;
	}

	
	/*
	 * not suitable for alarms
	 * 
	 */
	public List<Feed> getFeedsValues(String serial, Set<String> keys) {
		List<Feed> items = new ArrayList<Feed>();
		if (keys != null && keys.isEmpty()) {
			return items;
		}
		try {
			if (keys == null) {
				Statement stmt = prepareSelectFeedsValues(serial, null, client.readConsistencyLevel());
				ResultSet rs = client.executeWithRetry(stmt);
				Iterator<Row> iter = rs.iterator();
				while (iter.hasNext()) {
					Row row = iter.next();
					items.add(fillFeedValue(serial, row));
				}				
			}
			else {
				final AtomicInteger counter = new AtomicInteger();
				final Collection<List<String>> result = keys.stream()
				    .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / FEED_READ_CHUNK_SIZE))
				    .values();
				for (List<String> list : result) {
					Statement stmt = prepareSelectFeedsValues(serial, list, client.readConsistencyLevel());
					ResultSet rs = client.executeWithRetry(stmt);
					Iterator<Row> iter = rs.iterator();
					while (iter.hasNext()) {
						Row row = iter.next();
						items.add(fillFeedValue(serial, row));
					}
				}

			}

		} catch (DriverException e) {
			logger.error("cassandra not available retrieving  Feeds for serial " + serial, e);
		}
		return items;
	}
	
	
	public List<Feed> getFeeds(String serial, Set<String> keys) {
		List<Feed> items = new ArrayList<Feed>();
		if (keys != null && keys.isEmpty()) {
			return items;
		}
		try {
			if (keys == null) {
				Statement stmt = prepareSelectFeeds(serial, null, client.readConsistencyLevel());
				ResultSet rs = client.executeWithRetry(stmt);
				Iterator<Row> iter = rs.iterator();
				while (iter.hasNext()) {
					Row row = iter.next();
					items.add(fillFeed(row));
				}				
			}
			else {
				final AtomicInteger counter = new AtomicInteger();
				final Collection<List<String>> result = keys.stream()
				    .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / FEED_READ_CHUNK_SIZE))
				    .values();
				for (List<String> list : result) {
					Statement stmt = prepareSelectFeeds(serial, list, client.readConsistencyLevel());
					ResultSet rs = client.executeWithRetry(stmt);
					Iterator<Row> iter = rs.iterator();
					while (iter.hasNext()) {
						Row row = iter.next();
						items.add(fillFeed(row));
					}				
					
				}
//				for (String key : keys) {
//					Feed feed = getFeed(serial, key);
//					if (feed != null) {
//						items.add(feed);
//					}
//				}
			}

		} catch (DriverException e) {
			logger.error("cassandra not available retrieving  Feeds for serial " + serial, e);
		}
		return items;
	}

	
	
	

	

	public void updateOnLastValue(Feed item) {
		try {
			Statement stmt = prepareUpdateFeedLastValue(item, client.writeConsistencyLevel());
			client.executeWithRetry(stmt);
		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to update Feed for key " + item.getKey(), e);
		}
	}

	public void updateOnAlarmArmed(Feed item) {
		try {
			Statement stmt = prepareUpdateFeedAlarmed(item, client.writeConsistencyLevel());
			client.executeWithRetry(stmt);
		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to update Feed for key " + item.getKey(), e);
		}
	}

	public void updateOnLastRollup(DataSink item) {
		try {
			Statement stmt = prepareUpdateDataSinkOnLastRollup(item, client.writeConsistencyLevel());
			client.executeWithRetry(stmt);
		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to update DataSink for id " + item.getSerial(), e);
		}
	}

	public List<DataSink> getDataSinks() {
		List<DataSink> items = new ArrayList<DataSink>();
		try {
			Statement stmt = prepareSelectDataSinks(client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
				items.add(fillDataSink(row));
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving DataSink ", e);
		}
		return items;
	}

	public List<String> getDataSinkIds() {
		List<String> items = new ArrayList<String>();
		try {
			Statement stmt = prepareSelectDataSinkIds(client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
				items.add(row.getString(SN));
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving DataSink ", e);
		}
		return items;
	}

	public long getRecords(String serial) {
		long records = 0;
		try {
			Statement stmt = prepareSelectDataSinkRecords(serial, client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = rs.one();
				records = row.getLong(0);
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving DataSink " + serial, e);
		}
		return records;
	}

	public Date getSince(String serial) {
		List<Feed> feeds = getFeeds(serial, null);
		Date date = null;
		for (Feed feed : feeds) {
			Date since = feed.getSince();
			if (since != null) {
				if (date == null) {
					date = since;
				}
				else {
					if (since.before(date)) {
						date = since;
					}
				}				
			}
		}
		return date;
	}

	
	public Date getLastContact(String serial) {
		Date date = null;
		try {
			Statement stmt = prepareSelectDataSinkLastContact(serial, client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = rs.one();
				date = CassandraQueryBuilder.getDate(row, 0);
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving DataSink " + serial, e);
		}
		return date;
	}
	
	public String getCheckSum(String serial) {
		String checkSum = null;
		try {
			Statement stmt = prepareSelectDataSinkCheckSum(serial, client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = rs.one();
				checkSum = row.getString(0);
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving DataSink " + serial, e);
		}
		return checkSum;
	}

	public Date getLastRollup(String serial) {
		Date date = null;
		try {
			Statement stmt = prepareSelectDataSinkLastRollup(serial, client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = rs.one();
				date = CassandraQueryBuilder.getDate(row, 0);
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving DataSink " + serial, e);
		}
		return date;
	}

	public void updateOnSelected(Feed item) {
		try {
			Statement stmt = prepareUpdateFeedSelected(item, client.writeConsistencyLevel());
			client.executeWithRetry(stmt);
		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to update Feed for key " + item.getKey(), e);
		}
	}

	public void updateOnActive(Feed item) {
		try {
			Statement stmt = prepareUpdateFeedActive(item, client.writeConsistencyLevel());
			client.executeWithRetry(stmt);
		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to update Feed for key " + item.getKey(), e);
		}
	}

	public Map<IFeedKey, MeasureRaw> lastMeasures(List<IFeedKey> feeds) {
		Map<IFeedKey, MeasureRaw> measures = new HashMap<>();
		if (feeds.isEmpty()) {
			return measures;
		}
		Map<String, IFeedKey> keys = new HashMap<>();
		String sn = null;
		boolean multiple = false;
		for (IFeedKey feed : feeds) {
			keys.put(feed.getKey(), feed);
			if (sn == null) {
				sn = feed.getSerial();
			} else {
				if (!sn.equals(feed.getSerial())) {
					multiple = true;
					break;
				}
			}
		}
		if (multiple) {
			for (IFeedKey feed : feeds) {
				MeasureRaw measure = getLastMeasure(feed);
				if (measure != null) {
					measures.put(feed, measure);
				}
			}
		} else {
			try {
				Set<String> set = new HashSet<>(keys.keySet());
				String keyspace = client.getSession().getKeyspace()
						.map(id -> id.asInternal())
						.orElse(CassandraClient.getKeySpace());
				Statement stmt = prepareSelectLastMeasures(sn, set, keyspace, client.readConsistencyLevel());
				ResultSet rs = client.executeWithRetry(stmt);
				Iterator<Row> iter = rs.iterator();
				while (iter.hasNext()) {
					Row row = iter.next();
					MeasureRaw measure = new MeasureRaw(row.getString(KEY), CassandraQueryBuilder.getDate(row, TS));
					measure.setValue(row.getFloat(VAL));
					measures.put(keys.get(measure.getKey()), measure);
				}
			} catch (InvalidQueryException e) {
				logger.error("cassandra not available retrieving lastMeasures for sn " + sn, e);
			}
		}
		return measures;
	}

	public MeasureRaw getLastMeasure(IFeedKey feed) {
		MeasureRaw item = null;
		try {
			String keyspace = client.getSession().getKeyspace()
					.map(id -> id.asInternal())
					.orElse(CassandraClient.getKeySpace());
			Statement stmt = prepareSelectLastMeasure(feed.getSerial(), feed.getKey(), keyspace,
					client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = rs.one();
				item = new MeasureRaw(feed.getKey(), CassandraQueryBuilder.getDate(row, TS));
				item.setValue(row.getFloat(VAL));
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving  Feed for key " + feed.getKey(), e);
		}
		return item;
	}

	public void updateFeedBatch(List<Feed> items) {
		if (items.isEmpty()) return;
		BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED)
				.setConsistencyLevel(client.writeConsistencyLevel());
		for (Feed item : items) {
			BatchableStatement<?> stmt = (BatchableStatement<?>) prepareUpdateFeed(item, client.writeConsistencyLevel());
			batch = batch.add(stmt);
			if (batch.size() > FEED_UPDATE_BATCH_SIZE) {
				client.executeWithRetry(batch);
				batch = BatchStatement.newInstance(BatchType.UNLOGGED)
						.setConsistencyLevel(client.writeConsistencyLevel());
			}
		}
		if (batch.size() > 0) {
			client.executeWithRetry(batch);
		}
	}
	
	public void updateLastValuesBatch(List<Feed> feeds) {
		BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED)
				.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
		for (Feed feed : feeds) {
			if (feed.isChanged() && feed.hasLastValue()) {
				BatchableStatement<?> stmt = (BatchableStatement<?>) prepareUpdateFeedLastValue(feed,
						client.readConsistencyLevel());
				batch = batch.add(stmt);
			}
			if (batch.size() > FEED_UPDATE_BATCH_SIZE) {
				client.executeWithRetry(batch);
				batch = BatchStatement.newInstance(BatchType.UNLOGGED)
						.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
			}
		}
		if (batch.size() > 0) {
			client.executeWithRetry(batch);
		}
	}
	
	
	// // Bug #2035
	public void updateDataSinkActiveAlarms(String serial, boolean activeAlarms) {
		try {
			Statement stmt = prepareUpdateActiveAlarms(serial, activeAlarms, client.writeConsistencyLevel());
			client.executeWithRetry(stmt);
		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to update DataSink for id " + serial, e);
		}
		
	}

	



}
