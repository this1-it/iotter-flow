package it.thisone.iotter.ui.charts.controls;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

import it.thisone.iotter.ui.common.UIUtils;

public class CustomDateField extends CustomField<Date> {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(CustomDateField.class);
	private static final String DATE_FORMAT = "dd/MM/yy";
	private static final String REGEXP = "^([0]?[1-9]|[1|2][0-9]|[3][0|1])/([0]?[1-9]|[1][0-2])/([0-9]{2})$";

	private final TimeZone timeZone;
	private final ZoneId zoneId;
	private final Calendar calendar;
	private final SimpleDateFormat sdf;

	private HorizontalLayout content;
	private TextField textField;
	private DatePicker datePicker;
	private Date currentValue;

	public CustomDateField(TimeZone tz) {
		timeZone = tz;
		zoneId = tz.toZoneId();
		calendar = Calendar.getInstance(timeZone);
		sdf = new SimpleDateFormat(DATE_FORMAT);
		sdf.setTimeZone(timeZone);
		//addClassName("custom-datefield");
	}

	
	protected Component initContent() {
		if (content != null) {
			return content;
		}

		content = new HorizontalLayout();
		content.setSpacing(false);
		content.addClassName("timeinterval");

		textField = new TextField();
		//textField.addClassName(UIUtils.DISPLAY_768PX_STYLE);
		textField.addClassName("small");
		textField.setWidth("5.2em");
		textField.addValueChangeListener(event -> onTextChanged(event.getValue()));

		datePicker = new DatePicker();
		datePicker.addClassName("timeinterval");
		datePicker.addValueChangeListener(event -> {
			LocalDate localDate = event.getValue();
			if (localDate == null) {
				setModelValue(null, true);
				return;
			}
			Date date = Date.from(localDate.atStartOfDay(zoneId).toInstant());
			setValue(date);
		});

		content.add(textField, datePicker);
		return content;
	}

	private void onTextChanged(String value) {
		if (value == null || !value.matches(REGEXP)) {
			//textField.addClassName(UIUtils.INVALID_STYLE);
			return;
		}

		Date date = parseDateString(value);
		if (date == null) {
			//textField.addClassName(UIUtils.INVALID_STYLE);
			return;
		}

		//textField.removeClassName(UIUtils.INVALID_STYLE);
		LocalDate localDate = date.toInstant().atZone(zoneId).toLocalDate();
		datePicker.setValue(localDate);
		setValue(date);
	}

	@Override
	protected Date generateModelValue() {
		return currentValue;
	}

	@Override
	protected void setPresentationValue(Date newValue) {
		currentValue = newValue;
		if (newValue == null) {
			if (datePicker != null) {
				datePicker.clear();
			}
			if (textField != null) {
				textField.clear();
			}
			return;
		}
		if (datePicker != null) {
			datePicker.setValue(newValue.toInstant().atZone(zoneId).toLocalDate());
		}
		if (textField != null) {
			textField.setValue(dateToString(newValue));
			//textField.removeClassName(UIUtils.INVALID_STYLE);
		}
	}

	private Date parseDateString(String source) {
		if (source == null || source.length() != DATE_FORMAT.length()) {
			return null;
		}
		try {
			return sdf.parse(source.trim());
		} catch (Exception e) {
			logger.debug("parseDateString", e);
			return null;
		}
	}

	private String dateToString(Date date) {
		return sdf.format(date);
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public void setCompact(boolean compact) {
		if (textField == null) {
			return;
		}
		textField.setVisible(!compact);
//		if (compact) {
//			textField.addClassName(UIUtils.DISPLAY_768PX_STYLE);
//		} else {
//			textField.removeClassName(UIUtils.DISPLAY_768PX_STYLE);
//		}
	}
}
