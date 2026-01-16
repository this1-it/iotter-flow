package it.thisone.iotter.ui.common.fields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.combobox.ComboBox;

import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.ui.common.UIUtils;

public class DeviceSerialSelect extends ComboBox<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Map<String, String> deviceLabels = new HashMap<>();

	public DeviceSerialSelect() {
		super();
		initialize();
	}

	private void initialize() {
//		setEmptySelectionAllowed(false);
//		setTextInputAllowed(false);
		setItemLabelGenerator(serial -> deviceLabels.getOrDefault(serial, serial));
	}


	public void setGraph(GraphicWidget widget) {
        List<String> serials = new ArrayList<>();
		deviceLabels.clear();
		
		if (widget.getGroupWidget() != null && widget.getGroupWidget().getGroup() != null) {
			List<Device> devices = availableDevices(widget.getGroupWidget());
			for (Device device : devices) {
				serials.add(device.getSerial());
				deviceLabels.put(device.getSerial(), device.getLabel());
			}
		}
		setItems(serials);
		setValue(widget.getDevice());
	}
	
	
	
	private List<Device> availableDevices(GroupWidget groupwidget) {
		List<Device> available = new ArrayList<Device> ();
		if (groupwidget.getGroup() != null) {
			List<Device> devices = UIUtils.getServiceFactory().getDeviceService().findByGroup(groupwidget.getGroup());
			for (Device device : devices) {
				if (!device.getChannels().isEmpty()) {
					available.add(device);
				}
			}
		}
		else if (groupwidget.getDevice() != null) {
			Device device = UIUtils.getServiceFactory().getDeviceService().findBySerial(groupwidget.getDevice());
			if (device != null) {
				available.add(device);
			}
		}		
		return available;
	}

}
