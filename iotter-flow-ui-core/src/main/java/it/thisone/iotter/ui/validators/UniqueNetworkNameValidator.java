package it.thisone.iotter.ui.validators;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.ui.common.UIUtils;

public class UniqueNetworkNameValidator implements Validator<String> {
	private static final long serialVersionUID = 1L;
	private String originalValue;
	
	public UniqueNetworkNameValidator(String originalValue) {
		this.originalValue = originalValue;
	}

	@Override
	public ValidationResult apply(String value, ValueContext context) {
//		if (value == null || value.trim().isEmpty()) {
//			return ValidationResult.error(getTranslation("validators.unique_network_name"));
//		}
//		
//		if (value.equals(originalValue)) {
//			return ValidationResult.ok();
//		}
//		
//		String owner = getCurrentUser().getTenant();
//
//		Network network = null;
//		try {
//			network = UIUtils.getServiceFactory().getNetworkService().findByName(value.trim(), owner);
//		} catch (BackendServiceException e) {
//			return ValidationResult.error(getTranslation("validators.unique_network_name"));
//		}
//		
//		boolean isUnique = (network == null);
//		if (!isUnique) {
//			return ValidationResult.error(getTranslation("validators.unique_network_name"));
//		}
//		
		return ValidationResult.ok();
	}
}