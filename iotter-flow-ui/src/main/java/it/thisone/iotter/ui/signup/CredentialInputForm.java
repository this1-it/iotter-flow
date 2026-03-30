package it.thisone.iotter.ui.signup;

import org.vaadin.firitin.form.AbstractForm;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.UserService;
import it.thisone.iotter.ui.validators.AntiReDoSEmailValidator;

public class CredentialInputForm extends AbstractForm<CredentialInput> {
    private static final float SMALL_WIDTH = 16f;    

    private final UserService userService;
    private final DeviceService deviceService;

    private TextField username;
    private TextField email;
    private TextField serialNumber;
    private TextField activationKey;
    private PasswordField password;
    private PasswordField passwordConfirmation;

    public CredentialInputForm(UserService userService, DeviceService deviceService) {
        super(CredentialInput.class);
        this.userService = userService;
        this.deviceService = deviceService;

        username = new TextField(getI18nLabel("username"));
        username.setRequiredIndicatorVisible(true);
        username.setWidth(SMALL_WIDTH + "em");

        email = new TextField(getI18nLabel("email"));
        email.setRequiredIndicatorVisible(true);
        email.setWidth(SMALL_WIDTH + "em");

        serialNumber = new TextField(getI18nLabel("serialNumber"));
        serialNumber.setWidth(SMALL_WIDTH + "em");

        activationKey = new TextField(getI18nLabel("activationKey"));
        activationKey.setWidth(SMALL_WIDTH + "em");

        password = new PasswordField(getI18nLabel("password"));
        password.setRequiredIndicatorVisible(true);
        password.setWidth(SMALL_WIDTH + "em");

        passwordConfirmation = new PasswordField(getI18nLabel("passwordConfirmation"));
        passwordConfirmation.setRequiredIndicatorVisible(true);
        passwordConfirmation.setWidth(SMALL_WIDTH + "em");

        bindFields();
        addPasswordMatchCheck();
        setEntity(new CredentialInput());
    }
    
    private void bindFields() {
        getBinder().forField(username)
            .asRequired(getTranslation("validators.fieldgroup_errors"))
            .withValidator(value -> userService.findByName(value == null ? null : value.trim()) == null,
                    getI18nLabel("username_unique"))
            .withValidator(value -> value != null && value.trim().length() >= Constants.Validators.MIN_USERNAME_LENGTH,
                    getI18nLabel("username_too_short"))
            .bind(CredentialInput::getUsername, CredentialInput::setUsername);

        getBinder().forField(email)
            .asRequired(getTranslation("validators.fieldgroup_errors"))
            .withValidator((value, context) -> {
                if (value == null || value.trim().isEmpty()) {
                    return ValidationResult.ok();
                }
                return new AntiReDoSEmailValidator(getI18nLabel("valid_email")).apply(value, context);
            })
            .bind(CredentialInput::getEmail, CredentialInput::setEmail);

        getBinder().forField(serialNumber)
            .asRequired(getTranslation("validators.fieldgroup_errors"))
            .withValidator((value, context) -> {
                if (value != null && !value.trim().isEmpty()) {
                    Device device = deviceService.findBySerial(value.trim());
                    if (device == null || !device.isAvailableForActivation()) {
                        return ValidationResult.error(getTranslation("validators.device_serial_not_found"));
                    }
                }
                return ValidationResult.ok();
            })
            .bind(CredentialInput::getSerialNumber, CredentialInput::setSerialNumber);

        getBinder().forField(activationKey)
            .asRequired(getTranslation("validators.fieldgroup_errors"))
            .withValidator((value, context) -> {
                if (value != null && !value.trim().isEmpty()) {
                    String serialValue = serialNumber.getValue();
                    if (serialValue != null && !serialValue.trim().isEmpty()) {
                        Device device = deviceService.findBySerial(serialValue.trim());
                        if (device == null || !device.isAvailableForActivation()
                                || !value.equals(device.getActivationKey())) {
                            return ValidationResult.error(getTranslation("validators.device_activation_not_valid"));
                        }
                    }
                }
                return ValidationResult.ok();
            })
            .bind(CredentialInput::getActivationKey, CredentialInput::setActivationKey);

        getBinder().forField(password)
            .asRequired(getTranslation("validators.fieldgroup_errors"))
            .withValidator(this::validatePassword)
            .bind(CredentialInput::getPassword, CredentialInput::setPassword);

        getBinder().forField(passwordConfirmation)
            .asRequired(getTranslation("validators.fieldgroup_errors"))
            .withValidator(this::validatePasswordMatch)
            .bind(CredentialInput::getPasswordConfirmation, CredentialInput::setPasswordConfirmation);

        password.addValueChangeListener(event -> getBinder().validate());
        passwordConfirmation.addValueChangeListener(event -> getBinder().validate());
        serialNumber.addValueChangeListener(event -> getBinder().validate());
    }

    private void addPasswordMatchCheck() {
        password.addValueChangeListener(event -> updatePasswordMatchState());
        passwordConfirmation.addValueChangeListener(event -> updatePasswordMatchState());
    }

    private void updatePasswordMatchState() {
        String pwd = password.getValue();
        String confirm = passwordConfirmation.getValue();
        boolean mismatch = pwd != null && confirm != null && !pwd.equals(confirm);
        passwordConfirmation.setInvalid(mismatch);
        passwordConfirmation.setErrorMessage(mismatch ? getI18nLabel("password_mismatch") : null);
    }

    private ValidationResult validatePassword(String value, ValueContext context) {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.ok();
        }
        if (value.length() < Constants.Validators.MIN_PASSWORD_LENGTH) {
            return ValidationResult.error(getTranslation("validators.too-short-password"));
        }
        String usernameValue = username.getValue();
        if (usernameValue != null && usernameValue.equals(value)) {
            return ValidationResult.error(getTranslation("validators.username-equals-password"));
        }
        return ValidationResult.ok();
    }

    private ValidationResult validatePasswordMatch(String value, ValueContext context) {
        String original = password.getValue();
        if (value == null || value.trim().isEmpty() || original == null || original.trim().isEmpty()) {
            return ValidationResult.ok();
        }
        if (!value.equals(original)) {
            return ValidationResult.error(getTranslation("validators.message-passwords-do-not-match"));
        }
        return ValidationResult.ok();
    }

    public boolean validateForm() {
        return getBinder().writeBeanIfValid(getEntity());
    }

    public CredentialInput getValue() {
        return getEntity();
    }
    
    @Override
    protected Component createContent() {
        getToolbar().setVisible(false);
        getSaveButton().setVisible(false);
        getResetButton().setVisible(false);
        getDeleteButton().setVisible(false);
        return getFormLayout();
    }
    
    protected Component getFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new ResponsiveStep("0", 1),
            new ResponsiveStep("500px", 2)
        );
        formLayout.setWidthFull();
        formLayout.getStyle().set("box-sizing", "border-box");
        formLayout.getStyle().set("padding", "var(--lumo-space-m)");
        
        formLayout.add(username, email);
        formLayout.add(password, passwordConfirmation);
        formLayout.add(serialNumber, activationKey);

        return formLayout;
    }


    protected String getI18nLabel(String key) {
        return getTranslation("register." + key);
	}


}
