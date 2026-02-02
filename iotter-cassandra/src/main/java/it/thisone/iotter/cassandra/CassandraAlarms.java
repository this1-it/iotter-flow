package it.thisone.iotter.cassandra;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.DriverException;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BatchableStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;

import it.thisone.iotter.cassandra.model.Feed;
import it.thisone.iotter.cassandra.model.FeedAlarm;
import it.thisone.iotter.cassandra.model.FeedAlarmEvent;
import it.thisone.iotter.cassandra.model.FeedAlarmThresholds;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.AlarmStatus;

/*
 
cqlsh -e'SELECT * FROM aernet.feed_alarm_events' > alarms.txt
 
CREATE TABLE aernet_tp.feed_alarm_events (sn text,crt timestamp,key text,mbrs text,ntf boolean,op text,st text,ths float,ts timestamp,val float,PRIMARY KEY (sn, crt, key)) WITH CLUSTERING ORDER BY (crt DESC) AND default_time_to_live = 31536000;
 
drop table  aernet_tp.feed_alarm_events; 
alter table  aernet_tp.feed_alarm_events with clustering order by (crt desc);

copy aernet_tp.feed_alarm_events from './alarms.csv';

copy aerlink.feed_alarm_events to './alarms-aerlink.csv';
drop table  aerlink.feed_alarm_events; 
CREATE TABLE aerlink.feed_alarm_events (sn text,crt timestamp,key text,mbrs text,ntf boolean,op text,st text,ths float,ts timestamp,val float,PRIMARY KEY (sn, crt, key)) WITH CLUSTERING ORDER BY (crt DESC) AND default_time_to_live = 31536000;
copy aerlink.feed_alarm_events from './alarms-aerlink.csv';


 */

@Service
public class CassandraAlarms extends AlarmsQueryBuilder implements Serializable {

	private static final long serialVersionUID = -963150975938168570L;
	private static final int UPDATE_BATCH_SIZE = 16;
	private static Logger logger = LoggerFactory.getLogger(Constants.Notifications.LOG4J_CATEGORY);

	@Autowired
	private CassandraFeeds cassandraFeeds;

	
	@Autowired
	private CassandraClient client;


	
	public void updateAlarmThresholds(FeedAlarmThresholds item) {
		try {
			{
				Statement stmt = prepareUpdateAlarmThresholds(item);
				client.executeWithRetry(stmt);
				;
			}
		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to update FeedAlarmThresholds for id " + item.getKey(), e);
		}
	}
	
	public List<FeedAlarmThresholds> getAlarmsThresholds(String sn) {
		List<FeedAlarmThresholds> items = new ArrayList<>();
		Statement stmt = prepareSelectAlarmThresholds(sn, null);
		try {
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
				items.add(fillAlarmThresholds(row));
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving sink FeedAlarmThresholds " + sn, e);
		}
		return items;
	}


	public FeedAlarmThresholds getAlarmThresholds(String sn, String key) {
		FeedAlarmThresholds item = null;
		Statement stmt = prepareSelectAlarmThresholds(sn, key);
		try {
			;
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = rs.one();
				item = fillAlarmThresholds(row);
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving FeedAlarmThresholds " + key, e);
		}
		return item;
	}

	public FeedAlarm getAlarm(String sn, String key) {
		FeedAlarm item = null;
		Statement stmt = prepareSelectAlarm(sn, key);
		try {
			ResultSet rs = client.executeWithRetry(stmt);
			;
			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = rs.one();
				item = fillAlarm(row);
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving FeedAlarm " + key, e);
		}
		return item;
	}

	public void updateAlarm(FeedAlarm item) {
		try {
			Statement stmt = prepareUpdateAlarm(item);
		
			client.executeWithRetry(stmt);
			;
		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to update FeedAlarm for key " + item.getKey(), e);
		}
	}

