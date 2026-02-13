package it.thisone.iotter.ui.signup;

import org.vaadin.firitin.form.AbstractForm;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;

import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.main.UiConstants;

public class CredentialInputForm extends AbstractForm<CredentialInput> {
    private static final float SMALL_WIDTH = 16f;    
    
    private TextField username;
    private TextField email;
    private TextField serialNumber;
    private TextField activationKey;
    private PasswordField password;
    private PasswordField passwordConfirmation;
    
    public CredentialInputForm() {
        super(CredentialInput.class);

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
    }
    
    private void bindFields() {
        getBinder().forField(username)
            .asRequired(getI18nLabel("username_required"))
            .bind(CredentialInput::getUsername, CredentialInput::setUsername);

        getBinder().forField(email)
            .asRequired(getI18nLabel("email_required"))
            .bind(CredentialInput::getEmail, CredentialInput::setEmail);

        getBinder().forField(serialNumber)
            .bind(CredentialInput::getSerialNumber, CredentialInput::setSerialNumber);

        getBinder().forField(activationKey)
            .bind(CredentialInput::getActivationKey, CredentialInput::setActivationKey);

        getBinder().forField(password)
            .asRequired(getI18nLabel("password_required"))
            .bind(CredentialInput::getPassword, CredentialInput::setPassword);

        getBinder().forField(passwordConfirmation)
            .asRequired(getI18nLabel("passwordConfirmation_required"))
            .bind(CredentialInput::getPasswordConfirmation, CredentialInput::setPasswordConfirmation);
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

    
    @Override
    protected Component createContent() {
        HorizontalLayout toolbar = getToolbar();
        VerticalLayout content = new VerticalLayout(getFormLayout(), toolbar);
        content.setPadding(true);
        content.setSpacing(true);
        content.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, toolbar);
        return content;
    }
    
    protected Component getFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new ResponsiveStep("0", 1),
            new ResponsiveStep("40em", 2)
        );
        formLayout.setWidth("600px");
        formLayout.getStyle().set("padding", "var(--lumo-space-m)");
        
        formLayout.add(username, email);
        formLayout.add(password, passwordConfirmation);
        formLayout.add(serialNumber, activationKey);

        return formLayout;
    }


    protected String getI18nLabel(String key) {
		return getTranslation(UiConstants.IDENTITY_PROFILE + "." + key);
	}


}
