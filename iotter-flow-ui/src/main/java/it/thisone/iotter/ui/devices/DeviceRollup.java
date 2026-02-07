package it.thisone.iotter.ui.devices;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.dialog.Dialog;

import it.thisone.iotter.cassandra.model.MeasureStats;
import it.thisone.iotter.integration.CassandraService;
import it.thisone.iotter.integration.SubscriptionService;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.service.GroupWidgetService;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.common.EntitySelectedEvent;
import it.thisone.iotter.ui.common.EntitySelectedListener;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.ifc.ITabContent;
import it.thisone.iotter.util.PopupNotification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DeviceRollup extends BaseComponent implements ITabContent {
	// TODO(flow-migration): manual refactor required for Vaadin 8 APIs removed in Flow (dialogs/tabs/legacy layout or UIUtils context access).


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMNS = { "key", "label", "records", "firstMeasureDate",
			"lastMeasureDate", "frequency", "updated", "running", "since" };
	private Date lastContactDate;

	private Grid<MeasureStats> grid;
	private ListDataProvider<MeasureStats> dataProvider;
	private Device device;

	@Autowired
    private SubscriptionService subscriptionService;
	@Autowired
    private CassandraService cassandraService;

	public DeviceRollup(Device item) {
		super("device.rollup", "DeviceRollup");
		device = item;
		grid = createGrid();
		grid.setSizeFull();

		Checkbox checkbox = new Checkbox("rollup break");
		checkbox.setValue(true);

		MenuBar menu = new MenuBar();
		menu.setClassName("borderless");
		MenuItem refresh = menu.addItem(new Icon(VaadinIcon.REFRESH), e -> {
			refreshData();
			PopupNotification.show("refresh done");
		});
		//refresh.setDescription("refresh");

		MenuItem rollup = menu.addItem(new Icon(VaadinIcon.QUESTION), e -> {
			subscriptionService
					.rollupDevice(device.getSerial(), checkbox.getValue());
			PopupNotification.show("rollup started");
		});
		//rollup.setDescription("rollup");

		MenuItem delete = menu.addItem(new Icon(VaadinIcon.TRASH), e -> {
			subscriptionService
					.resetRollup(device.getSerial());
			PopupNotification.show("delete done");
		});
		//delete.setDescription("delete");

		// MenuItem chart = menu.addItem(new Icon(VaadinIcon.LINE_CHART), e -> {
		// 	DeviceRollupActivity details = new DeviceRollupActivity(device);
		// 	final Window dialog = createDialog(device.getSerial(), details, details.getWindowDimension(),
		// 			details.getWindowStyle());
		// 	details.addListener(new EntitySelectedListener() {
		// 		/**
		// 		 * 
		// 		 */
		// 		private static final long serialVersionUID = 1L;
		// 		@Override
		// 		public void entitySelected(EntitySelectedEvent<?> event) {
		// 			dialog.close();
		// 		}
		// 	});

		// 	UI.getCurrent().addWindow(dialog);
		// });
		//chart.setDescription("chart");

		HorizontalLayout menuLayout = new HorizontalLayout();
		//menuLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		menuLayout.add(menu);
		menuLayout.add(checkbox);
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.add(menuLayout);
		layout.add(grid);
		//layout.setExpandRatio(grid, 1f);

		setRootComposition(layout);
	}

	private Grid<MeasureStats> createGrid() {
		dataProvider = new ListDataProvider<>(new java.util.ArrayList<MeasureStats>());
		Grid<MeasureStats> table = new Grid<>();
		table.setDataProvider(dataProvider);
		for (String columnId : COLUMNS) {
			Grid.Column<MeasureStats> column;
			switch (columnId) {
			case "key":
				column = table.addColumn(MeasureStats::getKey);
				column.setKey(columnId);
				break;
			case "label":
				column = table.addColumn(MeasureStats::getLabel);
				column.setKey(columnId);
				break;
			case "records":
				column = table.addColumn(MeasureStats::getRecords);
				column.setKey(columnId);
				break;
			case "firstMeasureDate":
				column = table.addColumn(MeasureStats::getFirstMeasureDate);
				column.setKey(columnId);
				break;
			case "lastMeasureDate":
				column = table.addColumn(MeasureStats::getLastMeasureDate);
				column.setKey(columnId);
				break;
			case "frequency":
				column = table.addColumn(MeasureStats::getFrequency);
				column.setKey(columnId);
				break;
			case "updated":
				column = table.addColumn(MeasureStats::getUpdated);
				column.setKey(columnId);
				break;
			case "running":
				column = table.addColumn(MeasureStats::isRunning);
				column.setKey(columnId);
				break;
			case "since":
				column = table.addColumn(MeasureStats::getSince);
				column.setKey(columnId);
				break;
			default:
				continue;
			}
			column.setHeader(getI18nLabel(columnId));
		}
		table.setSelectionMode(Grid.SelectionMode.NONE);
		//table.setClassName(UIUtils.TABLE_STYLE);
		return table;
	}

	private void refreshData() {
		if (device == null) {
			return;
		}
		List<MeasureStats> stats = createContainer(device);
		dataProvider.getItems().clear();
		dataProvider.getItems().addAll(stats);
		dataProvider.refreshAll();
	}

	private List<MeasureStats> createContainer(Device device) {
		Set<String> keys = new LinkedHashSet<>(device.feedKeys());
		String sn = device.getSerial();
		List<MeasureStats> stats = cassandraService.getRollup()
				.getRollupStats(sn);
		for (MeasureStats stat : stats) {
			keys.remove(stat.getKey());
			Date dt = stat.getLastMeasureDate();
			if (lastContactDate == null) {
				lastContactDate = dt;
			} else {
				if (dt != null && dt.after(lastContactDate)) {
					lastContactDate = dt;
				}
			}
		}
		for (String key : keys) {
			stats.add(new MeasureStats(sn, key));
		}
		return stats;
	}

	public Date getLastContactDate() {
		return lastContactDate;
	}

	@Override
	public void lazyLoad() {
		// if (!isLoaded()) {
		// 	UI.getCurrent().access(new Runnable() {
		// 		@Override
		// 		public void run() {
		// 			refreshData();
		// 			UIUtils.push();
		// 		}
		// 	});
		// }

	}

	@Override
	public boolean isLoaded() {
		return dataProvider != null && !dataProvider.getItems().isEmpty();
	}

	@Override
	public void refresh() {

	}

}
