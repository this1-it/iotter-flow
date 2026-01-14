package it.thisone.iotter.persistence.model;

import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Indexes;
import org.hibernate.validator.constraints.NotEmpty;

import it.thisone.iotter.enums.AccountStatus;
import it.thisone.iotter.enums.AreasOfInterest;
import it.thisone.iotter.enums.JobRole;

/**
 * Entity class for users. This class keeps information about registered users.
 * 
 * @author tisone
 * 
 */
@Cacheable(false)
//@Cache(type = CacheType.WEAK, expiryTimeOfDay = @TimeOfDay(hour = 3))
@Entity
@Indexes({ @Index(name = "USER_OWNER_INDEX", columnNames = { "OWNER" }),
		@Index(name = "USER_NAME_INDEX", columnNames = { "USERNAME" }),
		@Index(name = "USER_STATUS_INDEX", columnNames = { "ACCOUNT_STATUS" }),
		@Index(name = "USER_LASTNAME_INDEX", columnNames = { "LAST_NAME" }) })
@Table(name = "USER")
public class User extends BaseEntity {

	private static final long serialVersionUID = -7221390454520192814L;

	public User() {
		super();
		this.groups = new HashSet<NetworkGroup>();
		this.roles = new HashSet<Role>();
		this.country = "IT";
		this.loginFailures = 0;
	}

	@Override
	public String toString() {
		return firstName + " " + lastName;
	}

	@Override
	public int hashCode() {
		return username != null ? username.hashCode() : 0;
	}

	@Override
	public boolean equals(final Object obj) {
		return obj != null && obj instanceof User && username.equals(((User) obj).getUsername());
	}

