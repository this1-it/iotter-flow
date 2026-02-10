package it.thisone.iotter.ui.networkgroups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;

import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.common.ConfirmationDialog;
import it.thisone.iotter.ui.common.ConfirmationDialog.Callback;
import it.thisone.iotter.util.PopupNotification;

public class NetworkGroupDevices extends BaseComponent {

    private static final long serialVersionUID = 4271498822795054411L;

    private final DeviceService deviceService;
    private final NetworkGroup entity;

    private Grid<Device> leftGrid;
    private Grid<Device> rightGrid;
    private ListDataProvider<Device> leftDataProvider;
    private ListDataProvider<Device> rightDataProvider;

    private Collection<Device> alreadyConfigured;
    private String alreadyConfiguredMessage;
    private DeviceMemberListener listener;

    public NetworkGroupDevices(NetworkGroup group, DeviceService deviceService) {
        super("networkgroup.bindings", "networkgroup.devices");
        this.entity = group;
        this.deviceService = deviceService;
        buildLayout();
    }

    private void buildLayout() {
        HorizontalLayout content = new HorizontalLayout();
        content.setPadding(true);
        content.setSizeFull();
        content.setDefaultVerticalComponentAlignment(Alignment.START);

        List<Device> rightItems = new ArrayList<>();
        List<Device> leftItems = new ArrayList<>();
        rightDataProvider = new ListDataProvider<>(rightItems);
        leftDataProvider = new ListDataProvider<>(leftItems);

        VerticalLayout left = new VerticalLayout();
        left.setPadding(false);
        left.setSpacing(true);
        left.setSizeFull();

        VerticalLayout right = new VerticalLayout();
        right.setPadding(false);
        right.setSpacing(true);
        right.setSizeFull();

        VerticalLayout center = new VerticalLayout();
        center.setPadding(false);
        center.setSpacing(true);
        center.setSizeFull();
        center.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        center.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.CENTER);

        Button moveRightButton = new Button(VaadinIcon.ARROW_CIRCLE_RIGHT_O.create());
        moveRightButton.setEnabled(false);

        Button moveLeftButton = new Button(VaadinIcon.ARROW_CIRCLE_LEFT_O.create());
        moveLeftButton.setEnabled(false);
        moveLeftButton.setVisible(!entity.isDefaultGroup());

        VerticalLayout buttons = new VerticalLayout(moveRightButton, moveLeftButton);
        buttons.setPadding(false);
        buttons.setSpacing(true);
        buttons.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        buttons.addClassName(BUTTONS_STYLE);
        center.add(buttons);

        content.add(left, center, right);
        content.setFlexGrow(0.5f, left);
        content.setFlexGrow(0.05f, center);
        content.setFlexGrow(0.5f, right);

        List<Device> enabled = deviceService.findByGroup(entity);
        rightItems.addAll(enabled);
        rightGrid = createGrid(rightDataProvider, getI18nLabel("devices"));

        List<Device> all;
        String caption = getI18nLabel("all_devices") + " " + entity.getNetwork().getName();
        if (entity.isDefaultGroup()) {
            all = deviceService.findByNetwork(entity.getNetwork());
        } else {
            NetworkGroup defaultGroup = entity.getNetwork().getDefaultGroup();
            all = deviceService.findByGroup(defaultGroup);
        }
        for (Device device : all) {
            if (!enabled.contains(device) && device.isAvailableForVisualization()) {
                leftItems.add(device);
            }
        }
        leftGrid = createGrid(leftDataProvider, caption);

        left.add(createFilterTextField(leftGrid), leftGrid);
        left.setFlexGrow(1f, leftGrid);

        right.add(createFilterTextField(rightGrid), rightGrid);
        right.setFlexGrow(1f, rightGrid);

        setRootComposition(content);

        leftGrid.asSingleSelect().addValueChangeListener(event -> moveRightButton.setEnabled(event.getValue() != null));
        rightGrid.asSingleSelect().addValueChangeListener(event -> moveLeftButton.setEnabled(event.getValue() != null));

