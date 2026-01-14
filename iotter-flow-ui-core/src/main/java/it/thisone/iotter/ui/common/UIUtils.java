package it.thisone.iotter.ui.common;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.persistence.EntityManager;
import javax.servlet.http.Cookie;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.vaadin.server.Page;
import com.vaadin.server.Page.Styles;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.Position;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.UI;
import com.wcs.wcslib.vaadin.widget.recaptcha.ReCaptcha;
import com.wcs.wcslib.vaadin.widget.recaptcha.shared.ReCaptchaOptions;

import eu.bitwalker.useragentutils.UserAgent;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.eventbus.EventBusWrapper;
import it.thisone.iotter.integration.CassandraService;
import it.thisone.iotter.integration.ServiceFactory;
import it.thisone.iotter.persistence.model.GeoLocation;
import it.thisone.iotter.persistence.model.MeasureUnitType;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.service.DatabaseMessageSource;
import it.thisone.iotter.security.EntityPermission;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.ifc.IUiFactory;
import it.thisone.iotter.ui.main.IMainUI;
import it.thisone.iotter.ui.main.UiConstants;
import it.thisone.iotter.util.EncryptUtils;

public final class UIUtils implements Serializable, UiConstants, Constants {
	// https://vaadin.com/book/-/page/layout.orderedlayout.html

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(UIUtils.class);

	/**
	 * @param username
	 * @param password
	 */
	public static UserDetailsAdapter authenticate(Object principal, Object credentials, boolean remember) {
		UsernamePasswordAuthenticationToken request = new UsernamePasswordAuthenticationToken(principal, credentials);
		Authentication result = ((IMainUI) UI.getCurrent()).getServiceFactory().getAuthManager().authenticate(request);
		UserDetailsAdapter user = (UserDetailsAdapter) result.getPrincipal();
		if (UIUtils.isCookiesEnabled()) {
			if (remember) {
				addCookie(USERNAME_COOKIE, principal.toString(), MAX_COOKIE_AGE);
				addCookie(PASSWORD_COOKIE, credentials.toString(), MAX_COOKIE_AGE);
			} else {
				if (getCookieByName(USERNAME_COOKIE) != null) {
					addCookie(USERNAME_COOKIE, principal.toString(), 0);
				}
				if (getCookieByName(PASSWORD_COOKIE) != null) {
					addCookie(PASSWORD_COOKIE, credentials.toString(), 0);
				}
			}
		}

		return user;
	}

	public static boolean isMobile() {
		return ((IMainUI) UI.getCurrent()).isMobile();
	}

	public static boolean isCookiesEnabled() {
		Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
		return (cookies != null);
	}

	public static String getCookieByName(String name) {
		// Fetch all cookies from the request
		Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
		if (cookies == null) {
			return null;
		}
		// Iterate to find cookie by its name
		for (Cookie cookie : cookies) {
			if (name.equals(cookie.getName())) {
				URLCodec codec = new URLCodec();
				try {
					// Bug #200 (In Progress): [VAADIN] remember me cookie is
					// not working after enabling vaadin push
					return codec.decode(cookie.getValue());
				} catch (DecoderException e) {
				}
			}
		}
		return null;
	}

	public static void addCookie(String name, String value, int age) {
		if (VaadinService.getCurrentResponse() != null) {
			Cookie cookie = new Cookie(name, value);
			// Make cookie expire in 30 days
			cookie.setMaxAge(age);
			cookie.setPath(VaadinService.getCurrentRequest().getContextPath());
			VaadinService.getCurrentResponse().addCookie(cookie);
		} else {
			// Bug #184 [Tomcat][Vaadin] push not working ( Push + WebSocket ) ≠
			// Cookies
			// Bug #200 (In Progress): [VAADIN] remember me cookie is not
			// working after enabling vaadin push
			URLCodec codec = new URLCodec();
			try {
				Page.getCurrent().getJavaScript()
						.execute(String.format("document.cookie = '%s=%s);';", name, codec.encode(value)));
			} catch (EncoderException e) {
				// Intentionally unhandled
			}
		}
	}

	public static Map<String, String> parseParameters(String event) {
		Map<String, String> parameters = new HashMap<String, String>();
		if (event != null) {
			String[] tokens = event.split(PARAM_SEPARATOR);
			for (String token : tokens) {
				if (token.contains("=")) {
					String[] params = token.split("=");
					parameters.put(params[0], params[1]);
				}
			}
		}
		return parameters;
	}

