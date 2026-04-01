package it.thisone.iotter.ui.authentication;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;

import it.thisone.iotter.ui.common.AuthenticatedUser;
import it.thisone.iotter.ui.main.MainView;

@Route("oauth2callback")
public class OAuth2CallbackView extends VerticalLayout implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        WrappedSession session = VaadinRequest.getCurrent().getWrappedSession();

        Authentication auth = (Authentication) session
                .getAttribute(GoogleOAuth2SuccessHandler.OAUTH2_AUTH_SESSION_KEY);

        if (auth != null) {
            session.removeAttribute(GoogleOAuth2SuccessHandler.OAUTH2_AUTH_SESSION_KEY);
            SecurityContextHolder.getContext().setAuthentication(auth);
            VaadinSession.getCurrent().setAttribute(
                    AuthenticatedUser.SESSION_AUTHENTICATION_KEY, auth);
            event.forwardTo(MainView.class);
        } else {
            event.forwardTo(LoginScreen.class);
        }
    }
}
