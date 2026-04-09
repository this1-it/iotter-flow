package it.thisone.iotter.ui.devices;

import java.util.List;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;

import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.enums.Protocol;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.common.EditorSavedEvent;
import it.thisone.iotter.ui.common.GroupWidgetSelectedEvent;
import it.thisone.iotter.ui.providers.BackendServices;


public class DeviceWidgetBox extends Composite<Div> {
	// TODO(flow-migration): manual refactor required for Vaadin 8 APIs removed in Flow (dialogs/tabs/legacy layout or UIUtils context access).


	private static final long serialVersionUID = 1L;
	private static final String NAME = "device.widget.box";
    private final UserDetailsAdapter currentUser;
    private final BackendServices backendServices;



	private Device device;

	public DeviceWidgetBox(UserDetailsAdapter currentUser, BackendServices backendServices) {
		super();
		this.currentUser = currentUser;
		this.backendServices = backendServices;
	}

	private VerticalLayout buildLayout(Device bean) {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();

		if (bean == null) {
			mainLayout.addClassName("border-top");
			return mainLayout;
		}

		device = bean;

		if (Protocol.NATIVE.equals(device.getProtocol())) {
			List<Device> slaves = backendServices.getDeviceService().findSlaves(device);
			Grid<Device> deviceGrid = new Grid<>();
			deviceGrid.setItems(slaves);
			deviceGrid.setSizeFull();
			deviceGrid.setSelectionMode(Grid.SelectionMode.NONE);
			deviceGrid.addColumn(Device::getLabel).setHeader(getTranslation("device.view.label"));
			deviceGrid.addColumn(Device::getDescription).setHeader(getTranslation("device.view.description"));
			deviceGrid.addColumn(this::formatStatus).setHeader(getTranslation("device.view.status"));

			Button provisioning = new Button(getI18nLabel("provisioning_button"));

			provisioning.setIcon(VaadinIcon.TAGS.create());
			provisioning.addClickListener(event -> fireEvent(
					new EditorSavedEvent(DeviceWidgetBox.this, device)));
			provisioning.setEnabled(!device.isDeActivated());

			//mainLayout.setDefaultComponentAlignment(Alignment.TOP_CENTER);
			mainLayout.setSpacing(true);
			mainLayout.add(deviceGrid);
			mainLayout.add(provisioning);
		}

		if (device.isAvailableForVisualization()) {
	
			DeviceInfo info = new DeviceInfo(device, currentUser, backendServices);
			info.addGroupWidgetSelectedListener(event ->
					fireEvent(new GroupWidgetSelectedEvent(DeviceWidgetBox.this, event.getGroupWidget())));
			info.buildContent();
			mainLayout.add(info);
		}

		if (mainLayout.getComponentCount() == 0) {
			mainLayout.addClassName("border-top");
		}

		return mainLayout;
	}

	private String formatStatus(Device device) {
		DeviceStatus status = device.getStatus();
		if (status == null) {
			return "";
		}
		return getTranslation(status.getI18nKey(), status.name().toLowerCase());
	}

	public void refresh(Device bean) {
		getContent().removeAll();
		getContent().add(buildLayout(bean));
	}

	public Registration addGroupWidgetSelectedListener(ComponentEventListener<GroupWidgetSelectedEvent> listener) {
		return addListener(GroupWidgetSelectedEvent.class, listener);
	}

    public String getI18nLabel(String key) {
        return getTranslation(NAME + "." + key);
    }
}
