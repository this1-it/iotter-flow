package it.thisone.iotter.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import it.thisone.iotter.enums.FtpType;

@Embeddable
public class FtpAccess implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
  

	public FtpAccess() {
		super();
	}
	
	
	@Column(name = "HOST")
	private String host;
	
	@Column(name = "USER")
	private String user;

	@Column(name = "PASSWORD")
	private String password;

	@Column(name = "PATH")
	private String path;

	@Column(name = "MAX_QUOTA")
	private int maxQuota = 0;
	
	@Column(name = "TYPE")
	private FtpType type = FtpType.DISABLE;


	public String getHost() {
		return host;
	}


	public void setHost(String host) {
		this.host = host;
	}


	public String getUser() {
		return user;
	}


	public void setUser(String user) {
		this.user = user;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getPath() {
		return path;
	}


	public void setPath(String path) {
		this.path = path;
	}


	public int getMaxQuota() {
		return maxQuota;
	}


	public void setMaxQuota(int maxQuota) {
		this.maxQuota = maxQuota;
	}


	public FtpType getType() {
		return type;
	}


	public void setType(FtpType type) {
		this.type = type;
	}

	
	

}
