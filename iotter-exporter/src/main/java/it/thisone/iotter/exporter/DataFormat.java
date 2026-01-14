package it.thisone.iotter.exporter;

import java.io.Serializable;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.TimeZone;

public class DataFormat implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Locale locale;
	private final TimeZone timeZone;
	private DecimalFormatSymbols decimalFormatSymbols;

	
	public DataFormat(Locale locale, TimeZone timeZone) {
		super();
		this.locale = locale;
		this.timeZone = timeZone;
		this.decimalFormatSymbols = DecimalFormatSymbols.getInstance(locale);
	}

	

	public TimeZone getTimeZone() {
		return timeZone;
	}


	public DecimalFormatSymbols getDecimalFormatSymbols() {
		return decimalFormatSymbols;
	}


	public Locale getLocale() {
		return locale;
	}
	
	public void setDecimalFormatSymbols(DecimalFormatSymbols decimalFormatSymbols) {
		this.decimalFormatSymbols = decimalFormatSymbols;
	}

//	public void setLocale(Locale locale) {
//		this.locale = locale;
//	}
//	public void setTimeZone(TimeZone timeZone) {
//		this.timeZone = timeZone;
//	}
	
}
