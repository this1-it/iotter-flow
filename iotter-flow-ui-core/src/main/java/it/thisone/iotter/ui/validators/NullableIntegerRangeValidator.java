package it.thisone.iotter.ui.validators;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

public class NullableIntegerRangeValidator implements Validator<Integer> {

	private static final long serialVersionUID = 1L;
	
	private final String errorMessage;
	private final Integer minValue;
	private final Integer maxValue;

	public NullableIntegerRangeValidator(String errorMessage, Integer minValue, Integer maxValue) {
		this.errorMessage = errorMessage;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	
	@Override
	public ValidationResult apply(Integer value, ValueContext context) {
		if (value == null) {
			return ValidationResult.ok();
		}
		if (value.equals(0)) {
			return ValidationResult.ok();
		}
		
		if (minValue != null && value < minValue) {
			return ValidationResult.error(errorMessage);
		}
		if (maxValue != null && value > maxValue) {
			return ValidationResult.error(errorMessage);
		}
		
		return ValidationResult.ok();
	}
}