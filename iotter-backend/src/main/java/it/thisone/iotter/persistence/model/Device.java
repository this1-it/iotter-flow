/**
 * Copyright 2016 ThisOne
 *
 */
package it.thisone.iotter.persistence.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Indexes;
import org.eclipse.persistence.annotations.PrivateOwned;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.AlarmStatus;
import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.enums.Protocol;
import it.thisone.iotter.util.EncryptUtils;
import it.thisone.iotter.util.Utils;


//@Cache(type = CacheType.SOFT, size = 6400, expiry = 360000, coordinationType = CacheCoordinationType.INVALIDATE_CHANGED_OBJECTS)
@Cacheable(false)
@Entity
@Indexes({ @Index(name = "DEVICE_INACTIVITY_INDEX", columnNames = { "INACTIVITY_MINUTES" }),
		@Index(name = "DEVICE_OWNER_INDEX", columnNames = { "OWNER" }),
		@Index(name = "DEVICE_SERIAL_INDEX", columnNames = { "SERIAL" }),
		@Index(name = "DEVICE_ACTIVATION_INDEX", columnNames = { "SERIAL", "ACTIVATION_KEY" }),
		@Index(name = "DEVICE_STATUS_INDEX", columnNames = { "STATUS" }) })
@Table(name = "DEVICE")
@NamedQuery(name = "Device.findBySerialCached", query = "SELECT e FROM Device e WHERE e.serial = :serial", hints = {
		@QueryHint(name = QueryHints.QUERY_RESULTS_CACHE, value = HintValues.TRUE), })
public class Device extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Device() {
		super();
		/*
		 * Feature #159 DeviceStatus state machine Uno strumento viene creato con lo
		 * stato PRODUCED
		 */

		status = DeviceStatus.PRODUCED;
		visible = true;
		tracing = true;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DEVICE_MODEL_ID")
	private DeviceModel model;

	@Column(name = "SERIAL", unique = true)
	private String serial;

	// alter table DEVICE add unique index ACTIVATION_KEY (ACTIVATION_KEY (24));

	@Column(name = "ACTIVATION_KEY", unique = true)
	private String activationKey;

