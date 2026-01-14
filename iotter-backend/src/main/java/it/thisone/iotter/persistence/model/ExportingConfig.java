package it.thisone.iotter.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import it.thisone.iotter.enums.Order;

@Embeddable
public class ExportingConfig implements Serializable {

	private static final long serialVersionUID = -3713794505880965486L;

	@Column(name = "EXPORT_TIME_ZONE")
	private String timeZone;
	
	@Column(name = "EXPORT_LOCALE")
	private String locale;
	
	@Column(name = "EXPORT_ORDER")
	@Enumerated(EnumType.STRING)
	private Order ordering;
	
	@Column(name = "EXPORT_DECIMAL_SEPARATOR")
	private String decimalSeparator;
	
	@Column(name = "EXPORT_CUSTOM_SEPARATOR")
	private String customSeparator;
	
	@Column(name = "EXPORT_COLUMN_SEPARATOR")
	private String columnSeparator;

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public Order getOrdering() {
		return ordering;
	}

	public void setOrdering(Order ordering) {
		this.ordering = ordering;
	}

	public String getDecimalSeparator() {
		return decimalSeparator;
	}

	public void setDecimalSeparator(String decimalSeparator) {
		this.decimalSeparator = decimalSeparator;
	}

	public String getCustomSeparator() {
		return customSeparator;
	}

	public void setCustomSeparator(String customSeparator) {
		this.customSeparator = customSeparator;
	}

	public String getColumnSeparator() {
		return columnSeparator;
	}

	public void setColumnSeparator(String columnSeparator) {
		this.columnSeparator = columnSeparator;
	}

}
