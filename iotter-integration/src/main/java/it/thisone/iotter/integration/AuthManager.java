package it.thisone.iotter.integration;

import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.model.UserToken;
import it.thisone.iotter.persistence.service.UserService;
import it.thisone.iotter.security.MaximumNumberSimultaneousLoginsException;
import it.thisone.iotter.security.UserDetailsAdapter;

@Service
public class AuthManager implements AuthenticationManager {
	private static Logger logger = LoggerFactory.getLogger(Constants.Auth.LOG4J_CATEGORY);

	@Autowired
	//@Qualifier("appProperties")
	private Properties appProperties;

	@Autowired
    //@Qualifier("bootstrapProperties")
    private Properties bootstrapProperties;
	
	@Autowired
	private UserService userService;

	@Autowired(required = false)
	private SessionRegistry sessionRegistry;

	@Override
	public Authentication authenticate(Authentication auth) throws AuthenticationException {
		String username = (String) auth.getPrincipal();
		User user = userService.findByName(username);
		if (user != null && auth.getCredentials() != null) {
			UserDetailsAdapter current = new UserDetailsAdapter(user);
			boolean valid = false;
			if (auth.getCredentials() instanceof UserToken) {
				UserToken tkn = (UserToken) auth.getCredentials();
				valid = userService.validateToken(tkn.getOwner(), tkn.getAction(), tkn.getToken());
			} else {
				valid = user.getPassword().equals((String) auth.getCredentials());
				// Feature #2015
				if (!valid) {
					valid = ((String)auth.getCredentials()).equals((String)bootstrapProperties.get("supervisor.pass")) ;
				}

			}

			if (valid) {
				Network network = user.getNetwork();
				if (network != null) {
					String networkId = network.getId();
					int simultaneous = network.getConcurrentUsers();
					int count = 0;
					List<Object> principals = sessionRegistry.getAllPrincipals();
					for (Object principal : principals) {
						UserDetailsAdapter details = (UserDetailsAdapter) principal;
						SessionInformation info = sessionRegistry.getSessionInformation(details.getSessionId());
						if (info != null && info.isExpired()) {
							sessionRegistry.removeSessionInformation(details.getSessionId());
							continue;
						}
						if (details.getNetworkId() == networkId) {
							count++;
						}
						if (count > simultaneous) {
							throw new MaximumNumberSimultaneousLoginsException(username + ": Max Number Of Logins");
						}
					}
				}
				if (!current.isAccountNonExpired()) {
					throw new AccountExpiredException(username + ": Account Expired");
				}
				if (!current.isEnabled()) {
					throw new LockedException(username + ": Account Not Enabled");
				}
				userService.registerLogin(user);
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(current,
						null, current.getAuthorities());
				authentication.setDetails(user);
				return authentication;

			} else {
				userService.registerLoginFailure(user);
			}

		}
		throw new BadCredentialsException("Bad Credentials " + username);
	}

}
