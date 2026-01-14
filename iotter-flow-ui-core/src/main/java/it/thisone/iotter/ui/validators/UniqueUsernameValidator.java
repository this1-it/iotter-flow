package it.thisone.iotter.ui.validators;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

import it.thisone.iotter.ui.common.UIUtils;

public class UniqueUsernameValidator implements Validator<String> {
	private static final long serialVersionUID = 1L;
	private final String errorMessage;

	public UniqueUsernameValidator(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public ValidationResult apply(String value, ValueContext context) {
		if (value == null || value.trim().isEmpty()) {
			return ValidationResult.error(errorMessage);
		}
		
		boolean isUnique = (UIUtils.getServiceFactory().getUserService().findByName(value.trim()) == null);
		if (!isUnique) {
			return ValidationResult.error(errorMessage);
		}
		
		return ValidationResult.ok();
	}
}