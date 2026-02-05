package it.thisone.iotter.ui.devices;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.annotations.PropertyId;
import com.vaadin.data.Binder;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.fields.NetworkGroupSelect;
import it.thisone.iotter.ui.common.fields.NetworkSelect;
import it.thisone.iotter.ui.validators.DeviceActivationKeyValidator;
import it.thisone.iotter.ui.validators.DeviceActivationSerialValidator;

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

    public DeviceActivatorForm(Network network) {
        super(new Device(), Device.class, "device.activator", network);
        this.defaultNetwork = network;
        initializeFields();
        bindFields();
        getBinder().readBean(getEntity());
        configureGroups();
    }

    private void initializeFields() {
        serial = new TextField(getI18nLabel(SERIAL));
        serial.setSizeFull();
        serial.setRequiredIndicatorVisible(true);

        activationKey = new TextField(getI18nLabel(ACTIVATION_KEY));
        activationKey.setSizeFull();
        activationKey.setRequiredIndicatorVisible(true);

        networkSelect = new NetworkSelect(loadNetworks());
        networkSelect.setSizeFull();
        networkSelect.setCaption(getI18nLabel("network"));

        List<NetworkGroup> availableGroups = loadNetworkGroups();
        groupsSelect = new NetworkGroupSelect(availableGroups, true);
        groupsSelect.setSizeFull();
        groupsSelect.setCaption(getI18nLabel(GROUPS));
        groupsSelect.setVisible(Constants.USE_GROUPS);
    }

    private List<Network> loadNetworks() {
        return UIUtils.getServiceFactory().getNetworkService()
                .findByOwner(UIUtils.getUserDetails().getTenant());
    }

    private List<NetworkGroup> loadNetworkGroups() {
        return UIUtils.getServiceFactory().getNetworkGroupService()
                .findByOwner(UIUtils.getUserDetails().getTenant());
    }

    private void configureGroups() {
        groupsSelect.setNetworkSelection(networkSelect, defaultNetwork, getEntity().getNetwork());
        groupsSelect.selectDefaultGroup();
    }

    private void bindFields() {
        Binder<Device> binder = getBinder();

        binder.forField(serial)
                .asRequired(UIUtils.localize("validators.fieldgroup_errors"))
                .withValidator(new DeviceActivationSerialValidator(ACTIVATION_KEY))
                .bind(Device::getSerial, Device::setSerial);

        binder.forField(activationKey)
                .asRequired(UIUtils.localize("validators.fieldgroup_errors"))
                .withValidator(new DeviceActivationKeyValidator(SERIAL))
                .bind(Device::getActivationKey, Device::setActivationKey);
    }

    @Override
    public Layout getFieldsLayout() {
        VerticalLayout mainLayout = buildMainLayout();
        FormLayout formLayout = new FormLayout();
        formLayout.setMargin(true);
        formLayout.setSpacing(true);
        formLayout.addComponents(serial, activationKey, networkSelect, groupsSelect);
        mainLayout.addComponent(buildPanel(formLayout));
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

    @Override
    public String getWindowStyle() {
        return "device-activator";
    }

    @Override
    public float[] getWindowDimension() {
        return UIUtils.M_DIMENSION;
    }
}