	public List<FeedAlarmEvent> getAlarmEvents(String serial, int limit) {
		List<FeedAlarmEvent> items = new ArrayList<>();
		try {
			Statement stmt = prepareSelectAlarmEvents(serial, limit);
			ResultSet rs = client.executeWithRetry(stmt);
			;
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = rs.one();
				items.add(fillAlarmEvent(row));
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving FeedAlarmEvents " + serial, e);
		}
		return items;
	}

	public void updateAlarmEvent(FeedAlarmEvent item) {
		try {
			Statement stmt = prepareUpdateAlarmEvent(item, 0, client.writeConsistencyLevel());
			client.executeWithRetry(stmt);
		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to update FeedAlarmEvent for key " + item.getKey(), e);
		}
	}

	public List<FeedAlarm> retrieveDelayedAlarms() {
		List<FeedAlarm> items = new ArrayList<FeedAlarm>();
		try {
			Statement stmt = prepareSelectDelayedAlarms()
					.setPageSize(PREFETCH_LIMIT);
			ResultSet rs = client.executeWithRetry(stmt);
			;
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
				items.add(fillAlarm(row));
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving FeedAlarms", e);
		}
		return items;
	}

	public List<FeedAlarm> retrieveRepeatedAlarms() {
		List<FeedAlarm> items = new ArrayList<FeedAlarm>();
		try {
			Statement stmt = prepareSelectRepeatedAlarms()
					.setPageSize(PREFETCH_LIMIT);
			ResultSet rs = client.executeWithRetry(stmt);
			;
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
				items.add(fillAlarm(row));
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving FeedAlarms", e);
		}
		return items;
	}

	public List<FeedAlarm> findActiveAlarms(String serial) {
		List<FeedAlarm> items = new ArrayList<FeedAlarm>();
		try {
			Statement stmt = prepareSelectAlarms(serial)
					.setPageSize(PREFETCH_LIMIT);
			ResultSet rs = client.executeWithRetry(stmt);
			;
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
				FeedAlarm item = fillAlarm(row);
				if (item.isActive()) {
					items.add(item);
				}
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving FeedAlarms", e);
		}
		return items;
	}

