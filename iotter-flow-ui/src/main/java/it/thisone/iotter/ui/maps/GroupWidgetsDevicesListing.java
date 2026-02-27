package it.thisone.iotter.ui.maps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.flow.components.TabSheet;
import org.springframework.beans.factory.ObjectProvider;

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
import it.thisone.iotter.persistence.service.GroupWidgetService;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.eventbus.DeviceGroupWidgetEvent;
import it.thisone.iotter.ui.eventbus.UIEventBus;
import it.thisone.iotter.ui.groupwidgets.GroupWidgetVisualizer;
import it.thisone.iotter.util.MapUtils;

public class GroupWidgetsDevicesListing extends BaseComponent {

    public static final Logger logger = LoggerFactory.getLogger(GroupWidgetsDevicesListing.class);

    private static final long serialVersionUID = 1340051957283147500L;

    private final Map<Device, Set<GroupWidget>> map;
    private final GroupWidgetService groupWidgetService;
    private final UIEventBus uiEventBus;
    private final ObjectProvider<GroupWidgetsListingBox> listingBoxProvider;

    private TabSheet tabsheet;

    public GroupWidgetsDevicesListing(String name, Map<Device, Set<GroupWidget>> devices) {
        this(name, devices, null, null, null);
    }

    public GroupWidgetsDevicesListing(String name, Map<Device, Set<GroupWidget>> devices,
            GroupWidgetService groupWidgetService, UIEventBus uiEventBus,
            ObjectProvider<GroupWidgetsListingBox> listingBoxProvider) {
        super("groupwidgets.deviceslisting", UUID.randomUUID().toString());
        this.map = devices;
        this.groupWidgetService = groupWidgetService;
        this.uiEventBus = uiEventBus;
        this.listingBoxProvider = listingBoxProvider;
        initialize(name, map);
    }

    public GroupWidgetsDevicesListing(Network network) {
        this(network, null, null, null);
    }

    public GroupWidgetsDevicesListing(Network network, GroupWidgetService groupWidgetService, UIEventBus uiEventBus,
            ObjectProvider<GroupWidgetsListingBox> listingBoxProvider) {
        super("groupwidgets.deviceslisting", network != null ? network.toString() : UUID.randomUUID().toString());
        this.map = network != null ? MapUtils.mappableDevices(network) : new HashMap<>();
        this.groupWidgetService = groupWidgetService;
        this.uiEventBus = uiEventBus;
        this.listingBoxProvider = listingBoxProvider;
        initialize(network != null ? network.getName() : "", map);
    }

    public GroupWidgetsDevicesListing(GroupWidget groupWidget) {
        this(groupWidget, null, null, null);
    }

    public GroupWidgetsDevicesListing(GroupWidget groupWidget, GroupWidgetService groupWidgetService, UIEventBus uiEventBus,
            ObjectProvider<GroupWidgetsListingBox> listingBoxProvider) {
        super("groupwidgets.deviceslisting");
        this.map = new HashMap<>();
        this.groupWidgetService = groupWidgetService;
        this.uiEventBus = uiEventBus;
        this.listingBoxProvider = listingBoxProvider;

        if (groupWidget != null) {
            setId(groupWidget.toString());
            for (GraphicWidget widget : groupWidget.getWidgets()) {
                for (GraphicFeed feed : widget.getFeeds()) {
                    Device device = feed.getChannel().getDevice();
                    map.computeIfAbsent(device, key -> new HashSet<>()).add(groupWidget);
                }
            }
            initialize(groupWidget.getName(), map);
        } else {
            initialize("", map);
        }
    }

    private void initialize(String name, Map<Device, Set<GroupWidget>> devices) {
        tabsheet = new TabSheet();
        tabsheet.setSizeFull();

        GroupWidgetsListingBox devicesMap;
        if (listingBoxProvider != null) {
            devicesMap = listingBoxProvider.getObject(UUID.randomUUID().toString(), name, devices);
        } else {
            devicesMap = new GroupWidgetsListingBox(UUID.randomUUID().toString(), name, devices);
        }
        Tab rootTab = tabsheet.addTab(name, devicesMap);
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
                                    groupWidgetService);
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
