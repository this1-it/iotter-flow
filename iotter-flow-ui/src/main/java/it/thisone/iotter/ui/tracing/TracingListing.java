package it.thisone.iotter.ui.tracing;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;

import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.lazyquerydataprovider.FilterableQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDataProvider;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryFactory;
import it.thisone.iotter.persistence.model.Tracing;
import it.thisone.iotter.persistence.repository.TracingRepository;
import it.thisone.iotter.security.Permissions;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.AbstractBaseEntityListing;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.common.UIUtils;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TracingListing extends AbstractBaseEntityListing<Tracing> {

	private static final long serialVersionUID = 1L;
	private static final String OWNER = "owner";
	private static final String DEVICE = "device";
	private static final String TRACING_VIEW = "tracing.view";

	private final Permissions permissions;

	@Autowired
	private TracingRepository tracingRepository;

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
		HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.setWidthFull();
		toolbar.setSpacing(true);
		toolbar.setPadding(true);
		toolbar.addClassName(UIUtils.TOOLBAR_STYLE);

		queryDefinition = new TracingQueryDefinition(Tracing.class, DEFAULT_LIMIT, permissions);
		queryDefinition.setQueryFilter(currentFilter);
		dataProvider = new LazyQueryDataProvider<>(queryDefinition, new TracingQueryFactory(tracingRepository));
		dataProvider.setCacheQueries(false);
		dataProvider.setFilter(currentFilter);
		setBackendDataProvider(dataProvider);

		grid = createGrid();
		VerticalLayout content = createContent(grid);
		setSelectable(grid);

		getMainLayout().add(toolbar, content);
		getMainLayout().setFlexGrow(1f, content);

		getButtonsLayout().add(createViewButton());
		toolbar.add(getButtonsLayout());
		toolbar.setVerticalComponentAlignment(Alignment.CENTER, getButtonsLayout());
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
		initFilters(grid);
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

	private void initFilters(Grid<Tracing> grid) {
		HeaderRow filterRow = grid.appendHeaderRow();

		TextField owner = new TextField();
		owner.setPlaceholder("Filter...");
		owner.setWidthFull();
		owner.addClassName("small");
		owner.setValueChangeMode(ValueChangeMode.LAZY);
		filterRow.getCell(grid.getColumnByKey(OWNER)).setComponent(owner);
		owner.addValueChangeListener(event -> {
			currentFilter.setOwner(event.getValue());
			queryDefinition.setQueryFilter(currentFilter);
			setFilter(currentFilter);
			refreshData();
		});

		TextField device = new TextField();
		device.setPlaceholder("Filter...");
		device.setWidthFull();
		device.addClassName("small");
		device.setValueChangeMode(ValueChangeMode.LAZY);
		filterRow.getCell(grid.getColumnByKey(DEVICE)).setComponent(device);
		device.addValueChangeListener(event -> {
			currentFilter.setDevice(event.getValue());
			queryDefinition.setQueryFilter(currentFilter);
			setFilter(currentFilter);
			refreshData();
		});
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

	private Button createViewButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.EYE.create());
		button.getElement().setProperty("title", getI18nLabel("view_action"));
		button.addClickListener(event -> openDetails(getCurrentValue(), getI18nLabel("view_dialog")));
		button.setVisible(permissions.isViewMode());
		return button;
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

	private static final class TracingFilter {
		private String owner;
		private String device;

		public String getOwner() {
			return owner;
		}

		public void setOwner(String owner) {
			this.owner = owner;
		}

		public boolean hasOwner() {
			return owner != null && !owner.trim().isEmpty();
		}

		public String getDevice() {
			return device;
		}

		public void setDevice(String device) {
			this.device = device;
		}

		public boolean hasDevice() {
			return device != null && !device.trim().isEmpty();
		}

		@Override
		public String toString() {
			return "TracingFilter{owner=" + owner + ", device=" + device + "}";
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
			String owner = filter != null && filter.hasOwner() ? filter.getOwner().trim() : null;
			String device = filter != null && filter.hasDevice() ? filter.getDevice().trim() : null;

			if (owner != null && device != null) {
				return tracingRepository.findByOwnerStartingWithIgnoreCaseAndDeviceStartingWithIgnoreCase(owner, device, pageable);
			}
			if (owner != null) {
				return tracingRepository.findByOwnerStartingWithIgnoreCase(owner, pageable);
			}
			if (device != null) {
				return tracingRepository.findByDeviceStartingWithIgnoreCase(device, pageable);
			}
			return tracingRepository.findAll(pageable);
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
