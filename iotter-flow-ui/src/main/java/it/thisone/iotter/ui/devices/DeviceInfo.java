package it.thisone.iotter.ui.devices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.provider.ListDataProvider;

import it.thisone.iotter.cassandra.model.Feed;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.integration.AlarmService;
import it.thisone.iotter.integration.CassandraService;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.common.ItemSelectedEvent;
import it.thisone.iotter.ui.common.ItemSelectedListener;

import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.ifc.IDeviceInfo;
import it.thisone.iotter.ui.ifc.ITabContent;
import it.thisone.iotter.ui.model.ChannelAdapter;
import it.thisone.iotter.ui.model.ChannelAdapterDataProvider;
import it.thisone.iotter.ui.providers.BackendServices;





public class DeviceInfo extends BaseComponent implements IDeviceInfo {

	private static final long serialVersionUID = 1L;
	private Accordion multicomponent;
	protected Device device;
	protected ChannelAdapterDataProvider container;
	protected Collection<GroupWidget> widgets;

    private final UserDetailsAdapter currentUser;
    private final BackendServices backendServices;

	public DeviceInfo(Device device, UserDetailsAdapter currentUser, BackendServices backendServices) {
		super("maps.devices.google", UUID.randomUUID().toString());
		this.currentUser = currentUser;
		this.backendServices = backendServices;
		multicomponent = new Accordion();
		multicomponent.setHeight("vh80");
		// multicomponent.addClassName(ValoTheme.TABSHEET_FRAMED);
		// multicomponent.addClassName(ValoTheme.TABSHEET_PADDED_TABBAR);
		// multicomponent.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
		// 	/**
		// 			 * 
		// 			 */
		// 	private static final long serialVersionUID = 1L;

		// 	public void selectedTabChange(SelectedTabChangeEvent event) {
		// 		TabSheet tabsheet = event.getTabSheet();
		// 		if (tabsheet.getSelectedTab() instanceof ITabContent) {
		// 			((ITabContent) tabsheet.getSelectedTab()).lazyLoad();
		// 		}
		// 	}
		// });

		setContent(device, new ArrayList<>());
		setRootComposition(multicomponent);
	}

	// public void setContent(Device device, Collection<GroupWidget> widgets) {
	// 	this.device = device;
	// 	this.widgets = filteredWidgetsByUser(widgets);
	// 	buildContent();
	// }

	@Override
	public void addListener(ItemSelectedListener listener) {
		// try {
		// 	Method method = ItemSelectedListener.class.getDeclaredMethod(ItemSelectedListener.ITEM_SELECTED,
		// 			new Class[] { ItemSelectedEvent.class });
		// 	addListener(ItemSelectedEvent.class, listener, method);
		// } catch (final java.lang.NoSuchMethodException e) {
		// 	throw new java.lang.RuntimeException("Internal error,  method not found");
		// }
	}

	@Override
	public void removeListener(ItemSelectedListener listener) {
		//removeListener(ItemSelectedEvent.class, listener);
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
		grid.addClassName("smallgrid");
		// grid.setHeightMode(HeightMode.CSS);
		// grid.setSelectionMode(SelectionMode.SINGLE);
		// grid.setSizeFull();
		grid.setEnabled(false);
		//grid.setHeaderVisible(false);

		grid.addSelectionListener(event -> {
			if (event.getFirstSelectedItem().isPresent()) {
				GroupWidget selectedWidget = event.getFirstSelectedItem().get();
				fireEvent(new ItemSelectedEvent(DeviceInfo.this, selectedWidget));
				grid.getSelectionModel().deselectAll();
			}
		});
		
		ListDataProvider<GroupWidget> dataProvider = new ListDataProvider<>(new ArrayList<>(widgets));
		grid.setDataProvider(dataProvider);
		
		grid.addColumn(new ComponentRenderer<>(this::buildLinkLabel))
				.setHeader(getI18nLabel("visualizations"))
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setTextAlign(ColumnTextAlign.START);
		
		return grid;
	}
	
	private Component buildLinkLabel(GroupWidget groupWidget) {
		HorizontalLayout row = new HorizontalLayout();
		row.setPadding(false);
		row.setSpacing(true);
		row.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		row.add(VaadinIcon.EXTERNAL_LINK.create(), new Span(groupWidget.getName()));
		return row;
	}

	