	public static String getResetPasswordURL(String url, String action, String user, String token) {
		StringBuilder sb = new StringBuilder();
		sb.append(url);
		sb.append("#!");
		sb.append(action);
		sb.append("/");
		sb.append(USERNAME_PARAM);
		sb.append("=");
		sb.append(EncryptUtils.urlEncode(user));
		sb.append(PARAM_SEPARATOR);
		sb.append(TOKEN_PARAM);
		sb.append("=");
		sb.append(token);

		return sb.toString();
	}

	public static String getResetPasswordURL(String action, String user, String token) {
		// return getResetPasswordURL(((IMainUI) UI.getCurrent()).getServerUrl(),
		// action, user, token);

		StringBuilder sb = new StringBuilder();
		sb.append("#!");
		sb.append(action);
		sb.append("/");
		sb.append(USERNAME_PARAM);
		sb.append("=");
		sb.append(EncryptUtils.urlEncode(user));
		sb.append(PARAM_SEPARATOR);
		sb.append(TOKEN_PARAM);
		sb.append("=");
		sb.append(token);

		return sb.toString();

	}

	public static String getDisplayURL(String action, String type, String mode, String id) {
		StringBuilder sb = new StringBuilder();
		sb.append(((IMainUI) UI.getCurrent()).getServerUrl());
		sb.append("#!");
		sb.append(action);
		sb.append("/");
		sb.append(VIEW_TYPE_PARAM);
		sb.append("=");
		sb.append(type);
		if (mode != null) {
			sb.append(PARAM_SEPARATOR);
			sb.append(VIEW_MODE_PARAM);
			sb.append("=");
			sb.append(mode);
		}

		sb.append(PARAM_SEPARATOR);
		sb.append(KEY_PARAM);
		sb.append("=");
		sb.append(id);

		return sb.toString();
	}

	
	public static String localize(String key, String defaultValue) {

		if (UI.getCurrent() == null) {
			return defaultValue;
		}
		return ((IMainUI) UI.getCurrent()).localize(key);
	}
	
	public static String localize(String key) {

		if (UI.getCurrent() == null) {
			return key;
		}
		return ((IMainUI) UI.getCurrent()).localize(key);
	}

	public static String localize(String code, Object[] args, String defaultMessage) {
		return ((IMainUI) UI.getCurrent()).localize(code, args, defaultMessage);
	}

	public static String messageBundle(String code) {
		return messageBundle(code, null, UI.getCurrent().getLocale());
	}

	public static String messageBundle(String code, String defaultMessage, Locale locale) {
		if (UI.getCurrent() == null) {
			return defaultMessage;
		}
		IMainUI ui = (IMainUI) UI.getCurrent();
		DatabaseMessageSource messageSource = ui.getDatabaseMessageSource();
		if (messageSource == null) {
			return defaultMessage != null ? defaultMessage : code;
		}
		return messageSource.getDatabaseMessage(code, defaultMessage, locale);
	}

	public static boolean hasRole(String role) {
		return ((IMainUI) UI.getCurrent()).hasRole(role);
	}

	public static boolean hasPermission(EntityPermission permission) {
		return ((IMainUI) UI.getCurrent()).hasPermission(permission);
	}

	public static UserDetailsAdapter getUserDetails() {
		UserDetailsAdapter details = ((IMainUI) UI.getCurrent()).getUserDetails();
		if (details == null) {
			User anonymous = new User();
			anonymous.setUsername("anonymous");
			anonymous.setOwner("anonymous");
			details = new UserDetailsAdapter(anonymous);
		}
		return details;
	}

	public static EntityManager getEntityManager() {
		return ((IMainUI) UI.getCurrent()).getEntityManager();
	}

	public static ServiceFactory getServiceFactory() {
		return ((IMainUI) UI.getCurrent()).getServiceFactory();
	}

	public static CassandraService getCassandraService() {
		return ((IMainUI) UI.getCurrent()).getCassandraService();
	}

	public static EventBusWrapper getUIEventBus() {
		return ((IMainUI) UI.getCurrent()).getUIEventBus();
	}

	public static void trace(TracingAction action, String message) {
		String username = getUserDetails().getUsername();
		String administrator = getUserDetails().getTenant();
		String network = getUserDetails().getNetwork();
		trace(action, username, administrator, network, null, message);
	}

	public static void trace(TracingAction action, String username, String administrator, String network, String device,
			String message) {
		logger.debug("{} {} {} {} {}", action, username, administrator, network, message);
		((IMainUI) UI.getCurrent()).getServiceFactory().getTracingService().trace(action, username, administrator,
				network, device, message);
	}

