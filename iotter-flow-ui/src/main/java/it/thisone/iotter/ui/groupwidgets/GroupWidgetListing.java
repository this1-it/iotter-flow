package it.thisone.iotter.ui.groupwidgets;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.vaadin.flow.components.TabSheet;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
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
import it.thisone.iotter.lazyquerydataprovider.FilterableQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDataProvider;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryFactory;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.repository.GroupWidgetRepository;
import it.thisone.iotter.security.Permissions;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.AbstractBaseEntityListing;
import it.thisone.iotter.ui.common.AuthenticatedUser;
import it.thisone.iotter.ui.common.ConfirmationDialogs;
import it.thisone.iotter.ui.common.PermissionsUtils;
import it.thisone.iotter.ui.common.SideDrawer;
import it.thisone.iotter.ui.networkgroups.NetworkGroupBindings;
import it.thisone.iotter.ui.providers.BackendServices;

import it.thisone.iotter.util.PopupNotification;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GroupWidgetListing extends AbstractBaseEntityListing<GroupWidget> {

    private static final long serialVersionUID = 1L;
    private static final String GROUPWIDGET_VIEW = "groupwidgets.view";

    private final Permissions permissions;
    private final GroupWidgetRepository groupWidgetRepository;
    private TabSheet tabsheet;
    private final BackendServices backendServices;
    private UserDetailsAdapter currentUser;

    private Network network;

    private Grid<GroupWidget> grid;
    private LazyQueryDataProvider<GroupWidget, GroupWidgetFilter> dataProvider;
    private GroupWidgetQueryDefinition queryDefinition;
    private GroupWidgetFilter currentFilter = new GroupWidgetFilter();



    @Autowired
    public GroupWidgetListing(AuthenticatedUser authenticatedUser, GroupWidgetRepository groupWidgetRepository,
            BackendServices backendServices) {
        super(GroupWidget.class, GROUPWIDGET_VIEW, GROUPWIDGET_VIEW, false);

        		currentUser = authenticatedUser.get()
				.orElseThrow(() -> new IllegalStateException("User must be authenticated to edit users"));

        this.permissions = PermissionsUtils.getPermissionsForGroupWidgetEntity(currentUser);
        setPermissions(permissions);
        this.groupWidgetRepository = groupWidgetRepository;
        this.backendServices = backendServices;
    }

    public void init(Network network,TabSheet tabsheet) {
        if (grid != null) {
            return;
        }
        this.tabsheet = tabsheet;
        this.network = network;
        buildLayout();
    }

    private void buildLayout() {
        queryDefinition = new GroupWidgetQueryDefinition(GroupWidget.class, DEFAULT_LIMIT, permissions);
        queryDefinition.setNetwork(network);
        queryDefinition.setOwner(currentUser.getTenant());
        queryDefinition.setPage(0, DEFAULT_LIMIT);
        queryDefinition.setQueryFilter(currentFilter);

        dataProvider = new LazyQueryDataProvider<>(queryDefinition, new GroupWidgetQueryFactory(groupWidgetRepository));
        dataProvider.setCacheQueries(false);
        dataProvider.setFilter(currentFilter);
        setBackendDataProvider(dataProvider);

        grid = createGrid();
        VerticalLayout tableLayout = createListingLayout(grid);
        setSelectable(grid);

        Button filterButton = new Button(getI18nLabel("filter"), VaadinIcon.FILTER.create());
        filterButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        filterButton.addThemeName("subtle");
        buildFilterPopover(filterButton);

        HorizontalLayout toolbar = buildSearchToolbar(filterButton, createAddButton());

        getMainLayout().add(toolbar, tableLayout);
        getMainLayout().setFlexGrow(1f, tableLayout);

        updateTotalCount();
    }

    @Override
    protected AbstractBaseEntityForm<GroupWidget> getEditor(GroupWidget item, boolean readOnly) {
        return new GroupWidgetForm(item, network, currentUser, backendServices.getNetworkService(), backendServices.getNetworkGroupService(),
                readOnly);
    }

    @Override
    protected void openDetails(GroupWidget item) {
        openVisualizer(item);
    }

    @Override
    protected void openRemove(GroupWidget item) {
        if (item == null) {
            return;
        }

        String header = String.format("%s: %s", getI18nLabel("remove_action"), item.getName());
        ConfirmationDialogs.openDanger(this, header, getI18nLabel("remove_dialog"), () -> {
            GroupWidgetDetails.removeExclusiveGroupIfNeeded(item, backendServices.getNetworkGroupService());
            backendServices.getGroupWidgetService().deleteById(item.getId());
            refreshCurrentPage();
        });
    }

    private Grid<GroupWidget> createGrid() {
        Grid<GroupWidget> table = new Grid<>();
        table.setDataProvider(dataProvider);
        table.setSelectionMode(Grid.SelectionMode.SINGLE);
        table.setSizeFull();

        List<Grid.Column<GroupWidget>> columns = new ArrayList<>();
        columns.add(table.addColumn(GroupWidget::getName).setKey("name"));
        columns.add(table.addColumn(this::formatNetwork).setKey("network"));
        columns.add(table.addColumn(GroupWidget::getCreator).setKey("creator"));

        if (permissions.isViewAllMode()) {
            columns.add(table.addColumn(GroupWidget::getOwner).setKey("owner"));
        }

        for (Grid.Column<GroupWidget> column : columns) {
            String columnKey = column.getKey();
            column.setSortable("name".equals(columnKey) || "owner".equals(columnKey));
            column.setHeader(getI18nLabel(columnKey));
        }

        table.setColumnOrder(columns.toArray(new Grid.Column[0]));
        table.addComponentColumn(widget -> {
            MenuBar menuBar = new MenuBar();
            menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
            MenuItem menuItem = menuBar.addItem("•••");
            menuItem.getElement().setAttribute("aria-label", "More options");
            SubMenu subMenu = menuItem.getSubMenu();
            if (permissions.isModifyMode()) {
                subMenu.addItem(getI18nLabel("map_action"), event -> openGroupWidgetMap(widget));
            }
            if (permissions.isViewMode()) {
                subMenu.addItem(getI18nLabel("view_action"), event -> openVisualizer(widget));
            }
            if (permissions.isModifyMode()) {
                subMenu.addItem(getI18nLabel("designer_action"), event -> openDesigner(widget));
            }
            if (permissions.isRemoveMode()) {
                subMenu.addItem(getI18nLabel("remove_action"), event -> openRemove(widget));
            }
            if (permissions.isModifyMode()) {
                subMenu.addItem(getI18nLabel("bindings_button"), event -> openAssociations(widget));
            }
            return menuBar;
        }).setWidth("70px").setFlexGrow(0).setKey("actions");
        return table;
    }

    private String formatNetwork(GroupWidget widget) {
        if (widget.getNetwork() == null) {
            return "";
        }
        return widget.getNetwork().getName();
    }

    private void buildFilterPopover(Button filterButton) {
        Popover popover = new Popover();
        popover.setTarget(filterButton);
        popover.setPosition(PopoverPosition.BOTTOM_START);

        ComboBox<String> ownerBox = createOwnerComboBox(backendServices.getUserService(),
                permissions.isViewAllMode(), currentFilter.getOwner());

        Button resetBtn = new Button(getTranslation("basic.editor.reset"), event -> ownerBox.clear());
        resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button cancelBtn = new Button(getTranslation("basic.editor.cancel"), event -> popover.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button updateBtn = new Button(getTranslation("basic.editor.filter"), event -> {
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

        VerticalLayout content = new VerticalLayout(ownerBox, buttons);
        content.setSpacing(true);
        content.setPadding(true);
        content.setWidth("300px");
        popover.add(content);
    }

    private VerticalLayout createListingLayout(Grid<GroupWidget> table) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setSpacing(true);
        layout.add(table);
        layout.setFlexGrow(1f, table);
        layout.setMargin(false);
		layout.setPadding(false);
        return layout;
    }

    private void refreshCurrentPage() {
        dataProvider.refreshAll();
        updateTotalCount();
        grid.getDataProvider().refreshAll();
        grid.asSingleSelect().clear();
    }

    private long getTotalCount() {
        return new GroupWidgetQuery(groupWidgetRepository, queryDefinition).countTotal();
    }

    private void updateTotalCount() {
        setTotalSize(getTotalCount());
    }

    private Button createAddButton() {
        Button button = new Button(getI18nLabel("add"), VaadinIcon.PLUS.create());
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.setId("add" + getId());
        button.addClickListener(event -> openEditor(new GroupWidget(), getI18nLabel("add_dialog")));
        button.setVisible(permissions.isCreateMode());
        return button;
    }

    private void openAssociations(GroupWidget item) {
        if (item == null) {
            return;
        }
        if (item.getGroup() == null) {
            PopupNotification.show(getI18nLabel("missing_bindings"), PopupNotification.Type.ERROR);
            return;
        }

        String caption = String.format("%s %s", getI18nLabel("bindings"), item.getName());
        NetworkGroupBindings content = new NetworkGroupBindings(item, backendServices.getNetworkGroupService(), backendServices.getDeviceService());
        Dialog dialog = createDialog(caption, content);
        dialog.open();
    }

    private void openGroupWidgetMap(GroupWidget item) {
        if (item == null) {
            return;
        }
        // TODO Flow migration: replace legacy Navigator-based map action with Flow routing.
        PopupNotification.show(getI18nLabel("map_action"), PopupNotification.Type.WARNING);
    }

    private void openDesigner(GroupWidget item) {
        if (item == null) {
            return;
        }
        GroupWidgetDesigner content = new GroupWidgetDesigner(item,
                currentUser, backendServices);
        Dialog dialog = createDialog(getI18nLabel("designer_action"), content);
        if (dialog instanceof SideDrawer) {
            dialog.addThemeName("side-drawer-fullscreen");
        }
        dialog.setWidth("100vw");
        dialog.setHeight("100vh");
        dialog.open();
    }

    private void openVisualizer(GroupWidget item) {
        if (item == null) {
            return;
        }
        GroupWidgetVisualizer visualizer = new GroupWidgetVisualizer(item.getId(), true,  backendServices);
        // Dialog dialog = createDialog(getI18nLabel("view_action"), visualizer);
        // if (dialog instanceof SideDrawer) {
        //     dialog.addThemeName("side-drawer-fullscreen");
        // }
        // dialog.setWidth("100vw");
        // dialog.setHeight("100vh");
        // dialog.open();


        if (visualizer != null) {
			Tab tab = tabsheet.addCloseableTab(item.getName(),visualizer);
			tabsheet.setSelectedTab(tab);
		}

    }

    private void openEditor(GroupWidget item, String label) {
        if (item == null) {
            return;
        }

        AbstractBaseEntityForm<GroupWidget> editor = getEditor(item, false);
        Dialog dialog = createDialog(label, editor);
        editor.setSavedHandler(entity -> {
            try {
                if (entity.isNew()) {
                    backendServices.getGroupWidgetService().create(entity);
                    dialog.close();
                    refreshCurrentPage();
                    openDesigner(entity);
                } else {
                    backendServices.getGroupWidgetService().update(entity);
                    dialog.close();
                    refreshCurrentPage();
                }
            } catch (Exception e) {
                PopupNotification.show(e.getMessage(), PopupNotification.Type.ERROR);
            }
        });
        dialog.open();
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


    private static final class GroupWidgetFilter {
        private String searchText;
        private String owner;

        public String getSearchText() {
            return searchText;
        }

        public void setSearchText(String searchText) {
            this.searchText = normalize(searchText);
        }

        public boolean hasSearchText() {
            return searchText != null && !searchText.trim().isEmpty();
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

        public boolean hasActiveFilter() {
            return hasOwner();
        }

        private String normalize(String value) {
            return value == null || value.trim().isEmpty() ? null : value.trim();
        }
    }

    private static final class GroupWidgetQueryDefinition extends LazyQueryDefinition<GroupWidget, GroupWidgetFilter>
            implements FilterableQueryDefinition<GroupWidgetFilter> {

        private static final long serialVersionUID = 1L;
        private GroupWidgetFilter queryFilter;
        private Network network;
        private final Permissions permissions;
        private String owner;
        private int pageIndex;
        private int pageSize;

        private GroupWidgetQueryDefinition(Class<GroupWidget> beanClass, int batchSize, Permissions permissions) {
            super(beanClass, batchSize);
            this.permissions = permissions;
        }

        @Override
        public void setQueryFilter(GroupWidgetFilter filter) {
            this.queryFilter = filter;
        }

        @Override
        public GroupWidgetFilter getQueryFilter() {
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

    private static final class GroupWidgetQueryFactory implements QueryFactory<GroupWidget, GroupWidgetFilter> {

        private final GroupWidgetRepository groupWidgetRepository;

        private GroupWidgetQueryFactory(GroupWidgetRepository groupWidgetRepository) {
            this.groupWidgetRepository = groupWidgetRepository;
        }

        @Override
        public it.thisone.iotter.lazyquerydataprovider.Query<GroupWidget, GroupWidgetFilter> constructQuery(
                QueryDefinition<GroupWidget, GroupWidgetFilter> queryDefinition) {
            return new GroupWidgetQuery(groupWidgetRepository, (GroupWidgetQueryDefinition) queryDefinition);
        }
    }

    private static final class GroupWidgetQuery
            implements it.thisone.iotter.lazyquerydataprovider.Query<GroupWidget, GroupWidgetFilter> {

        private final GroupWidgetRepository groupWidgetRepository;
        private final GroupWidgetQueryDefinition queryDefinition;

        private GroupWidgetQuery(GroupWidgetRepository groupWidgetRepository, GroupWidgetQueryDefinition queryDefinition) {
            this.groupWidgetRepository = groupWidgetRepository;
            this.queryDefinition = queryDefinition;
        }

        @Override
        public int size(QueryDefinition<GroupWidget, GroupWidgetFilter> queryDefinition) {
            Page<GroupWidget> page = findPage(0, 1);
            return (int) page.getTotalElements();
        }

        @Override
        public java.util.stream.Stream<GroupWidget> loadItems(
                QueryDefinition<GroupWidget, GroupWidgetFilter> queryDefinition, int offset, int limit) {
            int size = limit > 0 ? limit : this.queryDefinition.getPageSize();
            if (size <= 0) {
                size = 50;
            }
            int page = offset / size;
            Page<GroupWidget> widgets = findPage(page, size);
            return widgets.getContent().stream();
        }

        private Page<GroupWidget> findPage(int page, int size) {
            Sort sort = buildSort();
            Pageable pageable = PageRequest.of(page, size, sort);
            GroupWidgetFilter filter = queryDefinition.getQueryFilter();
            String search = filter != null && filter.hasSearchText() ? filter.getSearchText().trim() : null;
            String ownerFilter = filter != null && filter.hasOwner() ? filter.getOwner().trim() : null;

            if (queryDefinition.getNetwork() != null) {
                String owner = queryDefinition.getNetwork().getOwner();
                String networkId = queryDefinition.getNetwork().getId();
                if (search != null) {
                    return groupWidgetRepository.findByOwnerAndNetworkIdAndNameStartingWithIgnoreCase(owner, networkId,
                            search, pageable);
                }
                return groupWidgetRepository.findByOwnerAndNetworkId(owner, networkId, pageable);
            }

            if (queryDefinition.getPermissions().isViewAllMode()) {
                if (search != null && ownerFilter != null) {
                    return groupWidgetRepository.findByNameStartingWithIgnoreCaseAndOwnerStartingWithIgnoreCase(search,
                            ownerFilter, pageable);
                }
                if (search != null) {
                    return groupWidgetRepository.findByNameStartingWithIgnoreCase(search, pageable);
                }
                if (ownerFilter != null) {
                    return groupWidgetRepository.findByOwnerStartingWithIgnoreCase(ownerFilter, pageable);
                }
                return groupWidgetRepository.findAll(pageable);
            }

            String owner = queryDefinition.getOwner();
            if (search != null) {
                return groupWidgetRepository.findByOwnerAndNameStartingWithIgnoreCase(owner, search, pageable);
            }
            return groupWidgetRepository.findByOwner(owner, pageable);
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
                Sort.Direction direction = sortOrder.getDirection() == SortDirection.ASCENDING ? Sort.Direction.ASC
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
