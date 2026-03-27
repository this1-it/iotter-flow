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
import com.vaadin.flow.component.cookieconsent.CookieConsent;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import it.thisone.iotter.integration.AuthManager;
import it.thisone.iotter.security.MaximumNumberSimultaneousLoginsException;
import it.thisone.iotter.ui.anonymous.ForgotPasswordView;
import it.thisone.iotter.ui.common.AuthenticatedUser;
import it.thisone.iotter.ui.main.MainView;
import it.thisone.iotter.ui.signup.SignUpView;

/**
 * UI content when the user is not logged in yet.
 */
@Route("login")
@CssImport("./styles/shared-styles.css")
public class LoginScreen extends FlexLayout implements HasDynamicTitle {

    @Override
    public String getPageTitle() {
        return getTranslation("view.login");
    }

    private final AuthenticationManager authManager;

    @Autowired
    public LoginScreen(AuthManager authManager) {
        this.authManager = authManager;
        buildUI();
    }

    private void buildUI() {
        setSizeFull();
        setClassName("login-screen");

        LoginForm loginForm = new LoginForm();
        loginForm.addLoginListener(this::login);
        loginForm.addForgotPasswordListener(
                event -> UI.getCurrent().navigate(ForgotPasswordView.class));
        loginForm.addClassName("auth-login-form");

        Button signUpButton = new Button(getTranslation("login.register"),
                event -> UI.getCurrent().navigate(SignUpView.class));
        signUpButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        signUpButton.addClassName("auth-secondary-action");

        Button forgotPasswordButton = new Button(getTranslation("landing.forgotpassword"),
                event -> UI.getCurrent().navigate(ForgotPasswordView.class));
        forgotPasswordButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        forgotPasswordButton.addClassName("auth-inline-link");

        HorizontalLayout recoveryLinks = new HorizontalLayout(forgotPasswordButton);
        recoveryLinks.addClassName("auth-link-row");
        recoveryLinks.setSpacing(true);

        VerticalLayout authActions = new VerticalLayout();
        authActions.addClassName("login-panel");
        authActions.setSpacing(false);
        authActions.setPadding(false);

        Span eyebrow = new Span("Iotter");
        eyebrow.addClassName("auth-card-eyebrow");

        H1 title = new H1(getTranslation("view.login"));
        title.addClassName("auth-card-title");

        Span subtitle = new Span("Monitor devices, manage assets, and access your workspace.");
        subtitle.addClassName("auth-card-subtitle");

        HorizontalLayout divider = new HorizontalLayout();
        divider.addClassName("auth-card-divider");

        authActions.add(eyebrow, title, subtitle, divider, loginForm, recoveryLinks, signUpButton);
        authActions.setHorizontalComponentAlignment(Alignment.STRETCH, loginForm);
        authActions.setHorizontalComponentAlignment(Alignment.CENTER, recoveryLinks);
        authActions.setHorizontalComponentAlignment(Alignment.CENTER, signUpButton);

        Div authStage = new Div(authActions);
        authStage.addClassName("auth-stage");

        Component loginInformation = buildLoginInformation();

        Div authShell = new Div(loginInformation, authStage);
        authShell.addClassName("auth-shell");

        add(authShell);
        add(new CookieConsent());
    }

    private Component buildLoginInformation() {
        VerticalLayout loginInformation = new VerticalLayout();
        loginInformation.setClassName("login-information");
        loginInformation.setSpacing(false);
        loginInformation.setPadding(false);

        Image logo = new Image("icons/icon.png", "Iotter");
        logo.setWidth("72px");
        logo.setHeight("72px");
        logo.addClassName("auth-brand-logo");

        Span badge = new Span("Cloud Platform");
        badge.addClassName("auth-brand-badge");

        H1 loginInfoHeader = new H1("Manage your IoT operations from one control center.");
        loginInfoHeader.addClassName("auth-brand-title");

        Span loginInfoText = new Span(
                "Track devices, visualize telemetry, and keep users aligned with a single operational view.");
        loginInfoText.addClassName("auth-brand-copy");

        VerticalLayout highlights = new VerticalLayout();
        highlights.addClassName("auth-brand-highlights");
        highlights.setSpacing(false);
        highlights.setPadding(false);
        highlights.add(feature("Real-time telemetry and device status"));
        highlights.add(feature("Operational dashboards and provisioning tools"));
        highlights.add(feature("Secure tenant and user access management"));

        loginInformation.add(logo, badge, loginInfoHeader, loginInfoText, highlights);
        return loginInformation;
    }

    private Span feature(String text) {
        Span feature = new Span(text);
        feature.addClassName("auth-brand-feature");
        return feature;
    }

    private void login(LoginForm.LoginEvent event) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            event.getUsername(), event.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            VaadinSession.getCurrent()
                    .setAttribute(AuthenticatedUser.SESSION_AUTHENTICATION_KEY, authentication);
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
            return getTranslation("login.account_expired");
        }
        if (e instanceof LockedException) {
            return getTranslation("login.account_locked");
        }
        if (e instanceof MaximumNumberSimultaneousLoginsException) {
            return getTranslation("login.too_many_logins");
        }
        return getTranslation("login.bad_credentials");
    }
}
