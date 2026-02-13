package it.thisone.iotter.ui.signup;


import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import it.thisone.iotter.enums.IdentityProfileType;

public class LegalInfoInput {

    @NotNull(message = "Profile type is required")
    private IdentityProfileType profileType;

    @NotNull(message = "First name is required")
    @Size(max = 255, message = "First name must be at most 255 characters")
    private String firstName;

    @NotNull(message = "Last name is required")
    @Size(max = 255, message = "Last name must be at most 255 characters")
    private String lastName;

    @NotNull(message = "Tax code is required")
    @Size(min = 16, max = 16, message = "Tax code must be exactly 16 characters")
    private String taxCode;

    @NotNull(message = "Tax code country is required")
    @Size(min = 2, max = 255, message = "Tax code country must be between 2 and 255 characters")
    private String taxCodeCountry;

    @Size(max = 255, message = "VAT number must be at most 255 characters")
    private String vatNumber;

    // Optional date field, expected to be provided as Date when present
    private Date birthDate;

    @Size(max = 255, message = "Birth country must be at most 255 characters")
    private String birthCountry;

    @Size(max = 255, message = "Birth city must be at most 255 characters")
    private String birthCity;

    @Size(max = 255, message = "Company name must be at most 255 characters")
    private String companyName;

    @Size(max = 255, message = "Company registration ID must be at most 255 characters")
    private String companyRegistrationId;

    @Size(max = 255, message = "Company email must be at most 255 characters")
    private String companyEmail;

    @Size(max = 255, message = "Company phone must be at most 255 characters")
    private String companyPhone;

    @Size(max = 255, message = "Address street must be at most 255 characters")
    private String addressStreet;

    @Size(max = 255, message = "Address additions must be at most 255 characters")
    private String addressAdditions;

    @Size(max = 255, message = "Address zipcode must be at most 255 characters")
    private String addressZipcode;

    @Size(max = 255, message = "Address state/province must be at most 255 characters")
    private String addressStateProvince;

    @Size(max = 255, message = "Address city must be at most 255 characters")
    private String addressCity;

    @Size(max = 255, message = "Address country must be at most 255 characters")
    private String addressCountry;


    private boolean correctness = false;
    private boolean conditions = false;

    // Getters and setters

    public IdentityProfileType getProfileType() {
        return profileType;
    }

    public void setProfileType(IdentityProfileType profileType) {
        this.profileType = profileType;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName != null ? firstName.trim() : null;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName != null ? lastName.trim() : null;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode != null ? taxCode.trim() : null;
    }

    public String getTaxCodeCountry() {
        return taxCodeCountry;
    }

    public void setTaxCodeCountry(String taxCodeCountry) {
        this.taxCodeCountry = taxCodeCountry != null ? taxCodeCountry.trim() : null;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber != null ? vatNumber.trim() : null;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getBirthCountry() {
        return birthCountry;
    }

    public void setBirthCountry(String birthCountry) {
        this.birthCountry = birthCountry != null ? birthCountry.trim() : null;
    }

    public String getBirthCity() {
        return birthCity;
    }

    public void setBirthCity(String birthCity) {
        this.birthCity = birthCity != null ? birthCity.trim() : null;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName != null ? companyName.trim() : null;
    }

    public String getCompanyRegistrationId() {
        return companyRegistrationId;
    }

    public void setCompanyRegistrationId(String companyRegistrationId) {
        this.companyRegistrationId = companyRegistrationId != null ? companyRegistrationId.trim() : null;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail != null ? companyEmail.trim() : null;
    }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone != null ? companyPhone.trim() : null;
    }

    public String getAddressStreet() {
        return addressStreet;
    }

    public void setAddressStreet(String addressStreet) {
        this.addressStreet = addressStreet != null ? addressStreet.trim() : null;
    }

    public String getAddressAdditions() {
        return addressAdditions;
    }

    public void setAddressAdditions(String addressAdditions) {
        this.addressAdditions = addressAdditions != null ? addressAdditions.trim() : null;
    }

    public String getAddressZipcode() {
        return addressZipcode;
    }

    public void setAddressZipcode(String addressZipcode) {
        this.addressZipcode = addressZipcode != null ? addressZipcode.trim() : null;
    }

    public String getAddressStateProvince() {
        return addressStateProvince;
    }

    public void setAddressStateProvince(String addressStateProvince) {
        this.addressStateProvince = addressStateProvince != null ? addressStateProvince.trim() : null;
    }

    public String getAddressCountry() {
        return addressCountry;
    }

    public void setAddressCountry(String addressCountry) {
        this.addressCountry = addressCountry != null ? addressCountry.trim() : null;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity != null ? addressCity.trim() : null;
    }

    public boolean isCorrectness() {
        return correctness;
    }

    public void setCorrectness(boolean correctness) {
        this.correctness = correctness;
    }

    public boolean isConditions() {
        return conditions;
    }

    public void setConditions(boolean conditions) {
        this.conditions = conditions;
    }
}
