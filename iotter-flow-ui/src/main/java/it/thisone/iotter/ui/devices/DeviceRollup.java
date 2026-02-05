package it.thisone.iotter.ui.devices;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import it.thisone.iotter.cassandra.model.MeasureStats;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.common.EntitySelectedEvent;
import it.thisone.iotter.ui.common.EntitySelectedListener;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.ifc.ITabContent;
import it.thisone.iotter.util.PopupNotification;

public class DeviceRollup extends BaseComponent implements ITabContent {

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


	public DeviceRollup(Device item) {
		super("device.rollup", "DeviceRollup");
		device = item;
		grid = createGrid();
		grid.setSizeFull();

		CheckBox checkbox = new CheckBox("rollup break");
		checkbox.setValue(true);

		MenuBar menu = new MenuBar();
		menu.setStyleName("borderless");
		MenuItem refresh = menu.addItem("", VaadinIcons.REFRESH, new Command() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8346541839033375990L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				refreshData();
				PopupNotification.show("refresh done");
			}
		});
		refresh.setDescription("refresh");

		MenuItem rollup = menu.addItem("", VaadinIcons.QUESTION, new Command() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8346541839033375990L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				UIUtils.getServiceFactory().getSubscriptionService()
						.rollupDevice(device.getSerial(), checkbox.getValue());
				PopupNotification.show("rollup started");
			}
		});
		rollup.setDescription("rollup");

		MenuItem delete = menu.addItem("", VaadinIcons.TRASH, new Command() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8346541839033375990L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				UIUtils.getServiceFactory().getSubscriptionService()
						.resetRollup(device.getSerial());
				PopupNotification.show("delete done");
			}
		});
		delete.setDescription("delete");

		MenuItem chart = menu.addItem("", VaadinIcons.LINE_CHART, new Command() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8346541839033375990L;
			@Override
			public void menuSelected(MenuItem selectedItem) {
				DeviceRollupActivity details = new DeviceRollupActivity(device);
				final Window dialog = createDialog(device.getSerial(), details, details.getWindowDimension(),
						details.getWindowStyle());

				details.addListener(new EntitySelectedListener() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;
					@Override
					public void entitySelected(EntitySelectedEvent<?> event) {
						dialog.close();
					}
				});

				UI.getCurrent().addWindow(dialog);
			}
		});
		chart.setDescription("chart");

		HorizontalLayout menuLayout = new HorizontalLayout();
		menuLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		menuLayout.addComponent(menu);
		menuLayout.addComponent(checkbox);
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.addComponent(menuLayout);
		layout.addComponent(grid);
		layout.setExpandRatio(grid, 1f);

		setCompositionRoot(layout);
	}

	private Grid<MeasureStats> createGrid() {
		dataProvider = new ListDataProvider<>(new java.util.ArrayList<MeasureStats>());
		Grid<MeasureStats> table = new Grid<>(dataProvider);
		for (String columnId : COLUMNS) {
			Grid.Column<MeasureStats, ?> column;
			switch (columnId) {
			case "key":
				column = table.addColumn(MeasureStats::getKey).setId(columnId);
				break;
			case "label":
				column = table.addColumn(MeasureStats::getLabel).setId(columnId);
				break;
			case "records":
				column = table.addColumn(MeasureStats::getRecords).setId(columnId);
				break;
			case "firstMeasureDate":
				column = table.addColumn(MeasureStats::getFirstMeasureDate).setId(columnId);
				break;
			case "lastMeasureDate":
				column = table.addColumn(MeasureStats::getLastMeasureDate).setId(columnId);
				break;
			case "frequency":
				column = table.addColumn(MeasureStats::getFrequency).setId(columnId);
				break;
			case "updated":
				column = table.addColumn(MeasureStats::getUpdated).setId(columnId);
				break;
			case "running":
				column = table.addColumn(MeasureStats::isRunning).setId(columnId);
				break;
			case "since":
				column = table.addColumn(MeasureStats::getSince).setId(columnId);
				break;
			default:
				continue;
			}
			column.setCaption(getI18nLabel(columnId));
		}
		table.setSelectionMode(Grid.SelectionMode.NONE);
		table.setStyleName(UIUtils.TABLE_STYLE);
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
		List<MeasureStats> stats = UIUtils.getCassandraService().getRollup()
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
		if (!isLoaded()) {
			UI.getCurrent().access(new Runnable() {
				@Override
				public void run() {
					refreshData();
					UIUtils.push();
				}
			});
		}

	}

	@Override
	public boolean isLoaded() {
		return dataProvider != null && !dataProvider.getItems().isEmpty();
	}

	@Override
	public void refresh() {

	}

}
