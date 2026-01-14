package it.thisone.iotter.quartz;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.DriverException;
import com.google.common.collect.Range;

import it.thisone.iotter.cassandra.CassandraFeeds;
import it.thisone.iotter.cassandra.CassandraMeasures;
import it.thisone.iotter.cassandra.CassandraRollup;
import it.thisone.iotter.cassandra.InterpolationUtils;
import it.thisone.iotter.cassandra.model.DataSink;
import it.thisone.iotter.cassandra.model.Feed;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.exporter.CDataPoint;

// End Job : datasinks 516 processed in  in 22:12:57.527 [HH:mm:ss.SSS]

@Service
@Lazy
@DisallowConcurrentExecution
public class RollupCronJob implements Job, Serializable {

	private static Logger logger = LoggerFactory.getLogger(Constants.RollUp.ROLL_UP_LOG4J_CATEGORY);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String ROLL_UP = "roll_up-job";
	public static final String ROLL_UP_GROUP = "roll_up";
	public static final String ROLL_UP_CRON = "0 0 0/6 * * ?";


	@Autowired
	private CassandraFeeds cassandraFeeds;

	@Autowired
	private CassandraRollup cassandraRollup;
	
	@Autowired
	private CassandraMeasures cassandraMeasures;

	@Autowired
	private CacheManager cacheManager;

	@Override
	public void execute(final JobExecutionContext ctx) throws JobExecutionException {

		long duration = System.currentTimeMillis();
		String format = "dd/MM/yy HH:mm:ss.SSS";
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		Cache ticksCache = cacheManager.getCache(Constants.Cache.TICKS);
		int cnt = 0;
		int total = 0;
		int size = 0;
		try {
			ticksCache.clear();
			List<DataSink> list = cassandraFeeds.getDataSinks();
			size = list.size();
			list.stream().sorted(
					Comparator.comparing(DataSink::getLastRollup, Comparator.nullsFirst(Comparator.naturalOrder())));
			logger.info("Start Job {} datasinks {} found", ctx.getJobDetail(), size);
			for (DataSink item : list) {
				String sn = item.getSerial();
				Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				calendar.setTime(new Date());
				calendar.add(Calendar.DATE, -1);
				Date lastRollup = calendar.getTime();

				if (item.getLastContact() == null) {
					logger.info("{} Rollup skipped, device last contact is null", sn);
					size--;
					continue;
				}

				if (item.getLastRollup() != null //
						&& item.getLastRollup().after(item.getLastContact()) //
				) {
					logger.info("{} Rollup skipped, device seems inactive ", sn);
					size--;
					continue;
				}

				if (item.getLastRollup() == null) {
					item.setLastRollup(lastRollup);
				}

				try {
					List<Feed> feeds = cassandraFeeds.getFeeds(sn, null);
					if (!feeds.isEmpty()) {
						String lockId = String.format("%s-%s", sn, "rollup");
						if (cassandraRollup.lockSink(lockId, 1800)) {
							cnt++;
							Range<Date> interval = Range.closedOpen(item.getLastRollup(), new Date());
							String intervalString = sdf.format(interval.lowerEndpoint()) + " - "
									+ sdf.format(interval.upperEndpoint());
							logger.info("{} Rollup started {}/{} interval [{}]", sn, cnt, size, intervalString);
							item.setLastRollup(new Date());
							long elapsed = System.currentTimeMillis();
							int active = 0;
							Date lower = interval.lowerEndpoint();
							Date upper = interval.upperEndpoint();

							for (Feed feed : feeds) {
								try {
									if (feed.isActive() && feed.hasLastValue()) {
										active++;
										logger.debug("{} Rollup processing feed {}/{} {} ", sn, active, feeds.size(),
												feed.getKey());
										Range<Date> done = cassandraRollup.writeRollUp(feed, item.getOwner(), interval,
												false);
										if (done != null) {
											if (done.lowerEndpoint().before(lower)) {
												lower = done.lowerEndpoint();
											}
										}

									}
								} catch (DriverException e) {
									logger.error(sn, e);
								}
							}

							long records = cassandraRollup.countRollupStats(sn);
							item.setRecords(records);

							cassandraFeeds.updateOnLastRollup(item);
							cassandraRollup.unlockSink(lockId);
							elapsed = System.currentTimeMillis() - elapsed;
							intervalString = sdf.format(lower) + " - " + sdf.format(upper);

							logger.info("{} Rollup completed: {} feeds, processed in {}, interval [{}]", sn, active,
									InterpolationUtils.elapsed(elapsed), intervalString);
							total = total + active;

							ensureFullMeasuresSet(sn, feeds);
							
							
						} else {
							logger.info("{} Rollup skipped, cannot acquire lock", sn);
						}
					} else {
						cassandraFeeds.updateOnLastRollup(item);
						logger.info("{} Rollup skipped, device without feeds ", sn);
						size--;
					}

				} catch (Throwable e) {
					logger.error(ctx.getJobDetail().toString(), e);
				}

			}
		} catch (Throwable e) {
			logger.error(ctx.getJobDetail().toString(), e);
		}

		ticksCache.clear();

		duration = System.currentTimeMillis() - duration;
		long feedTime = total > 0 ? (duration / total) : 0;
		logger.info("End Job : datasinks {} processed in {}, feeds {} [{}] ", cnt, InterpolationUtils.elapsed(duration),
				total, feedTime);
	}

	public void ensureFullMeasuresSet(String sn, List<Feed> feeds) {
		List<CDataPoint> values = new ArrayList<>();
		for (Feed feed : feeds) {
			if (feed.isActive() && feed.hasLastValue()) {
				CDataPoint dp = new CDataPoint();
				dp.setValue(feed.getValue());
				dp.setId(feed.getRegisterId());
				values.add(dp);
			}
		}
		if (!values.isEmpty()) {
			cassandraMeasures.insertMeasureSet(sn, new Date(), values);
		}
		
	}

}
