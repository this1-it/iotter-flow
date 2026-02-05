package it.thisone.iotter.ui.common.fields;

import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.component.AbstractCompositeField;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.persistence.model.GeoLocation;
import it.thisone.iotter.ui.common.UIUtils;

public class GeoLocationField extends AbstractCompositeField<VerticalLayout, GeoLocationField, GeoLocation> {


	private static final long serialVersionUID = -2540220106323180054L;
	private GeoLocation currentValue;
	private static final String name = "geolocation.field";
	private Binder<GeoLocation> binder;
	private TextField latitude;
	private TextField longitude;
	private TextField address;
	private TextField elevation;

	public GeoLocationField() {
		super(null);

		binder = new Binder<>(GeoLocation.class);
		
		latitude = new TextField(getI18nLabel("latitude"));
		longitude = new TextField(getI18nLabel("longitude"));
		address = new TextField(getI18nLabel("address"));
		elevation = new TextField(getI18nLabel("elevation"));
		
		binder.forField(latitude).bind("latitude");
		binder.forField(longitude).bind("longitude");
		binder.forField(address).bind("address");
		binder.forField(elevation).bind("elevation");

		latitude.setWidth(Constants.FIELD_SIZE, Unit.EM);
		longitude.setWidth(Constants.FIELD_SIZE, Unit.EM);
		address.setWidth(Constants.FIELD_SIZE, Unit.EM);
		elevation.setWidth(Constants.FIELD_SIZE, Unit.EM);


		// Empty value representation is handled differently in Vaadin 8

		latitude.addValueChangeListener(event -> updateCurrentValue());
		longitude.addValueChangeListener(event -> updateCurrentValue());
		address.addValueChangeListener(event -> updateCurrentValue());
		elevation.addValueChangeListener(event -> updateCurrentValue());

		VerticalLayout layout = getContent();
		layout.setSpacing(true);
		layout.add(address,latitude,longitude,elevation);

	}

	// setImmediate is not needed in Vaadin 8
	


	@Override
	public void setEnabled(boolean enabled) {
		latitude.setEnabled(enabled);
		longitude.setEnabled(enabled);
		address.setEnabled(enabled);
		elevation.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	@Override
	protected void setPresentationValue(GeoLocation bean) {
		this.currentValue = bean;
		if (bean == null) {
			bean = new GeoLocation();
			this.currentValue = bean;
		}
		binder.setBean(bean);
	}

	@Override
	public GeoLocation getValue() {
		return this.currentValue;
	}

	private void updateCurrentValue() {
		try {
			GeoLocation bean = new GeoLocation();
			binder.writeBean(bean);
			this.currentValue = bean;
		} catch (ValidationException e) {
			// Handle validation errors
		}
	}


	

	public String getI18nLabel(String key) {
		return getTranslation(getI18nKey() + "." + key);
	}

	public String getI18nKey() {
		return name;
	}

}
