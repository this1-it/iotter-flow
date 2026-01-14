package it.thisone.iotter.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.thisone.iotter.cassandra.CassandraAlarms;
import it.thisone.iotter.cassandra.CassandraFeeds;
import it.thisone.iotter.cassandra.CassandraMeasures;
import it.thisone.iotter.cassandra.CassandraRegistry;
import it.thisone.iotter.cassandra.CassandraRollup;
import it.thisone.iotter.cassandra.model.ConfigurationRegistry;
import it.thisone.iotter.cassandra.model.ConfigurationRevision;
import it.thisone.iotter.cassandra.model.DataSink;
import it.thisone.iotter.cassandra.model.Feed;
import it.thisone.iotter.cassandra.model.IMeasureExporter;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.exporter.CDataPoint;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.rest.model.ConfigAttribute;
import it.thisone.iotter.rest.model.DataPoint;
import it.thisone.iotter.rest.model.DataResultSet;
import it.thisone.iotter.rest.model.DeviceConfiguration;
import it.thisone.iotter.util.BacNet;

import it.thisone.iotter.cassandra.model.ConfigurationRegistry;
import it.thisone.iotter.cassandra.model.DataSink;
import it.thisone.iotter.cassandra.model.Feed;
import it.thisone.iotter.cassandra.model.FeedAlarmThresholds;

@Component
public class CassandraService implements Serializable {

	@Autowired
	@Qualifier("mapper")
	public ObjectMapper mapper;

	private static Logger logger = LoggerFactory.getLogger(CassandraService.class);
	private static Logger asyncLogger = LoggerFactory.getLogger(Constants.AsyncExecutor.LOG4J_CATEGORY);

	private static final long serialVersionUID = 1L;

	@Autowired
	private CassandraAlarms alarms;

	public CassandraAlarms getAlarms() {
		return alarms;
	}

	@Autowired
	private CassandraFeeds feeds;

	public CassandraFeeds getFeeds() {
		return feeds;
	}

	@Autowired
	private CassandraMeasures measures;

	public CassandraMeasures getMeasures() {
		return measures;
	}

	@Autowired
	private IMeasureExporter export;

	public IMeasureExporter getExport() {
		return export;
	}

	@Autowired
	private CassandraRollup rollup;

	public CassandraRollup getRollup() {
		return rollup;
	}

	@Autowired
	private CassandraRegistry registry;

	public CassandraRegistry getRegistry() {
		return registry;
	}

	public int writeClientConfiguration(String serial, DeviceConfiguration config) {
		// retrieve device configuration revision
		ConfigurationRevision cfgRev = registry.getConfigurationRevision(serial, true);
		if (cfgRev == null) {
			return -1;
		}
		int revision = cfgRev.getRevision() + 1;
		for (ConfigAttribute source : config.getAttributes()) {
			ConfigurationRegistry target = new ConfigurationRegistry(serial, false, null);
			BeanUtils.copyProperties(source, target);
			target.setRevision(config.getRevision());
			registry.updateConfigurationRegistry(target);
		}
		return revision;
	}

	public ConfigurationRevision deviceConfigurationRevision(String serial) {
		return registry.getConfigurationRevision(serial, true);
	}

	public DeviceConfiguration deviceConfiguration(String serial) {
		DeviceConfiguration cfg = new DeviceConfiguration();
		cfg.setRevision(-1);
		try {
			ConfigurationRevision rev = registry.getConfigurationRevision(serial, true);
			if (rev != null) {
				cfg.setRevision(rev.getRevision());
				cfg.setRevisionTime(rev.getDate().getTime() / 1000);
				List<ConfigurationRegistry> values = registry.getConfigurations(serial, true);
				for (ConfigurationRegistry source : values) {
					ConfigAttribute target = new ConfigAttribute();
					BeanUtils.copyProperties(source, target);
					cfg.getAttributes().add(target);
				}
			}

		} catch (Exception e) {
			logger.error("error deserializing device " + serial, e);
		}
		return cfg;
	}

	@Cacheable(value = Constants.Cache.DATASINK, key = "#serial", unless = "#result == null")
	public DataSink findDataSinkCacheable(String serial) {
		DataSink result = feeds.getDataSink(serial);
		if (result != null) {
			logger.debug("findDataSinkCacheable {}", result.toString());
		}
		return result;
	}
	
	
	public static DataResultSet publishingDataResultSet(Device device, List<DataPoint> values, long lastContact) {
		DataResultSet result = new DataResultSet();
		
		for (DataPoint dp : values) {
			Optional<Channel> match = device.getChannels().stream().filter(o -> o.getRegisterId().equals(dp.getId()))
					.findFirst(); 
			
			if (match.isPresent()) {
				Channel chnl = match.get();
				Float value = chnl.getDefaultMeasure().convert(dp.getValue());
				dp.setTs(lastContact);
				dp.setValue(value);
				dp.setLabel(chnl.getConfiguration().getLabel());
				dp.setQual(chnl.getConfiguration().getQualifier());
				dp.setUnit(BacNet.lookUp(chnl.getDefaultMeasure().getType()));
				dp.setOffset(chnl.getDefaultMeasure().getOffset());
				dp.setScale(chnl.getDefaultMeasure().getScale());
				dp.setTypeVar(Channel.getTypeVar(chnl.getMetaData()));
				
			}
			
		}
		result.setValues(values);
		result.setSerial(device.getSerial());
		result.setFrom(lastContact);
		result.setTo(lastContact);
		result.setLastContact(lastContact);
		result.setRows(null);
		result.setBatch(null);
		return result;

		
	}

