package it.thisone.iotter.ui.devices;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

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
			mainLayout.addStyleName("border-top");
			return mainLayout;
		}

		device = bean;

		if (Protocol.NATIVE.equals(device.getProtocol())) {
			List<Device> slaves = UIUtils.getServiceFactory().getDeviceService().findSlaves(device);
			Grid<Device> deviceGrid = new Grid<>();
			deviceGrid.setItems(slaves);
			deviceGrid.setSizeFull();
			deviceGrid.setSelectionMode(Grid.SelectionMode.NONE);
			deviceGrid.addColumn(Device::getLabel).setCaption(getI18nLabel("label"));
			deviceGrid.addColumn(Device::getDescription).setCaption(getI18nLabel("description"));
			deviceGrid.addColumn(this::formatStatus).setCaption(getI18nLabel("status"));

			Button provisioning = new Button();
			provisioning.setStyleName(ValoTheme.BUTTON_PRIMARY);
			provisioning.setCaption(getI18nLabel("provisioning_button"));
			provisioning.setIcon(VaadinIcons.TAGS);
			provisioning.addClickListener(event -> fireEvent(
					new EditorSavedEvent(DeviceWidgetBox.this, device)));
			provisioning.setEnabled(!device.isDeActivated());

			mainLayout.setDefaultComponentAlignment(Alignment.TOP_CENTER);
			mainLayout.setSpacing(true);
			mainLayout.addComponent(deviceGrid);
			mainLayout.setExpandRatio(deviceGrid, 1f);
			mainLayout.addComponent(provisioning);
		}

		if (device.isAvailableForVisualization()) {
			Collection<GroupWidget> widgets = UIUtils.getServiceFactory().getGroupWidgetService().findByDevice(device);
			IDeviceInfo info = UIUtils.getUiFactory().getDeviceUiFactory().getDeviceInfo(device, widgets);
			info.addListener(new ItemSelectedListener() {
				@Override
				@SuppressWarnings("unchecked")
				public void itemSelected(ItemSelectedEvent event) {
					if (event.getSelected() != null && event.getSelected() instanceof GroupWidget) {
						GroupWidget bean =  (GroupWidget)event.getSelected();
						fireEvent(new EditorSavedEvent(DeviceWidgetBox.this, bean));
					}
				}
			});
			mainLayout.addComponent(info);
		}

		if (mainLayout.getComponentCount() == 0) {
			mainLayout.addStyleName("border-top");
		}

		return mainLayout;
	}

	private String formatStatus(Device device) {
		DeviceStatus status = device.getStatus();
		if (status == null) {
			return "";
		}
		return UIUtils.localize(status.getI18nKey(), status.name().toLowerCase());
	}

	public void refresh(Device bean) {
		setCompositionRoot(buildLayout(bean));
	}

	public void addListener(EditorSavedListener listener) {
		try {
			Method method = EditorSavedListener.class.getDeclaredMethod(
					EditorSavedListener.EDITOR_SAVED, new Class[] { EditorSavedEvent.class });
			addListener(EditorSavedEvent.class, listener, method);
		} catch (final java.lang.NoSuchMethodException e) {
			throw new RuntimeException("Internal error, editor saved method not found");
		}
	}

	public void removeListener(EditorSavedListener listener) {
		removeListener(EditorSavedEvent.class, listener);
	}

	public String getWindowStyle() {
		return "device-editor";
	}

	public float[] getWindowDimension() {
		return UIUtils.L_DIMENSION;
	}
}