	public User(String username, String password, AccountStatus accountStatus, String firstName, String lastName,
			String email, String street, String city, String zipCode, String phoneNumber, String country) {
		super();
		this.username = username;
		this.password = password;
		this.accountStatus = accountStatus;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.street = street;
		this.city = city;
		this.zip = zipCode;
		this.phone = phoneNumber;
		this.country = country;
		this.loginFailures = 0;
	}

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "USER_GROUP", joinColumns = @JoinColumn(name = "USER_ID"), inverseJoinColumns = @JoinColumn(name = "GROUP_ID"))
	private Set<NetworkGroup> groups;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "USER_ROLE", joinColumns = @JoinColumn(name = "USER_ID"), inverseJoinColumns = @JoinColumn(name = "ROLE_ID"))
	private Set<Role> roles;

	@NotEmpty
	@Column(name = "USERNAME", unique = true)
	private String username;

	@Column(name = "PASSWORD")
	private String password;

	@Column(name = "DESCRIPTION")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "ACCOUNT_STATUS")
	private AccountStatus accountStatus;

	@Column(name = "FORCE_PASSWORD_CHANGE")
	private boolean forcePasswordChange = false;

	@Column(name = "REASON_FOR_ACCOUNT_LOCKED")
	private String reasonForLockedAccount;

	@Column(name = "FIRST_NAME")
	private String firstName;

	@Column(name = "LAST_NAME")
	private String lastName;

	@Column(name = "EMAIL")
	private String email;

	@Column(name = "COMPANY")
	private String company;

	@Column(name = "STREET")
	private String street;

	@Column(name = "CITY")
	private String city;

	@Column(name = "ZIP")
	private String zip;

	// @Pattern(regexp="\\(\\d{3}\\)\\d{3}-\\d{4}")
	@Column(name = "PHONE")
	private String phone;

	@Column(name = "COUNTRY")
	private String country;

	@Temporal(TemporalType.DATE)
	@Column(name = "EXPIRY")
	private Date expiryDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "PREVIOUS_LOGIN")
	private Date previousLoginDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_LOGIN")
	private Date lastLoginDate;

	@Column(name = "LOGIN_FAILURES")
	private int loginFailures;

	@ElementCollection(fetch = FetchType.LAZY)
	@Enumerated(EnumType.STRING)
	@CollectionTable(name = "USER_AREAS", joinColumns = @JoinColumn(name = "USER_ID"))
	@Column(name = "AREAS")
	private Set<AreasOfInterest> areas;
	{
		this.areas = EnumSet.noneOf(AreasOfInterest.class);
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "JOB_ROLE", nullable = true)
	private JobRole jobRole;


	public Network getNetwork() {
		// Feature #1884
		for (NetworkGroup group : getGroups()) {
			if (group.getGroupType() == null && group.isDefaultGroup()) {
				return group.getNetwork();
			}
		}
		return null;
	}

	public Set<NetworkGroup> getGroups() {
		if (groups == null) {
			groups = new HashSet<NetworkGroup>();
		}
		return groups;
	}

	public void setGroups(Set<NetworkGroup> groups) {
		this.groups = groups;
	}

	public void addGroup(NetworkGroup group) {
		if (!getGroups().contains(group)) {
			getGroups().add(group);
		}
	}

	public Set<Role> getRoles() {
		if (roles == null) {
			roles = new HashSet<Role>();
		}
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = new HashSet<Role>(roles);
	}

	/**
	 * workaround for having single role assignment
	 * 
	 * @return
	 */
	public Role getRole() {
		if (roles == null || roles.isEmpty()) {
			return null;
		}
		return roles.iterator().next();
	}

	/**
	 * workaround for having single role assignment
	 * 
	 * @param role
	 */
	public void setRole(Role role) {
		this.roles = new HashSet<Role>();
		this.roles.add(role);
	}

	public void addRole(Role role) {
		if (!getRoles().contains(role)) {
			roles.add(role);
		}
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
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

	/**
	 * Get the username of the user
	 * 
	 * @return User's username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Set the username for the user
	 * 
	 * @param username
	 *            New username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Get the hashed password of the user
	 * 
	 * @return Hashed password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Set the hashed password of the user
	 * 
	 * @param password
	 *            New hHashed password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Sets the reason why the account has been locked
	 * 
	 * @param reasonForLockedAccount
	 *            Reason for account being locked
	 */
	public void setReasonForLockedAccount(String reasonForLockedAccount) {
		this.reasonForLockedAccount = reasonForLockedAccount;
	}

	/**
	 * Get the reason why the account has been locked
	 * 
	 * @return Reason for account being locked
	 */
	public String getReasonForLockedAccount() {
		return reasonForLockedAccount;
	}

	public boolean isForcePasswordChange() {
		return forcePasswordChange;
	}

	public void setForcePasswordChange(boolean forcePasswordChange) {
		this.forcePasswordChange = forcePasswordChange;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public Date getPreviousLoginDate() {
		return previousLoginDate;
	}

	public void setPreviousLoginDate(Date previousLoginDate) {
		this.previousLoginDate = previousLoginDate;
	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public AccountStatus getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(AccountStatus accountStatus) {
		this.accountStatus = accountStatus;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getLoginFailures() {
		return loginFailures;
	}

	public void setLoginFailures(int loginFailures) {
		this.loginFailures = loginFailures;
	}

	public JobRole getJobRole() {
		return jobRole;
	}

	public void setJobRole(JobRole jobRole) {
		this.jobRole = jobRole;
	}

	public Set<AreasOfInterest> getAreas() {
		return areas;
	}

	public void setAreas(Set<AreasOfInterest> areas) {
		this.areas = areas;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getDisplayName() {
		String first = firstName != null ? firstName : "";
		String last = lastName != null ? lastName : "";
		return String.format("%s %s", first, last).trim();
	}

	public boolean hasRole(String name) {
		for (Role role : getRoles()) {
			if (name.equals(role.getName())) {
				return true;
			}
		}
		return false;
	}

	public boolean hasGroup(NetworkGroup group) {
		return getGroups().contains(group);
	}

}
