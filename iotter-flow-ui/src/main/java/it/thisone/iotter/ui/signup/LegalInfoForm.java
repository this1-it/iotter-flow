package it.thisone.iotter.ui.signup;

import org.vaadin.firitin.form.AbstractForm;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.textfield.TextField;

import it.thisone.iotter.enums.IdentityProfileType;
import it.thisone.iotter.ui.common.fields.CountrySelect;

public class LegalInfoForm extends AbstractForm<LegalInfoInput> {
    private final ComboBox<IdentityProfileType> profileType;
    private final TextField firstName;
    private final TextField lastName;
    private final TextField taxCode;
    private final CountrySelect taxCodeCountry;
    private final TextField vatNumber;
    private final TextField birthCountry;
    private final TextField birthCity;
    private final TextField companyName;
    private final TextField companyRegistrationId;
    private final TextField companyEmail;
    private final TextField companyPhone;
    private final TextField addressStreet;
    private final TextField addressAdditions;
    private final TextField addressZipcode;
    private final TextField addressStateProvince;
    private final TextField addressCity;
    private final CountrySelect addressCountry;
    private final Checkbox correctness;
    private final Checkbox conditions;

    public LegalInfoForm() {
        super(LegalInfoInput.class);

        profileType = new ComboBox<>(label("profileType", "Profile Type"));
        profileType.setItems(IdentityProfileType.values());
        profileType.setItemLabelGenerator(type -> getTranslation(type.getI18nKey()));
        profileType.setRequiredIndicatorVisible(true);

        firstName = requiredText("firstName");
        lastName = requiredText("lastName");
        taxCode = requiredText("taxCode", "Tax Code");

        taxCodeCountry = new CountrySelect();
        taxCodeCountry.setLabel(label("taxCodeCountry", "Tax Code Country"));
        taxCodeCountry.setRequiredIndicatorVisible(true);
        taxCodeCountry.setWidthFull();

        vatNumber = fullWidthText("vatNumber", "VAT Number");
        birthCountry = fullWidthText("birthCountry", "Birth Country");
        birthCity = fullWidthText("birthCity", "Birth City");
        companyName = fullWidthText("company", "Company");
        companyRegistrationId = fullWidthText("companyRegistrationId", "Company Registration ID");
        companyEmail = fullWidthText("companyEmail", "Company Email");
        companyPhone = fullWidthText("phone", "Phone Number");
        addressStreet = fullWidthText("street");
        addressAdditions = fullWidthText("addressAdditions", "Address Additions");
        addressZipcode = fullWidthText("zip");
        addressStateProvince = fullWidthText("addressStateProvince", "State / Province");
        addressCity = fullWidthText("city");

        addressCountry = new CountrySelect();
        addressCountry.setLabel(label("country", "Country"));
        addressCountry.setWidthFull();

        correctness = new Checkbox(label("correctness", "I confirm that the information is correct"));
        conditions = new Checkbox(label("accept_disclaimer", "Accept disclaimer"));

        bindFields();
        setEntity(new LegalInfoInput());
    }

