package it.thisone.iotter.cassandra;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.DriverException;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.google.common.collect.Range;
import com.google.common.io.BaseEncoding;

import it.thisone.iotter.cassandra.model.ExportQuery;
import it.thisone.iotter.cassandra.model.ExportRow;
import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.IMeasureExporter;
import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.cassandra.model.MeasureDateComparator;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.cassandra.model.MeasureStats;
import it.thisone.iotter.concurrent.RateLimitControl;
import it.thisone.iotter.config.Constants;

@Service
public class CassandraExport implements Serializable, IMeasureExporter, CassandraConstants {

	private static final int maxRequests = 12;
	private static final TimeUnit timePeriod = TimeUnit.MINUTES;


	@Autowired
	private MeasuresQueries measuresQueries;

	@Autowired
	private RollupQueries rollupQueries;

	@Autowired
	private MeasuresTimestampsQueries timestampsQueries;
	


	@Autowired
	private CassandraClient client;

	private static final long serialVersionUID = -2122461156912967548L;

	private static Logger logger = LoggerFactory.getLogger(Constants.Exporter.LOG4J_CATEGORY);

	private static final int EXPORT_TTL = 30 * 60;

	@Override
	public Date getTimeStamp(String qid, int id) {
		return timestampsQueries.getTimeStamp(qid, id);
	}

	@Override
	public List<Date> getTimeStamps(String qid, int start, int end) {
		return timestampsQueries.getTimeStamps(qid, start, end);
	}

	@Override
	public int writeTimeStamps(String qid, List<FeedKey> feeds, Date from, Date to, Interpolation interpolation,
			boolean ascending, Set<Range<Date>> validities) {
		List<Date> dates = timestampsQueries.writeTimeStamps(qid, feeds, from, to, interpolation, ascending, validities);
		return dates.size();
	}

	public List<MeasureRaw> loadMeasures(List<FeedKey> feeds, Date from, Date to, boolean ascending) {
		if (feeds.isEmpty()) {
			return new ArrayList<MeasureRaw>();
		}
		int displayPoints = 1000;
		String sn = feeds.get(0).getSerial();
		String key = feeds.get(0).getKey();
		Range<Date> interval = Range.closed(from, to);
		Interpolation interpolation = rollupQueries.interpolationChoice(sn, key, interval, displayPoints);
		return loadMeasuresWithTicks(feeds, from, to, interpolation, ascending, null);
	}

	@Override
	public List<MeasureRaw> loadMeasuresWithTicks(List<FeedKey> feeds, Date from, Date to, Interpolation interpolation,
			boolean ascending, List<Date> ticks) {
		List<MeasureRaw> items = new ArrayList<MeasureRaw>();
		if (feeds.isEmpty()) {
			return items;
		}

		Range<Date> interval = Range.closedOpen(from, to);
		int ticksSize = (ticks != null) ? ticks.size(): 0;
		logger.debug("loadMeasuresWithTicks keys:{}, interpolation:{}, ticks: {}, interval: {} ", feeds.size(), interpolation.name(), ticksSize, interval.toString());
		if (interpolation.equals(Interpolation.RAW) || interpolation.getColumnFamily() == null) {
			for (FeedKey feedKey : feeds) {
				items.addAll(measuresQueries.fetchRaw(feedKey, interval, ticks));
			}
		} else {
			items.addAll(rollupQueries.fetchRollup(feeds, from, to, interpolation, ticksSize > 0));
			items.addAll(undoneRollup(feeds, interval, interpolation, ticksSize > 0));
		}

		if (ticks != null && items.size() < ticks.size() * feeds.size()) {
			List<Date> dates = new ArrayList<>();
			for (Date date : ticks) {
				dates.add(date);
			}
			for (MeasureRaw item : items) {
				dates.remove(item.getDate());
			}
			for (Date date : dates) {
				for (FeedKey feed : feeds) {
					logger.error("adding extra tick {} {}", feed.getKey(), date);
					items.add(new MeasureRaw(feed.getKey(), date));
				}
			}
		}

		if (ascending) {
			Collections.sort(items, new MeasureDateComparator());
		} else {
			Collections.sort(items, Collections.reverseOrder(new MeasureDateComparator()));
		}

		return items;
	}


	private List<MeasureRaw> undoneRollup(List<FeedKey> feeds, Range<Date> interval, Interpolation interpolation,
			boolean aggregation) {
		long elapsed = System.currentTimeMillis();
		List<MeasureRaw> items = new ArrayList<MeasureRaw>();
		TimeZone zone = TimeZone.getTimeZone("UTC");
		for (FeedKey feed : feeds) {
			Range<Date> undoneInterval = undoneRollupInterval(interval, feed, interpolation);
			if (undoneInterval == null) {
				continue;
			}
			items.addAll(rollupQueries.rollUpVirtual(feed, feed.getQualifier(), interpolation,
					CassandraConstants.MEASURES_CF, interval, zone, aggregation));
		}
		elapsed = (System.currentTimeMillis() - elapsed) / 1000;
		logger.debug("done virtual rollup in {} secs,  keys: {}, found: {} ", elapsed, feeds.size(), items.size());
		return items;
	}