        moveLeftButton.addClickListener(event -> removeDeviceFromGroup());
        moveRightButton.addClickListener(event -> addDeviceToGroup());
    }

    private void addDeviceToGroup() {
        Device device = leftGrid.asSingleSelect().getValue();
        if (device == null) {
            return;
        }

        if (rightDataProvider.getItems().contains(device)) {
            PopupNotification.show(getI18nLabel("device_already_belong_to_group"), PopupNotification.Type.WARNING);
            return;
        }

        if (entity.isNew()) {
            addItem(device);
            return;
        }

        boolean result = deviceService.addDeviceToGroup(device, entity);
        if (result) {
            addItem(device);
            PopupNotification.show(getI18nLabel("device_has_been_added_to_group"));
        } else {
            PopupNotification.show("Error", PopupNotification.Type.ERROR);
        }
    }

    private void removeDeviceFromGroup() {
        Device device = rightGrid.asSingleSelect().getValue();
        if (device == null) {
            return;
        }

        if (alreadyConfigured != null && alreadyConfigured.contains(device)) {
            PopupNotification.show(alreadyConfiguredMessage, PopupNotification.Type.WARNING);
            return;
        }

        if (entity.isNew()) {
            removeItem(device);
            return;
        }

        String caption = getI18nLabel("remove_device_warning");
        Callback callback = result -> {
            if (!result) {
                return;
            }
            boolean removed = deviceService.removeDeviceFromGroup(device, entity);
            if (removed) {
                removeItem(device);
                PopupNotification.show(getI18nLabel("device_has_been_removed_from_group"));
            } else {
                PopupNotification.show("Error", PopupNotification.Type.ERROR);
            }
        };
        ConfirmationDialog dialog = new ConfirmationDialog(caption, caption, callback);
        dialog.open();
    }

    public void addItem(Device device) {
        leftDataProvider.getItems().remove(device);
        rightDataProvider.getItems().add(device);
        leftDataProvider.refreshAll();
        rightDataProvider.refreshAll();
        if (listener != null) {
            listener.itemsChanged(getDevices());
        }
    }

    public void removeItem(Device device) {
        rightDataProvider.getItems().remove(device);
        leftDataProvider.getItems().add(device);
        rightDataProvider.refreshAll();
        leftDataProvider.refreshAll();
        if (listener != null) {
            listener.itemsChanged(getDevices());
        }
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
        } else {
            String lowerCaseFilter = text.toLowerCase();
            dataProvider.setFilter(device -> {
                String model = device.getModel() != null ? device.getModel().getName() : "";
                return device.getSerial().toLowerCase().contains(lowerCaseFilter)
                        || (device.getLabel() != null && device.getLabel().toLowerCase().contains(lowerCaseFilter))
                        || model.toLowerCase().contains(lowerCaseFilter);
            });
        }
    }

    private Grid<Device> createGrid(ListDataProvider<Device> dataProvider, String title) {
        Grid<Device> grid = new Grid<>();
        grid.setDataProvider(dataProvider);
        grid.addColumn(Device::getSerial).setHeader(getI18nLabel("serial")).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(Device::getLabel).setHeader(getI18nLabel("label")).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(device -> device.getModel() != null ? device.getModel().getName() : "")
                .setHeader(getI18nLabel("model")).setAutoWidth(true).setFlexGrow(1);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setSizeFull();
        grid.getElement().setProperty("title", title);
        return grid;
    }

    public void setAlreadyConfiguredDevices(Collection<Device> configured, String warningMessage) {
        this.alreadyConfigured = configured;
        this.alreadyConfiguredMessage = warningMessage;
    }

    public List<Device> getDevices() {
        return Collections.unmodifiableList(new ArrayList<>(rightDataProvider.getItems()));
    }

    public interface DeviceMemberListener {
        void itemsChanged(List<Device> items);
    }

    public void addMembersListener(DeviceMemberListener listener) {
        this.listener = listener;
    }

    public void removeMembersListener(DeviceMemberListener listener) {
        this.listener = null;
    }
}
