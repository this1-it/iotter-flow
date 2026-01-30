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

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;


import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinRequest;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.UI;

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
		throw new UnsupportedOperationException("vaadin8 legacy");
		// UsernamePasswordAuthenticationToken request = new UsernamePasswordAuthenticationToken(principal, credentials);
		// Authentication result = ((IMainUI) UI.getCurrent()).getServiceFactory().getAuthManager().authenticate(request);
		// UserDetailsAdapter user = (UserDetailsAdapter) result.getPrincipal();

		// return user;
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
throw new UnsupportedOperationException("vaadin8 legacy");

		// StringBuilder sb = new StringBuilder();
		// sb.append(((IMainUI) UI.getCurrent()).getServerUrl());
		// sb.append("#!");
		// sb.append(action);
		// sb.append("/");
		// sb.append(VIEW_TYPE_PARAM);
		// sb.append("=");
		// sb.append(type);
		// if (mode != null) {
		// 	sb.append(PARAM_SEPARATOR);
		// 	sb.append(VIEW_MODE_PARAM);
		// 	sb.append("=");
		// 	sb.append(mode);
		// }

		// sb.append(PARAM_SEPARATOR);
		// sb.append(KEY_PARAM);
		// sb.append("=");
		// sb.append(id);

		// return sb.toString();
	}

	@Deprecated
	public static String localize(String key, String defaultValue) {
throw new UnsupportedOperationException("vaadin8 legacy");
		// if (UI.getCurrent() == null) {
		// 	return defaultValue;
		// }
		// return ((IMainUI) UI.getCurrent()).localize(key);
	}
	
	@Deprecated
	public static String localize(String key) {
		throw new UnsupportedOperationException("vaadin8 legacy");
		//return key;
		// if (UI.getCurrent() == null) {
		// 	return key;
		// }
		// return ((IMainUI) UI.getCurrent()).localize(key);
	}

	@Deprecated
	public static String localize(String code, Object[] args, String defaultMessage) {
		// return ((IMainUI) UI.getCurrent()).localize(code, args, defaultMessage);
		throw new UnsupportedOperationException("vaadin8 legacy");
	}

	@Deprecated
	public static String messageBundle(String code) {
		//return messageBundle(code, null, UI.getCurrent().getLocale());
		throw new UnsupportedOperationException("vaadin8 legacy");
	}

	@Deprecated
	public static String messageBundle(String code, String defaultMessage, Locale locale) {
		throw new UnsupportedOperationException("vaadin8 legacy");
		// if (UI.getCurrent() == null) {
		// 	return defaultMessage;
		// }
		// IMainUI ui = (IMainUI) UI.getCurrent();
		// DatabaseMessageSource messageSource = ui.getDatabaseMessageSource();
		// if (messageSource == null) {
		// 	return defaultMessage != null ? defaultMessage : code;
		// }
		// return messageSource.getDatabaseMessage(code, defaultMessage, locale);
	}

	@Deprecated
	public static boolean hasRole(String role) {
throw new UnsupportedOperationException("vaadin8 legacy");
	}

	@Deprecated
	public static boolean hasPermission(EntityPermission permission) {
throw new UnsupportedOperationException("vaadin8 legacy");
	}

	@Deprecated
	public static UserDetailsAdapter getUserDetails() {
		throw new UnsupportedOperationException("vaadin8 legacy");
		//UserDetailsAdapter details = ((IMainUI) UI.getCurrent()).getUserDetails();
		// UserDetailsAdapter details = null;
		// if (details == null) {
		// 	User anonymous = new User();
		// 	anonymous.setUsername("anonymous");
		// 	anonymous.setOwner("anonymous");
		// 	details = new UserDetailsAdapter(anonymous);
		// }
		// return details;
	}

	@Deprecated
	public static EntityManager getEntityManager() {
		throw new UnsupportedOperationException("vaadin8 legacy");
	}

	@Deprecated
	public static ServiceFactory getServiceFactory() {
		//return ((IMainUI) UI.getCurrent()).getServiceFactory();
		throw new UnsupportedOperationException("vaadin8 legacy");
	}

	@Deprecated
	public static CassandraService getCassandraService() {
		//return ((IMainUI) UI.getCurrent()).getCassandraService();
		throw new UnsupportedOperationException("vaadin8 legacy");
	}

	/**
	 * @deprecated This method is no longer supported in Vaadin Flow.
	 *             Use @Autowired UIEventBus in Spring components instead.
	 * @throws UnsupportedOperationException always
	 */
	@Deprecated
	public static EventBusWrapper getUIEventBus() {
		throw new UnsupportedOperationException(
				"UIUtils.getUIEventBus() is not supported in Vaadin Flow. " +
				"Use @Autowired UIEventBus instead.");
	}

	@Deprecated
	public static void trace(TracingAction action, String message) {
				throw new UnsupportedOperationException("vaadin8 legacy");

		// String username = getUserDetails().getUsername();
		// String administrator = getUserDetails().getTenant();
		// String network = getUserDetails().getNetwork();
		// trace(action, username, administrator, network, null, message);
	}

	@Deprecated
	public static void trace(TracingAction action, String username, String administrator, String network, String device,
			String message) {

								throw new UnsupportedOperationException("vaadin8 legacy");

		// logger.debug("{} {} {} {} {}", action, username, administrator, network, message);
		// ((IMainUI) UI.getCurrent()).getServiceFactory().getTracingService().trace(action, username, administrator,
		// 		network, device, message);
	}


	public static Locale getLocale() {
		return UI.getCurrent().getLocale();
	}

	@Deprecated
	public static String googleMapApiKey() {
						throw new UnsupportedOperationException("vaadin8 legacy");

		// if (((IMainUI) UI.getCurrent()).getServerName().equals("localhost")) {
		// 	return "AIzaSyCeFtOnsiE1OMOy7gTTQ6Leoip4IXh4lX8";
		// }
		// String apiKey = "";
		// try {
		// 	Properties properties = ((IMainUI) UI.getCurrent()).getAppProperties();
		// 	apiKey = properties.getProperty("googlemap.apikey");
		// } catch (Exception e) {
		// 	logger.error("unable to retrieve googlemap.apikey", e);
		// }
		// return apiKey;
	}

	@Deprecated
	public static String portalName() {
						throw new UnsupportedOperationException("vaadin8 legacy");

	// 	String value = "";
	// 	Properties properties = ((IMainUI) UI.getCurrent()).getAppProperties();
	// 	if (properties != null) {
	// 		value = properties.getProperty("portal_name");
	// 	}
	// 	return value;
	}



	public static String toHtml(String input) {
		if (input == null) {
			return "";
		}
		input = input.replace("\n", "<br />\n");
		return input;
	}



	public static String getActivationURL(String action, String user, String token) {
				throw new UnsupportedOperationException("vaadin8 legacy");

		// return getActivationURL(((IMainUI) UI.getCurrent()).getServerUrl(), action, user, token);
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
				throw new UnsupportedOperationException("vaadin8 legacy");

		// StringBuilder sb = new StringBuilder();
		// sb.append(((IMainUI) UI.getCurrent()).getServerUrl());
		// sb.append("#!");
		// sb.append("login");
		// sb.append("/");
		// sb.append(USERNAME_PARAM);
		// sb.append("=");
		// sb.append(EncryptUtils.urlEncode(user));
		// sb.append(PARAM_SEPARATOR);
		// sb.append(TOKEN_PARAM);
		// sb.append("=");
		// sb.append(token);
		// sb.append(PARAM_SEPARATOR);
		// sb.append(ACTION_PARAM);
		// sb.append("=");
		// sb.append(action);
		// return sb.toString();
	}

	public static Properties getAppProperties() {
				throw new UnsupportedOperationException("vaadin8 legacy");

		// Properties properties = ((IMainUI) UI.getCurrent()).getAppProperties();
		// return properties;
	}

	@Deprecated
	public static TimeZone getBrowserTimeZone() {
						throw new UnsupportedOperationException("vaadin8 legacy");

		// return ((IMainUI) UI.getCurrent()).getTimeZone();
	}

	@Deprecated
	public static GeoLocation getGeoLocation() {
						throw new UnsupportedOperationException("vaadin8 legacy");

		// return ((IMainUI) UI.getCurrent()).getGeoLocation();
	}

	@Deprecated
	public static IUiFactory getUiFactory() {
						throw new UnsupportedOperationException("vaadin8 legacy");

		// return ((IMainUI) UI.getCurrent()).getUiFactory();
	}

	@Deprecated
	public static void startWidgetRefresher() {
						throw new UnsupportedOperationException("vaadin8 legacy");

		// ((IMainUI) UI.getCurrent()).startWidgetRefresher();
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
