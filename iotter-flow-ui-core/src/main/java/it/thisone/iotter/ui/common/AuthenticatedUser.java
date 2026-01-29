package it.thisone.iotter.ui.common;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import com.vaadin.flow.server.VaadinSession;

import it.thisone.iotter.security.UserDetailsAdapter;

/**
 * Injectable service providing access to the currently authenticated user.
 * This replaces the static {@link UserSession} class with a proper
 * Spring-managed, Vaadin session-scoped component.
 */
@VaadinSessionScope
@Component
public class AuthenticatedUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Vaadin session key used to persist the authenticated {@link Authentication}
     * across Vaadin requests.
     */
    public static final String SESSION_AUTHENTICATION_KEY = Authentication.class.getName();

    /**
     * Gets the current authenticated user from the security context.
     *
     * @return Optional containing the UserDetailsAdapter if authenticated,
     *         empty otherwise
     */
    public Optional<UserDetailsAdapter> get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isUserDetailsAuthentication(auth)) {
            auth = getAuthenticationFromVaadinSession();
            if (isUserDetailsAuthentication(auth)) {
                // Re-hydrate the Spring Security context for the current thread.
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        if (auth != null && auth.getPrincipal() instanceof UserDetailsAdapter) {
            return Optional.of((UserDetailsAdapter) auth.getPrincipal());
        }
        return Optional.empty();
    }

    private Authentication getAuthenticationFromVaadinSession() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return null;
        }
        return (Authentication) session.getAttribute(SESSION_AUTHENTICATION_KEY);
    }

    private boolean isUserDetailsAuthentication(Authentication auth) {
        return auth != null && auth.getPrincipal() instanceof UserDetailsAdapter;
    }

    /**
     * Checks if a user is currently logged in with a valid user ID.
     *
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return get().map(u -> u.getUserId() != null).orElse(false);
    }

    /**
     * Checks if the current user has the specified role.
     *
     * @param role the role name to check
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(String role) {
        return get().map(u -> u.hasRole(role)).orElse(false);
    }

    /**
     * Gets the user ID of the current user.
     *
     * @return Optional containing the user ID if authenticated, empty otherwise
     */
    public Optional<String> getUserId() {
        return get().map(UserDetailsAdapter::getUserId);
    }

    /**
     * Gets the tenant of the current user.
     *
     * @return Optional containing the tenant if authenticated, empty otherwise
     */
    public Optional<String> getTenant() {
        return get().map(UserDetailsAdapter::getTenant);
    }
}
