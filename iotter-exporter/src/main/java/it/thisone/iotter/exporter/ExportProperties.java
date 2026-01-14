package it.thisone.iotter.exporter;

import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.csv.CSVFormat;

import it.thisone.iotter.enums.ExportFileMode;
import it.thisone.iotter.enums.ExportFormat;
import it.thisone.iotter.enums.Order;

public class ExportProperties implements IExportProperties {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private ExportFormat format = ExportFormat.EXCEL;
	private ExportFileMode fileMode = ExportFileMode.SINGLE;
	private TimeZone timeZone = TimeZone.getDefault();
	private char columnSeparator = CSVFormat.EXCEL.getDelimiter();
	private char decimalSeparator = DecimalFormatSymbols.getInstance().getDecimalSeparator();
	private Order order = Order.ASCENDING;
	private Locale locale = Locale.getDefault();
	
	// Feature #2162
	private boolean legacy = true;

	
	public TimeZone getTimeZone() {
		return timeZone;
	}
	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}
	public char getColumnSeparator() {
		return columnSeparator;
	}
	public void setColumnSeparator(char delimiter) {
		this.columnSeparator = delimiter;
	}
	public char getDecimalSeparator() {
		return decimalSeparator;
	}
	public void setDecimalSeparator(char decimalSeparator) {
		this.decimalSeparator = decimalSeparator;
	}
	public ExportFileMode getFileMode() {
		return fileMode;
	}
	public void setFileMode(ExportFileMode fileMode) {
		this.fileMode = fileMode;
	}
	public ExportFormat getFormat() {
		return format;
	}
	public void setFormat(ExportFormat format) {
		this.format = format;
	}
	public Order getOrder() {
		return order;
	}
	public void setOrder(Order order) {
		this.order = order;
	}
	@Override
	public String getFileExtension() {
		if (fileMode.equals(ExportFileMode.MULTI)) {
			return "zip";
		}
		if (format.equals(ExportFormat.EXCEL)) {
			return "xls";
		}
		if (format.equals(ExportFormat.CSV)) {
			return "csv";
		}
		return null;
	}
	public Locale getLocale() {
		return locale;
	}
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	public boolean isLegacy() {
		return legacy;
	}
	public void setLegacy(boolean legacy) {
		this.legacy = legacy;
	}

}
