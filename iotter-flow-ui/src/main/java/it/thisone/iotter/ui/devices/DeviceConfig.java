package it.thisone.iotter.ui.devices;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import it.thisone.iotter.util.PopupNotification;
import com.vaadin.ui.Grid;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.rest.model.ConfigAttribute;
import it.thisone.iotter.rest.model.DeviceConfiguration;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.ifc.ITabContent;

public class DeviceConfig extends BaseComponent  implements ITabContent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int revision; 
	private Grid<ConfigAttribute> grid;
	private ListDataProvider<ConfigAttribute> dataProvider;
	private Device device;

	public DeviceConfig(final Device item) {
		super("device.config", "DeviceConfig");
		grid = new Grid<>();
		device = item;
		grid.setSizeFull();
		dataProvider = new ListDataProvider<>(new java.util.ArrayList<>());
		grid.setDataProvider(dataProvider);
		grid.addStyleName(UIUtils.TABLE_STYLE);
		buildColumns();
		MenuBar menu = createMenu(device);
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		// layout.setImmediate(true);
		layout.addComponent(menu);
		layout.addComponent(grid);
		layout.setExpandRatio(grid, 1f);
		setCompositionRoot(layout);
	}

	private void buildColumns() {
		grid.removeAllColumns();
		grid.addColumn(ConfigAttribute::getId).setId("id").setCaption(getI18nLabel("id"));
		grid.addColumn(ConfigAttribute::getPermission).setId("permission").setCaption(getI18nLabel("permission"));
		grid.addColumn(ConfigAttribute::getMin).setId("min").setCaption(getI18nLabel("min"));
		grid.addColumn(ConfigAttribute::getMax).setId("max").setCaption(getI18nLabel("max"));
		grid.addColumn(ConfigAttribute::getValue).setId("value").setCaption(getI18nLabel("value"));
		grid.addColumn(ConfigAttribute::getTopic).setId("topic").setCaption(getI18nLabel("topic"));
		grid.addColumn(ConfigAttribute::getUnit).setId("unit").setCaption(getI18nLabel("unit"));
		grid.addColumn(ConfigAttribute::getOid).setId("oid").setCaption(getI18nLabel("oid"));
		grid.addColumn(ConfigAttribute::getSection).setId("section").setCaption(getI18nLabel("section"));
		grid.addColumn(ConfigAttribute::getGroup).setId("group").setCaption(getI18nLabel("group"));
	}

	private MenuBar createMenu(final Device device) {
		MenuBar menu = new MenuBar();
		menu.setStyleName("borderless");
		
		MenuItem refresh = menu.addItem("", VaadinIcons.REFRESH, new Command() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8346541839033375990L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				dataProvider.getItems().clear();
				dataProvider.getItems().addAll(createItems(device));
				dataProvider.refreshAll();
				selectedItem.setText("Rev. " + revision);
				PopupNotification.show("refresh done");
			}
		});
		refresh.setText("Rev. " + revision);
		refresh.setDescription("refresh");


//		MenuItem delete = menu.addItem("", VaadinIcons.TRASH, new Command() {
//			/**
//			 * 
//			 */
//			private static final long serialVersionUID = 8346541839033375990L;
//
//			@Override
//			public void menuSelected(MenuItem selectedItem) {
//				UIUtils.getServiceFactory().getSubscriptionService()
//						.rollupDelete(device.getSerial());
//				PopupNotification.show("delete done");
//			}
//		});
//		delete.setDescription("delete");
		
		return menu;
	}

	private java.util.List<ConfigAttribute> createItems(Device device) {
		java.util.List<ConfigAttribute> items = new java.util.ArrayList<>();
		DeviceConfiguration cfg = UIUtils.getCassandraService().deviceConfiguration(device.getSerial());
		revision = cfg.getRevision();
		for (ConfigAttribute attr : cfg.getAttributes()) {
			items.add(attr);
		}
		return items;
	}

	@Override
	public void lazyLoad() {
		if (!isLoaded()) {
			this.refresh();
		}

	}

	@Override
	public boolean isLoaded() {
		return !dataProvider.getItems().isEmpty();
	}

	@Override
	public void refresh() {
		UI.getCurrent().access(new Runnable() {
			@Override
			public void run() {
				if (device != null) {
					dataProvider.getItems().clear();
					dataProvider.getItems().addAll(createItems(device));
					dataProvider.refreshAll();
				}
				UIUtils.push();
			}
		});

		
	}

}
