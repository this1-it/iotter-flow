package it.thisone.iotter.ui.deviceconfigurations;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.lazyquerydataprovider.FilterableQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDataProvider;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryDefinition;
import it.thisone.iotter.lazyquerydataprovider.QueryFactory;
import it.thisone.iotter.persistence.model.MeasureSensorType;
import it.thisone.iotter.persistence.repository.MeasureSensorTypeRepository;
import it.thisone.iotter.persistence.service.MeasureSensorTypeService;
import it.thisone.iotter.security.Permissions;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.AbstractBaseEntityListing;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.common.ConfirmationDialog;
import it.thisone.iotter.ui.common.ConfirmationDialog.Callback;
import it.thisone.iotter.ui.common.SideDrawer;
import it.thisone.iotter.util.PopupNotification;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MeasureSensorTypesListing extends AbstractBaseEntityListing<MeasureSensorType> {

	private static final long serialVersionUID = 1L;
	private static final String MEASURE_SENSOR_VIEW = "measuresensortype";

	private final Permissions permissions;

	@Autowired
	private MeasureSensorTypeRepository measureSensorTypeRepository;

	@Autowired
	private MeasureSensorTypeService measureSensorTypeService;

	private Grid<MeasureSensorType> grid;
	private LazyQueryDataProvider<MeasureSensorType, MeasureSensorTypeFilter> dataProvider;
	private MeasureSensorTypeQueryDefinition queryDefinition;
	private MeasureSensorTypeFilter currentFilter = new MeasureSensorTypeFilter();

	public MeasureSensorTypesListing() {
		this(new Permissions(true));
	}

	private MeasureSensorTypesListing(Permissions permissions) {
		super(MeasureSensorType.class, MEASURE_SENSOR_VIEW, MEASURE_SENSOR_VIEW, false);
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
		queryDefinition = new MeasureSensorTypeQueryDefinition(MeasureSensorType.class, DEFAULT_LIMIT);
		queryDefinition.setQueryFilter(currentFilter);
		dataProvider = new LazyQueryDataProvider<>(queryDefinition, new MeasureSensorTypeQueryFactory(measureSensorTypeRepository));
		dataProvider.setCacheQueries(false);
		dataProvider.setFilter(currentFilter);
		setBackendDataProvider(dataProvider);

		grid = createGrid();
		VerticalLayout content = createContent(grid);
		setSelectable(grid);

		HorizontalLayout toolbar = buildSearchToolbar(createAddButton());

		getMainLayout().add(toolbar, content);
		getMainLayout().setFlexGrow(1f, content);
		enableButtons(null);
	}

	@Override
	public AbstractBaseEntityForm<MeasureSensorType> getEditor(MeasureSensorType item, boolean readOnly) {
		return new MeasureSensorTypeForm(item);
	}

	private Grid<MeasureSensorType> createGrid() {
		Grid<MeasureSensorType> grid = new Grid<>();
		grid.setDataProvider(dataProvider);
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.setSizeFull();
		grid.addClassName("smallgrid");

		List<Grid.Column<MeasureSensorType>> columns = new ArrayList<>();
		Grid.Column<MeasureSensorType> codeColumn = grid.addColumn(MeasureSensorType::getCode).setKey("code");
		columns.add(codeColumn);
		Grid.Column<MeasureSensorType> nameColumn = grid.addColumn(MeasureSensorType::getName).setKey("name");
		columns.add(nameColumn);

		// if (permissions.isViewAllMode()) {
		// 	Grid.Column<MeasureSensorType> ownerColumn = grid.addColumn(MeasureSensorType::getOwner).setKey("owner");
		// 	columns.add(ownerColumn);
		// }

		for (Grid.Column<MeasureSensorType> column : columns) {
			column.setSortable(false);
			column.setHeader(getI18nLabel(column.getKey()));
		}

		grid.setColumnOrder(columns.toArray(new Grid.Column[0]));
		grid.addComponentColumn(item -> {
			MenuBar menuBar = new MenuBar();
			menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
			MenuItem menuItem = menuBar.addItem("•••");
			menuItem.getElement().setAttribute("aria-label", "More options");
			SubMenu subMenu = menuItem.getSubMenu();
			if (permissions.isModifyMode()) {
				subMenu.addItem(getI18nLabel("modify_action"),
						event -> openEditor(item, getI18nLabel("modify_dialog")));
			}
			if (permissions.isRemoveMode()) {
				subMenu.addItem(getI18nLabel("remove_action"), event -> openRemove(item));
			}
			return menuBar;
		}).setWidth("70px").setFlexGrow(0).setKey("actions");
		return grid;
	}

	private VerticalLayout createContent(Grid<MeasureSensorType> grid) {
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
		Button button = new Button(getI18nLabel("add"), VaadinIcon.PLUS.create());
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		button.setId("add" + getId() + ALWAYS_ENABLED_BUTTON);
		button.addClickListener(event -> openEditor(new MeasureSensorType(), getI18nLabel("add_dialog")));
		button.setVisible(permissions.isCreateMode());
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
		refreshData();
	}

	@Override
	protected void openRemove(MeasureSensorType item) {
		if (item == null) {
			return;
		}
		Callback callback = result -> {
			if (!result) {
				return;
			}

			measureSensorTypeService.deleteById(item.getId());
			refreshCurrentPage();
		};

		Dialog dialog = new ConfirmationDialog(getI18nLabel("remove_dialog"), getI18nLabel("remove_action"), callback);
		dialog.open();
	}

	private void openEditor(MeasureSensorType item, String label) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityForm<MeasureSensorType> editor = getEditor(item, false);
		Dialog dialog = BaseComponent.createDialog(label, editor);
		editor.setSavedHandler(entity -> {
			try {
				if (entity.isNew()) {
					measureSensorTypeService.create(entity);
				} else {
					measureSensorTypeService.update(entity);
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
	protected void openDetails(MeasureSensorType item) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityForm<MeasureSensorType> details = getEditor(item, true);
		SideDrawer dialog = (SideDrawer) BaseComponent.createDialog(getI18nLabel("view_dialog"), details);
		dialog.open();
	}

	private void refreshCurrentPage() {
		refreshData();
	}

	private static final class MeasureSensorTypeFilter {
		private String searchText;

		public String getSearchText() {
			return searchText;
		}

		public void setSearchText(String searchText) {
			this.searchText = searchText != null && searchText.trim().isEmpty() ? null : searchText;
		}

		public boolean hasSearchText() {
			return searchText != null && !searchText.trim().isEmpty();
		}
	}

	private static final class MeasureSensorTypeQueryDefinition extends LazyQueryDefinition<MeasureSensorType, MeasureSensorTypeFilter>
			implements FilterableQueryDefinition<MeasureSensorTypeFilter> {

		private static final long serialVersionUID = 1L;
		private MeasureSensorTypeFilter queryFilter;

		private MeasureSensorTypeQueryDefinition(Class<MeasureSensorType> beanClass, int batchSize) {
			super(beanClass, batchSize);
		}

		@Override
		public void setQueryFilter(MeasureSensorTypeFilter filter) {
			this.queryFilter = filter;
		}

		@Override
		public MeasureSensorTypeFilter getQueryFilter() {
			return queryFilter;
		}
	}

	private static final class MeasureSensorTypeQueryFactory implements QueryFactory<MeasureSensorType, MeasureSensorTypeFilter> {
		private final MeasureSensorTypeRepository measureSensorTypeRepository;

		private MeasureSensorTypeQueryFactory(MeasureSensorTypeRepository measureSensorTypeRepository) {
			this.measureSensorTypeRepository = measureSensorTypeRepository;
		}

		@Override
		public it.thisone.iotter.lazyquerydataprovider.Query<MeasureSensorType, MeasureSensorTypeFilter> constructQuery(
				QueryDefinition<MeasureSensorType, MeasureSensorTypeFilter> queryDefinition) {
			return new MeasureSensorTypeQuery(measureSensorTypeRepository,
					(MeasureSensorTypeQueryDefinition) queryDefinition);
		}
	}

	private static final class MeasureSensorTypeQuery
			implements it.thisone.iotter.lazyquerydataprovider.Query<MeasureSensorType, MeasureSensorTypeFilter> {
		private final MeasureSensorTypeRepository measureSensorTypeRepository;
		private final MeasureSensorTypeQueryDefinition queryDefinition;

		private MeasureSensorTypeQuery(MeasureSensorTypeRepository measureSensorTypeRepository,
				MeasureSensorTypeQueryDefinition queryDefinition) {
			this.measureSensorTypeRepository = measureSensorTypeRepository;
			this.queryDefinition = queryDefinition;
		}

		@Override
		public int size(QueryDefinition<MeasureSensorType, MeasureSensorTypeFilter> queryDefinition) {
			Page<MeasureSensorType> page = findPage(0, 1);
			return (int) page.getTotalElements();
		}

		@Override
		public java.util.stream.Stream<MeasureSensorType> loadItems(
				QueryDefinition<MeasureSensorType, MeasureSensorTypeFilter> queryDefinition, int offset, int limit) {
			Page<MeasureSensorType> page = findPage(offset / limit, limit);
			return page.getContent().stream();
		}

		private Page<MeasureSensorType> findPage(int page, int size) {
			Pageable pageable = PageRequest.of(page, size);
			MeasureSensorTypeFilter filter = queryDefinition.getQueryFilter();
			String searchText = filter != null && filter.hasSearchText() ? filter.getSearchText().trim() : null;
			if (searchText != null) {
				return measureSensorTypeRepository.findByNameStartingWithIgnoreCase(searchText, pageable);
			}
			return measureSensorTypeRepository.findAll(pageable);
		}
	}
}
