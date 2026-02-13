package it.thisone.iotter.ui.signup;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.main.UiConstants;
import it.thisone.iotter.ui.wizards.Wizard;
import it.thisone.iotter.ui.wizards.event.WizardCancelledEvent;
import it.thisone.iotter.ui.wizards.event.WizardCompletedEvent;
import it.thisone.iotter.ui.wizards.event.WizardProgressListener;
import it.thisone.iotter.ui.wizards.event.WizardStepActivationEvent;
import it.thisone.iotter.ui.wizards.event.WizardStepSetChangedEvent;

public class SignUpWizard extends Composite<VerticalLayout> implements
WizardProgressListener, ISignUpWizard {


	private static final long serialVersionUID = 4337523807788095820L;

	@SuppressWarnings("serial")
	public SignUpWizard() {


		Wizard wizard = new Wizard();
		wizard.addStep(new CredentialStep(this), "credentials");
		wizard.addStep(new LegalInfoStep(this), "legalinfo");
		wizard.addListener(this);

		// wizard.setUriFragmentEnabled(true);

		wizard.getCancelButton().addClickListener(event -> {
			// fireEvent(new EditorSavedEvent(SignUpWizard.this, null));
		});

		wizard.getFinishButton().addClickListener(event -> {
			// fireEvent(new EditorSavedEvent(SignUpWizard.this,
			// 		getBeanItem()));
		});

		wizard.getCancelButton().addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		wizard.getFinishButton().addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		wizard.getNextButton().addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		wizard.getBackButton().addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		wizard.getCancelButton().setText(getI18nLabel("cancel"));
		wizard.getFinishButton().setText(getI18nLabel("finish"));
		wizard.getNextButton().setText(getI18nLabel("next"));
		wizard.getBackButton().setText(getI18nLabel("back"));

		VerticalLayout content = getContent();
		content.setSizeFull();
		content.setPadding(true);

		content.add(wizard);
		// setImmediate(true);

	}



	public String getI18nLabel(String key) {
		return getTranslation(UiConstants.IDENTITY_PROFILE + "." + key);
	}



	@Override
	public void activeStepChanged(WizardStepActivationEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stepSetChanged(WizardStepSetChangedEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void wizardCompleted(WizardCompletedEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void wizardCancelled(WizardCancelledEvent event) {
		// TODO Auto-generated method stub
		
	}

}
