package it.thisone.iotter.ui.validators;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

import it.thisone.iotter.ui.common.UIUtils;

public class UniqueDeviceSerialValidator implements Validator<String> {
	private static final long serialVersionUID = 1L;

	@Override
	public ValidationResult apply(String value, ValueContext context) {
		if (value != null && !value.trim().isEmpty()) {
			boolean isUnique = (UIUtils.getServiceFactory().getDeviceService().findBySerial(value.trim()) == null);
			if (!isUnique) {
				return ValidationResult.error(UIUtils.localize("validators.serial_unique"));
			}
		} else {
			return ValidationResult.error(UIUtils.localize("validators.serial_unique"));
		}
		return ValidationResult.ok();
	}
}