    private void bindFields() {
        String requiredMessage = getTranslation("validators.fieldgroup_errors");

        getBinder().forField(profileType)
                .asRequired(requiredMessage)
                .bind(LegalInfoInput::getProfileType, LegalInfoInput::setProfileType);
        getBinder().forField(firstName)
                .asRequired(requiredMessage)
                .bind(LegalInfoInput::getFirstName, LegalInfoInput::setFirstName);
        getBinder().forField(lastName)
                .asRequired(requiredMessage)
                .bind(LegalInfoInput::getLastName, LegalInfoInput::setLastName);
        getBinder().forField(taxCode)
                .asRequired(requiredMessage)
                .bind(LegalInfoInput::getTaxCode, LegalInfoInput::setTaxCode);
        getBinder().forField(taxCodeCountry)
                .asRequired(requiredMessage)
                .bind(LegalInfoInput::getTaxCodeCountry, LegalInfoInput::setTaxCodeCountry);
        getBinder().forField(vatNumber)
                .bind(LegalInfoInput::getVatNumber, LegalInfoInput::setVatNumber);
        getBinder().forField(birthCountry)
                .bind(LegalInfoInput::getBirthCountry, LegalInfoInput::setBirthCountry);
        getBinder().forField(birthCity)
                .bind(LegalInfoInput::getBirthCity, LegalInfoInput::setBirthCity);
        getBinder().forField(companyName)
                .bind(LegalInfoInput::getCompanyName, LegalInfoInput::setCompanyName);
        getBinder().forField(companyRegistrationId)
                .bind(LegalInfoInput::getCompanyRegistrationId, LegalInfoInput::setCompanyRegistrationId);
        getBinder().forField(companyEmail)
                .bind(LegalInfoInput::getCompanyEmail, LegalInfoInput::setCompanyEmail);
        getBinder().forField(companyPhone)
                .bind(LegalInfoInput::getCompanyPhone, LegalInfoInput::setCompanyPhone);
        getBinder().forField(addressStreet)
                .bind(LegalInfoInput::getAddressStreet, LegalInfoInput::setAddressStreet);
        getBinder().forField(addressAdditions)
                .bind(LegalInfoInput::getAddressAdditions, LegalInfoInput::setAddressAdditions);
        getBinder().forField(addressZipcode)
                .bind(LegalInfoInput::getAddressZipcode, LegalInfoInput::setAddressZipcode);
        getBinder().forField(addressStateProvince)
                .bind(LegalInfoInput::getAddressStateProvince, LegalInfoInput::setAddressStateProvince);
        getBinder().forField(addressCity)
                .bind(LegalInfoInput::getAddressCity, LegalInfoInput::setAddressCity);
        getBinder().forField(addressCountry)
                .bind(LegalInfoInput::getAddressCountry, LegalInfoInput::setAddressCountry);
        getBinder().forField(correctness)
                .withValidator(Boolean.TRUE::equals, label("must_accept_disclaimer", "Mandatory"))
                .bind(LegalInfoInput::isCorrectness, LegalInfoInput::setCorrectness);
        getBinder().forField(conditions)
                .withValidator(Boolean.TRUE::equals, label("must_accept_disclaimer", "Mandatory"))
                .bind(LegalInfoInput::isConditions, LegalInfoInput::setConditions);
    }

    public boolean validateForm() {
        return getBinder().writeBeanIfValid(getEntity());
    }

    public LegalInfoInput getValue() {
        return getEntity();
    }

    @Override
    protected Component createContent() {
        getToolbar().setVisible(false);
        getSaveButton().setVisible(false);
        getResetButton().setVisible(false);
        getDeleteButton().setVisible(false);

        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));
        formLayout.add(profileType, taxCodeCountry);
        formLayout.add(firstName, lastName);
        formLayout.add(taxCode, vatNumber);
        formLayout.add(companyName, companyRegistrationId);
        formLayout.add(companyEmail, companyPhone);
        formLayout.add(addressStreet, addressAdditions);
        formLayout.add(addressZipcode, addressStateProvince);
        formLayout.add(addressCity, addressCountry);
        formLayout.add(birthCity, birthCountry);
        formLayout.add(correctness, conditions);
        return formLayout;
    }

    private TextField requiredText(String key) {
        return requiredText(key, null);
    }

    private TextField requiredText(String key, String fallback) {
        TextField field = fullWidthText(key, fallback);
        field.setRequiredIndicatorVisible(true);
        return field;
    }

    private TextField fullWidthText(String key) {
        return fullWidthText(key, null);
    }

    private TextField fullWidthText(String key, String fallback) {
        TextField field = new TextField(label(key, fallback));
        field.setWidthFull();
        return field;
    }

    private String label(String key, String fallback) {
        String translationKey = "register." + key;
        String translated = getTranslation(translationKey);
        if (!translationKey.equals(translated)) {
            return translated;
        }
        return fallback == null ? key : fallback;
    }
}