	public static DataResultSet createDataResultSet(List<Feed> feeds, Device device, long from, long to) {
		DataResultSet result = new DataResultSet();
		result.setSerial(device.getSerial());
		for (Feed feed : feeds) {
			Optional<Channel> match = device.getChannels().stream().filter(o -> o.getKey().equals(feed.getKey()))
					.findFirst();
			if (feed.hasLastValue() && match.isPresent()) {
				try {
					Channel chnl = match.get();

					feed.setIdentifier(chnl.getNumber());
					feed.setLabel(chnl.getConfiguration().getLabel());
					feed.setQualifier(chnl.getConfiguration().getQualifier());
					feed.setUnit(BacNet.lookUp(chnl.getDefaultMeasure().getType()));
					feed.setOffset(chnl.getDefaultMeasure().getOffset());
					feed.setScale(chnl.getDefaultMeasure().getScale());
					feed.setTypeVar(Channel.getTypeVar(chnl.getMetaData()));
					feed.setActive(chnl.isAvailable());
					DataPoint data = buildFullDataPoint(feed);
					long ts = data.getTs();
					if (ts > to)
						to = ts;
					if (ts < from)
						from = ts;
					result.getValues().add(data);

				} catch (Throwable e) {
					asyncLogger.error("createDataResultSet {} {} ", feed.getKey(), feed.getValue(), e);
				}
			}
		}
		result.setFrom(from);
		result.setTo(to);
		result.setLastContact(to);
		result.setRows(null);
		result.setBatch(null);
		return result;
	}

	public static DataPoint buildFullDataPoint(Feed feed) {
		DataPoint data = new DataPoint();
		long ts = feed.getDate().getTime() / 1000;
		Float value = feed.getMeasureUnit().convert(feed.getValue());
		data.setId(feed.getRegisterId());
		data.setTs(ts);
		data.setValue(value);
		data.setLabel(feed.getLabel());
		data.setQual(feed.getQualifier());
		data.setUnit(feed.getUnit());
		data.setScale(feed.getScale());
		data.setTypeVar(feed.getTypeVar());
		return data;
	}

	@Cacheable(value = Constants.Cache.DATAVALUES, key = "#device.serial", unless = "#result == null")
	public DataResultSet findDataResultSetCacheable(Device device) {
		List<Feed> feeds = this.getFeeds().getFeedsValues(device.getSerial(), null);
		long from = System.currentTimeMillis() / 1000;
		long to = 0;
		DataResultSet result = CassandraService.createDataResultSet(feeds, device, from, to);
		return result;
	}

	public void mergeDataResultSetValues(List<DataPoint> values, DataResultSet data) {
		if (values.isEmpty()) {
			return;
		}
		for (DataPoint dp : values) {
			Optional<DataPoint> match = data.getValues().stream().filter(o -> o.getId().equals(dp.getId())).findFirst();
			;
			if (match.isPresent()) {
				match.get().setTs(dp.getTs());
				match.get().setValue(dp.getValue());
			} else {
				data.getValues().add(dp);
			}

		}
		List<DataPoint> sorted = data.getValues().stream().sorted(Comparator.comparingLong(DataPoint::getTs))
				.collect(Collectors.toList());
		if (!sorted.isEmpty()) {
			DataPoint from = sorted.get(0);
			DataPoint to = sorted.get(sorted.size() - 1);
			data.setFrom(from.getTs());
			data.setTo(to.getTs());
		}

		asyncLogger.debug("mergeDataResultSetValues: serial {}, size {}, changed {}", data.getSerial(),
				data.getValues().size(), values.size());
	}

