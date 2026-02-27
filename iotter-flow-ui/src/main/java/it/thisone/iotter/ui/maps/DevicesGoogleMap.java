package it.thisone.iotter.ui.maps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vaadin.firitin.form.AbstractForm;

import com.flowingcode.vaadin.addons.googlemaps.GoogleMap;
import com.flowingcode.vaadin.addons.googlemaps.GoogleMap.MapType;
import com.flowingcode.vaadin.addons.googlemaps.GoogleMapMarker;
import com.flowingcode.vaadin.addons.googlemaps.LatLon;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GeoLocation;
import it.thisone.iotter.persistence.model.GeoMapPreferences;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.NetworkService;
import it.thisone.iotter.ui.common.BaseEditor;
import it.thisone.iotter.ui.common.SideDrawer;
import it.thisone.iotter.ui.common.fields.GeoLocationForm;
import it.thisone.iotter.util.GeoLocationUtil;

/**
 * TODO(flow-migration): marker click listeners and widget visualizer integration
 * still need full Flow parity validation.
 */
public class DevicesGoogleMap extends BaseEditor<Network> {
    private static final long serialVersionUID = 1L;

    public static final int MAP_ZOOM = 12;

    private final DeviceService deviceService;
    private final NetworkService networkService;
    private final String googleMapApiKey;

    private final VerticalLayout mainLayout;
    private GoogleMap googleMap;
    private final Map<String, Device> deviceMarker = new HashMap<>();
    private final Map<Device, Set<GroupWidget>> map;
    private final boolean editable;
    private final boolean showInfoWindow;

    private Network network;
    private SideDrawer infoDrawer;

    public DevicesGoogleMap(Network network, boolean editable, boolean infoWindow,
            DeviceService deviceService, NetworkService networkService, String googleMapApiKey) {
        this(network != null ? network.getId() : null,
                network != null ? network.getName() : null,
                buildDevicesMap(network, deviceService),
                network,
                editable,
                infoWindow,
                deviceService,
                networkService,
                googleMapApiKey);
    }

    public DevicesGoogleMap(Network network, boolean editable, boolean infoWindow) {
        this(network, editable, infoWindow, null, null, "");
    }

    public DevicesGoogleMap(String id, String caption, Map<Device, Set<GroupWidget>> devicesMap, Network network,
            boolean editable, boolean infoWindow) {
        this(id, caption, devicesMap, network, editable, infoWindow, null, null, "");
    }

    public DevicesGoogleMap(String id, String caption, Map<Device, Set<GroupWidget>> devicesMap, Network network,
            boolean editable, boolean infoWindow, DeviceService deviceService, NetworkService networkService,
            String googleMapApiKey) {
        super(MAPS_DEVICES_GOOGLE);
        if (id != null) {
            setId(id);
        }

        this.deviceService = deviceService;
        this.networkService = networkService;
        //this.googleMapApiKey = googleMapApiKey == null ? "" : googleMapApiKey;
        this.googleMapApiKey = "AIzaSyDOrimPpDgaMTm82IiQbkQ9myuLV-DgIx0";
        this.network = network;
        this.map = devicesMap != null ? devicesMap : new HashMap<>();
        this.editable = editable;
        this.showInfoWindow = infoWindow;

        GeoLocation center = null;
        if (network != null) {
            GeoMapPreferences mapPreferences = network.getMapPreferences();
            if (mapPreferences != null && mapPreferences.getLatitude() != null && mapPreferences.getLongitude() != null) {
                center = new GeoLocation();
                center.setLatitude(mapPreferences.getLatitude());
                center.setLongitude(mapPreferences.getLongitude());
            }
        }
        if (center == null) {
            center = findCenterPoint(map.keySet());
        }

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setSizeFull();

        if (center == null || map.isEmpty()) {
            openMissing();
        } else {
            openGoogleMap(center);
        }

        setRootComposition(mainLayout);
    }

    private static Map<Device, Set<GroupWidget>> buildDevicesMap(Network network, DeviceService deviceService) {
        Map<Device, Set<GroupWidget>> devicesMap = new HashMap<>();
        if (network == null || deviceService == null) {
            return devicesMap;
        }
        List<Device> devices = deviceService.findByNetwork(network);
        for (Device device : devices) {
            if (device != null && device.getMaster() == null) {
                devicesMap.put(device, new HashSet<>());
            }
        }
        return devicesMap;
    }

    private void openMissing() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        if (map.isEmpty()) {
            layout.add(new Span(getI18nLabel("missing_devices")));
        } else {
            layout.add(new Span(getI18nLabel("missing_geolocation_information")));
            if (editable) {
                GeoLocationForm location = new GeoLocationForm();
                //location.setCaption(getI18nLabel("choose_map_center"));
                location.setEntity(new GeoLocation());
                location.setSavedHandler((AbstractForm.SavedHandler<GeoLocation>) this::openGoogleMap);
                layout.add(location);
            }
        }

