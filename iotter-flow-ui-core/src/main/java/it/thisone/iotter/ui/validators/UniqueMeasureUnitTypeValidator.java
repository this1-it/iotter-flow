package it.thisone.iotter.ui.validators;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

import it.thisone.iotter.ui.common.UIUtils;

public class UniqueMeasureUnitTypeValidator implements Validator<Integer> {
	private static final long serialVersionUID = 1L;
	private Integer originalValue;

	public UniqueMeasureUnitTypeValidator(Integer value) {
		originalValue = value;
	}

	@Override
	public ValidationResult apply(Integer value, ValueContext context) {
		if(value == null) {
			return ValidationResult.error(UIUtils.localize("validators.unique_measure_unit_type"));
		}
		if(value.equals(originalValue)) {
			return ValidationResult.ok();
		}
		boolean isUnique = (UIUtils.getServiceFactory().getDeviceService().getUnitOfMeasure(value) == null);
		if (!isUnique) {
			return ValidationResult.error(UIUtils.localize("validators.unique_measure_unit_type"));
		}
		return ValidationResult.ok();
	}
}