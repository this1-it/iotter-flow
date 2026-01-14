package it.thisone.iotter.ui.ifc;

import java.util.Collection;

import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GroupWidget;

public interface IDeviceUiFactory {
	IDeviceInfo getDeviceInfo(Device device, Collection<GroupWidget> widgets);

	IProvisioningWizard getProvisioningWizard(String serial);
	

}