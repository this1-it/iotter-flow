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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.lazyquerydataprovider.FilterableQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDataProvider;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryFactory;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.repository.GroupWidgetRepository;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.GroupWidgetService;
import it.thisone.iotter.persistence.service.NetworkGroupService;
import it.thisone.iotter.persistence.service.NetworkService;
import it.thisone.iotter.security.Permissions;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.AbstractBaseEntityListing;
import it.thisone.iotter.ui.common.AuthenticatedUser;
import it.thisone.iotter.ui.common.ConfirmationDialog;
import it.thisone.iotter.ui.common.PermissionsUtils;
import it.thisone.iotter.ui.common.SideDrawer;
import it.thisone.iotter.ui.common.ConfirmationDialog.Callback;
import it.thisone.iotter.ui.networkgroups.NetworkGroupBindings;
import it.thisone.iotter.util.PopupNotification;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GroupWidgetListing extends AbstractBaseEntityListing<GroupWidget> {

    private static final long serialVersionUID = 1L;
    private static final String ASSOCIATIONS_BUTTON = "bindings_button";
    private static final String DESIGNER = "designer";
    private static final String GROUPWIDGET_VIEW = "groupwidgets.view";

    private final Permissions permissions;
    private final GroupWidgetRepository groupWidgetRepository;
    private final GroupWidgetService groupWidgetService;
    private final NetworkService networkService;
    private final NetworkGroupService networkGroupService;
    private final DeviceService deviceService;
    private final AuthenticatedUser authenticatedUser;

    private Network network;

    private Grid<GroupWidget> grid;
    private LazyQueryDataProvider<GroupWidget, GroupWidgetFilter> dataProvider;
    private GroupWidgetQueryDefinition queryDefinition;
    private GroupWidgetFilter currentFilter = new GroupWidgetFilter();

    // @Autowired
    // public GroupWidgetListing(GroupWidgetRepository groupWidgetRepository, GroupWidgetService groupWidgetService,
    //         NetworkService networkService, NetworkGroupService networkGroupService, DeviceService deviceService,
    //         AuthenticatedUser authenticatedUser) {


    //     this(groupWidgetRepository, groupWidgetService,
    //             networkService, networkGroupService, deviceService, authenticatedUser);
    // }

    @Autowired
    public GroupWidgetListing(GroupWidgetRepository groupWidgetRepository,
            GroupWidgetService groupWidgetService, NetworkService networkService, NetworkGroupService networkGroupService,
            DeviceService deviceService, AuthenticatedUser authenticatedUser) {
        super(GroupWidget.class, GROUPWIDGET_VIEW, GROUPWIDGET_VIEW, false);

        		UserDetailsAdapter currentUser = authenticatedUser.get()
				.orElseThrow(() -> new IllegalStateException("User must be authenticated to edit users"));

        this.permissions = PermissionsUtils.getPermissionsForGroupWidgetEntity(currentUser);
        setPermissions(permissions);
        this.groupWidgetRepository = groupWidgetRepository;
        this.groupWidgetService = groupWidgetService;
        this.networkService = networkService;
        this.networkGroupService = networkGroupService;
        this.deviceService = deviceService;
        this.authenticatedUser = authenticatedUser;
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

        queryDefinition = new GroupWidgetQueryDefinition(GroupWidget.class, DEFAULT_LIMIT, permissions);
        queryDefinition.setNetwork(network);
        queryDefinition.setOwner(getCurrentUserTenant());
        queryDefinition.setPage(0, DEFAULT_LIMIT);
        queryDefinition.setQueryFilter(currentFilter);

        dataProvider = new LazyQueryDataProvider<>(queryDefinition, new GroupWidgetQueryFactory(groupWidgetRepository));
        dataProvider.setCacheQueries(false);
        dataProvider.setFilter(currentFilter);
        setBackendDataProvider(dataProvider);

        grid = createGrid();
        VerticalLayout tableLayout = createTableLayout(toolbar, grid);
        setSelectable(grid);

        getButtonsLayout().add(createLinkButton());
        getButtonsLayout().add(createViewButton());
        getButtonsLayout().add(createDesignerButton());
        getButtonsLayout().add(createRemoveButton());
        getButtonsLayout().add(createAssociationsButton());
        getButtonsLayout().add(createAddButton());

        toolbar.add(getButtonsLayout());
        toolbar.setAlignSelf(Alignment.END, getButtonsLayout());

        getMainLayout().add(tableLayout);
        getMainLayout().setFlexGrow(1f, tableLayout);

        updateTotalCount();
    }

    @Override
    protected AbstractBaseEntityForm<GroupWidget> getEditor(GroupWidget item, boolean readOnly) {
        return new GroupWidgetForm(item, network, authenticatedUser.get().orElse(null), networkService, networkGroupService,
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

        Callback callback = result -> {
            if (!result) {
                return;
            }
            GroupWidgetDetails.removeExclusiveGroupIfNeeded(item, networkGroupService);
            groupWidgetService.deleteById(item.getId());
            refreshCurrentPage();
        };

        Dialog dialog = new ConfirmationDialog(getI18nLabel("remove_dialog"), getI18nLabel("remove_action"), callback);
        dialog.open();
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
        initFilters(table);
        return table;
    }

    private String formatNetwork(GroupWidget widget) {
        if (widget.getNetwork() == null) {
            return "";
        }
        return widget.getNetwork().getName();
    }

    private void initFilters(Grid<GroupWidget> table) {
        HeaderRow filterRow = table.appendHeaderRow();

        TextField name = new TextField();
        name.setPlaceholder("Filter...");
        name.setWidthFull();
        name.setValueChangeMode(ValueChangeMode.LAZY);
        filterRow.getCell(table.getColumnByKey("name")).setComponent(name);
        name.addValueChangeListener(event -> {
            currentFilter.setName(event.getValue());
            queryDefinition.setQueryFilter(currentFilter);
            setFilter(currentFilter);
            refreshCurrentPage();
        });

        if (permissions.isViewAllMode()) {
            TextField owner = new TextField();
            owner.setPlaceholder("Filter...");
            owner.setWidthFull();
            owner.setValueChangeMode(ValueChangeMode.LAZY);
            filterRow.getCell(table.getColumnByKey("owner")).setComponent(owner);
            owner.addValueChangeListener(event -> {
                currentFilter.setOwner(event.getValue());
                queryDefinition.setQueryFilter(currentFilter);
                setFilter(currentFilter);
                refreshCurrentPage();
            });
        }
    }

    private VerticalLayout createTableLayout(HorizontalLayout toolbar, Grid<GroupWidget> table) {
        VerticalLayout tableLayout = new VerticalLayout();
        tableLayout.setSizeFull();
        tableLayout.setSpacing(true);
        tableLayout.add(toolbar, table);
        tableLayout.setFlexGrow(1f, table);
        return tableLayout;
    }

    private void refreshCurrentPage() {
        dataProvider.refreshAll();
        updateTotalCount();
        grid.getDataProvider().refreshAll();
        grid.asSingleSelect().clear();
        enableButtons(null);
    }

    private long getTotalCount() {
        return new GroupWidgetQuery(groupWidgetRepository, queryDefinition).countTotal();
    }

    private void updateTotalCount() {
        setTotalSize(getTotalCount());
    }

    private Button createAddButton() {
        Button button = new Button(VaadinIcon.PLUS.create());
        button.getElement().setProperty("title", getI18nLabel("add"));
        button.setId("add" + getId() + ALWAYS_ENABLED_BUTTON);
        button.addClickListener(event -> openEditor(new GroupWidget(), getI18nLabel("add_dialog")));
        button.setVisible(permissions.isCreateMode());
        return button;
    }

    private Button createRemoveButton() {
        Button button = new Button(VaadinIcon.TRASH.create());
        button.getElement().setProperty("title", getI18nLabel("remove_action"));
        button.setId("remove" + getId());
        button.addClickListener(event -> openRemove(getCurrentValue()));
        button.setVisible(permissions.isRemoveMode());
        return button;
    }

    private Button createViewButton() {
        Button button = new Button(VaadinIcon.BAR_CHART.create());
        button.getElement().setProperty("title", getI18nLabel("view_action"));
        button.addClickListener(event -> openVisualizer(getCurrentValue()));
        button.setVisible(permissions.isViewMode());
        return button;
    }

    private Button createDesignerButton() {
        Button button = new Button(VaadinIcon.EDIT.create());
        button.setId(DESIGNER);
        button.getElement().setProperty("title", getI18nLabel("designer_action"));
        button.addClickListener(event -> openDesigner(getCurrentValue()));
        button.setVisible(permissions.isModifyMode());
        return button;
    }

    private Button createLinkButton() {
        Button button = new Button(VaadinIcon.LINK.create());
        button.getElement().setProperty("title", getI18nLabel("map_action"));
        button.addClickListener(event -> openGroupWidgetMap(getCurrentValue()));
        button.setVisible(permissions.isModifyMode());
        return button;
    }

    private Button createAssociationsButton() {
        Button button = new Button(VaadinIcon.RANDOM.create());
        button.setId(ASSOCIATIONS_BUTTON);
        button.getElement().setProperty("title", getI18nLabel(ASSOCIATIONS_BUTTON));
        button.addClickListener(event -> openAssociations(getCurrentValue()));
        button.setVisible(permissions.isModifyMode());
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
        NetworkGroupBindings content = new NetworkGroupBindings(item, networkGroupService, deviceService);
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
        GroupWidgetDesigner content = new GroupWidgetDesigner(item, groupWidgetService, networkService, networkGroupService,
                authenticatedUser.get().orElse(null));
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
        GroupWidgetVisualizer visualizer = new GroupWidgetVisualizer(item.getId(), true, groupWidgetService);
        Dialog dialog = createDialog(getI18nLabel("view_action"), visualizer);
        dialog.open();
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
                    groupWidgetService.create(entity);
                    dialog.close();
                    refreshCurrentPage();
                    openDesigner(entity);
                } else {
                    groupWidgetService.update(entity);
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
    public void enableButtons(GroupWidget item) {
        super.enableButtons(item);
        if (item == null) {
            return;
        }

        // UserDetailsAdapter user = authenticatedUser.get().orElse(null);
        // if (user == null) {
        //     return;
        // }

        // boolean author = item.isAuthor(user.getUsername());
        // boolean supervisor = user.hasRole(Constants.ROLE_SUPERVISOR);
        // boolean administrator = user.hasRole(Constants.ROLE_ADMINISTRATOR);
        // if (supervisor || administrator) {
        //     author = true;
        // }

        // boolean automatic = item.isExclusive();
        // Device device = deviceService.findBySerial(item.getDevice());
        // if (device == null) {
        //     automatic = false;
        // }

        // getButtonsLayout().getChildren().forEach(component -> {
        //     if (component instanceof Button) {
        //         Button button = (Button) component;
        //         String id = button.getId().orElse("");
        //         if (id.isEmpty()) {
        //             return;
        //         }

        //         button.setEnabled(author);
        //         if (supervisor && id.contains(DESIGNER)) {
        //             button.setEnabled(true);
        //         }
        //         if (automatic && id.contains(ASSOCIATIONS_BUTTON)) {
        //             button.setEnabled(author);
        //         }
        //         if (id.contains("add")) {
        //             button.setEnabled(true);
        //         }
        //         if (id.contains("remove")) {
        //             button.setEnabled(supervisor || !automatic);
        //         }
        //     }
        // });
    }

    private String getCurrentUserTenant() {
        return authenticatedUser.get().map(UserDetailsAdapter::getTenant).orElse(null);
    }

    private static final class GroupWidgetFilter {
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
            String name = filter != null && filter.hasName() ? filter.getName().trim() : null;
            String ownerFilter = filter != null && filter.hasOwner() ? filter.getOwner().trim() : null;

            if (queryDefinition.getNetwork() != null) {
                String owner = queryDefinition.getNetwork().getOwner();
                String networkId = queryDefinition.getNetwork().getId();
                if (name != null) {
                    return groupWidgetRepository.findByOwnerAndNetworkIdAndNameStartingWithIgnoreCase(owner, networkId,
                            name, pageable);
                }
                return groupWidgetRepository.findByOwnerAndNetworkId(owner, networkId, pageable);
            }

            if (queryDefinition.getPermissions().isViewAllMode()) {
                if (name != null && ownerFilter != null) {
                    return groupWidgetRepository.findByNameStartingWithIgnoreCaseAndOwnerStartingWithIgnoreCase(name,
                            ownerFilter, pageable);
                }
                if (name != null) {
                    return groupWidgetRepository.findByNameStartingWithIgnoreCase(name, pageable);
                }
                if (ownerFilter != null) {
                    return groupWidgetRepository.findByOwnerStartingWithIgnoreCase(ownerFilter, pageable);
                }
                return groupWidgetRepository.findAll(pageable);
            }

            String owner = queryDefinition.getOwner();
            if (name != null) {
                return groupWidgetRepository.findByOwnerAndNameStartingWithIgnoreCase(owner, name, pageable);
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