	private Range<Date> undoneRollupInterval(Range<Date> interval, FeedKey feed, Interpolation interpolation) {
		MeasureStats stats = rollupQueries.getRollupStats(feed.getSerial(), feed.getKey());
		Range<Date> dataInterval = rollupQueries.availableRollupInterval(stats, interpolation);
		Range<Date> undoneInterval = null;
		boolean existRollUpData = dataInterval != null && dataInterval.isConnected(interval);
		if (existRollUpData) {
			if (interval.upperEndpoint().after(dataInterval.upperEndpoint())) {
				undoneInterval = Range.closed(dataInterval.upperEndpoint(), interval.upperEndpoint());
				// long undoneSecs = (interval.upperEndpoint().getTime() -
				// dataInterval.upperEndpoint().getTime()) / 1000;
				// boolean useVirtual = interpolation.getSeconds() < undoneSecs;
				// logger.debug("undoneRollupInterval {} key {}",undoneInterval,feed.getKey());
			}
		} else {
			undoneInterval = interval;
		}
		return undoneInterval;
	}

	@Override
	public ExportQuery retrieveExportQuery(String qid) {
		ExportQuery item = null;
		try {
			Statement stmt = MeasuresQueryBuilder.prepareSelectExportQuery(qid, client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = rs.one();
				item = MeasuresQueryBuilder.fillExportQuery(row);
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving ExportQuery " + qid, e);
		}
		return item;

	}

	@Override
	public ExportQuery buildExportQuery(String serial, List<String> keys, long from, long to, Boolean ascending,
			String interpolation, String apiKey) {
		if (keys.isEmpty()) {
			return null;
		}

		if (ascending == null) {
			ascending = true;
		}
		if (interpolation == null) {
			interpolation = Interpolation.RAW.name();
		} else {
			try {
				interpolation = Interpolation.valueOf(interpolation).name();
			} catch (IllegalArgumentException e) {
				interpolation = Interpolation.RAW.name();
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append(serial);
		for (String key : keys) {
			sb.append(key);
		}
		sb.append(from);
		sb.append(to);
		sb.append(ascending);
		sb.append(interpolation);
		sb.append(apiKey);
		String qid = digest(sb.toString());
		ExportQuery query = this.retrieveExportQuery(qid);
		if (query != null && query.isValid()) {
			return query;
		}
		query = new ExportQuery(qid);
		Date fromDate = CassandraMeasures.toUTCDate(from * 1000);
		Date toDate = CassandraMeasures.toUTCDate(to * 1000);
		Date expires = CassandraMeasures.toUTCDate(System.currentTimeMillis() + (EXPORT_TTL - 60) * 1000);
		query.setStart(0l);
		query.setAscending(ascending);
		query.setBatchSize(MAX_BATCH_ITEMS / keys.size());
		query.setSerial(serial);
		query.setKeys(keys);
		query.setFrom(fromDate);
		query.setTo(toDate);
		query.setExpires(expires);
		query.setInterpolation(interpolation);

		return query;
	}

	private Range<Date> getBatchInterval(String uid, int index, int batchSize, boolean ascending) {
		Date from = new Date();
		Date to = new Date();
		if (ascending) {
			from = getTimeStamp(uid, index);
			to = getTimeStamp(uid, index + batchSize - 1);
			if (from == null) {
				logger.debug("invalid from {}", index);
				return null;
			}

			if (to == null) {
				to = from;
			}
		} else {
			to = getTimeStamp(uid, index);
			from = getTimeStamp(uid, index + batchSize - 1);
			if (to == null) {
				logger.debug("invalid to {}", index);
				return null;
			}
			if (from == null) {
				from = to;
			}
		}
		return Range.closed(from, to);
	}

	private List<Date> getBatchDates(String uid, int index, int batchSize) {
		int from = index;
		int to = index + batchSize - 1;
		return getTimeStamps(uid, from, to);
	}

	private String digest(String key) {
		try {
			byte[] message = key.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(message);
			byte[] digest = md.digest(message);
			StringBuilder sb = new StringBuilder(BaseEncoding.base32Hex().encode(digest));
			return sb.toString();

		} catch (Exception e) {
		}
		return key;
	}

	@Override
	public List<ExportRow> loadRows(ExportQuery query) {
		List<ExportRow> results = new ArrayList<ExportRow>();
		String serial = query.getSerial();
		Interpolation interpolation = Interpolation.valueOf(query.getInterpolation());
		int nextIndex = 0;
		List<FeedKey> keys = new ArrayList<>();
		for (int i = 0; i < query.getKeys().size(); i++) {
			String key = query.getKeys().get(i);
			keys.add(new FeedKey(serial, key));
		}

		long diff = query.getTo().getTime() - query.getFrom().getTime();
		logger.debug("loadRows start {} {} {}", query.getSerial(), diff, query.isSingleBatch());

		if (query.isSingleBatch()) {
			logger.debug("loadRows {}: using single batch ", query.getSerial());
			List<MeasureRaw> measures = loadMeasuresWithTicks(keys, query.getFrom(), query.getTo(), interpolation,
					query.isAscending(), null);
			List<Date> ticks = new ArrayList<>(measures.stream().map(t -> t.getDate()).collect(Collectors.toSet()));
			if (query.isAscending()) {
				Collections.sort(ticks);
			} else {
				Collections.sort(ticks, Collections.reverseOrder());
			}
			results = getBatchResult(measures, ticks, query.getKeys());
			query.setTotal((long) results.size());
			query.setNext(query.getTotal());
			query.setBatchSize(results.size());
		} else {
			int startIndex = query.getStart().intValue();
			if (startIndex >= query.getTotal().intValue()) {
				logger.debug("loadRows {}: query not executed, startIndex {} >=  total {}", query.getSerial(),
						startIndex, query.getTotal());
				query.setNext(-1l);
				return results;
			}
			int batchSize = query.getBatchSize();
			nextIndex = startIndex + batchSize;
			if (nextIndex >= query.getTotal().intValue()) {
				batchSize = (int) (query.getTotal() - startIndex);
			}

			logger.debug("loadRows {}: using startIndex {} and batchSize {} ", query.getSerial(), startIndex,
					batchSize);

			Range<Date> interval = getBatchInterval(query.getQid(), startIndex, batchSize, query.isAscending());
			if (interval != null) {
				Date from = interval.lowerEndpoint();
				Date to = interval.upperEndpoint();
				List<Date> ticks = getBatchDates(query.getQid(), startIndex, batchSize);
				List<MeasureRaw> measures = loadMeasuresWithTicks(keys, from, to, interpolation, query.isAscending(),
						ticks);
				// logger.debug("loadRows {}: found measures {} of {} keys in interval [{} {}]
				// ticks {} ", query.getSerial(), measures.size(), query.getKeys().size(),
				// sdf.format(from), sdf.format(to), ticks.size());
				results = getBatchResult(measures, ticks, query.getKeys());
				nextIndex = startIndex + results.size();
			} else {
				logger.debug("loadRows {}: query not executed, batch interval cannot be retrieved", query.getSerial());
				nextIndex = query.getTotal().intValue();
			}

		}

		updateExportQuery(query.getQid(), (long) nextIndex);
		query.setNext((long) nextIndex);

		logger.debug("loadRows {}: retrieved rows {} of total {}, next index {}", query.getSerial(), results.size(),
				query.getTotal(), nextIndex);

		return results;
	}

	private List<ExportRow> getBatchResult(List<MeasureRaw> measures, List<Date> ticks, List<String> keys) {
		List<ExportRow> results;
		results = Arrays.asList(new ExportRow[ticks.size()]);
		for (MeasureRaw measure : measures) {
			int pos = ticks.indexOf(measure.getDate());
			if (pos >= 0) {
				ExportRow item = results.get(pos);
				if (item == null) {
					item = new ExportRow(measure.getDate(), keys.size());
					results.set(pos, item);
				}
				int index = keys.indexOf(measure.getKey());
				if (index >= 0) {
					item.set(index, measure.getValue());
				}
			}

		}
		return results;
	}

	@Override
	public void writeTimeStamps(ExportQuery query) {
		List<FeedKey> feeds = new ArrayList<FeedKey>();
		for (String key : query.getKeys()) {
			feeds.add(new FeedKey(query.getSerial(), key));
		}
		int count = writeTimeStamps(query.getQid(), feeds, query.getFrom(), query.getTo(),
				Interpolation.valueOf(query.getInterpolation()), query.isAscending(), null);
		query.setTotal((long) count);
		updateExportQuery(query);
	}

	private void updateExportQuery(ExportQuery item) {
		try {
			Statement stmt = MeasuresQueryBuilder.prepareUpdateExportQuery(item, EXPORT_TTL,
					client.writeConsistencyLevel());
			client.executeWithRetry(stmt);
		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to insert ExportQuery for serial " + item.getSerial(), e);
		}
	}

	private void updateExportQuery(String qid, Long start) {
		try {
			Statement stmt = MeasuresQueryBuilder.prepareUpdateExportQuery(qid, start, client.writeConsistencyLevel());
			client.executeWithRetry(stmt);
		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to update ExportQuery qid " + qid, e);
		}
	}
	


}