	@SuppressWarnings("serial")
	public static ReCaptcha createReCaptcha() {
		// https://developers.google.com/recaptcha/docs/faq

		/*
		 * 
		 * Site key: 6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI Secret key:
		 * 6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe
		 * 
		 */

		try {
			Properties properties = ((IMainUI) UI.getCurrent()).getAppProperties();
			String privateKey = properties.getProperty("recaptcha.privatekey");
			String publicKey = properties.getProperty("recaptcha.publickey");
			return new ReCaptcha(privateKey, new ReCaptchaOptions() {
				{
					theme = "clean";
					sitekey = publicKey;
				}
			});
		} catch (Exception e) {
			logger.error("unable to instantiate ReCaptcha", e);
		}
		return null;
	}

	public static Locale getLocale() {
		return UI.getCurrent().getLocale();
	}

	public static String googleMapApiKey() {
		if (((IMainUI) UI.getCurrent()).getServerName().equals("localhost")) {
			return "AIzaSyCeFtOnsiE1OMOy7gTTQ6Leoip4IXh4lX8";
		}
		String apiKey = "";
		try {
			Properties properties = ((IMainUI) UI.getCurrent()).getAppProperties();
			apiKey = properties.getProperty("googlemap.apikey");
		} catch (Exception e) {
			logger.error("unable to retrieve googlemap.apikey", e);
		}
		return apiKey;
	}

	public static String portalName() {
		String value = "";
		Properties properties = ((IMainUI) UI.getCurrent()).getAppProperties();
		if (properties != null) {
			value = properties.getProperty("portal_name");
		}
		return value;
	}

	public static String getUserTokenCookie() {
		return getCookieByName(USERTOKEN_COOKIE);
	}

	public static String toHtml(String input) {
		if (input == null) {
			return "";
		}
		input = input.replace("\n", "<br />\n");
		return input;
	}

	public static String addBackgroundImageStyle(long imageId, int imageWidth, int imageHeight) {
		return addBackgroundImageStyle("image/" + imageId, imageWidth, imageHeight);
	}

	public static String addBackgroundImageStyle(String imageUrl, int imageWidth, int imageHeight) {
		String backgroundStyleName = String.format("%s-%sx%s", imageUrl.replaceAll("/", "").toLowerCase(), imageWidth,
				imageHeight);
		String backgroundImageStyle = String.format(".v-app .v-absolutelayout-%s { background-image: url(%s); "
				+ "background-size: %spx %spx; background-repeat: no-repeat; " + "background-position: center;}",
				backgroundStyleName, imageUrl, imageWidth, imageHeight);
		Styles styles = Page.getCurrent().getStyles();
		styles.add(backgroundImageStyle);
		return backgroundStyleName;
	}

	/**
	 * creates dinamically css style for label with color and font size
	 * 
	 * @param id
	 * @param color
	 * @param fontSize px
	 * @return
	 */
	public static String addLabelStyle(String id, String color, String fontSize, String extraStyle) {
		String colorStyle = "";
		String fontStyle = "";

		if (extraStyle == null)
			extraStyle = "";

		if (fontSize != null) {
			fontStyle = String.format("font-size: %s;", fontSize);
		} else {
			fontStyle = "";
		}

		if (color != null && !color.isEmpty()) {
			colorStyle = String.format("color:%s ;", color);
		}

		String styleName = String.format("%s%s", id, fontSize);
		String labelStyle = String.format(".v-app .v-label-%s { %s %s %s "
				// + "line-height: 1; letter-spacing: -0.05em; "
				// + "font-weight: 300; -webkit-font-smoothing: antialiased; "
				+ " } ", styleName, colorStyle, fontStyle, extraStyle);
		Styles styles = Page.getCurrent().getStyles();

		styles.add(labelStyle);
		return styleName;
	}

