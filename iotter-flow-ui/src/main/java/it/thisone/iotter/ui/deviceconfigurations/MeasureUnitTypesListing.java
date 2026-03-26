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
import it.thisone.iotter.persistence.model.MeasureUnitType;
import it.thisone.iotter.persistence.repository.MeasureUnitTypeRepository;
import it.thisone.iotter.persistence.service.MeasureUnitTypeService;
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
public class MeasureUnitTypesListing extends AbstractBaseEntityListing<MeasureUnitType> {

	private static final long serialVersionUID = 1L;
	private static final String MEASURE_UNIT_VIEW = "measureunittype";

	private final Permissions permissions;

	@Autowired
	private MeasureUnitTypeRepository measureUnitTypeRepository;

	@Autowired
	private MeasureUnitTypeService measureUnitTypeService;

	private Grid<MeasureUnitType> grid;
	private LazyQueryDataProvider<MeasureUnitType, MeasureUnitTypeFilter> dataProvider;
	private MeasureUnitTypeQueryDefinition queryDefinition;
	private MeasureUnitTypeFilter currentFilter = new MeasureUnitTypeFilter();

	public MeasureUnitTypesListing() {
		this(new Permissions(true));
	}

	private MeasureUnitTypesListing(Permissions permissions) {
		super(MeasureUnitType.class, MEASURE_UNIT_VIEW, MEASURE_UNIT_VIEW, false);
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
		queryDefinition = new MeasureUnitTypeQueryDefinition(MeasureUnitType.class, DEFAULT_LIMIT);
		queryDefinition.setQueryFilter(currentFilter);
		dataProvider = new LazyQueryDataProvider<>(queryDefinition, new MeasureUnitTypeQueryFactory(measureUnitTypeRepository));
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
	public AbstractBaseEntityForm<MeasureUnitType> getEditor(MeasureUnitType item, boolean readOnly) {
		return new MeasureUnitTypeForm(item);
	}

	private Grid<MeasureUnitType> createGrid() {
		Grid<MeasureUnitType> grid = new Grid<>();
		grid.setDataProvider(dataProvider);
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.setSizeFull();
		grid.addClassName("smallgrid");

		List<Grid.Column<MeasureUnitType>> columns = new ArrayList<>();
		Grid.Column<MeasureUnitType> codeColumn = grid.addColumn(MeasureUnitType::getCode).setKey("code");
		columns.add(codeColumn);
		Grid.Column<MeasureUnitType> nameColumn = grid.addColumn(MeasureUnitType::getName).setKey("name");
		columns.add(nameColumn);

		// if (permissions.isViewAllMode()) {
		// 	Grid.Column<MeasureUnitType> ownerColumn = grid.addColumn(MeasureUnitType::getOwner).setKey("owner");
		// 	columns.add(ownerColumn);
		// }

		for (Grid.Column<MeasureUnitType> column : columns) {
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

	private VerticalLayout createContent(Grid<MeasureUnitType> grid) {
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
		button.addClickListener(event -> openEditor(new MeasureUnitType(), getI18nLabel("add_dialog")));
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
	protected void openRemove(MeasureUnitType item) {
		if (item == null) {
			return;
		}

		Callback callback = result -> {
			if (!result) {
				return;
			}

			measureUnitTypeService.deleteById(item.getId());
			refreshCurrentPage();
		};

		Dialog dialog = new ConfirmationDialog(getI18nLabel("remove_dialog"), getI18nLabel("remove_action"), callback);
		dialog.open();
	}

	private void openEditor(MeasureUnitType item, String label) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityForm<MeasureUnitType> editor = getEditor(item, false);
		Dialog dialog = BaseComponent.createDialog(label, editor);
		editor.setSavedHandler(entity -> {
			try {
				if (entity.isNew()) {
					measureUnitTypeService.create(entity);
				} else {
					measureUnitTypeService.update(entity);
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
	protected void openDetails(MeasureUnitType item) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityForm<MeasureUnitType> details = getEditor(item, true);
		SideDrawer dialog = (SideDrawer) BaseComponent.createDialog(getI18nLabel("view_dialog"), details);
		dialog.open();
	}

	private void refreshCurrentPage() {
		refreshData();
	}

	private static final class MeasureUnitTypeFilter {
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

	private static final class MeasureUnitTypeQueryDefinition extends LazyQueryDefinition<MeasureUnitType, MeasureUnitTypeFilter>
			implements FilterableQueryDefinition<MeasureUnitTypeFilter> {

		private static final long serialVersionUID = 1L;
		private MeasureUnitTypeFilter queryFilter;

		private MeasureUnitTypeQueryDefinition(Class<MeasureUnitType> beanClass, int batchSize) {
			super(beanClass, batchSize);
		}

		@Override
		public void setQueryFilter(MeasureUnitTypeFilter filter) {
			this.queryFilter = filter;
		}

		@Override
		public MeasureUnitTypeFilter getQueryFilter() {
			return queryFilter;
		}
	}

	private static final class MeasureUnitTypeQueryFactory implements QueryFactory<MeasureUnitType, MeasureUnitTypeFilter> {
		private final MeasureUnitTypeRepository measureUnitTypeRepository;

		private MeasureUnitTypeQueryFactory(MeasureUnitTypeRepository measureUnitTypeRepository) {
			this.measureUnitTypeRepository = measureUnitTypeRepository;
		}

		@Override
		public it.thisone.iotter.lazyquerydataprovider.Query<MeasureUnitType, MeasureUnitTypeFilter> constructQuery(
				QueryDefinition<MeasureUnitType, MeasureUnitTypeFilter> queryDefinition) {
			return new MeasureUnitTypeQuery(measureUnitTypeRepository,
					(MeasureUnitTypeQueryDefinition) queryDefinition);
		}
	}

	private static final class MeasureUnitTypeQuery
			implements it.thisone.iotter.lazyquerydataprovider.Query<MeasureUnitType, MeasureUnitTypeFilter> {
		private final MeasureUnitTypeRepository measureUnitTypeRepository;
		private final MeasureUnitTypeQueryDefinition queryDefinition;

		private MeasureUnitTypeQuery(MeasureUnitTypeRepository measureUnitTypeRepository,
				MeasureUnitTypeQueryDefinition queryDefinition) {
			this.measureUnitTypeRepository = measureUnitTypeRepository;
			this.queryDefinition = queryDefinition;
		}

		@Override
		public int size(QueryDefinition<MeasureUnitType, MeasureUnitTypeFilter> queryDefinition) {
			Page<MeasureUnitType> page = findPage(0, 1);
			return (int) page.getTotalElements();
		}

		@Override
		public java.util.stream.Stream<MeasureUnitType> loadItems(
				QueryDefinition<MeasureUnitType, MeasureUnitTypeFilter> queryDefinition, int offset, int limit) {
			Page<MeasureUnitType> page = findPage(offset / limit, limit);
			return page.getContent().stream();
		}

		private Page<MeasureUnitType> findPage(int page, int size) {
			Pageable pageable = PageRequest.of(page, size);
			MeasureUnitTypeFilter filter = queryDefinition.getQueryFilter();
			String searchText = filter != null && filter.hasSearchText() ? filter.getSearchText().trim() : null;
			if (searchText != null) {
				return measureUnitTypeRepository.findByNameStartingWithIgnoreCase(searchText, pageable);
			}
			return measureUnitTypeRepository.findAll(pageable);
		}
	}
}
