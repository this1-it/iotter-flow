package it.thisone.iotter.ui.signup;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.IdentityProfile;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.UserService;
import it.thisone.iotter.ui.authentication.LoginScreen;
import it.thisone.iotter.ui.wizards.Wizard;
import it.thisone.iotter.ui.wizards.event.WizardCancelledEvent;
import it.thisone.iotter.ui.wizards.event.WizardCompletedEvent;
import it.thisone.iotter.ui.wizards.event.WizardProgressListener;
import it.thisone.iotter.ui.wizards.event.WizardStepActivationEvent;
import it.thisone.iotter.ui.wizards.event.WizardStepSetChangedEvent;

public class SignUpWizard extends Composite<VerticalLayout> implements
WizardProgressListener, ISignUpWizard {

	private static final long serialVersionUID = 4337523807788095820L;
    private final UserService userService;
    private final CredentialStep credentialStep;
    private final LegalInfoStep legalInfoStep;
    private final Wizard wizard;
    private boolean submitting;

	public SignUpWizard(UserService userService, DeviceService deviceService) {
        this.userService = userService;
        this.wizard = new Wizard();
        this.credentialStep = new CredentialStep(this, userService, deviceService);
        this.legalInfoStep = new LegalInfoStep(this);

		wizard.addStep(credentialStep, "credentials");
		wizard.addStep(legalInfoStep, "legalinfo");
		wizard.addListener(this);

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
		content.setPadding(false);

		content.add(wizard);
	}

	public String getI18nLabel(String key) {
		return getTranslation("register." + key);
	}

	@Override
	public void activeStepChanged(WizardStepActivationEvent event) {
	}

	@Override
	public void stepSetChanged(WizardStepSetChangedEvent event) {
	}

	@Override
	public void wizardCompleted(WizardCompletedEvent event) {
        if (submitting) {
            return;
        }
        submitting = true;
        setButtonsEnabled(false);
        try {
            CredentialInput credential = credentialStep.getValue();
            LegalInfoInput legalInfo = legalInfoStep.getValue();
            User user = buildUser(credential, legalInfo);
            IdentityProfile identityProfile = buildIdentityProfile(credential, legalInfo);
            userService.userRegistration(user, credential.getSerialNumber(), identityProfile);
            getUI().ifPresent(ui -> ui.navigate(LoginScreen.class));
        } catch (BackendServiceException e) {
            setButtonsEnabled(true);
            submitting = false;
            String message = e.getMessage() == null ? getTranslation("landing.register.message") : e.getMessage();
            it.thisone.iotter.util.PopupNotification.show(message, it.thisone.iotter.util.PopupNotification.Type.ERROR);
        }
	}

	@Override
	public void wizardCancelled(WizardCancelledEvent event) {
        getUI().ifPresent(ui -> ui.navigate(LoginScreen.class));
	}

    private void setButtonsEnabled(boolean enabled) {
        for (Button button : new Button[] { wizard.getCancelButton(), wizard.getBackButton(), wizard.getNextButton(),
                wizard.getFinishButton() }) {
            button.setEnabled(enabled);
        }
    }

    private User buildUser(CredentialInput credential, LegalInfoInput legalInfo) {
        User user = new User();
        user.setUsername(credential.getUsername());
        user.setPassword(credential.getPassword());
        user.setOwner(credential.getUsername());
        user.setEmail(credential.getEmail());
        user.setFirstName(legalInfo.getFirstName());
        user.setLastName(legalInfo.getLastName());
        user.setCompany(legalInfo.getCompanyName());
        user.setPhone(legalInfo.getCompanyPhone());
        user.setStreet(legalInfo.getAddressStreet());
        user.setCity(legalInfo.getAddressCity());
        user.setZip(legalInfo.getAddressZipcode());
        user.setCountry(legalInfo.getAddressCountry());
        return user;
    }

    private IdentityProfile buildIdentityProfile(CredentialInput credential, LegalInfoInput legalInfo) {
        IdentityProfile profile = new IdentityProfile();
        profile.setProfileType(legalInfo.getProfileType());
        profile.setFirstName(legalInfo.getFirstName());
        profile.setLastName(legalInfo.getLastName());
        profile.setTaxCode(legalInfo.getTaxCode());
        profile.setTaxCodeCountry(legalInfo.getTaxCodeCountry());
        profile.setVatNumber(legalInfo.getVatNumber());
        profile.setBirthCity(legalInfo.getBirthCity());
        profile.setBirthCountry(legalInfo.getBirthCountry());
        profile.setCompanyName(legalInfo.getCompanyName());
        profile.setCompanyRegistrationId(legalInfo.getCompanyRegistrationId());
        profile.setCompanyEmail(legalInfo.getCompanyEmail());
        profile.setCompanyPhone(legalInfo.getCompanyPhone());
        profile.setAddressStreet(legalInfo.getAddressStreet());
        profile.setAddressAdditions(legalInfo.getAddressAdditions());
        profile.setAddressZipcode(legalInfo.getAddressZipcode());
        profile.setAddressStateProvince(legalInfo.getAddressStateProvince());
        profile.setAddressCity(legalInfo.getAddressCity());
        profile.setAddressCountry(legalInfo.getAddressCountry());
        profile.setPersonalEmail(credential.getEmail());
        profile.setFullName((legalInfo.getFirstName() + " " + legalInfo.getLastName()).trim());
        return profile;
    }
}
