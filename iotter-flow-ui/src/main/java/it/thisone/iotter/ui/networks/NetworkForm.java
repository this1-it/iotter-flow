package it.thisone.iotter.ui.networks;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;

import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.fields.NetworkTypeOptionGroup;

public class NetworkForm extends AbstractBaseEntityForm<Network> {
	
	private static final long serialVersionUID = 4341036925298030121L;

	@PropertyId("name")
	private TextField name;

	@PropertyId("header")
	private TextArea header;

	
	@PropertyId("description")
	private TextArea description;

	@PropertyId("networkType")
	private NetworkTypeOptionGroup networkType;
	
	@PropertyId("timeZone")
	private ComboBox<String> timeZone;
	
	@PropertyId("concurrentUsers")
	private Component concurrentUsers;
	
	@PropertyId("anonymous")
	private Checkbox anonymous;

	private boolean fieldsInitialized;
	private boolean bindingsInitialized;
	
	public NetworkForm(Network entity) {
		this(entity, false);
	}

	public NetworkForm(Network entity, boolean readOnly) {
		super(entity, Network.class, "network.editor", null, null, readOnly);
		ensureFieldsInitialized();
		ensureBindingsInitialized();
		getBinder().readBean(entity);
	}

	@Override
	public VerticalLayout getFieldsLayout() {
		ensureFieldsInitialized();
		return super.getFieldsLayout();
	}

	private void ensureFieldsInitialized() {
		if (fieldsInitialized) {
			return;
		}
		initializeFields();
		fieldsInitialized = true;
	}

	private void ensureBindingsInitialized() {
		if (bindingsInitialized) {
			return;
		}
		bindFields();
		bindingsInitialized = true;
	}

	@Override
	protected void initializeFields() {
		name = new TextField();
		name.setSizeFull();
		name.setRequiredIndicatorVisible(true);
		name.setLabel(getI18nLabel("name"));
		name.setReadOnly(isReadOnly());
		
		header = new TextArea();
		header.setSizeFull();
		header.setLabel(getI18nLabel("header"));
		header.setReadOnly(isReadOnly());

		description = new TextArea();
		description.setSizeFull();
		description.setLabel(getI18nLabel("description"));
		description.setReadOnly(isReadOnly());
		
		TextField concurrentUsersField = new TextField();
		concurrentUsersField.setSizeFull();
		concurrentUsersField.setLabel(getI18nLabel("concurrentUsers"));
		concurrentUsersField.setReadOnly(isReadOnly());
		concurrentUsers = concurrentUsersField;

		anonymous = new Checkbox();
		anonymous.setSizeFull();
		anonymous.setLabel(getI18nLabel("anonymous"));
		anonymous.setReadOnly(isReadOnly());
		
		networkType = new NetworkTypeOptionGroup();
		networkType.setLabel(getI18nLabel("networkType"));
		networkType.setReadOnly(isReadOnly());
		
		timeZone = new ComboBox<>();
		timeZone.setSizeFull();
		timeZone.setLabel(getI18nLabel("timeZone"));
		timeZone.setItems(Arrays.asList(java.util.TimeZone.getAvailableIDs()));
		timeZone.setItemLabelGenerator(this::formatTimeZone);
		timeZone.setReadOnly(isReadOnly());
	}

	private String formatTimeZone(String zoneId) {
		java.util.TimeZone tz = java.util.TimeZone.getTimeZone(zoneId);
		SimpleDateFormat sdf = new SimpleDateFormat("XXX");
		sdf.setTimeZone(tz);
		return String.format("%s [%s]", tz.getID(), sdf.format(new Date()));
	}

	@Override
	protected void bindFields() {
		ensureFieldsInitialized();
		Binder<Network> binder = getBinder();
		String requiredMessage = getTranslation("validators.fieldgroup_errors");

		binder.forField(name)
				.asRequired(requiredMessage)
				.bind(Network::getName, Network::setName);

		binder.forField(header)
				.bind(Network::getHeader, Network::setHeader);

		binder.forField(description)
				.bind(Network::getDescription, Network::setDescription);

		TextField concurrentUsersField = (TextField) concurrentUsers;
		binder.forField(concurrentUsersField)
				.withConverter(new Converter<String, Integer>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Result<Integer> convertToModel(String value, ValueContext context) {
						if (value == null || value.trim().isEmpty()) {
							return Result.ok(null);
						}
						try {
							return Result.ok(Integer.valueOf(value.trim()));
						} catch (NumberFormatException e) {
							return Result.error(getI18nLabel("concurrentUsers"));
						}
					}

					@Override
					public String convertToPresentation(Integer value, ValueContext context) {
						return value == null ? "" : String.valueOf(value);
					}
				})
				.bind(Network::getConcurrentUsers, Network::setConcurrentUsers);

		binder.forField(anonymous)
				.bind(Network::isAnonymous, Network::setAnonymous);

		binder.forField(networkType)
				.bind(Network::getNetworkType, Network::setNetworkType);

		binder.forField(timeZone)
				.bind(Network::getTimeZone, Network::setTimeZone);
	}



	@Override
	protected void afterCommit() {
	}

	@Override
	protected void beforeCommit() throws EditorConstraintException {
		// no-op
	}

}
