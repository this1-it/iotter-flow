package it.thisone.iotter.ui.validators;



import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

public final class PasswordVerificationValidator implements Validator<String> {
    /** The default serial version UID. */
    private static final long serialVersionUID = 1L;
    /**
     * The original password property used to get the value of the original
     * password.
     */
    private final String originalPasswordPropertyId;
    


    /**
     * Constructor for setting the required parameters for the validator.
     * @param site the site.
     * @param originalPasswordProperty the original password property used to
     *            get the value of the original password.
     */
    public PasswordVerificationValidator(String originalPasswordPropertyId) {
        this.originalPasswordPropertyId = originalPasswordPropertyId;

    }


	@Override
	public ValidationResult apply(String value, ValueContext context) {

		return ValidationResult.ok();
	}
}
