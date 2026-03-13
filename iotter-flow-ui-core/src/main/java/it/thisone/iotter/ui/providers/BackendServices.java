package it.thisone.iotter.ui.providers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.thisone.iotter.integration.AlarmService;
import it.thisone.iotter.integration.AuthManager;
import it.thisone.iotter.integration.ExportService;
import it.thisone.iotter.integration.NotificationService;
import it.thisone.iotter.integration.SubscriptionService;
import it.thisone.iotter.mqtt.MqttOutboundService;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.GroupWidgetService;
import it.thisone.iotter.persistence.service.ImageDataService;
import it.thisone.iotter.persistence.service.MeasureUnitTypeService;
import it.thisone.iotter.persistence.service.ModbusProfileService;
import it.thisone.iotter.persistence.service.NetworkGroupService;
import it.thisone.iotter.persistence.service.NetworkService;
import it.thisone.iotter.persistence.service.RoleService;
import it.thisone.iotter.persistence.service.TracingService;
import it.thisone.iotter.persistence.service.UserService;
import it.thisone.iotter.ui.common.AuthenticatedUser;

@Service
public class BackendServices {

	@Autowired
	private ExportService exportService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private ModbusProfileService modbusProfileService;

	@Autowired
	private SubscriptionService subscriptionService;
	
	@Autowired
	private AuthManager authManager;

	@Autowired
	private UserService userService;
	
	@Autowired
	private RoleService roleService;

	@Autowired
	private NetworkGroupService groupService;

	@Autowired
	private NetworkService networkService;
	
	@Autowired
	private DeviceService deviceService;
	
	@Autowired
	private TracingService tracingService;

	@Autowired
	private GroupWidgetService groupWidgetService;

	@Autowired
	private ImageDataService imageDataService;
	
	@Autowired
	private MeasureUnitTypeService measureUnitTypeService;

	@Autowired
	private MqttOutboundService mqttService;

	@Autowired
	private AlarmService alarmService;

	@Autowired
    private AuthenticatedUser authenticatedUser;

	
	public UserService getUserService() {
		return userService;
	}

	public RoleService getRoleService() {
		return roleService;
	}

	public NetworkGroupService getNetworkGroupService() {
		return groupService;
	}

	public NetworkService getNetworkService() {
		return networkService;
	}

	public DeviceService getDeviceService() {
		return deviceService;
	}

	public TracingService getTracingService() {
		return tracingService;
	}

	public GroupWidgetService getGroupWidgetService() {
		return groupWidgetService;
	}

	public AuthManager getAuthManager() {
		return authManager;
	}

	public ImageDataService getImageDataService() {
		return imageDataService;
	}


	public SubscriptionService getSubscriptionService() {
		return subscriptionService;
	}

	public NotificationService getNotificationService() {
		return notificationService;
	}

	public AlarmService getAlarmService() {
		return alarmService;
	}
	
	public MqttOutboundService getMqttService() {
		return mqttService;
	}

	public ModbusProfileService getModbusProfileService() {
		return modbusProfileService;
	}

	public MeasureUnitTypeService getMeasureUnitTypeService() {
		return measureUnitTypeService;
	}

	public ExportService getExportService() {
		return exportService;
	}



	public AuthenticatedUser getAuthenticatedUser() { return authenticatedUser; }
	
}
