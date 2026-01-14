/**
 * Copyright 2016 ThisOne
 *
 */
package it.thisone.iotter.persistence.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;
import org.hibernate.validator.constraints.NotEmpty;

import it.thisone.iotter.enums.Protocol;

@Cacheable(true)
@Cache(type = CacheType.SOFT)
@Entity
@Table(name = "DEVICE_MODEL", uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME" }) })
public class DeviceModel extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DeviceModel() {
		super();
	}

	@NotEmpty
	@Column(name = "NAME")
	private String name;

	@Column(name = "USER_MANUAL")
	private String userManual;

	@Enumerated(EnumType.STRING)
	@Column(name = "PROTOCOL")
	private Protocol protocol;

	@Lob	
	@Column(name = "DESCRIPTION")
	private String description;
	
	
	@Column(name = "RTC")
	private boolean rtc;

	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getName());
//		if (getProtocol() != null) {
//			sb.append("[");
//			sb.append(getProtocol());
//			sb.append("]");
//		}
		return sb.toString();
	}

    @Override
    public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getName()).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
		if (obj instanceof DeviceModel == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final DeviceModel otherObject = (DeviceModel) obj;
		return new EqualsBuilder().append(getName(), otherObject.getName()).isEquals();
    }
	
	public String getUserManual() {
		return userManual;
	}

	public void setUserManual(String userManual) {
		this.userManual = userManual;
	}


	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isRtc() {
		return rtc;
	}

	public void setRtc(boolean rtc) {
		this.rtc = rtc;
	}


	
}
