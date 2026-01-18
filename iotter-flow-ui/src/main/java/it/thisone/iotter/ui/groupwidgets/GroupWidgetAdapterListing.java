package it.thisone.iotter.ui.groupwidgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.menubar.MenuItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;

import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.ui.ifc.IGroupWidgetListingField;
import it.thisone.iotter.ui.model.GroupWidgetAdapter;
import it.thisone.iotter.ui.model.GroupWidgetContainer;

// Feature #1884
public class GroupWidgetAdapterListing extends Composite<VerticalLayout> implements IGroupWidgetListingField {

	private static final long serialVersionUID = 2001077544797472399L;

	private static final String VISUALIZATION_KEY = "visualization";
	private static final String NETWORK_KEY = "network";
	private static final String SELECTED_KEY = "selected";

	private Grid<GroupWidgetAdapter> grid;
	private ListDataProvider<GroupWidgetAdapter> dataProvider;
	private TextField visualizationFilterField;
	private boolean selectedOnly;
	private String visualizationFilterText = "";

	private Collection<GroupWidget> items;

	public String getI18nLabel(String key) {
		return getTranslation("user.editor" + "." + key);
	}

	public GroupWidgetAdapterListing(Collection<GroupWidget> collection, Set<NetworkGroup> groups) {
		super();

		grid = new Grid<>();
		grid.addClassName("smallgrid");
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.setSizeFull();
		grid.setHeight("400px");

		GroupWidgetContainer container = new GroupWidgetContainer();
		container.addItems(collection, groups);
		dataProvider = container.asDataProvider();
		grid.setDataProvider(dataProvider);

		grid.removeAllColumns();

		Column<GroupWidgetAdapter> selectedColumn = grid.addComponentColumn(adapter -> {
			com.vaadin.flow.component.checkbox.Checkbox checkBox = new com.vaadin.flow.component.checkbox.Checkbox();
			checkBox.setValue(adapter.isSelected());
			checkBox.addValueChangeListener(event -> {
				adapter.setSelected(event.getValue());
				grid.getDataProvider().refreshItem(adapter);
			});
			return checkBox;
		}).setKey(SELECTED_KEY).setHeader("");
		selectedColumn.setTextAlign(ColumnTextAlign.CENTER);

		grid.addColumn(GroupWidgetAdapter::getVisualization)
			.setKey(VISUALIZATION_KEY)
			.setHeader(getI18nLabel("visualization"))
			.setFlexGrow(1);
		grid.addColumn(GroupWidgetAdapter::getNetwork)
			.setKey(NETWORK_KEY)
			.setHeader(getI18nLabel("network"))
			.setFlexGrow(1);

		initFilters();

		getContent().setSizeFull();
		getContent().add(grid);
	}

	private void initFilters() {
		HeaderRow filterRow = grid.appendHeaderRow();

		visualizationFilterField = new TextField();
		visualizationFilterField.setPlaceholder("Filter...");
		visualizationFilterField.setWidthFull();
		visualizationFilterField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
		visualizationFilterField.setValueChangeMode(ValueChangeMode.LAZY);
		visualizationFilterField.addValueChangeListener(event -> {
			visualizationFilterText = event.getValue() == null ? "" : event.getValue().trim();
			applyFilters();
		});

		filterRow.getCell(grid.getColumnByKey(VISUALIZATION_KEY)).setComponent(visualizationFilterField);
		filterRow.getCell(grid.getColumnByKey(SELECTED_KEY)).setComponent(filterSelectedMenuBar());
	}

	private MenuBar filterSelectedMenuBar() {
		MenuBar root = new MenuBar();
		root.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
		MenuItem select = root.addItem(VaadinIcon.EYE_SLASH.create());

		select.getSubMenu().addItem(getI18nLabel("visualizations.select_all"), event -> {
			clearFilters();
			select.setIcon(VaadinIcon.CHECK.create());
			for (GroupWidgetAdapter item : dataProvider.getItems()) {
				item.setSelected(true);
			}
			dataProvider.refreshAll();
			grid.sort(Collections.emptyList());
		});
		select.getSubMenu().addItem(getI18nLabel("visualizations.select_none"), event -> {
			clearFilters();
			select.setIcon(VaadinIcon.CLOSE_SMALL.create());
			for (GroupWidgetAdapter item : dataProvider.getItems()) {
				item.setSelected(false);
			}
			dataProvider.refreshAll();
			grid.sort(Collections.emptyList());
		});
		select.getSubMenu().addItem(getI18nLabel("visualizations.show_selected"), event -> {
			selectedOnly = true;
			select.setIcon(VaadinIcon.EYE.create());
			applyFilters();
		});
		select.getSubMenu().addItem(getI18nLabel("visualizations.show_all"), event -> {
			selectedOnly = false;
			select.setIcon(VaadinIcon.EYE_SLASH.create());
			applyFilters();
		});
		return root;
	}

	private void applyFilters() {
		dataProvider.clearFilters();
		dataProvider.setFilter(adapter -> {
			String visualization = adapter.getVisualization() == null ? "" : adapter.getVisualization();
			boolean matchesText = visualizationFilterText == null || visualizationFilterText.isEmpty()
				|| visualization.toLowerCase().contains(visualizationFilterText.toLowerCase());
			boolean matchesSelection = !selectedOnly || adapter.isSelected();
			return matchesText && matchesSelection;
		});
	}

	private void clearFilters() {
		visualizationFilterText = "";
		selectedOnly = false;
		if (visualizationFilterField != null) {
			visualizationFilterField.clear();
		}
		dataProvider.clearFilters();
	}

	public Collection<NetworkGroup> getSelectedGroups() {
		Collection<NetworkGroup> groups = new ArrayList<>();
		for (GroupWidgetAdapter adapter : dataProvider.getItems()) {
			if (adapter.isSelected() && adapter.getItem().getGroup() != null) {
				groups.add(adapter.getItem().getGroup());
			}
		}
		return groups;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Collection<GroupWidget>> getType() {
		try {
			return (Class<? extends Collection<GroupWidget>>) Class.forName("java.util.Collection");
		} catch (ClassNotFoundException e) {
		}
		return null;
	}

	@Override
	public void setValue(Collection<GroupWidget> collection) {
		items = collection;
	}

	@Override
	public Collection<GroupWidget> getValue() {
		return items;
	}
}
