package it.thisone.iotter.ui.devices;

import java.text.ChoiceFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import com.google.common.collect.Range;
import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;

import it.thisone.iotter.cassandra.model.FeedAlarmEvent;
import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.AlarmStatus;
import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.enums.ExportFileMode;
import it.thisone.iotter.enums.ExportFormat;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.exporter.ExportConfig;
import it.thisone.iotter.exporter.ExportProperties;
import it.thisone.iotter.integration.AlarmService;
import it.thisone.iotter.integration.CassandraService;
import it.thisone.iotter.integration.SubscriptionService;
import it.thisone.iotter.lazyquerydataprovider.FilterableQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDataProvider;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryFactory;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChannelComparator;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.repository.DeviceRepository;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.GroupWidgetService;
import it.thisone.iotter.persistence.service.MeasureUnitTypeService;
import it.thisone.iotter.persistence.service.NetworkGroupService;
import it.thisone.iotter.persistence.service.NetworkService;
import it.thisone.iotter.persistence.service.UserService;
import it.thisone.iotter.provisioning.ProvisionedEvent;
import it.thisone.iotter.security.EntityPermission;
import it.thisone.iotter.security.Permissions;
import it.thisone.iotter.security.UserDetailsAdapter;

import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.AbstractBaseEntityListing;
import it.thisone.iotter.ui.common.AuthenticatedUser;
import it.thisone.iotter.ui.common.ConfirmationDialog;
import it.thisone.iotter.ui.common.ConfirmationDialog.Callback;
import it.thisone.iotter.ui.common.EditorSavedEvent;
import it.thisone.iotter.ui.common.EditorSavedListener;
import it.thisone.iotter.ui.common.EditorSelectedEvent;
import it.thisone.iotter.ui.common.EditorSelectedListener;
import it.thisone.iotter.ui.common.EntityRemovedEvent;
import it.thisone.iotter.ui.common.EntityRemovedListener;
import it.thisone.iotter.ui.common.EntitySelectedEvent;
import it.thisone.iotter.ui.common.EntitySelectedListener;
import it.thisone.iotter.ui.common.PermissionsUtils;
import it.thisone.iotter.ui.common.SideDrawer;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.common.export.ExportDialog;
import it.thisone.iotter.ui.eventbus.DeviceChangedEvent;
import it.thisone.iotter.ui.eventbus.UIEventBus;
import it.thisone.iotter.ui.eventbus.WidgetRefreshEvent;
import it.thisone.iotter.ui.groupwidgets.GroupWidgetVisualizer;
import it.thisone.iotter.ui.ifc.IProvisioningWizard;
import it.thisone.iotter.ui.users.UserForm;
import it.thisone.iotter.util.PopupNotification;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DevicesListing extends AbstractBaseEntityListing<Device> {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(DevicesListing.class);

	private static final String DEVICE_STATUS = "deviceStatus";
	private static final String ALARM_STATUS = "alarmStatus";
	private static final String LABEL = "label";
	private static final String OWNER = "owner";
	private static final String DEVICE_VIEW = "device.view";
	private static final String DOWNLOAD_BUTTON = "download";
	private static final String ALARM_BUTTON = "alarm";
	private static final String RESET_BUTTON = "reset";
	private static final String MODIFY_BUTTON = "edit";
	private static final String REMOVE_BUTTON = "remove";

	public static final int ALARMED_LABEL_COUNT = 4;

	@Autowired
	private AuthenticatedUser authenticatedUser;
	@Autowired
	private UIEventBus uiEventBus;
	@Autowired
	private ObjectProvider<DeviceWidgetBox> deviceWidgetBoxProvider;
	@Autowired
	private DeviceRepository deviceRepository;

	@Autowired
	private ObjectProvider<DeviceForm> deviceFormProvider;

	@Autowired
	private DeviceService deviceService;

	@Autowired
	private GroupWidgetService groupWidgetService;
	@Autowired
	private AlarmService alarmService;
	@Autowired
	private NetworkService networkService;
	@Autowired
	private NetworkGroupService networkGroupService;

	@Autowired
	private CassandraService cassandraService;

	@Autowired
	private SubscriptionService subscriptionService;

	@Autowired
	private UserService userService;

	@Autowired
	private MeasureUnitTypeService measureUnitTypeService;

	private Network network;
	private boolean hasParameters = true;
	private DeviceWidgetBox boxes;
	private IProvisioningWizard wizard;
	private Permissions permissions;
	private boolean supervisor;
	private boolean production;
	private int counter = ALARMED_LABEL_COUNT;

	private Grid<Device> grid;
	private LazyQueryDataProvider<Device, DevicesFilter> dataProvider;
	private DevicesQueryDefinition queryDefinition;
	private DevicesFilter currentFilter = new DevicesFilter();
	private int currentLimit = DEFAULT_LIMIT;
	private UserDetailsAdapter currentUser;

	public DevicesListing() {
		super(Device.class, DEVICE_VIEW, DEVICE_VIEW, false);
	}

	public void init(Network network) {
		init(network, true);
	}

	public void init(Network network, boolean hasParameters) {
		if (grid != null) {
			return;
		}
		this.network = network;
		this.hasParameters = hasParameters;
		currentUser = authenticatedUser.get()
				.orElseThrow(() -> new IllegalStateException("User must be authenticated to edit devices"));
		this.permissions = PermissionsUtils.getPermissionsForUserEntity(currentUser);

		this.permissions = PermissionsUtils.getPermissionsForDeviceEntity(currentUser);
		setPermissions(this.permissions);
		this.supervisor = currentUser != null && currentUser.hasRole(Constants.ROLE_SUPERVISOR);
		this.production = currentUser != null && currentUser.hasRole(Constants.ROLE_PRODUCTION);
		buildLayout();
	}

	private void buildLayout() {
		HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.setWidthFull();
		toolbar.setSpacing(true);
		toolbar.setPadding(true);
		toolbar.addClassName(TOOLBAR_STYLE);

		queryDefinition = new DevicesQueryDefinition(Device.class, currentLimit, permissions);
		queryDefinition.setNetwork(network);
		queryDefinition.setOwner(authenticatedUser.getTenant().orElse(null));
		queryDefinition.setPage(0, currentLimit);
		queryDefinition.setQueryFilter(currentFilter);
		dataProvider = new LazyQueryDataProvider<>(queryDefinition,
				new DevicesQueryFactory(deviceRepository, cassandraService));
		dataProvider.setCacheQueries(false);
		dataProvider.setFilter(currentFilter);
		setBackendDataProvider(dataProvider);

		grid = createGrid();
		VerticalLayout listingLayout = createListingLayout(toolbar, grid);
		setSelectable(grid);
		setupDeviceWidgetBox(grid, listingLayout);

		getMainLayout().add(listingLayout);
		getMainLayout().setFlexGrow(1f, listingLayout);

		updateTotalCount();

		getButtonsLayout().add(createExportDataButton(), createAlarmsButton(), createResetButton(),
				createRemoveButton(),
				createModifyButton(), createActivateButton(), createViewButton(), createAddButton(),
				createImportButton());
		if (hasParameters && UIUtils.isMobile()) {
			getButtonsLayout().add(createDeviceWidgetButton());
		}
		toolbar.add(getButtonsLayout());
		toolbar.setAlignItems(Alignment.CENTER);
		enableButtons(null);
	}

	@Override
	public AbstractBaseEntityForm<Device> getEditor(Device item, boolean readOnly) {
		// return new DeviceForm(item, network,currentUser,readOnly);


		DeviceForm editor = deviceFormProvider.getObject(item, network, currentUser, readOnly
				// ,deviceService, alarmService, networkService,
				// networkGroupService, groupWidgetService,
				// cassandraService

		);
		editor.initialize();
		return editor;

	}

	@Override
	protected void openDetails(Device item) {
		if (item == null) {
			return;
		}

		AbstractBaseEntityForm<Device> details = getEditor(item, true);
		SideDrawer dialog = (SideDrawer) createDialog(getI18nLabel("view_dialog"), details);

		dialog.open();
	}

	@Override
	protected void openRemove(Device item) {
		if (item == null) {
			return;
		}

		AbstractBaseEntityForm<Device> details = getEditor(item, true);
		SideDrawer dialog = (SideDrawer) createDialog(getI18nLabel("remove_dialog"), details);

		details.setDeleteHandler(entity -> {
			try {
				deviceService.delete(entity);
				dialog.close();
				refreshCurrentPage();
			} catch (Exception e) {
				PopupNotification.show(e.getMessage(), PopupNotification.Type.ERROR);
			}
		});

	}

	private void refreshData() {
		dataProvider.refreshAll();
		updateTotalCount();
		grid.scrollToStart();
		grid.deselectAll();
		enableButtons(null);
	}

	private void refreshCurrentPage() {
		refreshData();
	}

	private long getTotalCount() {
		return new DevicesQuery(deviceRepository, cassandraService, queryDefinition).countTotal();
	}

	private void updateTotalCount() {
		setTotalSize(getTotalCount());
	}

	private Grid<Device> createGrid() {
		Grid<Device> table = new Grid<>();
		table.setDataProvider(dataProvider);
		table.setSelectionMode(Grid.SelectionMode.SINGLE);
		table.setSizeFull();
		// table.addClassName(UIUtils.TABLE_STYLE);

		List<Grid.Column<Device>> columns = new ArrayList<>();
		columns.add(table.addColumn(this::formatLabel).setKey(LABEL));
		columns.add(table.addColumn(this::formatProfile).setKey("description"));
		columns.add(table.addColumn(this::formatModel).setKey("model"));
		columns.add(table.addColumn(this::formatAlarmStatus).setKey(ALARM_STATUS));

		if (network == null) {
			columns.add(table.addColumn(this::formatNetwork).setKey("network"));
		}

		columns.add(table.addColumn(this::formatDeviceStatus).setKey(DEVICE_STATUS));

		if (permissions.isViewAllMode()) {
			columns.add(table.addColumn(Device::getOwner).setKey(OWNER));
			columns.add(table.addColumn(Device::getSerial).setKey("serial"));
			columns.add(table.addColumn(Device::isPublishing).setKey("publishing"));
			columns.add(table.addColumn(Device::isTracing).setKey("tracing"));
		}

		for (Grid.Column<Device> column : columns) {
			String id = column.getKey();
			if (!LABEL.equals(id) && !ALARM_STATUS.equals(id) && !DEVICE_STATUS.equals(id) && !OWNER.equals(id)) {
				column.setSortable(false);
			}
			column.setHeader(getI18nLabel(id));
		}

		table.setColumnOrder(columns.toArray(new Grid.Column[0]));
		initFilters(table);
		return table;
	}

	private void initFilters(Grid<Device> table) {
		HeaderRow filterRow = table.appendHeaderRow();

		TextField labelField = new TextField();
		labelField.setPlaceholder("Filter...");
		labelField.setWidthFull();
		labelField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
		labelField.setValueChangeMode(ValueChangeMode.LAZY);
		filterRow.getCell(table.getColumnByKey(LABEL)).setComponent(labelField);
		labelField.addValueChangeListener(event -> {
			currentFilter.setLabel(event.getValue());
			queryDefinition.setQueryFilter(currentFilter);
			setFilter(currentFilter);
			refreshCurrentPage();
		});

		ComboBox<AlarmStatus> alarmStatusCombo = new ComboBox<>();
		alarmStatusCombo.setPlaceholder("Filter...");
		alarmStatusCombo.setWidthFull();
		alarmStatusCombo.setClearButtonVisible(true);
		alarmStatusCombo.setItems(AlarmStatus.values());
		alarmStatusCombo
				.setItemLabelGenerator(status -> getI18nLabel("enum.alarmstatus." + status.name().toLowerCase()));
		filterRow.getCell(table.getColumnByKey(ALARM_STATUS)).setComponent(alarmStatusCombo);
		alarmStatusCombo.addValueChangeListener(event -> {
			currentFilter.setAlarmStatus(event.getValue());
			queryDefinition.setQueryFilter(currentFilter);
			setFilter(currentFilter);
			refreshCurrentPage();
		});

		ComboBox<DeviceStatus> deviceStatusCombo = new ComboBox<>();
		deviceStatusCombo.setPlaceholder("Filter...");
		deviceStatusCombo.setWidthFull();
		deviceStatusCombo.setClearButtonVisible(true);
		deviceStatusCombo.setItems(DeviceStatus.values());
		deviceStatusCombo
				.setItemLabelGenerator(status -> getI18nLabel("enum.devicestatus." + status.name().toLowerCase()));
		filterRow.getCell(table.getColumnByKey(DEVICE_STATUS)).setComponent(deviceStatusCombo);
		deviceStatusCombo.addValueChangeListener(event -> {
			currentFilter.setDeviceStatus(event.getValue());
			queryDefinition.setQueryFilter(currentFilter);
			setFilter(currentFilter);
			refreshCurrentPage();
		});

		if (permissions.isViewAllMode()) {
			TextField ownerField = new TextField();
			ownerField.setPlaceholder("Filter...");
			ownerField.setWidthFull();
			ownerField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
			ownerField.setValueChangeMode(ValueChangeMode.LAZY);
			filterRow.getCell(table.getColumnByKey(OWNER)).setComponent(ownerField);
			ownerField.addValueChangeListener(event -> {
				currentFilter.setOwner(event.getValue());
				queryDefinition.setQueryFilter(currentFilter);
				setFilter(currentFilter);
				refreshCurrentPage();
			});
		}
	}

	private String formatProfile(Device device) {
		if (device.getProfiles() == null || device.getProfiles().isEmpty()) {
			return "";
		}
		return device.getProfiles().stream().findFirst().map(profile -> profile.getDisplayName()).orElse("");
	}

	private String formatLabel(Device device) {
		String label = device.getLabel();
		if (label == null) {
			return "";
		}
		return label.length() <= 40 ? label : label.substring(0, 40) + "...";
	}

	private String formatModel(Device device) {
		return device.getModel() != null ? device.getModel().getName() : "";
	}

	private String formatAlarmStatus(Device device) {
		AlarmStatus status = device.getAlarmStatus();
		return status != null ? getI18nLabel("enum.alarmstatus." + status.name().toLowerCase()) : "";
	}

	private String formatDeviceStatus(Device device) {
		DeviceStatus status = device.getStatus();
		return status != null ? getI18nLabel("enum.devicestatus." + status.name().toLowerCase()) : "";
	}

	private String formatNetwork(Device device) {
		return device.getNetwork() != null ? device.getNetwork().getName() : "";
	}

	private VerticalLayout createListingLayout(HorizontalLayout toolbar, Grid<Device> table) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.add(toolbar, table);
		layout.setFlexGrow(1f, table);
		return layout;
	}

	private Button createAddButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.PLUS.create());
		button.getElement().setProperty("title", getI18nLabel("add"));
		button.setId("add" + getId() + ALWAYS_ENABLED_BUTTON);
		button.addClickListener(event -> openEditor(new Device(), getI18nLabel("add_dialog")));
		button.setVisible(permissions.isCreateMode());
		return button;
	}

	private Button createViewButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.INFO_CIRCLE.create());
		button.getElement().setProperty("title", getI18nLabel("view_action"));
		button.addClickListener(event -> openDetails(getCurrentValue()));
		button.setVisible(permissions.isViewMode());
		return button;
	}

	private Button createModifyButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.EDIT.create());
		button.getElement().setProperty("title", getI18nLabel("modify_action"));
		button.setId(MODIFY_BUTTON);
		button.addClickListener(event -> openEditor(getCurrentValue(), getI18nLabel("modify_dialog")));
		button.setVisible(permissions.isModifyMode());
		return button;
	}

	private Button createRemoveButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.TRASH.create());
		button.getElement().setProperty("title", getI18nLabel("remove_action"));
		button.setId(REMOVE_BUTTON);
		button.addClickListener(event -> openRemove(getCurrentValue()));
		button.setVisible(permissions.isRemoveMode());
		return button;
	}

	private Button createActivateButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.CHECK_CIRCLE.create());
		button.getElement().setProperty("title", getI18nLabel("activate_button"));
		button.setId("activate_button" + ALWAYS_ENABLED_BUTTON);
		button.addClickListener(event -> openActivation());
		button.setVisible(currentUser.hasPermission(EntityPermission.DEVICE.ACTIVATE));
		return button;
	}

	private Button createResetButton() {
		Button button = new Button();
		button.setIcon(supervisor ? VaadinIcon.REFRESH.create() : VaadinIcon.TRASH.create());
		button.getElement().setProperty("title",
				supervisor ? getI18nLabel("reset_button") : getI18nLabel("remove_button"));
		button.setId(RESET_BUTTON);
		button.addClickListener(event -> openReset(getCurrentValue()));
		button.setVisible(currentUser.hasPermission(EntityPermission.DEVICE.RESET));
		return button;
	}

	private Button createAlarmsButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.BELL.create());
		button.getElement().setProperty("title", getI18nLabel("alarms_button"));
		button.setId(ALARM_BUTTON);
		button.addClickListener(event -> openAlarms(getCurrentValue()));
		return button;
	}

	private Button createExportDataButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.DOWNLOAD.create());
		button.getElement().setProperty("title", getI18nLabel("export_button"));
		button.setId(DOWNLOAD_BUTTON);
		button.addClickListener(event -> openDownload(getCurrentValue()));
		button.setVisible(currentUser.hasPermission(EntityPermission.DEVICE.EXPORT_DATA));
		return button;
	}

	private Button createImportButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.UPLOAD.create());
		button.getElement().setProperty("title", getI18nLabel("import_production"));
		button.setId("import_button" + ALWAYS_ENABLED_BUTTON);
		button.addClickListener(event -> openImporter());
		button.setVisible(currentUser.hasPermission(EntityPermission.DEVICE.IMPORT));
		return button;
	}

	private Button createDeviceWidgetButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.INFO.create());
		button.getElement().setProperty("title", getI18nLabel("groupwidgetbox_button"));
		button.addClickListener(event -> openGroupWidgetBox());
		button.setVisible(currentUser.hasPermission(EntityPermission.DEVICE.MODIFY));
		return button;
	}

	private void openEditor(Device item, String label) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityForm<Device> editor = getEditor(item, false);
		Dialog dialog = createDialog(label, editor);

		editor.setSavedHandler(entity -> {
			try {
				if (entity.isNew()) {
					deviceService.create(entity);
				} else {
					deviceService.update(entity);
				}
				dialog.close();
				refreshCurrentPage();
			} catch (Exception e) {
				PopupNotification.show(e.getMessage(), PopupNotification.Type.ERROR);
			}
		});
		dialog.open();
	}

	private void openActivation() {
		final DeviceActivatorForm content = new DeviceActivatorForm(network, currentUser);
		Dialog dialog = createDialog(getI18nLabel("device_activation"), content);
		// if (dialog instanceof SideDrawer) {
		// ((SideDrawer) dialog).applyDimension(content.getWindowDimension());
		// }
		content.setSavedHandler(item -> {
			if (item != null) {
				try {
					subscriptionService.factoryReset(item.getSerial(), item.getActivationKey());
					userService.deviceActivation(item.getSerial(), item.getActivationKey(),
							currentUser.getUsername(), item.getNetwork());
					refreshCurrentPage();
					PopupNotification.show(getI18nLabel("device_activated"));
				} catch (BackendServiceException e) {
					logger.debug("device_activation", e);
					PopupNotification.show(getI18nLabel("device_not_activated"), PopupNotification.Type.ERROR);
				}
			} else {
				PopupNotification.show(getI18nLabel("device_not_activated"), PopupNotification.Type.ERROR);
			}
			dialog.close();
		});
		dialog.open();
	}

	private void openImporter() {
		final DeviceProductionImporter content = new DeviceProductionImporter();
		Dialog dialog = createDialog(getI18nLabel("import_production"), content);
		// if (dialog instanceof SideDrawer) {
		// ((SideDrawer) dialog).applyDimension(content.getWindowDimension());
		// }
		content.addListener(new EditorSelectedListener() {
			private static final long serialVersionUID = 1L;

			@Override
			@SuppressWarnings("unchecked")
			public void editorSelected(EditorSelectedEvent event) {
				dialog.close();
				if (event.getSelected() == null) {
					return;
				}
				Set<Device> items = (Set<Device>) event.getSelected();
				for (Device item : items) {
					try {
						deviceService.create(item);
					} catch (Exception e) {
						PopupNotification.show(e.getMessage(), PopupNotification.Type.ERROR);
					}
				}
				refreshCurrentPage();
			}
		});
		dialog.open();
	}

	private void openAlarms(Device device) {
		if (device == null) {
			return;
		}

		List<FeedAlarmEvent> events = cassandraService.getAlarms().getAlarmEvents(device.getSerial(), 30);
		ChoiceFormat cf = measureUnitTypeService.getMeasureUnitChoiceFormat();

		final DeviceAlarmsDetails content = new DeviceAlarmsDetails(device, events, cf);
		Dialog dialog = createDialog(getI18nLabel("device_alarms"), content);
		// if (dialog instanceof SideDrawer) {
		// ((SideDrawer) dialog).applyDimension(content.getWindowDimension());
		// }
		// content.addListener(new EntitySelectedListener() {
		// @Override
		// public void entitySelected(EntitySelectedEvent<?> event) {
		// dialog.close();
		// }
		// });
		dialog.open();
	}

	private void openDownload(Device device) {
		if (device == null) {
			return;
		}
		ExportProperties props = new ExportProperties();
		props.setLegacy(false);
		// props.setTimeZone(UIUtils.getBrowserTimeZone());
		props.setFileMode(ExportFileMode.SINGLE);
		props.setFormat(ExportFormat.EXCEL);
		props.setLocale(UIUtils.getLocale());

		ExportConfig config = new ExportConfig();
		config.setInterpolation(Interpolation.RAW);
		String lockId = device.getSerial();
		if (cassandraService.getRollup().existLockSink(lockId)) {
			PopupNotification.show(getTranslation("export.already_running_export"), PopupNotification.Type.ERROR);
			return;
		}

		Range<Date> interval = props.isLegacy() ? getMeasuresRange(device)
				: cassandraService.getMeasures().getMeasuresSetRange(device.getSerial());
		if (interval == null) {
			PopupNotification.show("Export Data NOT available", PopupNotification.Type.ERROR);
			return;
		}

		config.setInterval(interval);
		config.setName(device.getLabel());
		config.setLockId(lockId);

		List<Channel> channels = new ArrayList<>(device.getChannels());
		Collections.sort(channels, new ChannelComparator());
		for (Channel channel : channels) {
			config.getFeeds().add(ChartUtils.createExportFeed(channel));
		}

		ExportDialog dialog = new ExportDialog(config, props, device, java.util.concurrent.ForkJoinPool.commonPool());
		// dialog.setHeaderTitle("Export " + device.getLabel());
		dialog.open();
	}

	private void openReset(Device device) {
		if (device == null) {
			return;
		}
		VerticalLayout layout = new VerticalLayout();
		layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
		layout.add(new com.vaadin.flow.component.html.Span(
				supervisor ? getI18nLabel("reset_warning") : getI18nLabel("delete_warning")));
		Callback callback = result -> {
			if (!result) {
				return;
			}
			try {
				subscriptionService.factoryReset(device.getSerial(), device.getActivationKey());
				subscriptionService.provisioned(new ProvisionedEvent(device, ""));
			} catch (BackendServiceException e) {
				PopupNotification.show(e.getMessage(), PopupNotification.Type.ERROR);
			}
			if (boxes != null) {
				boxes.refresh(null);
			}
			refreshCurrentPage();
		};
		Dialog dialog = new ConfirmationDialog(
				supervisor ? getI18nLabel("device_reset") : getI18nLabel("remove_dialog"),
				layout, callback);
		dialog.open();
	}

	private void openGroupWidgetBox() {
		if (boxes == null) {
			return;
		}
		Dialog dialog = createDialog(getI18nLabel("groupwidgetbox_device"), boxes);
		// if (dialog instanceof SideDrawer) {
		// ((SideDrawer) dialog).applyDimension(boxes.getWindowDimension());
		// }
		dialog.open();
	}

	private void openVisualization(GroupWidget bean) {
		if (bean == null) {
			return;
		}
		Dialog dialog = createDialog(getI18nLabel("groupwidgetbox_device"),
				new GroupWidgetVisualizer(bean.getId().toString(), true));
		dialog.open();
	}

	private void openProvisioning(Device bean) {
		if (bean == null) {
			return;
		}
		// wizard =
		// UIUtils.getUiFactory().getDeviceUiFactory().getProvisioningWizard(bean.getSerial());
		// if (wizard == null) {
		// return;
		// }
		// Dialog dialog = createDialog(getI18nLabel("device_provisioning"), (Component)
		// wizard);
		// wizard.addListener(new EditorSavedListener() {
		// @Override
		// public void editorSaved(EditorSavedEvent event) {
		// if (event.getSavedItem() != null) {
		// refreshCurrentPage();
		// }
		// dialog.close();
		// }
		// });
		// dialog.open();
	}

	@Subscribe
	public void refreshOnDeviceChanged(final DeviceChangedEvent event) {
		// UI ui = getUI().orElse(null);
		// if (ui != null) {
		// ui.access(() -> {
		// try {
		// dataProvider.refreshAll();
		// ui.push();
		// } catch (Exception e) {
		// logger.warn("refreshOnDeviceChanged", e);
		// }
		// });
		// }
	}

	@Subscribe
	public void refreshWithUiAccess(final WidgetRefreshEvent event) {
		// UI ui = getUI().orElse(null);
		// if (ui != null) {
		// ui.access(() -> {
		// try {
		// counter--;
		// if (counter <= 0) {
		// refreshCurrentPage();
		// ui.push();
		// counter = new Random().nextInt(ALARMED_LABEL_COUNT) + ALARMED_LABEL_COUNT;
		// }
		// } catch (Throwable e) {
		// logger.error("refreshWithUiAccess", e);
		// }
		// });
		// }
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		uiEventBus.register(this);
	}

	@Override
	protected void onDetach(DetachEvent detachEvent) {
		uiEventBus.unregister(this);
		super.onDetach(detachEvent);
	}

	@Override
	public void enableButtons(Device item) {
		super.enableButtons(item);
		if (item == null) {
			return;
		}
		// for (Component component : getButtonsLayout()) {
		// if (!(component instanceof Button)) {
		// continue;
		// }
		// Button button = (Button) component;
		// String id = button.getId().orElse("");
		// boolean activated = !item.isDeActivated();
		// if (id.contains(MODIFY_BUTTON)) {
		// button.setEnabled(activated);
		// } else if (id.contains(ALARM_BUTTON)) {
		// button.setEnabled(!DeviceStatus.PRODUCED.equals(item.getStatus()) &&
		// activated);
		// } else if (id.contains(DOWNLOAD_BUTTON)) {
		// button.setEnabled(item.isAvailableForVisualization() && activated);
		// } else if (id.contains(RESET_BUTTON) || id.contains(REMOVE_BUTTON)) {
		// boolean enabled = item.getNetwork() == null && item.getMaster() == null;
		// if (enabled && id.contains(REMOVE_BUTTON) && item.isSticky()) {
		// enabled = !supervisor || production;
		// }
		// button.setEnabled(enabled);
		// }
		// }
	}

	private void setupDeviceWidgetBox(Grid<Device> table, VerticalLayout listingLayout) {
		if (!hasParameters) {
			return;
		}
		boxes = deviceWidgetBoxProvider.getObject();

		table.addSelectionListener(event -> boxes.refresh(event.getFirstSelectedItem().orElse(null)));
		boxes.addListener(new EditorSavedListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void editorSaved(EditorSavedEvent event) {
				if (event.getSavedItem() instanceof GroupWidget) {
					openVisualization((GroupWidget) event.getSavedItem());
				}
				if (event.getSavedItem() instanceof Device) {
					openProvisioning((Device) event.getSavedItem());
				}
			}
		});

		// if (UIUtils.isMobile()) {
		// return;
		// }

		SplitLayout splitPanel = new SplitLayout();
		splitPanel.setSizeFull();
		splitPanel.addToPrimary(table);
		splitPanel.addToSecondary(boxes);
		splitPanel.setSplitterPosition(60);
		table.getParent().ifPresent(parent -> {
			if (parent instanceof HasComponents) {
				((HasComponents) parent).remove(table);
			}
		});
		listingLayout.add(splitPanel);
		listingLayout.setFlexGrow(1f, splitPanel);
	}

	private static final class DevicesFilter {
		private String label;
		private AlarmStatus alarmStatus;
		private DeviceStatus deviceStatus;
		private String owner;

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public boolean hasLabel() {
			return label != null && !label.trim().isEmpty();
		}

		public AlarmStatus getAlarmStatus() {
			return alarmStatus;
		}

		public void setAlarmStatus(AlarmStatus alarmStatus) {
			this.alarmStatus = alarmStatus;
		}

		public boolean hasAlarmStatus() {
			return alarmStatus != null;
		}

		public DeviceStatus getDeviceStatus() {
			return deviceStatus;
		}

		public void setDeviceStatus(DeviceStatus deviceStatus) {
			this.deviceStatus = deviceStatus;
		}

		public boolean hasDeviceStatus() {
			return deviceStatus != null;
		}

		public String getOwner() {
			return owner;
		}

		public void setOwner(String owner) {
			this.owner = owner;
		}

		public boolean hasOwner() {
			return owner != null && !owner.trim().isEmpty();
		}
	}

	private static final class DevicesQueryDefinition extends LazyQueryDefinition<Device, DevicesFilter>
			implements FilterableQueryDefinition<DevicesFilter> {
		private static final long serialVersionUID = 1L;
		private DevicesFilter queryFilter;
		private Network network;
		private final Permissions permissions;
		private String owner;
		private int pageSize;

		public DevicesQueryDefinition(Class<Device> beanClass, int batchSize, Permissions permissions) {
			super(beanClass, batchSize);
			this.permissions = permissions;
		}

		@Override
		public void setQueryFilter(DevicesFilter filter) {
			this.queryFilter = filter;
		}

		@Override
		public DevicesFilter getQueryFilter() {
			return queryFilter;
		}

		public Network getNetwork() {
			return network;
		}

		public void setNetwork(Network network) {
			this.network = network;
		}

		public Permissions getPermissions() {
			return permissions;
		}

		public String getOwner() {
			return owner;
		}

		public void setOwner(String owner) {
			this.owner = owner;
		}

		public int getPageSize() {
			return pageSize;
		}

		public void setPage(int pageIndex, int pageSize) {
			this.pageSize = pageSize;
		}
	}

	private static final class DevicesQueryFactory implements QueryFactory<Device, DevicesFilter> {
		private final DeviceRepository deviceRepository;
		private final CassandraService cassandraService;

		private DevicesQueryFactory(DeviceRepository deviceRepository, CassandraService cassandraService) {
			this.deviceRepository = deviceRepository;
			this.cassandraService = cassandraService;
		}

		@Override
		public it.thisone.iotter.lazyquerydataprovider.Query<Device, DevicesFilter> constructQuery(
				QueryDefinition<Device, DevicesFilter> queryDefinition) {
			return new DevicesQuery(deviceRepository, cassandraService, (DevicesQueryDefinition) queryDefinition);
		}
	}

	private static final class DevicesQuery
			implements it.thisone.iotter.lazyquerydataprovider.Query<Device, DevicesFilter> {
		private final DeviceRepository deviceRepository;
		private final CassandraService cassandraService;
		private final DevicesQueryDefinition queryDefinition;

		private DevicesQuery(DeviceRepository deviceRepository, CassandraService cassandraService,
				DevicesQueryDefinition queryDefinition) {
			this.deviceRepository = deviceRepository;
			this.cassandraService = cassandraService;
			this.queryDefinition = queryDefinition;
		}

		@Override
		public int size(QueryDefinition<Device, DevicesFilter> queryDefinition) {
			return (int) findPage(0, 1).getTotalElements();
		}

		@Override
		public Stream<Device> loadItems(QueryDefinition<Device, DevicesFilter> queryDefinition, int offset, int limit) {
			int size = limit > 0 ? limit : this.queryDefinition.getPageSize();
			int page = size > 0 ? offset / size : 0;
			if (size <= 0) {
				size = this.queryDefinition.getPageSize() > 0 ? this.queryDefinition.getPageSize() : 100;
			}

			org.springframework.data.domain.Page<Device> devices = findPage(page, size);
			List<Device> deviceList = devices.getContent();
			populateAlarmStatusBatch(deviceList);
			DevicesFilter filter = this.queryDefinition.getQueryFilter();
			Stream<Device> stream = deviceList.stream();
			if (filter != null && filter.hasAlarmStatus()) {
				stream = stream.filter(d -> filter.getAlarmStatus().equals(d.getAlarmStatus()));
			}
			return stream;
		}

		private org.springframework.data.domain.Page<Device> findPage(int page, int size) {
			org.springframework.data.domain.Sort sort = buildSort();
			org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page,
					size, sort);
			DevicesFilter filter = queryDefinition.getQueryFilter();
			String label = filter != null && filter.hasLabel() ? filter.getLabel().trim() : null;
			DeviceStatus deviceStatus = filter != null && filter.hasDeviceStatus() ? filter.getDeviceStatus() : null;
			String ownerFilter = filter != null && filter.hasOwner() ? filter.getOwner().trim() : null;

			if (queryDefinition.getNetwork() != null) {
				String owner = queryDefinition.getNetwork().getOwner();
				String networkId = queryDefinition.getNetwork().getId();
				if (label != null && deviceStatus != null) {
					return deviceRepository.findByOwnerAndNetworkIdAndLabelStartingWithIgnoreCaseAndStatus(owner,
							networkId, label, deviceStatus, pageable);
				} else if (label != null) {
					return deviceRepository.findByOwnerAndNetworkIdAndLabelStartingWithIgnoreCase(owner, networkId,
							label, pageable);
				} else if (deviceStatus != null) {
					return deviceRepository.findByOwnerAndNetworkIdAndStatus(owner, networkId, deviceStatus, pageable);
				}
				return deviceRepository.findByOwnerAndNetworkId(owner, networkId, pageable);
			}

			if (queryDefinition.getPermissions().isViewAllMode()) {
				if (ownerFilter != null && label != null && deviceStatus != null) {
					return deviceRepository.findByOwnerStartingWithIgnoreCaseAndLabelStartingWithIgnoreCaseAndStatus(
							ownerFilter, label, deviceStatus, pageable);
				} else if (ownerFilter != null && label != null) {
					return deviceRepository.findByOwnerStartingWithIgnoreCaseAndLabelStartingWithIgnoreCase(ownerFilter,
							label, pageable);
				} else if (ownerFilter != null && deviceStatus != null) {
					return deviceRepository.findByOwnerStartingWithIgnoreCaseAndStatus(ownerFilter, deviceStatus,
							pageable);
				} else if (ownerFilter != null) {
					return deviceRepository.findByOwnerStartingWithIgnoreCase(ownerFilter, pageable);
				} else if (label != null && deviceStatus != null) {
					return deviceRepository.findByLabelStartingWithIgnoreCaseAndStatus(label, deviceStatus, pageable);
				} else if (label != null) {
					return deviceRepository.findByLabelStartingWithIgnoreCase(label, pageable);
				} else if (deviceStatus != null) {
					return deviceRepository.findByStatus(deviceStatus, pageable);
				}
				return deviceRepository.findAll(pageable);
			}

			String owner = queryDefinition.getOwner();
			if (label != null && deviceStatus != null) {
				return deviceRepository.findByOwnerAndLabelStartingWithIgnoreCaseAndStatus(owner, label, deviceStatus,
						pageable);
			} else if (label != null) {
				return deviceRepository.findByOwnerAndLabelStartingWithIgnoreCase(owner, label, pageable);
			} else if (deviceStatus != null) {
				return deviceRepository.findByOwnerAndStatus(owner, deviceStatus, pageable);
			}
			return deviceRepository.findByOwner(owner, pageable);
		}

		private org.springframework.data.domain.Sort buildSort() {
			List<QuerySortOrder> sortOrders = queryDefinition.getSortOrders();
			if (sortOrders == null || sortOrders.isEmpty()) {
				return org.springframework.data.domain.Sort.by("serial").ascending()
						.and(org.springframework.data.domain.Sort.by(LABEL).ascending());
			}
			List<org.springframework.data.domain.Sort.Order> orders = new ArrayList<>();
			for (QuerySortOrder sortOrder : sortOrders) {
				String property = sortOrder.getSorted();
				org.springframework.data.domain.Sort.Direction direction = sortOrder
						.getDirection() == SortDirection.ASCENDING
								? org.springframework.data.domain.Sort.Direction.ASC
								: org.springframework.data.domain.Sort.Direction.DESC;
				orders.add(new org.springframework.data.domain.Sort.Order(direction, property));
			}
			boolean hasLabelSorting = sortOrders.stream().anyMatch(so -> LABEL.equals(so.getSorted()));
			if (!hasLabelSorting) {
				orders.add(new org.springframework.data.domain.Sort.Order(
						org.springframework.data.domain.Sort.Direction.ASC, LABEL));
			}
			return org.springframework.data.domain.Sort.by(orders);
		}

		private void populateAlarmStatusBatch(List<Device> devices) {
			for (Device device : devices) {
				try {
					Date lastContactDate = cassandraService.getMeasures().getLastTick(device.getSerial(), null);
					device.changedAlarmStatus(lastContactDate, device.isAlarmed(), hasActiveAlarms(device));
				} catch (Exception e) {
					logger.error("Error populating alarm status for device {}", device.getSerial(), e);
				}
			}
		}

		private boolean hasActiveAlarms(Device device) {
			if (device.getChannels() == null || device.getChannels().isEmpty()) {
				return false;
			}
			return device.getChannels().stream()
					.anyMatch(ch -> ch.getConfiguration() != null && ch.getConfiguration().isActive()
							&& it.thisone.iotter.util.Utils.isTypeAlarm(ch.getMetaData()));
		}

		private long countTotal() {
			return findPage(0, 1).getTotalElements();
		}
	}

	private Range<Date> getMeasuresRange(Device device) {
		Date first = UIUtils.getCassandraService().getMeasures().getFirstTick(device.getSerial(), null);
		if (first == null) {
			first = new Date();
		}
		Date startDate = device.getActivationDate() != null ? device.getActivationDate() : device.getProductionDate();
		if (startDate != null && first.before(startDate)) {
			first = startDate;
		}
		Date last = UIUtils.getCassandraService().getMeasures().getLastTick(device.getSerial(), null);
		if (last == null) {
			last = new Date();
		}
		if (first.equals(last)) {
			return Range.singleton(first);
		}
		if (first.before(last)) {
			return Range.closedOpen(first, last);
		}
		return null;
	}
}
