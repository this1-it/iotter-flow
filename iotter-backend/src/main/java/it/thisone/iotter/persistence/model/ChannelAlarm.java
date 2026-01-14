package it.thisone.iotter.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import it.thisone.iotter.enums.Priority;

@Embeddable
public class ChannelAlarm implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8222610028535289152L;

	public ChannelAlarm() {
		super();
	}

	@Override
	public String toString() {
		return String.format("lowLow=%f ,low=%f, high=%f, highHigh=%f, armed=%b, priority=%s", lowLow, low, high, highHigh, armed, priority);
	}

	@Transient
	private boolean fired;

	@Column(name = "ALRM_ARMED")
	private boolean armed;

	@Column(name = "ALRM_NOTIFY")
	private boolean notify;

	@Enumerated(EnumType.STRING)
	@Column(name = "ALRM_PRIORITY")
	private Priority priority;

	@Column(name = "ALRM_LOW_LOW")
	private Float lowLow;

	@Column(name = "ALRM_LOW")
	private Float low;

	@Column(name = "ALRM_HIGH")
	private Float high;

	@Column(name = "ALRM_HIGH_HIGH")
	private Float highHigh;

	@Column(name = "ALRM_DELAY_MINUTES")
	private Integer delayMinutes;

	@Column(name = "ALRM_REPEAT_MINUTES")
	private Integer repeatMinutes;

	public boolean isArmed() {
		return armed;
	}

	public void setArmed(boolean armed) {
		this.armed = armed;
	}

	public Float getLowLow() {
		return lowLow;
	}

	public void setLowLow(Float lowLow) {
		this.lowLow = lowLow;
	}

	public Float getLow() {
		return low;
	}

	public void setLow(Float low) {
		this.low = low;
	}

	public Float getHigh() {
		return high;
	}

	public void setHigh(Float high) {
		this.high = high;
	}

	public Float getHighHigh() {
		return highHigh;
	}

	public void setHighHigh(Float highHigh) {
		this.highHigh = highHigh;
	}

	public Integer getDelayMinutes() {
		return delayMinutes;
	}

	public void setDelayMinutes(Integer delayMinutes) {
		this.delayMinutes = delayMinutes;
	}

	public Integer getRepeatMinutes() {
		return repeatMinutes;
	}

	public void setRepeatMinutes(Integer repeatMinutes) {
		this.repeatMinutes = repeatMinutes;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public boolean isNotify() {
		return notify;
	}

	public void setNotify(boolean notify) {
		this.notify = notify;
	}

	@Transient
	public boolean isEmpty() {
		return (low == null);
	}

	@Transient
	public boolean isFired() {
		return fired;
	}

	public void setFired(boolean fired) {
		this.fired = fired;
	}

	@Transient
	public boolean isValid() {
		if (isEmpty())
			return false;
		if (low < lowLow) {
			return false;
		}
		if (high > highHigh) {
			return false;
		}
		if (low > high) {
			return false;
		}
		if (lowLow >= highHigh) {
			return false;
		}
		return true;
	}

}
