package it.thisone.iotter.ui.model;

import java.io.Serializable;
import java.text.ChoiceFormat;
import java.util.Date;

import it.thisone.iotter.cassandra.model.IFeedKey;
import it.thisone.iotter.enums.AlarmStatus;
import it.thisone.iotter.enums.modbus.TypeVar;
import it.thisone.iotter.persistence.model.BaseEntity;
import it.thisone.iotter.persistence.model.Channel;

public class ChannelAdapter extends BaseEntity implements Serializable, IFeedKey {
	/**
	 * 
	 */
	private static final long serialVersionUID = 38374863082734843L;

	public ChannelAdapter() {
		super();
	}

	public ChannelAdapter(Channel item) {
		super();
		this.item = item;
	}
	
	@Override
	public long getConsistencyVersion() {
		return item.getConsistencyVersion();
	}

	private Channel item;

	private String label;

	private String displayName;

	private String serial;
	
	private String key;

	private String number;
	
	private String metaData;

	private boolean selected;

	private boolean disabled;

	private Float lastMeasure;

	private Date lastMeasureDate;

	private String lastMeasureValue;
	
	private String lastMeasureValueUnit;
	
	private String measureUnit;	

	private String thresholds;	

	private ChoiceFormat renderer;

	private TypeVar typeVar;

	private boolean controlled;

	private boolean alarmed;

	private boolean alarmFired;

	private AlarmStatus alarmStatus;

	private Date alarmDate;

	private String alarmValue;

	private String alarmMembers;

	private String alarmOperator;
	
	private boolean checked;

	private String fillColor;

	public Channel getItem() {
		return item;
	}
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public Float getLastMeasure() {
		return lastMeasure;
	}

	public void setLastMeasure(Float lastMeasure) {
		this.lastMeasure = lastMeasure;
	}

	public Date getLastMeasureDate() {
		return lastMeasureDate;
	}

	public void setLastMeasureDate(Date lastMeasureDate) {
		this.lastMeasureDate = lastMeasureDate;
	}

	public String getLastMeasureValue() {
		return lastMeasureValue;
	}

	public void setLastMeasureValue(String lastMeasureValue) {
		this.lastMeasureValue = lastMeasureValue;
	}

	public String getMeasureUnit() {
		return measureUnit;
	}

	public void setMeasureUnit(String measureUnit) {
		this.measureUnit = measureUnit;
	}

	public String getPattern() {
		if (getRenderer() != null) {
			return getRenderer().toPattern();
		}
		return "";
	}

	public ChoiceFormat getRenderer() {
		return renderer;
	}

	public void setRenderer(ChoiceFormat renderer) {
		this.renderer = renderer;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getMetaData() {
		return metaData;
	}

	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public TypeVar getTypeVar() {
		return typeVar;
	}

	public void setTypeVar(TypeVar typeVar) {
		this.typeVar = typeVar;
	}

	public String getThresholds() {
		return thresholds;
	}

	public void setThresholds(String thresholds) {
		this.thresholds = thresholds;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof ChannelAdapter == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final ChannelAdapter otherObject = (ChannelAdapter) obj;
		return getKey().equals(otherObject.getKey());
	}

	public boolean isAlarmed() {
		return alarmed;
	}

	public void setAlarmed(boolean alarmed) {
		this.alarmed = alarmed;
	}


	public Date getAlarmDate() {
		return alarmDate;
	}

	public void setAlarmDate(Date alarmDate) {
		this.alarmDate = alarmDate;
	}

	public boolean isControlled() {
		return controlled;
	}

	public void setControlled(boolean controlled) {
		this.controlled = controlled;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public boolean isAlarmFired() {
		return alarmFired;
	}

	public void setAlarmFired(boolean alarmFired) {
		this.alarmFired = alarmFired;
	}

	public AlarmStatus getAlarmStatus() {
		return alarmStatus;
	}

	public void setAlarmStatus(AlarmStatus alarmStatus) {
		this.alarmStatus = alarmStatus;
	}

	public String getAlarmValue() {
		return alarmValue;
	}

	public void setAlarmValue(String alarmValue) {
		this.alarmValue = alarmValue;
	}

	public String getLabel() {
		return label;
	}

	public String getAlarmMembers() {
		return alarmMembers;
	}

	public String getAlarmOperator() {
		return alarmOperator;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setAlarmMembers(String alarmMembers) {
		this.alarmMembers = alarmMembers;
	}

	public void setAlarmOperator(String alarmOperator) {
		this.alarmOperator = alarmOperator;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public String getFillColor() {
		return fillColor;
	}

	public void setFillColor(String fillColor) {
		this.fillColor = fillColor;
	}

	public String getLastMeasureValueUnit() {
		return lastMeasureValueUnit;
	}

	public void setLastMeasureValueUnit(String lastMeasureValueUnit) {
		this.lastMeasureValueUnit = lastMeasureValueUnit;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}




}
