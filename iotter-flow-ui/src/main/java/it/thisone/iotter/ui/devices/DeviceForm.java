package it.thisone.iotter.ui.devices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vaadin.flow.components.TabSheet;

import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.component.checkbox.Checkbox;


import com.vaadin.flow.component.formlayout.FormLayout;



import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.cassandra.CassandraFeeds;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.integration.AlarmService;
import it.thisone.iotter.integration.CassandraService;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChannelComparator;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.ExportingConfig;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.GroupWidgetService;
import it.thisone.iotter.persistence.service.NetworkGroupService;
import it.thisone.iotter.persistence.service.NetworkService;
import it.thisone.iotter.persistence.service.RoleService;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.channels.ChannelAlarmListing;
import it.thisone.iotter.ui.channels.ChannelLastMeasuresListing;
import it.thisone.iotter.ui.channels.ChannelListing;
import it.thisone.iotter.ui.channels.ChannelRemoteControlListing;
import it.thisone.iotter.ui.charts.controls.CustomDateField;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChannelUtils;
import it.thisone.iotter.ui.common.fields.DeviceModelSelect;
import it.thisone.iotter.ui.common.fields.DeviceStatusSelect;
import it.thisone.iotter.ui.common.fields.ExportingConfigField;
import it.thisone.iotter.ui.common.fields.LegacyDateTimeField;
import it.thisone.iotter.ui.common.fields.NetworkGroupSelect;
import it.thisone.iotter.ui.common.fields.NetworkSelect;
import it.thisone.iotter.ui.ifc.ITabContent;
import it.thisone.iotter.util.EncryptUtils;
import it.thisone.iotter.util.Utils;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DeviceForm extends AbstractBaseEntityForm<Device> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "device.editor";

	@PropertyId("serial")
	private TextField serial;

	@PropertyId("activationKey")
	private TextField activationKey;

	@PropertyId("label")
	private TextField label;

	@PropertyId("model")
	private DeviceModelSelect model;

	@PropertyId("firmwareVersion")
	private TextField firmwareVersion;

	@PropertyId("productionDate")
	private CustomDateField productionDate;

	@PropertyId("activationDate")
	private CustomDateField activationDate;

	@PropertyId("inactivityMinutes")
	private DeviceInactivityOptionGroup inactivityMinutes;

	@PropertyId("status")
	private DeviceStatusSelect status;

	@PropertyId("lastContactDate")
	private LegacyDateTimeField lastContactDate;

	@PropertyId("readApikey")
	private TextField readApikey;

	@PropertyId("writeApikey")
	private TextField writeApikey;

	@PropertyId("publishing")
	private Checkbox publishing;

	@PropertyId("tracing")
	private Checkbox tracing;

	private NetworkSelect networkSelect;
	private NetworkGroupSelect groupsSelect;
	private NetworkGroupSelect exclusiveGroups;

	@PropertyId("location.address")
	private TextField locationAddress;

	@PropertyId("location.latitude")
	private TextField locationLatitude;

	@PropertyId("location.longitude")
	private TextField locationLongitude;

	@PropertyId("location.elevation")
	private TextField locationElevation;


	private ExportingConfigField exportingConfig;

	private ChannelLastMeasuresListing lastValues;
	private ChannelListing channelListing;
	private ChannelAlarmListing alarmListing;
	private Collection<NetworkGroup> groups = new ArrayList<NetworkGroup>();

	@Autowired
	private DeviceService deviceService;
	@Autowired
	private AlarmService alarmService;
	@Autowired
    private  NetworkService networkService;
	@Autowired
    private NetworkGroupService networkGroupService;
	@Autowired
    private GroupWidgetService groupWidgetService;
	@Autowired
    private CassandraService cassandraService;

	
	@Autowired
	public DeviceForm(Device entity, Network network,  UserDetailsAdapter currentUser, boolean readOnly) {
		super(entity, Device.class, NAME, network, currentUser, readOnly);
		if (isCreateBean()) {
			initializeDefaults();
		}
		populateFields();
		bindFields();
	}

	private void initializeDefaults() {
		getEntity().setOwner(getCurrentUser().getUsername());
		boolean sticky = getCurrentUser().getUsername().equalsIgnoreCase(Constants.ROLE_PRODUCTION);
		getEntity().setSticky(sticky);
		getEntity().setTracing(true);
		getEntity().setStatus(DeviceStatus.PRODUCED);
		getEntity().setProductionDate(new Date());
	}

	protected void initializeFields() {
		serial = new TextField();
		serial.setSizeFull();
		serial.setReadOnly(isReadOnly());
		serial.setRequiredIndicatorVisible(isCreateBean());
		serial.setLabel(getI18nLabel("serial"));
		if (!isCreateBean()) {
			serial.setReadOnly(true);
		}

		activationKey = new TextField();
		activationKey.setSizeFull();
		activationKey.setReadOnly(isReadOnly());
		activationKey.setRequiredIndicatorVisible(isCreateBean());
		activationKey.setLabel(getI18nLabel("activationKey"));
		if (!isCreateBean()) {
			activationKey.setReadOnly(true);
		}

		label = new TextField();
		label.setSizeFull();
		label.setReadOnly(isReadOnly());
		label.setRequiredIndicatorVisible(true);
		label.setLabel(getI18nLabel("label"));

		model = new DeviceModelSelect(loadDeviceModels());
		model.setSizeFull();
		model.setReadOnly(isReadOnly());
		model.setRequiredIndicatorVisible(isCreateBean());
		model.setLabel(getI18nLabel("model"));
		if (!isCreateBean()) {
			model.setReadOnly(true);
		}

		firmwareVersion = new TextField();
		firmwareVersion.setSizeFull();
		firmwareVersion.setReadOnly(isReadOnly());
		firmwareVersion.setRequiredIndicatorVisible(isCreateBean());
		firmwareVersion.setLabel(getI18nLabel("firmwareVersion"));
		if (!isCreateBean()) {
			firmwareVersion.setReadOnly(true);
		}

		productionDate = new CustomDateField(UIUtils.getBrowserTimeZone());
		productionDate.setSizeFull();
		productionDate.setReadOnly(isReadOnly());
		productionDate.setRequiredIndicatorVisible(isCreateBean());
		productionDate.setLabel(getI18nLabel("productionDate"));
		if (!isCreateBean()) {
			productionDate.setReadOnly(true);
		}

		activationDate = new CustomDateField(UIUtils.getBrowserTimeZone());
		activationDate.setSizeFull();
		activationDate.setReadOnly(isReadOnly());
		activationDate.setRequiredIndicatorVisible(isCreateBean());
		activationDate.setLabel(getI18nLabel("activationDate"));
		if (!isCreateBean()) {
			activationDate.setReadOnly(true);
		}

		inactivityMinutes = new DeviceInactivityOptionGroup();
		inactivityMinutes.setReadOnly(isReadOnly());
		inactivityMinutes.setRequiredIndicatorVisible(isCreateBean());
		inactivityMinutes.setLabel(getI18nLabel("inactivityMinutes"));

		status = new DeviceStatusSelect();
		status.setSizeFull();
		status.setReadOnly(isReadOnly());
		status.setLabel(getI18nLabel("status"));
		status.setReadOnly(true);

		lastContactDate = new LegacyDateTimeField();
		lastContactDate.setReadOnly(isReadOnly());
		lastContactDate.setReadOnly(true);

		readApikey = new TextField();
		readApikey.setSizeFull();
		readApikey.setReadOnly(isReadOnly());
		readApikey.setLabel(getI18nLabel("readApikey"));

		writeApikey = new TextField();
		writeApikey.setSizeFull();
		writeApikey.setReadOnly(isReadOnly());
		writeApikey.setLabel(getI18nLabel("writeApikey"));
		writeApikey.setReadOnly(true);

		publishing = new Checkbox();
		publishing.setSizeFull();
		publishing.setReadOnly(isReadOnly());
		publishing.setLabel(getI18nLabel("publishing"));

		tracing = new Checkbox();
		tracing.setSizeFull();
		tracing.setReadOnly(isReadOnly());
		tracing.setLabel(getI18nLabel("tracing"));

		networkSelect = new NetworkSelect(new ArrayList<>());
		networkSelect.setSizeFull();
		networkSelect.setReadOnly(isReadOnly());
		networkSelect.setLabel(getI18nLabel("network"));

		groupsSelect = new NetworkGroupSelect(new ArrayList<>(), true);
		groupsSelect.setSizeFull();
		groupsSelect.setEnabled(!isReadOnly());
		groupsSelect.setVisible(Constants.USE_GROUPS);

		exclusiveGroups = new NetworkGroupSelect(new ArrayList<>(), true);
		exclusiveGroups.setSizeFull();
		exclusiveGroups.setEnabled(!isReadOnly());
		exclusiveGroups.setVisible(Constants.USE_GROUPS);

		locationAddress = new TextField();
		locationAddress.setSizeFull();
		locationAddress.setReadOnly(isReadOnly());
		locationAddress.setLabel(getI18nLabel("location.address"));

		locationLatitude = new TextField();
		locationLatitude.setSizeFull();
		locationLatitude.setReadOnly(isReadOnly());
		locationLatitude.setLabel(getI18nLabel("location.latitude"));

		locationLongitude = new TextField();
		locationLongitude.setSizeFull();
		locationLongitude.setReadOnly(isReadOnly());
		locationLongitude.setLabel(getI18nLabel("location.longitude"));

		locationElevation = new TextField();
		locationElevation.setSizeFull();
		locationElevation.setReadOnly(isReadOnly());
		locationElevation.setLabel(getI18nLabel("location.elevation"));



		exportingConfig = new ExportingConfigField();
		exportingConfig.setReadOnly(isReadOnly());
	}


    /**
     * Initialized fields that require services (networks, groups).
     * 
     */
    private void populateFields() {
		        Set<NetworkGroup> currentGroups = getEntity().getGroups();

		        // Populate network select
        networkSelect.setItems(loadNetworks());
        // Load and configure groups
        List<NetworkGroup> availableGroups = loadNetworkGroups();
        groupsSelect.setItems(availableGroups);
        exclusiveGroups.setItems(availableGroups);
        groupsSelect.asMultiSelect().setValue(currentGroups);
	}

	private void registerFields() {
		addField("serial", serial);
		addField("activationKey", activationKey);
		addField("label", label);
		addField("model", model);
		addField("firmwareVersion", firmwareVersion);
		addField("productionDate", productionDate);
		addField("activationDate", activationDate);
		addField("inactivityMinutes", inactivityMinutes);
		addField("status", status);
		addField("lastContactDate", lastContactDate);
		addField("readApikey", readApikey);
		addField("writeApikey", writeApikey);
		addField("publishing", publishing);
		addField("tracing", tracing);
		addField("_network_", networkSelect);
		addField("groups", groupsSelect);
		addField("exclusive_groups", exclusiveGroups);
		addField("location.address", locationAddress);
		addField("location.latitude", locationLatitude);
		addField("location.longitude", locationLongitude);
		addField("location.elevation", locationElevation);

		addField("exportingConfig", exportingConfig);
	}

	protected void bindFields() {
		String requiredMessage = getTranslation("validators.fieldgroup_errors");
		getBinder().forField(serial)
				.withValidator(value -> !isCreateBean() || (value != null && !value.trim().isEmpty()),
						requiredMessage)
				.withValidator((value, context) -> {
					if (!isCreateBean() || value == null || value.trim().isEmpty()) {
						return ValidationResult.ok();
					}
					if (deviceService.findBySerial(value.trim()) != null) {
						return ValidationResult.error(getTranslation("validators.serial_unique"));
					}
					return ValidationResult.ok();
				})
				.bind(Device::getSerial, Device::setSerial);

		getBinder().forField(activationKey)
				.withValidator(value -> !isCreateBean() || (value != null && !value.trim().isEmpty()),
						requiredMessage)
				.withValidator((value, context) -> {
					if (!isCreateBean() || value == null || value.trim().isEmpty()) {
						return ValidationResult.ok();
					}
					if (deviceService.findByActivationKey(value.trim()) != null) {
						return ValidationResult.error(getTranslation("validators.activation_key_unique"));
					}
					return ValidationResult.ok();
				})
				.bind(Device::getActivationKey, Device::setActivationKey);

		getBinder().forField(label)
				.asRequired(requiredMessage)
				.bind(Device::getLabel, Device::setLabel);

		getBinder().forField(model)
				.withValidator(value -> !isCreateBean() || value != null, requiredMessage)
				.bind(Device::getModel, Device::setModel);

		getBinder().forField(firmwareVersion)
				.withValidator(value -> !isCreateBean() || (value != null && !value.trim().isEmpty()),
						requiredMessage)
				.bind(Device::getFirmwareVersion, Device::setFirmwareVersion);

		getBinder().forField(productionDate)
				.withValidator(value -> !isCreateBean() || value != null, requiredMessage)
				.bind(Device::getProductionDate, Device::setProductionDate);

		getBinder().forField(activationDate)
				.withValidator(value -> !isCreateBean() || value != null, requiredMessage)
				.bind(Device::getActivationDate, Device::setActivationDate);

		getBinder().forField(inactivityMinutes)
				.withValidator(value -> !isCreateBean() || value != null, requiredMessage)
				.bind(Device::getInactivityMinutes,
						(device, value) -> device.setInactivityMinutes(value != null ? value : 0));

		getBinder().forField(status)
				.bind(Device::getStatus, Device::setStatus);

		getBinder().forField(lastContactDate)
				.bind(Device::getLastContactDate, Device::setLastContactDate);

		getBinder().forField(readApikey)
				.bind(Device::getReadApikey, Device::setReadApikey);

		getBinder().forField(writeApikey)
				.bind(Device::getWriteApikey, Device::setWriteApikey);

		getBinder().forField(publishing)
				.bind(device -> device.isPublishing(), Device::setPublishing);

		getBinder().forField(tracing)
				.bind(device -> device.isTracing(), Device::setTracing);

		getBinder().forField(locationAddress)
				.bind(device -> device.getLocation().getAddress(),
						(device, value) -> device.getLocation().setAddress(value));

		getBinder().forField(locationLatitude)
				.withConverter(new StringToDoubleConverter(getTranslation("validators.fieldgroup_errors")))
				.bind(device -> device.getLocation().getLatitude(),
						(device, value) -> device.getLocation().setLatitude(value != null ? value : 0d));

		getBinder().forField(locationLongitude)
				.withConverter(new StringToDoubleConverter(getTranslation("validators.fieldgroup_errors")))
				.bind(device -> device.getLocation().getLongitude(),
						(device, value) -> device.getLocation().setLongitude(value != null ? value : 0d));

		getBinder().forField(locationElevation)
				.withConverter(new StringToDoubleConverter(getTranslation("validators.fieldgroup_errors")))
				.bind(device -> device.getLocation().getElevation(),
						(device, value) -> device.getLocation().setElevation(value != null ? value : 0d));



		getBinder().forField(exportingConfig)
				.bind(Device::getExportingConfig, Device::setExportingConfig);
	}

	@Override
	public VerticalLayout getFieldsLayout() {
		// Ensure fields are initialized before building the layout
		// This is necessary because getFieldsLayout() is called during super() constructor
		// before initializeFields() can be executed
		if (serial == null) {
			initializeFields();
			registerFields();

		}

		Device entity = getEntity();
		VerticalLayout mainLayout = buildMainLayout();

		TabSheet multicomponent = new TabSheet();
				multicomponent.setSizeFull();

		mainLayout.add(multicomponent);

		multicomponent.addTab(getI18nLabel("general_tab"), buildPanel(buildGeneralForm()));

		groups = new ArrayList<NetworkGroup>();
		if (!isCreateBean()) {
			Date lastContactDateValue = cassandraService.getFeeds().getLastContact(entity.getSerial());
			entity.setLastContactDate(lastContactDateValue);
			lastContactDate.setValue(lastContactDateValue);

			boolean production = getCurrentUser().hasRole(Constants.ROLE_PRODUCTION);
			boolean supervisor = getCurrentUser().hasRole(Constants.ROLE_SUPERVISOR);

			if (supervisor) {
				multicomponent.addTab(getI18nLabel("measures_tab"), buildPanel(buildMeasuresForm()));
			}

			if (groupsSelect != null && supervisor) {
				groupsSelect.setMandatoryNetwork(Constants.USE_GROUPS);
				groupsSelect.setVisible(Constants.USE_GROUPS);
				if (networkSelect.getValue() == null) {
					Network selectedNetwork = getNetwork();
					if (selectedNetwork == null) {
						selectedNetwork = entity.getNetwork();
					}
					if (selectedNetwork != null) {
						networkSelect.setValue(selectedNetwork);
					}
				}
				groupsSelect.setNetworkSelection(networkSelect, getNetwork(), entity.getNetwork());
				applyGroupSelection(entity.getGroups());
				groups = Collections.unmodifiableCollection(groupsSelect.getSelectedItems());
				exclusiveGroups.setExclusiveGroups(entity.getGroups());
				networkSelect.setReadOnly(true);
				multicomponent.addTab(getI18nLabel("associations_tab"), buildPanel(buildAssociationsForm()));
			}

			boolean hasChannels = entity.getChannels() != null && !entity.getChannels().isEmpty();
			if (hasChannels) {
				List<Channel> channels = new ArrayList<Channel>(entity.getChannels());
				if (!supervisor) {
					List<Channel> filtered = new ArrayList<>();
					for (Channel channel : channels) {
						if (!channel.isCrucial()) {
							filtered.add(channel);
						}
					}
					channels = filtered;
				}
				Collections.sort(channels, new ChannelComparator());

				if (!production) {
					multicomponent.addTab(getI18nLabel("location_tab"), buildPanel(buildLocationForm()));
				}

				channelListing = new ChannelListing(channels);
				multicomponent.addTab(getI18nLabel("channels_tab"), channelListing);

				if (entity.isAlarmed()) {
					alarmService.setUpFiredAlarms(entity);
				}

				List<Channel> alarms = new ArrayList<>();
				for (Channel channel : channels) {
					if (channel.getConfiguration().isActive()) {
						if (Utils.messageBundleId(channel.getMetaData()) != null) {
							if (ChannelUtils.isTypeAlarm(channel)) {
								alarms.add(channel);
							}
						}
					}
				}

				alarmListing = new ChannelAlarmListing(alarms);
				NetworkGroup group = deviceService.getDeviceAlarmGroup(entity);
				alarmListing.setAlarmGroup(group);
				multicomponent.addTab(getI18nLabel("alarms_tab"), alarmListing);

				ChannelRemoteControlListing remote = new ChannelRemoteControlListing(channels);
				multicomponent.addTab(getI18nLabel("remote_tab"), remote);

				lastValues = new ChannelLastMeasuresListing(channels);
				lastValues.setSizeFull();
				multicomponent.addTab(getI18nLabel("lastvalues_tab"), lastValues);

				if (supervisor) {
					DeviceRollup rollup = new DeviceRollup(entity);
					multicomponent.addTab(getI18nLabel("rollup_tab"), rollup);

				}
			}

			if (entity.isRunning() && entity.getMaster() == null) {
				multicomponent.addTab(getI18nLabel("export_tab"), buildExportTab(entity));
			}
		}

		multicomponent.addSelectedChangeListener(event -> {
			com.vaadin.flow.component.Component selected = multicomponent.getSelectedTabContents();
			if (selected instanceof ITabContent) {
				((ITabContent) selected).lazyLoad();
			}
		});

		return mainLayout;
	}

	private FormLayout buildGeneralForm() {
		FormLayout layout = createFormLayout();
		layout.add(serial);
		layout.add(activationKey);
		layout.add(label);
		layout.add(model);
		layout.add(firmwareVersion);
		layout.add(productionDate);
		layout.add(activationDate);
		layout.add(inactivityMinutes);
		return layout;
	}

	private FormLayout buildMeasuresForm() {
		FormLayout layout = createFormLayout();
		layout.add(status);
		layout.add(lastContactDate);
		layout.add(readApikey);
		layout.add(writeApikey);
		layout.add(publishing);
		layout.add(tracing);
		return layout;
	}

	private FormLayout buildAssociationsForm() {
		FormLayout layout = createFormLayout();
		layout.add(networkSelect);
		layout.add(groupsSelect);
		return layout;
	}

	private FormLayout buildLocationForm() {
		FormLayout layout = createFormLayout();
		layout.add(locationAddress);
		layout.add(locationLatitude);
		layout.add(locationLongitude);
		layout.add(locationElevation);
		return layout;
	}

	private FormLayout buildExportForm() {
		FormLayout layout = createFormLayout();
		layout.add(exportingConfig);
		return layout;
	}

	private FormLayout createFormLayout() {
		FormLayout formLayout = new FormLayout();
		formLayout.setWidthFull();
		return formLayout;
	}

	private void applyGroupSelection(Collection<NetworkGroup> selectedGroups) {
		if (selectedGroups == null || selectedGroups.isEmpty()) {
			return;
		}
		groupsSelect.deselectAll();
		for (NetworkGroup group : selectedGroups) {
			groupsSelect.select(group);
		}
	}

	private List<Network> loadNetworks() {
		return networkService
				.findByOwner(getCurrentUser().getTenant());
	}

	private List<NetworkGroup> loadNetworkGroups() {
		return networkGroupService
				.findByOwner(getCurrentUser().getTenant());
	}

	private VerticalLayout buildExportTab(Device entity) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);

		layout.add(buildPanel(buildExportForm()));

		return layout;
	}

	private Collection<it.thisone.iotter.persistence.model.DeviceModel> loadDeviceModels() {
		return deviceService.getDeviceModels();
	}

	@Override
	protected void afterCommit() {
		Device source = getEntity();
		source.setLastContactDate(null);

		if (source.getWriteApikey() == null
				|| source.getWriteApikey().isEmpty()) {
			String writeApikeyValue = EncryptUtils.createWriteApiKey(source
					.getSerial());
			source.setWriteApikey(writeApikeyValue);
		}

		ExportingConfig exportingConfigBean = exportingConfig.getValue();
		source.setExportingConfig(exportingConfigBean);

		source.setModifier(getCurrentUser().getUsername());

		Device conflicted = null;
		if (!source.isNew() && alarmListing != null) {
			conflicted = deviceService.findOne(source.getId());
			alarmListing.commitAlarms(source);
		}

		if (alarmListing != null && conflicted != null
				&& conflicted.getConsistencyVersion() != source.getConsistencyVersion()
				&& Constants.SYSTEM.equals(conflicted.getModifier())) {
			org.springframework.beans.BeanUtils.copyProperties(source, conflicted,
					new String[] { "consistencyVersion", "status", "channels" });
			refreshItem(conflicted);
			getBinder().readBean(conflicted);
			alarmListing.commitAlarms(conflicted);
		}

		if (isCreateBean()) {
			deviceService.trace(source, TracingAction.DEVICE_CREATION,
					source.toString(), getCurrentUser().getName(), null);
		} else {
			deviceService.fireUpdatedEvent(source);
			deviceService.trace(source, TracingAction.DEVICE_UPDATE,
					source.toString(), getCurrentUser().getName(), null);
			alarmService.registerAlarms(source);
			if (channelListing != null && !channelListing.getRemoved().isEmpty()) {
				deviceService
						.deleteChannels(getEntity(), channelListing.getRemoved());
				conflicted = deviceService.findBySerial(source.getSerial());
				refreshItem(conflicted);
				getBinder().readBean(conflicted);
			}
		}
	}

	@Override
	protected void beforeCommit() throws EditorConstraintException {
		Device source = getEntity();

		if (groupsSelect != null) {
			Set<NetworkGroup> selected = new HashSet<>(groupsSelect.getSelectedItems());
			source.setGroups(selected);
		}

		if (groups == null) {
			return;
		}
		boolean hasVisualizations = groupWidgetService
				.hasVisualizations(source);
		if (hasVisualizations) {
			if (groupsSelect == null) {
				return;
			}
			Collection<NetworkGroup> selectedGroups = groupsSelect.getSelectedItems();
			for (NetworkGroup networkGroup : groups) {
				if (!selectedGroups.contains(networkGroup)) {
					throw new EditorConstraintException(
							getI18nLabel("groups.constraint"));
				}
			}
		}
	}



}
