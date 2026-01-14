package it.thisone.iotter.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;

/**
 * http://stackoverflow.com/questions/2106601/spring-limit-max-sessions-limit-max-users
 * http://stackoverflow.com/questions/19359644/how-to-control-duplicate-login-process-in-seam
 * 
 * @author tisone
 *
 */
public class MySessionAuthenticationStrategy extends ConcurrentSessionControlAuthenticationStrategy {
    int MAX_USERS = 1000; // Whatever
    SessionRegistry sessionRegistry;

    public MySessionAuthenticationStrategy(SessionRegistry sr) {
        super(sr);
        this.sessionRegistry = sr;
    }

    @Override
    public void onAuthentication(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        if (sessionRegistry.getAllPrincipals().size() > MAX_USERS) {
            throw new SessionAuthenticationException("Maximum number of users exceeded");
        }
        super.onAuthentication(authentication, request, response);
    }
}