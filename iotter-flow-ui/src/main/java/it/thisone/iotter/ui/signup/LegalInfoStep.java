package it.thisone.iotter.ui.signup;

import it.thisone.iotter.ui.wizards.WizardStep;

import com.vaadin.flow.component.Component;

public class LegalInfoStep implements WizardStep {

    private final ISignUpWizard signUpWizard;
    private final LegalInfoForm form;

	public LegalInfoStep(ISignUpWizard signUpWizard) {
        this.signUpWizard = signUpWizard;
        this.form = new LegalInfoForm();
    }

    @Override
	public String getCaption() {
		return signUpWizard.getI18nLabel("additional_info");
	}

	@Override
	public Component getContent() {
		return form;
	}

	@Override
	public boolean onAdvance() {
		return form.validateForm();
	}

	@Override
	public boolean onBack() {
		return true;
	}

    public LegalInfoInput getValue() {
        return form.getValue();
    }
}