	public static List<DataPoint> changes(List<Feed> feeds) {
		List<DataPoint> changes = new ArrayList<DataPoint>();
		for (Feed feed : feeds) {
			if (feed.isChanged() && feed.hasLastValue()) {
				DataPoint dp = new DataPoint();
				dp.setValue(feed.getValue());
				dp.setId(feed.getRegisterId());
				changes.add(dp);
			}
		}

		return changes;
	}

	
	/**
     * Inserts or updates data points for a given device at a specific timestamp.
     * data Points are stored to facilitate easy extraction of data into CSV or Excel formats
     * <p>
     * This method retrieves the most recent measures set for the device based on the provided timestamp.
     * It then compares the existing data points with a list of updated changes. For each updated data point,
     * it updates the existing value if the data point exists, or adds it if not. Additionally, if the number
     * of data points is less than the number of active feeds for the device, it ensures that placeholders
     * (with null values) are present for missing feeds.
     * <p>
     * After merging the changes, the method logs the number of differences, updates the stored measures set in Cassandra,
     * and returns the updated list of data points.
     * </p>
     *
     * @param device   the Device for which the data points are being updated
     * @param timestamp the timestamp representing the measure set time; used to determine the latest measures
     * @param changes  a list of DataPoint objects containing updated values for the device's channels
     * @return a List of DataPoint objects representing the merged and updated measures for the device
     */
	// Feature #2162
	public List<CDataPoint> insertDataPoints(Device device, Date timestamp, List<CDataPoint> changes)  {
	
		List<CDataPoint> values = getMeasures().getLastMeasuresSet(device.getSerial(),timestamp);

		List<Feed> activeFeeds = activeFeeds(device);
		if (values.size() < activeFeeds.size()) {
			for (Feed feed : activeFeeds) {
				Optional<CDataPoint> match = values.stream().filter(o -> o.getId().equals(feed.getRegisterId())).findFirst();
				if (!match.isPresent()) {
					CDataPoint dp = new CDataPoint();
					dp.setId(feed.getRegisterId());
					dp.setValue(null);
				}
			}
		}
		
		
		
		int diff = 0;
		for (CDataPoint dp : changes) {
			Optional<CDataPoint> match = values.stream().filter(o -> o.getId().equals(dp.getId())).findFirst();
			if (match.isPresent()) {
				if (match.get().getValue()!=null && match.get().getValue().compareTo(dp.getValue()) != 0) {
					diff++;
				}
				match.get().setValue(dp.getValue());

			} else {
				values.add(dp);
				diff++;
			}
		}
		
		//logger.debug("{} changes {}", device.getSerial(), diff);

		getMeasures().insertMeasureSet(device.getSerial(), timestamp, values);
		return values;
	}
	
	


	



	public ConfigurationRegistry buildConfigurationRegistry(Channel chnl) {
		ConfigurationRegistry target = new ConfigurationRegistry(chnl.getDevice().getSerial(), true, chnl.getNumber());
		BeanUtils.copyProperties(chnl.getRemote(), target, new String[] {});
		target.setLabel(chnl.getConfiguration().getLabel());
		target.setSection("");
		target.setGroup("");
		target.setTimestamp(chnl.getConfiguration().getActivationDate());
		return target;
	}

	public DataSink buildDataSink(Device device) {
		DataSink sink = new DataSink(device.getSerial());
		sink.setLabel(device.getLabel());
		sink.setOwner(device.getOwner());
		sink.setAlarmed(device.isAlarmed());
		if (device.getModel() != null) {
			sink.setProtocol(device.getModel().getProtocol().name());
		}
		if (device.getLastContactDate() != null) {
			sink.setLastContact(device.getLastContactDate());
		}
		sink.setStatus(device.getStatus().name());
		sink.setTimeZone(device.getTimeZone());
		sink.setWriteApiKey(device.getWriteApikey());
		sink.setPublishing(device.isPublishing());
		sink.setTracing(device.isTracing());
		sink.setCheckSum(device.getCheckSum());
		sink.setInactivityMinutes(device.getInactivityMinutes());
		if (device.getMaster() != null) {
			sink.setMaster(device.getMaster().getSerial());
		}
		return sink;
	}
	


	public List<Feed> activeFeeds(Device device) {
		return device.getChannels().stream().filter(o -> o.getConfiguration().isActive()).map(o -> buildFeed(o))
				.collect(Collectors.toList());
	}
	
	public Feed buildFeed(Channel chnl) {
		Feed feed = new Feed(chnl.getDevice().getSerial(), chnl.getKey());
		feed.setActive(chnl.getConfiguration().isActive());
		feed.setIdentifier(chnl.getNumber());
		feed.setSelected(chnl.getConfiguration().isSelected());
		feed.setLabel(chnl.getConfiguration().getLabel());
		feed.setQualifier(chnl.getConfiguration().getQualifier());
		feed.setUnit(BacNet.lookUp(chnl.getDefaultMeasure().getType()));
		feed.setOffset(chnl.getDefaultMeasure().getOffset());
		feed.setScale(chnl.getDefaultMeasure().getScale());
		feed.setAlarmed(chnl.getAlarm().isArmed());
		feed.setSince(chnl.getConfiguration().getActivationDate());
		feed.setTypeVar(Channel.getTypeVar(chnl.getMetaData()));
		return feed;
	}




}
