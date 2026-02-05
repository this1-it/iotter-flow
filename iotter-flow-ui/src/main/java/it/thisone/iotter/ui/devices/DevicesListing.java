package it.thisone.iotter.ui.devices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.google.common.collect.Range;
import com.google.common.eventbus.Subscribe;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;

import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.AlarmStatus;
import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.enums.ExportFileMode;
import it.thisone.iotter.enums.ExportFormat;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.exporter.ExportConfig;
import it.thisone.iotter.exporter.ExportProperties;
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
import it.thisone.iotter.persistence.repository.DeviceRepository;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.provisioning.ProvisionedEvent;
import it.thisone.iotter.security.EntityPermission;
import it.thisone.iotter.security.Permissions;
import it.thisone.iotter.ui.common.AbstractBaseEntityDetails;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.AbstractBaseEntityListing;
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
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.common.export.ExportDialog;
import it.thisone.iotter.ui.eventbus.DeviceChangedEvent;
import it.thisone.iotter.ui.eventbus.WidgetRefreshEvent;
import it.thisone.iotter.ui.groupwidgets.GroupWidgetVisualizer;
import it.thisone.iotter.ui.ifc.IProvisioningWizard;
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
	
	private Network network;
	private boolean hasParameters = true;
	private TabSheet tabsheet;
	private DeviceWidgetBox boxes;
	private IProvisioningWizard wizard;
	private final Permissions permissions;
	private final boolean supervisor = UIUtils.getUserDetails().hasRole(Constants.ROLE_SUPERVISOR);
	private final boolean production = UIUtils.getUserDetails().hasRole(Constants.ROLE_PRODUCTION);
	private int counter = ALARMED_LABEL_COUNT;

	@Autowired
	private DeviceRepository deviceRepository;

	@Autowired
	private DeviceService deviceService;

	private Grid<Device> grid;
	private LazyQueryDataProvider<Device, DevicesFilter> dataProvider;
	private DevicesQueryDefinition queryDefinition;
	private DevicesFilter currentFilter = new DevicesFilter();
	private int currentLimit = DEFAULT_LIMIT;

	public DevicesListing() {
		this(PermissionsUtils.getPermissionsForDeviceEntity());
	}

	private DevicesListing(Permissions permissions) {
		super(Device.class, DEVICE_VIEW, DEVICE_VIEW, false, permissions);
		this.permissions = permissions;
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
		buildLayout();
	}

	private void buildLayout() {
		HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.setWidth(100, Unit.PERCENTAGE);
		toolbar.setSpacing(true);
		toolbar.setMargin(true);
		toolbar.addStyleName(UIUtils.TOOLBAR_STYLE);

		queryDefinition = new DevicesQueryDefinition(Device.class, currentLimit, permissions);
		queryDefinition.setNetwork(network);
		queryDefinition.setOwner(UIUtils.getUserDetails().getTenant());
		queryDefinition.setPage(0, currentLimit);
		queryDefinition.setQueryFilter(currentFilter);
		dataProvider = new LazyQueryDataProvider<>(queryDefinition, new DevicesQueryFactory(deviceRepository));
		dataProvider.setCacheQueries(false);
		dataProvider.setFilter(currentFilter);
		setBackendDataProvider(dataProvider);

		grid = createGrid();

		VerticalLayout listingLayout = createListingLayout(toolbar, grid);
		setSelectable(grid);
		setupDeviceWidgetBox(grid, listingLayout);

		if (hasParameters) {
			tabsheet = new TabSheet();
			tabsheet.addStyleName(ValoTheme.TABSHEET_FRAMED);
			tabsheet.setSizeFull();
			tabsheet.addTab(listingLayout, getI18nLabel("devices_parameters"));
			getMainLayout().addComponent(tabsheet);
			getMainLayout().setExpandRatio(tabsheet, 1f);
		} else {
			listingLayout.setCaption(null);
			getMainLayout().addComponent(listingLayout);
			getMainLayout().setExpandRatio(listingLayout, 1f);
		}

		updateTotalCount();

		getButtonsLayout().addComponent(createExportDataButton());
		getButtonsLayout().addComponent(createAlarmsButton());
		getButtonsLayout().addComponent(createResetButton());
		getButtonsLayout().addComponent(createRemoveButton());
		getButtonsLayout().addComponent(createModifyButton());
		getButtonsLayout().addComponent(createActivateButton());
		getButtonsLayout().addComponent(createAddButton());
		getButtonsLayout().addComponent(createImportButton());
		if (hasParameters && UIUtils.isMobile()) {
			getButtonsLayout().addComponent(createDeviceWidgetButton());
		}
		toolbar.addComponent(getButtonsLayout());
		toolbar.setComponentAlignment(getButtonsLayout(), Alignment.MIDDLE_RIGHT);
		enableButtons(null);
	}

	@Override
	public AbstractBaseEntityForm<Device> getEditor(Device item) {
		return new DeviceForm(item, network);
	}

	@Override
	public AbstractBaseEntityDetails<Device> getDetails(Device item, boolean remove) {
		return new DeviceDetails(item, remove);
	}

	private void refreshData() {
		dataProvider.refreshAll();
		updateTotalCount();
		grid.scrollToStart();
		grid.asSingleSelect().clear();
		enableButtons(null);
	}

	private void refreshCurrentPage() {
		refreshData();
	}

	private long getTotalCount() {
		return new DevicesQuery(deviceRepository, queryDefinition).countTotal();
	}

	private void updateTotalCount() {
		long total = getTotalCount();
		setTotalSize(total);
	}

	private Grid<Device> createGrid() {
		Grid<Device> grid = new Grid<>();
		grid.setDataProvider(dataProvider);
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.setSizeFull();
		grid.addStyleName(UIUtils.TABLE_STYLE);

		List<Grid.Column<Device, ?>> columns = new ArrayList<>();

		// Always visible columns
		Grid.Column<Device, String> labelColumn = grid.addColumn(this::formatLabel).setId(LABEL);
		columns.add(labelColumn);

		Grid.Column<Device, String> profileColumn = grid.addColumn(this::formatProfile).setId("profile");
		columns.add(profileColumn);

		Grid.Column<Device, String> modelColumn = grid.addColumn(this::formatModel).setId("model");
		columns.add(modelColumn);

		Grid.Column<Device, String> alarmStatusColumn = grid.addColumn(this::formatAlarmStatus).setId(ALARM_STATUS);
		columns.add(alarmStatusColumn);

		if (network == null) {
			Grid.Column<Device, String> networkColumn = grid.addColumn(this::formatNetwork).setId("network");
			columns.add(networkColumn);
		}

		Grid.Column<Device, String> statusColumn = grid.addColumn(this::formatDeviceStatus).setId(DEVICE_STATUS);
		columns.add(statusColumn);

		// Conditional columns (viewAll mode)
		if (permissions.isViewAllMode()) {
			Grid.Column<Device, String> ownerColumn = grid.addColumn(Device::getOwner).setId(OWNER);
			columns.add(ownerColumn);

			Grid.Column<Device, String> serialColumn = grid.addColumn(Device::getSerial).setId("serial");
			columns.add(serialColumn);

			Grid.Column<Device, Boolean> publishingColumn = grid.addColumn(Device::isPublishing).setId("publishing");
			columns.add(publishingColumn);

			Grid.Column<Device, Boolean> tracingColumn = grid.addColumn(Device::isTracing).setId("tracing");
			columns.add(tracingColumn);
		}

		// Disable sorting on all columns except label, alarmStatus, deviceStatus, owner
		for (Grid.Column<Device, ?> column : columns) {
			String columnId = column.getId();
			if (!LABEL.equals(columnId) && !ALARM_STATUS.equals(columnId) && !DEVICE_STATUS.equals(columnId)
					&& !OWNER.equals(columnId)) {
				column.setSortable(false);
			}
		}

		// Set i18n captions
		for (Grid.Column<Device, ?> column : columns) {
			column.setCaption(getI18nLabel(column.getId()));
		}

		grid.setColumnOrder(columns.toArray(new Grid.Column[0]));
		initFilters(grid);
		return grid;
	}

	private void initFilters(Grid<Device> grid) {
		HeaderRow filterRow = grid.appendHeaderRow();

		// LABEL FILTER (TextField)
		TextField labelField = new TextField();
		labelField.setPlaceholder("Filter...");
		labelField.setWidth(100, Unit.PERCENTAGE);
		labelField.setStyleName(ValoTheme.TEXTFIELD_TINY);
		labelField.setValueChangeMode(ValueChangeMode.LAZY);
		filterRow.getCell(LABEL).setComponent(labelField);

		labelField.addValueChangeListener(event -> {
			currentFilter.setLabel(event.getValue());
			queryDefinition.setQueryFilter(currentFilter);
			setFilter(currentFilter);
			refreshCurrentPage();
		});

		// ALARM STATUS FILTER (ComboBox)
		ComboBox<AlarmStatus> alarmStatusCombo = new ComboBox<>();
		alarmStatusCombo.setPlaceholder("Filter...");
		alarmStatusCombo.setWidth(100, Unit.PERCENTAGE);
		alarmStatusCombo.setStyleName(ValoTheme.COMBOBOX_TINY);
		alarmStatusCombo.setEmptySelectionAllowed(true);
		alarmStatusCombo.setTextInputAllowed(false);
		alarmStatusCombo.setItems(AlarmStatus.values());
		alarmStatusCombo.setItemCaptionGenerator(
				status -> getI18nLabel("enum.alarmstatus." + status.name().toLowerCase()));
		filterRow.getCell(ALARM_STATUS).setComponent(alarmStatusCombo);

		alarmStatusCombo.addValueChangeListener(event -> {
			currentFilter.setAlarmStatus(event.getValue());
			queryDefinition.setQueryFilter(currentFilter);
			setFilter(currentFilter);
			refreshCurrentPage();
		});

		// DEVICE STATUS FILTER (ComboBox)
		ComboBox<DeviceStatus> deviceStatusCombo = new ComboBox<>();
		deviceStatusCombo.setPlaceholder("Filter...");
		deviceStatusCombo.setWidth(100, Unit.PERCENTAGE);
		deviceStatusCombo.setStyleName(ValoTheme.COMBOBOX_TINY);
		deviceStatusCombo.setEmptySelectionAllowed(true);
		deviceStatusCombo.setTextInputAllowed(false);
		deviceStatusCombo.setItems(DeviceStatus.values());
		deviceStatusCombo.setItemCaptionGenerator(
				status -> getI18nLabel("enum.devicestatus." + status.name().toLowerCase()));
		filterRow.getCell(DEVICE_STATUS).setComponent(deviceStatusCombo);

		deviceStatusCombo.addValueChangeListener(event -> {
			currentFilter.setDeviceStatus(event.getValue());
			queryDefinition.setQueryFilter(currentFilter);
			setFilter(currentFilter);
			refreshCurrentPage();
		});

		// OWNER FILTER (TextField, only in viewAll mode)
		if (permissions.isViewAllMode()) {
			TextField ownerField = new TextField();
			ownerField.setPlaceholder("Filter...");
			ownerField.setWidth(100, Unit.PERCENTAGE);
			ownerField.setStyleName(ValoTheme.TEXTFIELD_TINY);
			ownerField.setValueChangeMode(ValueChangeMode.LAZY);
			filterRow.getCell(OWNER).setComponent(ownerField);

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
		if (label.length() <= 40) {
			return label;
		}
		return label.substring(0, 40) + "...";
	}

	private String formatModel(Device device) {
		if (device.getModel() == null) {
			return "";
		}
		return device.getModel().getName();
	}

	private String formatAlarmStatus(Device device) {
		AlarmStatus status = device.getAlarmStatus();
		if (status == null) {
			return "";
		}
		return getI18nLabel("enum.alarmstatus." + status.name().toLowerCase());
	}

	private String formatDeviceStatus(Device device) {
		DeviceStatus status = device.getStatus();
		if (status == null) {
			return "";
		}
		return getI18nLabel("enum.devicestatus." + status.name().toLowerCase());
	}

	private String formatNetwork(Device device) {
		Network net = device.getNetwork();
		return net != null ? net.getName() : "";
	}

	private VerticalLayout createListingLayout(HorizontalLayout toolbar, Grid<Device> grid) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.addComponent(toolbar);
		layout.addComponent(grid);
		layout.setExpandRatio(grid, 1f);
		return layout;
	}

	private Button createAddButton() {
		Button button = new Button();
		button.setIcon(UIUtils.ICON_ADD);
		button.setDescription(getI18nLabel("add"));
		button.setId("add" + getId() + ALWAYS_ENABLED_BUTTON);
		button.addClickListener(event -> openEditor(new Device(), getI18nLabel("add_dialog")));
		button.setVisible(permissions.isCreateMode());
		return button;
	}

	private Button createViewButton() {
		Button button = new Button();
		button.setIcon(UIUtils.ICON_DETAILS);
		button.setDescription(getI18nLabel("view_action"));
		button.addClickListener(event -> openDetails(getCurrentValue(), getI18nLabel("view_dialog"), false));
		button.setVisible(permissions.isViewMode());
		return button;
	}

	private Button createModifyButton() {
		Button button = new Button();
		button.setIcon(UIUtils.ICON_MODIFY);
		button.setDescription(getI18nLabel("modify_action"));
		button.setId(MODIFY_BUTTON);
		button.addClickListener(event -> openEditor(getCurrentValue(), getI18nLabel("modify_dialog")));
		button.setVisible(permissions.isModifyMode());
		return button;
	}

	private Button createRemoveButton() {
		Button button = new Button();
		button.setIcon(UIUtils.ICON_REMOVE);
		button.setDescription(getI18nLabel("remove_action"));
		button.setId(REMOVE_BUTTON);
		button.addClickListener(event -> openDetails(getCurrentValue(), getI18nLabel("remove_dialog"), true));
		button.setVisible(permissions.isRemoveMode());
		return button;
	}

	private Button createActivateButton() {
		Button button = new Button();
		button.setIcon(UIUtils.ICON_ACTIVATE);
		button.setDescription(getI18nLabel("activate_button"));
		button.setId("activate_button" + ALWAYS_ENABLED_BUTTON);
		button.addClickListener(event -> openActivation());
		button.setVisible(UIUtils.hasPermission(EntityPermission.DEVICE.ACTIVATE));
		return button;
	}

	private Button createResetButton() {
		Button button = new Button();
		if (supervisor) {
			button.setIcon(UIUtils.ICON_RESET);
			button.setDescription(getI18nLabel("reset_button"));
		} else {
			button.setIcon(UIUtils.ICON_REMOVE);
			button.setDescription(getI18nLabel("remove_button"));
		}
		button.setId(RESET_BUTTON);
		button.addClickListener(event -> openReset(getCurrentValue()));
		button.setVisible(UIUtils.hasPermission(EntityPermission.DEVICE.RESET));
		return button;
	}

	private Button createAlarmsButton() {
		Button button = new Button();
		button.setIcon(VaadinIcons.BELL);
		button.setDescription(getI18nLabel("alarms_button"));
		button.setId(ALARM_BUTTON);
		button.addClickListener(event -> openAlarms(getCurrentValue()));
		return button;
	}

	private Button createExportDataButton() {
		Button button = new Button();
		button.setIcon(VaadinIcons.DOWNLOAD);
		button.setDescription(getI18nLabel("export_button"));
		button.setId(DOWNLOAD_BUTTON);
		button.addClickListener(event -> openDownload(getCurrentValue()));
		button.setVisible(UIUtils.hasPermission(EntityPermission.DEVICE.EXPORT_DATA));
		return button;
	}

	private Button createImportButton() {
		Button button = new Button();
		button.setIcon(VaadinIcons.UPLOAD);
		button.setDescription(getI18nLabel("import_production"));
		button.setId("import_button" + ALWAYS_ENABLED_BUTTON);
		button.addClickListener(event -> openImporter());
		button.setVisible(UIUtils.hasPermission(EntityPermission.DEVICE.IMPORT));
		return button;
	}

	private Button createDeviceWidgetButton() {
		Button button = new Button();
		button.setIcon(VaadinIcons.INFO);
		button.setDescription(getI18nLabel("groupwidgetbox_button"));
		button.addClickListener(event -> openGroupWidgetBox());
		button.setVisible(UIUtils.hasPermission(EntityPermission.DEVICE.MODIFY));
		return button;
	}

	private void openEditor(Device item, String label) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityForm<Device> editor = getEditor(item);
		Window dialog = createDialog(label, editor, editor.getWindowDimension(), editor.getWindowStyle());
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
				PopupNotification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
			}
		});
		UI.getCurrent().addWindow(dialog);
	}

	private void openActivation() {
		final DeviceActivatorForm content = new DeviceActivatorForm(network);
		Window dialog = createDialog(getI18nLabel("device_activation"), content, content.getWindowDimension(),
				content.getWindowStyle());
		content.setSavedHandler(item -> {
			if (item != null) {
				String deviceSerial = item.getSerial();
				String activationKey = item.getActivationKey();
				String username = UIUtils.getUserDetails().getUsername();
				
				try {
					UIUtils.getServiceFactory().getSubscriptionService().factoryReset(deviceSerial, activationKey);
					UIUtils.getServiceFactory().getUserService().deviceActivation(deviceSerial,
							activationKey, username, item.getNetwork());
				} catch (BackendServiceException e) {
					logger.debug("device_activation", e);
				}

				refreshCurrentPage();
				PopupNotification.show(getI18nLabel("device_activated"));
			} else {
				PopupNotification.show(getI18nLabel("device_not_activated"), Notification.Type.ERROR_MESSAGE);
			}
			dialog.close();
		});
		UI.getCurrent().addWindow(dialog);
	}

	private void openImporter() {
		final DeviceProductionImporter content = new DeviceProductionImporter();
		Window dialog = createDialog(getI18nLabel("import_production"), content, content.getWindowDimension(),
				content.getWindowStyle());
		content.addListener(new EditorSelectedListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void editorSelected(EditorSelectedEvent event) {
				dialog.close();
				if (event.getSelected() != null) {
					@SuppressWarnings("unchecked")
					Set<Device> items = event.getSelected();
					for (Device item : items) {
						try {
							deviceService.create(item);
						} catch (Exception e) {
							PopupNotification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
						}
					}
					refreshCurrentPage();
				}
			}
		});
		UI.getCurrent().addWindow(dialog);
	}

	private void openAlarms(Device device) {
		if (device == null) {
			return;
		}
		final DeviceAlarmsDetails content = new DeviceAlarmsDetails(device);
		Window dialog = createDialog(getI18nLabel("device_alarms"), content, content.getWindowDimension(),
				content.getWindowStyle());
		content.addListener(new EntitySelectedListener() {
			@Override
			public void entitySelected(EntitySelectedEvent<?> event) {
				dialog.close();
			}
		});
		UI.getCurrent().addWindow(dialog);
	}

	private void openDownload(Device device) {
		if (device == null) {
			return;
		}
		ExportProperties props = new ExportProperties();
		props.setLegacy(false);
		props.setTimeZone(UIUtils.getBrowserTimeZone());
		props.setFileMode(ExportFileMode.SINGLE);
		props.setFormat(ExportFormat.EXCEL);
		props.setLocale(UIUtils.getLocale());

		ExportConfig config = new ExportConfig();
		config.setInterpolation(Interpolation.RAW);

		String lockId = device.getSerial();
		if (UIUtils.getCassandraService().getRollup().existLockSink(lockId)) {
			PopupNotification.show(UIUtils.localize("export.already_running_export"), Notification.Type.ERROR_MESSAGE);
			return;
		}

		Range<Date> interval = Range.all();
		if (props.isLegacy()) {
			interval = getMeasuresRange(device);
		} else {
			interval = UIUtils.getCassandraService().getMeasures().getMeasuresSetRange(device.getSerial());
		}

		if (interval == null) {
			PopupNotification.show("Export Data NOT available", Notification.Type.ERROR_MESSAGE);
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

		ExportDialog dialog = new ExportDialog(config, props, device);
		dialog.setCaption("Export " + device.getLabel());
		dialog.resize(UIUtils.L_DIMENSION);
		UI.getCurrent().addWindow(dialog);
	}

	private void openReset(Device device) {
		if (device == null) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(VaadinIcons.WARNING.getHtml());
		sb.append("&nbsp;<b>");
		sb.append(device.getLabel());
		sb.append("</b>&nbsp;");
		if (supervisor) {
			sb.append(getI18nLabel("reset_warning"));
		} else {
			sb.append(getI18nLabel("delete_warning"));
		}
		sb.append("&nbsp;");
		com.vaadin.ui.Label label = new com.vaadin.ui.Label(sb.toString(), ContentMode.HTML);
		String caption = getI18nLabel("device_reset");
		if (!supervisor) {
			caption = getI18nLabel("remove_dialog");
		}
		VerticalLayout layout = new VerticalLayout();
		layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		layout.addComponent(label);
		layout.setMargin(true);
		layout.setSizeFull();
		Callback callback = new Callback() {
			@Override
			public void onDialogResult(boolean result) {
				if (!result) {
					return;
				}
				try {
					UIUtils.getServiceFactory().getSubscriptionService().factoryReset(device.getSerial(),
							device.getActivationKey());
					ProvisionedEvent evt = new ProvisionedEvent(device, "");
					UIUtils.getServiceFactory().getSubscriptionService().provisioned(evt);
				} catch (BackendServiceException e) {
					PopupNotification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
				}
				if (boxes != null) {
					boxes.refresh(null);
				}
				refreshCurrentPage();
			}
		};
		UI.getCurrent().addWindow(new ConfirmationDialog(caption, layout, callback));
	}

	private void openGroupWidgetBox() {
		if (boxes == null) {
			return;
		}
		Window dialog = createDialog(getI18nLabel("groupwidgetbox_device"), boxes, boxes.getWindowDimension(),
				boxes.getWindowStyle());
		UI.getCurrent().addWindow(dialog);
	}

	private void openVisualization(GroupWidget bean) {
		if (tabsheet == null || bean == null || bean == null) {
			return;
		}
		GroupWidgetVisualizer content = new GroupWidgetVisualizer(bean.getId().toString(), true);
		Tab existing = tabsheet.getTab(content);
		if (existing != null) {
			tabsheet.removeTab(existing);
		}
		Tab tab = tabsheet.addTab(content);
		tab.setClosable(true);
		tabsheet.setSelectedTab(tab);
		tabsheet.setCloseHandler((sheet, tabContent) -> {
			Tab current = sheet.getTab(tabContent);
			if (tabContent instanceof GroupWidgetVisualizer) {
				((GroupWidgetVisualizer) tabContent).removeListeners();
			}
			sheet.removeTab(current);
		});
	}

	private void openProvisioning(Device bean) {
		if (bean == null) {
			return;
		}
		wizard = UIUtils.getUiFactory().getDeviceUiFactory().getProvisioningWizard(bean.getSerial());
		if (wizard == null) {
			return;
		}
		Window dialog = createDialog(getI18nLabel("device_provisioning"), (Component) wizard, wizard.getWindowDimension(),
				wizard.getWindowStyle());
		wizard.addListener(new EditorSavedListener() {
			@Override
			public void editorSaved(EditorSavedEvent event) {
				if (event.getSavedItem() != null) {
					refreshCurrentPage();
				}
				dialog.close();
			}
		});
		UI.getCurrent().addWindow(dialog);
	}

	private void openDetails(Device item, String label, boolean remove) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityDetails<Device> details = getDetails(item, remove);
		Window dialog = createDialog(label, details, UIUtils.S_DIMENSION, UIUtils.S_WINDOW_STYLE);
		details.addListener(new EntityRemovedListener() {
			@Override
			public void entityRemoved(EntityRemovedEvent<?> event) {
				dialog.close();
				if (event.getItem() != null) {
					refreshCurrentPage();
				}
			}
		});
		details.addListener(new EntitySelectedListener() {
			@Override
			public void entitySelected(EntitySelectedEvent<?> event) {
				dialog.close();
			}
		});
		UI.getCurrent().addWindow(dialog);
	}

	// Event bus integration for dynamic updates
	@Subscribe
	public void refreshOnDeviceChanged(final DeviceChangedEvent event) {
		UI ui = this.getUI();
		if (ui != null) {
			ui.access(() -> {
				try {
					dataProvider.refreshAll();
					ui.push();
				} catch (Exception e) {
					logger.warn("refreshOnDeviceChanged", e);
				}
			});
		}
	}

	@Subscribe
	public void refreshWithUiAccess(final WidgetRefreshEvent event) {
		if (this.getParent() != null && this.getParent().getUI() != null) {
			UI ui = this.getParent().getUI();
			ui.access(() -> {
				try {
					counter--;
					if (counter <= 0) {
						refreshCurrentPage();
						ui.push();
						Random random = new Random();
						counter = random.nextInt(ALARMED_LABEL_COUNT) + ALARMED_LABEL_COUNT;
						logger.debug("refreshWithUiAccess ALARMED_LABEL_COUNT {}", counter);
					}
				} catch (Throwable e) {
					logger.error("refreshWithUiAccess", e);
				}
			});
		}
	}

	@Override
	public void attach() {
		super.attach();
		UIUtils.getUIEventBus().register(this);
	}

	@Override
	public void detach() {
		UIUtils.getUIEventBus().unregister(this);
		super.detach();
	}

	@Override
	public void enableButtons(Device item) {
		super.enableButtons(item);
		if (item != null) {
			for (Component component : getButtonsLayout()) {
				if (!(component instanceof Button)) {
					continue;
				}
				Button button = (Button) component;
				if (button.getId() == null) {
					continue;
				}
				boolean activated = !item.isDeActivated();
				if (button.getId().contains(MODIFY_BUTTON)) {
					button.setEnabled(activated);
				} else if (button.getId().contains(ALARM_BUTTON)) {
					boolean enabled = !DeviceStatus.PRODUCED.equals(item.getStatus()) && activated;
					button.setEnabled(enabled);
				} else if (button.getId().contains(DOWNLOAD_BUTTON)) {
					boolean enabled = item.isAvailableForVisualization() && activated;
					button.setEnabled(enabled);
				} else if (button.getId().contains(RESET_BUTTON)) {
					boolean enabled = (item.getNetwork() == null) && (item.getMaster() == null);
					button.setEnabled(enabled);
				} else if (button.getId().contains(REMOVE_BUTTON)) {
					boolean enabled = (item.getNetwork() == null) && (item.getMaster() == null);
					if (enabled && item.isSticky()) {
						if (supervisor) {
							enabled = false;
						}
						if (production) {
							enabled = true;
						}
					}
					button.setEnabled(enabled);
				}
			}
		}
	}

	private void setupDeviceWidgetBox(Grid<Device> grid, VerticalLayout listingLayout) {
		if (!hasParameters) {
			return;
		}
		boxes = new DeviceWidgetBox();
		boxes.setSizeFull();

		grid.addSelectionListener(event -> {
			Device selected = event.getFirstSelectedItem().orElse(null);
			if (selected != null) {
				boxes.refresh(selected);
			} else {
				boxes.refresh(null);
			}
		});

		boxes.addListener(new EditorSavedListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void editorSaved(EditorSavedEvent event) {
				if (event.getSavedItem() != null) {
					if (event.getSavedItem() instanceof GroupWidget) {
						openVisualization((GroupWidget) event.getSavedItem());
					}
					if (event.getSavedItem() instanceof Device) {
						openProvisioning((Device) event.getSavedItem());
					}
				}
			}
		});

		if (UIUtils.isMobile()) {
			return;
		}

		HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
		splitPanel.setSizeFull();
		splitPanel.setStyleName(ValoTheme.SPLITPANEL_LARGE);
		splitPanel.setFirstComponent(grid);
		splitPanel.setSecondComponent(boxes);
		splitPanel.setSplitPosition(60, Sizeable.Unit.PERCENTAGE);
		listingLayout.replaceComponent(grid, splitPanel);
		listingLayout.setExpandRatio(splitPanel, 1f);
	}

	// Inner classes for filter infrastructure

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

		@Override
		public String toString() {
			return "DevicesFilter{label=" + label + ", alarmStatus=" + alarmStatus + ", deviceStatus=" + deviceStatus
					+ ", owner=" + owner + "}";
		}
	}

	private static final class DevicesQueryDefinition extends LazyQueryDefinition<Device, DevicesFilter>
			implements FilterableQueryDefinition<DevicesFilter> {

		private static final long serialVersionUID = 1L;

		private DevicesFilter queryFilter;
		private Network network;
		private final Permissions permissions;
		private String owner;
		private int pageIndex;
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

		public int getPageIndex() {
			return pageIndex;
		}

		public int getPageSize() {
			return pageSize;
		}

		public void setPage(int pageIndex, int pageSize) {
			this.pageIndex = pageIndex;
			this.pageSize = pageSize;
		}
	}

	private static final class DevicesQueryFactory implements QueryFactory<Device, DevicesFilter> {
		private final DeviceRepository deviceRepository;

		private DevicesQueryFactory(DeviceRepository deviceRepository) {
			this.deviceRepository = deviceRepository;
		}

		@Override
		public it.thisone.iotter.lazyquerydataprovider.Query<Device, DevicesFilter> constructQuery(
				QueryDefinition<Device, DevicesFilter> queryDefinition) {
			return new DevicesQuery(deviceRepository, (DevicesQueryDefinition) queryDefinition);
		}
	}

	private static final class DevicesQuery
			implements it.thisone.iotter.lazyquerydataprovider.Query<Device, DevicesFilter> {
		private final DeviceRepository deviceRepository;
		private final DevicesQueryDefinition queryDefinition;

		private DevicesQuery(DeviceRepository deviceRepository, DevicesQueryDefinition queryDefinition) {
			this.deviceRepository = deviceRepository;
			this.queryDefinition = queryDefinition;
		}

		@Override
		public int size(QueryDefinition<Device, DevicesFilter> queryDefinition) {
			Page<Device> page = findPage(0, 1);
			long total = page.getTotalElements();
			System.out.println("DevicesQuery.size() - getTotalElements: " + total);
			return (int) total;
		}

		@Override
		public Stream<Device> loadItems(QueryDefinition<Device, DevicesFilter> queryDefinition, int offset,
				int limit) {
			int page = offset / this.queryDefinition.getPageSize();
			int size = this.queryDefinition.getPageSize();
			System.out.println("DevicesQuery.loadItems() - offset: " + offset + ", limit: " + limit
					+ ", calculated page: " + page + ", using pageSize: " + size);
			Page<Device> devices = findPage(page, size);
			System.out.println(
					"DevicesQuery.loadItems() - loaded " + devices.getContent().size() + " devices from page " + page);

			// Populate transient alarmStatus for each device
			List<Device> deviceList = devices.getContent();
			populateAlarmStatusBatch(deviceList);

			// Filter by alarmStatus if filter is set (post-query filtering since @Transient)
			Stream<Device> stream = deviceList.stream();
			DevicesFilter filter = this.queryDefinition.getQueryFilter();
			if (filter != null && filter.hasAlarmStatus()) {
				stream = stream.filter(d -> filter.getAlarmStatus().equals(d.getAlarmStatus()));
			}

			return stream;
		}

		private Page<Device> findPage(int page, int size) {
			Sort sort = buildSort();
			Pageable pageable = PageRequest.of(page, size, sort);
			DevicesFilter filter = queryDefinition.getQueryFilter();

			// Extract filter values
			String label = filter != null && filter.hasLabel() ? filter.getLabel().trim() : null;
			DeviceStatus deviceStatus = filter != null && filter.hasDeviceStatus() ? filter.getDeviceStatus() : null;
			String ownerFilter = filter != null && filter.hasOwner() ? filter.getOwner().trim() : null;

			// PERMISSION ROUTING: Network-scoped mode
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

			// PERMISSION ROUTING: ViewAll mode
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

			// PERMISSION ROUTING: Owner-scoped mode (default)
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

		private Sort buildSort() {
			List<QuerySortOrder> sortOrders = queryDefinition.getSortOrders();

			// Default sorting by serial if no sort specified
			if (sortOrders == null || sortOrders.isEmpty()) {
				return Sort.by(new Sort.Order(Sort.Direction.ASC, "serial"),
						new Sort.Order(Sort.Direction.ASC, LABEL));
			}

			// Convert Vaadin QuerySortOrder to Spring Data Sort
			List<Sort.Order> orders = new ArrayList<>();
			for (QuerySortOrder sortOrder : sortOrders) {
				String property = sortOrder.getSorted();
				Sort.Direction direction = sortOrder.getDirection() == SortDirection.ASCENDING ? Sort.Direction.ASC
						: Sort.Direction.DESC;
				orders.add(new Sort.Order(direction, property));
			}

			// Add secondary sort by label if not already included
			boolean hasLabelSorting = sortOrders.stream().anyMatch(so -> LABEL.equals(so.getSorted()));
			if (!hasLabelSorting) {
				orders.add(new Sort.Order(Sort.Direction.ASC, LABEL));
			}

			return Sort.by(orders);
		}

		private void populateAlarmStatusBatch(List<Device> devices) {
			// Populate transient alarmStatus for each device
			for (Device device : devices) {
				populateAlarmStatus(device);
			}
		}

		private void populateAlarmStatus(Device device) {
			try {
				// Get last contact date from service/cache
				Date lastContactDate = getLastContactDate(device.getSerial());
				boolean alarmed = device.isAlarmed();
				boolean hasActiveAlarms = hasActiveAlarms(device);

				// Calculate and set alarm status
				device.changedAlarmStatus(lastContactDate, alarmed, hasActiveAlarms);
			} catch (Exception e) {
				// If calculation fails, set to UNDEFINED
				logger.error("Error populating alarm status for device " + device.getSerial(), e);
			}
		}

		private Date getLastContactDate(String serial) {
			try {
				// Query Cassandra for last measure timestamp
				return UIUtils.getCassandraService().getMeasures().getLastTick(serial, null);
			} catch (Exception e) {
				return null;
			}
		}

		private boolean hasActiveAlarms(Device device) {
			// Check if device has any active alarm channels
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
		if (first.before(startDate)) {
			first = startDate;
		}
		Date last = UIUtils.getCassandraService().getMeasures().getLastTick(device.getSerial(), null);
		if (last == null) {
			last = new Date();
		}

		Range<Date> interval = null;
		if (first.equals(last)) {
			interval = Range.singleton(first);
		} else if (first.before(last)) {
			interval = Range.closedOpen(first, last);
		}
		return interval;
	}
}
