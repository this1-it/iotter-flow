package it.thisone.iotter.ui.networkgroups;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.button.Button;
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

import it.thisone.iotter.lazyquerydataprovider.FilterableQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDataProvider;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryFactory;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.repository.NetworkGroupRepository;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.NetworkGroupService;
import it.thisone.iotter.security.Permissions;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.AbstractBaseEntityListing;
import it.thisone.iotter.ui.common.AuthenticatedUser;
import it.thisone.iotter.ui.common.EditorSavedEvent;
import it.thisone.iotter.ui.common.EditorSavedListener;
import it.thisone.iotter.ui.common.PermissionsUtils;
import it.thisone.iotter.ui.common.SideDrawer;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.util.PopupNotification;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NetworkGroupsListing extends AbstractBaseEntityListing<NetworkGroup> {

	private static final long serialVersionUID = 1L;
	private static final String NETWORKGROUP_VIEW = "networkgroup.view";
	private static final String NAME = "name";
	private static final String DESCRIPTION = "description";
	private static final String OWNER = "owner";


	private Network network;

	@Autowired
	private DeviceService deviceService;
	@Autowired
	private NetworkGroupRepository networkGroupRepository;
	@Autowired
	private NetworkGroupService networkGroupService;
	@Autowired
	private AuthenticatedUser authenticatedUser;

	private Grid<NetworkGroup> grid;
	private LazyQueryDataProvider<NetworkGroup, NetworkGroupFilter> dataProvider;
	private NetworkGroupQueryDefinition queryDefinition;
	private NetworkGroupFilter currentFilter = new NetworkGroupFilter();



	public NetworkGroupsListing() {
		super(NetworkGroup.class, NETWORKGROUP_VIEW, NETWORKGROUP_VIEW, false);
	}

	public void init(Network network) {
		if (grid != null) {
			return;
		}
		this.network = network;

        		UserDetailsAdapter currentUser = authenticatedUser.get()
				.orElseThrow(() -> new IllegalStateException("User must be authenticated to edit users"));
		Permissions permissions = PermissionsUtils.getPermissionsForNetworkGroupEntity(currentUser);
		setPermissions(permissions);

		buildLayout();
	}

	private void buildLayout() {
		HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.setWidthFull();
		toolbar.setSpacing(true);
		toolbar.setPadding(true);
		toolbar.addClassName(UIUtils.TOOLBAR_STYLE);

		queryDefinition = new NetworkGroupQueryDefinition(NetworkGroup.class, DEFAULT_LIMIT, getPermissions());
		queryDefinition.setNetwork(network);
		queryDefinition.setOwner(authenticatedUser.getTenant().orElse(null));
		queryDefinition.setPage(0, DEFAULT_LIMIT);
		queryDefinition.setQueryFilter(currentFilter);

		dataProvider = new LazyQueryDataProvider<>(queryDefinition, new NetworkGroupQueryFactory(networkGroupRepository));
		dataProvider.setCacheQueries(false);
		dataProvider.setFilter(currentFilter);
		setBackendDataProvider(dataProvider);

		grid = createGrid();
		VerticalLayout contentLayout = createContentLayout(toolbar, grid);
		setSelectable(grid);

		getButtonsLayout().add(createRemoveButton(), createModifyButton(), createBindingsButton(), createAddButton());
		toolbar.add(getButtonsLayout());
		toolbar.setAlignItems(Alignment.CENTER);
		enableButtons(null);

		getMainLayout().add(contentLayout);
		getMainLayout().setFlexGrow(1f, contentLayout);

		updateTotalCount();
	}

	@Override
	protected AbstractBaseEntityForm<NetworkGroup> getEditor(NetworkGroup item, boolean readOnly) {
		if (network != null) {
			item.setNetwork(network);
		}
		return new NetworkGroupForm(item, network, readOnly);
	}

	private Grid<NetworkGroup> createGrid() {
		Grid<NetworkGroup> grid = new Grid<>();
		grid.setDataProvider(dataProvider);
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.setSizeFull();
		

		List<Grid.Column<NetworkGroup>> columns = new ArrayList<>();
		columns.add(grid.addColumn(NetworkGroup::getName).setKey(NAME));
		columns.add(grid.addColumn(NetworkGroup::getDescription).setKey(DESCRIPTION));
		if (network == null) {
			columns.add(grid.addColumn(this::formatNetwork).setKey("network"));
		}
		columns.add(grid.addColumn(group -> formatBoolean(group.isDefaultGroup())).setKey("defaultGroup"));
		columns.add(grid.addColumn(group -> formatBoolean(group.isExclusive())).setKey("exclusive"));

		if (getPermissions().isViewAllMode()) {
			columns.add(grid.addColumn(NetworkGroup::getOwner).setKey(OWNER));
		}

		for (Grid.Column<NetworkGroup> column : columns) {
			String columnId = column.getKey();
			column.setSortable(NAME.equals(columnId) || OWNER.equals(columnId));
			column.setHeader(getI18nLabel(columnId));
		}

		grid.setColumnOrder(columns.toArray(new Grid.Column[0]));
		initFilters(grid);
		return grid;
	}

	private String formatNetwork(NetworkGroup group) {
		return group.getNetwork() != null ? group.getNetwork().getName() : "";
	}

	private String formatBoolean(boolean value) {
		return value ? getTranslation("basic.editor.yes") : getTranslation("basic.editor.no");
	}

	private void initFilters(Grid<NetworkGroup> grid) {
		HeaderRow filterRow = grid.appendHeaderRow();

		TextField name = new TextField();
		name.setPlaceholder("Filter...");
		name.setWidthFull();
		name.addThemeVariants(TextFieldVariant.LUMO_SMALL);
		name.setValueChangeMode(ValueChangeMode.LAZY);
		filterRow.getCell(grid.getColumnByKey(NAME)).setComponent(name);
		name.addValueChangeListener(event -> {
			currentFilter.setName(event.getValue());
			queryDefinition.setQueryFilter(currentFilter);
			setFilter(currentFilter);
			refreshCurrentPage();
		});

		TextField description = new TextField();
		description.setPlaceholder("Filter...");
		description.setWidthFull();
		description.addThemeVariants(TextFieldVariant.LUMO_SMALL);
		description.setValueChangeMode(ValueChangeMode.LAZY);
		filterRow.getCell(grid.getColumnByKey(DESCRIPTION)).setComponent(description);
		description.addValueChangeListener(event -> {
			currentFilter.setDescription(event.getValue());
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
			filterRow.getCell(grid.getColumnByKey(OWNER)).setComponent(owner);
			owner.addValueChangeListener(event -> {
				currentFilter.setOwner(event.getValue());
				queryDefinition.setQueryFilter(currentFilter);
				setFilter(currentFilter);
				refreshCurrentPage();
			});
		}
	}

	private VerticalLayout createContentLayout(HorizontalLayout toolbar, Grid<NetworkGroup> grid) {
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

	private void updateTotalCount() {
		long total = new NetworkGroupQuery(networkGroupRepository, queryDefinition).countTotal();
		setTotalSize(total);
	}

	private Button createAddButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.PLUS.create());
		button.getElement().setAttribute("title", getI18nLabel("add"));
		button.setId("add" + getId() + ALWAYS_ENABLED_BUTTON);
		button.addClickListener(event -> openEditor(new NetworkGroup(), getI18nLabel("add_dialog")));
		button.setVisible(getPermissions().isCreateMode());
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

	private Button createBindingsButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.RANDOM.create());
		button.setId("bindings_button");
		button.getElement().setAttribute("title", getI18nLabel("bindings_button"));
		button.addClickListener(event -> openBindings(getCurrentValue()));
		return button;
	}

	private void openEditor(NetworkGroup item, String label) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityForm<NetworkGroup> editor = getEditor(item, false);
		SideDrawer dialog = (SideDrawer) createDialog(label, editor);
		editor.setSavedHandler(entity -> {
			try {
				if (entity.isNew()) {
					networkGroupService.create(entity);
				} else {
					networkGroupService.update(entity);
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
	protected void openDetails(NetworkGroup item) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityForm<NetworkGroup> details = getEditor(item, true);
		SideDrawer dialog = (SideDrawer) createDialog(getI18nLabel("view_dialog"), details);
		dialog.open();
	}

	@Override
	protected void openRemove(NetworkGroup item) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityForm<NetworkGroup> details = getEditor(item, true);
		SideDrawer dialog = (SideDrawer) createDialog(getI18nLabel("remove_dialog"), details);
		details.setDeleteHandler(entity -> {
			try {
				networkGroupService.remove(entity);
				dialog.close();
				refreshCurrentPage();
			} catch (Exception e) {
				PopupNotification.show(e.getMessage(), PopupNotification.Type.ERROR);
			}
		});
		dialog.open();
	}

	private void openBindings(NetworkGroup item) {
		if (item == null) {
			return;
		}
		String caption = String.format("%s: %s", getI18nLabel("bindings"), item.getName());
		// NetworkGroupBindings content = new NetworkGroupBindings(item, networkGroupService, deviceService);
		// SideDrawer dialog = (SideDrawer) createDialog(caption, content);
		// content.addListener(new EditorSavedListener() {
		// 	private static final long serialVersionUID = 1L;

		// 	@Override
		// 	public void editorSaved(EditorSavedEvent event) {
		// 		dialog.close();
		// 	}
		// });
		// dialog.open();
	}

	private static final class NetworkGroupFilter {
		private String name;
		private String description;
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

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public boolean hasDescription() {
			return description != null && !description.trim().isEmpty();
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
			return "NetworkGroupFilter{name=" + name + ", description=" + description + ", owner=" + owner + "}";
		}
	}

	private static final class NetworkGroupQueryDefinition extends LazyQueryDefinition<NetworkGroup, NetworkGroupFilter>
			implements FilterableQueryDefinition<NetworkGroupFilter> {

		private static final long serialVersionUID = 1L;

		private NetworkGroupFilter queryFilter;
		private Network network;
		private final Permissions permissions;
		private String owner;
		private int pageIndex;
		private int pageSize;

		private NetworkGroupQueryDefinition(Class<NetworkGroup> beanClass, int batchSize, Permissions permissions) {
			super(beanClass, batchSize);
			this.permissions = permissions;
		}

		@Override
		public void setQueryFilter(NetworkGroupFilter filter) {
			this.queryFilter = filter;
		}

		@Override
		public NetworkGroupFilter getQueryFilter() {
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

	private static final class NetworkGroupQueryFactory implements QueryFactory<NetworkGroup, NetworkGroupFilter> {
		private final NetworkGroupRepository networkGroupRepository;

		private NetworkGroupQueryFactory(NetworkGroupRepository networkGroupRepository) {
			this.networkGroupRepository = networkGroupRepository;
		}

		@Override
		public it.thisone.iotter.lazyquerydataprovider.Query<NetworkGroup, NetworkGroupFilter> constructQuery(
				QueryDefinition<NetworkGroup, NetworkGroupFilter> queryDefinition) {
			return new NetworkGroupQuery(networkGroupRepository, (NetworkGroupQueryDefinition) queryDefinition);
		}
	}

	private static final class NetworkGroupQuery
			implements it.thisone.iotter.lazyquerydataprovider.Query<NetworkGroup, NetworkGroupFilter> {
		private final NetworkGroupRepository networkGroupRepository;
		private final NetworkGroupQueryDefinition queryDefinition;

		private NetworkGroupQuery(NetworkGroupRepository networkGroupRepository,
				NetworkGroupQueryDefinition queryDefinition) {
			this.networkGroupRepository = networkGroupRepository;
			this.queryDefinition = queryDefinition;
		}

		@Override
		public int size(QueryDefinition<NetworkGroup, NetworkGroupFilter> queryDefinition) {
			return (int) findPage(0, 1).getTotalElements();
		}

		@Override
		public Stream<NetworkGroup> loadItems(QueryDefinition<NetworkGroup, NetworkGroupFilter> queryDefinition,
				int offset, int limit) {
			int page = offset / this.queryDefinition.getPageSize();
			int size = this.queryDefinition.getPageSize();
			Page<NetworkGroup> groups = findPage(page, size);
			return groups.getContent().stream();
		}

		private Page<NetworkGroup> findPage(int page, int size) {
			Sort sort = buildSort();
			Pageable pageable = PageRequest.of(page, size, sort);
			NetworkGroupFilter filter = queryDefinition.getQueryFilter();

			String name = filter != null && filter.hasName() ? filter.getName().trim() : null;
			String description = filter != null && filter.hasDescription() ? filter.getDescription().trim() : null;
			String ownerFilter = filter != null && filter.hasOwner() ? filter.getOwner().trim() : null;

			if (queryDefinition.getNetwork() != null) {
				String owner = queryDefinition.getNetwork().getOwner();
				String networkId = queryDefinition.getNetwork().getId();
				if (name != null && description != null) {
					return networkGroupRepository
							.findByOwnerAndNetworkIdAndNameStartingWithIgnoreCaseAndDescriptionStartingWithIgnoreCase(
									owner, networkId, name, description, pageable);
				} else if (name != null) {
					return networkGroupRepository.findByOwnerAndNetworkIdAndNameStartingWithIgnoreCase(owner, networkId,
							name, pageable);
				} else if (description != null) {
					return networkGroupRepository.findByOwnerAndNetworkIdAndDescriptionStartingWithIgnoreCase(owner,
							networkId, description, pageable);
				}
				return networkGroupRepository.findByOwnerAndNetworkId(owner, networkId, pageable);
			}

			if (queryDefinition.getPermissions().isViewAllMode()) {
				if (ownerFilter != null) {
					if (name != null && description != null) {
						return networkGroupRepository
								.findByOwnerStartingWithIgnoreCaseAndNameStartingWithIgnoreCaseAndDescriptionStartingWithIgnoreCase(
										ownerFilter, name, description, pageable);
					} else if (name != null) {
						return networkGroupRepository.findByOwnerStartingWithIgnoreCaseAndNameStartingWithIgnoreCase(
								ownerFilter, name, pageable);
					} else if (description != null) {
						return networkGroupRepository
								.findByOwnerStartingWithIgnoreCaseAndDescriptionStartingWithIgnoreCase(ownerFilter,
										description, pageable);
					}
					return networkGroupRepository.findByOwnerStartingWithIgnoreCase(ownerFilter, pageable);
				}
				if (name != null && description != null) {
					return networkGroupRepository.findByNameStartingWithIgnoreCaseAndDescriptionStartingWithIgnoreCase(
							name, description, pageable);
				} else if (name != null) {
					return networkGroupRepository.findByNameStartingWithIgnoreCase(name, pageable);
				} else if (description != null) {
					return networkGroupRepository.findByDescriptionStartingWithIgnoreCase(description, pageable);
				}
				return networkGroupRepository.findAll(pageable);
			}

			String owner = queryDefinition.getOwner();
			if (name != null && description != null) {
				return networkGroupRepository.findByOwnerAndNameStartingWithIgnoreCaseAndDescriptionStartingWithIgnoreCase(
						owner, name, description, pageable);
			} else if (name != null) {
				return networkGroupRepository.findByOwnerAndNameStartingWithIgnoreCase(owner, name, pageable);
			} else if (description != null) {
				return networkGroupRepository.findByOwnerAndDescriptionStartingWithIgnoreCase(owner, description,
						pageable);
			}
			return networkGroupRepository.findByOwner(owner, pageable);
		}

		private Sort buildSort() {
			List<QuerySortOrder> sortOrders = queryDefinition.getSortOrders();
			if (sortOrders == null || sortOrders.isEmpty()) {
				return Sort.by(new Sort.Order(Sort.Direction.ASC, NAME));
			}

			List<Sort.Order> orders = new ArrayList<>();
			for (QuerySortOrder sortOrder : sortOrders) {
				String property = sortOrder.getSorted();
				Sort.Direction direction = sortOrder.getDirection() == SortDirection.ASCENDING ? Sort.Direction.ASC
						: Sort.Direction.DESC;
				orders.add(new Sort.Order(direction, property));
			}

			boolean hasNameSorting = sortOrders.stream().anyMatch(so -> NAME.equals(so.getSorted()));
			if (!hasNameSorting) {
				orders.add(new Sort.Order(Sort.Direction.ASC, NAME));
			}

			return Sort.by(orders);
		}

		private long countTotal() {
			return findPage(0, 1).getTotalElements();
		}
	}
}
