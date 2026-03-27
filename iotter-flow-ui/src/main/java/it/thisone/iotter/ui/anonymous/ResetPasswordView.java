package it.thisone.iotter.ui.anonymous;

import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.vaadin.firitin.form.AbstractForm;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.integration.AuthManager;
import it.thisone.iotter.integration.NotificationService;
import it.thisone.iotter.persistence.service.UserService;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.authentication.LoginScreen;
import it.thisone.iotter.ui.common.AuthenticatedUser;
import it.thisone.iotter.ui.common.BaseView;
import it.thisone.iotter.ui.main.MainView;
import it.thisone.iotter.util.PopupNotification;

@SuppressWarnings("serial")
@Route(ResetPasswordView.NAME)
public class ResetPasswordView extends BaseView implements BeforeEnterObserver {

	public static final String NAME = "resetpassword";

	private final UserService userService;
	private final NotificationService notificationService;
	private final AuthManager authManager;
	private final Properties appProperties;

	private ResetPasswordForm form;
	private String username;

	public ResetPasswordView(UserService userService, NotificationService notificationService, AuthManager authManager,
			@Qualifier("appProperties") Properties appProperties) {
		this.userService = userService;
		this.notificationService = notificationService;
		this.authManager = authManager;
		this.appProperties = appProperties;
		addClassName("resetpassword-view");
		addClassName("anonymous-auth-view");
	}

	private void buildLayout(String user) {
		removeAll();
		this.username = user;
		if (user == null) {
			Span unauthorized = new Span(getI18nLabel("unathorized"));
			unauthorized.addClassName("h1");
			add(unauthorized);
			setHorizontalComponentAlignment(Alignment.CENTER, unauthorized);
			return;
		}

		form = new ResetPasswordForm();
		form.setSubmitHandler(this::handleConfirm);
		form.setCancelHandler(event -> getUI().ifPresent(ui -> ui.navigate(LoginScreen.class)));
		form.setUsername(user);
		add(AnonymousAuthLayout.singleColumn(appProperties.getProperty("portal_name", "Iotter"), getI18nLabel("title"),
				"Set a new password for your account.", form));

		// TODO(flow-migration): Responsive.makeResponsive(this) has no direct Flow equivalent here.
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		List<String> users = event.getLocation().getQueryParameters().getParameters().get(AuthenticatedUserKey.USER);
		List<String> tokens = event.getLocation().getQueryParameters().getParameters().get(AuthenticatedUserKey.TOKEN);
		String user = users == null || users.isEmpty() ? null : users.get(0);
		String token = tokens == null || tokens.isEmpty() ? null : tokens.get(0);

		boolean validToken = user != null && token != null && userService.validateToken(user, NAME, token);
		if (validToken) {
			buildLayout(user);
			return;
		}

		PopupNotification.show(getI18nLabel("invalid_username"), PopupNotification.Type.ERROR);
		event.rerouteTo(LoginScreen.class);
	}

	private void handleConfirm(ResetPasswordData data) {
		if (username == null || data.getNewPassword() == null) {
			return;
		}
		try {
			userService.changePassword(username, data.getNewPassword());
			Authentication authentication = authManager.authenticate(
					new UsernamePasswordAuthenticationToken(username, data.getNewPassword()));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			VaadinSession.getCurrent().setAttribute(AuthenticatedUser.SESSION_AUTHENTICATION_KEY, authentication);

			UserDetailsAdapter details = (UserDetailsAdapter) authentication.getPrincipal();
			notificationService.successResetPassword(details.getEmail(), null, details.getName());
			userService.deleteToken(username, NAME);

			UI.getCurrent().navigate(MainView.class);
		} catch (Exception e) {
			String error = "Please add missing information before submitting";
			if (e.getCause() != null && e.getCause().getMessage() != null) {
				error = e.getCause().getMessage();
			} else if (e.getMessage() != null) {
				error = e.getMessage();
			}
			PopupNotification.show(error, PopupNotification.Type.ERROR);
		}
	}

	@Override
	public String getI18nKey() {
		return NAME;
	}

	public static class ResetPasswordData {
		private String newPassword;
		private String verifiedPassword;

		public String getNewPassword() {
			return newPassword;
		}

		public void setNewPassword(String newPassword) {
			this.newPassword = newPassword;
		}

		public String getVerifiedPassword() {
			return verifiedPassword;
		}

