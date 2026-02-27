package it.thisone.iotter.ui.maps;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;

import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.DeviceCustomMap;
import it.thisone.iotter.persistence.model.DeviceWidget;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.NetworkGroupService;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.fields.DeviceWidgetSelect;

public class DeviceCustomMapForm extends AbstractBaseEntityForm<DeviceCustomMap> {

    private static final long serialVersionUID = 1L;

    @PropertyId("name")
    private TextField name;

    @PropertyId("defaultMap")
    private Checkbox defaultMap;

    @PropertyId("widgets")
    private DeviceWidgetSelect widgets;

    private final Network network;
    private final NetworkGroupService networkGroupService;
    private final DeviceService deviceService;
    private boolean fieldsInitialized;
    private boolean bindingsInitialized;

    public DeviceCustomMapForm(DeviceCustomMap entity, Network network) {
        this(entity, network, null, null, false);
    }

    public DeviceCustomMapForm(DeviceCustomMap entity, Network network, NetworkGroupService networkGroupService,
            DeviceService deviceService) {
        this(entity, network, networkGroupService, deviceService, false);
    }

    public DeviceCustomMapForm(DeviceCustomMap entity, Network network, NetworkGroupService networkGroupService,
            DeviceService deviceService, boolean readOnly) {
        super(entity, DeviceCustomMap.class, "groupwidgets.custommap", network, null, readOnly);
        this.network = network;
        this.networkGroupService = networkGroupService;
        this.deviceService = deviceService;
        ensureFieldsInitialized();
        ensureBindingsInitialized();
        getBinder().readBean(entity);
    }

    private void ensureFieldsInitialized() {
        if (fieldsInitialized) {
            return;
        }
        initializeFields();
        fieldsInitialized = true;
    }

    private void ensureBindingsInitialized() {
        if (bindingsInitialized) {
            return;
        }
        bindFields();
        bindingsInitialized = true;
    }

    @Override
    protected void initializeFields() {
        DeviceCustomMap entity = getEntity();
        name = new TextField();
        name.setLabel(getI18nLabel("name"));
        name.setSizeFull();
        name.setRequiredIndicatorVisible(true);
        name.setReadOnly(isReadOnly());

        defaultMap = new Checkbox(getI18nLabel("defaultMap"));
        defaultMap.setReadOnly(isReadOnly());

        widgets = new DeviceWidgetSelect();
        widgets.setRows(5);
        widgets.setLeftColumnCaption(getI18nLabel("available_devices"));
        widgets.setRightColumnCaption(getI18nLabel("map_devices"));
        widgets.setReadOnly(isReadOnly());

        widgets.setOptions(loadAvailableWidgets());
        if (entity.getWidgets() != null) {
            widgets.setValue(entity.getWidgets());
        }
    }

    private Set<DeviceWidget> loadAvailableWidgets() {
        if (network == null || networkGroupService == null || deviceService == null) {
            // TODO(flow-migration): provide services from parent when this form is used.
            return new HashSet<>();
        }
        List<NetworkGroup> groups = networkGroupService.findByNetwork(network);
        Map<Device, Set<GroupWidget>> map = deviceService.findMappableDevices(groups);
        Set<DeviceWidget> options = new HashSet<>();
        for (Device device : map.keySet()) {
            options.add(new DeviceWidget(device.getLabel(), device.getSerial()));
        }
        return options;
    }

    @Override
    protected void bindFields() {
        ensureFieldsInitialized();
        Binder<DeviceCustomMap> binder = getBinder();

        binder.forField(name)
                .asRequired(getTranslation("validators.fieldgroup_errors"))
                .bind(DeviceCustomMap::getName, DeviceCustomMap::setName);

        binder.forField(defaultMap)
                .bind(DeviceCustomMap::isDefaultMap, DeviceCustomMap::setDefaultMap);

//        binder.forField(widgets)
//                .bind(DeviceCustomMap::getWidgets, DeviceCustomMap::setWidgets);
    }

    @Override
    public VerticalLayout getFieldsLayout() {
        ensureFieldsInitialized();
        VerticalLayout mainLayout = buildMainLayout();
        mainLayout.setPadding(true);

        FormLayout formLayout = new FormLayout();
        
        formLayout.add(name, widgets, defaultMap);

        mainLayout.add(formLayout);
        mainLayout.setFlexGrow(1f, formLayout);
        mainLayout.setHorizontalComponentAlignment(Alignment.CENTER, formLayout);
        return mainLayout;
    }

    @Override
    protected void afterCommit() {
    }

    @Override
    protected void beforeCommit() throws EditorConstraintException {
    }


}
