package it.thisone.iotter.ui.charts.controls;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
//import com.vaadin.flow.component.combobox.ComboBoxVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
//import com.vaadin.flow.component.menubar.MenuItem;
//import com.vaadin.flow.component.orderedlayout.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import it.thisone.iotter.enums.Period;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.TimeIntervalHelper;
import it.thisone.iotter.ui.common.fields.LegacyDateTimeField;
import it.thisone.iotter.ui.model.TimeInterval;
import it.thisone.iotter.ui.model.TimePeriod;
import it.thisone.iotter.util.PopupNotification;

public class TimeIntervalField extends CustomField<TimeInterval> {

	private static final long serialVersionUID = -9092884775692037358L;
	public static final String NAME = "timecontrol";

	private HorizontalLayout content;
	private LegacyDateTimeField fromDateField;
	private LegacyDateTimeField toDateField;
	private Span gmt;
	private MenuBar periodMenu;

	private TimeFieldPopup fromTimeField;
	private TimeFieldPopup toTimeField;

	private HorizontalLayout fromLayout;
	private HorizontalLayout toLayout;
	private HorizontalLayout periodLayout;

	private final TimeIntervalHelper helper;
	private TimeInterval currentValue;
	private final Collection<TimePeriod> periods;

	public TimeIntervalField(TimeZone tz, Collection<TimePeriod> periods) {
		helper = new TimeIntervalHelper(tz);
		this.periods = periods;
		initContent();
	}

	public void setCompact(boolean compact) {
		// legacy API preserved
	}

	protected Component initContent() {
		if (content != null) {
			return content;
		}

		String fromCaption = getI18nLabel("fromCaption");
		String toCaption = getI18nLabel("toCaption");
		Span from = new Span(fromCaption);
		Span to = new Span(toCaption);

		fromDateField = new LegacyDateTimeField();
		//fromDateField.addClassName("small");

		toDateField = new LegacyDateTimeField();
		//toDateField.addClassName("small");
		toDateField.setMax(new Date());

		fromLayout = new HorizontalLayout();
		//fromLayout.setAlignItems(Alignment.CENTER);
		fromLayout.addClassName("timefield");
		fromLayout.add(from, fromDateField);
		fromLayout.setFlexGrow(0.2f, from);
		fromLayout.setFlexGrow(0.8f, fromDateField);
		if (fromTimeField != null) {
			fromLayout.add(fromTimeField);
			fromLayout.setFlexGrow(0.4f, fromTimeField);
		}

		toLayout = new HorizontalLayout();
		//toLayout.setAlignItems(Alignment.CENTER);
		toLayout.addClassName("timefield");
		toLayout.add(to, toDateField);
		toLayout.setFlexGrow(0.2f, to);
		toLayout.setFlexGrow(0.8f, toDateField);
		if (toTimeField != null) {
			toLayout.add(toTimeField);
			toLayout.setFlexGrow(0.4f, toTimeField);
		}

		Calendar calendar = helper.getCalendar();
		SimpleDateFormat sdf = new SimpleDateFormat("XXX");
		sdf.setTimeZone(helper.getTimeZone());
		gmt = new Span(sdf.format(calendar.getTime()));
		//gmt.addClassName(UIUtils.DISPLAY_1024PX_STYLE);

		periodLayout = new HorizontalLayout();
//		periodLayout.setAlignItems(Alignment.CENTER);
		periodLayout.addClassName("timefield");
		periodLayout.add(gmt);
		periodMenu = createPeriodsMenuBar();
		periodLayout.add(periodMenu);

		content = new HorizontalLayout(fromLayout, toLayout, periodLayout);
//		content.setAlignItems(Alignment.CENTER);
		add(content);
		return content;
	}

	@Override
	protected TimeInterval generateModelValue() {
		return currentValue;
	}

	@Override
	protected void setPresentationValue(TimeInterval value) {
		this.currentValue = value;
		if (value != null) {
			setFields(value);
		}
	}

	public void resetValue() {
		// legacy API preserved
	}

	public void updateValue() {
		TimeInterval interval = buildTimeInterval();
		if (interval != null) {
			setValue(interval);

		}

	}

