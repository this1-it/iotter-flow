package it.thisone.iotter.ui.maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.flow.components.TabSheet;

import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.data.provider.ListDataProvider;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.DeviceCustomMap;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.GroupWidgetService;
import it.thisone.iotter.persistence.service.NetworkGroupService;
import it.thisone.iotter.persistence.service.NetworkService;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.common.AuthenticatedUser;
import it.thisone.iotter.ui.common.BaseEditor;
import it.thisone.iotter.ui.common.ConfirmationDialog;
import it.thisone.iotter.ui.eventbus.DeviceGroupWidgetEvent;
import it.thisone.iotter.ui.eventbus.PendingChangesEvent;
import it.thisone.iotter.ui.eventbus.UIEventBus;
import it.thisone.iotter.ui.groupwidgets.GroupWidgetVisualizer;
import it.thisone.iotter.util.MapUtils;

public class GroupWidgetsCustomMap extends BaseEditor<Network> {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(GroupWidgetsCustomMap.class);

    private final boolean editable;
    private final NetworkService networkService;
    private final DeviceService deviceService;
    private final NetworkGroupService networkGroupService;
    private final GroupWidgetService groupWidgetService;
    private final UIEventBus uiEventBus;
    private final ObjectProvider<DevicesImageOverlayMap> devicesImageOverlayMapProvider;

    @Autowired
	private AuthenticatedUser authenticatedUser;

    private Network network;
    private Map<Device, Set<GroupWidget>> map;

    private TabSheet tabsheet;
    private ComboBox<DeviceCustomMap> comboBox;
    private ListDataProvider<DeviceCustomMap> dataProvider;
    private HorizontalLayout footer;
    private VerticalLayout imageLayout;

    public GroupWidgetsCustomMap(String networkId, boolean editable) {
        this(networkId, editable, null, null, null, null, null, null, null);
    }

    public GroupWidgetsCustomMap(String networkId, boolean editable,
            NetworkService networkService,
            DeviceService deviceService,
            NetworkGroupService networkGroupService,
            GroupWidgetService groupWidgetService,
            AuthenticatedUser authenticatedUser,
            UIEventBus uiEventBus,
            ObjectProvider<DevicesImageOverlayMap> devicesImageOverlayMapProvider) {
        super("groupwidgets.custommap");
        this.editable = editable;
        this.networkService = networkService;
        this.deviceService = deviceService;
        this.networkGroupService = networkGroupService;
        this.groupWidgetService = groupWidgetService;
        this.uiEventBus = uiEventBus;
        this.devicesImageOverlayMapProvider = devicesImageOverlayMapProvider;

        if (networkService != null) {
            network = networkService.findOne(networkId);
        }
        if (network == null) {
            network = new Network();
            network.setId(networkId);
            // TODO(flow-migration): provide NetworkService from parent to load map data.
        }
        setItem(network);

        UserDetailsAdapter details = authenticatedUser.get().orElse(null);
        map = network.getId() != null ? MapUtils.mappableDevices(network,details) : new HashMap<>();

        buildLayout();
        init();
    }

    private void buildLayout() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(true);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setSpacing(true);
        toolbar.setPadding(true);

        comboBox = createComboBox();
        comboBox.setWidthFull();
        toolbar.add(comboBox);
        toolbar.setFlexGrow(1f, comboBox);

        HorizontalLayout buttonbar = createButtonbar();
        buttonbar.add(createAddButton());
        buttonbar.setVisible(editable);
        toolbar.add(buttonbar);

        imageLayout = new VerticalLayout();
        imageLayout.setSizeFull();
        imageLayout.setPadding(false);
        imageLayout.setSpacing(false);

        footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setPadding(true);
        footer.setSpacing(true);
        footer.setVisible(editable);

        mainLayout.add(toolbar, imageLayout, footer);
        mainLayout.setFlexGrow(1f, imageLayout);

        tabsheet = new TabSheet();
        tabsheet.setSizeFull();
        String caption = network != null && network.getName() != null ? network.getName() : getI18nLabel("title");
        Tab tab = tabsheet.addTab(caption, mainLayout);
        tabsheet.setSelectedTab(tab);

