package it.thisone.iotter.ui.providers;

import org.springframework.stereotype.Service;

import it.thisone.iotter.cassandra.CassandraAlarms;
import it.thisone.iotter.cassandra.CassandraFeeds;
import it.thisone.iotter.cassandra.CassandraMeasures;
import it.thisone.iotter.cassandra.CassandraRollup;
import it.thisone.iotter.exporter.IExportProvider;
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

	private final ExportService exportService;
	private final NotificationService notificationService;
	private final ModbusProfileService modbusProfileService;
	private final SubscriptionService subscriptionService;
	private final AuthManager authManager;
	private final UserService userService;
	private final RoleService roleService;
	private final NetworkGroupService groupService;
	private final NetworkService networkService;
	private final DeviceService deviceService;
	private final TracingService tracingService;
	private final GroupWidgetService groupWidgetService;
	private final ImageDataService imageDataService;
	private final MeasureUnitTypeService measureUnitTypeService;
	private final MqttOutboundService mqttService;
	private final AlarmService alarmService;
	private final AuthenticatedUser authenticatedUser;

    private final CassandraAlarms cassandraAlarms;
    private final CassandraFeeds cassandraFeeds;
    private final CassandraMeasures cassandraMeasures;
    private final CassandraRollup cassandraRollup;
    private final IExportProvider exportProvider;


	public BackendServices(
			ExportService exportService,
			NotificationService notificationService,
			ModbusProfileService modbusProfileService,
			SubscriptionService subscriptionService,
			AuthManager authManager,
			UserService userService,
			RoleService roleService,
			NetworkGroupService groupService,
			NetworkService networkService,
			DeviceService deviceService,
			TracingService tracingService,
			GroupWidgetService groupWidgetService,
			ImageDataService imageDataService,
			MeasureUnitTypeService measureUnitTypeService,
			MqttOutboundService mqttService,
			AlarmService alarmService,
			AuthenticatedUser authenticatedUser,
			CassandraAlarms cassandraAlarms, 
			CassandraFeeds cassandraFeeds,
            CassandraMeasures cassandraMeasures, 
			CassandraRollup cassandraRollup,
            IExportProvider exportProvider
		) {
		this.exportService = exportService;
		this.notificationService = notificationService;
		this.modbusProfileService = modbusProfileService;
		this.subscriptionService = subscriptionService;
		this.authManager = authManager;
		this.userService = userService;
		this.roleService = roleService;
		this.groupService = groupService;
		this.networkService = networkService;
		this.deviceService = deviceService;
		this.tracingService = tracingService;
		this.groupWidgetService = groupWidgetService;
		this.imageDataService = imageDataService;
		this.measureUnitTypeService = measureUnitTypeService;
		this.mqttService = mqttService;
		this.alarmService = alarmService;
		this.authenticatedUser = authenticatedUser;
        this.cassandraAlarms = cassandraAlarms;
        this.cassandraFeeds = cassandraFeeds;
        this.cassandraMeasures = cassandraMeasures;
        this.cassandraRollup = cassandraRollup;
        this.exportProvider = exportProvider;

	}

	public CassandraAlarms getCassandraAlarms() { return cassandraAlarms; }
    public CassandraFeeds getCassandraFeeds() { return cassandraFeeds; }
    public CassandraMeasures getCassandraMeasures() { return cassandraMeasures; }
    public CassandraRollup getCassandraRollup() { return cassandraRollup; }
    public IExportProvider getExportProvider() { return exportProvider; }

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

	public AuthenticatedUser getAuthenticatedUser() {
		return authenticatedUser;
	}
}
