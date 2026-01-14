
package it.thisone.iotter.ui.validators;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

public final class PasswordValidator implements Validator<String> {
    /** The default serial version UID. */
    private static final long serialVersionUID = 1L;


    /**
     * The password verification property ID used to trigger revalidation of
     * that property when original password property changes.
     */
    private final String passwordVerificationPropertyId;

    
    private boolean allowedEmptyPassword;
    
    /**
     * Constructor for setting the required parameters for validator.
     * @param site the site
     * @param originalPasswordProperty The original password property used to
     *            set value on each change so it is available for the other
     *            validator.
     * @param passwordVerificationPropertyId The password verification property
     *            ID used to trigger revalidation of that property when original
     *            password property changes.
     */
    public PasswordValidator(final String passwordVerificationPropertyId,  boolean allowedEmptyPassword) {
        this.passwordVerificationPropertyId = passwordVerificationPropertyId;
        this.allowedEmptyPassword = allowedEmptyPassword;
    }



	@Override
	public ValidationResult apply(String value, ValueContext context) {
//        String password = (String) value;
//        editor.validateField(passwordVerificationPropertyId);
//        if (password.isEmpty() && allowedEmptyPassword) {
//            return null;
//        }
//        if (password.length() < Constants.Validators.MIN_PASSWORD_LENGTH) {
//            return ValidationResult.error(UIUtils.localize("validators.too-short-password"));
//        }
//        if (editor.equalsField("username", password)) {
//            return ValidationResult.error(UIUtils.localize("validators.username-equals-password"));        	
//        }
		return ValidationResult.ok();
	}

}
