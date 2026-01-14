package it.thisone.iotter.ui.common.fields;

import java.text.DecimalFormatSymbols;

import com.vaadin.flow.component.AbstractCompositeField;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import it.thisone.iotter.enums.Order;
import it.thisone.iotter.persistence.model.ExportingConfig;
import it.thisone.iotter.ui.common.UIUtils;


public class ExportingConfigField extends AbstractCompositeField<FormLayout, ExportingConfigField, ExportingConfig>  {

	private static final long serialVersionUID = -1174408782444376617L;
	private static final String i18nkey = "export.dialog";
	
	private Binder<ExportingConfig> binder;
	
	private RadioButtonGroup<Order> orderingField;
	private ComboBox<String> decimalSeparatorField;
	private ComboBox<String> columnSeparatorField;
	
	private TextField customSeparatorField;
	private TextField localeField;
	private TextField timeZoneField;
	
	public ExportingConfigField() {
		super(null);
		
		FormLayout fieldLayout = getContent();


		String[] separators = new String[] { ",", ";", "\t" };
		columnSeparatorField = new ComboBox<>();
		columnSeparatorField.setItems(separators);
		columnSeparatorField.setItemLabelGenerator(item -> {
			switch (item) {
				case ",": return ",";
				case ";": return ";";
				case "\t": return "tab";
				default: return item;
			}
		});
		
		columnSeparatorField.setLabel(getI18nLabel("column_separator"));

		String[] decimals = new String[] { ".", "," };
		decimalSeparatorField = new ComboBox<>();
		decimalSeparatorField.setItems(decimals);

		decimalSeparatorField.setLabel(getI18nLabel("decimal_separator"));		
		orderingField = createOrderRadioButtonGroup();		
		orderingField.setLabel(getI18nLabel("export_order"));		
		
		customSeparatorField = new TextField(getI18nLabel("custom_separator"));
		customSeparatorField.setPlaceholder("");
		customSeparatorField.setMaxLength(1);
		
		localeField = new TextField(getI18nLabel("locale"));
		localeField.setPlaceholder("");
		
		timeZoneField = new TextField(getI18nLabel("timeZone"));
		timeZoneField.setPlaceholder("");
		
		fieldLayout.add(orderingField,columnSeparatorField,customSeparatorField,decimalSeparatorField);
		
		
		binder = new Binder<>(ExportingConfig.class);
		binder.forField(orderingField).bind(ExportingConfig::getOrdering, ExportingConfig::setOrdering);
		binder.forField(columnSeparatorField).bind(ExportingConfig::getColumnSeparator, ExportingConfig::setColumnSeparator);
		binder.forField(customSeparatorField).bind(ExportingConfig::getCustomSeparator, ExportingConfig::setCustomSeparator);
		binder.forField(decimalSeparatorField).bind(ExportingConfig::getDecimalSeparator, ExportingConfig::setDecimalSeparator);
		binder.forField(localeField).bind(ExportingConfig::getLocale, ExportingConfig::setLocale);
		binder.forField(timeZoneField).bind(ExportingConfig::getTimeZone, ExportingConfig::setTimeZone);

		
	}
	

	@Override
	protected void setPresentationValue(ExportingConfig newValue) {
		if (newValue == null) {
			newValue = createDefaultExportingConfig();
		}
		binder.setBean(newValue);
	}

	@Override
	public ExportingConfig getValue() {
		return binder.getBean();
	}
	
	private ExportingConfig createDefaultExportingConfig() {
		ExportingConfig config = new ExportingConfig();
		config.setOrdering(Order.ASCENDING);
		config.setCustomSeparator("");
		config.setColumnSeparator(";");
		String decimalSeparator = new String(new char[] { DecimalFormatSymbols.getInstance().getDecimalSeparator() });
		config.setDecimalSeparator(decimalSeparator);
		config.setTimeZone(UIUtils.getBrowserTimeZone().getID());
		config.setLocale(UIUtils.getLocale().toLanguageTag());
		return config;
	}
	

	
	public String getI18nKey() {
		return i18nkey;
	}
	public String getI18nLabel(String key) {
		return UIUtils.localize(getI18nKey()  + "." + key);
	}
	
	private RadioButtonGroup<Order> createOrderRadioButtonGroup() {
		RadioButtonGroup<Order> optionGroup = new RadioButtonGroup<>();
		optionGroup.setItems(Order.values());

		optionGroup.setRenderer(new ComponentRenderer<Span,Order>(order ->
        new Span(UIUtils.localize(order.getI18nKey()))
				));

//	    optionGroup.setItemLabelGenerator(
//	            literal -> UIUtils.localize(literal.getI18nKey())
//	        );

		return optionGroup;
	}
}
