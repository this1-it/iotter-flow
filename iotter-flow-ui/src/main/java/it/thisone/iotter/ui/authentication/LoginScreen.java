package it.thisone.iotter.ui.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import it.thisone.iotter.integration.AuthManager;
import it.thisone.iotter.security.MaximumNumberSimultaneousLoginsException;
import it.thisone.iotter.ui.MainView;

/**
 * UI content when the user is not logged in yet.
 */
@Route("Login")
@PageTitle("Login")
@CssImport("./styles/shared-styles.css")
public class LoginScreen extends FlexLayout {

    private final AuthenticationManager authManager;

    @Autowired
    public LoginScreen(AuthManager authManager) {
        this.authManager = authManager;
        buildUI();
    }

    private void buildUI() {
        setSizeFull();
        setClassName("login-screen");

        // login form, centered in the available part of the screen
        LoginForm loginForm = new LoginForm();
        loginForm.addLoginListener(this::login);
        loginForm.addForgotPasswordListener(
                event -> Notification.show("Hint: same as username"));

        // layout to center login form when there is sufficient screen space
        FlexLayout centeringLayout = new FlexLayout();
        centeringLayout.setSizeFull();
        centeringLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        centeringLayout.setAlignItems(Alignment.CENTER);
        centeringLayout.add(loginForm);

        // information text about logging in
        Component loginInformation = buildLoginInformation();

        add(loginInformation);
        add(centeringLayout);
    }

    private Component buildLoginInformation() {
        VerticalLayout loginInformation = new VerticalLayout();
        loginInformation.setClassName("login-information");

        H1 loginInfoHeader = new H1("Login Information");
        loginInfoHeader.setWidth("100%");
        Span loginInfoText = new Span(
                "Log in as \"admin\" to have full access. Log in with any " +
                        "other username to have read-only access. For all " +
                        "users, the password is same as the username.");
        loginInfoText.setWidth("100%");
        loginInformation.add(loginInfoHeader);
        loginInformation.add(loginInfoText);

        return loginInformation;
    }

    private void login(LoginForm.LoginEvent event) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            event.getUsername(), event.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            navigateAfterLogin();
        } catch (AuthenticationException e) {
            event.getSource().setError(true);
            Notification.show(buildFailureMessage(e));
        }
    }

    private void navigateAfterLogin() {
        String target = (String) VaadinSession.getCurrent().getAttribute("POST_LOGIN_ROUTE");
        VaadinSession.getCurrent().setAttribute("POST_LOGIN_ROUTE", null);
        if (target != null) {
            UI.getCurrent().navigate(target);
        } else {
            UI.getCurrent().navigate(MainView.class);
        }
    }

    private String buildFailureMessage(AuthenticationException e) {
        if (e instanceof AccountExpiredException) {
            return "Account expired";
        }
        if (e instanceof LockedException) {
            return "Account locked";
        }
        if (e instanceof MaximumNumberSimultaneousLoginsException) {
            return "Too many concurrent logins";
        }
        return "Bad credentials";
    }
}
