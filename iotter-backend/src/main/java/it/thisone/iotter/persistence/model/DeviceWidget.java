package it.thisone.iotter.persistence.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;
import org.eclipse.persistence.annotations.TimeOfDay;

import it.thisone.iotter.persistence.ifc.IMarker;

@Cacheable(true)
@Cache(type = CacheType.SOFT, // Cache everything until the JVM decides memory is low.
expiryTimeOfDay=@TimeOfDay(hour=3) // 3:00 AM
//expiry = 3600000 // 60 minutes
)

@Entity
@Table(name = "DEVICE_WIDGET")
public class DeviceWidget extends BaseEntity implements IMarker {
	public DeviceWidget(String label, String serial) {
		super();
		this.label = label;
		this.serial = serial;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DeviceWidget() {
		super();
	}


	@Column(name = "X")
	private float x;
	
	@Column(name = "Y")
	private float y;
	
	
	@Column(name = "LABEL")
	private String label;
	
	@Column(name = "SERIAL")
	private String serial;

	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CUSTOM_MAP_ID")
	private DeviceCustomMap customMap;


	@Override
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}


	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public DeviceCustomMap getCustomMap() {
		return customMap;
	}

	public void setCustomMap(DeviceCustomMap groupMap) {
		this.customMap = groupMap;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(getSerial());
		if (getLabel() != null) {
			sb.append(" ");
			sb.append(getLabel());
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getSerial()).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof DeviceWidget == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final DeviceWidget otherObject = (DeviceWidget) obj;
		return new EqualsBuilder().append(this.getSerial(), otherObject.getSerial()).isEquals();
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public void setX(float x) {
		this.x = x;
	}

	@Override
	public float getY() {
		return y;
	}

	@Override
	public void setY(float y) {
		this.y = y;
	}
	
	@Override
	public String getMarkerId() {
		return getSerial();
	}

}
