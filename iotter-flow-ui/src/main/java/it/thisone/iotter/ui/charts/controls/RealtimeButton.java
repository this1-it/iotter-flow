package it.thisone.iotter.ui.charts.controls;

import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.icon.VaadinIcon;

import it.thisone.iotter.persistence.model.GraphicWidgetOptions;
import it.thisone.iotter.ui.eventbus.RealTimeEvent;
import it.thisone.iotter.ui.eventbus.UIEventBus;

public class RealtimeButton extends CustomField<GraphicWidgetOptions> {

	private static final long serialVersionUID = -5165890591061738156L;
	private GraphicWidgetOptions currentValue;
	private Button optionsButton;
	private UIEventBus uiEventBus;

	public RealtimeButton() {
		initContent();
	}

	public RealtimeButton(UIEventBus uiEventBus) {
		this.uiEventBus = uiEventBus;
		initContent();
	}

	protected Component initContent() {
		if (optionsButton == null) {
			optionsButton = new Button();
			optionsButton.addClickListener(event -> {
				GraphicWidgetOptions value = getValue();
				boolean current = value != null && Boolean.TRUE.equals(value.getRealTime());
				updateValue(!current);
			});
			add(optionsButton);
			updateValue(false);
		}
		return optionsButton;
	}

	@Override
	protected GraphicWidgetOptions generateModelValue() {
		return currentValue;
	}

	@Override
	protected void setPresentationValue(GraphicWidgetOptions options) {
		this.currentValue = options;
		if (options == null) {
			options = new GraphicWidgetOptions();
			this.currentValue = options;
		}
		options.setScale(null);
		options.setShowGrid(null);
		options.setShowMarkers(null);
		options.setAutoScale(null);

		if (optionsButton != null) {
			if (Boolean.TRUE.equals(options.getRealTime())) {
				optionsButton.setIcon(VaadinIcon.EYE.create());
			} else {
				optionsButton.setIcon(VaadinIcon.EYE_SLASH.create());
			}
		}
	}

	private void updateValue(boolean realTime) {
		GraphicWidgetOptions options = new GraphicWidgetOptions();
		options.setRealTime(realTime);
		options.setScale(null);
		options.setShowGrid(null);
		options.setShowMarkers(null);
		options.setAutoScale(null);
		setValue(options);
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		if (uiEventBus != null) {
			uiEventBus.register(this);
		}
	}

	@Override
	protected void onDetach(DetachEvent detachEvent) {
		if (uiEventBus != null) {
			uiEventBus.unregister(this);
		}
		super.onDetach(detachEvent);
	}

	// @see optionField it/thisone/iotter/ui/groupwidgets/GroupWidgetVisualizer.java
	@Subscribe
	public void changeRealTime(RealTimeEvent event) {
		updateValue(event.getRealTime());
	}

	public void setUiEventBus(UIEventBus uiEventBus) {
		this.uiEventBus = uiEventBus;
	}
}
