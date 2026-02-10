package it.thisone.iotter.ui.networkgroups;

import java.util.Collections;
import java.util.List;

import org.vaadin.flow.components.TabSheet;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;

import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.NetworkGroupService;
import it.thisone.iotter.ui.common.BaseEditor;
import it.thisone.iotter.ui.ifc.INetworkGroupUsers;
import it.thisone.iotter.ui.networkgroups.NetworkGroupDevices.DeviceMemberListener;

public class NetworkGroupBindings extends BaseEditor<GroupWidget> {

    private static final long serialVersionUID = 1L;

    private final NetworkGroup entity;
    private final NetworkGroupDevices devices;
    private final INetworkGroupUsers users;

    private final TabSheet content;
    private final Tab devicesTab;
    private final Button saveButton;

    public NetworkGroupBindings(GroupWidget bean, NetworkGroupService networkGroupService, DeviceService deviceService) {
        super("networkgroup.bindings");
        setItem(bean);

        if (bean.getGroup() == null) {
            throw new IllegalArgumentException("GroupWidget has no associated NetworkGroup");
        }

        if (bean.getGroup().isNew()) {
            this.entity = bean.getGroup();
        } else {
            NetworkGroup managed = networkGroupService.findOne(bean.getGroup().getId());
            this.entity = managed != null ? managed : bean.getGroup();
        }

        this.devices = new NetworkGroupDevices(entity, deviceService);
        this.users = new NetworkGroupUsersPanel();
        this.content = new TabSheet();
        content.setSizeFull();
        content.addClassName("tabsheet-framed");
        this.devicesTab = content.addTab(getI18nLabel("devices_tab"), devices);
        content.addTab(getI18nLabel("users_tab"), (Component) users);

        devices.setAlreadyConfiguredDevices(bean.getAssociatedDevices(), getI18nLabel("device_already_configured"));

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);

        mainLayout.add(content);
        mainLayout.setFlexGrow(1f, content);

        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setSpacing(true);
        footer.setPadding(true);
        footer.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        footer.getStyle().set("justify-content", "center");

        saveButton = createSaveButton();
        saveButton.setText(getI18nLabel("save_button"));
        saveButton.setIcon(null);

        Button cancelButton = createCancelButton();
        cancelButton.setText(getI18nLabel("cancel_button"));
        cancelButton.setIcon(null);

        footer.add(saveButton, cancelButton);
        mainLayout.add(footer);

        if (bean.isExclusive()) {
            content.remove(devicesTab);
            saveButton.setVisible(false);
        }

        setRootComposition(mainLayout);
    }

    @Override
    protected void onSave() {
    }

    @Override
    protected void onCancel() {
    }

    public String getWindowStyle() {
        return "networkgroup-members-editor";
    }

    public float[] getWindowDimension() {
        return XL_DIMENSION;
    }

    public List<Device> getDevices() {
        return devices.getDevices();
    }

    public List<User> getUsers() {
        return users.getUsers();
    }

    public Component getTabContent() {
        return content;
    }

    public Network getNetwork() {
        return entity.getNetwork();
    }

    public void addMembersListener(DeviceMemberListener listener) {
        devices.addMembersListener(listener);
    }

    public void removeMembersListener(DeviceMemberListener listener) {
        devices.removeMembersListener(listener);
    }

    private static final class NetworkGroupUsersPanel extends VerticalLayout implements INetworkGroupUsers {
        private static final long serialVersionUID = 1L;

        private NetworkGroupUsersPanel() {
            setSizeFull();
            setPadding(true);
            add(new Span("Users panel migration pending"));
        }

        @Override
        public List<User> getUsers() {
            return Collections.emptyList();
        }
    }
}
