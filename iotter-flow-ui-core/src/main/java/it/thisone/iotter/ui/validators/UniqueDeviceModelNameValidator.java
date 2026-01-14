package it.thisone.iotter.ui.validators;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

import it.thisone.iotter.ui.common.UIUtils;

public class UniqueDeviceModelNameValidator implements Validator<String> {
	private static final long serialVersionUID = 1L;
	private String originalValue;

	public UniqueDeviceModelNameValidator(String originalValue) {
		this.originalValue = originalValue;
	}

	@Override
	public ValidationResult apply(String value, ValueContext context) {
		if (value == null || value.trim().isEmpty()) {
			return ValidationResult.error(UIUtils.localize("validators.unique_device_model_name"));
		}
		
		if (value.trim().equals(originalValue)) {
			return ValidationResult.ok();
		}
		
		boolean isUnique = (UIUtils.getServiceFactory().getDeviceService().getDeviceModel(value) == null);
		if (!isUnique) {
			return ValidationResult.error(UIUtils.localize("validators.unique_device_model_name"));
		}
		
		return ValidationResult.ok();
	}
}