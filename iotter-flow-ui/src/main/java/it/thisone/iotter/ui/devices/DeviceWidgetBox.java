package it.thisone.iotter.ui.devices;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;


import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.enums.Protocol;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.common.EditorSavedEvent;
import it.thisone.iotter.ui.common.EditorSavedListener;
import it.thisone.iotter.ui.common.ItemSelectedEvent;
import it.thisone.iotter.ui.common.ItemSelectedListener;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.ifc.IDeviceInfo;

public class DeviceWidgetBox extends BaseComponent {
	// TODO(flow-migration): manual refactor required for Vaadin 8 APIs removed in Flow (dialogs/tabs/legacy layout or UIUtils context access).


	private static final long serialVersionUID = 1L;
	private static final String NAME = "device.widget.box";

	private Device device;

	public DeviceWidgetBox() {
		super(NAME, UUID.randomUUID().toString());
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
			List<Device> slaves = UIUtils.getServiceFactory().getDeviceService().findSlaves(device);
			Grid<Device> deviceGrid = new Grid<>();
			deviceGrid.setItems(slaves);
			deviceGrid.setSizeFull();
			deviceGrid.setSelectionMode(Grid.SelectionMode.NONE);
			deviceGrid.addColumn(Device::getLabel).setHeader(getI18nLabel("label"));
			deviceGrid.addColumn(Device::getDescription).setHeader(getI18nLabel("description"));
			deviceGrid.addColumn(this::formatStatus).setHeader(getI18nLabel("status"));

			Button provisioning = new Button();
			// provisioning.setClassName(ValoTheme.BUTTON_PRIMARY);
			// provisioning.setCaption(getI18nLabel("provisioning_button"));
			// provisioning.setIcon(VaadinIcon.TAGS);
			provisioning.addClickListener(event -> fireEvent(
					new EditorSavedEvent(DeviceWidgetBox.this, device)));
			provisioning.setEnabled(!device.isDeActivated());

			//mainLayout.setDefaultComponentAlignment(Alignment.TOP_CENTER);
			mainLayout.setSpacing(true);
			mainLayout.add(deviceGrid);
			//mainLayout.setExpandRatio(deviceGrid, 1f);
			mainLayout.add(provisioning);
		}

		if (device.isAvailableForVisualization()) {
			// Collection<GroupWidget> widgets = UIUtils.getServiceFactory().getGroupWidgetService().findByDevice(device);
			// IDeviceInfo info = UIUtils.getUiFactory().getDeviceUiFactory().getDeviceInfo(device, widgets);
			// info.addListener(new ItemSelectedListener() {
			// 	@Override
			// 	@SuppressWarnings("unchecked")
			// 	public void itemSelected(ItemSelectedEvent event) {
			// 		if (event.getSelected() != null && event.getSelected() instanceof GroupWidget) {
			// 			GroupWidget bean =  (GroupWidget)event.getSelected();
			// 			fireEvent(new EditorSavedEvent(DeviceWidgetBox.this, bean));
			// 		}
			// 	}
			// });
			// mainLayout.add(info);
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
		setRootComposition(buildLayout(bean));
	}

	public void addListener(EditorSavedListener listener) {
		// try {
		// 	Method method = EditorSavedListener.class.getDeclaredMethod(
		// 			EditorSavedListener.EDITOR_SAVED, new Class[] { EditorSavedEvent.class });
		// 	addListener(EditorSavedEvent.class, listener, method);
		// } catch (final java.lang.NoSuchMethodException e) {
		// 	throw new RuntimeException("Internal error, editor saved method not found");
		// }
	}

	public void removeListener(EditorSavedListener listener) {
		// removeListener(EditorSavedEvent.class, listener);
	}


}
