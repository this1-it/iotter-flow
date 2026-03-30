package it.thisone.iotter.ui.anonymous;

import java.util.Properties;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Qualifier;

import org.vaadin.firitin.form.AbstractForm;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.router.Route;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.integration.NotificationService;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.model.UserToken;
import it.thisone.iotter.persistence.service.UserService;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.authentication.LoginScreen;
import it.thisone.iotter.ui.common.BaseView;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.main.MainView;
import it.thisone.iotter.ui.validators.AntiReDoSEmailValidator;
import it.thisone.iotter.util.PopupNotification;

@SuppressWarnings("serial")
@Route(ForgotPasswordView.NAME)
public class ForgotPasswordView extends BaseView {

	public static final String NAME = "forgotpassword";

	private final UserService userService;
	private final NotificationService notificationService;
	private final Properties appProperties;
	private final Component captcha;

	private ForgotPasswordForm form;

	public ForgotPasswordView(UserService userService, NotificationService notificationService,
			@Qualifier("appProperties") Properties appProperties) {
		this.userService = userService;
		this.notificationService = notificationService;
		this.appProperties = appProperties;
		addClassName("forgotpassword-view");
		addClassName("anonymous-auth-view");
		// TODO(flow-migration): restore Flow-compatible ReCaptcha component and validation.
		// this.captcha = UIUtils.createReCaptcha();
		this.captcha = null;
		buildLayout();
	}

	@Override
	public String getI18nKey() {
		return NAME;
	}

	private void buildLayout() {
		form = new ForgotPasswordForm(captcha);
		form.setSubmitHandler(this::handleConfirm);
		form.setCancelHandler(event -> getUI().ifPresent(ui -> ui.navigate(LoginScreen.class)));
		add(AnonymousAuthLayout.singleColumn(getPortalName(), getI18nLabel("title"),
				"Enter your username to receive password recovery instructions.", form));

		// TODO(flow-migration): Responsive.makeResponsive(this) has no Flow equivalent here.
	}

	private void handleConfirm(ForgotPasswordData data) {
		// TODO(flow-migration): restore captcha validation after migrating the component.
		// if (captcha != null && captcha.isVisible() && !captcha.validate()) {
		// 	PopupNotification.show(getI18nLabel("invalid_captcha"), PopupNotification.Type.ERROR);
		// 	captcha.reload();
		// 	return;
		// }

		String login = data.getUsername();
		if (login == null || login.isEmpty()) {
			return;
		}

		User user = userService.findByName(login);
		if (user == null) {
			PopupNotification.show(getI18nLabel("invalid_username"), PopupNotification.Type.ERROR);
			return;
		}

		UserDetailsAdapter details = new UserDetailsAdapter(user);
		if (!details.isEnabled()) {
			PopupNotification.show(getI18nLabel("user_not_enabled"), PopupNotification.Type.ERROR);
			return;
		}
		if (!details.hasRole(Constants.ROLE_ADMINISTRATOR)) {
			PopupNotification.show(getI18nLabel("user_not_administrator"), PopupNotification.Type.ERROR);
			return;
		}

		if (isValidEmail(user.getEmail())) {
			sendPasswordResetToken(details);
			getUI().ifPresent(ui -> ui.navigate(MainView.class));
			return;
		}

		form.setUsernameReadOnly(true);
		form.setEmailVisible(true);
		if (isValidEmail(data.getEmail())) {
			user.setEmail(data.getEmail());
			userService.update(user);
			sendPasswordResetToken(new UserDetailsAdapter(user));
			getUI().ifPresent(ui -> ui.navigate(MainView.class));
			return;
		}

		PopupNotification.show(getI18nLabel("email_invalid"), PopupNotification.Type.ERROR);
	}

	private boolean isValidEmail(String value) {
		if (value == null || value.isEmpty()) {
			return false;
		}
		AntiReDoSEmailValidator validator = new AntiReDoSEmailValidator(getI18nLabel("email_invalid"));
		ValidationResult result = validator.apply(value, new ValueContext());
		return !result.isError();
	}

	private void sendPasswordResetToken(UserDetailsAdapter details) {
		String email = details.getEmail();
		String fullname = details.getName();
		String login = details.getUsername();
		UserToken token = userService.createUserToken(login, ResetPasswordView.NAME, null, Constants.USER_TOKEN_HOURS);
		String url = buildResetPasswordUrl(token.getOwner(), token.getToken());
		notificationService.resetPassword(email, null, fullname, login, url, null);
	}

