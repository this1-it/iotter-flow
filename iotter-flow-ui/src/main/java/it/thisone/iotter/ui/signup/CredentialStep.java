package it.thisone.iotter.ui.signup;

import it.thisone.iotter.ui.wizards.WizardStep;

import com.vaadin.flow.component.Component;

import it.thisone.iotter.config.Constants;

public class CredentialStep implements WizardStep, Constants {

	CredentialInputForm form;
	
	public CredentialStep(SignUpWizard signUpWizard) {
		this.form = new CredentialInputForm();
	}

	@Override
	public String getCaption() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Component getContent() {
		// TODO Auto-generated method stub
		return this.form;
	}

	@Override
	public boolean onAdvance() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onBack() {
		// TODO Auto-generated method stub
		return false;
	}

}
