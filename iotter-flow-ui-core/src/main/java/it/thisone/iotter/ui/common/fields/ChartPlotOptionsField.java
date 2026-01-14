package it.thisone.iotter.ui.common.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.DashStyle;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.component.Alignment;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.AbstractCompositeField;
import com.vaadin.flow.component.Unit;

import org.vaadin.flow.components.GridLayout;

import it.thisone.iotter.enums.ChartAxis;
import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChartPlotOptions;
import it.thisone.iotter.persistence.model.MeasureRange;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.common.charts.CustomMarkerSymbolEnum;

public class ChartPlotOptionsField extends AbstractCompositeField<GridLayout, ChartPlotOptionsField, ChartPlotOptions> {
	private ChartPlotOptions currentValue;
	private Binder<ChartPlotOptions> binder;
	public static final String I18NKEY = "graphplotoptions.field";
	public static final int SPEED_MEASURE_UNIT = 17;
	public static final int DEGREE_MEASURE_UNIT = 34;
	public static final int BOOLEAN_MEASURE_UNIT = 68;

	public static final Integer[] DIRECTION_SENSORS = new Integer[] { 63, 68, 42, 52 };

	public static final String[] MARKER_SYMBOLS = { //
			CustomMarkerSymbolEnum.DIAMOND.name(), //
			CustomMarkerSymbolEnum.TRIANGLE.name(), //
			CustomMarkerSymbolEnum.TRIANGLE_DOWN.name(), //
			CustomMarkerSymbolEnum.SQUARE.name(), //
			CustomMarkerSymbolEnum.CIRCLE.name() //
			// CustomMarkerSymbolEnum.ARROW.name() //
	};

	public static final String[] DASH_STYLES = { //
			DashStyle.SOLID.name(), //
			DashStyle.DASH.name(), //
			DashStyle.DOT.name(), //
			DashStyle.SHORTDASH.name(), //
			DashStyle.LONGDASH.name(), //
			DashStyle.SHORTDASHDOT.name(), //
			DashStyle.LONGDASHDOT.name() //
	};
	public static final String[] CHART_TYPES = { //
			ChartType.AREASPLINE.toString(), //
			ChartType.SPLINE.toString(), //
			ChartType.LINE.toString() //
			// ChartType.COLUMN.toString() //
	};

	public static final String[] AXIS = { //
			ChartAxis.X.name(), //
			ChartAxis.Y.name(), //
			ChartAxis.Z.name() //
	};

	/**
	 * 
	 */
	private static final long serialVersionUID = -5915474863487200895L;

	private GraphicWidgetType type;

	private ComboBox<String> axis;
	private ComboBox<String> markerReference;
	private ComboBox<String> chartType;
	private ComboBox<String> markerSymbol;
	private ComboBox<String> dashStyle;
	private Checkbox autoscale;
	private ColorField colorPicker;
	private MeasureRangeField extremesField;
	private GridLayout gridLayout;


