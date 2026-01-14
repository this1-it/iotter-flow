package it.thisone.iotter.ui.validators;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.ui.common.UIUtils;

public class UniqueNetworkGroupNameValidator implements Validator<String> {
	private static final long serialVersionUID = 1L;
	private String originalValue;
	private Network network;

	public UniqueNetworkGroupNameValidator(String value, Network _network) {
		originalValue = value;
		network = _network;
	}

	@Override
	public ValidationResult apply(String value, ValueContext context) {
		if(value == null || value.trim().isEmpty()) {
			return ValidationResult.error(UIUtils.localize("validators.unique_group_name"));
		}
		if(value.equals(originalValue)) {
			return ValidationResult.ok();
		}
		NetworkGroup group = null;
		try {
			group = UIUtils.getServiceFactory().getNetworkGroupService().findByName(value, network);
		} catch (BackendServiceException e) {
			return ValidationResult.error(UIUtils.localize("validators.unique_group_name"));
		}
		
		boolean isUnique = (group == null);
		if (!isUnique) {
			return ValidationResult.error(UIUtils.localize("validators.unique_group_name"));
		}
		return ValidationResult.ok();
	}
}