package it.thisone.iotter.ui.maps;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.vaadin.addons.componentfactory.leaflet.LeafletMap;
import org.vaadin.addons.componentfactory.leaflet.layer.raster.ImageOverlay;
import org.vaadin.addons.componentfactory.leaflet.layer.ui.marker.Marker;
import org.vaadin.addons.componentfactory.leaflet.types.LatLng;
import org.vaadin.addons.componentfactory.leaflet.types.LatLngBounds;
import org.vaadin.addons.componentfactory.leaflet.types.Point;



import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import it.thisone.iotter.persistence.ifc.IMarker;
import it.thisone.iotter.persistence.model.ImageData;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.common.EditorSavedEvent;
import it.thisone.iotter.ui.common.fields.EditableImage;

public abstract class ImageOverlayMap extends BaseComponent {
    private float width;
    private float height;
    private float originalWidth;
    private float originalHeight;
    private static final long serialVersionUID = 1L;

    private VerticalLayout mainLayout;
    private LeafletMap leafletMap;
    private final boolean editable;
    private ImageData image;
    private final List<IMarker> imarkers;
    private List<Marker> markers;
    private final Map<String, Marker> markerById = new HashMap<>();
    private float mapHeight;
    private float mapWidth;

    public ImageOverlayMap(ImageData image, List<IMarker> imarkers, boolean editable) {
        super("groupwidgets.custommap", UUID.randomUUID().toString());
        this.editable = editable;
        this.image = image;
        this.imarkers = imarkers;
        this.markers = new ArrayList<>();
    }

    protected abstract Marker createMarker(Point point, String markerId);

    protected abstract void setImage(ImageData image);

    protected void initContent(float mapWidth, float mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setSizeFull();

        if (image != null && image.getData().length > 0) {
            openLeafletMap();
        } else {
            mainLayout.removeAll();
            Span missing = new Span(getI18nLabel("missing_image"));
            mainLayout.add(missing);
            mainLayout.setHorizontalComponentAlignment(Alignment.CENTER, missing);
            setRootComposition(mainLayout);
        }
    }

    public Button createImageButton() {
        Button button = new Button(VaadinIcon.UPLOAD.create());
        button.addClassName("borderless");
        button.getElement().setAttribute("title", getI18nLabel("upload_image_button"));
        button.addClickListener(event -> openEditableImage());
        return button;
    }

    private void openLeafletMap() {
        mainLayout.removeAll();
        leafletMap = createLeafletMap();
        mainLayout.add(leafletMap);
        mainLayout.setFlexGrow(1.0f, leafletMap);
        setRootComposition(mainLayout);
    }

    private String getImageResourceUrl(final ImageData imageData) {
        StreamResource resource = new StreamResource(imageData.getFilename(),
                () -> new ByteArrayInputStream(imageData.getData()));
        StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        return registration.getResourceUri().toString();
    }

    private LeafletMap createLeafletMap() {
        width = image.getWidth();
        height = image.getHeight();

        originalWidth = width;
        originalHeight = height;

        LeafletMap createdMap = new LeafletMap();
        if (image == null) {
            return createdMap;
        }

        String imageUrl = getImageResourceUrl(image);

        if (width > mapWidth && mapWidth > 0) {
            width = mapWidth;
            height = Math.round(width * originalHeight / originalWidth);
            mapHeight = height;
        }

        if (height > mapHeight && mapHeight > 0) {
            height = mapHeight;
            width = Math.round(height * originalWidth / originalHeight);
        }

        LatLngBounds bounds = new LatLngBounds(new LatLng(0, 0), new LatLng(height, width));

        ImageOverlay imageOverlay = new ImageOverlay(imageUrl);
        imageOverlay.setBounds(bounds);
        createdMap.addLayer(imageOverlay);

        if (editable) {
            LatLngBounds maxBounds = new LatLngBounds(new LatLng(0, 0), new LatLng(height, width));
            createdMap.setMaxBounds(maxBounds);
            createdMap.setView(new LatLng(height / 2, width / 2), 0);
        }

        createdMap.setMinZoom(0);
        createdMap.setMaxZoom(0);

        markers = new ArrayList<>();
        markerById.clear();
        for (IMarker imarker : imarkers) {
            Point point = getMarkerPoint(imarker.getMarkerId());
            if (point == null) {
                point = new Point(height / 2, width / 2);
            }
            Marker marker = createMarker(point, imarker.getMarkerId());
            if (marker != null) {
                createdMap.addLayer(marker);
                markers.add(marker);
                markerById.put(imarker.getMarkerId(), marker);
                // TODO(flow-migration): restore drag listeners with Flow Leaflet events.
            }
        }

        return createdMap;
    }

    public LeafletMap getLeafletMap() {
        return leafletMap;
    }

    private void saveImageData(ImageData img) {
        if (img.getData().length == 0) {
            image = img;
        } else if (image == null) {
            // TODO(flow-migration): inject ImageDataService from parent instead of UIUtils
            // service lookups.
            image = img;
        } else {
            if (image.getData().length != img.getData().length) {
                image.setData(img.getData());
                image.setFilename(img.getFilename());
                image.setHeight(img.getHeight());
                image.setWidth(img.getWidth());
                image.setMimetype(img.getMimetype());
            }
        }
        setImage(image);
        if (image != null && image.getData().length > 0) {
            openLeafletMap();
        } else {
            mainLayout.removeAll();
            Span missing = new Span(getI18nLabel("missing_image"));
            mainLayout.add(missing);
            mainLayout.setHorizontalComponentAlignment(Alignment.CENTER, missing);
            setRootComposition(mainLayout);
        }
    }

    public void openEditableImage() {
        EditableImage content = new EditableImage(image);
        Dialog dialog = createDialog(getI18nLabel("dialog_upload_image"), content);
        // content.addListener(EditorSavedEvent.class, event -> {
        //     if (event.getSavedItem() instanceof ImageData) {
        //         saveImageData((ImageData) event.getSavedItem());
        //         // TODO(flow-migration): inject and post PendingChangesEvent via UIEventBus.
        //     }
        //     dialog.close();
        // });
        dialog.open();
    }

    private void setMarkerPoint(String markerId, Marker marker) {
        for (IMarker imarker : imarkers) {
            if (imarker.getMarkerId().equals(markerId)) {
                float x = marker.getLatLng().getLng().floatValue();
                float y = marker.getLatLng().getLat().floatValue();
                imarker.setX(resize(x, width, originalWidth));
                imarker.setY(resize(y, height, originalHeight));
                // TODO(flow-migration): inject and post PendingChangesEvent via UIEventBus.
            }
        }
    }

    private Point getMarkerPoint(String markerId) {
        for (IMarker imarker : imarkers) {
            if (imarker.getMarkerId().equals(markerId)) {
                if (imarker.getX() != 0 && imarker.getY() != 0) {
                    float y = resize(imarker.getY(), originalHeight, height);
                    float x = resize(imarker.getX(), originalWidth, width);
                    return new Point(y, x);
                }
            }
        }
        return null;
    }

    private float resize(float dimension, float original, float current) {
        return Math.round(((dimension / original) * current));
    }

    protected Marker getMarker(String markerId) {
        return markerById.get(markerId);
    }

    protected void syncMarkerPoint(String markerId) {
        Marker marker = markerById.get(markerId);
        if (marker != null) {
            setMarkerPoint(markerId, marker);
        }
    }
}