	public ChartPlotOptionsField() {
		super(null);
		type = GraphicWidgetType.MULTI_TRACE;

		binder = new Binder<>(ChartPlotOptions.class);

		gridLayout = getContent();
		extremesField = new MeasureRangeField();
		markerReference = createMarkerReference();
		chartType = createChartType();
		markerSymbol = createMarkerSymbol();
		dashStyle = createDashStyle();
		colorPicker = createColorPicker();
		axis = createAxis();

		// Bind ComboBox fields to ChartPlotOptions properties
		binder.forField(axis).bind("axis");
		binder.forField(chartType).bind("chartType");
		binder.forField(markerSymbol).bind("markerSymbol");
		binder.forField(dashStyle).bind("dashStyle");
		binder.forField(markerReference).bind("feedReference");

		// Add value change listeners to update current value
		axis.addValueChangeListener(event -> updateCurrentValue());
		chartType.addValueChangeListener(event -> {
			updateCurrentValue();
			handleVisible();
		});
		markerSymbol.addValueChangeListener(event -> {
			updateCurrentValue();
			handleVisible();
		});
		dashStyle.addValueChangeListener(event -> updateCurrentValue());
		markerReference.addValueChangeListener(event -> updateCurrentValue());
		
		// Add value change listener for color picker
		colorPicker.addValueChangeListener(event -> updateCurrentValue());

		autoscale = new Checkbox();
		autoscale.setLabel(getI18nLabel("autoscale"));

		autoscale.addValueChangeListener(event -> {

			boolean visible = !event.getValue();
			if (!visible) {
				extremesField.setValue(new MeasureRange());
			}
			extremesField.setVisible(visible);

		});

		extremesField.addValueChangeListener(event -> {

			MeasureRange range = event.getValue();
			if (range.isValid()) {
				getValue().setExtremes(range);
			} else {
				getValue().setExtremes(null);
			}

		});

//		autoscale.addValueChangeListener(new ValueChangeListener() {
//			@Override
//			public void valueChange(com.vaadin.v7.data.Property.ValueChangeEvent event) {
//				boolean visible = !((Boolean) ((Checkbox) event.getProperty()).getValue());
//				if (!visible) {
//					extremesField.setValue(new MeasureRange());
//				}
//				extremesField.setVisible(visible);
//			}
//		});
//
//		extremesField.addValueChangeListener(new Property.ValueChangeListener() {
//			@Override
//			public void valueChange(com.vaadin.v7.data.Property.ValueChangeEvent event) {
//				MeasureRange range = ((MeasureRangeField) event.getProperty()).getValue();
//				if (range.isValid()) {
//					getValue().setExtremes(range);
//				} else {
//					getValue().setExtremes(null);
//				}
//			}
//		});

		setEnabled(false);

	}

	@Override
	public void setWidth(float width, Unit unit) {
		if (gridLayout != null) {
			axis.setWidth(width, unit);
			colorPicker.setWidth(width, unit);
			dashStyle.setWidth(width, unit);
			markerSymbol.setWidth(width, unit);
			chartType.setWidth(width, unit);
			extremesField.setWidth(width, unit);
			markerReference.setWidth(width, unit);
			gridLayout.setWidth(width, unit);
		}

		super.setWidth(width, unit);
	}

	public void buildContent() {
		switch (type) {
		case LAST_MEASURE_TABLE:
			break;
		case EMBEDDED:
		case LAST_MEASURE:
			gridLayout.setRows(1);
			gridLayout.setColumns(1);
			gridLayout.setSpacing(true);
			gridLayout.setPadding(true);
			gridLayout.addComponent(colorPicker, 0, 0);
			break;
		case WIND_ROSE:
			/**
			 * preset compatible values
			 */
			chartType.setValue(ChartType.COLUMN.toString());
			// markerSymbol.setValue(CustomMarkerSymbolEnum.ARROW.name());
			markerReference.setEnabled(true);
			gridLayout.setRows(1);
			gridLayout.setColumns(1);
			gridLayout.setSpacing(true);
			gridLayout.setPadding(true);
			gridLayout.addComponent(markerReference, 0, 0);
			break;

		case HISTOGRAM:
			/**
			 * preset compatible values
			 */
			chartType.setValue(ChartType.COLUMN.toString());
			gridLayout.setRows(1);
			gridLayout.setColumns(1);
			gridLayout.setPadding(true);
			gridLayout.setSpacing(true);
			gridLayout.addComponent(colorPicker, 0, 0);
			break;

		case CUSTOM:
		case MULTI_TRACE:
			gridLayout.setRows(3);
			gridLayout.setColumns(3);
			gridLayout.setColumnExpandRatio(0, 0.33f);
			gridLayout.setColumnExpandRatio(1, 0.33f);
			gridLayout.setColumnExpandRatio(2, 0.33f);
			gridLayout.setPadding(true);
			gridLayout.setSpacing(true);

			gridLayout.addComponent(chartType, 0, 0);
			gridLayout.addComponent(dashStyle, 1, 0);
			gridLayout.addComponent(colorPicker, 2, 0);

			gridLayout.addComponent(markerSymbol, 0, 1);
			gridLayout.addComponent(markerReference, 1, 1);

			gridLayout.addComponent(autoscale, 0, 2);
			gridLayout.addComponent(extremesField, 1, 2, 2, 2);

			gridLayout.setComponentAlignment(autoscale, Alignment.MIDDLE_LEFT);

			break;
		default:
			gridLayout = new GridLayout(1, 1);
			break;
		}

	}

