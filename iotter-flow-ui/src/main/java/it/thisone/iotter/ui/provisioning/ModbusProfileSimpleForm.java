package it.thisone.iotter.ui.provisioning;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.vaadin.firitin.form.AbstractForm;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.validator.IntegerRangeValidator;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.persistence.model.ModbusProfile;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.main.UiConstants;
import it.thisone.iotter.util.PopupNotification;
import it.thisone.iotter.util.PopupNotification.Type;

public class ModbusProfileSimpleForm extends AbstractForm<ModbusProfile> {
	private static final float SMALL_WIDTH = 8f;
	private static final long serialVersionUID = 1L;

	@PropertyId("configuration.slaveName")
	private TextField slaveName;

	@PropertyId("configuration.slaveID")
	private TextField slaveID;

	@PropertyId("configuration.sampleRate")
	private TextField sampleRate;

	@PropertyId("configuration.speed")
	private ComboBox<String> speed;

	@PropertyId("configuration.dataBits")
	private ComboBox<String> dataBits;

	@PropertyId("configuration.parity")
	private ComboBox<String> parity;

	@PropertyId("configuration.stopBits")
	private ComboBox<String> stopBits;

	@PropertyId("configuration.protocol")
	private ComboBox<String> protocol;

	@PropertyId("configuration.host")
	private TextField host;

	@PropertyId("configuration.port")
	private TextField port;

	private List<String> otherSlaveNames = Collections.emptyList();
	private List<Integer> otherSlaveIds = Collections.emptyList();

	public ModbusProfileSimpleForm() {
		super(ModbusProfile.class);
		initializeFields();
		bindFields();
	}

