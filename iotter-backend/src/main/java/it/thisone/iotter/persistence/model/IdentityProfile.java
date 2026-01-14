package it.thisone.iotter.persistence.model;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import it.thisone.iotter.enums.IdentityProfileType;
/**
 * JPA entity for IdentityProfile, extending BaseEntity.
 */
@Entity
@Table(name = "IDENTITY_PROFILE")
public class IdentityProfile extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@OneToOne
	@JoinColumn(name = "USER_ID", referencedColumnName = "id")
	private User user;
	
	@Enumerated(EnumType.STRING)
    @Column(name = "PROFILE_TYPE", nullable = false)
	private IdentityProfileType profileType;
	
   
    
	@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT", nullable = false)
    private Date createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private Date updatedAt;
    
    @Column(name = "FIRST_NAME", nullable = false)
    private String firstName;
    
    @Column(name = "LAST_NAME", nullable = false)
    private String lastName;
    
    @Column(name = "TAX_CODE", nullable = false)
    private String taxCode;
    
    @Column(name = "BIRTH_DATE")
    private String birthDate;
    
    @Column(name = "PERSONAL_PHONE")
    private String personalPhone;
    
    @Column(name = "PERSONAL_EMAIL")
    private String personalEmail;
    
    @Column(name = "COMPANY_NAME")
    private String companyName;
    
    @Column(name = "VAT_NUMBER")
    private String vatNumber;
    
    @Column(name = "ADDRESS_STREET")
    private String addressStreet;
    
    @Column(name = "JOB_TITLE")
    private String jobTitle;
    
    @Column(name = "DEPARTMENT")
    private String department;
    
    @Column(name = "COMPANY_EMAIL")
    private String companyEmail;
    
    @Column(name = "COMPANY_PHONE")
    private String companyPhone;

    
    @Column(name = "FULL_NAME")
    private String fullName;
    
    @Column(name = "BIRTH_CITY")
    private String birthCity;
    
    @Column(name = "BIRTH_COUNTRY")
    private String birthCountry;
    
    @Column(name = "TAX_CODE_COUNTRY", nullable = false)
    private String taxCodeCountry;
    
    @Column(name = "COMPANY_REGISTRATION_ID")
    private String companyRegistrationId;
    
    @Column(name = "ADDRESS_ADDITIONS")
    private String addressAdditions;
    
    @Column(name = "ADDRESS_ZIPCODE")
    private String addressZipcode;
    
    @Column(name = "ADDRESS_COUNTRY")
    private String addressCountry;
    
    @Column(name = "ADDRESS_CITY")
    private String addressCity;
    
    @Column(name = "ADDRESS_STATE_PROVINCE")
    private String addressStateProvince;

    // Getters and setters

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getPersonalPhone() {
        return personalPhone;
    }

    public void setPersonalPhone(String personalPhone) {
        this.personalPhone = personalPhone;
    }

    public String getPersonalEmail() {
        return personalEmail;
    }

    public void setPersonalEmail(String personalEmail) {
        this.personalEmail = personalEmail;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getAddressStreet() {
        return addressStreet;
    }

    public void setAddressStreet(String addressStreet) {
        this.addressStreet = addressStreet;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
    }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }

    public IdentityProfileType getProfileType() {
        return profileType;
    }

    public void setProfileType(IdentityProfileType profileType) {
        this.profileType = profileType;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBirthCity() {
        return birthCity;
    }

    public void setBirthCity(String birthCity) {
        this.birthCity = birthCity;
    }

    public String getBirthCountry() {
        return birthCountry;
    }

    public void setBirthCountry(String birthCountry) {
        this.birthCountry = birthCountry;
    }

    public String getTaxCodeCountry() {
        return taxCodeCountry;
    }

    public void setTaxCodeCountry(String taxCodeCountry) {
        this.taxCodeCountry = taxCodeCountry;
    }

    public String getCompanyRegistrationId() {
        return companyRegistrationId;
    }

    public void setCompanyRegistrationId(String companyRegistrationId) {
        this.companyRegistrationId = companyRegistrationId;
    }

    public String getAddressAdditions() {
        return addressAdditions;
    }

    public void setAddressAdditions(String addressAdditions) {
        this.addressAdditions = addressAdditions;
    }

    public String getAddressZipcode() {
        return addressZipcode;
    }

    public void setAddressZipcode(String addressZipcode) {
        this.addressZipcode = addressZipcode;
    }

    public String getAddressCountry() {
        return addressCountry;
    }

    public void setAddressCountry(String addressCountry) {
        this.addressCountry = addressCountry;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public String getAddressStateProvince() {
        return addressStateProvince;
    }

    public void setAddressStateProvince(String addressStateProvince) {
        this.addressStateProvince = addressStateProvince;
    }

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
