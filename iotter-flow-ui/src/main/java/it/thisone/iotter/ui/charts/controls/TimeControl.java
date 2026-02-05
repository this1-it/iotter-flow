package it.thisone.iotter.ui.charts.controls;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.eventbus.CloseOpenWindowsEvent;
import it.thisone.iotter.ui.eventbus.UIEventBus;
import it.thisone.iotter.ui.main.UiConstants;
import it.thisone.iotter.util.PopupNotification;

/**
 * component which contains field to control window and scroll
 * @author tisone
 * Feature #261 Enhance chart Timecontrols for mobile display
 * changed layout to CssLayout
 */
public class TimeControl extends Div {

	private static final long serialVersionUID = 1L;
	private static final String TIMEBUTTONS = "timebuttons";

	private final TimeIntervalField timeInterval;
	private final TimePeriodPopup timePeriod;
	private final TimeLastMeasureButton timeLastMeasure;
	private UIEventBus uiEventBus;
	private Button controlButton;

	private final Button minus;
	private final Button plus;

	private final HorizontalLayout buttonsLayout;
	private final HorizontalLayout scrollLayout;

	private final Button apply;
	private final Button clear;

	public TimeControl(TimeIntervalField interval, TimePeriodPopup period, TimeLastMeasureButton lastMeasure) {
		addClassName(TIMEBUTTONS);

		timePeriod = period;
		timePeriod.getElement().setProperty("title", getI18nLabel("timePeriodPopup"));

		timeInterval = interval;
		timeLastMeasure = lastMeasure;

		minus = new Button();
		minus.setIcon(com.vaadin.flow.component.icon.VaadinIcon.ARROW_LEFT.create());
		minus.getElement().setProperty("title", getI18nLabel("minus"));
		minus.addClickListener(event -> timeInterval.scrollWindow(
				timePeriod.getValue().getPeriod(), -timePeriod.getValue().getAmount()));

		plus = new Button();
		plus.setIcon(com.vaadin.flow.component.icon.VaadinIcon.ARROW_RIGHT.create());
		plus.getElement().setProperty("title", getI18nLabel("plus"));
		plus.addClickListener(event -> timeInterval.scrollWindow(
				timePeriod.getValue().getPeriod(), timePeriod.getValue().getAmount()));

		scrollLayout = new HorizontalLayout(minus, timePeriod, plus);
		//scrollLayout.setAlignItems(Alignment.CENTER);

		buttonsLayout = new HorizontalLayout();
		//buttonsLayout.setAlignItems(Alignment.CENTER);
		buttonsLayout.addClassName(TIMEBUTTONS);

		apply = new Button();
		String setCaption = getI18nLabel("setCaption");
		apply.setIcon(com.vaadin.flow.component.icon.VaadinIcon.CHECK.create());
		//apply.addClassName(UIUtils.FOCUSED_STYLE);
		apply.getElement().setProperty("title", setCaption);
		apply.addClickListener(event -> {
			if (uiEventBus != null) {
				uiEventBus.post(new CloseOpenWindowsEvent());
			}
//			if (!timeInterval.updateValue()) {
//				PopupNotification.show(getI18nLabel("invalidInterval"), PopupNotification.Type.WARNING);
//			}
		});

		clear = new Button();
		String cancelCaption = getI18nLabel("clearCaption");
		clear.getElement().setProperty("title", cancelCaption);
		clear.addClickListener(event -> timeInterval.resetValue());

		buttonsLayout.add(apply);

		add(scrollLayout);
		add(timeInterval.getFromLayout());
		add(timeInterval.getToLayout());
		add(timeInterval.getPeriodLayout());
		add(buttonsLayout);
	}

	public String getI18nLabel(String key) {
		return getTranslation("timecontrol." + key);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		minus.setEnabled(enabled);
		plus.setEnabled(enabled);
		if (timeLastMeasure != null) {
			timeLastMeasure.setEnabled(enabled);
		}
		timePeriod.setEnabled(enabled);
		timeInterval.setEnabled(enabled);
		apply.setEnabled(enabled);
		clear.setEnabled(enabled);
	}

	public void activeLocalControl(boolean enabled) {
		if (controlButton != null) {
			if (enabled) {
				controlButton.addClassName(UiConstants.FOCUSED_STYLE);
			} else {
				controlButton.removeClassName(UiConstants.FOCUSED_STYLE);
			}
		}
	}

	public Button getLastMeasure() {
		return timeLastMeasure;
	}

	public Button getControlButton() {
		return controlButton;
	}

	public void setControlButton(Button controlButton) {
		this.controlButton = controlButton;
	}

	public HorizontalLayout getButtonsLayout() {
		return buttonsLayout;
	}

	public HorizontalLayout getScrollLayout() {
		return scrollLayout;
	}

	public void setUiEventBus(UIEventBus uiEventBus) {
		this.uiEventBus = uiEventBus;
	}
}
