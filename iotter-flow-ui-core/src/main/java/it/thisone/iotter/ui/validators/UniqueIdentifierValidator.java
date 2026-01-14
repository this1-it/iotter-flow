package it.thisone.iotter.ui.validators;

import java.util.List;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

public class UniqueIdentifierValidator<T extends Comparable<?>> implements Validator<T> {
	private static final long serialVersionUID = 1L;
	private List<T> others;
	private Class<T> type;
	private String errorMessage;
    
	public UniqueIdentifierValidator(String errorMessage, Class<T> type, List<T> others) {
		this.errorMessage = errorMessage;
		this.others = others;
	    this.type = type;
	}

	@Override
	public ValidationResult apply(T value, ValueContext context) {
		if (value == null || (String.class.equals(type) && "".equals(value))) {
			return ValidationResult.error(errorMessage);
		}
		
		if (others.isEmpty()) {
			return ValidationResult.ok();
		}
		
		boolean isUnique = !others.contains(value);
		if (!isUnique) {
			return ValidationResult.error(errorMessage);
		}
		
		return ValidationResult.ok();
	}
}