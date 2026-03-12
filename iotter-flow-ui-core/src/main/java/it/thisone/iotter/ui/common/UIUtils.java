package it.thisone.iotter.ui.common;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.Date;

import java.util.Locale;


import java.util.TimeZone;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;


import it.thisone.iotter.config.Constants;

import it.thisone.iotter.ui.main.UiConstants;

public final class UIUtils implements Serializable, UiConstants, Constants {
	// https://vaadin.com/book/-/page/layout.orderedlayout.html

	/**
	 ## Phase 3: Vaadin Components (constructor injection with individual services)

### Simple field components
- **ChannelSelect.java** → add `DeviceService` to constructor
- **ChannelTreeSelect.java** → add `DeviceService` to constructor
- **DeviceSerialSelect.java** → add `DeviceService` to constructor
- **UniqueUsernameValidator.java** → add `UserService` to constructor

	 */
	private static final long serialVersionUID = 1L;











	











	public static Locale getLocale() {
		return UI.getCurrent().getLocale();
	}






	public static String toHtml(String input) {
		if (input == null) {
			return "";
		}
		input = input.replace("\n", "<br />\n");
		return input;
	}






	@Deprecated
	public static TimeZone getBrowserTimeZone() {
						throw new UnsupportedOperationException("vaadin8 legacy");

		// return ((IMainUI) UI.getCurrent()).getTimeZone();
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



	public static boolean isMobile() {
		// TODO Auto-generated method stub
		return false;
	}
}
