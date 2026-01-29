package it.thisone.iotter.ui.users;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;

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
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.GroupWidgetService;
import it.thisone.iotter.persistence.service.NetworkGroupService;
import it.thisone.iotter.persistence.service.NetworkService;
import it.thisone.iotter.persistence.service.RoleService;
import it.thisone.iotter.persistence.service.UserService;

import it.thisone.iotter.security.Permissions;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.common.AbstractBaseEntityDetails;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.AbstractBaseEntityListing;
import it.thisone.iotter.ui.common.EntityRemovedEvent;
import it.thisone.iotter.ui.common.EntityRemovedListener;
import it.thisone.iotter.ui.common.EntitySelectedEvent;
import it.thisone.iotter.ui.common.EntitySelectedListener;
import it.thisone.iotter.ui.common.PermissionsUtils;
import it.thisone.iotter.ui.common.SideDrawer;
import it.thisone.iotter.ui.common.AuthenticatedUser;
import it.thisone.iotter.util.PopupNotification;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UsersListing extends AbstractBaseEntityListing<User> {

	private static final long serialVersionUID = 1L;
	private static final String ACCOUNT_STATUS = "accountStatus";
	private static final String USERNAME = "username";
	private static final String EMAIL = "email";
	private static final String OWNER = "owner";
	private static final String USERS_VIEW = "users.view";
	
	private Network network;
	private final Permissions permissions;

	@Autowired
	private AuthenticatedUser authenticatedUser;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private DeviceService deviceService;
	@Autowired
	private RoleService roleService;
	@Autowired
	private NetworkService networkService;
	@Autowired
	private NetworkGroupService networkGroupService;
	@Autowired
	private GroupWidgetService groupWidgetService;
	@Autowired
	private ObjectProvider<UserForm> userFormProvider;

	private Grid<User> grid;
	private LazyQueryDataProvider<User, UsersFilter> dataProvider;
	private UsersQueryDefinition queryDefinition;
	private UsersFilter currentFilter = new UsersFilter();
	private int currentLimit = DEFAULT_LIMIT;

	public UsersListing() {
		this(PermissionsUtils.getPermissionsForUserEntity());
	}

	private UsersListing(Permissions permissions) {
		super(User.class, USERS_VIEW, USERS_VIEW, false, permissions);
		this.permissions = permissions;
	}

	public void init(Network network) {
		if (grid != null) {
			return;
		}
		this.network = network;
		buildLayout();
	}

	private void buildLayout() {
		HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.setWidthFull();
		toolbar.setSpacing(true);
		toolbar.setPadding(true);
		toolbar.addClassName(TOOLBAR_STYLE);


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

		getMainLayout().add(toolbar, content);
		getMainLayout().setFlexGrow(1f, content);

		updateTotalCount();

		getButtonsLayout().add(createRemoveButton(), createModifyButton(), createViewButton(), createAddButton());
		toolbar.add(getButtonsLayout());
		toolbar.setAlignItems(Alignment.CENTER);
		enableButtons(null);

	}

	@Override
	public AbstractBaseEntityForm<User> getEditor(User item) {
       /*
	 Spring 4.3+ automatically autowires a single constructor without needing @Autowired. Vaadin/Spring's ObjectProvider uses this constructor injection implicitly without
  annotations. If multiple constructors exist, @Autowired is required to specify which one to use.

  
	   */

                 UserDetailsAdapter currentUser = authenticatedUser.get().orElseThrow(
                () -> new IllegalStateException("User must be authenticated to edit users"));


		return userFormProvider.getObject(item, network, currentUser, roleService, networkService,
				networkGroupService, groupWidgetService);
	}

	@Override
	public AbstractBaseEntityDetails<User> getDetails(User item, boolean remove) {
		return new UserDetails(item, remove, deviceService, userService);
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
		initFilters(grid);
		return grid;
	}

	private void initFilters(Grid<User> grid) {
		// Create header row for filters
		HeaderRow filterRow = grid.appendHeaderRow();

		// Create username filter TextField
		TextField username = new TextField();
		username.setPlaceholder("Filter...");
		username.setWidthFull();
		username.addThemeVariants(TextFieldVariant.LUMO_SMALL);
		username.setValueChangeMode(ValueChangeMode.LAZY);

		// Add TextField to the username column header cell
		filterRow.getCell(grid.getColumnByKey(USERNAME)).setComponent(username);

		// Add value change listener to trigger filtering
		username.addValueChangeListener(event -> {
			currentFilter.setUsername(event.getValue());
			queryDefinition.setQueryFilter(currentFilter);
			setFilter(currentFilter);
			refreshCurrentPage();
		});

		// Create email filter TextField
		TextField email = new TextField();
		email.setPlaceholder("Filter...");
		email.setWidthFull();
		email.addThemeVariants(TextFieldVariant.LUMO_SMALL);
		email.setValueChangeMode(ValueChangeMode.LAZY);

		// Add TextField to the email column header cell
		filterRow.getCell(grid.getColumnByKey(EMAIL)).setComponent(email);

		// Add value change listener to trigger filtering
		email.addValueChangeListener(event -> {
			currentFilter.setEmail(event.getValue());
			queryDefinition.setQueryFilter(currentFilter);
			setFilter(currentFilter);
			refreshCurrentPage();
		});

		// Create owner filter TextField (only if owner column is visible)
		if (permissions.isViewAllMode()) {
			TextField owner = new TextField();
			owner.setPlaceholder("Filter...");
			owner.setWidthFull();
			owner.addThemeVariants(TextFieldVariant.LUMO_SMALL);
			owner.setValueChangeMode(ValueChangeMode.LAZY);

			// Add TextField to the owner column header cell
			filterRow.getCell(grid.getColumnByKey(OWNER)).setComponent(owner);

			// Add value change listener to trigger filtering
			owner.addValueChangeListener(event -> {
				currentFilter.setOwner(event.getValue());
				queryDefinition.setQueryFilter(currentFilter);
				setFilter(currentFilter);
				refreshCurrentPage();
			});
		}

		// Create AccountStatus ComboBox filter
		ComboBox<AccountStatus> statusComboBox = new ComboBox<>();
		statusComboBox.setPlaceholder("Filter...");
		statusComboBox.setWidthFull();
		statusComboBox.addClassName("small");
		statusComboBox.setClearButtonVisible(true);
		statusComboBox.setAllowCustomValue(false);

		// Add all statuses except HIDDEN
		statusComboBox.setItems(
			AccountStatus.ACTIVE,
			AccountStatus.NEED_ACTIVATION,
			AccountStatus.EXPIRED,
			AccountStatus.SUSPENDED,
			AccountStatus.LOCKED
		);

		// Set i18n captions for enum values
		statusComboBox.setItemLabelGenerator(status ->
			getI18nLabel("enum.accountstatus." + status.name().toLowerCase()));

		// Add to header cell
		filterRow.getCell(grid.getColumnByKey(ACCOUNT_STATUS)).setComponent(statusComboBox);

		// Handle value changes
		statusComboBox.addValueChangeListener(event -> {
			currentFilter.setAccountStatus(event.getValue());
			queryDefinition.setQueryFilter(currentFilter);
			setFilter(currentFilter);
			refreshCurrentPage();
		});
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
		return roles.stream()
				.filter(Objects::nonNull)
				.sorted(Comparator.comparing(Role::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
				.map(Role::getName)
				.filter(Objects::nonNull)
				.collect(Collectors.joining(", "));
	}

	private String formatGroups(User user) {
		Set<NetworkGroup> groups = user.getGroups();
		if (groups == null || groups.isEmpty()) {
			return "";
		}
		return groups.stream()
				.filter(Objects::nonNull)
				.sorted(Comparator.comparing(NetworkGroup::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
				.map(NetworkGroup::getName)
				.filter(Objects::nonNull)
				.collect(Collectors.joining(", "));
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
		Button button = new Button();
		button.setIcon(VaadinIcon.PLUS.create());
		button.getElement().setAttribute("title", getI18nLabel("add"));
		button.setId("add" + getId() + ALWAYS_ENABLED_BUTTON);
		button.addClickListener(event -> openEditor(new User(), getI18nLabel("add_dialog")));
		button.setVisible(permissions.isCreateMode());
		return button;
	}

	private Button createViewButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.INFO_CIRCLE.create());
		button.getElement().setAttribute("title", getI18nLabel("view_action"));
		button.addClickListener(event -> openDetails(getCurrentValue(), getI18nLabel("view_dialog"), false));
		button.setVisible(permissions.isViewMode());
		return button;
	}

	private Button createModifyButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.EDIT.create());
		button.getElement().setAttribute("title", getI18nLabel("modify_action"));
		button.addClickListener(event -> openEditor(getCurrentValue(), getI18nLabel("modify_dialog")));
		button.setVisible(permissions.isModifyMode());
		return button;
	}

	private Button createRemoveButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.TRASH.create());
		button.getElement().setAttribute("title", getI18nLabel("remove_action"));
		button.addClickListener(event -> openDetails(getCurrentValue(), getI18nLabel("remove_dialog"), true));
		button.setVisible(permissions.isRemoveMode());
		return button;
	}

	private void openEditor(User item, String label) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityForm<User> editor = getEditor(item);
		SideDrawer dialog = (SideDrawer) createDialog(label, editor, editor.getWindowDimension(), editor.getWindowStyle());
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

	private void openDetails(User item, String label, boolean remove) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityDetails<User> details = getDetails(item, remove);
		SideDrawer dialog = (SideDrawer) createDialog(label, details, S_DIMENSION, S_WINDOW_STYLE);
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
		dialog.open();
	}

	private static final class UsersFilter {
		private String username;
		private String email;
		private String owner;
		private AccountStatus accountStatus;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public boolean hasUsername() {
			return username != null && !username.trim().isEmpty();
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public boolean hasEmail() {
			return email != null && !email.trim().isEmpty();
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

		@Override
		public String toString() {
			return "UsersFilter{username=" + username + ", email=" + email + ", owner=" + owner +
				   ", accountStatus=" + accountStatus + "}";
		}
	}

	private static final class UsersQueryDefinition extends LazyQueryDefinition<User, UsersFilter>
			implements FilterableQueryDefinition<UsersFilter> {

		private static final long serialVersionUID = 1L;

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
			System.out.println("UsersQuery.loadItems() - offset: " + offset + ", limit: " + limit +
					", calculated page: " + page + ", using pageSize: " + size);
			Page<User> users = findPage(page, size);
			System.out.println("UsersQuery.loadItems() - loaded " + users.getContent().size() + " users from page " + page);
			return users.getContent().stream();
		}

		private Page<User> findPage(int page, int size) {
			Sort sort = buildSort();
			Pageable pageable = PageRequest.of(page, size, sort);
			AccountStatus hidden = AccountStatus.HIDDEN;
			UsersFilter filter = queryDefinition.getQueryFilter();
			String username = filter != null && filter.hasUsername() ? filter.getUsername().trim() : null;
			String email = filter != null && filter.hasEmail() ? filter.getEmail().trim() : null;
			String ownerFilter = filter != null && filter.hasOwner() ? filter.getOwner().trim() : null;
			AccountStatus statusFilter = filter != null && filter.hasAccountStatus() ? filter.getAccountStatus() : null;

			if (queryDefinition.getNetwork() != null) {
				String owner = queryDefinition.getNetwork().getOwner();
				String networkId = queryDefinition.getNetwork().getId();

				if (statusFilter != null) {
					// Filter by specific AccountStatus
					if (username != null && email != null) {
						return userRepository.findByOwnerAndNetworkIdAndUsernameStartingWithIgnoreCaseAndEmailStartingWithIgnoreCaseAndAccountStatus(
								owner, networkId, username, email, statusFilter, pageable);
					} else if (username != null) {
						return userRepository.findByOwnerAndNetworkIdAndUsernameStartingWithIgnoreCaseAndAccountStatus(
								owner, networkId, username, statusFilter, pageable);
					} else if (email != null) {
						return userRepository.findByOwnerAndNetworkIdAndEmailStartingWithIgnoreCaseAndAccountStatus(
								owner, networkId, email, statusFilter, pageable);
					}
					return userRepository.findByOwnerAndNetworkIdAndAccountStatus(owner, networkId, statusFilter, pageable);
				} else {
					// Exclude HIDDEN status
					if (username != null && email != null) {
						return userRepository.findByOwnerAndNetworkIdAndUsernameStartingWithIgnoreCaseAndEmailStartingWithIgnoreCaseAndAccountStatusNot(
								owner, networkId, username, email, hidden, pageable);
					} else if (username != null) {
						return userRepository.findByOwnerAndNetworkIdAndUsernameStartingWithIgnoreCaseAndAccountStatusNot(
								owner, networkId, username, hidden, pageable);
					} else if (email != null) {
						return userRepository.findByOwnerAndNetworkIdAndEmailStartingWithIgnoreCaseAndAccountStatusNot(
								owner, networkId, email, hidden, pageable);
					}
					return userRepository.findByOwnerAndNetworkIdAndAccountStatusNot(owner, networkId, hidden, pageable);
				}
			}

			if (queryDefinition.getPermissions().isViewAllMode()) {
				if (statusFilter != null) {
					// Filter by specific AccountStatus
					if (username != null && email != null && ownerFilter != null) {
						return userRepository.findByOwnerStartingWithIgnoreCaseAndUsernameStartingWithIgnoreCaseAndEmailStartingWithIgnoreCaseAndAccountStatus(
								ownerFilter, username, email, statusFilter, pageable);
					} else if (username != null && email != null) {
						return userRepository.findByUsernameStartingWithIgnoreCaseAndEmailStartingWithIgnoreCaseAndAccountStatus(
								username, email, statusFilter, pageable);
					} else if (username != null && ownerFilter != null) {
						return userRepository.findByOwnerStartingWithIgnoreCaseAndUsernameStartingWithIgnoreCaseAndAccountStatus(
								ownerFilter, username, statusFilter, pageable);
					} else if (email != null && ownerFilter != null) {
						return userRepository.findByOwnerStartingWithIgnoreCaseAndEmailStartingWithIgnoreCaseAndAccountStatus(
								ownerFilter, email, statusFilter, pageable);
					} else if (username != null) {
						return userRepository.findByUsernameStartingWithIgnoreCaseAndAccountStatus(username, statusFilter, pageable);
					} else if (email != null) {
						return userRepository.findByEmailStartingWithIgnoreCaseAndAccountStatus(email, statusFilter, pageable);
					} else if (ownerFilter != null) {
						return userRepository.findByOwnerStartingWithIgnoreCaseAndAccountStatus(ownerFilter, statusFilter, pageable);
					}
					return userRepository.findByAccountStatus(statusFilter, pageable);
				} else {
					// Exclude HIDDEN status
					if (username != null && email != null && ownerFilter != null) {
						return userRepository.findByOwnerStartingWithIgnoreCaseAndUsernameStartingWithIgnoreCaseAndEmailStartingWithIgnoreCaseAndAccountStatusNot(
								ownerFilter, username, email, hidden, pageable);
					} else if (username != null && email != null) {
						return userRepository.findByUsernameStartingWithIgnoreCaseAndEmailStartingWithIgnoreCaseAndAccountStatusNot(
								username, email, hidden, pageable);
					} else if (username != null && ownerFilter != null) {
						return userRepository.findByOwnerStartingWithIgnoreCaseAndUsernameStartingWithIgnoreCaseAndAccountStatusNot(
								ownerFilter, username, hidden, pageable);
					} else if (email != null && ownerFilter != null) {
						return userRepository.findByOwnerStartingWithIgnoreCaseAndEmailStartingWithIgnoreCaseAndAccountStatusNot(
								ownerFilter, email, hidden, pageable);
					} else if (username != null) {
						return userRepository.findByUsernameStartingWithIgnoreCaseAndAccountStatusNot(username, hidden, pageable);
					} else if (email != null) {
						return userRepository.findByEmailStartingWithIgnoreCaseAndAccountStatusNot(email, hidden, pageable);
					} else if (ownerFilter != null) {
						return userRepository.findByOwnerStartingWithIgnoreCaseAndAccountStatusNot(ownerFilter, hidden, pageable);
					}
					return userRepository.findByAccountStatusNot(hidden, pageable);
				}
			}

			String owner = queryDefinition.getOwner();
			if (statusFilter != null) {
				// Filter by specific AccountStatus
				if (username != null && email != null) {
					return userRepository.findByOwnerAndUsernameStartingWithIgnoreCaseAndEmailStartingWithIgnoreCaseAndAccountStatus(
							owner, username, email, statusFilter, pageable);
				} else if (username != null) {
					return userRepository.findByOwnerAndUsernameStartingWithIgnoreCaseAndAccountStatus(owner, username, statusFilter,
							pageable);
				} else if (email != null) {
					return userRepository.findByOwnerAndEmailStartingWithIgnoreCaseAndAccountStatus(owner, email, statusFilter,
							pageable);
				}
				return userRepository.findByOwnerAndAccountStatus(owner, statusFilter, pageable);
			} else {
				// Exclude HIDDEN status
				if (username != null && email != null) {
					return userRepository.findByOwnerAndUsernameStartingWithIgnoreCaseAndEmailStartingWithIgnoreCaseAndAccountStatusNot(
							owner, username, email, hidden, pageable);
				} else if (username != null) {
					return userRepository.findByOwnerAndUsernameStartingWithIgnoreCaseAndAccountStatusNot(owner, username, hidden,
							pageable);
				} else if (email != null) {
					return userRepository.findByOwnerAndEmailStartingWithIgnoreCaseAndAccountStatusNot(owner, email, hidden,
							pageable);
				}
				return userRepository.findByOwnerAndAccountStatusNot(owner, hidden, pageable);
			}
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
				Sort.Direction direction = sortOrder.getDirection() == SortDirection.ASCENDING
					? Sort.Direction.ASC
					: Sort.Direction.DESC;
				orders.add(new Sort.Order(direction, property));
			}

			// Add secondary sort by username if not already included
			boolean hasUsernameSorting = sortOrders.stream()
				.anyMatch(so -> USERNAME.equals(so.getSorted()));
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
