package it.thisone.iotter.security;

import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import it.thisone.iotter.enums.AccountStatus;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.model.Role;
import it.thisone.iotter.persistence.model.User;

/**
 * 
 * User Information to be store in security context
 * 
 * @author tisone
 *
 */
public class UserDetailsAdapter implements UserDetails, Principal {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String username;
	private String email;
	private String password;
	private String name;
	private String tenant;
	private Date loginDate;
	private Date previousLoginDate;
	private String sessionId;
	private String home;
	

	private boolean accountNonExpired = false;
	private boolean accountNonLocked = false;
	private boolean credentialsNonExpire = false;
	private boolean enabled = false;

	private Set<GrantedAuthority> authorities;
	private Set<String> roles;
	private Set<String> groupIds;
	private String network;
	private String networkId;
	private String userId;

	public UserDetailsAdapter(User account) {
		userId = account.getId();
		roles = new HashSet<String>();
		if (userId == null) {
			// Bug #390 Supervisor creates a Administrator with wrong owner
			for (Role r : account.getRoles()) {
				roles.add(r.getName());
			}
			return;
		}
		username = account.getUsername();
		email = account.getEmail();
		password = account.getPassword();
		tenant = (username.equals(account.getOwner())) ? username : account
				.getOwner();

		previousLoginDate = account.getPreviousLoginDate();
		loginDate = new Date();
		if (account.getExpiryDate() != null) {
			Date now = new Date();
			accountNonExpired = now.before(account.getExpiryDate());
		} else {
			accountNonExpired = true;
		}

		if (account.getAccountStatus() != null) {
			accountNonLocked = !account.getAccountStatus().equals(
					AccountStatus.LOCKED);
			credentialsNonExpire = !account.isForcePasswordChange();
			enabled = account.getAccountStatus().equals(AccountStatus.ACTIVE);
			name = account.getDisplayName();
			PermissionsToRole.initialize();
			roles = new HashSet<String>();
			authorities = new HashSet<GrantedAuthority>();
			for (Role r : account.getRoles()) {
				roles.add(r.getName());
				authorities.add(new SimpleGrantedAuthority(r.getName()));
				List<EntityPermission> permissions = PermissionsToRole
						.getPermissions(r.getName());
				for (EntityPermission entityPermission : permissions) {
					authorities.add(new SimpleGrantedAuthority(entityPermission
							.toString()));
				}
			}
			groupIds = new HashSet<String>();
			for (NetworkGroup g : account.getGroups()) {
				groupIds.add(g.getId());
			}
			if (account.getNetwork() != null) {
				networkId = account.getNetwork().getId();
				network = account.getNetwork().getName();
			}
		}
	}

	public String getUserId() {
		return userId;
	}

	public String getEmail() {
		return email;
	}

	public String getTenant() {
		return tenant;
	}

	public boolean hasRole(String name) {
		if (roles ==null) return false;
		return roles.contains(name);
	}

	public Set<String> getRoles() {
		return roles;
	}

	public String getNetwork() {
		return network;
	}

	public String getNetworkId() {
		return networkId;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return credentialsNonExpire;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public String getName() {
		return name;
	}

	public Date getLoginDate() {
		return loginDate;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getHome() {
		return home;
	}

	public void setHome(String home) {
		this.home = home;
	}

	public Date getPreviousLoginDate() {
		return previousLoginDate;
	}

	public void setPreviousLoginDate(Date previousLoginDate) {
		this.previousLoginDate = previousLoginDate;
	}

}