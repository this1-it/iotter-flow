package it.thisone.iotter.persistence.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Indexes;

import com.google.common.collect.Range;

import it.thisone.iotter.enums.modbus.TypeRead;

/*
@Cache(type = CacheType.SOFT, // Cache everything until the JVM decides memory is low.
size = 64000, // Use 64,000 as the initial cache size.
// expiry = 3600000 // 60 minutes
expiryTimeOfDay=@TimeOfDay(hour=3) // 3:00 AM
)
*/

//@Cache(type = CacheType.SOFT, size = 64000, expiry = 360000, coordinationType = CacheCoordinationType.INVALIDATE_CHANGED_OBJECTS)

@Cacheable(false)
@Entity
@Indexes({ @Index(name = "CHANNEL_NUMBER_INDEX", columnNames = { "NUMBER" }) })
@Table(name = "CHANNEL")
public class Channel extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * create channel with empty configuration
	 */
	public Channel() {
		super();
		getConfiguration().setActive(true);
		getConfiguration().setActivationDate(new Date());
	}

	/**
	 * return canonical channel label
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
//		if (!getConfiguration().isHideNumber()) {
//			sb.append(getNumber()).append(" ");
//		}
		sb.append(getNumber()).append(" [").append(getConfiguration().isActive()).append("] ");

		if (getConfiguration().getLabel() != null) {
			sb.append(getConfiguration().getLabel()).append(" ");
		}

		if (getMetaData() != null) {
			sb.append(getMetaData()).append(" ").append(getOid());
		}

//		if (getConfiguration().getSubLabel() != null) {
//			sb.append(getConfiguration().getSubLabel());
//		}
		return sb.toString().trim();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getNumber()). //
				append(getKey()). //
				toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Channel == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final Channel otherObject = (Channel) obj;
		return new EqualsBuilder(). //
				append(this.getNumber(), otherObject.getNumber()).append(this.getKey(), otherObject.getKey())
				.isEquals();
	}

	/**
	 * activate channel and eventually backup previous activity period
	 * 
	 * @param channel
	 */
	public void activateChannel(Date reactivationDate) {
		if (!getConfiguration().isActive()) {
			Date start = getConfiguration().getActivationDate();
			Date end = getConfiguration().getDeactivationDate();
			if (start != null && end != null) {
				long gap = reactivationDate.getTime() - end.getTime();
				if (gap > 5 * 60 * 1000) {
					ValidityInterval validity = new ValidityInterval();
					validity.setStartDate(start);
					validity.setEndDate(end);
					addValidity(validity);
				} else {
					// false de-activation
					reactivationDate = start;
				}
			}
			getConfiguration().setActive(true);
			getConfiguration().setActivationDate(reactivationDate);
			getConfiguration().setDeactivationDate(null);
		}
	}

	public void deActivateChannel(Date date) {
		if (getConfiguration().isActive()) {
			getConfiguration().setActive(false);
			getConfiguration().setDeactivationDate(date);
		}
	}

	@Column(name = "UNIQUE_KEY")
	private String uniqueKey;

	@Column(name = "NUMBER")
	private String number;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DEVICE_ID")
	private Device device;

	private ChannelConfiguration configuration;

	@ElementCollection(targetClass = ValidityInterval.class)
	@CollectionTable(name = "CHANNEL_VALIDITY", joinColumns = @JoinColumn(name = "ID"))
	@AttributeOverrides({ @AttributeOverride(name = "startDate", column = @Column(name = "START")), //
			@AttributeOverride(name = "endDate", column = @Column(name = "END")) //
	})
	private Set<ValidityInterval> validities = new HashSet<ValidityInterval>();

	@ElementCollection(targetClass = MeasureUnit.class)
	@CollectionTable(name = "CHANNEL_MEASURE", joinColumns = @JoinColumn(name = "ID"))
	@OrderColumn(name = "POSITION")
	private List<MeasureUnit> measures = new ArrayList<MeasureUnit>();

	@Embedded
	private ChannelAlarm alarm;

	@Embedded
	private ChannelRemoteControl remote;

	@Column(name = "META_DATA")
	private String metaData;

	@Column(name = "OID")
	private String oid;

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	/**
	 * @return the configuration
	 */
	public ChannelConfiguration getConfiguration() {
		if (configuration == null)
			configuration = new ChannelConfiguration();
		return configuration;
	}

	/**
	 * @param configuration the configuration to set
	 */
	public void setConfiguration(ChannelConfiguration configuration) {
		this.configuration = configuration;
	}

	public Set<ValidityInterval> getValidities() {
//		if (validities == null)
//			validities = new HashSet<ValidityInterval>();
		return validities;
	}

	public void setValidities(Set<ValidityInterval> validities) {
		this.validities = validities;
	}

	public void addValidity(ValidityInterval value) {
		boolean overlap = false;
		for (ValidityInterval range : getValidities()) {
			overlap = range.overlaps(value);
			if (overlap) {
				break;
			}
		}
		if (!overlap) {
			getValidities().add(value);
		}
	}

	public List<MeasureUnit> getMeasures() {
//		if (measures == null)
//			measures = new ArrayList<MeasureUnit>();
		return measures;
	}

	public void setMeasures(List<MeasureUnit> measures) {
		this.measures = measures;
	}

	public MeasureUnit getDefaultMeasure() {
		try {
			return measures.get(0);
		} catch (Throwable e) {
		}
		MeasureUnit unit = new MeasureUnit();
		unit.setFormat("");
		unit.setOffset(0f);
		unit.setScale(1f);
		unit.setType(255);
		return unit;

	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	public void setUniqueKey(String key) {
		this.uniqueKey = key;
	}

	// Feature #195 Create device param with different id and same configuration
	// ( same unique key)
	public String internalKey() {
		StringBuffer sb = new StringBuffer();
		sb.append(getNumber());
		sb.append(".");
		sb.append(getUniqueKey());
		return sb.toString();
	}

	public String getKey() {
		StringBuffer sb = new StringBuffer();
		if (getDevice() != null) {
			sb.append(getDevice().getSerial());
			sb.append(".");
			sb.append(getUniqueKey());
		}
		return sb.toString();
	}

	public ChannelAlarm getAlarm() {
		if (alarm == null) {
			alarm = new ChannelAlarm();
		}
		return alarm;
	}

	public void setAlarm(ChannelAlarm alarm) {
		this.alarm = alarm;
	}

	public String getMetaData() {
		return metaData;
	}

	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}

	public ChannelRemoteControl getRemote() {
		if (remote == null) {
			remote = new ChannelRemoteControl();
		}
		return remote;
	}

	public void setRemote(ChannelRemoteControl remote) {
		this.remote = remote;
	}

	@Transient
	public List<Range<Date>> getValidityRanges() {
		List<Range<Date>> intervals = new ArrayList<Range<Date>>();

		if (getConfiguration().isActive()) {
			Range<Date> range = Range.greaterThan(getConfiguration().getActivationDate());
			intervals.add(range);
		} else {
			Range<Date> range = Range.closedOpen(getConfiguration().getActivationDate(),
					getConfiguration().getDeactivationDate());
			intervals.add(range);
		}

		for (ValidityInterval interval : getValidities()) {
			Range<Date> range = Range.closedOpen(interval.getStartDate(), interval.getEndDate());
			intervals.add(range);
		}
		return intervals;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
//		if (oid == null) {
//			System.out.println("channel null oid");
//		}
		this.oid = oid;
	}

	public ChannelAlarm buildChannelAlarm(ModbusRegister register) {
		ChannelAlarm alarm = null;
		double min = register.getMin();
		double max = register.getMax();

		if (min == 0.0 && max == 1.0) {
			alarm = new ChannelAlarm();
			alarm.setArmed(true);
			alarm.setNotify(true);
			alarm.setLowLow(-1f);
			alarm.setLow(-1f);
			alarm.setHigh(0f);
			alarm.setHighHigh(1f);
			alarm.setDelayMinutes(0);
			alarm.setRepeatMinutes(0);
			alarm.setPriority(register.getPriority());
		}
		return alarm;
	}

	public static String getTypeVar(String metadata) {
		if (metadata != null && metadata.contains("|")) {
			String parts[] = StringUtils.split(metadata, "|");
			if (parts.length >= 4) {
				return parts[3];
			}
		}
		return null;
	}

	@Transient
	public boolean isCrucial() {
		if (this.metaData != null) {
			return this.metaData.contains(ModbusRegister.CRUCIAL_PROP);
		}
		return false;
	}

	@Transient
	public String getMetaIdentifier() {
		if (this.metaData != null) {
			int i = StringUtils.ordinalIndexOf(this.metaData, "|", 3);
			if (i > 0) {
				return this.metaData.substring(0, i);
			}
		}
		return null;
	}

	@Transient
	public Integer getAddress() {
		if (this.number != null && this.number.contains(":")) {
			String[] parts = this.number.split(":");
			return Integer.parseInt(parts[1]);
		}
		return null;
	}

	@Transient
	public TypeRead getTypeRead() {
		if (this.number != null && this.number.contains(":")) {
			String[] parts = this.number.split(":");
			for (TypeRead value : TypeRead.values()) {
				if (value.getShortName().equalsIgnoreCase(parts[2])) {
					return value;
				}
			}
		}
		return null;
	}
	
	@Transient
	public String getRegisterId() {
		int index = this.getNumber() != null ? this.getNumber().indexOf(":") : -1 ;
		String id = index > 0 ? this.getNumber().substring(index + 1) : this.getNumber();
		return id;
	}

	public boolean isAvailable() {
		return !isCrucial() && getConfiguration().isActive();
	}

	public static Predicate<Channel> hasSameNumberAndNewerActivationDate(Channel channel) {
		return other -> !other.getKey().equals(channel.getKey()) && other.getNumber().equals(channel.getNumber())
				&& other.getConfiguration().getActivationDate().after(channel.getConfiguration().getActivationDate());
	}

}