		public void setVerifiedPassword(String verifiedPassword) {
			this.verifiedPassword = verifiedPassword;
		}
	}

	private static class ResetPasswordForm extends AbstractForm<ResetPasswordData> {
		private static final long serialVersionUID = 1L;

		private final PasswordField newPassword;
		private final PasswordField verifiedPassword;
		private final Button cancelButton;

		private Consumer<ResetPasswordData> submitHandler;
		private String username;

		ResetPasswordForm() {
			super(ResetPasswordData.class);

			newPassword = new PasswordField(getI18nLabel("new_password"));
			newPassword.setRequiredIndicatorVisible(true);
			newPassword.setWidth(20.0f, Unit.EM);

			verifiedPassword = new PasswordField(getI18nLabel("verified_password"));
			verifiedPassword.setRequiredIndicatorVisible(true);
			verifiedPassword.setWidth(20.0f, Unit.EM);

			cancelButton = new Button(getI18nLabel("cancel"));
			// TODO(flow-migration): restore legacy button theme mapping if needed.
			// cancelButton.addClassName(UIUtils.BUTTON_DEFAULT_STYLE);

			setEntity(new ResetPasswordData());
			setupBinder();
			setupHandlers();
		}

		private void setupBinder() {
			getBinder().forField(newPassword).asRequired(getI18nLabel("new_password")).withValidator(this::validatePassword)
					.bind(ResetPasswordData::getNewPassword, ResetPasswordData::setNewPassword);

			getBinder().forField(verifiedPassword).asRequired(getI18nLabel("verified_password"))
					.withValidator(this::validatePasswordMatch)
					.bind(ResetPasswordData::getVerifiedPassword, ResetPasswordData::setVerifiedPassword);

			getBinder().addStatusChangeListener(event -> getSaveButton().setEnabled(event.getBinder().isValid()));
		}

		private ValidationResult validatePassword(String value, ValueContext context) {
			if (value == null || value.isEmpty()) {
				return ValidationResult.ok();
			}
			if (value.length() < Constants.Validators.MIN_PASSWORD_LENGTH) {
				return ValidationResult.error(getTranslation("validators.too-short-password"));
			}
			if (username != null && username.equals(value)) {
				return ValidationResult.error(getTranslation("validators.username-equals-password"));
			}
			return ValidationResult.ok();
		}

		private ValidationResult validatePasswordMatch(String value, ValueContext context) {
			String original = newPassword.getValue();
			if (value == null || original == null) {
				return ValidationResult.ok();
			}
			if (!value.equals(original)) {
				return ValidationResult.error(getTranslation("validators.message-passwords-do-not-match"));
			}
			return ValidationResult.ok();
		}

		private void setupHandlers() {
			setSavedHandler(entity -> {
				if (submitHandler != null) {
					submitHandler.accept(entity);
				}
			});
		}

		void setSubmitHandler(Consumer<ResetPasswordData> submitHandler) {
			this.submitHandler = submitHandler;
		}

		void setCancelHandler(ComponentEventListener<ClickEvent<Button>> listener) {
			cancelButton.addClickListener(listener);
		}

		void setUsername(String username) {
			this.username = username;
		}

		@Override
		protected Component createContent() {
			// TODO(flow-migration): restore legacy primary button theme if needed.
			// getSaveButton().addClassName(UIUtils.BUTTON_DEFAULT_STYLE);
			getSaveButton().setText(getI18nLabel("confirm"));
			getSaveButton().setId("confirm");
			getSaveButton().setEnabled(false);

			getResetButton().setVisible(false);
			getDeleteButton().setVisible(false);

			VerticalLayout mainLayout = new VerticalLayout();
			mainLayout.setSpacing(true);
			mainLayout.setPadding(true);
			mainLayout.addClassName("fields");
			mainLayout.add(newPassword, verifiedPassword);

			HorizontalLayout buttonLayout = new HorizontalLayout();
			buttonLayout.setSpacing(true);
			buttonLayout.setPadding(true);
			buttonLayout.add(getSaveButton(), cancelButton);
			mainLayout.add(buttonLayout);

			return mainLayout;
		}

		private String getI18nLabel(String key) {
			return getTranslation("resetpassword." + key);
		}
	}

	private static final class AuthenticatedUserKey {
		private static final String USER = "user";
		private static final String TOKEN = "token";

		private AuthenticatedUserKey() {
		}
	}
}
