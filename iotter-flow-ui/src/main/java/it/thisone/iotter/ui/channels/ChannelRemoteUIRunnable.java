package it.thisone.iotter.ui.channels;

import java.math.BigDecimal;

import it.thisone.iotter.enums.modbus.Permission;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.ui.uitask.UIRunnable;

public class ChannelRemoteUIRunnable implements UIRunnable {

	private final Object chartAdapter;
	private final MeasureUnit unit;
	private final Permission permission;
	private final ChannelRemoteControlField remote;
	private String errorMessage;

	public ChannelRemoteUIRunnable(Object chartAdapter, ChannelRemoteControlField remote, MeasureUnit unit,
			Permission permission) {
		this.chartAdapter = chartAdapter;
		this.remote = remote;
		this.unit = unit;
		this.permission = permission;
	}

	@Override
	public void runInBackground() {
		remote.setEnabled(false);
		if (remote.getValue() == null || remote.getValue().getValue() == null) {
			errorMessage = "remote value is empty";
			return;
		}
		if (permission == Permission.READ) {
			errorMessage = remote.getTranslation("mqtt.setvalue.remote_control_not_issued");
			return;
		}
		BigDecimal ignoredRawValue = unit.calculateRaw(remote.getValue().getValue().floatValue());
		// TODO Flow migration: inject and use MqttService + UIEventBus here.
	}

	@Override
	public void runInUI(Throwable ex) {
		remote.setEnabled(true);
		if (ex != null) {
			errorMessage = ex.getMessage();
		}
		remote.setValidationError(errorMessage);
	}
}