	protected VerticalLayout buildGeneral() {
		String status = getTranslation(device.getStatus().getI18nKey());
		Date date = device.getLastContactDate();
		String timestamp = date != null ? ChartUtils.formatDate(date, null) : "";
		VerticalLayout layout = new VerticalLayout();
		layout.setPadding(true);
		layout.setSpacing(false);
		layout.add(buildGeneralStatusRow(status, timestamp, device.checkInactive(date)));
		return layout;
	}

	protected Component buildContent() {
		Date lastContact = backendServices.getCassandraFeeds().getLastContact(device.getSerial());
		device.setLastContactDate(lastContact);
		VerticalLayout general = buildGeneral();
		AccordionPanel generalPanel = multicomponent.add("", general);
		generalPanel.setSummary(buildDeviceSummary(lastContact));
		//generalTab.setClassName(ValoTheme.LABEL_BOLD);
 
		if (!device.getChannels().isEmpty()) {
			container = new ChannelAdapterDataProvider();
			container.setMeasureRenderer(backendServices.getMeasureUnitTypeService().getMeasureUnitChoiceFormat());
			container.addChannels(device.getChannels());
			List<ChannelAdapter> selected = filterSelected();

			if (lastContact != null && device.isRunning() && !selected.isEmpty()) {
				multicomponent.add(getI18nLabel("measures"), buildLastMeasures(selected));
			} else {
				general.add(buildInfoRow(VaadinIcon.SIGNAL, getI18nLabel("no_measures")));
			}

			widgets = backendServices.getGroupWidgetService().findByDevice(device);
			if (widgets != null && !widgets.isEmpty()) {
				multicomponent.add(getI18nLabel("visualizations"), buildLinks());
			} else {
				general.add(buildInfoRow(VaadinIcon.BAR_CHART, getI18nLabel("no_visualizations")));
			}
			long count = backendServices.getCassandraAlarms().countActiveAlarms(device.getSerial());
			boolean hasAlarms = (count > 0);

			if (hasAlarms) {
				AccordionPanel alarmPanel = multicomponent.add(getI18nLabel("alarms"), buildAlarms());
				multicomponent.open(alarmPanel);
			} else {
				general.add(buildInfoRow(VaadinIcon.BELL_SLASH, getI18nLabel("no_alarms")));
			}

		}
		return multicomponent;
	}

	private Component buildDeviceSummary(Date lastContact) {
		HorizontalLayout summary = new HorizontalLayout();
		summary.setWidthFull();
		summary.setPadding(false);
		summary.setSpacing(true);
		summary.setDefaultVerticalComponentAlignment(Alignment.CENTER);

		Icon flag = VaadinIcon.FLAG.create();
		Span label = new Span(device.getLabel());
		label.getStyle().set("font-weight", "600");

		Span serial = new Span(device.getSerial());
		serial.getStyle().set("color", "var(--lumo-secondary-text-color)");

		Span connection = buildStatusBadge(getTranslation(device.getStatus().getI18nKey()).toUpperCase(),
				device.isRunning() ? "var(--lumo-success-color-10pct)" : "var(--lumo-contrast-10pct)",
				device.isRunning() ? "var(--lumo-success-text-color)" : "var(--lumo-body-text-color)");

		Span availability = buildStatusBadge(device.checkInactive(lastContact) ? "OFFLINE" : "ONLINE",
				device.checkInactive(lastContact) ? "var(--lumo-error-color-10pct)" : "var(--lumo-success-color-10pct)",
				device.checkInactive(lastContact) ? "var(--lumo-error-text-color)" : "var(--lumo-success-text-color)");

		summary.add(flag, label, serial, connection, availability);
		return summary;
	}

	private Component buildGeneralStatusRow(String status, String timestamp, boolean inactive) {
		VerticalLayout wrapper = new VerticalLayout();
		wrapper.setPadding(false);
		wrapper.setSpacing(false);

		HorizontalLayout primary = new HorizontalLayout();
		primary.setPadding(false);
		primary.setSpacing(true);
		primary.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		primary.add(VaadinIcon.FLAG.create(), new Span(device.getSerial()), buildStatusBadge(status.toUpperCase(),
				"var(--lumo-contrast-10pct)", "var(--lumo-body-text-color)"));

		HorizontalLayout secondary = new HorizontalLayout();
		secondary.setPadding(false);
		secondary.setSpacing(true);
		secondary.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		secondary.add(VaadinIcon.PLUG.create(),
				buildStatusBadge(inactive ? "OFFLINE" : "ONLINE",
						inactive ? "var(--lumo-error-color-10pct)" : "var(--lumo-success-color-10pct)",
						inactive ? "var(--lumo-error-text-color)" : "var(--lumo-success-text-color)"));
		if (hasText(timestamp)) {
			Span time = new Span(timestamp);
			time.getStyle().set("color", "var(--lumo-secondary-text-color)");
			secondary.add(time);
		}

		wrapper.add(primary, secondary);
		return wrapper;
	}

