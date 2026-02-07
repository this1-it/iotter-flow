package it.thisone.iotter.ui.devices;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.component.formlayout.FormLayout;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.NetworkGroupService;
import it.thisone.iotter.persistence.service.NetworkService;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.fields.NetworkGroupSelect;
import it.thisone.iotter.ui.common.fields.NetworkSelect;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DeviceActivatorForm extends AbstractBaseEntityForm<Device> {

	public static final String GROUPS = "groups";
	public static final String ACTIVATION_KEY = "activationKey";
	public static final String SERIAL = "serial";
	public static final String NETWORK_PROPERTY = "_network_";
    private static final long serialVersionUID = 1L;

    @PropertyId("serial")
    private TextField serial;

    @PropertyId("activationKey")
    private TextField activationKey;

    private NetworkSelect networkSelect;
    private NetworkGroupSelect groupsSelect;
    private final Network defaultNetwork;

    @Autowired
    private DeviceService deviceService;
    @Autowired
    private NetworkService networkService;
	@Autowired
    private NetworkGroupService networkGroupService;

    @Autowired
    public DeviceActivatorForm(Network network, UserDetailsAdapter currentUser) {
        super(new Device(), Device.class, "device.activator", network, currentUser, false);
        this.defaultNetwork = network;
  
        bindFields();

        configureGroups();
    }

    protected void initializeFields() {
        serial = new TextField(getI18nLabel(SERIAL));
        serial.setSizeFull();
        serial.setRequiredIndicatorVisible(true);

        activationKey = new TextField(getI18nLabel(ACTIVATION_KEY));
        activationKey.setSizeFull();
        activationKey.setRequiredIndicatorVisible(true);

        networkSelect = new NetworkSelect(loadNetworks());
        networkSelect.setSizeFull();
        networkSelect.setLabel(getI18nLabel("network"));

        List<NetworkGroup> availableGroups = loadNetworkGroups();
        groupsSelect = new NetworkGroupSelect(availableGroups, true);
        groupsSelect.setSizeFull();
        //groupsSelect.setLabel(getI18nLabel(GROUPS));
        groupsSelect.setVisible(Constants.USE_GROUPS);
    }

    private List<Network> loadNetworks() {
        return networkService
                .findByOwner(getCurrentUser().getTenant());
    }

    private List<NetworkGroup> loadNetworkGroups() {
        return networkGroupService
                .findByOwner(getCurrentUser().getTenant());
    }

    private void configureGroups() {
        groupsSelect.setNetworkSelection(networkSelect, defaultNetwork, getEntity().getNetwork());
        groupsSelect.selectDefaultGroup();
    }

    protected void bindFields() {
        Binder<Device> binder = getBinder();

        binder.forField(serial)
                .asRequired(getTranslation("validators.fieldgroup_errors"))
                .withValidator((value, context) -> {
                    if (value != null && !value.trim().isEmpty()) {
                        Device device = deviceService.findBySerial(value.trim());
                        if (device == null || !device.isAvailableForActivation()) {
                            return ValidationResult.error(getTranslation("validators.device_serial_not_found"));
                        }
                    }
                    return ValidationResult.ok();
                })
                .bind(Device::getSerial, Device::setSerial);

        binder.forField(activationKey)
                .asRequired(getTranslation("validators.fieldgroup_errors"))
                .withValidator((value, context) -> {
                    if (value != null && !value.trim().isEmpty()) {
                        String serialValue = serial.getValue();
                        if (serialValue != null && !serialValue.trim().isEmpty()) {
                            Device device = deviceService.findBySerial(serialValue.trim());
                            if (device == null || !device.isAvailableForActivation()
                                    || !value.equals(device.getActivationKey())) {
                                return ValidationResult.error(getTranslation("validators.device_activation_not_valid"));
                            }
                        }
                    }
                    return ValidationResult.ok();
                })
                .bind(Device::getActivationKey, Device::setActivationKey);
    }

    @Override
    public VerticalLayout getFieldsLayout() {
        VerticalLayout mainLayout = buildMainLayout();
        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.add(serial, activationKey, networkSelect, groupsSelect);
        mainLayout.add(buildPanel(formLayout));
        return mainLayout;
    }

    @Override
    protected void afterCommit() {
    }

    @Override
    protected void beforeCommit() throws EditorConstraintException {
        Set<NetworkGroup> selectedGroups = new HashSet<>(groupsSelect.getSelectedItems());
        getEntity().setGroups(selectedGroups);
    }

}