	private String buildResetPasswordUrl(String user, String token) {
		// TODO(flow-migration): restore absolute URL generation if mail templates require it.
		return ResetPasswordView.NAME + "?" + UIUtils.USERNAME_PARAM + "=" + user + "&" + UIUtils.TOKEN_PARAM + "="
				+ token;
	}

	private String getPortalName() {
		return appProperties.getProperty("portal_name", "Iotter Flow");
	}

	public static class ForgotPasswordData {
		private String username;
		private String email;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}
	}

	private static class ForgotPasswordForm extends AbstractForm<ForgotPasswordData> {
		private static final long serialVersionUID = 1L;

		private final TextField username;
		private final TextField email;
		private final Button cancelButton;
		private final AntiReDoSEmailValidator validator;
		private final Component captcha;

		private Consumer<ForgotPasswordData> submitHandler;

		ForgotPasswordForm(Component captcha) {
			super(ForgotPasswordData.class);
			this.captcha = captcha;
			this.validator = new AntiReDoSEmailValidator("A valid EMAIL address is required");

			username = new TextField(getI18nLabel("username"));
			username.setRequiredIndicatorVisible(true);
			username.setWidth(20.0f, Unit.EM);
			username.focus();

			email = new TextField("Email");
			email.setWidth(20.0f, Unit.EM);
			email.setRequiredIndicatorVisible(false);
			email.setVisible(false);

			cancelButton = new Button(getI18nLabel("cancel"));
			// TODO(flow-migration): restore legacy button theme mapping if still needed.
			// cancelButton.addClassName(UIUtils.BUTTON_DEFAULT_STYLE);

			setEntity(new ForgotPasswordData());
			setupBinder();
			setupHandlers();
		}

		private void setupBinder() {
			getBinder().forField(username).asRequired(getI18nLabel("username_required"))
					.bind(ForgotPasswordData::getUsername, ForgotPasswordData::setUsername);

			getBinder().forField(email).withValidator((value, context) -> {
				if (!email.isVisible()) {
					return ValidationResult.ok();
				}
				return validator.apply(value, context);
			}).bind(ForgotPasswordData::getEmail, ForgotPasswordData::setEmail);
		}

		private void setupHandlers() {
			setSavedHandler(entity -> {
				if (submitHandler != null) {
					submitHandler.accept(entity);
				}
			});
		}

		void setSubmitHandler(Consumer<ForgotPasswordData> submitHandler) {
			this.submitHandler = submitHandler;
		}

		void setCancelHandler(com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>> listener) {
			cancelButton.addClickListener(listener);
		}

		void setEmailVisible(boolean visible) {
			email.setVisible(visible);
			email.setRequiredIndicatorVisible(visible);
			if (visible) {
				email.focus();
			}
		}

		boolean isEmailVisible() {
			return email.isVisible();
		}

		void setUsernameReadOnly(boolean readOnly) {
			username.setReadOnly(readOnly);
		}

		@Override
		protected Component createContent() {
			// TODO(flow-migration): restore legacy primary button theme if needed.
			// getSaveButton().addClassName(UIUtils.BUTTON_DEFAULT_STYLE);
			getSaveButton().setText(getI18nLabel("confirm"));
			getSaveButton().setId("confirm");

			getResetButton().setVisible(false);
			getDeleteButton().setVisible(false);

			VerticalLayout mainLayout = new VerticalLayout();
			mainLayout.setSpacing(true);
			mainLayout.setPadding(true);
			mainLayout.addClassName("fields");

			VerticalLayout fieldsLayout = new VerticalLayout();
			fieldsLayout.setSpacing(true);
			fieldsLayout.add(username, email);
			mainLayout.add(fieldsLayout);

			if (captcha != null) {
				mainLayout.add(captcha);
			}

			HorizontalLayout buttonLayout = new HorizontalLayout();
			buttonLayout.setSpacing(true);
			buttonLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
			buttonLayout.add(getSaveButton(), cancelButton);
			mainLayout.add(buttonLayout);

			return mainLayout;
		}

		private String getI18nLabel(String key) {
			return getTranslation("forgotpassword." + key);
		}
	}
}
