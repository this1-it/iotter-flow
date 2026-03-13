package it.thisone.iotter.integration;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


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

@Component
public class ServiceFactory {
	private static Logger logger = LoggerFactory.getLogger(ServiceFactory.class);	
	@PostConstruct
	public void init() {
		logger.debug("ServiceFactory initialized.");
		checkEnhancerBySpringCGLIB(userService.getClass());
		checkEnhancerBySpringCGLIB(groupService.getClass());
		checkEnhancerBySpringCGLIB(networkService.getClass());
		checkEnhancerBySpringCGLIB(deviceService.getClass());
	}
	
	private <T> void checkEnhancerBySpringCGLIB(Class<T> clazz) {
		// Bean 'deviceService' of type [it.thisone.iotter.persistence.service.DeviceService] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
		String value = clazz.toString();
		if (!value.contains("EnhancerBySpringCGLIB")) {
			throw new IllegalArgumentException(String.format("Bean %s is NOT ENHANCED", clazz.getName()));
		}
		
	}

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
	private RecoveryService recoveryService;

	
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

	public RecoveryService getRecoveryService() {
		return recoveryService;
	}


	
}
