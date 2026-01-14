package it.thisone.iotter.quartz;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.thisone.iotter.cassandra.CassandraAlarms;
import it.thisone.iotter.cassandra.CassandraFeeds;
import it.thisone.iotter.cassandra.model.Feed;
import it.thisone.iotter.cassandra.model.FeedAlarmEvent;
import it.thisone.iotter.cassandra.model.FeedAlarmThresholds;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.integration.AlarmService;

@Deprecated
@Service
@DisallowConcurrentExecution
public class AlarmJob implements InterruptableJob, Serializable {

	public static final String SERIAL = "serial";
	public static final String MEASURES = "measures";

	public static final String OWNER = "owner";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(Constants.AsyncExecutor.LOG4J_CATEGORY);

	@Autowired
	private CassandraFeeds cassandraFeeds;

	@Autowired
	private CassandraAlarms cassandraAlarms;

	@Autowired
	private AlarmService service;

	@Override
	@SuppressWarnings("unchecked")
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String serial = (String) context.getTrigger().getJobDataMap().get(SERIAL);
		List<MeasureRaw> measures = (List<MeasureRaw>) context.getTrigger().getJobDataMap().get(MEASURES);
		Map<String, Feed> feeds = new HashMap<>();
		Map<String, FeedAlarmThresholds> thresholds = new HashMap<>();
		
		if (serial != null && measures != null) {
			for (MeasureRaw measure : measures) {
				logger.debug("Analize Alarm: {} ", measure.getKey());
				Feed feed = feeds.get(measure.getKey());
				if (feed == null) {
					feed = cassandraFeeds.getFeed(serial, measure.getKey());
					feeds.put(measure.getKey(),feed);
				}
				FeedAlarmThresholds threshold = thresholds.get(measure.getKey());
				if (threshold == null) {
					threshold = cassandraAlarms.getAlarmThresholds(serial, measure.getKey());
					thresholds.put(measure.getKey(),threshold);
				}
				FeedAlarmEvent fired = cassandraAlarms.analizeAlarmThresholds(measure, feed, threshold);
				if (fired != null && fired.isNotify()) {
					service.notifyAlarm(fired);
					logger.debug("Notified Alarm {}", fired);
				}
			}
		}
	}



	@Override
	public void interrupt() throws UnableToInterruptJobException {
	}

}
