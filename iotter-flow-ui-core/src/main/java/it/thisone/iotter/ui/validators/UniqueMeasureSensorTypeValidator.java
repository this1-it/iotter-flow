package it.thisone.iotter.ui.validators;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

import it.thisone.iotter.ui.common.UIUtils;

public class UniqueMeasureSensorTypeValidator implements Validator<Integer> {
	private static final long serialVersionUID = 1L;
	private Integer originalValue;

	public UniqueMeasureSensorTypeValidator(Integer value) {
		originalValue = value;
	}

	@Override
	public ValidationResult apply(Integer value, ValueContext context) {
//		if(value == null) {
//			return ValidationResult.error(getTranslation("validators.unique_measure_sensor_type"));
//		}
//		if(value.equals(originalValue)) {
//			return ValidationResult.ok();
//		}
//		boolean isUnique = (UIUtils.getServiceFactory().getDeviceService().getSensor(value) == null);
//		if (!isUnique) {
//			return ValidationResult.error(getTranslation("validators.unique_measure_sensor_type"));
//		}
		return ValidationResult.ok();
	}
}