	@Column(name = "LABEL")
	private String label;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "FREQUENCY")
	private int frequency;

	@Column(name = "BATTERY_LEVEL")
	private int batteryLevel;

	@Column(name = "AUTO_CALIBRATION")
	private boolean autoCalibration;

	@Column(name = "FIRMWARE_VERSION")
	private String firmwareVersion;

	@Column(name = "PROGRAM_VERSION")
	private String programVersion;

	@Column(name = "LOGGING_SPACE")
	private int loggingSpace;

	@Column(name = "CERTIFICATION_NUMBER")
	private String certificationNumber;

	@Column(name = "MAX_MEASURE_NUMBER")
	private int maxMeasureNumber;

	@Column(name = "READ_API_KEY")
	private String readApikey;

	@Column(name = "WRITE_API_KEY")
	private String writeApikey;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS")
	private DeviceStatus status;

	@Temporal(TemporalType.DATE)
	@Column(name = "PRODUCTION")
	private Date productionDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_CERTIFICATION")
	private Date lastCertificationDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CONF_DATE")
	private Date configurationDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ACTIVE_DATE")
	private Date activationDate;

	@Column(name = "SAMPLE_PERIOD")
	private long samplePeriod;

	@Column(name = "VISIBLE")
	private boolean visible;

	@Column(name = "TIME_ZONE")
	private String timeZone;

	@Column(name = "MODIFIER")
	private String modifier;

	@PrivateOwned
	@OneToMany(orphanRemoval = true, mappedBy = "device", fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
	private Set<Channel> channels;

	@Embedded
	private GeoLocation location;

	@Embedded
	private FtpAccess ftpAccess;

	@Embedded
	private SimCard simCard;

	@Lob
	@Column(name = "CONFIGURATION")
	private String configuration;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "DEVICE_GROUP", joinColumns = @JoinColumn(name = "DEVICE_ID"), inverseJoinColumns = @JoinColumn(name = "GROUP_ID"))
	private Set<NetworkGroup> groups;

	@Column(name = "ALARMED")
	private boolean alarmed;

	@Column(name = "INACTIVITY_MINUTES")
	private int inactivityMinutes;

	@PrivateOwned
	@OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
	private Set<ModbusProfile> profiles;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MASTER_ID")
	private Device master;

	@Column(name = "STICKY")
	private boolean sticky;

	@Column(name = "PUBLISHING")
	private boolean publishing;

	@Column(name = "TRACING")
	private boolean tracing;

	@Column(name = "CHECK_SUM")
	private String checkSum;
	
	@Column(name = "EXPORTING")
	private boolean exporting;

	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_EXPORT_DATE")
	private Date lastExportDate;

	
	@Embedded
	private DeviceHistory history;
	
	@Embedded
	private ExportingConfig exportingConfig;

	@Transient
	private Date lastContactDate;
	

	
	/**
	 * all groups must belong to same network
	 * 
	 * @return
	 */
	public Network getNetwork() {
		if (!getGroups().isEmpty()) {
			NetworkGroup group = getGroups().iterator().next();
			if (group != null) {
				return group.getNetwork();
			}
		}
		return null;
	}

	public boolean isAvailableForActivation() {
		if (getStatus() == null)
			return false;

		if (getMaster() != null) {
			return false;
		}

		return (getStatus().equals(DeviceStatus.PRODUCED) || getStatus().equals(DeviceStatus.VERIFIED));
	}

	public boolean isAvailableForVisualization() {
		return !getChannels().isEmpty();
	}

	public boolean isActivated() {
		if (getStatus() == null)
			return false;
		return (getStatus().equals(DeviceStatus.ACTIVATED) || getStatus().equals(DeviceStatus.CONNECTED));
	}

	public boolean isRunning() {
		if (getStatus() == null)
			return false;
		return (getStatus().equals(DeviceStatus.VERIFIED) || getStatus().equals(DeviceStatus.CONNECTED));
	}

	public boolean notActive() {
		if (getStatus() == null)
			return true;
		return (getStatus().equals(DeviceStatus.PRODUCED) || getStatus().equals(DeviceStatus.DEACTIVATED));
	}

	public boolean isDeActivated() {
		if (getStatus() == null)
			return true;
		return getStatus().equals(DeviceStatus.DEACTIVATED);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (getLabel() != null) {
			sb.append(getLabel());
		}
		if (getDescription() != null) {
			sb.append(" ").append(getDescription());
		}
		if (getModel() != null) {
			sb.append(" ").append(getModel().getName());
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getSerial()).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Device == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final Device otherObject = (Device) obj;
		return new EqualsBuilder().append(getSerial(), otherObject.getSerial()).isEquals();
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int getBatteryLevel() {
		return batteryLevel;
	}

	public void setBatteryLevel(int batteryLevel) {
		this.batteryLevel = batteryLevel;
	}

	public boolean isAutoCalibration() {
		return autoCalibration;
	}

	public void setAutoCalibration(boolean autoCalibration) {
		this.autoCalibration = autoCalibration;
	}

	public GeoLocation getLocation() {
		if (location == null)
			location = new GeoLocation();
		return location;
	}

	public void setLocation(GeoLocation location) {
		this.location = location;
	}

	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public void setFirmwareVersion(String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

	public String getProgramVersion() {
		return programVersion;
	}

	public void setProgramVersion(String programVersion) {
		this.programVersion = programVersion;
	}

	public int getLoggingSpace() {
		return loggingSpace;
	}

	public void setLoggingSpace(int loggingSpace) {
		this.loggingSpace = loggingSpace;
	}

	public String getCertificationNumber() {
		return certificationNumber;
	}

	public void setCertificationNumber(String certificationNumber) {
		this.certificationNumber = certificationNumber;
	}

	public int getMaxMeasureNumber() {
		return maxMeasureNumber;
	}

	public void setMaxMeasureNumber(int maxMeasureNumber) {
		this.maxMeasureNumber = maxMeasureNumber;
	}

	public DeviceStatus getStatus() {
		return status;
	}

	public void setStatus(DeviceStatus status) {
		this.status = status;
	}

	public Date getProductionDate() {
		return productionDate;
	}

	public void setProductionDate(Date productionDate) {
		this.productionDate = productionDate;
	}

	public Date getLastCertificationDate() {
		return lastCertificationDate;
	}

	public void setLastCertificationDate(Date lastCertificationDate) {
		this.lastCertificationDate = lastCertificationDate;
	}

	public Set<Channel> getChannels() {
		if (channels == null) {
			channels = new HashSet<Channel>();
		}
		return channels;
	}

	public void setChannels(Set<Channel> channels) {
		for (Channel channel : channels) {
			channel.setDevice(this);
			channel.setOwner(getOwner());
		}
		this.channels = channels;
	}

	/*
	 * Add channel as active
	 */
	public void addChannel(Channel channel) {
		if (!getChannels().contains(channel)) {
			if (channel.getConfiguration().getActivationDate() == null) {
				channel.getConfiguration().setActivationDate(new Date());
			}
			channel.getConfiguration().setDeactivationDate(null);
			channel.getConfiguration().setActive(true);
			channel.setDevice(this);
			channel.setOwner(getOwner());
			getChannels().add(channel);
		}
	}

	public List<String> feedKeys() {
		List<String> keys = new ArrayList<String>();
		for (Channel channel : getChannels()) {
			String key = String.format("%s.%s", getSerial(), channel.getUniqueKey());
			keys.add(key);
		}
		return keys;
	}

	public FtpAccess getFtpAccess() {
		if (ftpAccess == null)
			ftpAccess = new FtpAccess();
		return ftpAccess;
	}

	public void setFtpAccess(FtpAccess ftpAccess) {
		this.ftpAccess = ftpAccess;
	}

	public SimCard getSimCard() {
		if (simCard == null)
			simCard = new SimCard();
		return simCard;
	}

	public void setSimCard(SimCard simCard) {
		this.simCard = simCard;
	}

	public void addGroup(NetworkGroup group) {
		if (group != null && !getGroups().contains(group)) {
			getGroups().add(group);
		}
	}

	public Set<NetworkGroup> getGroups() {
		if (groups == null) {
			groups = new HashSet<NetworkGroup>();
		}
		return groups;
	}

	public void setGroups(Set<NetworkGroup> groups) {
		this.groups = groups;
	}

	public String getReadApikey() {
		return readApikey;
	}

	public void setReadApikey(String readApikey) {
		this.readApikey = readApikey;
	}

	public String getWriteApikey() {
		return writeApikey;
	}

	public void setWriteApikey(String writeApikey) {
		this.writeApikey = writeApikey;
	}

	public String getActivationKey() {
		return activationKey;
	}

	public void setActivationKey(String activationKey) {
		this.activationKey = activationKey;
	}

	@Transient
	public Date getLastContactDate() {
		return lastContactDate;
	}

	@Transient
	public void setLastContactDate(Date lastContactDate) {
		this.lastContactDate = lastContactDate;
	}

	public DeviceModel getModel() {
		return model;
	}

	public void setModel(DeviceModel model) {
		this.model = model;
	}

	public Protocol getProtocol() {
		Protocol value = null;
		if (getModel() != null) {
			value = getModel().getProtocol();
		}
		return value;
	}

	/**
	 * serialized configuration
	 * 
	 * @return
	 */
	public String getConfiguration() {
		return configuration;
	}

	/**
	 * serialized configuration
	 * 
	 * @param configuration
	 */
	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public Date getConfigurationDate() {
		return configurationDate;
	}

	public void setConfigurationDate(Date date) {
		this.configurationDate = date;
	}

	public long getSamplePeriod() {
		return samplePeriod;
	}

	public void setSamplePeriod(long samplePeriod) {
		this.samplePeriod = samplePeriod;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public boolean isAlarmed() {
		return alarmed;
	}

	public void setAlarmed(boolean alarmed) {
		this.alarmed = alarmed;
	}

	public int getInactivityMinutes() {
		return inactivityMinutes;
	}

	public void setInactivityMinutes(int inactivityMinutes) {
		this.inactivityMinutes = inactivityMinutes;
	}

	public boolean isInactive() {
		if (inactivityMinutes <= 0) {
			return false;
		}
		if (lastContactDate == null && inactivityMinutes > 0) {
			return true;
		}
		// Date inactivityDate = new Date(System.currentTimeMillis() -
		// Constants.Provisioning.INACTIVITY_MINUTES * 60 * 1000);
		// return lastContactDate != null &&
		// lastContactDate.before(inactivityDate);
		return checkInactive(lastContactDate);
	}

	public boolean checkInactive(Date lastContactDate) {
		if (lastContactDate == null) {
			return true;
		}
		Date inactivityDate = new Date(System.currentTimeMillis() - Constants.Provisioning.INACTIVITY_MINUTES * 60 * 1000);
		return lastContactDate.before(inactivityDate);
	}

	public Set<ModbusProfile> getProfiles() {
		if (profiles == null) {
			profiles = new HashSet<ModbusProfile>();
		}
		return profiles;
	}

	public void setProfiles(Set<ModbusProfile> profiles) {
		for (ModbusProfile profile : profiles) {
			profile.setOwner(getOwner());
		}
		this.profiles = profiles;
	}

	public Device getMaster() {
		return master;
	}

	public void setMaster(Device master) {
		if (!this.equals(master)) {
			this.master = master;
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(Date activationDate) {
		this.activationDate = activationDate;
	}

	public boolean isSticky() {
		return sticky;
	}

	public void setSticky(boolean sticky) {
		this.sticky = sticky;
	}

	public DeviceHistory getHistory() {
		if (history == null)
			history = new DeviceHistory();
		return history;
	}

	public void setHistory(DeviceHistory history) {
		this.history = history;
	}

	public boolean isPublishing() {
		return publishing;
	}

	public void setPublishing(boolean publishing) {
		this.publishing = publishing;
	}

	public boolean isTracing() {
		return tracing;
	}

	public void setTracing(boolean tracing) {
		this.tracing = tracing;
	}



	@Transient
	public List<Channel> alarms() {
		return this.getChannels().stream().filter(o -> o.getConfiguration().isActive() && Utils.isTypeAlarm(o.getMetaData())).collect(Collectors.toList());
	}

	public String calculateCheckSum() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getStatus().name());
		for (Channel chnl : this.getChannels()) {
			sb.append(chnl.getKey());
			sb.append(chnl.getConfiguration().isActive());
			sb.append(chnl.getConfiguration().isSelected());
			if (chnl.getAlarm().isValid()) {
				sb.append(chnl.getAlarm().toString());
			}
			if (chnl.getRemote().isValid()) {
				sb.append(chnl.getRemote().toString());
			}
		}
		return EncryptUtils.digest(sb.toString());
	}

	public String getCheckSum() {
		return checkSum;
	}

	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}

	@Transient
	public boolean hasRtc() {
		if (getMaster() != null) {
			return getMaster().hasRtc();
		}
		if (getModel() != null) {
			return getModel().isRtc();
		}
		return false;
	}
	
	// Feature #1885
	public void changeSlaveId(String originalId, String slaveId) {
		String oldPrefix = originalId + ":";
		String newPrefix = slaveId + ":";
		for (Channel channel : this.getChannels()) {
			String number = channel.getNumber();
			if (number.startsWith(oldPrefix)) {
				String newNumber = number.replaceFirst(oldPrefix, newPrefix);
				channel.setNumber(newNumber);
				if (channel.getRemote().isValid()) {
					String topic = channel.getRemote().getTopic().replaceFirst(number, newNumber);
					channel.getRemote().setTopic(topic);					
				}
			}
		}
	}
	
	
	@Transient
	private AlarmStatus alarmStatus;


	public AlarmStatus getAlarmStatus() {
		return alarmStatus;
	}

	public void setAlarmStatus(AlarmStatus alarmStatus) {
		this.alarmStatus = alarmStatus;
	}
	
	public AlarmStatus changedAlarmStatus(Date lastContactDate, boolean alarmed, boolean activeAlarms) {
		alarmStatus = AlarmStatus.UNDEFINED;
		if (DeviceStatus.PRODUCED.equals(this.status)) {
			return alarmStatus;
		}
		this.lastContactDate = lastContactDate;
		this.alarmed = alarmed;
		if (checkInactive(lastContactDate)) {
			alarmStatus = AlarmStatus.OFFLINE;
		}
		else if (this.alarmed && ! this.channels.isEmpty()) {
			alarmStatus = activeAlarms ? AlarmStatus.ON : AlarmStatus.OFF; 
		}
		return alarmStatus;
	}
	
    public List<Channel> removeInactiveDuplicates() {
    	List<Channel> removed = new ArrayList<>();
        Iterator<Channel> iterator = channels.iterator();
        while (iterator.hasNext()) {
        	Channel currentBean = iterator.next();
            long duplicateCount = channels.stream()
                .filter(other -> other.getNumber() == currentBean.getNumber())
                .count();

            if (!currentBean.getConfiguration().isActive() && duplicateCount > 1) {
                iterator.remove();
                removed.add(currentBean);
            }
        }
        return removed;
    }

	public boolean isExporting() {
		return exporting;
	}

	public void setExporting(boolean exporting) {
		this.exporting = exporting;
	}

	public Date getLastExportDate() {
		return lastExportDate;
	}

	public void setLastExportDate(Date lastExportDate) {
		this.lastExportDate = lastExportDate;
	}

	public ExportingConfig getExportingConfig() {
		return exportingConfig;
	}

	public void setExportingConfig(ExportingConfig exportConfig) {
		this.exportingConfig = exportConfig;
	}
	

}