	/**
	 * Vaadin 7 will support Internet Explorer 8 and newer. Vaadin 7 will support
	 * the latest Firefox, Chrome, Safari and Opera major version available at the
	 * time of release. From that point on all new major versions of the browsers
	 * will additionally be supported.
	 * 
	 * 
	 * Supported Web Browsers Google Chrome 23 or newer Safari 6 or newer Internet
	 * Explorer 8 or newer Mozilla Firefox 17 or newer Opera 15 or newer
	 * 
	 * @param ua
	 * @return
	 */
	public static UAgent checkSupportedBrowser(String header) {
		int supportedVersion = 0;
		int majorVersion = -1;

		UserAgent userAgent = UserAgent.parseUserAgentString(header);
		UAgent agent = new UAgent(header);
		agent.setMobile(false);
		agent.setFamily("Unknown");
		agent.setVersion(String.valueOf(supportedVersion));

		try {
			if (userAgent.getBrowser() != null) {
				agent.setFamily(userAgent.getBrowser().getGroup().getName());
				switch (userAgent.getBrowser().getBrowserType()) {
				case MOBILE_BROWSER:
					agent.setMobile(true);
					break;
				default:
					agent.setMobile(false);
					break;
				}
			}

			if (userAgent.getBrowserVersion() != null) {
				agent.setVersion(userAgent.getBrowserVersion().getMajorVersion());
			}

			switch (userAgent.getBrowser().getGroup()) {
			case OPERA:
			case OPERA_MINI:
			case OPERA_MOBILE:
				// October 2014, 25
				supportedVersion = 15;
				break;
			case IE:
				supportedVersion = 8;
				break;
			case EDGE:
			case EDGE_MOBILE:
				// return false;
				supportedVersion = 99999;
				break;
			case SAFARI:
				// October 2014, 8
				supportedVersion = 6;
				break;
			case CHROME:
			case CHROME_MOBILE:
				// October 2014, 38
				supportedVersion = 23;
				break;
			case FIREFOX:
				// October 2014, 36
				supportedVersion = 17;
				break;
			default:
				// return false;
				supportedVersion = 9999;
			}

			majorVersion = Integer.parseInt(agent.getVersion());
		} catch (Exception e) {
			logger.error(header, e);
		}

		boolean supported = (majorVersion >= supportedVersion);

		if (!supported) {
			String msg = "This application is not optimized for your browser:<br/>"
					// + agent.getFamily() + " " //
					// + agent.getVersion() //
					// + "<br/>" //
					+ "If you wish, you can load it anyway." //
					+ "<p>Supported Web Browsers<p/>" //
					+ "Google Chrome 23 or newer</br>" //
					+ "Safari 6 or newer</br>" //
					+ "Internet Explorer 8 or newer</br>" //
					+ "Mozilla Firefox 17 or newer</br>" //
					+ "Opera 15 or newer</br>"; //

			Notification n = new Notification("WARNING", msg);
			n.setHtmlContentAllowed(true);
			n.setDelayMsec(-1);
			n.setPosition(Position.TOP_CENTER);
			n.setStyleName("system closable");
			n.show(UI.getCurrent().getPage());
		}
		// agent.setMobile(true);

		return agent;
	}

	public static String getActivationURL(String action, String user, String token) {
		return getActivationURL(((IMainUI) UI.getCurrent()).getServerUrl(), action, user, token);
	}

	public static String getActivationURL(String url, String action, String user, String token) {
		StringBuilder sb = new StringBuilder();
		sb.append(url);
		sb.append("#!");
		sb.append(action);
		sb.append("/");
		sb.append(USERNAME_PARAM);
		sb.append("=");
		sb.append(EncryptUtils.urlEncode(user));
		sb.append(PARAM_SEPARATOR);
		sb.append(TOKEN_PARAM);
		sb.append("=");
		sb.append(token);
		return sb.toString();
	}

	public static String getAutoLoginURL(String action, String user, String token) {
		StringBuilder sb = new StringBuilder();
		sb.append(((IMainUI) UI.getCurrent()).getServerUrl());
		sb.append("#!");
		sb.append("login");
		sb.append("/");
		sb.append(USERNAME_PARAM);
		sb.append("=");
		sb.append(EncryptUtils.urlEncode(user));
		sb.append(PARAM_SEPARATOR);
		sb.append(TOKEN_PARAM);
		sb.append("=");
		sb.append(token);
		sb.append(PARAM_SEPARATOR);
		sb.append(ACTION_PARAM);
		sb.append("=");
		sb.append(action);
		return sb.toString();
	}

	public static Properties getAppProperties() {
		Properties properties = ((IMainUI) UI.getCurrent()).getAppProperties();
		return properties;
	}

	public static TimeZone getBrowserTimeZone() {
		return ((IMainUI) UI.getCurrent()).getTimeZone();
	}

	public static GeoLocation getGeoLocation() {
		return ((IMainUI) UI.getCurrent()).getGeoLocation();
	}

	public static IUiFactory getUiFactory() {
		return ((IMainUI) UI.getCurrent()).getUiFactory();
	}

	public static void startWidgetRefresher() {
		((IMainUI) UI.getCurrent()).startWidgetRefresher();
	}

