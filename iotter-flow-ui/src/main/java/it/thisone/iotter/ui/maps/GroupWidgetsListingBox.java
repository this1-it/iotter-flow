package it.thisone.iotter.ui.maps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.vaadin.flow.components.TabSheet;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.data.provider.ListDataProvider;

import it.thisone.iotter.cassandra.model.DataSink;
import it.thisone.iotter.integration.CassandraService;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.service.GroupWidgetService;
import it.thisone.iotter.ui.devices.DeviceInfo;
import it.thisone.iotter.ui.groupwidgets.GroupWidgetVisualizer;
import it.thisone.iotter.ui.providers.BackendServices;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GroupWidgetsListingBox extends Composite<Div> {

    private static final long serialVersionUID = 1L;
    private static final String DEVICE_VIEW = "device.view";

    private Map<Device, Set<GroupWidget>> map;

    private ListDataProvider<Device> dataProvider;
    private Grid<Device> grid;
    private VerticalLayout infoPanel;

    @Autowired
    private ObjectProvider<DeviceInfo> deviceInfoProvider;



    @Autowired
    private BackendServices visualizerServices;

    public GroupWidgetsListingBox() {
        this(null, null, new HashMap<>());
    }

    public GroupWidgetsListingBox(String id, String caption, Map<Device, Set<GroupWidget>> listing) {
        super();
        if (id != null) {
            setId(id);
        }
        this.map = listing != null ? listing : new HashMap<>();
        buildLayout(caption);
    }

    public String getI18nLabel(String key) {
        return getTranslation(DEVICE_VIEW + "." + key);
    }

    private void buildLayout(String caption) {
        dataProvider = new ListDataProvider<>(new ArrayList<>(map.keySet()));
        grid = createGrid();

        infoPanel = new VerticalLayout();
        infoPanel.setWidthFull();
        infoPanel.setPadding(true);
        infoPanel.setSpacing(true);

        SplitLayout splitLayout = new SplitLayout(grid, infoPanel);
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(60);

        VerticalLayout listingLayout = new VerticalLayout();
        listingLayout.setSizeFull();
        listingLayout.setPadding(false);
        listingLayout.setSpacing(true);

        if (caption != null) {
            Span title = new Span(getI18nLabel("title") + " " + caption);
            title.addClassName("h3");
            listingLayout.add(title);
        }

        listingLayout.add(splitLayout);
        listingLayout.setFlexGrow(1f, splitLayout);

        getContent().removeAll();
        getContent().setSizeFull();
        getContent().add(listingLayout);
        grid.select(dataProvider.getItems().stream().findFirst().orElse(null));
    }

    private Grid<Device> createGrid() {
        Grid<Device> deviceGrid = new Grid<>();
        deviceGrid.setDataProvider(dataProvider);
        deviceGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        deviceGrid.setSizeFull();

        deviceGrid.addColumn(Device::getSerial).setKey("serial").setHeader(getI18nLabel("serial"));
        deviceGrid.addColumn(Device::getLabel).setKey("label").setHeader(getI18nLabel("label"));
        deviceGrid.addColumn(this::formatNetwork).setKey("network").setHeader(getI18nLabel("network"));
        deviceGrid.addColumn(this::formatStatus).setKey("status").setHeader(getI18nLabel("status"));
        deviceGrid.addComponentColumn(this::createAlarmedLabel).setKey("alarmed").setHeader("");

        deviceGrid.addSelectionListener(event -> showDeviceInfo(event.getFirstSelectedItem().orElse(null)));

        return deviceGrid;
    }

    private String formatNetwork(Device device) {
        if (device.getNetwork() == null) {
            return "";
        }
        return device.getNetwork().getName();
    }

    private String formatStatus(Device device) {
        if (device.getStatus() == null) {
            return "";
        }
        return getTranslation(device.getStatus().getI18nKey(), device.getStatus().name().toLowerCase());
    }

    private Component createAlarmedLabel(Device device) {
        Span label = new Span();
        label.addClassName("no-alarm");


            DataSink item = visualizerServices.getCassandraFeeds().getDataSink(device.getSerial());
            if (item != null) {
                device.changedAlarmStatus(item.getLastContact(), item.isAlarmed(), item.hasActiveAlarms());
                if (device.getAlarmStatus() != null) {
                    label.setText(device.getAlarmStatus().name());
                }
            }
        

        return label;
    }

    private void showDeviceInfo(Device device) {
        infoPanel.removeAll();

        if (device == null) {
            return;
        }

        Collection<GroupWidget> widgets = map.get(device);
        if (deviceInfoProvider == null) {
            // TODO(flow-migration): this component must be instantiated by Spring/ObjectProvider.
            infoPanel.add(new Span(device.getSerial()));
            return;
        }

        DeviceInfo info = deviceInfoProvider.getObject(device);
        info.addListener(event -> {
            if (event.getSelected() instanceof GroupWidget) {
                openVisualization((GroupWidget) event.getSelected());
            }
        });

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        if (widgets == null || widgets.isEmpty()) {
            layout.add(new Span(getI18nLabel("missing_visualizations")));
        }
        layout.add(info);
        infoPanel.add(layout);
    }

    private void openVisualization(GroupWidget widget) {
        if (widget == null) {
            return;
        }

        GroupWidgetVisualizer content = new GroupWidgetVisualizer(widget.getId().toString(), true, visualizerServices);
        if (getParent().isPresent() && getParent().get() instanceof TabSheet) {
            TabSheet tabsheet = (TabSheet) getParent().get();
            Tab tab = tabsheet.addTab(widget.getName(), content);
            tabsheet.setSelectedTab(tab);
            return;
        }

        // TODO(flow-migration): parent tab host is not a Flow TabSheet, cannot open visualization tab.
    }
}
