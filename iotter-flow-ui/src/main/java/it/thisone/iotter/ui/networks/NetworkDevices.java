package it.thisone.iotter.ui.networks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;

import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.ui.common.BaseEditor;
import it.thisone.iotter.ui.common.ConfirmationDialog;
import it.thisone.iotter.ui.common.ConfirmationDialog.Callback;
import it.thisone.iotter.util.PopupNotification;

/**
 *
 * Feature #298 Disassocia strumento dalla rete
 *
 */
public class NetworkDevices extends BaseEditor<Network> {

    private static final long serialVersionUID = 4271498822795054411L;

    private final DeviceService deviceService;
    private final Network network;

    private Grid<Device> leftGrid;
    private Grid<Device> rightGrid;
    private ListDataProvider<Device> leftDataProvider;
    private ListDataProvider<Device> rightDataProvider;

    public NetworkDevices(Network item, DeviceService deviceService) {
        super("device.editor", "network.devices");
        setItem(item);
        this.network = item;
        this.deviceService = Objects.requireNonNull(deviceService, "deviceService is required");

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);

        Component content = buildContent();
        mainLayout.add(content);
        mainLayout.setFlexGrow(1f, content);

        mainLayout.add(buildFooter());
        setRootComposition(mainLayout);
    }

    private Component buildContent() {
        HorizontalLayout content = new HorizontalLayout();
        content.setPadding(true);
        content.setSizeFull();
        content.setDefaultVerticalComponentAlignment(Alignment.START);

        List<Device> rightItems = new ArrayList<>();
        List<Device> leftItems = new ArrayList<>();
        rightDataProvider = new ListDataProvider<>(rightItems);
        leftDataProvider = new ListDataProvider<>(leftItems);

        VerticalLayout left = new VerticalLayout();
        left.setDefaultHorizontalComponentAlignment(Alignment.START);
        left.setSizeFull();

        VerticalLayout right = new VerticalLayout();
        right.setDefaultHorizontalComponentAlignment(Alignment.START);
        right.setSizeFull();

        VerticalLayout center = new VerticalLayout();
        center.setSizeFull();
        center.setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        Button moveRightButton = new Button(VaadinIcon.ARROW_CIRCLE_RIGHT_O.create());
        moveRightButton.setEnabled(false);

        Button moveLeftButton = new Button(VaadinIcon.ARROW_CIRCLE_LEFT_O.create());
        moveLeftButton.setEnabled(false);

        VerticalLayout buttons = new VerticalLayout(moveRightButton, moveLeftButton);
        buttons.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        buttons.addClassName(BUTTONS_STYLE);
        center.add(buttons);

        content.add(left, center, right);
        content.setFlexGrow(0.5f, left);
        content.setFlexGrow(0.05f, center);
        content.setFlexGrow(0.5f, right);

        List<Device> disconnected = new ArrayList<>();
        List<Device> connected = new ArrayList<>();
        List<Device> devices = deviceService.findByOwner(network.getOwner());

        for (Device device : devices) {
            if (device.getMaster() == null) {
                if (device.getNetwork() == null) {
                    disconnected.add(device);
                } else if (device.getNetwork().equals(network)) {
                    connected.add(device);
                }
            }
        }

        Comparator<Device> bySerial = Comparator.comparing(
                Device::getSerial,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        disconnected.sort(bySerial);
        connected.sort(bySerial);

        leftItems.addAll(disconnected);
        leftGrid = createGrid(leftDataProvider);
        Span leftTitle = new Span(getI18nLabel("disconnected_devices"));

        left.add(leftTitle, createFilterTextField(leftGrid), leftGrid);
        left.setFlexGrow(1f, leftGrid);
        left.setSpacing(true);

        rightItems.addAll(connected);
        rightGrid = createGrid(rightDataProvider);
        Span rightTitle = new Span(getI18nLabel("connected_devices") + " " + network.getName());

        right.add(rightTitle, createFilterTextField(rightGrid), rightGrid);
        right.setFlexGrow(1f, rightGrid);
        right.setSpacing(true);

        leftGrid.asSingleSelect().addValueChangeListener(event -> moveRightButton.setEnabled(event.getValue() != null));
        rightGrid.asSingleSelect().addValueChangeListener(event -> moveLeftButton.setEnabled(event.getValue() != null));

        moveLeftButton.addClickListener(event -> disconnectDeviceFromNetwork());
        moveRightButton.addClickListener(event -> addDeviceToNetwork());

        return content;
    }

    private void addDeviceToNetwork() {
        Device device = leftGrid.asSingleSelect().getValue();
        if (device == null) {
            return;
        }

        if (rightDataProvider.getItems().contains(device)) {
            PopupNotification.show(getI18nLabel("device_already_belong_to_network"), PopupNotification.Type.WARNING);
            return;
        }

        boolean result = deviceService.connect(device, network);

        if (result) {
            leftDataProvider.getItems().remove(device);
            rightDataProvider.getItems().add(device);
            leftDataProvider.refreshAll();
            rightDataProvider.refreshAll();
            PopupNotification.show(getI18nLabel("device_has_been_added_to_network"));
        } else {
            PopupNotification.show("Error", PopupNotification.Type.ERROR);
        }
    }

    private void disconnectDeviceFromNetwork() {
        Device device = rightGrid.asSingleSelect().getValue();
        if (device == null) {
            return;
        }

        String caption = getI18nLabel("device_disconnect");
        String message = getI18nLabel("disconnect_warning");
        Callback callback = result -> {
            if (!result) {
                return;
            }
            deviceService.disconnect(device, false);
            rightDataProvider.getItems().remove(device);
            leftDataProvider.getItems().add(device);
            rightDataProvider.refreshAll();
            leftDataProvider.refreshAll();
            PopupNotification.show(getI18nLabel("device_has_been_disconnected_from_network"));
        };

        Dialog dialog = new ConfirmationDialog(caption, message, callback);
        dialog.open();
    }

    private Grid<Device> createGrid(ListDataProvider<Device> dataProvider) {
        Grid<Device> grid = new Grid<>();
        grid.setDataProvider(dataProvider);
        grid.addColumn(Device::getSerial).setHeader(getI18nLabel("serial")).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(Device::getLabel).setHeader(getI18nLabel("label")).setAutoWidth(true).setFlexGrow(1);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setSizeFull();
        return grid;
    }

    private Component buildFooter() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        buttonLayout.setSpacing(true);

        Button closeButton = createCancelButton();
        closeButton.setIcon(null);
        closeButton.setText(getTranslation("basic.editor.close"));
        buttonLayout.add(closeButton);

        HorizontalLayout footer = new HorizontalLayout(buttonLayout);
        footer.setWidthFull();
        footer.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        footer.setJustifyContentMode(HorizontalLayout.JustifyContentMode.CENTER);
        return footer;
    }

    @Override
    protected void onSave() {
    }

    @Override
    protected void onCancel() {
    }

    public String getWindowStyle() {
        return "network-devices-editor";
    }

    private TextField createFilterTextField(Grid<Device> grid) {
        TextField tf = new TextField();
        tf.setPlaceholder(getI18nLabel("search_devices_hint"));
        tf.setWidthFull();
        tf.setValueChangeMode(ValueChangeMode.LAZY);
        tf.addValueChangeListener(event -> {
            String text = event.getValue();
            @SuppressWarnings("unchecked")
            ListDataProvider<Device> dataProvider = (ListDataProvider<Device>) grid.getDataProvider();
            applyFilter(text, dataProvider);
        });
        return tf;
    }

    private void applyFilter(String text, ListDataProvider<Device> dataProvider) {
        if (text == null || text.trim().isEmpty()) {
            dataProvider.clearFilters();
            return;
        }

        String lowerCaseFilter = text.toLowerCase();
        dataProvider.setFilter(device ->
                device.getSerial().toLowerCase().contains(lowerCaseFilter)
                        || (device.getLabel() != null && device.getLabel().toLowerCase().contains(lowerCaseFilter)));
    }

    public float[] getWindowDimension() {
        return XL_DIMENSION;
    }
}
