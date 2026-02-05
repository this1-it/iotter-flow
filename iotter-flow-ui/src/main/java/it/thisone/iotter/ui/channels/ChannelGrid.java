package it.thisone.iotter.ui.channels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.vaadin.flow.data.provider.ListDataProvider;

import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.data.provider.SortDirection;


import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChannelComparator;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.common.MarkupsUtils;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChannelUtils;
import it.thisone.iotter.ui.devices.DeviceForm;
import it.thisone.iotter.ui.main.UiConstants;

public class ChannelGrid extends BaseComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ListDataProvider<Channel> dataProvider;
	private Grid<Channel> grid;

	/**
	 * Default constructor which populates the select with existing Channels.
	 */
	public ChannelGrid(Collection<Channel> channels) {
    	super(DeviceForm.NAME, "ChannelTable");
		grid = createGrid(new ArrayList<Channel>(channels));
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.add(grid);
		layout.setFlexGrow(1f, grid);
		getContent().removeAll();
		getContent().add(layout);
   }

	private Grid<Channel> createGrid(List<Channel> channels) {
		if (channels != null) {
			Collections.sort(channels, new ChannelComparator());
		}

		dataProvider = new ListDataProvider<Channel>(channels == null ? Collections.emptyList() : channels);
		Grid<Channel> table = new Grid<>();
		table.setDataProvider(dataProvider);

		List<Grid.Column<Channel>> columns = new ArrayList<Grid.Column<Channel>>();

		columns.add(table.addComponentColumn(this::buildChannelLabel)
				.setKey(ChannelUtils.CHANNEL_LABEL)
				.setFlexGrow(2));
		columns.add(table.addComponentColumn(channel -> buildMeasureAttribute(channel, "type"))
				.setKey("measures.type")
				.setFlexGrow(1));
		columns.add(table.addComponentColumn(channel -> buildMeasureAttribute(channel, "typeVar"))
				.setKey("measures.typeVar")
				.setFlexGrow(1));
		columns.add(table.addColumn(this::formatQualifier)
				.setKey("configuration.qualifier")
				.setFlexGrow(1));
		columns.add(table.addComponentColumn(channel -> buildMeasureAttribute(channel, "scale"))
				.setKey("measures.scale")
				.setFlexGrow(1));
		columns.add(table.addComponentColumn(channel -> buildMeasureAttribute(channel, "offset"))
				.setKey("measures.offset")
				.setFlexGrow(1));
		columns.add(table.addComponentColumn(this::formatValidities)
				.setKey("validities")
				.setFlexGrow(1));
		columns.add(table.addColumn(Channel::getNumber)
				.setKey("id")
				.setFlexGrow(1));

		if (isSupervisor()) {
			columns.add(0, table.addColumn(this::formatUniqueKey)
					.setKey("uniqueKey")
					.setFlexGrow(1));
		}

		for (Grid.Column<Channel> column : columns) {
			column.setHeader(getI18nLabel(column.getKey()));
		}

		HeaderRow filterRow = table.appendHeaderRow();
		TextField filterField = new TextField();
		filterField.setPlaceholder(getI18nLabel("search_devices_hint"));
		filterField.setWidthFull();
		filterField.setValueChangeMode(ValueChangeMode.LAZY);
		filterField.addClassName("small");
		filterField.addValueChangeListener(event -> applyFilter(event.getValue()));
		filterRow.getCell(table.getColumnByKey(ChannelUtils.CHANNEL_LABEL)).setComponent(filterField);

		table.setSelectionMode(Grid.SelectionMode.NONE);
		table.setSizeFull();

		dataProvider.setSortOrder(this::sortKey, SortDirection.ASCENDING);

		return table;
	}

	private void applyFilter(String text) {
		if (text == null || text.trim().isEmpty()) {
			dataProvider.clearFilters();
			return;
		}
		String lower = text.toLowerCase();
		dataProvider.setFilter(channel -> {
			String display = ChannelUtils.displayName(channel);
			return display != null && display.toLowerCase().contains(lower);
		});
	}

	private String sortKey(Channel channel) {
		String display = ChannelUtils.displayName(channel);
		return display != null ? display : "";
	}

	private Component buildChannelLabel(Channel channel) {
		Span channelLabel = new Span(ChannelUtils.displayName(channel));
		if (channel.getConfiguration().isActive()) {
			//channelLabel.setClassName(UiConstants.ACTIVE_PARAM_STYLE);
		}
		return channelLabel;
	}

	private Component buildMeasureAttribute(Channel channel, String propertyId) {
		List<MeasureUnit> measures = channel != null ? channel.getMeasures() : Collections.emptyList();
		String typeVar = ChannelUtils.getTypeVar(channel != null ? channel.getMetaData() : null);
		String content = "";
		if ("typeVar".equals(propertyId)) {
			content = typeVar;
		} else if ("type".equals(propertyId)) {
			content = MarkupsUtils.toHtmlMeasureUnit(measures);
		} else if ("format".equals(propertyId)) {
			content = MarkupsUtils.toHtmlMeasureFormat(measures);
		} else if ("offset".equals(propertyId)) {
			content = MarkupsUtils.toHtmlMeasureOffset(measures);
		} else if ("scale".equals(propertyId)) {
			content = MarkupsUtils.toHtmlMeasureScale(measures);
		}
	    Span span = new Span();
	    span.getElement().setProperty("innerHTML", content);
	    return span;
	}

	private Component formatValidities(Channel channel) {
		String content = MarkupsUtils.channelRange(channel).toString();
	    Span span = new Span();
	    span.getElement().setProperty("innerHTML", content);
	    return span;
	}

	private String formatUniqueKey(Channel channel) {
		return channel.getDevice().getSerial() + "." + channel.getUniqueKey();
	}

	private String formatQualifier(Channel channel) {
		String qualifier = String.format("%d - %d", channel.getConfiguration().getQualifier(),
				channel.getConfiguration().getSensor());
		return qualifier;
	}

	private boolean isSupervisor() {
		// TODO Flow migration: inject authenticated user context and evaluate roles.
		return false;
	}
}