	@Override
	protected Component createContent() {
		HorizontalLayout toolbar = getToolbar();
		VerticalLayout content = new VerticalLayout(getFormLayout(), toolbar);
		content.setPadding(true);
		content.setSpacing(true);
		content.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, toolbar);
		return content;
	}

	protected Component getFlowLayout() {
		sampleRate.setEnabled(true);
		slaveName.setEnabled(false);
		slaveID.setEnabled(false);
		protocol.setEnabled(false);
		port.setEnabled(false);
		host.setEnabled(false);
		speed.setEnabled(false);
		dataBits.setEnabled(false);
		parity.setEnabled(false);
		stopBits.setEnabled(false);
		HorizontalLayout layout = new HorizontalLayout(sampleRate, speed, dataBits, parity, stopBits, protocol, port, host);
		layout.setPadding(false);
		layout.setSpacing(true);
		return layout;
	}

	protected Component getFormLayout() {
		sampleRate.setEnabled(true);
		slaveName.setEnabled(true);
		slaveID.setEnabled(true);
		protocol.setEnabled(true);
		port.setEnabled(true);
		host.setEnabled(true);
		speed.setEnabled(true);
		dataBits.setEnabled(true);
		parity.setEnabled(true);
		stopBits.setEnabled(true);

		FormLayout formLayout = new FormLayout();
		formLayout.add(slaveName, slaveID, sampleRate, protocol, port, host, speed, dataBits, parity, stopBits);
		formLayout.setWidth("400px");
		formLayout.getStyle().set("padding", "var(--lumo-space-m)");
		return formLayout;
	}

	@Override
	public void setEntity(ModbusProfile entity) {
		super.setEntity(entity);
		if (entity != null) {
			changeProtocol(entity.getConfiguration().getProtocol());
		}
	}

	public ModbusProfileSimpleForm setValidations(List<String> others, List<Integer> otherIds) {
		otherSlaveNames = others != null ? others : Collections.emptyList();
		otherSlaveIds = otherIds != null ? otherIds : Collections.emptyList();
		return this;
	}

	public boolean commit() {
		if (getEntity() == null) {
			return true;
		}
		try {
			getBinder().writeBean(getEntity());
			return true;
		} catch (ValidationException e) {
			PopupNotification.show(getTranslation("validators.fieldgroup_errors"),
					Type.WARNING);
			return false;
		}
	}

	public TextField getSlaveName() {
		return slaveName;
	}

	public String getI18nLabel(String key) {
		return getTranslation(UiConstants.PROVISIONING + "." + key);
	}

	private void initializeFields() {
		slaveID = new TextField(getI18nLabel("configuration.slaveID"));
		slaveID.setRequiredIndicatorVisible(true);
		slaveID.setWidth(SMALL_WIDTH + "em");

		slaveName = new TextField(getI18nLabel("configuration.slaveName"));
		slaveName.setRequiredIndicatorVisible(true);
		slaveName.setWidth(SMALL_WIDTH + "em");

		sampleRate = new TextField(getI18nLabel("configuration.sampleRate"));
		sampleRate.setRequiredIndicatorVisible(true);
		sampleRate.setWidth(SMALL_WIDTH + "em");

		speed = createComboBox(getI18nLabel("configuration.speed"));
		speed.setItems(Arrays.asList(Constants.Provisioning.SPEED));
		speed.setWidth(SMALL_WIDTH + "em");

		dataBits = createComboBox(getI18nLabel("configuration.dataBits"));
		dataBits.setItems(Arrays.asList(Constants.Provisioning.DATA_BITS));
		dataBits.setWidth(SMALL_WIDTH + "em");

		parity = createComboBox(getI18nLabel("configuration.parity"));
		parity.setItems(Arrays.asList(Constants.Provisioning.PARITY));
		parity.setWidth(SMALL_WIDTH + "em");

		stopBits = createComboBox(getI18nLabel("configuration.stopBits"));
		stopBits.setItems(Arrays.asList(Constants.Provisioning.STOP_BITS));
		stopBits.setWidth(SMALL_WIDTH + "em");

		host = new TextField(getI18nLabel("configuration.host"));
		host.setWidth(SMALL_WIDTH + "em");

		port = new TextField(getI18nLabel("configuration.port"));
		port.setRequiredIndicatorVisible(true);
		port.setWidth(SMALL_WIDTH + "em");

		protocol = createComboBox(getI18nLabel("configuration.protocol"));
		protocol.setItems(Arrays.asList(Constants.Provisioning.PROTOCOL));
		protocol.setWidth(SMALL_WIDTH + "em");
		protocol.addValueChangeListener(event -> changeProtocol(event.getValue()));
	}

	private void bindFields() {
		getBinder().forField(slaveID)
				.asRequired(getTranslation("validators.fieldgroup_errors"))
				.withConverter(new StringToIntegerConverter(getTranslation("validators.conversion_error")))
				.withValidator(new IntegerRangeValidator(getI18nLabel("invalid_slaveID"), 0, 50000))
				.withValidator(value -> otherSlaveIds.isEmpty() || !otherSlaveIds.contains(value),
						getI18nLabel("not_unique_slaveID"))
				.bind(profile -> profile.getConfiguration().getSlaveID(),
						(profile, value) -> profile.getConfiguration().setSlaveID(value));

		getBinder().forField(slaveName)
				.asRequired(getTranslation("validators.fieldgroup_errors"))
				.withValidator(value -> value != null && value.matches("^[a-zA-Z0-9_]*$"),
						getI18nLabel("invalid_slaveName"))
				.withValidator(value -> otherSlaveNames.isEmpty() || !otherSlaveNames.contains(value),
						getI18nLabel("not_unique_slaveName"))
				.bind(profile -> profile.getConfiguration().getSlaveName(),
						(profile, value) -> profile.getConfiguration().setSlaveName(value));

		getBinder().forField(sampleRate)
				.asRequired(getTranslation("validators.fieldgroup_errors"))
				.withConverter(new StringToIntegerConverter(getTranslation("validators.conversion_error")))
				.withValidator(new IntegerRangeValidator(getI18nLabel("invalid_sampleRate"), 10, 86400))
				.bind(profile -> profile.getConfiguration().getSampleRate(),
						(profile, value) -> profile.getConfiguration().setSampleRate(value));

		getBinder().forField(protocol)
				.bind(profile -> profile.getConfiguration().getProtocol(),
						(profile, value) -> profile.getConfiguration().setProtocol(value));

		getBinder().forField(port)
				.withValidator(value -> !isTcp(protocol.getValue()) || (value != null && !value.trim().isEmpty()),
						getTranslation("validators.fieldgroup_errors"))
				.bind(profile -> profile.getConfiguration().getPort(),
						(profile, value) -> profile.getConfiguration().setPort(value));

		getBinder().forField(host)
				.withValidator(value -> !isTcp(protocol.getValue()) || (value != null && !value.trim().isEmpty()),
						getTranslation("validators.fieldgroup_errors"))
				.bind(profile -> profile.getConfiguration().getHost(),
						(profile, value) -> profile.getConfiguration().setHost(value));

		getBinder().forField(speed)
				.bind(profile -> profile.getConfiguration().getSpeed(),
						(profile, value) -> profile.getConfiguration().setSpeed(value));

		getBinder().forField(dataBits)
				.bind(profile -> profile.getConfiguration().getDataBits(),
						(profile, value) -> profile.getConfiguration().setDataBits(value));

		getBinder().forField(parity)
				.bind(profile -> profile.getConfiguration().getParity(),
						(profile, value) -> profile.getConfiguration().setParity(value));

		getBinder().forField(stopBits)
				.bind(profile -> profile.getConfiguration().getStopBits(),
						(profile, value) -> profile.getConfiguration().setStopBits(value));
	}

	private ComboBox<String> createComboBox(String caption) {
		ComboBox<String> combo = new ComboBox<>(caption);
		combo.setClearButtonVisible(false);
		combo.setAllowCustomValue(false);
		return combo;
	}

	private void changeProtocol(String value) {
		boolean tcp = "ETHERNET".equals(value) || "MODBUSTCP".equals(value);
		host.setVisible(tcp);
		port.setVisible(tcp);
		host.setRequiredIndicatorVisible(tcp);
		port.setRequiredIndicatorVisible(tcp);

		speed.setVisible(!tcp);
		dataBits.setVisible(!tcp);
		stopBits.setVisible(!tcp);
		parity.setVisible(!tcp);
	}

	private boolean isTcp(String value) {
		return "ETHERNET".equals(value) || "MODBUSTCP".equals(value);
	}
}
