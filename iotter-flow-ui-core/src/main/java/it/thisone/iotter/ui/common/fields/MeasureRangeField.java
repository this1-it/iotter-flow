package it.thisone.iotter.ui.common.fields;

import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.CustomField;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

import it.thisone.iotter.persistence.model.MeasureRange;
import it.thisone.iotter.ui.common.UIUtils;


public class MeasureRangeField extends CustomField<MeasureRange> {

	private static final long serialVersionUID = 3406308824644017551L;
	private MeasureRange currentValue;
	private static final String name = "measurerange.field";
	private Binder<MeasureRange> binder;
	private TextField upperField;
	private TextField lowerField;

	@SuppressWarnings("serial")
	public MeasureRangeField() {
		super();
		binder = new Binder<>(MeasureRange.class);
		upperField = new TextField(getI18nLabel("upper"));
		lowerField = new TextField(getI18nLabel("lower"));
		
		binder.forField(upperField).bind("upper");
		binder.forField(lowerField).bind("lower");
		// upperField.setImmediate(true);		
		// lowerField.setImmediate(true);		
		// Null representation handled differently in Vaadin 8
		
		upperField.addValueChangeListener(event -> updateCurrentValue());
		lowerField.addValueChangeListener(event -> updateCurrentValue());

	}

	@Override
	public void setWidth(float width, Unit unit) {
		if (binder != null) {
			upperField.setWidth(width, unit);
			lowerField.setWidth(width, unit);	
		}
		super.setWidth(width, unit);
	}
	
	@Override
	protected Component initContent() {
		HorizontalLayout content = new HorizontalLayout();
		content.setSpacing(true);
		content.addComponent(lowerField);
		content.addComponent(upperField);
		return content;
	}

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        upperField.setEnabled(enabled);
        lowerField.setEnabled(enabled);
        super.setEnabled(enabled);
    }
	
	@Override
	protected void doSetValue(MeasureRange bean) {
		this.currentValue = bean;
		if (bean == null) {
			bean = new MeasureRange();
			this.currentValue = bean;
		}
		binder.setBean(bean);
	}

	private void updateCurrentValue() {
		try {
			MeasureRange bean = new MeasureRange();
			binder.writeBean(bean);
			this.currentValue = bean;
		} catch (ValidationException e) {
			// Handle validation errors
		}
	}
	

	@Override
	public MeasureRange getValue() {
		return this.currentValue;
	}
	
	
	// Validation is now handled by Binder in Vaadin 8
	

    public String getI18nLabel(String key) {
		return UIUtils.localize(getI18nKey()  + "." + key);
    }

	public String getI18nKey() {
		return name;
	}

	public TextField getUpperField() {
		return upperField;
	}

	public TextField getLowerField() {
		return lowerField;
	}

}