	private Component buildInfoRow(VaadinIcon iconType, String text) {
		HorizontalLayout row = new HorizontalLayout();
		row.setWidthFull();
		row.setPadding(false);
		row.setSpacing(true);
		row.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		row.add(iconType.create(), new Span(text));
		return row;
	}

	private Span buildStatusBadge(String text, String backgroundColor, String textColor) {
		Span badge = new Span(text);
		badge.getStyle().set("background", backgroundColor);
		badge.getStyle().set("color", textColor);
		badge.getStyle().set("border-radius", "999px");
		badge.getStyle().set("font-size", "var(--lumo-font-size-s)");
		badge.getStyle().set("font-weight", "600");
		badge.getStyle().set("padding", "0.15rem 0.55rem");
		return badge;
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	
	
	
	public class ChannelContainerGrid extends Composite<VerticalLayout> implements ITabContent {
		private static final long serialVersionUID = 1L;
		private Grid<ChannelAdapter> grid;
		private List<ChannelAdapter> channels;
		private ListDataProvider<ChannelAdapter> dataProvider;
		
		public ChannelContainerGrid(List<ChannelAdapter> items, String[] visibleColumns) {
			super();
			channels = items;
			grid = new Grid<>();
			grid.addClassName("smallgrid");
			// grid.setHeightMode(HeightMode.CSS);
			// grid.setSelectionMode(SelectionMode.NONE);
			grid.setSizeFull();
			grid.setEnabled(false);
			//grid.setHeaderVisible(false);
			
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
			
			getContent().add(grid);
		}

		@Override
		public void refresh() {
			// UI.getCurrent().access(new Runnable() {
			// 	@Override
			// 	public void run() {
			// 		dataProvider.getItems().clear();
			// 		dataProvider.getItems().addAll(channels);
			// 		dataProvider.refreshAll();
			// 		UIUtils.push();
			// 	}
			// });
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
	
	
	public class AlarmContainerGrid extends Composite<VerticalLayout> implements ITabContent {
		private static final long serialVersionUID = 1L;
		private Grid<ChannelAdapter> grid;

		private ListDataProvider<ChannelAdapter> dataProvider;

		public AlarmContainerGrid(Device entity) {
			super();
			device = entity;
			grid = new Grid<>();
			grid.addClassName("smallgrid");
			// grid.setHeightMode(HeightMode.CSS);
			// grid.setSelectionMode(SelectionMode.NONE);
			grid.setSizeFull();
			grid.setEnabled(false);
			//grid.setHeaderVisible(false);
			
			dataProvider = new ListDataProvider<>(new ArrayList<>());
			grid.setDataProvider(dataProvider);
			
			// Add columns for alarms
			grid.addColumn(ChannelAdapter::getDisplayName);
			grid.addColumn(ChannelAdapter::getAlarmValue);
			grid.addColumn(ChannelAdapter::getAlarmDate);
			
			setRootComposition(grid);
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
			// UI.getCurrent().access(new Runnable() {
			// 	@Override
			// 	public void run() {
			// 		List<FeedAlarmEvent> events = CassandraService.getAlarms().getAlarmEvents(device.getSerial(), 30);
			// 		ChannelAdapterDataProvider ccontainer = new ChannelAdapterDataProvider();
			// 		ccontainer.addChannels(device.getChannels());
			// 		List<IFeedAlarm> alarms = new ArrayList<>();
			// 		alarms.addAll(events);
					
			// 		List<ChannelAdapter> allAdapters = ccontainer.renderAlarms(alarms);
			// 		// Filter only alarmed items and sort by alarm date descending
			// 		List<ChannelAdapter> filteredAdapters = allAdapters.stream()
			// 				.filter(adapter -> Boolean.TRUE.equals(adapter.isAlarmed()))
			// 				.sorted((a, b) -> {
			// 					if (a.getAlarmDate() == null && b.getAlarmDate() == null) return 0;
			// 					if (a.getAlarmDate() == null) return 1;
			// 					if (b.getAlarmDate() == null) return -1;
			// 					return b.getAlarmDate().compareTo(a.getAlarmDate()); // descending
			// 				})
			// 				.collect(java.util.stream.Collectors.toList());
					
			// 		dataProvider.getItems().clear();
			// 		dataProvider.getItems().addAll(filteredAdapters);
			// 		dataProvider.refreshAll();
			// 		UIUtils.push();
			// 	}
			// });
		}
	}
	
	// Bug #2053
	public Collection<GroupWidget> filteredWidgetsByUser(Collection<GroupWidget> widgets) {
		
		if (currentUser.hasRole(Constants.ROLE_ADMINISTRATOR) || currentUser.hasRole(Constants.ROLE_SUPERVISOR) ) {
			return widgets;
		}
		Collection<GroupWidget> filtered = new ArrayList<GroupWidget>();
		User user = backendServices.getUserService().findOne(currentUser.getUserId());
		for (GroupWidget gw : widgets) {
			if (user.getGroups().contains(gw.getGroup())) {
				filtered.add(gw);
			}
		}
		return filtered;
	}


	private GraphicFeed resetFeed;



	@Override
	public void setContent(Device device, Collection<GroupWidget> widgets) {
		// if (device.getStatus().equals(DeviceStatus.CONNECTED)) {
		// 	try {
		// 		device = updateContent(device.getSerial(), widgets);
		// 	} catch (BackendServiceException e) {
		// 		//logger.error(device.getSerial(), e);
		// 	}
		// }
		
		for (GroupWidget gwidget : widgets) {
			if (gwidget.isExclusive()) {
				for (GraphicWidget widget : gwidget.getWidgets()) {
					for (GraphicFeed feed : widget.getFeeds()) {
						if (feed.getChannel() != null && feed.getSection()!=null && feed.getSection().startsWith("reset")) {
							resetFeed = feed;
							break;
						}
					}
				}
			}
		}

	this.device = device;
	this.widgets = (currentUser != null && !widgets.isEmpty())
			? filteredWidgetsByUser(widgets)
			: new ArrayList<>(widgets);


	}

	private Device updateContent(String sn, Collection<GroupWidget> widgets) throws BackendServiceException {
		boolean changed = false;
		Device device = backendServices.getDeviceService().findBySerial(sn);
		try {
			List<String> keys = new ArrayList<String>();
			Map<String, Channel> map = new HashMap<String, Channel>();
			for (Channel chnl : device.getChannels()) {
				if (chnl.getMetaData() != null) {
					map.put(chnl.getMetaData(), chnl);
				}
			}

			for (GroupWidget gwidget : widgets) {
				if (gwidget.isExclusive()) {
					changed = false;
					for (GraphicWidget widget : gwidget.getWidgets()) {
						for (GraphicFeed feed : widget.getFeeds()) {
							
							if (feed.getChannel() != null && !feed.getChannel().getMeasures().isEmpty()) {
								keys.add(feed.getKey());
							} else {
								Channel chnl = map.get(feed.getMetaData());
								if (chnl != null) {
									feed.setChannel(chnl);
									feed.setMeasure(chnl.getDefaultMeasure());
									keys.add(feed.getKey());
									changed = true;
								}
							}
							
							if (feed.getChannel() != null && feed.getSection()!=null && feed.getSection().startsWith("ascii")){
								keys.remove(feed.getKey());
								feed.getChannel().getConfiguration().setSelected(false);
							}
							
							
							
						}
					}
					if (changed) {
						backendServices.getGroupWidgetService().update(gwidget);
					}
				}
			}

			changed = false;
			for (Channel channel : device.getChannels()) {
				boolean selected = keys.contains(channel.getKey());
				if (channel.getConfiguration().isSelected() != selected) {
					channel.getConfiguration().setSelected(selected);
					Feed item = new Feed(device.getSerial(), channel.getKey());
					item.setSelected(selected);
					backendServices.getCassandraFeeds().updateOnSelected(item);
					changed = true;
				}
			}
			if (changed) {
				backendServices.getDeviceService().update(device);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			throw new BackendServiceException(t);
		}
		// for (Channel channel : device.getChannels()) {
		// if (channel.getConfiguration().isSelected()) {
		// logger.error(channel.toString() + " " +
		// channel.getConfiguration().isSelected());
		// }
		// }

		return device;
	}

//	@Override
//	protected Component buildAlarms() {
//		return new QuickAlarmInfo(device, resetFeed);
//	}

}
