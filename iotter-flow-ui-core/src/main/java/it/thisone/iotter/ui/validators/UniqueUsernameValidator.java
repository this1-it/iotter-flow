package it.thisone.iotter.ui.validators;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

import it.thisone.iotter.persistence.service.UserService;

public class UniqueUsernameValidator implements Validator<String> {
	private static final long serialVersionUID = 1L;
	private final String errorMessage;
	private UserService userService;

	public UniqueUsernameValidator(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Override
	public ValidationResult apply(String value, ValueContext context) {
		if (value == null || value.trim().isEmpty()) {
			return ValidationResult.error(errorMessage);
		}

		boolean isUnique = (userService.findByName(value.trim()) == null);
		if (!isUnique) {
			return ValidationResult.error(errorMessage);
		}

		return ValidationResult.ok();
	}
}
