package it.thisone.iotter.enums;


public enum TracingAction 
// implements Internationalizable 
{
	LOGIN("Login"),
	LOGIN_FAILED("Login Failed"),
	NETWORK_CREATION("Network Creation"),
	DEVICE_CREATION("Device Creation"),
	DEVICE_UPDATE("Device Update"),
	DEVICE_REMOVE("Device Remove"),
	DEVICE_RESET("Device Factory Reset"),
	DEVICE_ACTIVE("Device Activation"),
	DEVICE_DEACTIVATED("Device Deactivated"),
	DEVICE_CONNECT("Device Connected"),
	DEVICE_EXPORT("Device Export"),
	DEVICE_CONFIGURATION("Device Configuration"),
	DEVICE_CONFIGURATION_RESET("Device Reset Configuration"),
	MQTT_PROVISIONED("Device Provisioned"),
	DEVICE_PROVISIONING("Device Provisioning"),
	CHANNEL_CREATION("Param Creation"),
	CHANNEL_DEACTIVATED("Param Deactivated"), 
	CHANNEL_ACTIVATED("Param Activated"),
	ERROR_BACKEND("Error Backend"), 
	ERROR_REST("Error REST"), 
	ERROR_UI("Error UI"),
	ERROR_FTP("Error Ftp"),
	POST_REST("POST REST"), 
	ALARM("Alarm");

	private String displayName;

	private TracingAction(String value) {
		this.displayName = value;
	}

    @Override public String toString() { return displayName; }
	
//	@Override
//	public String getI18nKey() {
//        return "enum.tracing." + name().toLowerCase();        
//    }
	


}
