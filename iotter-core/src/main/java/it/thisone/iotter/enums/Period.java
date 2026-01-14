package it.thisone.iotter.enums;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import it.thisone.iotter.common.Internationalizable;

public enum Period implements Internationalizable {
	// SECOND(Calendar.SECOND), // remove option since refresher works on 30
	// seconds schedule
	MINUTE(Calendar.MINUTE), //
	HOUR(Calendar.HOUR), //
	DAY(Calendar.DATE), //
	WEEK(Calendar.WEEK_OF_YEAR), //
	MONTH(Calendar.MONTH), //
	YEAR(Calendar.YEAR);

	private final int calendarField;

	private Period(int calendarField) {
		this.calendarField = calendarField;
	}

	@Override
	public String getI18nKey() {
		return "enum.period." + name().toLowerCase();
	}

	public int getCalendarField() {
		return calendarField;
	}

	public long getMillis() {
		long millis = -1;
		switch (this) {
		case MINUTE:
			millis = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
			break;
		case HOUR:
			millis = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS);
			break;
		case DAY:
			millis = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
			break;
		case MONTH:
			millis = TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS);
			break;
		case WEEK:
			millis = TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS);
			break;
		case YEAR:
			millis = TimeUnit.MILLISECONDS.convert(365, TimeUnit.DAYS);
			break;
		default:
			break;

		}

		return millis;
	}

}