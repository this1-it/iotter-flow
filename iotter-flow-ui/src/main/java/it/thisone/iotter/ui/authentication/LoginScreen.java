package it.thisone.iotter.ui.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.cookieconsent.CookieConsent;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import it.thisone.iotter.integration.AuthManager;
import it.thisone.iotter.security.MaximumNumberSimultaneousLoginsException;
import it.thisone.iotter.ui.anonymous.AnonymousAuthLayout;
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
        loginForm.setForgotPasswordButtonVisible(false);
        loginForm.addClassName("auth-login-form");

        Span accountText = new Span(getTranslation("login.no_account"));
        accountText.addClassName("auth-bottom-text");

        Button signUpLink = new Button(getTranslation("login.register"),
                event -> UI.getCurrent().navigate(SignUpView.class));
        signUpLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        signUpLink.addClassName("auth-bottom-link");

        HorizontalLayout leftLinks = new HorizontalLayout(accountText, signUpLink);
        leftLinks.setAlignItems(Alignment.CENTER);
        leftLinks.setSpacing(true);

        Button forgotPasswordLink = new Button(getTranslation("landing.forgotpassword"),
                event -> UI.getCurrent().navigate(ForgotPasswordView.class));
        forgotPasswordLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        forgotPasswordLink.addClassName("auth-bottom-link");

        HorizontalLayout bottomRow = new HorizontalLayout(leftLinks, forgotPasswordLink);
        bottomRow.setWidthFull();
        bottomRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        bottomRow.setAlignItems(Alignment.CENTER);
        bottomRow.addClassName("auth-bottom-row");

        VerticalLayout formContent = new VerticalLayout();
        formContent.setSpacing(false);
        formContent.setPadding(false);
        formContent.setHorizontalComponentAlignment(Alignment.STRETCH, loginForm);
        formContent.add(loginForm, bottomRow);

        add(AnonymousAuthLayout.singleColumn("Iotter", getTranslation("view.login"),
                "Monitor devices, manage assets, and access your workspace.", formContent));
        add(new CookieConsent());
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
