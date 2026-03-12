package it.thisone.iotter.ui.providers;

import java.io.Serializable;

import it.thisone.iotter.cassandra.CassandraAlarms;
import it.thisone.iotter.cassandra.CassandraFeeds;
import it.thisone.iotter.cassandra.CassandraMeasures;
import it.thisone.iotter.cassandra.CassandraRollup;
import it.thisone.iotter.exporter.IExportProvider;
import it.thisone.iotter.integration.AlarmService;
import it.thisone.iotter.integration.NotificationService;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.GroupWidgetService;
import it.thisone.iotter.ui.common.AuthenticatedUser;
import org.springframework.stereotype.Service;

/**
 * Bundles services needed by chart adapters and widget visualizers.
 * Threaded through the factory chain since providers are not Spring beans.
 */
@Service
public class VisualizerServices implements Serializable {

    private static final long serialVersionUID = 1L;

    private final CassandraAlarms cassandraAlarms;
    private final CassandraFeeds cassandraFeeds;
    private final CassandraMeasures cassandraMeasures;
    private final CassandraRollup cassandraRollup;
    private final IExportProvider exportProvider;
    private final AlarmService alarmService;
    private final NotificationService notificationService;
    private final DeviceService deviceService;
    private final GroupWidgetService groupWidgetService;
    private final AuthenticatedUser authenticatedUser;

    public VisualizerServices(CassandraAlarms cassandraAlarms, CassandraFeeds cassandraFeeds,
            CassandraMeasures cassandraMeasures, CassandraRollup cassandraRollup,
            IExportProvider exportProvider, AlarmService alarmService,
            NotificationService notificationService, DeviceService deviceService,
            GroupWidgetService groupWidgetService, AuthenticatedUser authenticatedUser) {
        this.cassandraAlarms = cassandraAlarms;
        this.cassandraFeeds = cassandraFeeds;
        this.cassandraMeasures = cassandraMeasures;
        this.cassandraRollup = cassandraRollup;
        this.exportProvider = exportProvider;
        this.alarmService = alarmService;
        this.notificationService = notificationService;
        this.deviceService = deviceService;
        this.groupWidgetService = groupWidgetService;
        this.authenticatedUser = authenticatedUser;
    }

    public CassandraAlarms getCassandraAlarms() { return cassandraAlarms; }
    public CassandraFeeds getCassandraFeeds() { return cassandraFeeds; }
    public CassandraMeasures getCassandraMeasures() { return cassandraMeasures; }
    public CassandraRollup getCassandraRollup() { return cassandraRollup; }
    public IExportProvider getExportProvider() { return exportProvider; }
    public AlarmService getAlarmService() { return alarmService; }
    public NotificationService getNotificationService() { return notificationService; }
    public DeviceService getDeviceService() { return deviceService; }
    public GroupWidgetService getGroupWidgetService() { return groupWidgetService; }
    public AuthenticatedUser getAuthenticatedUser() { return authenticatedUser; }
}