	public int countActiveAlarms(String serial) {
		int count = 0;
		try {
			Statement stmt = prepareCountActiveAlarms(serial);
			ResultSet rs = client.executeWithRetry(stmt);
			Iterator<Row> iter = rs.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();
				if (row.getBool(ACTIVE) && !row.getString(SN).equals(row.getString(KEY))) {
					count++;
				}
			}
		} catch (DriverException e) {
			logger.error("cassandra not available countActiveAlarms", e);
		}
		logger.debug("countActiveAlarms {} {} ", serial, count);
		return count;
	}

	public FeedAlarmEvent analizeAlarmThresholds(MeasureRaw measure, Feed param, FeedAlarmThresholds thresholds) {
		if (thresholds != null && thresholds.isArmed()) {
			return fireFeedAlarm(measure, thresholds, param);
		}
		return null;
	}

	public FeedAlarmEvent fireFeedAlarm( //
			MeasureRaw measure, //
			FeedAlarmThresholds thresholds, //
			Feed param) {

		if (param == null)
			return null;

		AlarmStatus status = AlarmStatus.UNDEFINED;
		Date timestamp = new Date(0);
		Float value = null;
		Float threshold = null;

		FeedAlarm alarm = getAlarm(param.getSerial(), param.getKey());

		if (alarm == null) {
			alarm = new FeedAlarm(param.getSerial(), param.getKey());
			alarm.setStatus(AlarmStatus.UNDEFINED.name());
		} else {
			status = AlarmStatus.valueOf(alarm.getStatus());
			value = alarm.getValue();
			timestamp = alarm.getTimestamp();
		}

		float alarmValue = param.getMeasureUnit().convert(measure.getValue());

		if (checkActive(status)) {
			switch (status) {
			case FIRE_UP:
				if (lessEqual(alarmValue, thresholds.getHigh())) {
					status = AlarmStatus.REENTER;
					value = alarmValue;
					threshold = thresholds.getHigh();
				}
				break;
			case FIRE_DOWN:
				if (greaterEqual(alarmValue, thresholds.getLow())) {
					status = AlarmStatus.REENTER;
					value = alarmValue;
					threshold = thresholds.getLow();
				}
				break;
			default:
				break;
			}
		} else {
			if (lessEqual(alarmValue, thresholds.getLowLow())) {
				status = AlarmStatus.FIRE_DOWN;
				timestamp = measure.getDate();
				value = alarmValue;
				threshold = thresholds.getLowLow();
			}
			if (greaterEqual(alarmValue, thresholds.getHighHigh())) {
				status = AlarmStatus.FIRE_UP;
				timestamp = measure.getDate();
				value = alarmValue;
				threshold = thresholds.getHighHigh();
			}

		}

		AlarmStatus previous = AlarmStatus.valueOf(alarm.getStatus());
		boolean changed = !status.equals(previous);
		boolean active = checkActive(status);
		boolean notify = thresholds.isNotify();

		if (changed) {
			Date created = new Date();
			if (active) {
				alarm.setDelayed(thresholds.getDelayMinutes() > 0);
				alarm.setRepeated(thresholds.getRepeatMinutes() > 0);
				alarm.setTimestamp(timestamp);
				if (alarm.isDelayed()) {
					notify = false;
				}
				created = timestamp;
			} else {
				if (alarm.isDelayed()) {
					// check if alarm has been already notified
					FeedAlarmEvent firedUp = getFeedAlarmEvent(alarm.getSerial(), alarm.getKey(), alarm.getTimestamp());
					if (firedUp != null) {
						notify = firedUp.isNotify();
					}
				}
				alarm.setDelayed(false);
				alarm.setRepeated(false);
			}
			alarm.setUpdated(new Date());
			alarm.setActive(active);
			alarm.setStatus(status.name());
			alarm.setValue(value);
			alarm.setThreshold(threshold);
			updateAlarm(alarm);
			return createAlarmEvent(alarm, notify, created);
		}
		else {
			logger.debug("not creating alarm event, status NOT CHANGED {} ", alarm.toString());
		}
		

		return null;
	}

	public FeedAlarmEvent createAlarmEvent(FeedAlarm alarm, boolean notify, Date created) {
		FeedAlarmEvent event = new FeedAlarmEvent(alarm.getSerial(), alarm.getKey(), alarm.getTimestamp(), created);
		event.setThreshold(alarm.getThreshold());
		event.setStatus(alarm.getStatus());
		event.setValue(alarm.getThreshold());
		event.setNotify(notify);
		updateAlarmEvent(event);
		logger.debug("createAlarmEvent {}", event.toString());
		return event;
	}

	public FeedAlarmEvent getFeedAlarmEvent(String sn, String key, Date created) {
		FeedAlarmEvent item = null;
		try {
			Statement stmt = prepareSelectAlarmEvent(sn, created, key, client.readConsistencyLevel());
			ResultSet rs = client.executeWithRetry(stmt);
			;

			Iterator<Row> iter = rs.iterator();
			if (iter.hasNext()) {
				Row row = rs.one();
				item = fillAlarmEvent(row);
			}
		} catch (DriverException e) {
			logger.error("cassandra not available retrieving FeedAlarmEvent " + key, e);
		}
		return item;

	}

	private boolean checkActive(AlarmStatus status) {
		return status.equals(AlarmStatus.FIRE_DOWN) || status.equals(AlarmStatus.FIRE_UP);
	}

	private boolean greaterEqual(Float f1, Float f2) {
		return (f1.compareTo(f2) >= 0);
	}

	private boolean lessEqual(Float f1, Float f2) {
		return (f1.compareTo(f2) <= 0);
	}

	public void deleteAlarms(String sn) {
		try {
			Statement stmt = prepareDeleteAlarms(sn);
			client.executeWithRetry(stmt);
			;
		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to delete for sn " + sn, e);
		}

	}

	public void deleteThresholds(String sn) {
		try {
			Statement stmt = prepareDeleteThresholds(sn);
			client.executeWithRetry(stmt);
			;
		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to delete for sn " + sn, e);
		}
	}

	public void deleteEvents(String sn) {
		try {
			Statement stmt = prepareDeleteEvents(sn);
			client.executeWithRetry(stmt);
			;
		} catch (DriverException e) {
			logger.error("unrecoverable error, unable to delete for sn " + sn, e);
		}
	}

	public FeedAlarmEvent fireInactivityAlarm(String sn, boolean inactive, boolean notify, Date lastContact) {
		FeedAlarm alarm = getAlarm(sn, sn);
		if (alarm == null) {
			alarm = new FeedAlarm(sn, sn);
			alarm.setStatus(AlarmStatus.REENTER.name());
			alarm.setDelayed(false);
			alarm.setRepeated(false);
			alarm.setActive(false);
			alarm.setTimestamp(new Date());
		}

		AlarmStatus previous = AlarmStatus.valueOf(alarm.getStatus());
		AlarmStatus status = inactive ? AlarmStatus.ON : AlarmStatus.REENTER;
		boolean changed = !status.equals(previous);
		if (changed) {
			alarm.setStatus(status.name());
			alarm.setActive(false);
			if (inactive) {
				alarm.setTimestamp(lastContact);
				alarm.setUpdated(null);
			} else {
				alarm.setUpdated(lastContact);
			}
			updateAlarm(alarm);
			return createAlarmEvent(alarm, notify, lastContact);
		}
		else {
			logger.debug("not creating inactivity alarm event, status NOT CHANGED {} ", alarm.toString());
		}
		

		return null;
	}

	public int checkActiveAlarms(String serial) {
		int count = 0;
		List<FeedAlarm> alarms = findActiveAlarms(serial);
		for (FeedAlarm alarm : alarms) {
			// retrieve last value
			Feed feed = cassandraFeeds.getFeedValue(serial, alarm.getKey());
			if (!alarm.getValue().equals(feed.getValue())) {
				alarm.setActive(false);
				alarm.setStatus(AlarmStatus.REENTER.name());
				alarm.setUpdated(feed.getDate());
				this.updateAlarm(alarm);
			}
			else {
				count++;
			}
		}
		return count;
	}

	public void updateThresholdsBatch(List<FeedAlarmThresholds> items) {
		if (items.isEmpty()) return;
		BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED)
				.setConsistencyLevel(client.writeConsistencyLevel());
		for (FeedAlarmThresholds item : items) {
			BatchableStatement<?> stmt = (BatchableStatement<?>) prepareUpdateAlarmThresholds(item);
			batch = batch.add(stmt);
			if (batch.size() > UPDATE_BATCH_SIZE) {
				client.executeWithRetry(batch);
				batch = BatchStatement.newInstance(BatchType.UNLOGGED)
						.setConsistencyLevel(client.writeConsistencyLevel());
			}
		}
		if (batch.size() > 0) {
			client.executeWithRetry(batch);
		}
	}

	public void updateAlarmsBatch(List<FeedAlarm> items) {
		if (items.isEmpty()) return;
		BatchStatement batch = BatchStatement.newInstance(BatchType.UNLOGGED)
				.setConsistencyLevel(client.writeConsistencyLevel());
		for (FeedAlarm item : items) {
			BatchableStatement<?> stmt = (BatchableStatement<?>) prepareUpdateAlarm(item);
			batch = batch.add(stmt);
			if (batch.size() > UPDATE_BATCH_SIZE) {
				client.executeWithRetry(batch);
				batch = BatchStatement.newInstance(BatchType.UNLOGGED)
						.setConsistencyLevel(client.writeConsistencyLevel());
			}
		}
		if (batch.size() > 0) {
			client.executeWithRetry(batch);
		}
	}

}
