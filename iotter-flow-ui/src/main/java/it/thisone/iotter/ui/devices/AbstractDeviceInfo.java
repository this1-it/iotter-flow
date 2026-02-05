package it.thisone.iotter.ui.devices;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.themes.ValoTheme;

import it.thisone.iotter.cassandra.model.FeedAlarmEvent;
import it.thisone.iotter.cassandra.model.IFeedAlarm;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.common.ItemSelectedEvent;
import it.thisone.iotter.ui.common.ItemSelectedListener;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.ifc.IDeviceInfo;
import it.thisone.iotter.ui.ifc.ITabContent;
import it.thisone.iotter.ui.main.IMainUI;
import it.thisone.iotter.ui.model.ChannelAdapter;
import it.thisone.iotter.ui.model.ChannelAdapterDataProvider;

public abstract class AbstractDeviceInfo extends BaseComponent implements IDeviceInfo {
	private static final long serialVersionUID = 1L;
	private Accordion multicomponent;
	protected Device device;
	protected ChannelAdapterDataProvider container;
	protected Collection<GroupWidget> widgets;

	public AbstractDeviceInfo() {
		super(UIUtils.MAPS_DEVICES_GOOGLE, UUID.randomUUID().toString());
		multicomponent = new Accordion();
		multicomponent.addStyleName(ValoTheme.TABSHEET_FRAMED);
		multicomponent.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
		multicomponent.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			/**
					 * 
					 */
			private static final long serialVersionUID = 1L;

			public void selectedTabChange(SelectedTabChangeEvent event) {
				TabSheet tabsheet = event.getTabSheet();
				if (tabsheet.getSelectedTab() instanceof ITabContent) {
					((ITabContent) tabsheet.getSelectedTab()).lazyLoad();
				}
			}
		});

		setCompositionRoot(multicomponent);
	}

	public void setContent(Device device, Collection<GroupWidget> widgets) {
		this.device = device;
		this.widgets = filteredWidgetsByUser(widgets);
		buildContent();
	}

	@Override
	public void addListener(ItemSelectedListener listener) {
		try {
			Method method = ItemSelectedListener.class.getDeclaredMethod(ItemSelectedListener.ITEM_SELECTED,
					new Class[] { ItemSelectedEvent.class });
			addListener(ItemSelectedEvent.class, listener, method);
		} catch (final java.lang.NoSuchMethodException e) {
			throw new java.lang.RuntimeException("Internal error,  method not found");
		}
	}

	@Override
	public void removeListener(ItemSelectedListener listener) {
		removeListener(ItemSelectedEvent.class, listener);
	}


	private List<ChannelAdapter> filterSelected() {
		// Get all items and filter/sort them
		Collection<ChannelAdapter> allItems = container.getItems();
		return allItems.stream()
				.filter(adapter -> Boolean.TRUE.equals(adapter.isSelected()))
				.sorted((a, b) -> {
					String metaA = a.getMetaData() != null ? a.getMetaData() : "";
					String metaB = b.getMetaData() != null ? b.getMetaData() : "";
					return metaA.compareTo(metaB);
				})
				.collect(Collectors.toList());
	}



	protected Component buildLastMeasures(List<ChannelAdapter> items) {
		String[] visibleColumns = new String[] { "displayName", "lastMeasureValue", "measureUnit" };
		ChannelContainerGrid grid = new ChannelContainerGrid(items, visibleColumns);
		return grid;
	}

	protected Component buildAlarms() {
		AlarmContainerGrid grid = new AlarmContainerGrid(device);
		return grid;
	}

	
	protected Component buildLinks() {
		final Grid<GroupWidget> grid = new Grid<>();
		grid.addStyleName("smallgrid");
		grid.setHeightMode(HeightMode.CSS);
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setSizeFull();
		grid.setEnabled(false);
		grid.setHeaderVisible(false);

		grid.addSelectionListener(event -> {
			if (event.getFirstSelectedItem().isPresent()) {
				GroupWidget selectedWidget = event.getFirstSelectedItem().get();
				fireEvent(new ItemSelectedEvent(AbstractDeviceInfo.this, selectedWidget));
				grid.getSelectionModel().deselectAll();
			}
		});
		
		ListDataProvider<GroupWidget> dataProvider = new ListDataProvider<>(new ArrayList<>(widgets));
		grid.setDataProvider(dataProvider);
		
		grid.addColumn(nameValueProvider()).setRenderer(new HtmlRenderer());
		
		return grid;
	}
	
	private ValueProvider<GroupWidget, String> nameValueProvider() {
		return groupWidget -> String.format("%s %s", 
				VaadinIcons.EXTERNAL_LINK.getHtml(),
				groupWidget.getName());
	}

	

	protected VerticalLayout buildGeneral() {
		String status = UIUtils.localize(device.getStatus().getI18nKey());
		Date date = device.getLastContactDate();
		String timestamp = date != null ? ChartUtils.formatDate(date, null) : "";
		StringBuffer sb = new StringBuffer();
		sb.append(VaadinIcons.FLAG.getHtml());
		sb.append("&nbsp;");
		sb.append(device.getSerial());
		sb.append("&nbsp;");
		sb.append(VaadinIcons.COGS.getHtml());
		sb.append("&nbsp;");
		sb.append(status.toUpperCase());
		sb.append("<br/>");
		if (device.checkInactive(date)) {
			sb.append("<span style=\"color: #ff0000;\">");
			sb.append(VaadinIcons.PLUG.getHtml());
			sb.append("&nbsp;OFFLINE");
			sb.append("&nbsp;");
			sb.append(timestamp);
			sb.append("</span>");
			sb.append("&nbsp;");
		} else {
			sb.append(VaadinIcons.PLUG.getHtml());
			sb.append("&nbsp;ONLINE");
			sb.append("&nbsp;");
			sb.append(timestamp);
			sb.append("&nbsp;");
		}

		Label label = new Label(sb.toString(), ContentMode.HTML);
		label.addStyleName(ValoTheme.LABEL_TINY);
		label.setSizeFull();
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.addComponent(label);
		return layout;
	}

	protected Component buildContent() {
		Date lastContact = UIUtils.getCassandraService().getFeeds().getLastContact(device.getSerial());
		device.setLastContactDate(lastContact);
		VerticalLayout general = buildGeneral();
		Tab generalTab = multicomponent.addTab(general, device.getLabel(), VaadinIcons.DASHBOARD);
		generalTab.setStyleName(ValoTheme.LABEL_BOLD);
 
		if (!device.getChannels().isEmpty()) {
			container = new ChannelAdapterDataProvider();
			container.addChannels(device.getChannels());
			List<ChannelAdapter> selected = filterSelected();

			if (lastContact != null && device.isRunning() && !selected.isEmpty()) {
				multicomponent.addTab(buildLastMeasures(selected), getI18nLabel("measures"), VaadinIcons.SIGNAL);
			} else {
				String content = String.format("%s %s", VaadinIcons.SIGNAL.getHtml(), getI18nLabel("no_measures"));
				Label label = new Label(content, ContentMode.HTML);
				label.addStyleName(ValoTheme.LABEL_TINY);
				label.setSizeFull();
				general.addComponent(label);
			}

			if (widgets != null && !widgets.isEmpty()) {
				multicomponent.addTab(buildLinks(), getI18nLabel("visualizations"), VaadinIcons.BAR_CHART);
			} else {
				String content = String.format("%s %s", VaadinIcons.BAR_CHART.getHtml(),
						getI18nLabel("no_visualizations"));
				Label label = new Label(content, ContentMode.HTML);
				label.addStyleName(ValoTheme.LABEL_TINY);
				label.setSizeFull();
				general.addComponent(label);
			}
			long count = UIUtils.getCassandraService().getAlarms().countActiveAlarms(device.getSerial());
			boolean hasAlarms = (count > 0);

			if (hasAlarms) {
				Tab tab = multicomponent.addTab(buildAlarms(), getI18nLabel("alarms"), VaadinIcons.BELL);
				multicomponent.setSelectedTab(tab);
			} else {
				String content = String.format("%s %s", VaadinIcons.BELL_SLASH.getHtml(), getI18nLabel("no_alarms"));
				Label label = new Label(content, ContentMode.HTML);
				label.addStyleName(ValoTheme.LABEL_TINY);
				label.setSizeFull();
				general.addComponent(label);
			}

		}
		return multicomponent;
	}

	
	
	
	public class ChannelContainerGrid extends CustomComponent implements ITabContent {
		private static final long serialVersionUID = 1L;
		private Grid<ChannelAdapter> grid;
		private List<ChannelAdapter> channels;
		private ListDataProvider<ChannelAdapter> dataProvider;
		
		public ChannelContainerGrid(List<ChannelAdapter> items, String[] visibleColumns) {
			super();
			channels = items;
			grid = new Grid<>();
			grid.addStyleName("smallgrid");
			grid.setHeightMode(HeightMode.CSS);
			grid.setSelectionMode(SelectionMode.NONE);
			grid.setSizeFull();
			grid.setEnabled(false);
			grid.setHeaderVisible(false);
			
			dataProvider = new ListDataProvider<>(new ArrayList<>());
			grid.setDataProvider(dataProvider);
			
			// Add columns based on visibleColumns array
			for (String columnId : visibleColumns) {
				switch (columnId) {
					case "displayName":
						grid.addColumn(ChannelAdapter::getDisplayName);
						break;
					case "lastMeasureValue":
						grid.addColumn(ChannelAdapter::getLastMeasureValue);
						break;
					case "measureUnit":
						grid.addColumn(ChannelAdapter::getMeasureUnit);
						break;
				}
			}
			
			setCompositionRoot(grid);
		}

		@Override
		public void refresh() {
			UI.getCurrent().access(new Runnable() {
				@Override
				public void run() {
					dataProvider.getItems().clear();
					dataProvider.getItems().addAll(channels);
					dataProvider.refreshAll();
					UIUtils.push();
				}
			});
		}

		
		@Override
		public boolean isLoaded() {
			return dataProvider.getItems().size() > 0;
		}

		@Override
		public void lazyLoad() {
			if (!isLoaded()) {
				this.refresh();
			}
		}
	}
	
	
	public class AlarmContainerGrid extends CustomComponent implements ITabContent {
		private static final long serialVersionUID = 1L;
		private Grid<ChannelAdapter> grid;
		private Device device;
		private ListDataProvider<ChannelAdapter> dataProvider;

		public AlarmContainerGrid(Device entity) {
			super();
			device = entity;
			grid = new Grid<>();
			grid.addStyleName("smallgrid");
			grid.setHeightMode(HeightMode.CSS);
			grid.setSelectionMode(SelectionMode.NONE);
			grid.setSizeFull();
			grid.setEnabled(false);
			grid.setHeaderVisible(false);
			
			dataProvider = new ListDataProvider<>(new ArrayList<>());
			grid.setDataProvider(dataProvider);
			
			// Add columns for alarms
			grid.addColumn(ChannelAdapter::getDisplayName);
			grid.addColumn(ChannelAdapter::getAlarmValue);
			grid.addColumn(ChannelAdapter::getAlarmDate);
			
			setCompositionRoot(grid);
		}

		@Override
		public boolean isLoaded() {
			return dataProvider.getItems().size() > 0;
		}

		@Override
		public void lazyLoad() {
			if (!isLoaded()) {
				this.refresh();
			}
		}

		@Override
		public void refresh() {
			UI.getCurrent().access(new Runnable() {
				@Override
				public void run() {
					List<FeedAlarmEvent> events = UIUtils.getCassandraService().getAlarms().getAlarmEvents(device.getSerial(), 30);
					ChannelAdapterDataProvider ccontainer = new ChannelAdapterDataProvider();
					ccontainer.addChannels(device.getChannels());
					List<IFeedAlarm> alarms = new ArrayList<>();
					alarms.addAll(events);
					
					List<ChannelAdapter> allAdapters = ccontainer.renderAlarms(alarms);
					// Filter only alarmed items and sort by alarm date descending
					List<ChannelAdapter> filteredAdapters = allAdapters.stream()
							.filter(adapter -> Boolean.TRUE.equals(adapter.isAlarmed()))
							.sorted((a, b) -> {
								if (a.getAlarmDate() == null && b.getAlarmDate() == null) return 0;
								if (a.getAlarmDate() == null) return 1;
								if (b.getAlarmDate() == null) return -1;
								return b.getAlarmDate().compareTo(a.getAlarmDate()); // descending
							})
							.collect(java.util.stream.Collectors.toList());
					
					dataProvider.getItems().clear();
					dataProvider.getItems().addAll(filteredAdapters);
					dataProvider.refreshAll();
					UIUtils.push();
				}
			});
		}
	}
	
	// Bug #2053
	public Collection<GroupWidget> filteredWidgetsByUser(Collection<GroupWidget> widgets) {
		UserDetailsAdapter details = ((IMainUI) UI.getCurrent()).getUserDetails();
		if (details.hasRole(Constants.ROLE_ADMINISTRATOR) || details.hasRole(Constants.ROLE_SUPERVISOR) ) {
			return widgets;
		}
		Collection<GroupWidget> filtered = new ArrayList<GroupWidget>();
		User user = UIUtils.getServiceFactory().getUserService().findOne(details.getUserId());
		for (GroupWidget gw : widgets) {
			if (user.getGroups().contains(gw.getGroup())) {
				filtered.add(gw);
			}
		}
		return filtered;
	}


}
