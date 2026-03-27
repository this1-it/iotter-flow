package it.thisone.iotter.ui.signup;

import it.thisone.iotter.ui.wizards.WizardStep;

import com.vaadin.flow.component.Component;

import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.UserService;

public class CredentialStep implements WizardStep {

    private final ISignUpWizard signUpWizard;
    private final CredentialInputForm form;

    public CredentialStep(ISignUpWizard signUpWizard, UserService userService, DeviceService deviceService) {
        this.signUpWizard = signUpWizard;
        this.form = new CredentialInputForm(userService, deviceService);
    }

	@Override
	public String getCaption() {
		return signUpWizard.getI18nLabel("login_info");
	}

	@Override
	public Component getContent() {
		return this.form;
	}

	@Override
	public boolean onAdvance() {
		return form.validateForm();
	}

	@Override
	public boolean onBack() {
		return true;
	}

    public CredentialInput getValue() {
        return form.getValue();
    }
}