	@Override
	public void setEnabled(boolean enabled) {
		markerReference.setEnabled(enabled);
		chartType.setEnabled(enabled);
		markerSymbol.setEnabled(enabled);
		dashStyle.setEnabled(enabled);
		colorPicker.setEnabled(enabled);
		axis.setEnabled(enabled);
		autoscale.setEnabled(enabled);
		// if (enabled) {
		// extremesField.setEnabled(!autoScale.getValue());
		// }
		// else {
		// extremesField.setEnabled(enabled);
		// }
		super.setEnabled(enabled);
	}

	// @Override
//	public void validate() throws InvalidValueException {
//		if (!autoscale.getValue() && (!extremesField.getValue().isValid())) {
//			throw new Validator.EmptyValueException(getI18nLabel("invalid_extremes"));
//		}
//	}

	@Override
	public ChartPlotOptions getValue() {
		if (currentValue == null) {
			return null;
		}
		if (!currentValue.getMarkerSymbol().equals(CustomMarkerSymbolEnum.ARROW.name())) {
			currentValue.setFeedReference(null);
		}
		MeasureRange range = extremesField.getValue();
		if (!range.isNull() && !autoscale.getValue()) {
			currentValue.setExtremes(range);
		} else {
			currentValue.setExtremes(null);
		}
		return currentValue;
	}

	private void updateCurrentValue() {
		try {
			ChartPlotOptions bean = new ChartPlotOptions();
			binder.writeBean(bean);
			// Copy additional fields not bound by binder
			bean.setFillColor(colorPicker.getValue());
			MeasureRange range = extremesField.getValue();
			if (!range.isNull() && !autoscale.getValue()) {
				bean.setExtremes(range);
			} else {
				bean.setExtremes(null);
			}
			this.currentValue = bean;
		} catch (ValidationException e) {
			// Handle validation errors
		}
	}

	@Override
	protected void setPresentationValue(ChartPlotOptions value) {
		if (value != null && value.getFillColor() == null) {
			value.setFillColor(ChartUtils.quiteRandomHexColor());
		}
		this.currentValue = value;
		if (value == null) {
			value = new ChartPlotOptions();
			this.currentValue = value;
		}
		binder.setBean(value);
		
		colorPicker.setValue(value.getFillColor());
		if (value.getExtremes() == null) {
			autoscale.setValue(true);
		}
		extremesField.setValue(value.getExtremes());
		handleVisible();
	}

	private void handleVisible() {
		axis.setVisible(false);
		switch (type) {
		case LAST_MEASURE:
		case EMBEDDED:
			colorPicker.setVisible(true);
			markerReference.setRequiredIndicatorVisible(false);
			markerReference.setVisible(false);
			chartType.setVisible(false);
			dashStyle.setVisible(false);
			markerSymbol.setVisible(false);
			break;

		case WIND_ROSE:

			markerReference.setRequiredIndicatorVisible(true);
			markerReference.setVisible(true);
			colorPicker.setVisible(true);
			chartType.setVisible(false);
			dashStyle.setVisible(false);
			markerSymbol.setVisible(false);
			break;

		case HISTOGRAM:
			colorPicker.setVisible(true);
			markerReference.setRequiredIndicatorVisible(false);
			markerReference.setVisible(false);
			chartType.setVisible(false);
			dashStyle.setVisible(false);
			markerSymbol.setVisible(false);
			break;

		case CUSTOM:
		case MULTI_TRACE:
			boolean column = getValue().getChartType().equals(ChartType.COLUMN.toString());
			boolean arrow = getValue().getMarkerSymbol().equals(CustomMarkerSymbolEnum.ARROW.name());
			markerReference.setVisible(arrow);
			markerReference.setRequiredIndicatorVisible(arrow);

			if (column) {
				dashStyle.setVisible(false);
				markerSymbol.setVisible(false);
				markerReference.setVisible(false);
			} else {

				markerSymbol.setVisible(true);
				dashStyle.setVisible(true);
				markerReference.setVisible(arrow);
			}

			break;

		default:
			break;
		}

	}

	@SuppressWarnings("serial")
	private ColorField createColorPicker() {
		ColorField colorPicker = new ColorField();
		colorPicker.setLabel(getI18nLabel("colorpicker"));
		return colorPicker;
	}

