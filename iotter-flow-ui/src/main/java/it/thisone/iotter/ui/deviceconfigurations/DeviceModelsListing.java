package it.thisone.iotter.ui.deviceconfigurations;

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
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;

import it.thisone.iotter.lazyquerydataprovider.FilterableQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDataProvider;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryFactory;
import it.thisone.iotter.persistence.model.DeviceModel;
import it.thisone.iotter.persistence.repository.DeviceModelRepository;
import it.thisone.iotter.persistence.service.DeviceModelService;
import it.thisone.iotter.security.Permissions;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.AbstractBaseEntityListing;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.common.ConfirmationDialog;
import it.thisone.iotter.ui.common.ConfirmationDialog.Callback;
import it.thisone.iotter.ui.common.SideDrawer;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.util.PopupNotification;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DeviceModelsListing extends AbstractBaseEntityListing<DeviceModel> {

	private static final long serialVersionUID = 1L;
	private static final String NAME = "name";
	private static final String DEVICE_MODEL_VIEW = "device_model.view";

	private final Permissions permissions;

	@Autowired
	private DeviceModelRepository deviceModelRepository;

	@Autowired
	private DeviceModelService deviceModelService;

	private Grid<DeviceModel> grid;
	private LazyQueryDataProvider<DeviceModel, DeviceModelFilter> dataProvider;
	private DeviceModelQueryDefinition queryDefinition;
	private DeviceModelFilter currentFilter = new DeviceModelFilter();

	public DeviceModelsListing() {
		this(new Permissions(true));
	}

	private DeviceModelsListing(Permissions permissions) {
		super(DeviceModel.class, DEVICE_MODEL_VIEW, DEVICE_MODEL_VIEW, false);
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

		queryDefinition = new DeviceModelQueryDefinition(DeviceModel.class, DEFAULT_LIMIT, permissions);
		queryDefinition.setQueryFilter(currentFilter);
		dataProvider = new LazyQueryDataProvider<>(queryDefinition, new DeviceModelQueryFactory(deviceModelRepository));
		dataProvider.setCacheQueries(false);
		dataProvider.setFilter(currentFilter);
		setBackendDataProvider(dataProvider);

		grid = createGrid();
		VerticalLayout content = createContent(grid);
		setSelectable(grid);

		getMainLayout().add(toolbar, content);
		getMainLayout().setFlexGrow(1f, content);

		getButtonsLayout().add(createRemoveButton(), createModifyButton(), createAddButton());
		toolbar.add(getButtonsLayout());
		toolbar.setVerticalComponentAlignment(Alignment.CENTER, getButtonsLayout());
		enableButtons(null);
	}

	@Override
	public AbstractBaseEntityForm<DeviceModel> getEditor(DeviceModel item, boolean readonly) {
		return new DeviceModelForm(item);
	}

	private Grid<DeviceModel> createGrid() {
		Grid<DeviceModel> grid = new Grid<>();
		grid.setDataProvider(dataProvider);
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.setSizeFull();
		grid.addClassName("smallgrid");

		List<Grid.Column<DeviceModel>> columns = new ArrayList<>();
		Grid.Column<DeviceModel> nameColumn = grid.addColumn(DeviceModel::getName).setKey(NAME);
		columns.add(nameColumn);
		Grid.Column<DeviceModel> protocolColumn = grid.addColumn(DeviceModel::getProtocol).setKey("protocol");
		columns.add(protocolColumn);

		// if (permissions.isViewAllMode()) {
		// 	Grid.Column<DeviceModel> ownerColumn = grid.addColumn(DeviceModel::getOwner).setKey("owner");
		// 	columns.add(ownerColumn);
		// }

		for (Grid.Column<DeviceModel> column : columns) {
			String columnId = column.getKey();
			column.setSortable(NAME.equals(columnId));
			column.setHeader(getI18nLabel(columnId));
		}

		grid.setColumnOrder(columns.toArray(new Grid.Column[0]));
		initFilters(grid);
		return grid;
	}

	private void initFilters(Grid<DeviceModel> grid) {
		HeaderRow filterRow = grid.appendHeaderRow();
		TextField name = new TextField();
		name.setPlaceholder("Filter...");
		name.setWidthFull();
		name.setValueChangeMode(ValueChangeMode.LAZY);
		filterRow.getCell(grid.getColumnByKey(NAME)).setComponent(name);
		name.addValueChangeListener(event -> {
			currentFilter.setName(event.getValue());
			queryDefinition.setQueryFilter(currentFilter);
			setFilter(currentFilter);
			refreshData();
		});
	}

	private VerticalLayout createContent(Grid<DeviceModel> grid) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.add(grid);
		layout.setFlexGrow(1f, grid);
		return layout;
	}

	private void refreshData() {
		dataProvider.refreshAll();
		grid.scrollToStart();
		grid.deselectAll();
		enableButtons(null);
	}

	private Button createAddButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.PLUS.create());
		button.getElement().setProperty("title", getI18nLabel("add"));
		button.setId("add" + getId() + ALWAYS_ENABLED_BUTTON);
		button.addClickListener(event -> openEditor(new DeviceModel(), getI18nLabel("add_dialog")));
		button.setVisible(permissions.isCreateMode());
		return button;
	}

	private Button createModifyButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.EDIT.create());
		button.getElement().setProperty("title", getI18nLabel("modify_action"));
		button.addClickListener(event -> openEditor(getCurrentValue(), getI18nLabel("modify_dialog")));
		button.setVisible(permissions.isModifyMode());
		return button;
	}

	private Button createRemoveButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.TRASH.create());
		button.getElement().setProperty("title", getI18nLabel("remove_action"));
		button.addClickListener(event -> openRemove(getCurrentValue()));
		button.setVisible(permissions.isRemoveMode());
		return button;
	}

	@Override
	protected void openRemove(DeviceModel item) {
		if (item == null) {
			return;
		}

		Callback callback = result -> {
			if (!result) {
				return;
			}

			deviceModelService.deleteById(item.getId());
			refreshCurrentPage();
		};

		Dialog dialog = new ConfirmationDialog(getI18nLabel("remove_dialog"), getI18nLabel("remove_action"), callback);
		dialog.open();
	}

	private void openEditor(DeviceModel item, String label) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityForm<DeviceModel> editor = getEditor(item, false);
		Dialog dialog = BaseComponent.createDialog(label, editor);
		editor.setSavedHandler(entity -> {
			try {
				if (entity.isNew()) {
					deviceModelService.create(entity);
				} else {
					deviceModelService.update(entity);
				}
				dialog.close();
				refreshData();
			} catch (Exception e) {
				PopupNotification.show(e.getMessage(), PopupNotification.Type.ERROR);
			}
		});
		dialog.open();
	}

	@Override
	protected void openDetails(DeviceModel item) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityForm<DeviceModel> details = getEditor(item, true);
		SideDrawer dialog = (SideDrawer) BaseComponent.createDialog(getI18nLabel("view_dialog"), details);
		dialog.open();
	}

	private void refreshCurrentPage() {
		refreshData();
	}

	private static final class DeviceModelFilter {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean hasName() {
			return name != null && !name.trim().isEmpty();
		}

		@Override
		public String toString() {
			return "DeviceModelFilter{name=" + name + "}";
		}
	}

	private static final class DeviceModelQueryDefinition extends LazyQueryDefinition<DeviceModel, DeviceModelFilter>
			implements FilterableQueryDefinition<DeviceModelFilter> {

		private static final long serialVersionUID = 1L;
		private DeviceModelFilter queryFilter;
		private final Permissions permissions;

		private DeviceModelQueryDefinition(Class<DeviceModel> beanClass, int batchSize, Permissions permissions) {
			super(beanClass, batchSize);
			this.permissions = permissions;
		}

		@Override
		public void setQueryFilter(DeviceModelFilter filter) {
			this.queryFilter = filter;
		}

		@Override
		public DeviceModelFilter getQueryFilter() {
			return queryFilter;
		}

		public Permissions getPermissions() {
			return permissions;
		}
	}

	private static final class DeviceModelQueryFactory implements QueryFactory<DeviceModel, DeviceModelFilter> {
		private final DeviceModelRepository deviceModelRepository;

		private DeviceModelQueryFactory(DeviceModelRepository deviceModelRepository) {
			this.deviceModelRepository = deviceModelRepository;
		}

		@Override
		public it.thisone.iotter.lazyquerydataprovider.Query<DeviceModel, DeviceModelFilter> constructQuery(
				QueryDefinition<DeviceModel, DeviceModelFilter> queryDefinition) {
			return new DeviceModelQuery(deviceModelRepository, (DeviceModelQueryDefinition) queryDefinition);
		}
	}

	private static final class DeviceModelQuery
			implements it.thisone.iotter.lazyquerydataprovider.Query<DeviceModel, DeviceModelFilter> {

		private final DeviceModelRepository deviceModelRepository;
		private final DeviceModelQueryDefinition queryDefinition;

		private DeviceModelQuery(DeviceModelRepository deviceModelRepository, DeviceModelQueryDefinition queryDefinition) {
			this.deviceModelRepository = deviceModelRepository;
			this.queryDefinition = queryDefinition;
		}

		@Override
		public int size(QueryDefinition<DeviceModel, DeviceModelFilter> queryDefinition) {
			Page<DeviceModel> page = findPage(0, 1);
			return (int) page.getTotalElements();
		}

		@Override
		public java.util.stream.Stream<DeviceModel> loadItems(
				QueryDefinition<DeviceModel, DeviceModelFilter> queryDefinition, int offset, int limit) {
			Page<DeviceModel> models = findPage(offset / limit, limit);
			return models.getContent().stream();
		}

		private Page<DeviceModel> findPage(int page, int size) {
			Sort sort = buildSort();
			Pageable pageable = PageRequest.of(page, size, sort);
			DeviceModelFilter filter = queryDefinition.getQueryFilter();
			String name = filter != null && filter.hasName() ? filter.getName().trim() : null;

			if (name != null) {
				return deviceModelRepository.findByNameStartingWithIgnoreCase(name, pageable);
			}
			return deviceModelRepository.findAll(pageable);
		}

		private Sort buildSort() {
			List<QuerySortOrder> sortOrders = queryDefinition.getSortOrders();
			if (sortOrders == null || sortOrders.isEmpty()) {
				return Sort.by(NAME).ascending();
			}

			List<Sort.Order> orders = new ArrayList<>();
			for (QuerySortOrder sortOrder : sortOrders) {
				String property = sortOrder.getSorted();
				if (!NAME.equals(property)) {
					continue;
				}
				Sort.Direction direction = sortOrder.getDirection() == SortDirection.ASCENDING
						? Sort.Direction.ASC
						: Sort.Direction.DESC;
				orders.add(new Sort.Order(direction, property));
			}
			if (orders.isEmpty()) {
				return Sort.by(NAME).ascending();
			}
			return Sort.by(orders);
		}
	}
}
