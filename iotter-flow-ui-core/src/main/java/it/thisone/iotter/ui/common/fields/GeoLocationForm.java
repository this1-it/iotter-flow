package it.thisone.iotter.ui.common.fields;

import org.vaadin.firitin.form.AbstractForm;
import org.vaadin.firitin.components.formlayout.VFormLayout;

import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.validator.DoubleRangeValidator;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.textfield.TextField;

import it.thisone.iotter.persistence.model.GeoLocation;
import it.thisone.iotter.ui.common.UIUtils;

public class GeoLocationForm extends AbstractForm<GeoLocation> { 

	private static final String name = "geolocation.field";
	private static final long serialVersionUID = -2540220106323180054L;

	
	private final TextField address;

	private final TextField latitude;

	private final TextField longitude;

	
	public GeoLocationForm() {
		super(GeoLocation.class);
		address = new TextField(getI18nLabel("address"));
		address.setWidth(100, Unit.PERCENTAGE);

		latitude = new TextField(getI18nLabel("latitude"));
		latitude.setWidth(100, Unit.PERCENTAGE);
		latitude.setRequiredIndicatorVisible(true);
		
		longitude = new TextField(getI18nLabel("longitude"));
		longitude.setWidth(100, Unit.PERCENTAGE);
		longitude.setRequiredIndicatorVisible(true);

		bindFields();
	}

	private void bindFields() {
		getBinder().forField(address)
			.bind(GeoLocation::getAddress, GeoLocation::setAddress);

		getBinder().forField(latitude)
			.asRequired(UIUtils.localize("validators.fieldgroup_errors"))
			.withConverter(new StringToDoubleConverter(getI18nLabel("invalid_latitude")))
			.withValidator(new DoubleRangeValidator(getI18nLabel("invalid_latitude"), -90d, 90d))
			.bind(GeoLocation::getLatitude, GeoLocation::setLatitude);

		getBinder().forField(longitude)
			.asRequired(UIUtils.localize("validators.fieldgroup_errors"))
			.withConverter(new StringToDoubleConverter(getI18nLabel("invalid_longitude")))
			.withValidator(new DoubleRangeValidator(getI18nLabel("invalid_longitude"), -180d, 180d))
			.bind(GeoLocation::getLongitude, GeoLocation::setLongitude);
	}

	public String getI18nLabel(String key) {
		return UIUtils.localize(getI18nKey() + "." + key);
	}

	public String getI18nKey() {
		return name;
	}


	@Override
	protected Component createContent() {
		return new VFormLayout(
				getFormLayout(),
				getToolbar()).withMargin(true);
	}

	protected Component getFormLayout() {
		return new VFormLayout(address, latitude, longitude).withMargin(true).withWidth(null);
	}

}
