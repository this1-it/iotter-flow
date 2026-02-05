package it.thisone.iotter.ui.devices;

import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.ui.common.AbstractBaseEntityDetails;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.UIUtils;

public class DeviceDetails extends AbstractBaseEntityDetails<Device> {

	private static final long serialVersionUID = 1L;

	public DeviceDetails(Device item, boolean remove) {
		super(item, Device.class, "device.editor",
				new String[] { "serial", "label", "model", "status", "network" }, remove);
	}

	@Override
	protected void onRemove() throws EditorConstraintException {
		Device entity = getBean();

		// Validate removal constraints
		if (entity.getNetwork() != null) {
			throw new EditorConstraintException(getI18nLabel("device.network.constraint"));
		}
		if (entity.getMaster() != null) {
			throw new EditorConstraintException(getI18nLabel("device.master.constraint"));
		}

		// Cleanup before removal
		UIUtils.getServiceFactory().getSubscriptionService().beforeDeviceRemoval(entity);
	}
}
