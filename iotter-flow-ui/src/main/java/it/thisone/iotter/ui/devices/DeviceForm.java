package it.thisone.iotter.ui.devices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.annotations.PropertyId;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.ValueContext;
import com.vaadin.data.converter.StringToDoubleConverter;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChannelComparator;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.ExportingConfig;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
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
import it.thisone.iotter.ui.networkgroups.NetworkGroupUsers;
import it.thisone.iotter.ui.validators.UniqueDeviceActivationKeyValidator;
import it.thisone.iotter.ui.validators.UniqueDeviceSerialValidator;
import it.thisone.iotter.util.EncryptUtils;
import it.thisone.iotter.util.Utils;

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
	private CheckBox publishing;

	@PropertyId("tracing")
	private CheckBox tracing;

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

	public DeviceForm(Device entity, Network network) {
		super(entity, Device.class, NAME, network);
		if (isCreateBean()) {
			initializeDefaults();
		}
		// initializeFields() and registerFields() are called from getFieldsLayout()
		// during super() constructor, so we don't need to call them again here
		bindFields();
	}

	private void initializeDefaults() {
		getEntity().setOwner(UIUtils.getUserDetails().getUsername());
		boolean sticky = UIUtils.getUserDetails().getUsername().equalsIgnoreCase(Constants.ROLE_PRODUCTION);
		getEntity().setSticky(sticky);
		getEntity().setTracing(true);
		getEntity().setStatus(DeviceStatus.PRODUCED);
		getEntity().setProductionDate(new Date());
	}

	private void initializeFields() {
		serial = new TextField();
		serial.setSizeFull();
		serial.setRequiredIndicatorVisible(isCreateBean());
		serial.setCaption(getI18nLabel("serial"));
		if (!isCreateBean()) {
			serial.setReadOnly(true);
		}

		activationKey = new TextField();
		activationKey.setSizeFull();
		activationKey.setRequiredIndicatorVisible(isCreateBean());
		activationKey.setCaption(getI18nLabel("activationKey"));
		if (!isCreateBean()) {
			activationKey.setReadOnly(true);
		}

		label = new TextField();
		label.setSizeFull();
		label.setRequiredIndicatorVisible(true);
		label.setCaption(getI18nLabel("label"));

		model = new DeviceModelSelect(loadDeviceModels());
		model.setSizeFull();
		model.setRequiredIndicatorVisible(isCreateBean());
		model.setCaption(getI18nLabel("model"));
		if (!isCreateBean()) {
			model.setReadOnly(true);
		}

		firmwareVersion = new TextField();
		firmwareVersion.setSizeFull();
		firmwareVersion.setRequiredIndicatorVisible(isCreateBean());
		firmwareVersion.setCaption(getI18nLabel("firmwareVersion"));
		if (!isCreateBean()) {
			firmwareVersion.setReadOnly(true);
		}

		productionDate = new CustomDateField(UIUtils.getBrowserTimeZone());
		productionDate.setSizeFull();
		productionDate.setRequiredIndicatorVisible(isCreateBean());
		productionDate.setCaption(getI18nLabel("productionDate"));
		if (!isCreateBean()) {
			productionDate.setReadOnly(true);
		}

		activationDate = new CustomDateField(UIUtils.getBrowserTimeZone());
		activationDate.setSizeFull();
		activationDate.setRequiredIndicatorVisible(isCreateBean());
		activationDate.setCaption(getI18nLabel("activationDate"));
		if (!isCreateBean()) {
			activationDate.setReadOnly(true);
		}

		inactivityMinutes = new DeviceInactivityOptionGroup();
		inactivityMinutes.setSizeFull();
		inactivityMinutes.setRequiredIndicatorVisible(isCreateBean());
		inactivityMinutes.setCaption(getI18nLabel("inactivityMinutes"));

		status = new DeviceStatusSelect();
		status.setSizeFull();
		status.setCaption(getI18nLabel("status"));
		status.setReadOnly(true);

		lastContactDate = new LegacyDateTimeField();
		lastContactDate.setSizeFull();
		lastContactDate.setCaption(getI18nLabel("lastContactDate"));
		lastContactDate.setDateFormat("yyyy-MM-dd HH:mm:ss");
		lastContactDate.setReadOnly(true);

		readApikey = new TextField();
		readApikey.setSizeFull();
		readApikey.setCaption(getI18nLabel("readApikey"));

		writeApikey = new TextField();
		writeApikey.setSizeFull();
		writeApikey.setCaption(getI18nLabel("writeApikey"));
		writeApikey.setReadOnly(true);

		publishing = new CheckBox();
		publishing.setSizeFull();
		publishing.setCaption(getI18nLabel("publishing"));

		tracing = new CheckBox();
		tracing.setSizeFull();
		tracing.setCaption(getI18nLabel("tracing"));

		networkSelect = new NetworkSelect(loadNetworks());
		networkSelect.setSizeFull();
		networkSelect.setCaption(getI18nLabel("network"));

		groupsSelect = new NetworkGroupSelect(loadNetworkGroups(), true);
		groupsSelect.setSizeFull();
		groupsSelect.setCaption(getI18nLabel("groups"));
		groupsSelect.setVisible(Constants.USE_GROUPS);

		exclusiveGroups = new NetworkGroupSelect(loadNetworkGroups(), true);
		exclusiveGroups.setSizeFull();
		exclusiveGroups.setCaption(getI18nLabel("exclusive_groups"));
		exclusiveGroups.setVisible(Constants.USE_GROUPS);

		locationAddress = new TextField();
		locationAddress.setSizeFull();
		locationAddress.setCaption(getI18nLabel("location.address"));

		locationLatitude = new TextField();
		locationLatitude.setSizeFull();
		locationLatitude.setCaption(getI18nLabel("location.latitude"));

		locationLongitude = new TextField();
		locationLongitude.setSizeFull();
		locationLongitude.setCaption(getI18nLabel("location.longitude"));

		locationElevation = new TextField();
		locationElevation.setSizeFull();
		locationElevation.setCaption(getI18nLabel("location.elevation"));



		exportingConfig = new ExportingConfigField();
		exportingConfig.setSizeFull();
		exportingConfig.setCaption(getI18nLabel("exportingConfig"));
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

	private void bindFields() {
		String requiredMessage = UIUtils.localize("validators.fieldgroup_errors");
		getBinder().forField(serial)
				.withValidator(value -> !isCreateBean() || (value != null && !value.trim().isEmpty()),
						requiredMessage)
				.bind(Device::getSerial, Device::setSerial);

		getBinder().forField(activationKey)
				.withValidator(value -> !isCreateBean() || (value != null && !value.trim().isEmpty()),
						requiredMessage)
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
				.bind(Device::isPublishing, Device::setPublishing);

		getBinder().forField(tracing)
				.bind(Device::isTracing, Device::setTracing);

		getBinder().forField(locationAddress)
				.bind(device -> device.getLocation().getAddress(),
						(device, value) -> device.getLocation().setAddress(value));

		getBinder().forField(locationLatitude)
				.withConverter(new StringToDoubleConverter(UIUtils.localize("validators.fieldgroup_errors")))
				.bind(device -> device.getLocation().getLatitude(),
						(device, value) -> device.getLocation().setLatitude(value != null ? value : 0d));

		getBinder().forField(locationLongitude)
				.withConverter(new StringToDoubleConverter(UIUtils.localize("validators.fieldgroup_errors")))
				.bind(device -> device.getLocation().getLongitude(),
						(device, value) -> device.getLocation().setLongitude(value != null ? value : 0d));

		getBinder().forField(locationElevation)
				.withConverter(new StringToDoubleConverter(UIUtils.localize("validators.fieldgroup_errors")))
				.bind(device -> device.getLocation().getElevation(),
						(device, value) -> device.getLocation().setElevation(value != null ? value : 0d));



		getBinder().forField(exportingConfig)
				.bind(Device::getExportingConfig, Device::setExportingConfig);
	}

	@Override
	public Layout getFieldsLayout() {
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
		multicomponent.addStyleName(ValoTheme.TABSHEET_FRAMED);
		multicomponent.setSizeFull();

		mainLayout.addComponent(multicomponent);

		multicomponent.addTab(buildPanel(buildGeneralForm()), getI18nLabel("general_tab"));

		groups = new ArrayList<NetworkGroup>();
		if (!isCreateBean()) {
			Date lastContactDateValue = UIUtils.getCassandraService().getFeeds().getLastContact(entity.getSerial());
			entity.setLastContactDate(lastContactDateValue);
			lastContactDate.setValue(lastContactDateValue);

			boolean production = UIUtils.getUserDetails().hasRole(Constants.ROLE_PRODUCTION);
			boolean supervisor = UIUtils.getUserDetails().hasRole(Constants.ROLE_SUPERVISOR);

			if (supervisor) {
				multicomponent.addTab(buildPanel(buildMeasuresForm()), getI18nLabel("measures_tab"));
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

				multicomponent.addTab(buildPanel(buildAssociationsForm()), getI18nLabel("associations_tab"));
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
					multicomponent.addTab(buildPanel(buildLocationForm()), getI18nLabel("location_tab"));
				}

				channelListing = new ChannelListing(channels);
				multicomponent.addTab(channelListing, getI18nLabel("channels_tab"));

				if (entity.isAlarmed()) {
					UIUtils.getServiceFactory().getAlarmService().setUpFiredAlarms(entity);
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
				NetworkGroup group = UIUtils.getServiceFactory().getDeviceService().getDeviceAlarmGroup(entity);
				alarmListing.setAlarmGroup(group);
				multicomponent.addTab(alarmListing, getI18nLabel("alarms_tab"));

				ChannelRemoteControlListing remote = new ChannelRemoteControlListing(channels);
				multicomponent.addTab(remote, getI18nLabel("remote_tab"));

				lastValues = new ChannelLastMeasuresListing(channels);
				lastValues.setSizeFull();
				multicomponent.addTab(lastValues, getI18nLabel("lastvalues_tab"));

				if (supervisor) {
					DeviceRollup rollup = new DeviceRollup(entity);
					rollup.setSizeFull();
					multicomponent.addTab(rollup, getI18nLabel("rollup_tab"));
					DeviceConfig config = new DeviceConfig(entity);
					config.setSizeFull();
					multicomponent.addTab(config, getI18nLabel("config_tab"));
				}
			}

			if (entity.isRunning() && entity.getMaster() == null) {
				multicomponent.addTab(buildExportTab(entity), getI18nLabel("export_tab"));
			}
		}

		multicomponent.addSelectedTabChangeListener(event -> {
			TabSheet tabsheet = event.getTabSheet();
			if (tabsheet.getSelectedTab() instanceof ITabContent) {
				((ITabContent) tabsheet.getSelectedTab()).lazyLoad();
			}
		});

		return mainLayout;
	}

	private FormLayout buildGeneralForm() {
		FormLayout layout = createFormLayout();
		layout.addComponent(serial);
		layout.addComponent(activationKey);
		layout.addComponent(label);
		layout.addComponent(model);
		layout.addComponent(firmwareVersion);
		layout.addComponent(productionDate);
		layout.addComponent(activationDate);
		layout.addComponent(inactivityMinutes);
		return layout;
	}

	private FormLayout buildMeasuresForm() {
		FormLayout layout = createFormLayout();
		layout.addComponent(status);
		layout.addComponent(lastContactDate);
		layout.addComponent(readApikey);
		layout.addComponent(writeApikey);
		layout.addComponent(publishing);
		layout.addComponent(tracing);
		return layout;
	}

	private FormLayout buildAssociationsForm() {
		FormLayout layout = createFormLayout();
		layout.addComponent(networkSelect);
		layout.addComponent(groupsSelect);
		return layout;
	}

	private FormLayout buildLocationForm() {
		FormLayout layout = createFormLayout();
		layout.addComponent(locationAddress);
		layout.addComponent(locationLatitude);
		layout.addComponent(locationLongitude);
		layout.addComponent(locationElevation);
		return layout;
	}

	private FormLayout buildExportForm() {
		FormLayout layout = createFormLayout();
		layout.addComponent(exportingConfig);
		return layout;
	}

	private FormLayout createFormLayout() {
		FormLayout formLayout = new FormLayout();
		formLayout.setMargin(true);
		formLayout.setSpacing(true);
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
		return UIUtils.getServiceFactory().getNetworkService()
				.findByOwner(UIUtils.getUserDetails().getTenant());
	}

	private List<NetworkGroup> loadNetworkGroups() {
		return UIUtils.getServiceFactory().getNetworkGroupService()
				.findByOwner(UIUtils.getUserDetails().getTenant());
	}

	private Component buildExportTab(Device entity) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);

		layout.addComponent(buildPanel(buildExportForm()));

		NetworkGroup group = UIUtils.getServiceFactory().getDeviceService().getDeviceExportGroup(entity);
		String[] visibleColumns = new String[] { "username", "email", "displayName" };
		NetworkGroupUsers users = new NetworkGroupUsers(group, visibleColumns);
		users.setSizeFull();
		layout.addComponent(users);
		return layout;
	}

	private Collection<it.thisone.iotter.persistence.model.DeviceModel> loadDeviceModels() {
		return UIUtils.getServiceFactory().getDeviceService().getDeviceModels();
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

		source.setModifier(UIUtils.getUserDetails().getUsername());

		Device conflicted = null;
		if (!source.isNew() && alarmListing != null) {
			conflicted = UIUtils.getServiceFactory().getDeviceService().findOne(source.getId());
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
			UIUtils.getServiceFactory().getDeviceService().trace(source, TracingAction.DEVICE_CREATION,
					source.toString(), UIUtils.getUserDetails().getName(), null);
		} else {
			UIUtils.getServiceFactory().getDeviceService().fireUpdatedEvent(source);
			UIUtils.getServiceFactory().getDeviceService().trace(source, TracingAction.DEVICE_UPDATE,
					source.toString(), UIUtils.getUserDetails().getName(), null);
			UIUtils.getServiceFactory().getAlarmService().registerAlarms(source);
			if (channelListing != null && !channelListing.getRemoved().isEmpty()) {
				UIUtils.getServiceFactory().getDeviceService()
						.deleteChannels(getEntity(), channelListing.getRemoved());
				conflicted = UIUtils.getServiceFactory().getDeviceService().findBySerial(source.getSerial());
				refreshItem(conflicted);
				getBinder().readBean(conflicted);
			}
		}
	}

	@Override
	protected void beforeCommit() throws EditorConstraintException {
		Device source = getEntity();

		validateRequiredFields();

		if (groupsSelect != null) {
			Set<NetworkGroup> selected = new HashSet<>(groupsSelect.getSelectedItems());
			source.setGroups(selected);
		}

		if (groups == null) {
			return;
		}
		boolean hasVisualizations = UIUtils.getServiceFactory()
				.getGroupWidgetService()
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

	private void validateRequiredFields() throws EditorConstraintException {
		if (isCreateBean()) {
			requireField(serial);
			requireField(activationKey);
			requireField(model);
			requireField(firmwareVersion);
			requireField(productionDate);
			requireField(activationDate);
			requireField(inactivityMinutes);
			validateUniqueOnCreate();
		}
		requireField(label);
	}

	private void requireField(Component field) throws EditorConstraintException {
		if (field instanceof TextField) {
			String value = ((TextField) field).getValue();
			if (value == null || value.trim().isEmpty()) {
				throw new EditorConstraintException(UIUtils.localize("validators.fieldgroup_errors"));
			}
		} else if (field instanceof CustomDateField) {
			if (((CustomDateField) field).getValue() == null) {
				throw new EditorConstraintException(UIUtils.localize("validators.fieldgroup_errors"));
			}
		} else if (field instanceof DeviceInactivityOptionGroup) {
			if (((DeviceInactivityOptionGroup) field).getValue() == null) {
				throw new EditorConstraintException(UIUtils.localize("validators.fieldgroup_errors"));
			}
		} else if (field instanceof DeviceModelSelect) {
			if (((DeviceModelSelect) field).getValue() == null) {
				throw new EditorConstraintException(UIUtils.localize("validators.fieldgroup_errors"));
			}
		}
	}

	private void validateUniqueOnCreate() throws EditorConstraintException {
		ValidationResult serialResult = new UniqueDeviceSerialValidator()
				.apply(serial.getValue(), new ValueContext(serial));
		if (serialResult.isError()) {
			throw new EditorConstraintException(serialResult.getErrorMessage());
		}
		ValidationResult activationResult = new UniqueDeviceActivationKeyValidator()
				.apply(activationKey.getValue(), new ValueContext(activationKey));
		if (activationResult.isError()) {
			throw new EditorConstraintException(activationResult.getErrorMessage());
		}
	}

	@Override
	public String getWindowStyle() {
		return "device-editor";
	}

	@Override
	public float[] getWindowDimension() {
		return UIUtils.L_DIMENSION;
	}
}
