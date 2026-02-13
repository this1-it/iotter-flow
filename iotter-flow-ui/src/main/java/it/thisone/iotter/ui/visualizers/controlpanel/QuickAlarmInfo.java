package it.thisone.iotter.ui.visualizers.controlpanel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;

import it.thisone.iotter.cassandra.model.FeedAlarm;
import it.thisone.iotter.cassandra.model.IFeedAlarm;
import it.thisone.iotter.integration.AlarmService;
import it.thisone.iotter.integration.CassandraService;
import it.thisone.iotter.mqtt.MqttOutboundService;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.ui.common.AuthenticatedUser;
import it.thisone.iotter.ui.common.ConfirmationDialog.Callback;
import it.thisone.iotter.ui.eventbus.DeviceChangedEvent;
import it.thisone.iotter.ui.eventbus.UIEventBus;
import it.thisone.iotter.ui.ifc.ITabContent;
import it.thisone.iotter.ui.model.ChannelAdapter;
import it.thisone.iotter.ui.model.ChannelAdapterDataProvider;
import it.thisone.iotter.ui.visualizers.controlpanel.IconSetResolver.IconSet;
import it.thisone.iotter.ui.visualizers.controlpanel.QuickCommandButton.QuickCommandCallback;
import it.thisone.iotter.ui.visualizers.controlpanel.QuickCommandButton.QuickCommandClickCallback;

public class QuickAlarmInfo extends VerticalLayout implements ITabContent {

    private static final long serialVersionUID = 43952018985504330L;
    public static Logger logger = LoggerFactory.getLogger(QuickAlarmInfo.class);

    private final Grid<ChannelAdapter> grid;
    private final ListDataProvider<ChannelAdapter> dataProvider;
    private final Device device;
    private final CassandraService cassandraService;
    private final AlarmService alarmService;
    private final UIEventBus uiEventBus;
    private final AuthenticatedUser authenticatedUser;
    private final MqttOutboundService mqttOutboundService;

    public QuickAlarmInfo(Device device, GraphicFeed feed,
            CassandraService cassandraService,
            AlarmService alarmService,
            UIEventBus uiEventBus,
            AuthenticatedUser authenticatedUser,
            MqttOutboundService mqttOutboundService) {
        super();
        this.device = device;
        this.cassandraService = cassandraService;
        this.alarmService = alarmService;
        this.uiEventBus = uiEventBus;
        this.authenticatedUser = authenticatedUser;
        this.mqttOutboundService = mqttOutboundService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        grid = new Grid<>();
        grid.addClassName("smallgrid");
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();
        grid.setEnabled(false);

        dataProvider = new ListDataProvider<>(new ArrayList<>());
        grid.setDataProvider(dataProvider);

        grid.addColumn(ChannelAdapter::getDisplayName).setKey("displayName").setHeader("");
        grid.addColumn(ChannelAdapter::getAlarmValue).setKey("alarmValue").setHeader("");
        grid.addColumn(ChannelAdapter::getAlarmDate).setKey("alarmDate").setHeader("");

        if (feed != null && feed.getChannel() != null) {
            IconSetResolver resolver = new IconSetResolver();
            IconSet set = resolver.resolveIconSetName(feed.getResourceID());

            final QuickCommandButton reset = new QuickCommandButton(set, mqttOutboundService, cassandraService);
            reset.setTopic(feed.getChannel().getRemote().getTopic());
            reset.setKey(feed.getChannel().getKey());
            reset.setSerial(feed.getChannel().getDevice().getSerial());

            boolean anonymous = authenticatedUser == null
                    || authenticatedUser.get().map(user -> !user.isEnabled()).orElse(true);
            reset.setEnabled(!anonymous);

            reset.setCallback(new QuickCommandCallback() {
                @Override
                public boolean checkResult(float value) {
                    if (cassandraService == null) {
                        return false;
                    }
                    int count = cassandraService.getAlarms().countActiveAlarms(reset.getSerial());
                    return count == 0;
                }

                @Override
                public void onSuccess() {
                    if (uiEventBus != null) {
                        uiEventBus.post(new DeviceChangedEvent(reset.getSerial()));
                    }
                }

                @Override
                public void beforeCommand() {
                    if (alarmService == null) {
                        return;
                    }
                    String username = authenticatedUser == null
                            ? "anonymous"
                            : authenticatedUser.get().map(user -> user.getUsername()).orElse("anonymous");
                    alarmService.notifyAlarmReset(reset.getSerial(), username);
                }

                @Override
                public void onError() {
                    if (cassandraService != null) {
                        cassandraService.getAlarms().checkActiveAlarms(reset.getSerial());
                    }
                }
            });

            Callback callback = result -> {
                if (result) {
                    QuickCommandClickCallback clickCallback = QuickCommandButton.createClickCallback(reset);
                    clickCallback.clicked();
                }
            };

            reset.addClickListener(reset.buildResetAlarmsClickListener(device, callback));
            reset.setWidthFull();
            add(reset);
        }

        add(grid);
        expand(grid);
    }

    @Override
    public boolean isLoaded() {
        return !dataProvider.getItems().isEmpty();
    }

    @Override
    public void lazyLoad() {
        if (!isLoaded()) {
            refresh();
        }
    }

    @Override
    public void refresh() {
        Runnable update = () -> {
            if (cassandraService == null) {
                return;
            }
            List<FeedAlarm> events = cassandraService.getAlarms().findActiveAlarms(device.getSerial());

            ChannelAdapterDataProvider localProvider = new ChannelAdapterDataProvider();
            localProvider.addChannels(device.getChannels());

            List<IFeedAlarm> alarms = new ArrayList<>();
            alarms.addAll(events);
            List<ChannelAdapter> renderedAlarms = localProvider.renderAlarms(alarms);

            List<ChannelAdapter> filteredAlarms = renderedAlarms.stream()
                    .filter(ChannelAdapter::isAlarmed)
                    .sorted((a1, a2) -> {
                        if (a1.getAlarmDate() == null && a2.getAlarmDate() == null) {
                            return 0;
                        }
                        if (a1.getAlarmDate() == null) {
                            return 1;
                        }
                        if (a2.getAlarmDate() == null) {
                            return -1;
                        }
                        return a2.getAlarmDate().compareTo(a1.getAlarmDate());
                    })
                    .collect(Collectors.toList());

            dataProvider.getItems().clear();
            dataProvider.getItems().addAll(filteredAlarms);
            dataProvider.refreshAll();
        };

        UI ui = getUI().orElse(null);
        if (ui != null) {
            ui.access(update);
        } else {
            update.run();
        }
    }
}