	@SuppressWarnings("serial")
	private ComboBox<String> createDashStyle() {
		ComboBox<String> combo = new ComboBox<>();
		combo.setItems(DASH_STYLES);
		combo.setLabel(getI18nLabel("dashstyle"));
		combo.setEmptySelectionAllowed(false);
		combo.setTextInputAllowed(false);
		combo.setLabelCaptionGenerator(item -> getI18nLabel(item));
		return combo;
	}

	private ComboBox<String> createMarkerSymbol() {
		ComboBox<String> combo = new ComboBox<>();
		combo.setItems(MARKER_SYMBOLS);
		combo.setLabel(getI18nLabel("markersymbol"));
		combo.setEmptySelectionAllowed(false);
		combo.setTextInputAllowed(false);
		combo.setLabelCaptionGenerator(item -> getI18nLabel(item));
		return combo;
	}

	@SuppressWarnings("serial")
	private ComboBox<String> createChartType() {
		ComboBox<String> combo = new ComboBox<>();
		combo.setItems(CHART_TYPES);
		combo.setLabel(getI18nLabel("charttype"));
		combo.setEmptySelectionAllowed(false);
		combo.setTextInputAllowed(false);
		combo.setLabelCaptionGenerator(item -> getI18nLabel(item));
		return combo;
	}

	@SuppressWarnings("serial")
	private ComboBox<String> createAxis() {
		ComboBox<String> combo = new ComboBox<>();
		combo.setItems(AXIS);
		combo.setLabel(getI18nLabel("axis"));
		combo.setEmptySelectionAllowed(false);
		combo.setTextInputAllowed(false);
		return combo;
	}

	@SuppressWarnings("serial")
	private ComboBox<String> createMarkerReference() {
		ComboBox<String> combo = new ComboBox<>();
		combo.setEmptySelectionAllowed(true);
		combo.setLabel(getI18nLabel("markerreference"));
		combo.setTextInputAllowed(false);
		return combo;
	}

	public static boolean isSpeed(Channel channel) {
		if (channel.getDefaultMeasure().getType().equals(SPEED_MEASURE_UNIT)) {
			return true;
		}
		return false;
	}

	public static boolean isDirection(Channel channel) {
		if (channel.getDefaultMeasure().getType().equals(DEGREE_MEASURE_UNIT)) {
			return true;
		}
		/*
		 * Feature #231 Gestione dell'attributo "code sensor" nei parametri dello
		 * strumento.
		 */
		return Arrays.asList(DIRECTION_SENSORS).contains(channel.getConfiguration().getSensor());
	}

	public void setChannel(Channel channel) {
		List<String> ids = new ArrayList<String>();
		List<String> names = new ArrayList<String>();
		if (channel != null) {
			// int i = 0;
			String serial = channel.getDevice().getSerial();
			for (Channel chnl : channel.getDevice().getChannels()) {
				if (!chnl.equals(channel)) {
					if (isDirection(chnl)) {
						ids.add(serial + "." + chnl.getUniqueKey());
						names.add(chnl.toString());
						// i++;
					}
				}
			}
			setEnabled(true);
		} else {
			setEnabled(false);
		}

		String[] _ids = new String[ids.size()];
		ids.toArray(_ids);
		String[] _names = new String[names.size()];
		names.toArray(_names);

		markerReference.setItems(_ids);
		markerReference.setLabelCaptionGenerator(id -> {
			for (int i = 0; i < _ids.length; i++) {
				if (_ids[i].equals(id)) {
					return _names[i];
				}
			}
			return id;
		});

		markerReference.setEmptySelectionAllowed(false);

		if (_ids.length > 0)
			markerReference.setValue(_ids[0]);
		if (getValue() != null && getValue().getFeedReference() != null) {
			markerReference.setValue(getValue().getFeedReference());
		}

		boolean visible = getValue().getMarkerSymbol().equals(CustomMarkerSymbolEnum.ARROW.name());
		markerReference.setVisible(visible);

	}

	@Override
	protected GridLayout initContent() {
		return new GridLayout();
	}

	public String getI18nLabel(String key) {
		return UIUtils.localize(getI18nKey() + "." + key.toLowerCase());
	}

	public String getI18nKey() {
		return I18NKEY;
	}

	public void setGraphWidgetType(GraphicWidgetType graphWidgetType) {
		this.type = graphWidgetType;
	}

}
