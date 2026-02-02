package it.thisone.iotter.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.server.VaadinSession;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Error view that handles IllegalStateException (typically authentication errors).
 * Redirects to login page when user is not authenticated.
 */
public class AuthenticationErrorView extends VerticalLayout implements HasErrorParameter<IllegalStateException> {

    private static final long serialVersionUID = -8589371266525064393L;
	private static final Logger logger = LoggerFactory.getLogger(AuthenticationErrorView.class);

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<IllegalStateException> parameter) {
        String message = parameter.getException().getMessage();
        logger.warn("IllegalStateException during navigation to '{}': {}",
                event.getLocation().getPath(), message);



        VaadinSession.getCurrent().getSession().invalidate();
        UI.getCurrent().navigate("login");

        return HttpServletResponse.SC_UNAUTHORIZED;
    }
}
