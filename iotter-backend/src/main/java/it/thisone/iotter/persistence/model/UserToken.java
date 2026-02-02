package it.thisone.iotter.persistence.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Indexes;
import javax.validation.constraints.NotEmpty;

@Cacheable(false)
@Entity
@Indexes ({
	@Index(name="USER_TOKEN_OWNER_INDEX", columnNames={"OWNER"}),
	@Index(name="USER_TOKEN_INDEX", columnNames={"TOKEN"})
})
@Table(name = "USER_TOKEN", uniqueConstraints = { @UniqueConstraint(columnNames = { "TOKEN", "OWNER"} ) })
public class UserToken extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public UserToken() {
		super();
	}

	public UserToken(String owner, String action, String token) {
		super();
		setOwner(owner);
		setAction(action);
		if (token == null) {
			setToken(UUID.randomUUID().toString().toUpperCase());
		}
		else {
			setToken(token);
		}
	}

	@NotEmpty
	@Column(name = "TOKEN")
	private String token;

	@NotEmpty
	@Column(name = "ACTION")
	private String action;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "EXPIRY_DATE")
	private Date expiryDate;

	public String getToken() {
		return token;
	}


	public void setToken(String token) {
		this.token = token;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public boolean isExpired() {
		return (getExpiryDate() != null) ? getExpiryDate().before(new Date()): false;
	}

	
	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
	
	
}


