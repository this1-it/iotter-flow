package it.thisone.iotter.ui.common.charts;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import com.vaadin.flow.component.datepicker.DatePicker;

import it.thisone.iotter.enums.Period;
import it.thisone.iotter.ui.model.TimeInterval;
import it.thisone.iotter.ui.model.TimePeriod;

public class TimeIntervalHelper {
	public TimeIntervalHelper(TimeZone timeZone) {
		super();
		this.timeZone = timeZone;
	}

	private TimeZone timeZone;
	private String dateFormat = "yyyy-MM-dd HH:mm";
	
	public TimeInterval period(Date now, TimePeriod period) {
		TimeInterval interval = null;
		switch (period.getType()) {
		case CURRENT:
			interval = currentTimePeriod(now, period);
			break;
		case LAST:
			interval = lastTimePeriod(now, period);
			break;
		}
		
		return interval;
	}
	
	public TimeInterval movingPeriod(Date date, TimePeriod value) {
		return lastTimePeriod(date,value);
	}

	public TimeInterval lastTimePeriod(Date endDate, TimePeriod period) {
		if (endDate == null) {
			endDate = getCalendar().getTime();
		}
		if (period == null) {
			period = new TimePeriod();
		}

		Calendar calendar = getCalendar();
		calendar.setTime(endDate);
		// END
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		endDate = getCalendar().getTime();

		// START
		calendar.add(period.getPeriod().getCalendarField(), - period.getAmount());
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar.getTime();
		TimeInterval interval = new TimeInterval(startDate, endDate);
		interval.setExtremes(false);
		return interval;
	}

	public TimeInterval lastTimePeriod(TimePeriod period) {
		return lastTimePeriod(getCalendar().getTime(), period);
	}

	public Calendar getCalendar() {
		return Calendar.getInstance(timeZone);
	}

	
	
	
	/**
	 * 
	 * @param now
	 * @param period
	 * @return
	 */
	public TimeInterval currentTimePeriod(Date now, TimePeriod period) {
		TimeInterval interval = currentPeriod(now, period.getPeriod());
		if (period.getAmount() > 1) {
			Calendar calendar = getCalendar();
			calendar.setTime(interval.getStartDate());
			calendar.add(period.getPeriod().getCalendarField(), -(period.getAmount() - 1));
			interval.setStartDate(calendar.getTime());
		}
		return interval;
	}

	public TimeInterval currentPeriod(Date now, Period period) {
		if (now == null) {
			now = getCalendar().getTime();
		}
		TimeInterval interval = new TimeInterval();
		switch (period) {
		case MINUTE:
			interval = currentMinute(now);
			break;
		case HOUR:
			interval = currentHour(now);
			break;
		case DAY:
			interval = currentDay(now);
			break;
		case WEEK:
			interval = currentWeek(now);
			break;
		case MONTH:
			interval = currentMonth(now);
			break;
		case YEAR:
			interval = currentYear(now);
			break;
		default:
			break;
		}
		return interval;
	}

	public TimeInterval currentYear(Date date) {
		// Bug #220 [VAADIN] first sample of a chart is not showed
		int millis = randomMillis();
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_YEAR, 1); // first day of the year.
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar.getTime();

