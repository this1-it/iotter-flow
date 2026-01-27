package it.thisone.iotter.ui.common;

import com.vaadin.flow.server.VaadinSession;

import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.security.UserDetailsAdapter;

/**
 * @deprecated Use {@link AuthenticatedUser} instead. This class relies on static
 * access patterns that are not compatible with Vaadin Flow's dependency injection model.
 * Inject {@link AuthenticatedUser} into your components and use {@code authenticatedUser.get()}
 * to obtain the current user.
 */
@Deprecated
public final class UserSession {

    public static final String USER_DETAILS_KEY = UserDetailsAdapter.class.getName();

    private UserSession() {
    }

    public static void setUserDetails(UserDetailsAdapter details) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(USER_DETAILS_KEY, details);
        }
    }

    public static UserDetailsAdapter getUserDetails() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return createAnonymous();
        }
        UserDetailsAdapter details = (UserDetailsAdapter) session.getAttribute(USER_DETAILS_KEY);
        if (details == null) {
            return createAnonymous();
        }
        return details;
    }

    private static UserDetailsAdapter createAnonymous() {
        User anonymous = new User();
        anonymous.setUsername("anonymous");
        anonymous.setOwner("anonymous");
        return new UserDetailsAdapter(anonymous);
    }
}