        mainLayout.removeAll();
        mainLayout.add(layout);
        mainLayout.setFlexGrow(1f, layout);
    }

    private void openGoogleMap(GeoLocation center) {
        mainLayout.removeAll();
        if (editable) {
            mainLayout.add(createToolbar());
        }
        googleMap = createGoogleMap(map.keySet(), center, editable, showInfoWindow);
        mainLayout.add(googleMap);
        mainLayout.setFlexGrow(1f, googleMap);
    }

    private Component createInfo(GoogleMapMarker clickedMarker) {
        Device device = getDevice(clickedMarker.getId().get());
        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        if (device == null) {
            info.add(new Span(getI18nLabel("missing_devices")));
            return info;
        }
        info.add(new Span(device.getSerial() != null ? device.getSerial() : ""));
        info.add(new Span(device.getLabel() != null ? device.getLabel() : ""));
        return info;
    }

    private GeoLocation findCenterPoint(Collection<Device> devices) {
        List<GeoLocation> locations = new ArrayList<>();
        for (Device device : devices) {
            GeoLocation location = device.getLocation();
            if (location != null && !location.isUndefined()) {
                locations.add(device.getLocation());
            }
        }
        if (locations.isEmpty()) {
            return null;
        }
        return GeoLocationUtil.getCenterPoint(locations);
    }

    private GoogleMap createGoogleMap(Collection<Device> devices, GeoLocation center, boolean draggable,
            boolean openInfoWindow) {
        GoogleMap createdMap = new GoogleMap(googleMapApiKey, null, null);
        createdMap.setWidthFull();
        createdMap.setHeight("600px");
        createdMap.setCenter(new LatLon(center.getLatitude(), center.getLongitude()));

        if (network != null) {
            MapType type = MapType.ROADMAP;
            createdMap.setMapType(type);
            GeoMapPreferences mapPreferences = network.getMapPreferences();
            if (mapPreferences != null) {
                if (mapPreferences.getType() != null) {
                    try {
                        type = MapType.valueOf(mapPreferences.getType());
                        createdMap.setMapType(type);
                    } catch (IllegalArgumentException e) {
                        // keep default map type
                    }
                }
                if (mapPreferences.getZoom() != null) {
                    createdMap.setZoom(mapPreferences.getZoom());
                }

                if (mapPreferences.getLatitude() != null && mapPreferences.getLongitude() != null) {
                    center.setLatitude(mapPreferences.getLatitude());
                    center.setLongitude(mapPreferences.getLongitude());
                    createdMap.setCenter(new LatLon(mapPreferences.getLatitude(), mapPreferences.getLongitude()));
                }
            }
        }

        for (Device device : devices) {
            GeoLocation location = (device.getLocation() == null || device.getLocation().isUndefined())
                    ? center
                    : device.getLocation();
            LatLon position = new LatLon(location.getLatitude(), location.getLongitude());
            GoogleMapMarker marker = new GoogleMapMarker(device.toString(), position, draggable);
            marker.setId(device.getId());
            marker.setAnimationEnabled(true);
            deviceMarker.put(marker.getId().get(), device);
            createdMap.addMarker(marker);
        }

        // TODO(flow-migration): re-enable marker click integration after GoogleMap listener
        // migration is validated.
        return createdMap;
    }

    @Override
    protected void onSave() {
        if (network != null && networkService != null && googleMap != null) {
            GeoMapPreferences mapPreferences = new GeoMapPreferences();
            mapPreferences.setType(googleMap.getMapType().name());
            mapPreferences.setZoom(googleMap.getZoom());
            mapPreferences.setLatitude(googleMap.getCenter().getLat());
            mapPreferences.setLongitude(googleMap.getCenter().getLon());
            network.setMapPreferences(mapPreferences);
            try {
                networkService.update(network);
            } catch (Exception e) {
                // TODO(flow-migration): surface error with a consistent notification strategy.
            }
        }
    }

    public GoogleMap getGoogleMap() {
        return googleMap;
    }

    public Device getDevice(String markerId) {
        return deviceMarker.get(markerId);
    }

    private void openInfoWindow(GoogleMapMarker clickedMarker) {
        openDrawer(clickedMarker);
    }

    private void openDrawer(GoogleMapMarker clickedMarker) {
        if (infoDrawer != null && infoDrawer.isOpened()) {
            infoDrawer.close();
        }
        SideDrawer drawer = new SideDrawer(getI18nLabel("title"));
        drawer.setDrawerContent(createContent(clickedMarker, drawer));
        drawer.open();
        infoDrawer = drawer;
    }

    public Component createContent(GoogleMapMarker clickedMarker, final SideDrawer drawer) {
        Component info = createInfo(clickedMarker);
        Button close = new Button(VaadinIcon.CLOSE.create(), event -> drawer.close());

        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);
        layout.add(close, info);
        layout.setHorizontalComponentAlignment(Alignment.END, close);
        layout.setHorizontalComponentAlignment(Alignment.CENTER, info);
        layout.setFlexGrow(1f, info);
        layout.setId(String.valueOf(clickedMarker.getId()));
        return layout;
    }

    @Override
    protected void onCancel() {
    }
}
