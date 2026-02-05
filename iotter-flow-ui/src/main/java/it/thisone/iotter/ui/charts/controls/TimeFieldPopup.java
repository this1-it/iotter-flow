package it.thisone.iotter.ui.charts.controls;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
//import com.vaadin.flow.component.combobox.ComboBoxVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
//import com.vaadin.flow.component.orderedlayout.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;

import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.eventbus.CloseOpenWindowsEvent;
import it.thisone.iotter.ui.eventbus.UIEventBus;

public class TimeFieldPopup extends CustomField<Date> {
	private static final long serialVersionUID = -9092884775692037358L;

	private static final String DATE_FORMAT = "HH:mm:ss";
	private static final String REGEXP = "^(([0-1]?[0-9])|([2][0-3])):([0-5]?[0-9])(:([0-5]?[0-9]))?$";

	private Date currentValue;
	private HorizontalLayout content;
	private Dialog popUp;

	private TextField tf;
	private Button clockButton;
	private final TimeZone timeZone;
	private UIEventBus uiEventBus;

	public TimeFieldPopup(TimeZone tz) {
		timeZone = tz;
		initContent();
		//addClassName("timefield");
	}

	protected Component initContent() {
		if (content == null) {
			tf = new TextField();
			//tf.addClassName(UIUtils.DISPLAY_768PX_STYLE);
			tf.addThemeVariants(TextFieldVariant.LUMO_SMALL);
			tf.setWidth("5.2em");
			tf.setVisible(!isMobileView());
//			tf.addValueChangeListener(event -> {
//				String value = event.getValue();
//				if (value != null && value.matches(REGEXP)) {
//					tf.removeClassName(UIUtils.INVALID_STYLE);
//				} else {
//					tf.addClassName(UIUtils.INVALID_STYLE);
//				}
//			});

			clockButton = new Button();
			clockButton.setIcon(VaadinIcon.CLOCK.create());
			clockButton.addClickListener(event -> openPopup());

			content = new HorizontalLayout(tf, clockButton);
			//content.setAlignItems(Alignment.CENTER);
			content.setSpacing(true);
			content.addClassName("timeinterval");
			add(content);
		}
		return content;
	}

	@Override
	protected Date generateModelValue() {
		return currentValue;
	}

	@Override
	protected void setPresentationValue(Date newValue) {
		this.currentValue = newValue;
		if (newValue != null && tf != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			sdf.setTimeZone(timeZone);
			tf.setValue(sdf.format(newValue));
			content.getElement().setProperty("title", tf.getValue());
			//tf.removeClassName(UIUtils.INVALID_STYLE);
		}
	}

	private void openPopup() {
		if (popUp == null) {
			popUp = new Dialog();
			//popUp.addClassName("timepopup");
			popUp.setCloseOnEsc(true);
			popUp.setCloseOnOutsideClick(true);
		}

		if (!popUp.isOpened()) {
			if (uiEventBus != null) {
				uiEventBus.post(new CloseOpenWindowsEvent());
			}
			popUp.removeAll();
			popUp.add(buildPopupContent());

			if (isMobileView()) {
				// TODO(flow-migration): replace this with a real responsive strategy based on client details.
				popUp.setWidthFull();
				popUp.setHeightFull();
			}
			popUp.open();
		} else {
			popUp.close();
		}
	}

	public Component buildPopupContent() {
		Date time = getValue();
		if (time == null) {
			time = this.currentValue != null ? this.currentValue : new Date();
		}

		Calendar calendar = getCalendar();
		calendar.setTime(time);

		final ComboBox<Integer> hourBox = createTimeComboBox(24);
		hourBox.setValue(calendar.get(Calendar.HOUR_OF_DAY));

		final ComboBox<Integer> minuteBox = createTimeComboBox(60);
		minuteBox.setValue(calendar.get(Calendar.MINUTE));

		final ComboBox<Integer> secondBox = createTimeComboBox(60);
		secondBox.setValue(calendar.get(Calendar.SECOND));

		Button apply = new Button();
		apply.setIcon(VaadinIcon.CHECK.create());
		apply.setWidth("25px");
		apply.addClickListener(event -> {
			Date newValue = calendarTime(hourBox.getValue(), minuteBox.getValue(), secondBox.getValue());
			if (newValue != null) {
				setValue(newValue);
				if (popUp != null) {
					popUp.close();
				}
			}
		});

		HorizontalLayout buttons = new HorizontalLayout(apply);
		//buttons.addClassName(UIUtils.BUTTONS_STYLE);

		Span hourLabel = new Span(getTranslation("enum.period.hours"));
		hourLabel.addClassName("small");

		Span minuteLabel = new Span(getTranslation("enum.period.minutes"));
		minuteLabel.addClassName("small");

		Span secondLabel = new Span(getTranslation("enum.period.seconds"));
		secondLabel.addClassName("small");

		HorizontalLayout layout = new HorizontalLayout(
				hourLabel, hourBox,
				minuteLabel, minuteBox,
				secondLabel, secondBox,
				buttons);
		layout.setPadding(true);
		layout.addClassName("timefield");
		//layout.setAlignItems(Alignment.CENTER);
		return layout;
	}

	private Date calendarTime(int hour, int minute, int second) {
		if (hour < 0 || hour > 23) {
			return null;
		}
		if (minute < 0 || minute > 59) {
			return null;
		}
		if (second < 0 || second > 59) {
			return null;
		}
		Calendar calendar = getCalendar();
		calendar.setTime(this.currentValue != null ? this.currentValue : new Date());
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	private ComboBox<Integer> createTimeComboBox(int size) {
		final ComboBox<Integer> combo = new ComboBox<>();
		//combo.addThemeVariants(ComboBoxVariant.LUMO_SMALL);

		List<Integer> items = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			items.add(i);
		}

		combo.setItems(items);
		combo.setAllowCustomValue(false);
		combo.setClearButtonVisible(false);
		combo.setWidth("4em");
		combo.setPageSize(12);

		return combo;
	}

	private Calendar getCalendar() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(timeZone);
		return calendar;
	}

	@Override
	protected void onDetach(DetachEvent detachEvent) {
		if (popUp != null) {
			popUp.close();
		}
		super.onDetach(detachEvent);
	}

	public void setCompact(boolean compact) {
		if (tf == null) {
			return;
		}
		tf.setVisible(!compact);
//		if (compact) {
//			tf.addClassName(UIUtils.DISPLAY_768PX_STYLE);
//		} else {
//			tf.removeClassName(UIUtils.DISPLAY_768PX_STYLE);
//		}
	}

	public void setUiEventBus(UIEventBus uiEventBus) {
		this.uiEventBus = uiEventBus;
	}

	private boolean isMobileView() {
		// TODO(flow-migration): implement explicit mobile detection via ExtendedClientDetails.
		return false;
	}
}
