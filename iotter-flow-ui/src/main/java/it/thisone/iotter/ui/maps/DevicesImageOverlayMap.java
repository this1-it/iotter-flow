package it.thisone.iotter.ui.maps;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.vaadin.addons.componentfactory.leaflet.layer.ui.marker.Marker;
import org.vaadin.addons.componentfactory.leaflet.types.Icon;
import org.vaadin.addons.componentfactory.leaflet.types.LatLng;
import org.vaadin.addons.componentfactory.leaflet.types.Point;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.DeviceCustomMap;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.ImageData;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.ui.common.SideDrawer;
import it.thisone.iotter.util.MapUtils;
import it.thisone.iotter.util.PopupNotification;

public class DevicesImageOverlayMap extends ImageOverlayMap {

    private DeviceCustomMap entity;
    private static final long serialVersionUID = 1L;
    private Map<Device, Set<GroupWidget>> devices;
    private final DeviceService deviceService;

    public DevicesImageOverlayMap(DeviceCustomMap entity,
            Map<Device, Set<GroupWidget>> devices, boolean editable) {
        this(entity, devices, editable, null);
    }

    public DevicesImageOverlayMap(DeviceCustomMap entity,
            Map<Device, Set<GroupWidget>> devices, boolean editable, DeviceService deviceService) {
        super(entity.getImage(), entity.getIMarkers(), editable);
        this.entity = entity;
        this.devices = devices;
        this.deviceService = deviceService;
        initContent(-1, -1);
    }

    public Device getDevice(String serial) {
        for (Device device : devices.keySet()) {
            if (Objects.equals(device.getSerial(), serial)) {
                return device;
            }
        }
        if (deviceService == null) {
            return null;
        }
        return deviceService.findBySerial(serial);
    }

    @Override
    protected Marker createMarker(Point point, String markerId) {
        Device device = getDevice(markerId);
        if (device != null) {
            final Marker marker = new Marker(new LatLng(point.getX(), point.getY()));
            marker.onClick(event -> openDrawer(markerId));
            Icon icon = new Icon(MapUtils.getIconUrl(device));
            marker.setIcon(icon);
            return marker;
        }
        return null;
    }

    @Override
    protected void setImage(ImageData image) {
        entity.setImage(image);
    }

    private Component createInfo(Device device) {
        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        if (device == null) {
            return info;
        }
        info.add(device.getSerial() != null ? new com.vaadin.flow.component.html.Span(device.getSerial())
                : new com.vaadin.flow.component.html.Span(""));
        info.add(device.getLabel() != null ? new com.vaadin.flow.component.html.Span(device.getLabel())
                : new com.vaadin.flow.component.html.Span(""));
        return info;
    }

    private void openDrawer(String markerId) {
        Device device = getDevice(markerId);
        Component info = createInfo(device);

        SideDrawer drawer = new SideDrawer(getI18nLabel("title"));

        Button close = new Button(VaadinIcon.CLOSE.create(), event -> drawer.close());

        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);
        layout.add(close);
        layout.setHorizontalComponentAlignment(Alignment.END, close);
        layout.add(info);
        layout.setHorizontalComponentAlignment(Alignment.CENTER, info);
        layout.setFlexGrow(1f, info);
        layout.setId(markerId);

        drawer.setDrawerContent(layout);
        drawer.open();
    }

    private void openVisualization(GroupWidget widget) {
        // TODO(flow-migration): replace legacy parent TabSheet flow with Flow tabs.
        PopupNotification.show("Visualization tab migration pending", PopupNotification.Type.WARNING);
    }
}
