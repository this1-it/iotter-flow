package it.thisone.iotter.ui.validators;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

import it.thisone.iotter.ui.common.UIUtils;

/*
 * 	//Bug #333 [PERSISTENCE] a runtime error occurs inserting a new device (duplicate activation key)

 */
public class UniqueDeviceActivationKeyValidator implements Validator<String> {
	private static final long serialVersionUID = 1L;

	@Override
	public ValidationResult apply(String value, ValueContext context) {
//		if(value != null && !value.isEmpty()) {
//			boolean isUnique = (UIUtils.getServiceFactory().getDeviceService().findByActivationKey(value) == null);
//			if (!isUnique) {
//				return ValidationResult.error(getTranslation("validators.activation_key_unique"));
//			}
//		} else {
//			return ValidationResult.error(getTranslation("validators.activation_key_unique"));
//		}
		return ValidationResult.ok();
	}
}