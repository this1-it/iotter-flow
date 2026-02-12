package it.thisone.iotter.rest;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.thisone.iotter.cassandra.CassandraConfortOnDemand;
import it.thisone.iotter.config.CODConfig;
import it.thisone.iotter.mqtt.MqttOutboundService;
import it.thisone.iotter.mqtt.MqttServiceException;
import it.thisone.iotter.quartz.ConfortOnDemandJob;
import it.thisone.iotter.rest.model.client.ConfortOnDemandEvent;

@Service
public class ConfortOnDemandService {
	private static final String TYPE_IN = "IN";
	public static final String TOPIC_PATTERN = "iotter/device/%s/set/%s";
	private static Logger logger = LoggerFactory.getLogger(CODConfig.CONFORT_ON_DEMAND_LOG4J_CATEGORY);

	@Autowired
	private CassandraConfortOnDemand cassandra;
	@Autowired
	private MqttOutboundService outboundService;

	public void executeCronJob() {
		List<ConfortOnDemandEvent> pks = cassandra.selectPKs();
		for (ConfortOnDemandEvent event : pks) {
			event.setType(TYPE_IN);
			event.setUserid(ConfortOnDemandJob.COD_JOB_CRON);
			process(event);
		}
	}

	public void process(ConfortOnDemandEvent event) {
		boolean cronJob = event.getUserid().equals(ConfortOnDemandJob.COD_JOB_CRON);
		if (!cronJob) {
			cassandra.insert(event);
		}

		// select all events using partition key
		List<ConfortOnDemandEvent> items = cassandra.select(event.getSerial(), event.getBeacon());
		// filter not expired events of type IN
		List<ConfortOnDemandEvent> events = items.stream().filter(e -> e.isAvailable()).collect(Collectors.toList());

		// filter events with high priority
		List<ConfortOnDemandEvent> highPriority = events.stream().filter(e -> e.getPriority() > 0)
				.collect(Collectors.toList());
		if (!highPriority.isEmpty()) {
			events = highPriority;
		}

		if (!events.isEmpty()) {
			// calculate average
			Double value = events.stream().mapToDouble(ConfortOnDemandEvent::getSetpointWanted).average()
					.orElse(Double.NaN);
			// apply average
			writeSetpoint(event, events.get(0).getTopic(), value, items);
		} else {
			// all events are expired or OUT, take the latest's default value
			if (!items.isEmpty()) {
				events = items.stream()
						.sorted(Comparator.comparing(ConfortOnDemandEvent::getTimestamp, Comparator.reverseOrder()))
						.collect(Collectors.toList());
				// apply default
				writeSetpoint(event, events.get(0).getTopic(), events.get(0).getSetpointDefault(), items);
			}
		}

		if (cronJob) {
			for (ConfortOnDemandEvent item : items) {
				if (item.isAbsent()) {
					cassandra.delete(item);
				}
			}
		}

		// Date triggerStartTime = event.triggerStartTime();
		// if (triggerStartTime.before(new Date())) return;
		//
		// String triggerName = String.format("%s%s%s", event.getSerial(),
		// event.getBeacon(), event.getUserid());
		//
		// Trigger trigger = newTrigger().withIdentity(triggerName,
		// ConfortOnDemandJob.COD_JOB_GROUP).startAt(triggerStartTime).build();
		// JobDataMap jobDataMap = trigger.getJobDataMap();
		// jobDataMap.put(ConfortOnDemandJob.COD_EVENT, event);
		// JobKey jobKey = new JobKey(trigger.getKey().getName(),
		// trigger.getKey().getGroup());
		// JobDetail jobDetail =
		// newJob(ConfortOnDemandJob.class).withIdentity(jobKey).build();
		// try {
		// scheduler.scheduleJob(jobDetail, trigger);
		// } catch (SchedulerException e) {
		// if (e instanceof ObjectAlreadyExistsException) {
		// } else {
		// }
		// }

	}

	private void writeSetpoint(ConfortOnDemandEvent event, String topic, Number value,
			List<ConfortOnDemandEvent> items) {
		String eventName = String.format("client %s [sn: %s addr: %s]", event.getUserid(), event.getSerial(),
				event.getBeacon());
		logger.debug(String.format("%s: START", eventName));

		for (ConfortOnDemandEvent item : items) {
			String msg = String.format(
					"%s found registered event: client=%s, expired=%b, type=%s, priority=%d, wanted=%f, since=%tc",
					eventName, //
					item.getUserid(), //
					item.isAbsent(), //
					item.getType(), //
					item.getPriority(), //
					item.getSetpointWanted(), //
					item.getTimestamp() //
			);
			logger.debug(msg);
		}

		String[] parts = StringUtils.split(topic, "|");
		for (int j = 0; j < parts.length; j++) {
			String remoteTopic = String.format(TOPIC_PATTERN, event.getSerial(), parts[j]);
			try {
				outboundService.setValue(remoteTopic, value);
				logger.debug(String.format("%s: write mqtt=%s, value=%f", eventName, remoteTopic, value));
			} catch (MqttServiceException e) {
				logger.error(eventName, e);
			}
		}

		logger.debug(String.format("%s: END", eventName));

	}
}
