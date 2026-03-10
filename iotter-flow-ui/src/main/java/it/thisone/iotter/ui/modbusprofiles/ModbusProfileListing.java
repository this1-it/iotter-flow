package it.thisone.iotter.ui.modbusprofiles;

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
import it.thisone.iotter.persistence.model.ModbusProfile;
import it.thisone.iotter.persistence.repository.ModbusProfileRepository;
import it.thisone.iotter.persistence.service.ModbusProfileService;
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
public class ModbusProfileListing extends AbstractBaseEntityListing<ModbusProfile> {

	private static final long serialVersionUID = 1L;
	private static final String DISPLAY_NAME = "displayName";
	private static final String MODBUS_PROFILE_VIEW = "modbus_profile.view";

	private final Permissions permissions;

	@Autowired
	private ModbusProfileRepository modbusProfileRepository;
	@Autowired
	private ModbusProfileService modbusProfileService;

	private Grid<ModbusProfile> grid;
	private LazyQueryDataProvider<ModbusProfile, ModbusProfileFilter> dataProvider;
	private ModbusProfileQueryDefinition queryDefinition;
	private ModbusProfileFilter currentFilter = new ModbusProfileFilter();

	public ModbusProfileListing() {
		this(new Permissions(true));
	}

	private ModbusProfileListing(Permissions permissions) {
		super(ModbusProfile.class, MODBUS_PROFILE_VIEW, MODBUS_PROFILE_VIEW, false);
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

		queryDefinition = new ModbusProfileQueryDefinition(ModbusProfile.class, DEFAULT_LIMIT, permissions);
		queryDefinition.setQueryFilter(currentFilter);
		dataProvider = new LazyQueryDataProvider<>(queryDefinition, new ModbusProfileQueryFactory(modbusProfileRepository));
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
	protected AbstractBaseEntityForm<ModbusProfile> getEditor(ModbusProfile item, boolean readOnly) {
		return new ModbusProfileForm(item);
	}

	private Grid<ModbusProfile> createGrid() {
		Grid<ModbusProfile> grid = new Grid<>();
		grid.setDataProvider(dataProvider);
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.addItemClickListener(event -> grid.select(event.getItem()));
		grid.setSizeFull();
		grid.addClassName("smallgrid");

		List<Grid.Column<ModbusProfile>> columns = new ArrayList<>();
		columns.add(grid.addColumn(ModbusProfile::getDisplayName).setKey(DISPLAY_NAME));
		columns.add(grid.addColumn(ModbusProfile::getRevision).setKey("revision"));
		columns.add(grid.addColumn(ModbusProfile::getTemplate).setKey("template"));
		columns.add(grid.addColumn(ModbusProfile::getState).setKey("state"));
		columns.add(grid.addColumn(ModbusProfile::getResource).setKey("resource"));

		for (Grid.Column<ModbusProfile> column : columns) {
			String columnId = column.getKey();
			column.setSortable(DISPLAY_NAME.equals(columnId));
			column.setHeader(getI18nLabel(columnId));
		}

		grid.setColumnOrder(columns.toArray(new Grid.Column[0]));
		initFilters(grid);
		return grid;
	}

	private void initFilters(Grid<ModbusProfile> grid) {
		HeaderRow filterRow = grid.appendHeaderRow();
		TextField name = new TextField();
		name.setPlaceholder("Filter...");
		name.setWidthFull();
		name.setValueChangeMode(ValueChangeMode.LAZY);
		filterRow.getCell(grid.getColumnByKey(DISPLAY_NAME)).setComponent(name);
		name.addValueChangeListener(event -> {
			currentFilter.setName(event.getValue());
			queryDefinition.setQueryFilter(currentFilter);
			setFilter(currentFilter);
			refreshData();
		});
	}

	private VerticalLayout createContent(Grid<ModbusProfile> grid) {
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
		button.addClickListener(event -> openEditor(new ModbusProfile(), getI18nLabel("add_dialog")));
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
	protected void openRemove(ModbusProfile item) {
		if (item == null) {
			return;
		}
		Callback callback = result -> {
			if (!result) {
				return;
			}

			modbusProfileService.deleteById(item.getId());
			refreshCurrentPage();
		};

		Dialog dialog = new ConfirmationDialog(getI18nLabel("remove_dialog"), getI18nLabel("remove_action"), callback);
		dialog.open();
	}

	private void openEditor(ModbusProfile item, String label) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityForm<ModbusProfile> editor = getEditor(item, false);
		Dialog dialog = BaseComponent.createDialog(label, editor);
		dialog.addThemeName("side-drawer-fullscreen");
		editor.setSavedHandler(entity -> {
			try {
				if (entity.isNew()) {
					modbusProfileService.create(entity);
				} else {
					modbusProfileService.update(entity);
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
	protected void openDetails(ModbusProfile item) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityForm<ModbusProfile> details = getEditor(item, true);
		SideDrawer dialog = (SideDrawer) BaseComponent.createDialog(getI18nLabel("view_dialog"), details);
		dialog.addThemeName("side-drawer-fullscreen");
		dialog.open();
	}

	private void refreshCurrentPage() {
		refreshData();
	}

	private static final class ModbusProfileFilter {
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
			return "ModbusProfileFilter{name=" + name + "}";
		}
	}

	private static final class ModbusProfileQueryDefinition extends LazyQueryDefinition<ModbusProfile, ModbusProfileFilter>
			implements FilterableQueryDefinition<ModbusProfileFilter> {

		private static final long serialVersionUID = 1L;
		private ModbusProfileFilter queryFilter;
		private final Permissions permissions;

		private ModbusProfileQueryDefinition(Class<ModbusProfile> beanClass, int batchSize, Permissions permissions) {
			super(beanClass, batchSize);
			this.permissions = permissions;
		}

		@Override
		public void setQueryFilter(ModbusProfileFilter filter) {
			this.queryFilter = filter;
		}

		@Override
		public ModbusProfileFilter getQueryFilter() {
			return queryFilter;
		}

		public Permissions getPermissions() {
			return permissions;
		}
	}

	private static final class ModbusProfileQueryFactory implements QueryFactory<ModbusProfile, ModbusProfileFilter> {
		private final ModbusProfileRepository modbusProfileRepository;

		private ModbusProfileQueryFactory(ModbusProfileRepository modbusProfileRepository) {
			this.modbusProfileRepository = modbusProfileRepository;
		}

		@Override
		public it.thisone.iotter.lazyquerydataprovider.Query<ModbusProfile, ModbusProfileFilter> constructQuery(
				QueryDefinition<ModbusProfile, ModbusProfileFilter> queryDefinition) {
			return new ModbusProfileQuery(modbusProfileRepository, (ModbusProfileQueryDefinition) queryDefinition);
		}
	}

	private static final class ModbusProfileQuery
			implements it.thisone.iotter.lazyquerydataprovider.Query<ModbusProfile, ModbusProfileFilter> {

		private final ModbusProfileRepository modbusProfileRepository;
		private final ModbusProfileQueryDefinition queryDefinition;

		private ModbusProfileQuery(ModbusProfileRepository modbusProfileRepository,
				ModbusProfileQueryDefinition queryDefinition) {
			this.modbusProfileRepository = modbusProfileRepository;
			this.queryDefinition = queryDefinition;
		}

		@Override
		public int size(QueryDefinition<ModbusProfile, ModbusProfileFilter> queryDefinition) {
			Page<ModbusProfile> page = findPage(0, 1);
			return (int) page.getTotalElements();
		}

		@Override
		public java.util.stream.Stream<ModbusProfile> loadItems(
				QueryDefinition<ModbusProfile, ModbusProfileFilter> queryDefinition, int offset, int limit) {
			Page<ModbusProfile> profiles = findPage(offset / limit, limit);
			return profiles.getContent().stream();
		}

		private Page<ModbusProfile> findPage(int page, int size) {
			Sort sort = buildSort();
			Pageable pageable = PageRequest.of(page, size, sort);
			ModbusProfileFilter filter = queryDefinition.getQueryFilter();
			String name = filter != null && filter.hasName() ? filter.getName().trim() : null;

			if (name != null) {
				return modbusProfileRepository.findByDisplayNameStartingWithIgnoreCaseAndResourceIsNotNull(name, pageable);
			}
			return modbusProfileRepository.findByResourceIsNotNull(pageable);
		}

		private Sort buildSort() {
			List<QuerySortOrder> sortOrders = queryDefinition.getSortOrders();
			if (sortOrders == null || sortOrders.isEmpty()) {
				return Sort.by(DISPLAY_NAME).ascending();
			}

			List<Sort.Order> orders = new ArrayList<>();
			for (QuerySortOrder sortOrder : sortOrders) {
				String property = sortOrder.getSorted();
				if (!DISPLAY_NAME.equals(property)) {
					continue;
				}
				Sort.Direction direction = sortOrder.getDirection() == SortDirection.ASCENDING
						? Sort.Direction.ASC
						: Sort.Direction.DESC;
				orders.add(new Sort.Order(direction, property));
			}
			if (orders.isEmpty()) {
				return Sort.by(DISPLAY_NAME).ascending();
			}
			return Sort.by(orders);
		}
	}
}