        setRootComposition(tabsheet);
    }

    private void init() {
        if (network.getCustomMaps() != null && !network.getCustomMaps().isEmpty()) {
            DeviceCustomMap defaultMap = network.getCustomMaps().get(0);
            for (DeviceCustomMap custom : network.getCustomMaps()) {
                if (custom.isDefaultMap()) {
                    defaultMap = custom;
                    break;
                }
            }
            comboBox.setValue(defaultMap);
        } else {
            footer.removeAll();
            imageLayout.removeAll();
            if (editable) {
                openEditor(null, getI18nLabel("create_dialog"));
            } else {
                imageLayout.add(new Span(getI18nLabel("missing_image")));
            }
        }
    }

    private void showMap(DeviceCustomMap custom) {
        if (custom == null) {
            imageLayout.setVisible(false);
            footer.setVisible(false);
            return;
        }

        DevicesImageOverlayMap imageMap;
        if (devicesImageOverlayMapProvider != null) {
            imageMap = devicesImageOverlayMapProvider.getObject(custom, map, editable, deviceService);
        } else {
            imageMap = new DevicesImageOverlayMap(custom, map, editable, deviceService);
        }
        footer.removeAll();
        imageLayout.removeAll();
        imageLayout.add(imageMap);

        if (editable) {
            HorizontalLayout editorLayout = new HorizontalLayout();
            editorLayout.setSpacing(true);

            Button modify = createModifyButton(custom);
            Button remove = createRemoveButton(custom);
            Button upload = imageMap.createImageButton();

            modify.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            remove.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            upload.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            editorLayout.add(modify, remove, upload);
            footer.add(editorLayout);
        }

        footer.setVisible(editable);
        imageLayout.setVisible(true);
    }

    private Button createAddButton() {
        Button button = new Button();
        button.setIcon(VaadinIcon.PLUS.create());
        button.getElement().setProperty("title", getI18nLabel("add_action"));
        button.setId("add_map");
        button.addClickListener(event -> openEditor(null, getI18nLabel("create_dialog")));
        return button;
    }

    private Button createModifyButton(final DeviceCustomMap entity) {
        Button button = new Button();
        button.setIcon(VaadinIcon.EDIT.create());
        button.getElement().setProperty("title", getI18nLabel("modify_button"));
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        button.addClickListener(event -> openEditor(entity, getI18nLabel("modify_dialog")));
        return button;
    }

    private Button createRemoveButton(final DeviceCustomMap entity) {
        Button button = new Button();
        button.setIcon(VaadinIcon.TRASH.create());
        button.getElement().setProperty("title", getI18nLabel("remove_button"));
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        button.addClickListener(event -> openRemove(entity, getI18nLabel("remove_dialog")));
        return button;
    }

    private ComboBox<DeviceCustomMap> createComboBox() {
        ComboBox<DeviceCustomMap> combo = new ComboBox<>();
        dataProvider = createDataProvider();
        combo.setDataProvider(dataProvider);
        combo.setItemLabelGenerator(DeviceCustomMap::getName);
        combo.setAllowCustomValue(false);
        combo.addValueChangeListener(event -> showMap(event.getValue()));
        return combo;
    }

    private ListDataProvider<DeviceCustomMap> createDataProvider() {
        List<DeviceCustomMap> items = new ArrayList<>();
        if (network.getCustomMaps() != null) {
            for (DeviceCustomMap custom : network.getCustomMaps()) {
                if (custom.getExternalId() == null) {
                    custom.setExternalId(UUID.randomUUID().toString());
                }
                items.add(custom);
            }
        }

        items.sort((a, b) -> Boolean.compare(b.isDefaultMap(), a.isDefaultMap()));
        return new ListDataProvider<>(items);
    }

    private void openRemove(DeviceCustomMap entity, String label) {
        if (entity == null) {
            return;
        }

        ConfirmationDialog.Callback callback = result -> {
            if (result) {
                network.getCustomMaps().remove(entity);
                dataProvider.getItems().remove(entity);
                dataProvider.refreshAll();
                init();
            }
        };

        String caption = getTranslation("basic.editor.are_you_sure");
        Dialog dialog = new ConfirmationDialog(caption, label, callback);
        dialog.open();
    }

    private void openEditor(DeviceCustomMap entity, String label) {
        DeviceCustomMap current = entity;
        if (current == null) {
            current = new DeviceCustomMap();
            current.setExternalId(UUID.randomUUID().toString());
            current.setOwner(network.getOwner());
            current.setNetwork(network);
        }

        DeviceCustomMapForm content = new DeviceCustomMapForm(current, network, networkGroupService, deviceService);
        Dialog dialog = createDialog(label, content);

        content.setSavedHandler(itemId -> {
            if (itemId != null) {
                if (!network.getCustomMaps().contains(itemId)) {
                    network.getCustomMaps().add(itemId);
                    dataProvider.getItems().add(itemId);
                    dataProvider.refreshAll();
                    comboBox.setValue(itemId);
                } else {
                    showMap(itemId);
                }

                if (itemId.isDefaultMap()) {
                    for (DeviceCustomMap custom : network.getCustomMaps()) {
                        if (!custom.equals(itemId)) {
                            custom.setDefaultMap(false);
                        }
                    }
                }
            }
            dialog.close();
        });

        dialog.open();
    }

    @Override
    protected void onSave() {
        if (networkService == null) {
            // TODO(flow-migration): inject NetworkService from parent to persist custom maps.
            return;
        }
        try {
            networkService.update(network);
        } catch (BackendServiceException e) {
            logger.error("Unable to save custom map", e);
        }
    }

    @Override
    protected void onCancel() {
        if (networkService == null || network.getId() == null) {
            return;
        }
        try {
            network = networkService.findOne(network.getId());
            networkService.update(network);
        } catch (BackendServiceException e) {
            logger.error("Unable to reload custom map", e);
        }
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
                            GroupWidgetVisualizer content = new GroupWidgetVisualizer(
                                    groupWidget.getId().toString(), true, groupWidgetService);
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

    @Subscribe
    public void pendingChanges(PendingChangesEvent event) {
        setPendingChanges(true);
        if (getSaveButton() != null) {
            getSaveButton().addClassName("pending-changes");
        }
    }
}
