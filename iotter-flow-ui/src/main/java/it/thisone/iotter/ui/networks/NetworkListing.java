package it.thisone.iotter.ui.networks;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.vaadin.flow.components.TabSheet;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;

import it.thisone.iotter.enums.NetworkType;
import it.thisone.iotter.lazyquerydataprovider.FilterableQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDataProvider;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryFactory;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.repository.NetworkRepository;

import it.thisone.iotter.security.EntityPermission;
import it.thisone.iotter.security.Permissions;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.AbstractBaseEntityListing;


import it.thisone.iotter.ui.common.PermissionsUtils;
import it.thisone.iotter.ui.common.SideDrawer;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.maps.DevicesGoogleMap;
import it.thisone.iotter.ui.maps.DevicesImageOverlayMap;
import it.thisone.iotter.ui.maps.GroupWidgetsCustomMap;
import it.thisone.iotter.ui.maps.GroupWidgetsDevicesListing;
import it.thisone.iotter.ui.maps.GroupWidgetsListingBox;
import it.thisone.iotter.ui.eventbus.UIEventBus;
import it.thisone.iotter.util.PopupNotification;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NetworkListing extends AbstractBaseEntityListing<Network> {

	private static final long serialVersionUID = 1L;
	private static final String MIGRATION_BUTTON = "migration";
	private static final String NETWORKS_VIEW = "networks.view";

	private TabSheet tabsheet;

	@Autowired
	private NetworkRepository networkRepository;

	// @Autowired
	// private AuthenticatedUser authenticatedUser;
	// @Autowired
	// private NetworkService networkService;
	// @Autowired
	// private DeviceService deviceService;
	// @Autowired
	// private NetworkGroupService networkGroupService;
	// @Autowired
	// private GroupWidgetService groupWidgetService;
	@Autowired
	private UIEventBus uiEventBus;
	@Autowired
	private ObjectProvider<GroupWidgetsListingBox> groupWidgetsListingBoxProvider;
	@Autowired
	private ObjectProvider<DevicesImageOverlayMap> devicesImageOverlayMapProvider;

	@Autowired
	private it.thisone.iotter.ui.providers.VisualizerServices visualizerServices;

	@Autowired
	private it.thisone.iotter.ui.providers.BackendServices backendServices;

	@org.springframework.beans.factory.annotation.Value("${googlemap.apikey:}")
	private String googleMapApiKey;

	private Grid<Network> grid;
	private LazyQueryDataProvider<Network, NetworkFilter> dataProvider;
	private NetworkQueryDefinition queryDefinition;
	private NetworkFilter currentFilter = new NetworkFilter();
	private UserDetailsAdapter currentUser;

	public NetworkListing() {
		super(Network.class, NETWORKS_VIEW, NETWORKS_VIEW, false);
	}

	public void init() {
		if (grid != null) {
			return;
		}

		currentUser = backendServices.getAuthenticatedUser().get()
				.orElseThrow(() -> new IllegalStateException("User must be authenticated to edit users"));
		Permissions permissions = PermissionsUtils.getPermissionsForNetworkEntity(currentUser);
		setPermissions(permissions);

		buildLayout();
	}

	private void buildLayout() {
		HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.setWidthFull();
		toolbar.setSpacing(true);
		toolbar.setPadding(true);
		toolbar.addClassName(UIUtils.TOOLBAR_STYLE);

		queryDefinition = new NetworkQueryDefinition(Network.class, DEFAULT_LIMIT, getPermissions());
		queryDefinition.setOwner(currentUser.getTenant());
		queryDefinition.setPage(0, DEFAULT_LIMIT);
		queryDefinition.setQueryFilter(currentFilter);

		dataProvider = new LazyQueryDataProvider<>(queryDefinition, new NetworkQueryFactory(networkRepository));
		dataProvider.setCacheQueries(false);
		dataProvider.setFilter(currentFilter);
		setBackendDataProvider(dataProvider);

		grid = createGrid();
		VerticalLayout contentLayout = createContentLayout(toolbar, grid);
		setSelectable(grid);

		getButtonsLayout().add(createMapViewButton(), createMapEditButton(), createConfigurationsButton(),
				createMigrationButton(), createRemoveButton(), createModifyButton(), createAddButton());
		toolbar.add(getButtonsLayout());
		toolbar.setAlignItems(Alignment.CENTER);
		enableButtons(null);

		tabsheet = new TabSheet();
		tabsheet.addClassName("tabsheet-framed");
		tabsheet.setSizeFull();
		tabsheet.addTab(getI18nLabel("title"), contentLayout);
		getMainLayout().add(tabsheet);
		getMainLayout().setFlexGrow(1f, tabsheet);

		updateTotalCount();
	}

	@Override
	protected AbstractBaseEntityForm<Network> getEditor(Network item, boolean readOnly) {
		return new NetworkForm(item, readOnly);
	}

	private Grid<Network> createGrid() {
		Grid<Network> grid = new Grid<>();
		grid.setDataProvider(dataProvider);
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.setSizeFull();

		List<Grid.Column<Network>> columns = new ArrayList<>();
		columns.add(grid.addColumn(Network::getName).setKey("name"));
		columns.add(grid.addColumn(Network::getDescription).setKey("description"));
		columns.add(grid.addColumn(network -> formatNetworkType(network.getNetworkType())).setKey("networkType"));
		columns.add(grid.addColumn(network -> formatPublic(network.isAnonymous())).setKey("anonymous"));
		columns.add(grid.addColumn(Network::getTimeZone).setKey("timeZone"));

		if (getPermissions().isViewAllMode()) {
			columns.add(grid.addColumn(Network::getOwner).setKey("owner"));
		}

		for (Grid.Column<Network> column : columns) {
			String columnId = column.getKey();
			column.setSortable("name".equals(columnId) || "owner".equals(columnId));
			column.setHeader(getI18nLabel(columnId));
		}

		grid.setColumnOrder(columns.toArray(new Grid.Column[0]));
		initFilters(grid);
		return grid;
	}

	private String formatNetworkType(NetworkType type) {
		return type == null ? "" : getTranslation(type.getI18nKey());
	}

	private String formatPublic(boolean anonymous) {
		return anonymous ? getTranslation("basic.editor.yes") : getTranslation("basic.editor.no");
	}

	private void initFilters(Grid<Network> grid) {
		HeaderRow filterRow = grid.appendHeaderRow();

		TextField name = new TextField();
		name.setPlaceholder("Filter...");
		name.setWidthFull();
		name.addThemeVariants(TextFieldVariant.LUMO_SMALL);
		name.setValueChangeMode(ValueChangeMode.LAZY);
		filterRow.getCell(grid.getColumnByKey("name")).setComponent(name);
		name.addValueChangeListener(event -> {
			currentFilter.setName(event.getValue());
			queryDefinition.setQueryFilter(currentFilter);
			setFilter(currentFilter);
			refreshCurrentPage();
		});

		if (getPermissions().isViewAllMode()) {
			TextField owner = new TextField();
			owner.setPlaceholder("Filter...");
			owner.setWidthFull();
			owner.addThemeVariants(TextFieldVariant.LUMO_SMALL);
			owner.setValueChangeMode(ValueChangeMode.LAZY);
			filterRow.getCell(grid.getColumnByKey("owner")).setComponent(owner);
			owner.addValueChangeListener(event -> {
				currentFilter.setOwner(event.getValue());
				queryDefinition.setQueryFilter(currentFilter);
				setFilter(currentFilter);
				refreshCurrentPage();
			});
		}
	}

	private VerticalLayout createContentLayout(HorizontalLayout toolbar, Grid<Network> grid) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.add(toolbar, grid);
		layout.setFlexGrow(1f, grid);
		return layout;
	}

	private void refreshCurrentPage() {
		dataProvider.refreshAll();
		updateTotalCount();
		grid.scrollToStart();
		grid.deselectAll();
		enableButtons(null);
	}

	private long getTotalCount() {
		return new NetworkQuery(networkRepository, queryDefinition).countTotal();
	}

	private void updateTotalCount() {
		long total = getTotalCount();
		setTotalSize(total);
	}

	private Button createMapViewButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.MAP_MARKER.create());
		button.getElement().setAttribute("title", getI18nLabel("mapview_action"));
		button.addClickListener(event -> openNetworkMap(getCurrentValue()));
		button.setVisible(getPermissions().isViewMode());
		return button;
	}

	private Button createMapEditButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.EDIT.create());
		button.getElement().setAttribute("title", getI18nLabel("map_action"));
		button.addClickListener(event -> openEditableNetworkMap(getCurrentValue()));
		button.setVisible(getPermissions().isModifyMode());
		return button;
	}

	private Button createConfigurationsButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.COG.create());
		button.getElement().setAttribute("title", getI18nLabel("relations_action"));
		button.addClickListener(event -> openNetworkConfigurations(getCurrentValue()));
		button.setVisible(getPermissions().isViewMode());
		return button;
	}

	private Button createMigrationButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.EXCHANGE.create());
		button.getElement().setAttribute("title", getI18nLabel("migration_button"));
		button.setId(MIGRATION_BUTTON);
		button.addClickListener(event -> openMigration(getCurrentValue()));
		button.setVisible(currentUser.hasPermission(EntityPermission.DEVICE.MIGRATE)
				);
		return button;
	}

	private Button createModifyButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.EDIT.create());
		button.getElement().setAttribute("title", getI18nLabel("modify_action"));
		button.addClickListener(event -> openEditor(getCurrentValue(), getI18nLabel("modify_dialog")));
		button.setVisible(getPermissions().isModifyMode());
		return button;
	}

	private Button createRemoveButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.TRASH.create());
		button.getElement().setAttribute("title", getI18nLabel("remove_action"));
		button.addClickListener(event -> openRemove(getCurrentValue()));
		button.setVisible(getPermissions().isRemoveMode());
		return button;
	}

	private Button createAddButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.PLUS.create());
		button.getElement().setAttribute("title", getI18nLabel("add"));
		button.setId("add" + getId() + ALWAYS_ENABLED_BUTTON);
		button.addClickListener(event -> openEditor(new Network(), getI18nLabel("add_dialog")));
		button.setVisible(getPermissions().isCreateMode());
		return button;
	}

	private void openNetworkMap(Network item) {
		if (item == null) {
			return;
		}
		// Legacy Vaadin 8 map flow kept for reference during migration.
		// TODO(flow-migration): Re-enable after map components/navigation are migrated
		// to Flow.
		//
		// if (item.isAnonymous()) {
		// 	String url = UIUtils.getDisplayURL("display", Network.class.getSimpleName(),
		// 			UiConstants.VIEW_MODE_DEFAULT, item.getId());
		// 	UI.getCurrent().getNavigator().navigateTo(url.substring(url.indexOf("!") +
		// 			1));
		// 	return;
		// }

		Component content;
		Network network = backendServices.getNetworkService().findOne(item.getId());
		switch (item.getNetworkType()) {
			case GEOGRAPHIC:
				content = new DevicesGoogleMap(network, false, true, backendServices.getDeviceService(), backendServices.getNetworkService(), googleMapApiKey);
				break;
			case CUSTOM:
				content = new GroupWidgetsCustomMap(item.getId(), false, backendServices,uiEventBus, devicesImageOverlayMapProvider, visualizerServices);
				break;
			default:
				content = new GroupWidgetsDevicesListing(network, backendServices, uiEventBus,
						groupWidgetsListingBoxProvider, visualizerServices);
				break;
		}

		if (content != null) {
			Tab tab = tabsheet.addTab("",content);
			//tab.se(true);
			tabsheet.setSelectedTab(tab);
		}

	}

	private void openEditableNetworkMap(Network item) {
		if (item == null) {
			return;
		}
		// Legacy Vaadin 8 editable-map flow kept for reference during migration.
		// TODO(flow-migration): Re-enable after map editors/events are migrated to
		// Flow.
		//
		// BaseEditor content = null;
		// switch (item.getNetworkType()) {
		// case GEOGRAPHIC:
		// Network network = networkService.findOne(item.getId());
		// content = new DevicesGoogleMap(network, permissions.isModifyMode(), true);
		// break;
		// case CUSTOM:
		// content = new GroupWidgetsCustomMap(item.getId(),
		// permissions.isModifyMode());
		// break;
		// default:
		// PopupNotification.show("This network map is not editable",
		// com.vaadin.ui.Notification.Type.WARNING_MESSAGE);
		// break;
		// }
		//
		// if (content != null) {
		// Tab tab = tabsheet.addTab(content);
		// tab.setIcon(UIUtils.ICON_EDIT);
		// tab.setClosable(true);
		// tabsheet.setSelectedTab(tab);
		// content.addListener(new EditorSavedListener() {
		// @Override
		// public void editorSaved(EditorSavedEvent event) {
		// TabSheet sheet = (TabSheet) event.getComponent().getParent();
		// Tab removed = sheet.getTab(event.getComponent());
		// sheet.removeTab(removed);
		// }
		// });
		// }
		PopupNotification.show("Editable network map migration pending", PopupNotification.Type.WARNING);
	}

	private void openNetworkConfigurations(Network item) {
		if (item == null) {
			return;
		}
		// Legacy Vaadin 8 relations flow kept for reference during migration.
		// TODO(flow-migration): Re-enable after NetworkRelations is migrated to Flow.
		//
		// NetworkRelations relations = new NetworkRelations(item.getId());
		// Tab tab = tabsheet.addTab(relations);
		// tab.setIcon(UIUtils.ICON_NETWORKS);
		// tab.setClosable(true);
		// tabsheet.setSelectedTab(tab);
		PopupNotification.show("Network relations migration pending", PopupNotification.Type.WARNING);
	}

	private void openMigration(Network item) {
		if (item == null) {
			return;
		}
		// Legacy Vaadin 8 migration dialog flow kept for reference during migration.
		// TODO(flow-migration): Re-enable after NetworkDevices dialog/editor is
		// migrated to Flow.
		//
		// NetworkDevices content = new NetworkDevices(item);
		// Window dialog = createDialog(getI18nLabel("device_migration"), content,
		// content.getWindowDimension(), content.getWindowStyle());
		// content.addListener(new EditorSavedListener() {
		// @Override
		// public void editorSaved(EditorSavedEvent event) {
		// dialog.close();
		// }
		// });
		// UI.getCurrent().addWindow(dialog);
		PopupNotification.show("Network device migration dialog migration pending", PopupNotification.Type.WARNING);
	}

	private void openEditor(Network item, String label) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityForm<Network> editor = getEditor(item, false);
		SideDrawer dialog = (SideDrawer) createDialog(label, editor);
		editor.setSavedHandler(entity -> {
			try {
				if (entity.isNew()) {
					backendServices.getNetworkService().create(entity);
				} else {
					backendServices.getNetworkService().update(entity);
				}
				dialog.close();
				refreshCurrentPage();
			} catch (Exception e) {
				PopupNotification.show(e.getMessage(), PopupNotification.Type.ERROR);
			}
		});
		dialog.open();
	}

	@Override
	protected void openDetails(Network item) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityForm<Network> details = getEditor(item, true);
		SideDrawer dialog = (SideDrawer) createDialog(getI18nLabel("view_dialog"), details);
		dialog.open();
	}

	@Override
	protected void openRemove(Network item) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityForm<Network> details = getEditor(item, true);
		SideDrawer dialog = (SideDrawer) createDialog(getI18nLabel("remove_dialog"), details);
		details.setDeleteHandler(entity -> {
			try {
				backendServices.getNetworkService().disconnect(entity);
				dialog.close();
				refreshCurrentPage();
			} catch (Exception e) {
				PopupNotification.show(e.getMessage(), PopupNotification.Type.ERROR);
			}
		});
		dialog.open();
	}

	private static final class NetworkFilter {
		private String name;
		private String owner;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean hasName() {
			return name != null && !name.trim().isEmpty();
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
			return "NetworkFilter{name=" + name + ", owner=" + owner + "}";
		}
	}

	private static final class NetworkQueryDefinition extends LazyQueryDefinition<Network, NetworkFilter>
			implements FilterableQueryDefinition<NetworkFilter> {

		private NetworkFilter queryFilter;
		private final Permissions permissions;
		private String owner;
		private int pageIndex;
		private int pageSize;

		private NetworkQueryDefinition(Class<Network> beanClass, int batchSize, Permissions permissions) {
			super(beanClass, batchSize);
			this.permissions = permissions;
		}

		@Override
		public void setQueryFilter(NetworkFilter filter) {
			this.queryFilter = filter;
		}

		@Override
		public NetworkFilter getQueryFilter() {
			return queryFilter;
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

	private static final class NetworkQueryFactory implements QueryFactory<Network, NetworkFilter> {
		private final NetworkRepository networkRepository;

		private NetworkQueryFactory(NetworkRepository networkRepository) {
			this.networkRepository = networkRepository;
		}

		@Override
		public it.thisone.iotter.lazyquerydataprovider.Query<Network, NetworkFilter> constructQuery(
				QueryDefinition<Network, NetworkFilter> queryDefinition) {
			return new NetworkQuery(networkRepository, (NetworkQueryDefinition) queryDefinition);
		}
	}

	private static final class NetworkQuery
			implements it.thisone.iotter.lazyquerydataprovider.Query<Network, NetworkFilter> {

		private final NetworkRepository networkRepository;
		private final NetworkQueryDefinition queryDefinition;

		private NetworkQuery(NetworkRepository networkRepository, NetworkQueryDefinition queryDefinition) {
			this.networkRepository = networkRepository;
			this.queryDefinition = queryDefinition;
		}

		@Override
		public int size(QueryDefinition<Network, NetworkFilter> queryDefinition) {
			Page<Network> page = findPage(0, 1);
			return (int) page.getTotalElements();

		}

		@Override
		public java.util.stream.Stream<Network> loadItems(
				QueryDefinition<Network, NetworkFilter> queryDefinition, int offset, int limit) {

			int size = limit > 0 ? limit : this.queryDefinition.getPageSize();
			int page = size > 0 ? offset / size : 0;
			if (size <= 0) {
				size = this.queryDefinition.getPageSize() > 0 ? this.queryDefinition.getPageSize() : 100;
			}

			Page<Network> networks = findPage(page, size);
			return networks.getContent().stream();
		}

		private Page<Network> findPage(int page, int size) {
			Sort sort = buildSort();
			Pageable pageable = PageRequest.of(page, size, sort);
			NetworkFilter filter = queryDefinition.getQueryFilter();
			String name = filter != null && filter.hasName() ? filter.getName().trim() : null;
			String ownerFilter = filter != null && filter.hasOwner() ? filter.getOwner().trim() : null;

			if (queryDefinition.getPermissions().isViewAllMode()) {
				if (name != null && ownerFilter != null) {
					return networkRepository.findByNameStartingWithIgnoreCaseAndOwnerStartingWithIgnoreCase(
							name, ownerFilter, pageable);
				} else if (name != null) {
					return networkRepository.findByNameStartingWithIgnoreCase(name, pageable);
				} else if (ownerFilter != null) {
					return networkRepository.findByOwnerStartingWithIgnoreCase(ownerFilter, pageable);
				}
				return networkRepository.findAll(pageable);
			}

			String owner = queryDefinition.getOwner();
			if (name != null) {
				return networkRepository.findByOwnerAndNameStartingWithIgnoreCase(owner, name, pageable);
			}
			return networkRepository.findByOwner(owner, pageable);
		}

		private Sort buildSort() {
			List<QuerySortOrder> sortOrders = queryDefinition.getSortOrders();
			if (sortOrders == null || sortOrders.isEmpty()) {
				return Sort.by("name").ascending();
			}

			List<Sort.Order> orders = new ArrayList<>();
			for (QuerySortOrder sortOrder : sortOrders) {
				String property = sortOrder.getSorted();
				if (!"name".equals(property) && !"owner".equals(property)) {
					continue;
				}
				Sort.Direction direction = sortOrder.getDirection() == SortDirection.ASCENDING
						? Sort.Direction.ASC
						: Sort.Direction.DESC;
				orders.add(new Sort.Order(direction, property));
			}
			if (orders.isEmpty()) {
				return Sort.by("name").ascending();
			}
			return Sort.by(orders);
		}

		private long countTotal() {
			return findPage(0, 1).getTotalElements();
		}
	}
}