		calendar.set(Calendar.MONTH, 11); // 11 = december
		calendar.set(Calendar.DAY_OF_MONTH, 31);

		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, millis);

		Date endDate = calendar.getTime();
		return new TimeInterval(startDate, endDate);
	}

	public TimeInterval currentMonth(Date date) {
		// Bug #220 [VAADIN] first sample of a chart is not showed
		int millis = randomMillis();
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		// first day of the month.
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar.getTime();

		// last day of the month.
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, millis);

		Date endDate = calendar.getTime();
		return new TimeInterval(startDate, endDate);
	}

	public TimeInterval currentWeek(Date date) {
		// Bug #220 [VAADIN] first sample of a chart is not showed
		int millis = randomMillis();
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		// Bug #137 [VAADIN] Time control with period week does not work
		// properly at Sunday
		calendar.setFirstDayOfWeek(Calendar.MONDAY);

		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		Date startDate = calendar.getTime();
		calendar.add(Calendar.DATE, 6);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, millis);
		Date endDate = calendar.getTime();
		return new TimeInterval(startDate, endDate);
	}

	public TimeInterval currentDay(Date date) {
		// Bug #220 [VAADIN] first sample of a chart is not showed
		int millis = randomMillis();
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar.getTime();

		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, millis);
		Date endDate = calendar.getTime();

		return new TimeInterval(startDate, endDate);
	}

	public TimeInterval currentHour(Date date) {
		// Bug #220 [VAADIN] first sample of a chart is not showed
		int millis = randomMillis();
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar.getTime();

		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, millis);
		Date endDate = calendar.getTime();
		return new TimeInterval(startDate, endDate);
	}

	public TimeInterval currentMinute(Date date) {
		// Bug #220 [VAADIN] first sample of a chart is not showed
		int millis = randomMillis();
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar.getTime();

		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, millis);
		Date endDate = calendar.getTime();
		return new TimeInterval(startDate, endDate);
	}

	private int randomMillis() {
		return (int) (Math.random() * 500);
	}


	private Date buildDate(Date date, Date time, boolean start) {
		int millis = randomMillis();
		Calendar calendarDate = getCalendar();
		calendarDate.setTime(date);
		Calendar calendarTime = getCalendar();
		calendarTime.setTime(time);
		calendarDate.set(Calendar.HOUR_OF_DAY, calendarTime.get(Calendar.HOUR_OF_DAY));
		calendarDate.set(Calendar.MINUTE, calendarTime.get(Calendar.MINUTE));
		calendarDate.set(Calendar.SECOND, calendarTime.get(Calendar.SECOND));
		calendarDate.set(Calendar.MILLISECOND, start ? 0 : millis);
		return calendarDate.getTime();
	}



	private Date parseDate(String value, String pattern) throws ParseException {
		String separator = "-";
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		sdf.setLenient(false);
		sdf.setTimeZone(getTimeZone());
		String parts[] = pattern.split(separator);
		String fields[] = value.split(separator);
		if (parts.length != fields.length) {
			throw new ParseException(pattern, 0);
		}
		for (int i = 0; i < parts.length; i++) {
			fields[i] = StringUtils.leftPad(fields[i].trim(), parts[i].length(), '0');
		}
		value = StringUtils.join(fields, separator);
		return sdf.parse(value);
	}

	private Date parseTime(String value, String pattern) throws ParseException {
		String separator = ":";
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		sdf.setLenient(false);
		sdf.setTimeZone(getTimeZone());
		String parts[] = "00:00:00".split(separator);
		String fields[] = value.split(separator);
		for (int i = 0; i < fields.length; i++) {
			parts[i] = StringUtils.leftPad(fields[i].trim(), parts[i].length(), '0');
		}
		value = StringUtils.join(parts, separator);
		return sdf.parse(value);
	}



	private String getI18nKey() {
		return "timecontrol";
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public String getDateFormat() {
		return dateFormat;
	}


	@SuppressWarnings("serial")
	public DatePicker createDateField() {
		DatePicker field = new DatePicker();
		
		field.setPlaceholder(dateFormat);
		//field.setStyleName("timeinterval");


		
		return field;
	}

	/**
	 * Utility method to convert java.util.Date to LocalDate using the configured timezone
	 * @param date the Date to convert
	 * @return LocalDate representation, or null if date is null
	 */
	public LocalDate toLocalDate(Date date) {
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
	public Date toDate(LocalDate localDate) {
		if (localDate == null) {
			return null;
		}
		return Date.from(localDate.atStartOfDay(timeZone.toZoneId()).toInstant());
	}






}
