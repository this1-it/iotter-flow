package it.thisone.iotter.ui.common.fields;

import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.component.AbstractCompositeField;
import com.vaadin.flow.component.Component;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.MeasureRange;
import it.thisone.iotter.ui.common.UIUtils;


public class MeasureRangeField extends 
AbstractCompositeField<HorizontalLayout, MeasureRangeField, MeasureRange>

 
{

	private static final long serialVersionUID = 3406308824644017551L;
	private MeasureRange currentValue;
	private static final String name = "measurerange.field";
	private Binder<MeasureRange> binder;
	private TextField upperField;
	private TextField lowerField;

	@SuppressWarnings("serial")
	public MeasureRangeField() {
		super(null);
		binder = new Binder<>(MeasureRange.class);
		upperField = new TextField(getI18nLabel("upper"));
		lowerField = new TextField(getI18nLabel("lower"));
		
		HorizontalLayout content = getContent();
		content.setSpacing(true);
		content.add(lowerField);
		content.add(upperField);

		
		binder.forField(upperField).bind("upper");
		binder.forField(lowerField).bind("lower");
		// upperField.setImmediate(true);		
		// lowerField.setImmediate(true);		
		// Null representation handled differently in Vaadin 8
		
		upperField.addValueChangeListener(event -> updateCurrentValue());
		lowerField.addValueChangeListener(event -> updateCurrentValue());

	}



    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        upperField.setEnabled(enabled);
        lowerField.setEnabled(enabled);
        super.setEnabled(enabled);
    }
	
	@Override
	protected void setPresentationValue(MeasureRange bean) {
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
	

    public String getI18nLabel(String key) {
		return getTranslation(getI18nKey()  + "." + key);
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
