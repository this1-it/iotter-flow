package it.thisone.iotter.ui.users;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

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
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.AccountStatus;
import it.thisone.iotter.lazyquerydataprovider.FilterableQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDataProvider;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryFactory;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.model.Role;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.repository.UserRepository;

import it.thisone.iotter.persistence.service.UserService;
import it.thisone.iotter.security.Permissions;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.AbstractBaseEntityListing;
import it.thisone.iotter.ui.common.AuthenticatedUser;
import it.thisone.iotter.ui.common.ConfirmationDialogs;
import it.thisone.iotter.ui.common.PermissionsUtils;
import it.thisone.iotter.ui.common.SideDrawer;
import it.thisone.iotter.ui.eventbus.UIEventBus;
import it.thisone.iotter.ui.providers.BackendServices;
import it.thisone.iotter.util.PopupNotification;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UsersListing extends AbstractBaseEntityListing<User> {

	public static Logger logger = LoggerFactory.getLogger(UsersListing.class);
	private static final long serialVersionUID = 1L;
	private static final String ACCOUNT_STATUS = "accountStatus";
	private static final String USERNAME = "username";
	private static final String EMAIL = "email";
	private static final String OWNER = "owner";
	private static final String USERS_VIEW = "users.view";

	private Network network;
	private Permissions permissions;

	@Autowired
	private AuthenticatedUser authenticatedUser;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserService userService;

	@Autowired
	private BackendServices backendServices;

	@Autowired
	private UIEventBus eventBus;


	private Grid<User> grid;
	private LazyQueryDataProvider<User, UsersFilter> dataProvider;
	private UsersQueryDefinition queryDefinition;
	private UsersFilter currentFilter = new UsersFilter();
	private int currentLimit = DEFAULT_LIMIT;

	private UsersListing() {
		super(User.class, USERS_VIEW, USERS_VIEW, false);
	}

	public void init(Network network) {
		if (grid != null) {
			return;
		}
		this.network = network;
		UserDetailsAdapter currentUser = authenticatedUser.get()
				.orElseThrow(() -> new IllegalStateException("User must be authenticated to edit users"));
		this.permissions = PermissionsUtils.getPermissionsForUserEntity(currentUser);
		buildLayout();
	}

	private void buildLayout() {
		queryDefinition = new UsersQueryDefinition(User.class, currentLimit, permissions);
		queryDefinition.setNetwork(network);
		queryDefinition.setOwner(authenticatedUser.getTenant().orElse(null));
		queryDefinition.setPage(0, currentLimit);
		queryDefinition.setQueryFilter(currentFilter);
		dataProvider = new LazyQueryDataProvider<>(queryDefinition, new UsersQueryFactory(userRepository));
		dataProvider.setCacheQueries(false);
		dataProvider.setFilter(currentFilter);
		setBackendDataProvider(dataProvider);

		grid = createGrid();
		VerticalLayout content = createContent(grid);
		setSelectable(grid);

		Button filterButton = new Button(getI18nLabel("filter"), VaadinIcon.FILTER.create());
		filterButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        filterButton.addThemeName("subtle");
		buildFilterPopover(filterButton);

		HorizontalLayout toolbar = buildSearchToolbar(filterButton, createAddButton());

		getMainLayout().add(toolbar, content);
		getMainLayout().setFlexGrow(1f, content);
		updateTotalCount();
		enableButtons(null);
	}

	private void buildFilterPopover(Button filterButton) {
		Popover popover = new Popover();
		popover.setTarget(filterButton);
		popover.setPosition(PopoverPosition.BOTTOM_START);

		ComboBox<AccountStatus> statusBox = new ComboBox<>(getI18nLabel(ACCOUNT_STATUS));
		statusBox.setItems(AccountStatus.ACTIVE, AccountStatus.NEED_ACTIVATION, AccountStatus.EXPIRED,
				AccountStatus.SUSPENDED, AccountStatus.LOCKED);
		statusBox.setItemLabelGenerator(s -> getI18nLabel("enum.accountstatus." + s.name().toLowerCase()));
		statusBox.setClearButtonVisible(true);
		statusBox.setPlaceholder(getI18nLabel("any"));
		statusBox.setWidthFull();

		ComboBox<String> ownerBox = createOwnerComboBox(userService, permissions.isViewAllMode(),
				currentFilter.getOwner());

		Button resetBtn = new Button(getTranslation("basic.editor.reset"), e -> {
			statusBox.clear();
			ownerBox.clear();
		});
		resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

		Button cancelBtn = new Button(getTranslation("basic.editor.cancel"), e -> popover.close());
		cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

		Button updateBtn = new Button(getTranslation("basic.editor.filter"), e -> {
			currentFilter.setAccountStatus(statusBox.getValue());
			currentFilter.setOwner(ownerBox.getValue());
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

		VerticalLayout content = new VerticalLayout(statusBox, ownerBox, buttons);
		content.setSpacing(true);
		content.setPadding(true);
		content.setWidth("300px");
		popover.add(content);
	}

	@Override
	public AbstractBaseEntityForm<User> getEditor(User item, boolean readOnly) {
		UserDetailsAdapter currentUser = authenticatedUser.get()
				.orElseThrow(() -> new IllegalStateException("User must be authenticated to edit users"));

		return new UserForm(item, network, currentUser, backendServices, eventBus, readOnly);
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

	@Override
	protected void onSearch(String searchText) {
		currentFilter.setSearchText(searchText);
		queryDefinition.setQueryFilter(currentFilter);
		setFilter(currentFilter);
		refreshCurrentPage();
	}

	@Override
	protected void onRefresh() {
		refreshData();
	}

	private long getTotalCount() {
		return new UsersQuery(userRepository, queryDefinition).countTotal();
	}

	private void updateTotalCount() {
		long total = getTotalCount();
		setTotalSize(total);
	}

	private Grid<User> createGrid() {
		Grid<User> grid = new Grid<>();
		grid.setDataProvider(dataProvider);
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.setSizeFull();
		grid.addClassName("smallgrid");

		List<Grid.Column<User>> columns = new ArrayList<>();
		Grid.Column<User> usernameColumn = grid.addColumn(User::getUsername).setKey(USERNAME);
		columns.add(usernameColumn);
		Grid.Column<User> firstNameColumn = grid.addColumn(User::getFirstName).setKey("firstName");
		columns.add(firstNameColumn);
		Grid.Column<User> lastNameColumn = grid.addColumn(User::getLastName).setKey("lastName");
		columns.add(lastNameColumn);
		Grid.Column<User> emailColumn = grid.addColumn(User::getEmail).setKey("email");
		columns.add(emailColumn);
		Grid.Column<User> rolesColumn = grid.addColumn(this::formatRoles).setKey("roles");
		columns.add(rolesColumn);

		if (Constants.USE_GROUPS) {
			Grid.Column<User> groupsColumn = grid.addColumn(this::formatGroups).setKey("groups");
			columns.add(groupsColumn);
		}

		if (network == null) {
			Grid.Column<User> networkColumn = grid.addColumn(this::formatNetwork).setKey("network");
			columns.add(networkColumn);
		}

		Grid.Column<User> statusColumn = grid.addColumn(this::formatAccountStatus).setKey(ACCOUNT_STATUS);
		columns.add(statusColumn);

		if (permissions.isViewAllMode()) {
			Grid.Column<User> ownerColumn = grid.addColumn(User::getOwner).setKey("owner");
			columns.add(ownerColumn);
		}

		// Disable sorting on all columns except username, email, and owner
		for (Grid.Column<User> column : columns) {
			String columnId = column.getKey();
			if (!USERNAME.equals(columnId) && !EMAIL.equals(columnId) && !"owner".equals(columnId)) {
				column.setSortable(false);
			}
		}

		for (Grid.Column<User> column : columns) {
			column.setHeader(getI18nLabel(column.getKey()));
		}

		grid.setColumnOrder(columns.toArray(new Grid.Column[0]));

		grid.addComponentColumn(user -> {
			MenuBar menuBar = new MenuBar();
			menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
			MenuItem menuItem = menuBar.addItem("•••");
			menuItem.getElement().setAttribute("aria-label", "More options");
			SubMenu subMenu = menuItem.getSubMenu();
			if (permissions.isViewMode()) {
				subMenu.addItem(getI18nLabel("view_action"), event -> openDetails(user));
			}
			if (permissions.isModifyMode()) {
				subMenu.addItem(getI18nLabel("modify_action"), event -> openEditor(user, getI18nLabel("modify_dialog")));
			}
			if (permissions.isRemoveMode()) {
				subMenu.addItem(getI18nLabel("remove_action"), event -> openRemove(user));
			}
			return menuBar;
		}).setWidth("70px").setFlexGrow(0).setKey("actions");

		return grid;
	}

	private VerticalLayout createContent(Grid<User> grid) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.add(grid);
		layout.setFlexGrow(1f, grid);
		return layout;
	}

	private String formatRoles(User user) {
		Set<Role> roles = user.getRoles();
		if (roles == null || roles.isEmpty()) {
			return "";
		}
		return roles.stream().filter(Objects::nonNull)
				.sorted(Comparator.comparing(Role::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
				.map(Role::getName).filter(Objects::nonNull).collect(Collectors.joining(", "));
	}

	private String formatGroups(User user) {
		Set<NetworkGroup> groups = user.getGroups();
		if (groups == null || groups.isEmpty()) {
			return "";
		}
		return groups.stream().filter(Objects::nonNull)
				.sorted(Comparator.comparing(NetworkGroup::getName,
						Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
				.map(NetworkGroup::getName).filter(Objects::nonNull).collect(Collectors.joining(", "));
	}

	private String formatNetwork(User user) {
		if (user.getNetwork() == null) {
			return "";
		}
		return user.getNetwork().getName();
	}

	private String formatAccountStatus(User user) {
		if (user.getAccountStatus() == null) {
			return "";
		}
		return user.getAccountStatus().name();
	}

	private Button createAddButton() {
		Button button = new Button(getI18nLabel("add"), VaadinIcon.PLUS.create());
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		button.setId("add" + getId() + ALWAYS_ENABLED_BUTTON);
		button.addClickListener(event -> openEditor(new User(), getI18nLabel("add_dialog")));
		button.setVisible(permissions.isCreateMode());
		return button;
	}

	private void openEditor(User item, String label) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityForm<User> editor = getEditor(item, false);
		SideDrawer dialog = (SideDrawer) createDialog(label, editor);
		dialog.addThemeName("side-drawer-middlescreen");
		editor.setSavedHandler(entity -> {
			try {
				if (entity.isNew()) {
					userService.create(entity);
				} else {
					userService.update(entity);
				}
				dialog.close();
				refreshCurrentPage();
			} catch (Exception e) {
				PopupNotification.show(e.getMessage(), PopupNotification.Type.ERROR);
			}
		});
		dialog.open();
	}

	protected void openDetails(User item) {
		if (item == null) {
			return;
		}

		AbstractBaseEntityForm<User> details = getEditor(item, true);
		SideDrawer dialog = (SideDrawer) createDialog(getI18nLabel("view_dialog"), details);
		dialog.addThemeName("side-drawer-middlescreen");

		dialog.open();
	}

	protected void openRemove(User item) {
		if (item == null) {
			return;
		}

		String name = item.getDisplayName();
		if (name == null || name.isBlank()) {
			name = item.getUsername();
		}
		String header = String.format("%s: %s", getI18nLabel("remove_action"), name);
		ConfirmationDialogs.openDanger(this, header, getI18nLabel("remove_dialog"), () -> {
			try {
				userService.deleteById(item.getId());
				refreshCurrentPage();
			} catch (Exception e) {
				PopupNotification.show(e.getMessage(), PopupNotification.Type.ERROR);
			}
		});
	}

	
	private static final class UsersFilter {
		private String searchText;
		private String owner;
		private AccountStatus accountStatus;

		public String getSearchText() {
			return searchText;
		}

		public void setSearchText(String searchText) {
			this.searchText = (searchText != null && searchText.trim().isEmpty()) ? null : searchText;
		}

		public boolean hasSearchText() {
			return searchText != null && !searchText.trim().isEmpty();
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

		public AccountStatus getAccountStatus() {
			return accountStatus;
		}

		public void setAccountStatus(AccountStatus accountStatus) {
			this.accountStatus = accountStatus;
		}

		public boolean hasAccountStatus() {
			return accountStatus != null;
		}

		public boolean hasActiveFilter() {
			return hasAccountStatus() || hasOwner();
		}

		@Override
		public String toString() {
			return "UsersFilter{searchText=" + searchText + ", owner=" + owner + ", accountStatus=" + accountStatus + "}";
		}
	}

	private static final class UsersQueryDefinition extends LazyQueryDefinition<User, UsersFilter>
			implements FilterableQueryDefinition<UsersFilter> {

		private UsersFilter queryFilter;
		private Network network;
		private final Permissions permissions;
		private String owner;
		private int pageIndex;
		private int pageSize;

		public UsersQueryDefinition(Class<User> beanClass, int batchSize, Permissions permissions) {
			super(beanClass, batchSize);
			this.permissions = permissions;
		}

		@Override
		public void setQueryFilter(UsersFilter filter) {
			this.queryFilter = filter;
		}

		@Override
		public UsersFilter getQueryFilter() {
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

	private static final class UsersQueryFactory implements QueryFactory<User, UsersFilter> {
		private final UserRepository userRepository;

		private UsersQueryFactory(UserRepository userRepository) {
			this.userRepository = userRepository;
		}

		@Override
		public it.thisone.iotter.lazyquerydataprovider.Query<User, UsersFilter> constructQuery(
				QueryDefinition<User, UsersFilter> queryDefinition) {
			return new UsersQuery(userRepository, (UsersQueryDefinition) queryDefinition);
		}
	}

	private static final class UsersQuery implements it.thisone.iotter.lazyquerydataprovider.Query<User, UsersFilter> {
		private final UserRepository userRepository;
		private final UsersQueryDefinition queryDefinition;

		private UsersQuery(UserRepository userRepository, UsersQueryDefinition queryDefinition) {
			this.userRepository = userRepository;
			this.queryDefinition = queryDefinition;
		}

		@Override
		public int size(QueryDefinition<User, UsersFilter> queryDefinition) {
			Page<User> page = findPage(0, 1);
			long total = page.getTotalElements();
			return (int) total;
		}

		@Override
		public java.util.stream.Stream<User> loadItems(QueryDefinition<User, UsersFilter> queryDefinition, int offset,
				int limit) {
			int size = limit > 0 ? limit : this.queryDefinition.getPageSize();
			int page = size > 0 ? offset / size : 0;
			System.out.println("UsersQuery.loadItems() - offset: " + offset + ", limit: " + limit
					+ ", calculated page: " + page + ", using pageSize: " + size);
			Page<User> users = findPage(page, size);
			System.out.println(
					"UsersQuery.loadItems() - loaded " + users.getContent().size() + " users from page " + page);
			return users.getContent().stream();
		}

		private Page<User> findPage(int page, int size) {
			Sort sort = buildSort();
			Pageable pageable = PageRequest.of(page, size, sort);
			AccountStatus hidden = AccountStatus.HIDDEN;
			UsersFilter filter = queryDefinition.getQueryFilter();
			String search = filter != null && filter.hasSearchText() ? filter.getSearchText().trim() : null;
			AccountStatus statusFilter = filter != null && filter.hasAccountStatus() ? filter.getAccountStatus() : null;
			String ownerFilter = filter != null && filter.hasOwner() ? filter.getOwner() : null;

			if (queryDefinition.getNetwork() != null) {
				String owner = queryDefinition.getNetwork().getOwner();
				String networkId = queryDefinition.getNetwork().getId();
				if (search != null && statusFilter != null) {
					return userRepository.searchByOwnerAndNetworkIdAndAccountStatus(owner, networkId, search, statusFilter, pageable);
				} else if (search != null) {
					return userRepository.searchByOwnerAndNetworkId(owner, networkId, search, hidden, pageable);
				} else if (statusFilter != null) {
					return userRepository.findByOwnerAndNetworkIdAndAccountStatus(owner, networkId, statusFilter, pageable);
				}
				return userRepository.findByOwnerAndNetworkIdAndAccountStatusNot(owner, networkId, hidden, pageable);
			}

			if (queryDefinition.getPermissions().isViewAllMode()) {
				// owner selected in popover — delegate to owner-scoped queries
				if (ownerFilter != null) {
					if (search != null && statusFilter != null) {
						return userRepository.searchByOwnerAndAccountStatus(ownerFilter, search, statusFilter, pageable);
					} else if (search != null) {
						return userRepository.searchByOwner(ownerFilter, search, hidden, pageable);
					} else if (statusFilter != null) {
						return userRepository.findByOwnerAndAccountStatus(ownerFilter, statusFilter, pageable);
					}
					return userRepository.findByOwnerAndAccountStatusNot(ownerFilter, hidden, pageable);
				}
				if (search != null && statusFilter != null) {
					return userRepository.searchAllAndAccountStatus(search, statusFilter, pageable);
				} else if (search != null) {
					return userRepository.searchAll(search, hidden, pageable);
				} else if (statusFilter != null) {
					return userRepository.findByAccountStatus(statusFilter, pageable);
				}
				return userRepository.findByAccountStatusNot(hidden, pageable);
			}

			String owner = queryDefinition.getOwner();
			if (search != null && statusFilter != null) {
				return userRepository.searchByOwnerAndAccountStatus(owner, search, statusFilter, pageable);
			} else if (search != null) {
				return userRepository.searchByOwner(owner, search, hidden, pageable);
			} else if (statusFilter != null) {
				return userRepository.findByOwnerAndAccountStatus(owner, statusFilter, pageable);
			}
			return userRepository.findByOwnerAndAccountStatusNot(owner, hidden, pageable);
		}

		private Sort buildSort() {
			List<QuerySortOrder> sortOrders = queryDefinition.getSortOrders();

			// If no sort orders from Grid, use default sorting
			if (sortOrders == null || sortOrders.isEmpty()) {
				return Sort.by("lastName").ascending().and(Sort.by("username").ascending());
			}

			// Convert Vaadin QuerySortOrder to Spring Data Sort
			List<Sort.Order> orders = new ArrayList<>();
			for (QuerySortOrder sortOrder : sortOrders) {
				String property = sortOrder.getSorted();
				Sort.Direction direction = sortOrder.getDirection() == SortDirection.ASCENDING ? Sort.Direction.ASC
						: Sort.Direction.DESC;
				orders.add(new Sort.Order(direction, property));
			}

			// Add secondary sort by username if not already included
			boolean hasUsernameSorting = sortOrders.stream().anyMatch(so -> USERNAME.equals(so.getSorted()));
			if (!hasUsernameSorting) {
				orders.add(new Sort.Order(Sort.Direction.ASC, USERNAME));
			}

			return Sort.by(orders);
		}

		private long countTotal() {
			return findPage(0, 1).getTotalElements();
		}
	}
}
