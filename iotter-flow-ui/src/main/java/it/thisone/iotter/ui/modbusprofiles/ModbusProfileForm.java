package it.thisone.iotter.ui.modbusprofiles;

import java.io.StringWriter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;

import it.thisone.iotter.enums.modbus.TemplateState;
import it.thisone.iotter.persistence.model.MeasureUnitType;
import it.thisone.iotter.persistence.model.ModbusProfile;
import it.thisone.iotter.persistence.model.ModbusRegister;
import it.thisone.iotter.persistence.model.ResourceData;
import it.thisone.iotter.persistence.service.MeasureUnitTypeService;
import it.thisone.iotter.persistence.service.ModbusProfileService;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.fields.EditableResourceData;
import it.thisone.iotter.ui.common.fields.TemplateStateSelect;
import it.thisone.iotter.ui.modbusregisters.ModbusRegisterGrid;
import it.thisone.iotter.ui.provisioning.IProvisioningProvider;
import it.thisone.iotter.ui.provisioning.ProvisioningFactory;
import it.thisone.iotter.util.BacNet;
import it.thisone.iotter.util.PopupNotification;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ModbusProfileForm extends AbstractBaseEntityForm<ModbusProfile> {

	private static final long serialVersionUID = 1L;
	private static final String NAME = "modbus_profile.editor";

	private boolean create;
	private TextArea showLog;
	private ModbusRegisterGrid registers;
	private VerticalLayout fieldsLayout;
	private Registration resourceValueChangeRegistration;
	private EditableResourceData upload;
	private boolean fieldsInitialized;

	private TextField displayName;
	private TextField revision;
	private TemplateStateSelect state;
	
	@Autowired
	private MeasureUnitTypeService measureUnitTypeService;

	@Autowired
	private ModbusProfileService modbusProfileService;
	
	
	public ModbusProfileForm(ModbusProfile entity) {
		super(entity, ModbusProfile.class, NAME, null, null, false);
		ensureFieldsInitialized();
		getBinder().readBean(entity);
		create = isCreateBean();
	}

	@Override
	public void setEntity(ModbusProfile entity) {
		super.setEntity(entity);
		if (entity != null) {
			getBinder().readBean(entity);
		}
		create = isCreateBean();
	}

	@Override
	protected void initializeFields() {
		ensureFieldsInitialized();
	}

	@Override
	protected void bindFields() {
		// Fields are bound once in ensureFieldsInitialized().
	}

	private void initializeFieldsInternal() {
		upload = new EditableResourceData();
		upload.getResourceLabel().setText(getI18nLabel("data"));

		displayName = new TextField(getI18nLabel("displayName"));
		displayName.setWidthFull();
		displayName.setReadOnly(true);

		revision = new TextField(getI18nLabel("revision"));
		revision.setWidthFull();
		revision.setReadOnly(true);

		state = new TemplateStateSelect();
		state.setWidthFull();
		state.setLabel(getI18nLabel("state"));
	}

	private void registerFields() {
		addField("data", upload);
		addField("displayName", displayName);
		addField("revision", revision);
		addField("state", state);
	}

	private void bindFieldsInternal() {
		getBinder().forField(displayName)
				.bind(ModbusProfile::getDisplayName, ModbusProfile::setDisplayName);

		getBinder().forField(revision)
				.bind(ModbusProfile::getRevision, ModbusProfile::setRevision);

		getBinder().forField(state)
				.bind(ModbusProfile::getState, ModbusProfile::setState);

		getBinder().forField(upload)
				.bind(ModbusProfile::getData, ModbusProfile::setData);
	}

	@Override
	public VerticalLayout getFieldsLayout() {
		ensureFieldsInitialized();
		List<IProvisioningProvider> providers = ProvisioningFactory.findProviders();

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setPadding(true);
		mainLayout.setSpacing(true);

		create = isCreateBean();
		mainLayout.add(upload);
		upload.getElement().getStyle().set("height", "auto");
		upload.getElement().getStyle().set("min-height", "0");

		if (create) {
			state.removeItem(TemplateState.DISABLED);
			getSaveButton().setEnabled(false);
			if (!providers.isEmpty()) {
				resourceValueChangeRegistration = upload
						.addValueChangeListener(createResourceValueChangeListener(upload, providers.get(0)));
			}

			showLog = new TextArea();
			showLog.setWidthFull();
			showLog.setVisible(false);
			mainLayout.add(showLog);
			mainLayout.setFlexGrow(1f, showLog);
		}

		registers = new ModbusRegisterGrid();
		registers.setRows(getEntity().getRegisters());

		HorizontalLayout header = new HorizontalLayout();
		header.setWidthFull();
		header.setSpacing(true);
		header.add(displayName, revision, state);
		header.setFlexGrow(1f, displayName, revision, state);

		fieldsLayout = new VerticalLayout();
		fieldsLayout.setSpacing(false);
		fieldsLayout.setPadding(false);
		fieldsLayout.setWidthFull();
		fieldsLayout.add(header);

		mainLayout.add(fieldsLayout, registers);
		mainLayout.setFlexGrow(1f, registers);

		return mainLayout;
	}

	private void ensureFieldsInitialized() {
		if (fieldsInitialized) {
			return;
		}
		initializeFieldsInternal();
		registerFields();
		bindFieldsInternal();
		fieldsInitialized = true;
	}

	private HasValue.ValueChangeListener<HasValue.ValueChangeEvent<ResourceData>> createResourceValueChangeListener(
			final EditableResourceData field, final IProvisioningProvider provider) {
		return event -> importResource(event.getValue(), provider);
	}

	protected void importResource(ResourceData data, IProvisioningProvider provider) {
		if (data == null) {
			return;
		}
		fieldsLayout.setEnabled(false);
		StringWriter writer = new StringWriter();
		ModbusProfile source = provider.readProfileFromExcel(data.getFilename(), data.getData(), writer);
		if (source != null) {
			source.setState(TemplateState.DRAFT);
		}
		String result = writer.toString();
		boolean valid = source != null && !result.contains("ERROR");
		showLog.setVisible(!valid);
		fieldsLayout.setVisible(valid);
		getSaveButton().setEnabled(valid);

		if (valid) {
			setUp(source);
			if (resourceValueChangeRegistration != null) {
				resourceValueChangeRegistration.remove();
			}
			refreshItem(source);
			getBinder().readBean(source);
			resourceValueChangeRegistration = upload.addValueChangeListener(createResourceValueChangeListener(upload, provider));
			registers.setImporting(true);
			registers.setRows(getEntity().getRegisters());
			displayName.setReadOnly(true);
			revision.setReadOnly(true);
			int total = getEntity().getRegisters().size();
			String msg = getTranslation(NAME + ".imported_registers", total);
			PopupNotification.show(msg, PopupNotification.Type.HUMANIZED);
		} else {
			PopupNotification.show(getI18nLabel("has_errors"), PopupNotification.Type.ERROR);
			showLog.setValue(result);
		}
		upload.setReadOnly(true);
		fieldsLayout.setEnabled(true);
	}

	private void setUp(ModbusProfile profile) {
		for (ModbusRegister register : profile.getRegisters()) {
			if (!(register.getMeasureUnit() < 0)) {
				if (measureUnitTypeService
						.findByCode(register.getMeasureUnit()) == null) {
					String name = BacNet.lookUp(register.getMeasureUnit());
					MeasureUnitType type = new MeasureUnitType();
					type.setCode(register.getMeasureUnit());
					type.setName(name);
					measureUnitTypeService.create(type);
				}
			}
		}

	}

	@Override
	protected void beforeCommit() throws EditorConstraintException {
		if (create) {
			ModbusProfile template = modbusProfileService
					.findOne(getEntity().getId());
			if (template != null) {
				throw new EditorConstraintException(getI18nLabel("duplicate_constraint"));
			}
		}
	}

	@Override
	protected void afterCommit() {
	}

	@Override
	protected void reset(ModbusProfile entity) {
		if (resourceValueChangeRegistration != null) {
			resourceValueChangeRegistration.remove();
			resourceValueChangeRegistration = null;
		}
		super.reset(entity);
	}

	public String getWindowStyle() {
		return NAME;
	}

	public float[] getWindowDimension() {
		return UIUtils.XL_DIMENSION;
	}
}
