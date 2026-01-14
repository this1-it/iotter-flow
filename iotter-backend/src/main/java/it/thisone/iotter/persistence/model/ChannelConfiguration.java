package it.thisone.iotter.persistence.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class ChannelConfiguration implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ChannelConfiguration() {
		super();
	}
	
	@Transient
	private String displayName;

	@Column(name = "EXCLUSIVE")
	private boolean exclusive;
	
	@Column(name = "SELECTED")
	private boolean selected;
	
	@Column(name = "LABEL")
	private String label;

	@Column(name = "SUB_LABEL")
	private String subLabel;

	@Column(name = "HIDE_NUM")
	private boolean hideNumber;
	
	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "ACTIVE")
	private boolean active;
	
	@Column(name = "QUALIFIER")
	private int qualifier;

	// Feature #247 Introduzione dei codici SENSOR per i parametri dello strumento
	@Column(name = "SENSOR")
	private int sensor;
	
	@Column(name = "ACTIVATION_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date activationDate;

	@Column(name = "DEACTIVATION_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date deactivationDate;

	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getLabel());
		sb.append(" ");
		sb.append(getSubLabel());
		return sb.toString();
	}
	
    @Override
    public int hashCode() {
		return new HashCodeBuilder(17, 37).
				append(isHideNumber()). //
				append(getLabel()). //
				append(getSubLabel()). //
				append(getDescription()). //
				toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
		if (obj instanceof ChannelConfiguration == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final ChannelConfiguration otherObject = (ChannelConfiguration) obj;
		return new EqualsBuilder() //
		.append(this.isHideNumber(), otherObject.isHideNumber()) //
		.append(this.getLabel(), otherObject.getLabel()) //
		.append(this.getSubLabel(), otherObject.getSubLabel()) //
		.append(this.getDescription(), otherObject.getDescription()).isEquals();
    }
	
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}


	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Date getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(Date activationDate) {
		this.activationDate = activationDate;
	}

	public Date getDeactivationDate() {
		return deactivationDate;
	}

	public void setDeactivationDate(Date deactivationDate) {
		this.deactivationDate = deactivationDate;
	}

	public String getSubLabel() {
		return subLabel;
	}

	public void setSubLabel(String subLabel) {
		this.subLabel = subLabel;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isHideNumber() {
		return hideNumber;
	}

	public void setHideNumber(boolean hideNumber) {
		this.hideNumber = hideNumber;
	}

	public int getQualifier() {
		return qualifier;
	}

	public void setQualifier(int qualifier) {
		this.qualifier = qualifier;
	}

	public int getSensor() {
		return sensor;
	}

	public void setSensor(int sensor) {
		this.sensor = sensor;
	}

	public String getDisplayName() {
		if (displayName == null) return label;
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isExclusive() {
		return exclusive;
	}

	public void setExclusive(boolean exclusive) {
		this.exclusive = exclusive;
	}



	
}
