package it.thisone.iotter.ui.maps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.flow.components.TabSheet;

import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;

import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.GroupWidgetService;
import it.thisone.iotter.persistence.service.NetworkService;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.common.AuthenticatedUser;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.eventbus.DeviceGroupWidgetEvent;
import it.thisone.iotter.ui.eventbus.UIEventBus;
import it.thisone.iotter.ui.groupwidgets.GroupWidgetVisualizer;
import it.thisone.iotter.ui.providers.VisualizerServices;
import it.thisone.iotter.util.MapUtils;

public class GroupWidgetsGoogleMap extends BaseComponent {

    public static final Logger logger = LoggerFactory.getLogger(GroupWidgetsGoogleMap.class);

    private static final long serialVersionUID = 1340051957283147500L;

    private final Map<Device, Set<GroupWidget>> map;
    private final GroupWidgetService groupWidgetService;
    private final VisualizerServices visualizerServices;
    private final UIEventBus uiEventBus;
    private final DeviceService deviceService;
    private final NetworkService networkService;
    private final String googleMapApiKey;

    private TabSheet tabsheet;

    public GroupWidgetsGoogleMap(Network network) {
        this(network, null, null, null, null, null, "", null);
    }

    public GroupWidgetsGoogleMap(Network network,
        GroupWidgetService groupWidgetService,

            DeviceService deviceService,
            NetworkService networkService,
            AuthenticatedUser authenticatedUser,
            UIEventBus uiEventBus,
            String googleMapApiKey,
            VisualizerServices visualizerServices) {
        super("groupwidgets.googlemap");
        setId(network != null ? network.toString() : UUID.randomUUID().toString());

        UserDetailsAdapter details = authenticatedUser.get().orElse(null);
        this.map = network != null ? MapUtils.mappableDevices(network, details) : new HashMap<>();
        this.groupWidgetService = groupWidgetService;
        this.visualizerServices = visualizerServices;
        this.uiEventBus = uiEventBus;
        this.deviceService = deviceService;
        this.networkService = networkService;
        this.googleMapApiKey = googleMapApiKey != null ? googleMapApiKey : "";
        initialize(network != null ? network.getName() : "", map, network);
    }

    public GroupWidgetsGoogleMap(GroupWidget groupWidget) {
        this(groupWidget, null, null, null, null, "", null);
    }

    public GroupWidgetsGoogleMap(GroupWidget groupWidget, GroupWidgetService groupWidgetService, UIEventBus uiEventBus,
            DeviceService deviceService, NetworkService networkService, String googleMapApiKey,
            VisualizerServices visualizerServices) {
        super("groupwidgets.googlemap");
        this.map = new HashMap<>();
        this.groupWidgetService = groupWidgetService;
        this.visualizerServices = visualizerServices;
        this.uiEventBus = uiEventBus;
        this.deviceService = deviceService;
        this.networkService = networkService;
        this.googleMapApiKey = googleMapApiKey != null ? googleMapApiKey : "";

        if (groupWidget != null) {
            setId(groupWidget.toString());
            for (GraphicWidget widget : groupWidget.getWidgets()) {
                for (GraphicFeed feed : widget.getFeeds()) {
                    Device device = feed.getChannel().getDevice();
                    map.computeIfAbsent(device, key -> new HashSet<>()).add(groupWidget);
                }
            }
            initialize(groupWidget.getName(), map, groupWidget.getNetwork());
        } else {
            initialize("", map, null);
        }
    }

    private void initialize(String name, Map<Device, Set<GroupWidget>> devicesMap, Network network) {
        tabsheet = new TabSheet();
        tabsheet.setSizeFull();

        DevicesGoogleMap devicesGoogleMap = new DevicesGoogleMap(UUID.randomUUID().toString(), name, devicesMap, network,
                false, true, deviceService, networkService, googleMapApiKey);
        Tab rootTab = tabsheet.addTab(name, devicesGoogleMap);
        tabsheet.setSelectedTab(rootTab);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(true);
        mainLayout.add(tabsheet);
        mainLayout.setFlexGrow(1f, tabsheet);

        setRootComposition(mainLayout);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (uiEventBus != null) {
            uiEventBus.register(this);
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (uiEventBus != null) {
            uiEventBus.unregister(this);
        }
        super.onDetach(detachEvent);
    }

    @Subscribe
    public void deviceGroupWidget(DeviceGroupWidgetEvent event) {
        try {
            for (Device device : map.keySet()) {
                if (event.getDevice().equals(device.getSerial())) {
                    Set<GroupWidget> groupWidgets = map.get(device);
                    for (GroupWidget groupWidget : groupWidgets) {
                        if (groupWidget.getId().toString().equals(event.getWidget())) {
                            if (groupWidgetService == null) {
                                // TODO(flow-migration): inject GroupWidgetService from parent to open visualizers.
                                return;
                            }
                            GroupWidgetVisualizer content = new GroupWidgetVisualizer(groupWidget.getId().toString(), true,
                                    groupWidgetService, visualizerServices);
                            Tab tab = tabsheet.addTab(groupWidget.getName(), content);
                            tabsheet.setSelectedTab(tab);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