	public static void checkSupportedBrowser() {
		WebBrowser browser = Page.getCurrent().getWebBrowser();
		if (browser.isTooOldToFunctionProperly()) {
			String msg = "This application is not optimized for your browser !<br/>"
					+ "If you wish, you can load it anyway." //
					+ "<p>Supported Web Browsers<p/>" //
					+ "Google Chrome 23 or newer</br>" //
					+ "Safari 6 or newer</br>" //
					+ "Internet Explorer 8 or newer</br>" //
					+ "Mozilla Firefox 17 or newer</br>" //
					+ "Opera 15 or newer</br>"; //
			Notification n = new Notification("WARNING", msg);
			n.setHtmlContentAllowed(true);
			n.setDelayMsec(-1);
			n.setPosition(Position.TOP_CENTER);
			n.setStyleName("system closable");
			n.show(UI.getCurrent().getPage());
		}

	}

	public static String getMeasureUnitPattern() {
		List<MeasureUnitType> units = getServiceFactory().getMeasureUnitTypeService().findAll();
		Collections.sort(units, new Comparator<MeasureUnitType>() {
			@Override
			public int compare(MeasureUnitType o1, MeasureUnitType o2) {
				return o1.getCode().compareTo(o2.getCode());
			}
		});
		List<String> values = new ArrayList<>();
		for (MeasureUnitType unit : units) {
			// '<' || ch == '#' || ch == '\u2264'
			String name = unit.getName();
			name = name.replaceAll("<", "");
			name = name.replaceAll("#", "");
			name = name.replaceAll("\u2264", "");
			values.add(String.format("%d#%s", unit.getCode(), name));
		}
		return StringUtils.join(values, "|");
	}

	public static String getAppVersion() {
		String sep = "<br/>";
		StringBuilder sb = new StringBuilder();
		sb.append("title:");
		sb.append(getAppProperties().getProperty(PropertyName.IMPLEMENTATION_TITLE, "iotTER"));
		sb.append(sep);
		sb.append("version:");
		sb.append(getAppProperties().getProperty(PropertyName.IMPLEMENTATION_VERSION, "1.0"));
		sb.append(sep);
		sb.append("branch:");
		sb.append(getAppProperties().getProperty(PropertyName.BUILD_SCM_BRANCH, ""));
		sb.append(sep);
		sb.append("build number:");
		sb.append(getAppProperties().getProperty(PropertyName.BUILD_NUMBER, ""));
		sb.append(sep);
		sb.append("build date:");
		sb.append(getAppProperties().getProperty(PropertyName.BUILD_DATE_TIME, ""));
		return sb.toString();
	}

	public static void push() {
		try {
			if (UI.getCurrent().getPushConfiguration().getPushMode().equals(PushMode.MANUAL)) {
				UI.getCurrent().push();
			}
		} finally {
		}
	}

	/**
	 * Utility method to convert java.util.Date to LocalDate using the configured timezone
	 * @param date the Date to convert
	 * @return LocalDate representation, or null if date is null
	 */
	public static LocalDate toLocalDate(Date date, TimeZone timeZone) {
		if (date == null) {
			return null;
		}
		return date.toInstant().atZone(timeZone.toZoneId()).toLocalDate();
	}

	/**
	 * Utility method to convert LocalDate to java.util.Date using the configured timezone
	 * @param localDate the LocalDate to convert
	 * @return Date representation, or null if localDate is null
	 */
	public static Date toDate(LocalDate localDate, TimeZone timeZone) {
		if (localDate == null) {
			return null;
		}
		return Date.from(localDate.atStartOfDay(timeZone.toZoneId()).toInstant());
	}

	/**
	 * Utility method to convert java.util.Date to LocalDateTime using the configured timezone
	 * @param date the Date to convert
	 * @param timeZone the timezone to use for conversion
	 * @return LocalDateTime representation, or null if date is null
	 */
	public static LocalDateTime toLocalDateTime(Date date, TimeZone timeZone) {
		if (date == null) {
			return null;
		}
		return date.toInstant().atZone(timeZone.toZoneId()).toLocalDateTime();
	}

	/**
	 * Utility method to convert LocalDateTime to java.util.Date using the configured timezone
	 * @param localDateTime the LocalDateTime to convert
	 * @param timeZone the timezone to use for conversion
	 * @return Date representation, or null if localDateTime is null
	 */
	public static Date toDate(LocalDateTime localDateTime, TimeZone timeZone) {
		if (localDateTime == null) {
			return null;
		}
		return Date.from(localDateTime.atZone(timeZone.toZoneId()).toInstant());
	}
}
