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
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

import it.thisone.iotter.enums.NetworkType;
import it.thisone.iotter.lazyquerydataprovider.FilterableQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDataProvider;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryFactory;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.repository.NetworkRepository;
import it.thisone.iotter.persistence.service.UserService;

import it.thisone.iotter.security.EntityPermission;
import it.thisone.iotter.security.Permissions;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.AbstractBaseEntityListing;
import it.thisone.iotter.ui.common.AuthenticatedUser;
import it.thisone.iotter.ui.common.ConfirmationDialogs;
import it.thisone.iotter.ui.common.PermissionsUtils;
import it.thisone.iotter.ui.common.SideDrawer;
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
	private static final String NETWORKS_VIEW = "networks.view";

	private TabSheet tabsheet;

	@Autowired
	private NetworkRepository networkRepository;

	@Autowired
	private AuthenticatedUser authenticatedUser;
	@Autowired
	private UserService userService;
	@Autowired
	private UIEventBus uiEventBus;
	@Autowired
	private ObjectProvider<GroupWidgetsListingBox> groupWidgetsListingBoxProvider;
	@Autowired
	private ObjectProvider<DevicesImageOverlayMap> devicesImageOverlayMapProvider;
	@Autowired
	private ObjectProvider<NetworkRelations> networkRelationsProvider;

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

		currentUser = authenticatedUser.get()
				.orElseThrow(() -> new IllegalStateException("User must be authenticated to edit users"));
		Permissions permissions = PermissionsUtils.getPermissionsForNetworkEntity(currentUser);
		setPermissions(permissions);

		buildLayout();
	}

	private void buildLayout() {
		queryDefinition = new NetworkQueryDefinition(Network.class, DEFAULT_LIMIT, getPermissions());
		queryDefinition.setOwner(currentUser.getTenant());
		queryDefinition.setPage(0, DEFAULT_LIMIT);
		queryDefinition.setQueryFilter(currentFilter);

		dataProvider = new LazyQueryDataProvider<>(queryDefinition, new NetworkQueryFactory(networkRepository));
		dataProvider.setCacheQueries(false);
		dataProvider.setFilter(currentFilter);
		setBackendDataProvider(dataProvider);

		grid = createGrid();
		Button filterButton = new Button(getI18nLabel("filter"), VaadinIcon.FILTER.create());
		filterButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		filterButton.addThemeName("subtle");
		buildFilterPopover(filterButton);

		HorizontalLayout toolbar = buildSearchToolbar(filterButton, createAddButton());
		VerticalLayout contentLayout = createListingLayout(toolbar, grid);
		setSelectable(grid);

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
		grid.addComponentColumn(item -> {
			MenuBar menuBar = new MenuBar();
			menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
			MenuItem menuItem = menuBar.addItem("•••");
			menuItem.getElement().setAttribute("aria-label", "More options");
			SubMenu subMenu = menuItem.getSubMenu();
			if (getPermissions().isViewMode()) {
				subMenu.addItem(getI18nLabel("mapview_action"), event -> openNetworkMap(item));
			}
			if (getPermissions().isModifyMode()) {
				subMenu.addItem(getI18nLabel("map_action"), event -> openEditableNetworkMap(item));
			}
			if (getPermissions().isViewMode()) {
				subMenu.addItem(getI18nLabel("relations_action"), event -> openNetworkConfigurations(item));
			}
			if (currentUser.hasPermission(EntityPermission.DEVICE.MIGRATE)) {
				subMenu.addItem(getI18nLabel("migration_button"), event -> openMigration(item));
			}
			if (getPermissions().isRemoveMode()) {
				subMenu.addItem(getI18nLabel("remove_action"), event -> openRemove(item));
			}
			if (getPermissions().isModifyMode()) {
				subMenu.addItem(getI18nLabel("modify_action"),
						event -> openEditor(item, getI18nLabel("modify_dialog")));
			}
			return menuBar;
		}).setWidth("70px").setFlexGrow(0).setKey("actions");
		return grid;
	}

	private String formatNetworkType(NetworkType type) {
		return type == null ? "" : getTranslation(type.getI18nKey());
	}

	private String formatPublic(boolean anonymous) {
		return anonymous ? getTranslation("basic.editor.yes") : getTranslation("basic.editor.no");
	}

	private String formatAnonymousOption(Boolean anonymous) {
		return Boolean.TRUE.equals(anonymous) ? getTranslation("basic.editor.yes")
				: getTranslation("basic.editor.no");
	}

	private void buildFilterPopover(Button filterButton) {
		Popover popover = new Popover();
		popover.setTarget(filterButton);
		popover.setPosition(PopoverPosition.BOTTOM_START);

		ComboBox<NetworkType> networkTypeBox = new ComboBox<>(getI18nLabel("networkType"));
		networkTypeBox.setItems(NetworkType.values());
		networkTypeBox.setItemLabelGenerator(this::formatNetworkType);
		networkTypeBox.setClearButtonVisible(true);
		networkTypeBox.setPlaceholder(getI18nLabel("any"));
		networkTypeBox.setWidthFull();
		networkTypeBox.setValue(currentFilter.getNetworkType());

		ComboBox<String> ownerBox = createOwnerComboBox(userService, getPermissions().isViewAllMode(),
				currentFilter.getOwner());

		ComboBox<Boolean> anonymousBox = new ComboBox<>(getI18nLabel("anonymous"));
		anonymousBox.setItems(Boolean.TRUE, Boolean.FALSE);
		anonymousBox.setItemLabelGenerator(this::formatAnonymousOption);
		anonymousBox.setClearButtonVisible(true);
		anonymousBox.setPlaceholder(getI18nLabel("any"));
		anonymousBox.setWidthFull();
		anonymousBox.setValue(currentFilter.getAnonymous());

		Button resetBtn = new Button(getTranslation("basic.editor.reset"), e -> {
			networkTypeBox.clear();
			ownerBox.clear();
			anonymousBox.clear();
		});
		resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

		Button cancelBtn = new Button(getTranslation("basic.editor.cancel"), e -> popover.close());
		cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

		Button updateBtn = new Button(getTranslation("basic.editor.filter"), e -> {
			currentFilter.setNetworkType(networkTypeBox.getValue());
			currentFilter.setOwner(ownerBox.getValue());
			currentFilter.setAnonymous(anonymousBox.getValue());
			queryDefinition.setQueryFilter(currentFilter);
			setFilter(currentFilter);
			refreshCurrentPage();
			popover.close();
			filterButton.setClassName(currentFilter.hasActiveFilter() ? "filter-active" : "");
		});
		updateBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		HorizontalLayout buttons = new HorizontalLayout(resetBtn, cancelBtn, updateBtn);
		buttons.setJustifyContentMode(JustifyContentMode.END);
		buttons.setWidthFull();

		VerticalLayout content = new VerticalLayout(networkTypeBox, ownerBox, anonymousBox, buttons);
		content.setSpacing(true);
		content.setPadding(true);
		content.setWidth("300px");
		popover.add(content);
	}

	private VerticalLayout createListingLayout(HorizontalLayout toolbar, Grid<Network> grid) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.add(toolbar, grid);
		layout.setFlexGrow(1f, grid);
		layout.setMargin(false);
		layout.setPadding(false);
		return layout;
	}

	private void refreshCurrentPage() {
		dataProvider.refreshAll();
		updateTotalCount();
		grid.scrollToStart();
		grid.deselectAll();
	}

	private long getTotalCount() {
		return new NetworkQuery(networkRepository, queryDefinition).countTotal();
	}

	private void updateTotalCount() {
		long total = getTotalCount();
		setTotalSize(total);
	}

	private Button createAddButton() {
		Button button = new Button(getI18nLabel("add"), VaadinIcon.PLUS.create());
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		button.setId("add" + getId());
		button.addClickListener(event -> openEditor(new Network(), getI18nLabel("add_dialog")));
		button.setVisible(getPermissions().isCreateMode());
		return button;
	}

	@Override
	protected void onSearch(String searchText) {
		currentFilter.setSearchText(searchText);
		queryDefinition.setQueryFilter(currentFilter);
		setFilter(currentFilter);
		refreshCurrentPage();
	}

	@Override
	protected void onRefresh() {
		refreshCurrentPage();
	}

	private void openNetworkMap(Network item) {
		if (item == null) {
			return;
		}
		Component content;
		Network network = backendServices.getNetworkService().findOne(item.getId());
		switch (item.getNetworkType()) {
			case GEOGRAPHIC:
				content = new DevicesGoogleMap(network, false, true, backendServices.getDeviceService(), backendServices.getNetworkService(), googleMapApiKey);
				break;
			case CUSTOM:
				content = new GroupWidgetsCustomMap(item.getId(), false, currentUser, backendServices,uiEventBus, devicesImageOverlayMapProvider);
				break;
			default:
				content = new GroupWidgetsDevicesListing(network, currentUser, backendServices, uiEventBus,
						groupWidgetsListingBoxProvider);
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

		Component content;
		boolean editable = getPermissions().isModifyMode();
		switch (item.getNetworkType()) {
			case GEOGRAPHIC:
				Network network = backendServices.getNetworkService().findOne(item.getId());
				content = new DevicesGoogleMap(network, editable, true,
						backendServices.getDeviceService(), backendServices.getNetworkService(), googleMapApiKey);
				break;
			case CUSTOM:
				content = new GroupWidgetsCustomMap(item.getId(), editable,currentUser,
						backendServices, uiEventBus, devicesImageOverlayMapProvider);
				break;
			default:
				PopupNotification.show(getI18nLabel("map_not_editable"), PopupNotification.Type.WARNING);
				return;
		}

		Tab tab = tabsheet.addTab("", content);
		tabsheet.setSelectedTab(tab);
	}

	private void openNetworkConfigurations(Network item) {
		if (item == null) {
			return;
		}
		NetworkRelations relations = networkRelationsProvider.getObject();
		relations.init(item.getId());
		Tab tab = tabsheet.addTab("", relations);
		tabsheet.setSelectedTab(tab);
	}

	private void openMigration(Network item) {
		if (item == null) {
			return;
		}
		NetworkDevices content = new NetworkDevices(item, backendServices.getDeviceService());
		SideDrawer dialog = new SideDrawer(getI18nLabel("device_migration"));
		dialog.setDrawerContent(content);
		dialog.open();
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
		String header = String.format("%s: %s", getI18nLabel("remove_action"), item.getName());
		ConfirmationDialogs.openDanger(this, header, getI18nLabel("remove_dialog"), () -> {
			try {
				backendServices.getNetworkService().disconnect(item);
				refreshCurrentPage();
			} catch (Exception e) {
				PopupNotification.show(e.getMessage(), PopupNotification.Type.ERROR);
			}
		});
	}

	private static final class NetworkFilter {
		private String searchText;
		private NetworkType networkType;
		private String owner;
		private Boolean anonymous;

		public String getSearchText() {
			return searchText;
		}

		public void setSearchText(String searchText) {
			this.searchText = normalize(searchText);
		}

		public boolean hasSearchText() {
			return searchText != null && !searchText.trim().isEmpty();
		}

		public NetworkType getNetworkType() {
			return networkType;
		}

		public void setNetworkType(NetworkType networkType) {
			this.networkType = networkType;
		}

		public boolean hasNetworkType() {
			return networkType != null;
		}

		public String getOwner() {
			return owner;
		}

		public void setOwner(String owner) {
			this.owner = normalize(owner);
		}

		public boolean hasOwner() {
			return owner != null && !owner.trim().isEmpty();
		}

		public Boolean getAnonymous() {
			return anonymous;
		}

		public void setAnonymous(Boolean anonymous) {
			this.anonymous = anonymous;
		}

		public boolean hasAnonymous() {
			return anonymous != null;
		}

		public boolean hasActiveFilter() {
			return hasNetworkType() || hasOwner() || hasAnonymous();
		}

		private String normalize(String value) {
			return value != null && value.trim().isEmpty() ? null : value;
		}

		@Override
		public String toString() {
			return "NetworkFilter{searchText=" + searchText + ", networkType=" + networkType + ", owner=" + owner
					+ ", anonymous=" + anonymous + "}";
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
			String search = filter != null && filter.hasSearchText() ? filter.getSearchText().trim() : null;
			String ownerFilter = filter != null && filter.hasOwner() ? filter.getOwner().trim() : null;
			NetworkType networkType = filter != null && filter.hasNetworkType() ? filter.getNetworkType() : null;
			Boolean anonymous = filter != null && filter.hasAnonymous() ? filter.getAnonymous() : null;

			if (queryDefinition.getPermissions().isViewAllMode()) {
				return networkRepository.findAllByFilters(search, ownerFilter, networkType, anonymous, pageable);
			}

			return networkRepository.findByOwnerAndFilters(queryDefinition.getOwner(), search, networkType, anonymous,
					pageable);
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
