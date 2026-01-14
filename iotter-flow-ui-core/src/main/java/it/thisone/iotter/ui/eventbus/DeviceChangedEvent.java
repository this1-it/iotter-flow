package it.thisone.iotter.ui.eventbus;
import it.thisone.iotter.enums.AlarmStatus;

public class DeviceChangedEvent {
    final private String serial;
    final private AlarmStatus alarmStatus;
    
    public DeviceChangedEvent(String serial) {
    	this.serial = serial;
    	this.alarmStatus = null;
    }
    public DeviceChangedEvent(String serial,AlarmStatus alarmStatus) {
    	this.serial = serial;
    	this.alarmStatus = alarmStatus;
    }
	public String getSerial() {
		return serial;
	}
	public AlarmStatus getAlarmStatus() {
		return alarmStatus;
	}

}