	private TimeInterval buildTimeInterval() {
		Date fromDate = fromDateField != null ? fromDateField.getValue() : null;
		Date toDate = toDateField != null ? toDateField.getValue() : null;

		if (fromDate == null || toDate == null) {
			PopupNotification.show(getI18nLabel("dateFormatError") + ": " + helper.getDateFormat(),
					PopupNotification.Type.WARNING);
			return null;
		}

		try {
			if (fromDate.after(helper.getCalendar().getTime())) {
				PopupNotification.show(getI18nLabel("futureStartDate"), PopupNotification.Type.WARNING);
				return null;
			}

			long millis = (int) (Math.random() * 100);
			millis = toDate.getTime() + millis;
			return new TimeInterval(fromDate, new Date(millis));
		} catch (Exception ex) {
			PopupNotification.show(getI18nLabel("invalidInterval"), PopupNotification.Type.WARNING);
			return null;
		}
	}

	private void setFields(TimeInterval interval) {
		if (interval == null) {
			return;
		}
		if (fromDateField != null) {
			fromDateField.setValue(interval.getStartDate());
		}
		if (fromTimeField != null) {
			fromTimeField.setValue(interval.getStartDate());
		}
		if (toDateField != null) {
			Date endDate = interval.getEndDate();
			if (endDate != null) {
				Date rangeEnd = toDateField.getMax();
				if (rangeEnd == null || endDate.after(rangeEnd)) {
					toDateField.setMax(endDate);
				}
			}
			toDateField.setValue(endDate);
		}
		if (toTimeField != null) {
			toTimeField.setValue(interval.getEndDate());
		}
	}

	private String getI18nLabel(String key) {
		return getTranslation(getI18nKey() + "." + key);
	}

	private String getI18nKey() {
		return NAME;
	}

	private MenuBar createPeriodsMenuBar() {
		final MenuBar root = new MenuBar();
		root.getElement().setProperty("title", getI18nLabel("periods"));
		root.setOpenOnHover(false);
		root.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
		com.vaadin.flow.component.contextmenu.MenuItem controls = root.addItem(VaadinIcon.GRID_BIG.create());

		for (TimePeriod period : periods) {
			final String name = period.getName() != null ? period.getName() : period.toString();
			controls.getSubMenu().addItem(name, event -> {
				Date now = new Date();
				TimeInterval interval = helper.period(now, period);
				setFields(interval);
				root.getElement().setProperty("title", name);
				updateValue();
			});
		}

		return root;
	}

	public ComboBox<TimePeriod> getPeriodsComboBox() {
		final ComboBox<TimePeriod> combo = new ComboBox<>();
		combo.setItems(periods);
		//combo.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
		combo.setWidth("6.5em");
		combo.setItemLabelGenerator(TimePeriod::getName);
		combo.addValueChangeListener(event -> {
			TimePeriod selected = event.getValue();
			if (selected == null) {
				return;
			}
			TimeInterval interval = helper.period(new Date(), selected);
			setFields(interval);
			updateValue();
		});
		return combo;
	}

	public void scrollWindow(Period period, int amount) {
		TimeInterval interval = buildTimeInterval();
		if (interval == null) {
			return;
		}
		Calendar calendar = helper.getCalendar();
		calendar.setTime(interval.getStartDate());
		calendar.add(period.getCalendarField(), amount);
		if (calendar.getTime().after(helper.getCalendar().getTime())) {
			PopupNotification.show(getI18nLabel("futureStartDate"), PopupNotification.Type.WARNING);
			return;
		}

		Date startDate = calendar.getTime();
		calendar.setTime(interval.getEndDate());
		calendar.add(period.getCalendarField(), amount);
		Date endDate = calendar.getTime();

		setValue(new TimeInterval(startDate, endDate));
	}

	public TimeIntervalHelper getHelper() {
		return helper;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (periodMenu != null) {
			//periodMenu.setEnabled(enabled);
		}
		if (toDateField != null) {
			toDateField.setEnabled(enabled);
		}
		if (fromDateField != null) {
			fromDateField.setEnabled(enabled);
		}
	}

	public HorizontalLayout getFromLayout() {
		return fromLayout;
	}

	public HorizontalLayout getToLayout() {
		return toLayout;
	}

	public HorizontalLayout getPeriodLayout() {
		return periodLayout;
	}

	public MenuBar getPeriodMenu() {
		return periodMenu;
	}
}
