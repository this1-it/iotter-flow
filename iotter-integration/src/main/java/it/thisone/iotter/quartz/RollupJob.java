package it.thisone.iotter.quartz;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.google.common.collect.Range;

import it.thisone.iotter.cassandra.CassandraFeeds;
import it.thisone.iotter.cassandra.CassandraRollup;
import it.thisone.iotter.cassandra.InterpolationUtils;
import it.thisone.iotter.cassandra.model.DataSink;
import it.thisone.iotter.cassandra.model.Feed;
import it.thisone.iotter.cassandra.model.MeasureAggregation;
import it.thisone.iotter.config.Constants;

@Service
@DisallowConcurrentExecution
public class RollupJob implements InterruptableJob, Serializable {


	public static final String ENABLE_BREAK = "enableBreak";

	public static final String MEASURES = "measures";

	public static final String OWNER = "owner";
	
	public static final String DEVICE = "serial";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(Constants.RollUp.ROLL_UP_LOG4J_CATEGORY);

	@Autowired
	private CassandraRollup cassandraRollup;
	
	@Autowired
	private CassandraFeeds cassandraFeeds;

	
	@Autowired
	private CacheManager cacheManager;

	@Override
	@SuppressWarnings("unchecked")
	public void execute(JobExecutionContext context) throws JobExecutionException {
		List<MeasureAggregation> measures = (List<MeasureAggregation>) context.getTrigger().getJobDataMap()
				.get(MEASURES);
		Boolean enableBreak = (Boolean) context.getTrigger().getJobDataMap().get(ENABLE_BREAK);
		String owner = (String) context.getTrigger().getJobDataMap().get(OWNER);
		String serial = (String) context.getTrigger().getJobDataMap().get(DEVICE);
		JobKey key = context.getTrigger().getJobKey();

		Cache cache = cacheManager.getCache(Constants.Cache.TICKS);
		cache.clear();	
		
		
		String lockId = String.format("%s-%s", serial,"rollup");
		if (!cassandraRollup.lockSink(lockId, 900)) {
			logger.info("{} Rollup Job locked", key);
			return;
		}
		long elapsed = System.currentTimeMillis();
		int size = 0;
		for (MeasureAggregation measure : measures) {
			try {
				Feed feed = cassandraFeeds.getFeed(measure.getSerial(), measure.getKey());
				if (feed != null && feed.isActive() && feed.hasLastValue()) {
					Range<Date> done = cassandraRollup.writeRollUp(feed, owner, measure.getInterval(), enableBreak);
					if (done != null)
						size++;					
				}


			} catch (Throwable e) {
				logger.error("Rollup Job " + key, e);
			}
		}
		

		if (size > 0) {
			DataSink item = new DataSink(serial);
			item.setLastRollup(new Date());
			long records = cassandraRollup.countRollupStats(serial);
			item.setRecords(records);
			cassandraFeeds.updateOnLastRollup(item);
			elapsed = System.currentTimeMillis() - elapsed;
			long feedTime = elapsed / size;

			logger.info("{} Rollup Job completed: {} feeds processed in {} [{}]", serial, size, InterpolationUtils.elapsed(elapsed), feedTime);
		} else {
			logger.info("{} Rollup Job skipped", serial);
		}
		
		cassandraRollup.unlockSink(lockId);
		cache.clear();

	}

	@Override
	public void interrupt() throws UnableToInterruptJobException {
		logger.info("Rollup Job interrupted");
	}

}
