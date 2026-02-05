package it.thisone.iotter.ui.charts.controls;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.enums.ChartScaleType;
import it.thisone.iotter.persistence.model.GraphicWidgetOptions;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.fields.ChartScaleTypeSelect;

/**
 * Chart options editor for visualization controls.
 */
@SuppressWarnings("serial")
public class GraphicWidgetOptionsField extends CustomField<GraphicWidgetOptions> {

	private static final String NAME = "chart.options";

	private GraphicWidgetOptions currentValue;

	private Button apply;
	private Checkbox realTime;
	private ChartScaleTypeSelect scale;
	private Checkbox showGrid;
	private Checkbox localControls;
	private Checkbox autoScale;
	private Checkbox showMarkers;
	private Button optionsButton;
	private Dialog popUp;
	private HorizontalLayout content;

	public GraphicWidgetOptionsField() {
		initContent();
	}

	@Override
	protected GraphicWidgetOptions generateModelValue() {
		return currentValue;
	}

	@Override
	protected void setPresentationValue(GraphicWidgetOptions options) {
		if (options == null) {
			options = new GraphicWidgetOptions();
		}
		this.currentValue = options;

		if (showMarkers != null && !showMarkers.isReadOnly()) {
			showMarkers.setValue(Boolean.TRUE.equals(options.getShowMarkers()));
		}
		if (autoScale != null && !autoScale.isReadOnly()) {
			autoScale.setValue(Boolean.TRUE.equals(options.getAutoScale()));
		}
		if (realTime != null && !realTime.isReadOnly()) {
			realTime.setValue(Boolean.TRUE.equals(options.getRealTime()));
		}
		if (showGrid != null && !showGrid.isReadOnly()) {
			showGrid.setValue(Boolean.TRUE.equals(options.getShowGrid()));
		}
		if (scale != null && !scale.isReadOnly()) {
			scale.setValue(options.getScale());
		}
		if (localControls != null && !localControls.isReadOnly()) {
			localControls.setValue(Boolean.TRUE.equals(options.getLocalControls()));
		}
		updateDependentState();
	}

	private void openPopup() {
		ensurePopup();
		setPresentationValue(currentValue);
		popUp.open();
	}

	private Component buildPopupContent() {
		ensureFields();

		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setPadding(true);

		String gridCaption = getI18nLabel("gridCaption");
		String realTimeCaption = getI18nLabel("realTimeCaption");
		String scaleCaption = getI18nLabel("scaleCaption");
		String localControlsCaption = getI18nLabel("localControlsCaption");
		String autoScaleCaption = getI18nLabel("autoScaleCaption");
		String showMarkersCaption = getI18nLabel("showMarkersCaption");

		localControls.setLabel(localControlsCaption);
		realTime.setLabel(realTimeCaption);
		showGrid.setLabel(gridCaption);
		autoScale.setLabel(autoScaleCaption);
		showMarkers.setLabel(showMarkersCaption);
		scale.setLabel(scaleCaption);
		scale.setWidth("10em");

		apply = new Button();
		apply.setIcon(VaadinIcon.CHECK.create());
		apply.getElement().setProperty("title", getI18nLabel("setCaption"));
		apply.addClickListener(event -> {
			updateValueFromInputs();
			if (popUp != null) {
				popUp.close();
			}
		});

		HorizontalLayout buttonBar = new HorizontalLayout(apply);
		//buttonBar.addClassName(UIUtils.BUTTONS_STYLE);
		buttonBar.setSpacing(true);

		layout.add(localControls, realTime, showGrid, showMarkers);
		// legacy behavior kept: scale/autoScale controls exist but are hidden from popup.
		layout.add(buttonBar);

		return layout;
	}

	private void updateValueFromInputs() {
		GraphicWidgetOptions options = new GraphicWidgetOptions();
		options.setAutoScale(autoScale != null ? autoScale.getValue() : false);
		options.setShowMarkers(showMarkers != null ? showMarkers.getValue() : false);
		options.setRealTime(realTime != null ? realTime.getValue() : false);
		options.setScale(scale != null ? (ChartScaleType) scale.getValue() : null);
		options.setShowGrid(showGrid != null ? showGrid.getValue() : false);
		options.setLocalControls(localControls != null ? localControls.getValue() : false);
		setValue(options);
	}

	protected Component initContent() {
		ensureFields();
		if (optionsButton == null) {
			optionsButton = new Button();
			optionsButton.setIcon(VaadinIcon.COG.create());
			optionsButton.addClickListener(event -> openPopup());
		}
		if (content == null) {
			content = new HorizontalLayout(optionsButton);
			content.setSpacing(false);
			add(content);
		}
		ensurePopup();
		return content;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(readOnly);
		if (apply != null) {
			apply.setEnabled(!readOnly);
		}
		if (showGrid != null) {
			showGrid.setReadOnly(readOnly);
		}
		if (scale != null) {
			scale.setReadOnly(readOnly);
		}
		if (realTime != null) {
			realTime.setReadOnly(readOnly);
		}
		if (localControls != null) {
			localControls.setReadOnly(readOnly);
		}
		if (showMarkers != null) {
			showMarkers.setReadOnly(readOnly);
		}
		if (autoScale != null) {
			autoScale.setReadOnly(readOnly);
		}
	}

	public String getI18nLabel(String key) {
		return getTranslation(getI18nKey() + "." + key);
	}

	public String getI18nKey() {
		return NAME;
	}

	public Checkbox getLocalControls() {
		return localControls;
	}

	public ChartScaleTypeSelect getScale() {
		return scale;
	}

	public Checkbox getShowGrid() {
		return showGrid;
	}

	public Checkbox getRealTime() {
		return realTime;
	}

	public Checkbox getAutoScale() {
		return autoScale;
	}

	public Checkbox getShowMarkers() {
		return showMarkers;
	}

	private void ensurePopup() {
		if (popUp == null) {
			popUp = new Dialog();
			popUp.setCloseOnOutsideClick(true);
			popUp.setCloseOnEsc(true);
			popUp.add(buildPopupContent());
		}
	}

	private void ensureFields() {
		if (localControls == null) {
			localControls = new Checkbox();
			localControls.addValueChangeListener(event -> updateDependentState());
		}
		if (realTime == null) {
			realTime = new Checkbox();
		}
		if (showGrid == null) {
			showGrid = new Checkbox();
		}
		if (scale == null) {
			scale = new ChartScaleTypeSelect();
		}
		if (autoScale == null) {
			autoScale = new Checkbox();
		}
		if (showMarkers == null) {
			showMarkers = new Checkbox();
		}
	}

	private void updateDependentState() {
		boolean enabled = localControls != null && localControls.getValue();
		if (realTime != null) {
			realTime.setEnabled(enabled);
		}
		if (showGrid != null) {
			showGrid.setEnabled(enabled);
		}
		if (scale != null) {
			scale.setEnabled(enabled);
		}
		if (autoScale != null) {
			autoScale.setEnabled(enabled);
		}
		if (showMarkers != null) {
			showMarkers.setEnabled(enabled);
		}
	}
}
