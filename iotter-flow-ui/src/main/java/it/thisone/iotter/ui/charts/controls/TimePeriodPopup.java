package it.thisone.iotter.ui.charts.controls;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;

//import com.vaadin.flow.component.orderedlayout.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import it.thisone.iotter.enums.Period;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.model.TimePeriod;
import it.thisone.iotter.ui.model.TimePeriod.TimePeriodEnum;

public class TimePeriodPopup extends CustomField<TimePeriod> {

	private static final long serialVersionUID = 5099612269691341014L;
	private TimePeriod currentValue;
	private Span label;
	private MenuBar root;
	private HorizontalLayout content;

	public TimePeriodPopup() {
		initContent();
		setValue(new TimePeriod());
	}

	protected Component initContent() {
		if (content != null) {
			return content;
		}

		content = new HorizontalLayout();
		content.addClassName("timefield");
		//content.setAlignItems(Alignment.CENTER);

		label = new Span();
		//label.addClassName(UIUtils.DISPLAY_1024PX_STYLE);

		root = createPeriodsMenuBar();
		content.add(label, root);
		add(content);
		updateLabel();
		return content;
	}

	@Override
	protected TimePeriod generateModelValue() {
		return currentValue;
	}

	@Override
	protected void setPresentationValue(TimePeriod value) {
		this.currentValue = value != null ? value : new TimePeriod();
		updateLabel();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (root != null) {
			//root.setEnabled(enabled);
		}
	}

	private MenuBar createPeriodsMenuBar() {
		List<TimePeriod> periods = getPeriods();
		final MenuBar menuBar = new MenuBar();
		menuBar.setOpenOnHover(false);
		menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
		MenuItem controls = menuBar.addItem(VaadinIcon.CLOCK.create());

		for (final TimePeriod period : periods) {
			controls.getSubMenu().addItem(periodLabel(period), event -> setValue(period));
		}
		return menuBar;
	}

	private void updateLabel() {
		if (label != null) {
			label.setText(periodLabel(getValue()));
		}
	}

	private String periodLabel(TimePeriod period) {
		if (period == null) {
			period = new TimePeriod();
		}
		String i18nKey = period.getPeriod().getI18nKey();
		if (period.getAmount() > 1) {
			i18nKey = i18nKey + "s";
		}
		return period.getAmount() + " " + getTranslation(i18nKey);
	}

	public void setCompact(boolean compact) {
		if (label == null) {
			return;
		}
		label.setVisible(!compact);
//		if (compact) {
//			label.addClassName(UIUtils.DISPLAY_1024PX_STYLE);
//		} else {
//			label.removeClassName(UIUtils.DISPLAY_1024PX_STYLE);
//		}
	}

	private List<TimePeriod> getPeriods() {
		List<TimePeriod> periods = new ArrayList<>();
		periods.add(new TimePeriod(Period.MINUTE, 1, TimePeriodEnum.CURRENT));
		periods.add(new TimePeriod(Period.MINUTE, 5, TimePeriodEnum.CURRENT));
		periods.add(new TimePeriod(Period.MINUTE, 15, TimePeriodEnum.CURRENT));
		periods.add(new TimePeriod(Period.HOUR, 1, TimePeriodEnum.CURRENT));
		periods.add(new TimePeriod(Period.HOUR, 6, TimePeriodEnum.CURRENT));
		periods.add(new TimePeriod(Period.DAY, 1, TimePeriodEnum.CURRENT));
		periods.add(new TimePeriod(Period.WEEK, 1, TimePeriodEnum.CURRENT));
		periods.add(new TimePeriod(Period.MONTH, 1, TimePeriodEnum.CURRENT));
		return periods;
	}
}
