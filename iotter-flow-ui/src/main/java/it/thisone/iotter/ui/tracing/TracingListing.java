package it.thisone.iotter.ui.tracing;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

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
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.lazyquerydataprovider.FilterableQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDataProvider;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryFactory;
import it.thisone.iotter.persistence.model.Tracing;
import it.thisone.iotter.persistence.repository.TracingRepository;
import it.thisone.iotter.persistence.service.UserService;
import it.thisone.iotter.security.Permissions;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.AbstractBaseEntityListing;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.common.charts.TimeIntervalHelper;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TracingListing extends AbstractBaseEntityListing<Tracing> {

	private static final long serialVersionUID = 1L;
	private static final String OWNER = "owner";
	private static final String DEVICE = "device";
	private static final String TRACING_VIEW = "tracing.view";

	private final Permissions permissions;
	private final TimeIntervalHelper timeIntervalHelper = new TimeIntervalHelper(TimeZone.getDefault());

	@Autowired
	private TracingRepository tracingRepository;
	@Autowired
	private UserService userService;

	private Grid<Tracing> grid;
	private LazyQueryDataProvider<Tracing, TracingFilter> dataProvider;
	private TracingQueryDefinition queryDefinition;
	private TracingFilter currentFilter = new TracingFilter();

	public TracingListing() {
		this(new Permissions(true));
	}

	public TracingListing(List<Tracing> listing, String[] visibleColumns) {
		this(new Permissions(true));
		buildListLayout(listing, visibleColumns);
	}

	private TracingListing(Permissions permissions) {
		super(Tracing.class, TRACING_VIEW, TRACING_VIEW, false);
		this.permissions = permissions;
		setPermissions(permissions);
	}

	public void init() {
		if (grid != null) {
			return;
		}
		buildLayout();
	}

	private void buildLayout() {
		queryDefinition = new TracingQueryDefinition(Tracing.class, DEFAULT_LIMIT, permissions);
		queryDefinition.setQueryFilter(currentFilter);
		dataProvider = new LazyQueryDataProvider<>(queryDefinition, new TracingQueryFactory(tracingRepository));
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

		HorizontalLayout toolbar = buildSearchToolbar(filterButton, createPlaceholderAddButton());

		getMainLayout().add(toolbar, content);
		getMainLayout().setFlexGrow(1f, content);
		enableButtons(null);
	}

	private void buildListLayout(List<Tracing> listing, String[] visibleColumns) {
		grid = createGrid(listing, visibleColumns);
		VerticalLayout content = createContent(grid);
		setSelectable(grid);
		getMainLayout().add(content);
		getMainLayout().setFlexGrow(1f, content);
	}

	@Override
	protected AbstractBaseEntityForm<Tracing> getEditor(Tracing item, boolean readOnly) {
		return null;
	}

	private void refreshData() {
		dataProvider.refreshAll();
		grid.scrollToStart();
		grid.deselectAll();
		enableButtons(null);
	}

	private Grid<Tracing> createGrid() {
		Grid<Tracing> grid = new Grid<>();
		grid.setDataProvider(dataProvider);
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.setSizeFull();
		grid.addClassName("smallgrid");

		List<Grid.Column<Tracing>> columns = new ArrayList<>();
		columns.add(grid.addComponentColumn(this::buildCategoryIcon).setKey("category").setHeader(""));
		columns.add(grid.addColumn(Tracing::getAction).setKey("action"));
		columns.add(grid.addColumn(Tracing::getOwner).setKey(OWNER));
		columns.add(grid.addColumn(Tracing::getDevice).setKey(DEVICE));
		columns.add(grid.addColumn(Tracing::getTimeStamp).setKey("timeStamp"));
		columns.add(grid.addColumn(Tracing::getNetwork).setKey("network"));
		columns.add(grid.addColumn(Tracing::getAdministrator).setKey("administrator"));
		columns.add(grid.addColumn(Tracing::getDescription).setKey("description"));

		for (Grid.Column<Tracing> column : columns) {
			String columnId = column.getKey();
			if (!OWNER.equals(columnId) && !DEVICE.equals(columnId)) {
				column.setSortable(false);
			}
			if (!"category".equals(columnId)) {
				column.setHeader(getI18nLabel(columnId));
			}
		}

		grid.setColumnOrder(columns.toArray(new Grid.Column[0]));
		grid.addComponentColumn(item -> {
			MenuBar menuBar = new MenuBar();
			menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
			MenuItem menuItem = menuBar.addItem("•••");
			menuItem.getElement().setAttribute("aria-label", "More options");
			SubMenu subMenu = menuItem.getSubMenu();
			if (permissions.isViewMode()) {
				subMenu.addItem(getI18nLabel("view_action"), event -> openDetails(item));
			}
			return menuBar;
		}).setWidth("70px").setFlexGrow(0).setKey("actions");
		return grid;
	}

	private Grid<Tracing> createGrid(List<Tracing> listing, String[] visibleColumns) {
		Grid<Tracing> grid = new Grid<>();
		grid.setSizeFull();
		grid.addClassName("smallgrid");
		setDataProvider(new ListDataProvider<>(listing));
		grid.setDataProvider(getDataProvider());

		List<String> columnsToShow = Arrays.asList(visibleColumns);
		List<Grid.Column<Tracing>> columns = new ArrayList<>();

		for (String columnId : columnsToShow) {
			Grid.Column<Tracing> column = null;
			if ("category".equals(columnId)) {
				column = grid.addComponentColumn(this::buildCategoryIcon).setKey(columnId).setHeader("");
			} else if ("action".equals(columnId)) {
				column = grid.addColumn(Tracing::getAction).setKey(columnId).setHeader(getI18nLabel(columnId));
			} else if (OWNER.equals(columnId)) {
				column = grid.addColumn(Tracing::getOwner).setKey(columnId).setHeader(getI18nLabel(columnId));
			} else if (DEVICE.equals(columnId)) {
				column = grid.addColumn(Tracing::getDevice).setKey(columnId).setHeader(getI18nLabel(columnId));
			} else if ("timeStamp".equals(columnId)) {
				column = grid.addColumn(Tracing::getTimeStamp).setKey(columnId).setHeader(getI18nLabel(columnId));
			} else if ("network".equals(columnId)) {
				column = grid.addColumn(Tracing::getNetwork).setKey(columnId).setHeader(getI18nLabel(columnId));
			} else if ("administrator".equals(columnId)) {
				column = grid.addColumn(Tracing::getAdministrator).setKey(columnId).setHeader(getI18nLabel(columnId));
			} else if ("description".equals(columnId)) {
				column = grid.addColumn(Tracing::getDescription).setKey(columnId).setHeader(getI18nLabel(columnId));
			}
			if (column != null) {
				columns.add(column);
			}
		}

		if (!columns.isEmpty()) {
			grid.setColumnOrder(columns.toArray(new Grid.Column[0]));
		}
		return grid;
	}

	private void buildFilterPopover(Button filterButton) {
		Popover popover = new Popover();
		popover.setTarget(filterButton);
		popover.setPosition(PopoverPosition.BOTTOM_START);

		ComboBox<TracingAction> actionBox = new ComboBox<>(getI18nLabel("action"));
		actionBox.setItems(TracingAction.values());
		actionBox.setClearButtonVisible(true);
		actionBox.setPlaceholder(getI18nLabel("any"));
		actionBox.setWidthFull();
		actionBox.setValue(currentFilter.getAction());

		ComboBox<String> administratorBox = createAdministratorComboBox(currentFilter.getAdministrator());
		DatePicker fromDateField = timeIntervalHelper.createDateField();
		fromDateField.setLabel(getI18nLabel("from_date"));
		fromDateField.setWidthFull();
		fromDateField.setValue(timeIntervalHelper.toLocalDate(currentFilter.getFromDate()));

		DatePicker toDateField = timeIntervalHelper.createDateField();
		toDateField.setLabel(getI18nLabel("to_date"));
		toDateField.setWidthFull();
		toDateField.setValue(timeIntervalHelper.toLocalDate(currentFilter.getToDate()));

		Button resetBtn = new Button(getTranslation("basic.editor.reset"), e -> {
			actionBox.clear();
			administratorBox.clear();
			fromDateField.clear();
			toDateField.clear();
		});
		resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

		Button cancelBtn = new Button(getTranslation("basic.editor.cancel"), e -> popover.close());
		cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

		Button updateBtn = new Button(getTranslation("basic.editor.filter"), e -> {
			currentFilter.setAction(actionBox.getValue());
			currentFilter.setAdministrator(administratorBox.getValue());
			currentFilter.setFromDate(toStartOfDay(fromDateField.getValue()));
			currentFilter.setToDate(toEndOfDay(toDateField.getValue()));
			queryDefinition.setQueryFilter(currentFilter);
			setFilter(currentFilter);
			refreshData();
			popover.close();
			filterButton.setClassName(currentFilter.hasActiveFilter() ? "filter-active" : "");
		});
		updateBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		HorizontalLayout buttons = new HorizontalLayout(resetBtn, cancelBtn, updateBtn);
		buttons.setJustifyContentMode(JustifyContentMode.END);
		buttons.setWidthFull();

		VerticalLayout content = new VerticalLayout(actionBox, administratorBox, fromDateField, toDateField, buttons);
		content.setSpacing(true);
		content.setPadding(true);
		content.setWidth("300px");
		popover.add(content);
	}

	private Date toStartOfDay(LocalDate localDate) {
		if (localDate == null) {
			return null;
		}
		return timeIntervalHelper.toDate(localDate);
	}

	private Date toEndOfDay(LocalDate localDate) {
		if (localDate == null) {
			return null;
		}
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		calendar.setTime(timeIntervalHelper.toDate(localDate));
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}

	private ComboBox<String> createAdministratorComboBox(String value) {
		ComboBox<String> administratorBox = new ComboBox<>(getI18nLabel("administrator"));
		administratorBox.setWidthFull();
		administratorBox.setClearButtonVisible(true);
		administratorBox.setPlaceholder(getI18nLabel("any"));
		List<String> administrators = userService.findByRole(Constants.ROLE_ADMINISTRATOR).stream()
				.map(user -> user.getUsername())
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.collect(Collectors.toList());
		administratorBox.setItems((item, filter) -> item.toLowerCase().startsWith(filter.toLowerCase()),
				administrators);
		administratorBox.setValue(value);
		return administratorBox;
	}

	private Button createPlaceholderAddButton() {
		Button button = new Button();
		button.setVisible(false);
		return button;
	}

	private VerticalLayout createContent(Grid<Tracing> grid) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.add(grid);
		layout.setFlexGrow(1f, grid);
		return layout;
	}

	private Icon buildCategoryIcon(Tracing tracing) {
		VaadinIcon iconType = VaadinIcon.RANDOM;
		if (tracing != null && tracing.getAction() != null) {
			switch (tracing.getAction()) {
			case LOGIN_FAILED:
			case LOGIN:
				iconType = VaadinIcon.KEY;
				break;
			case CHANNEL_ACTIVATED:
			case CHANNEL_CREATION:
			case CHANNEL_DEACTIVATED:
			case DEVICE_CONFIGURATION:
				iconType = VaadinIcon.COGS;
				break;
			case NETWORK_CREATION:
			case DEVICE_CREATION:
			case DEVICE_UPDATE:
				iconType = VaadinIcon.DATABASE;
				break;
			case DEVICE_EXPORT:
				iconType = VaadinIcon.DOWNLOAD;
				break;
			case ERROR_REST:
			case ERROR_UI:
				iconType = VaadinIcon.BUG;
				break;
			case MQTT_PROVISIONED:
				iconType = VaadinIcon.ENVELOPE;
				break;
			default:
				iconType = VaadinIcon.RANDOM;
				break;
			}
		}
		return iconType.create();
	}

	private void openDetails(Tracing item, String label) {
		if (item == null) {
			return;
		}
		TracingDetails details = new TracingDetails(item);
		Dialog dialog = BaseComponent.createDialog(label, details);
		dialog.open();
	}

	@Override
	protected void openDetails(Tracing item) {
		openDetails(item, getI18nLabel("view_dialog"));
	}

	@Override
	protected void openRemove(Tracing item) {
		// no remove action for tracing entries
	}

	@Override
	protected void onSearch(String searchText) {
		currentFilter.setSearchText(searchText);
		queryDefinition.setQueryFilter(currentFilter);
		setFilter(currentFilter);
		refreshData();
	}

	@Override
	protected void onRefresh() {
		refreshData();
	}

	private static final class TracingFilter {
		private String searchText;
		private TracingAction action;
		private String administrator;
		private Date fromDate;
		private Date toDate;

		public String getSearchText() {
			return searchText;
		}

		public void setSearchText(String searchText) {
			this.searchText = normalize(searchText);
		}

		public boolean hasSearchText() {
			return searchText != null && !searchText.trim().isEmpty();
		}

		public TracingAction getAction() {
			return action;
		}

		public void setAction(TracingAction action) {
			this.action = action;
		}

		public boolean hasAction() {
			return action != null;
		}

		public String getAdministrator() {
			return administrator;
		}

		public void setAdministrator(String administrator) {
			this.administrator = normalize(administrator);
		}

		public boolean hasAdministrator() {
			return administrator != null && !administrator.trim().isEmpty();
		}

		public Date getFromDate() {
			return fromDate;
		}

		public void setFromDate(Date fromDate) {
			this.fromDate = fromDate;
		}

		public boolean hasFromDate() {
			return fromDate != null;
		}

		public Date getToDate() {
			return toDate;
		}

		public void setToDate(Date toDate) {
			this.toDate = toDate;
		}

		public boolean hasToDate() {
			return toDate != null;
		}

		public String getOwner() {
			return searchText;
		}

		public void setOwner(String owner) {
			setSearchText(owner);
		}

		public boolean hasOwner() {
			return hasSearchText();
		}

		public String getDevice() {
			return searchText;
		}

		public void setDevice(String device) {
			setSearchText(device);
		}

		public boolean hasDevice() {
			return hasSearchText();
		}

		public boolean hasActiveFilter() {
			return hasAction() || hasAdministrator() || hasFromDate() || hasToDate();
		}

		private String normalize(String value) {
			return value != null && value.trim().isEmpty() ? null : value;
		}

		@Override
		public String toString() {
			return "TracingFilter{searchText=" + searchText + ", action=" + action + ", administrator="
					+ administrator + ", fromDate=" + fromDate + ", toDate=" + toDate + "}";
		}
	}

	private static final class TracingQueryDefinition extends LazyQueryDefinition<Tracing, TracingFilter>
			implements FilterableQueryDefinition<TracingFilter> {

		private static final long serialVersionUID = 1L;

		private TracingFilter queryFilter;
		private final Permissions permissions;

		private TracingQueryDefinition(Class<Tracing> beanClass, int batchSize, Permissions permissions) {
			super(beanClass, batchSize);
			this.permissions = permissions;
		}

		@Override
		public void setQueryFilter(TracingFilter filter) {
			this.queryFilter = filter;
		}

		@Override
		public TracingFilter getQueryFilter() {
			return queryFilter;
		}

		public Permissions getPermissions() {
			return permissions;
		}
	}

	private static final class TracingQueryFactory implements QueryFactory<Tracing, TracingFilter> {
		private final TracingRepository tracingRepository;

		private TracingQueryFactory(TracingRepository tracingRepository) {
			this.tracingRepository = tracingRepository;
		}

		@Override
		public it.thisone.iotter.lazyquerydataprovider.Query<Tracing, TracingFilter> constructQuery(
				QueryDefinition<Tracing, TracingFilter> queryDefinition) {
			return new TracingQuery(tracingRepository, (TracingQueryDefinition) queryDefinition);
		}
	}

	private static final class TracingQuery implements it.thisone.iotter.lazyquerydataprovider.Query<Tracing, TracingFilter> {
		private final TracingRepository tracingRepository;
		private final TracingQueryDefinition queryDefinition;

		private TracingQuery(TracingRepository tracingRepository, TracingQueryDefinition queryDefinition) {
			this.tracingRepository = tracingRepository;
			this.queryDefinition = queryDefinition;
		}

		@Override
		public int size(QueryDefinition<Tracing, TracingFilter> queryDefinition) {
			Page<Tracing> page = findPage(0, 1);
			return (int) page.getTotalElements();
		}

		@Override
		public java.util.stream.Stream<Tracing> loadItems(QueryDefinition<Tracing, TracingFilter> queryDefinition, int offset,
				int limit) {
			Page<Tracing> tracings = findPage(offset / limit, limit);
			return tracings.getContent().stream();
		}

		private Page<Tracing> findPage(int page, int size) {
			Sort sort = buildSort();
			Pageable pageable = PageRequest.of(page, size, sort);
			TracingFilter filter = queryDefinition.getQueryFilter();
			String search = filter != null && filter.hasSearchText() ? filter.getSearchText().trim() : null;
			TracingAction action = filter != null && filter.hasAction() ? filter.getAction() : null;
			String administrator = filter != null && filter.hasAdministrator()
					? filter.getAdministrator().trim()
					: null;
			Date fromDate = filter != null && filter.hasFromDate() ? filter.getFromDate() : null;
			Date toDate = filter != null && filter.hasToDate() ? filter.getToDate() : null;

			return tracingRepository.findAllByFilters(search, action, administrator, fromDate, toDate, pageable);
		}

		private Sort buildSort() {
			List<QuerySortOrder> sortOrders = queryDefinition.getSortOrders();
			if (sortOrders == null || sortOrders.isEmpty()) {
				return Sort.by("timeStamp").descending();
			}

			List<Sort.Order> orders = new ArrayList<>();
			for (QuerySortOrder sortOrder : sortOrders) {
				String property = sortOrder.getSorted();
				if (!OWNER.equals(property) && !DEVICE.equals(property)) {
					continue;
				}
				Sort.Direction direction = sortOrder.getDirection() == SortDirection.ASCENDING
						? Sort.Direction.ASC
						: Sort.Direction.DESC;
				orders.add(new Sort.Order(direction, property));
			}
			if (orders.isEmpty()) {
				return Sort.by(OWNER).ascending().and(Sort.by(DEVICE).ascending());
			}
			return Sort.by(orders);
		}
	}
}
