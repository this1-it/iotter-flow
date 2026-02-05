package it.thisone.iotter.ui.channels;

import java.text.ChoiceFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.flow.data.provider.ListDataProvider;
import java.util.Comparator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.HeaderRow;


import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChannelRemoteControl;
import it.thisone.iotter.security.Permissions;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.AbstractBaseEntityListing;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChannelUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.devices.DeviceForm;
import it.thisone.iotter.ui.ifc.ITabContent;

import it.thisone.iotter.ui.model.ChannelAdapterDataProvider;

public class ChannelRemoteControlListing extends AbstractBaseEntityListing<Channel> implements ITabContent {

	private static final long serialVersionUID = 1L;

	private final Permissions permissions;
	private final List<Channel> channels;
	private final ChannelAdapterDataProvider adapterProvider = new ChannelAdapterDataProvider();
	private ListDataProvider<Channel> dataProvider;
	private Grid<Channel> grid;
	private boolean loaded;

	public ChannelRemoteControlListing(Collection<Channel> items) {
		super(Channel.class, DeviceForm.NAME, "channel.remote", false);
		this.permissions = new Permissions(true);
		this.channels = items == null ? new ArrayList<>() : new ArrayList<>(items);
		buildLayout();
	}

	private void buildLayout() {
		HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.setWidthFull();
		toolbar.setSpacing(true);
		toolbar.setPadding(true);
		//toolbar.addClassName(UIUtils.TOOLBAR_STYLE);

		grid = createGrid();
		VerticalLayout content = createContent(grid);
		setSelectable(grid);

		getButtonsLayout().add(createModifyButton());
		toolbar.add(getButtonsLayout());
		toolbar.setAlignSelf(Alignment.END, getButtonsLayout());
		enableButtons(null);

		getMainLayout().add(toolbar);
		getMainLayout().add(content);
		getMainLayout().setFlexGrow(1f, content);
	}

	@Override
	public AbstractBaseEntityForm<Channel> getEditor(Channel item, boolean readonly) {
		return new ChannelRemoteControlForm(item);
	}



	private Grid<Channel> createGrid() {
		Grid<Channel> grid = new Grid<>();
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.setSizeFull();
		//grid.addClassName(UIUtils.TABLE_STYLE);

		dataProvider = new ListDataProvider<>(new ArrayList<>());
		//dataProvider.setSortComparator(buildLabelComparator());
		grid.setDataProvider(dataProvider);
		setDataProvider(dataProvider);

		List<Grid.Column<Channel>> columns = new ArrayList<>();
		columns.add(grid.addComponentColumn(this::createLabel).setKey(ChannelUtils.CHANNEL_LABEL));
		columns.add(grid.addColumn(Channel::getNumber).setKey("number"));
		columns.add(grid.addColumn(this::formatThresholds).setKey("remote.thresholds"));
		columns.add(grid.addColumn(this::formatPermission).setKey("remote.permission"));
		columns.add(grid.addColumn(this::formatRemoteValue).setKey("remote.value"));
		columns.add(grid.addColumn(channel -> ChannelUtils.getTypeVar(channel.getMetaData()))
				.setKey("measures.typeVar"));
		columns.add(grid.addColumn(this::formatMeasureType).setKey("measures.type"));

		if (isSupervisor()) {
			columns.add(grid.addColumn(channel -> String.valueOf(channel.isCrucial())).setKey("crucial"));
		}

		for (Grid.Column<Channel> column : columns) {
			column.setSortable(false);
			column.setHeader(getI18nLabel(column.getKey()));
		}

		grid.setColumnOrder(columns.toArray(new Grid.Column[0]));
		initFilters(grid);
		return grid;
	}

	private Comparator<Channel> buildLabelComparator() {
		return (left, right) -> {
			String leftLabel = ChannelUtils.displayName(left);
			String rightLabel = ChannelUtils.displayName(right);
			if (leftLabel == null && rightLabel == null) {
				return 0;
			}
			if (leftLabel == null) {
				return -1;
			}
			if (rightLabel == null) {
				return 1;
			}
			return leftLabel.compareToIgnoreCase(rightLabel);
		};
	}

	private void initFilters(Grid<Channel> grid) {
		HeaderRow filterRow = grid.appendHeaderRow();
		TextField labelField = new TextField();
		labelField.setPlaceholder("Filter...");
		labelField.setWidthFull();
		labelField.setValueChangeMode(ValueChangeMode.LAZY);
		filterRow.getCell(grid.getColumnByKey(ChannelUtils.CHANNEL_LABEL)).setComponent(labelField);

		labelField.addValueChangeListener(event -> {
			String filter = event.getValue();
			dataProvider.clearFilters();
			if (filter != null && !filter.trim().isEmpty()) {
				String lower = filter.toLowerCase();
				dataProvider.setFilter(channel -> {
					String label = ChannelUtils.displayName(channel);
					return label != null && label.toLowerCase().contains(lower);
				});
			}
		});
	}

	private Span createLabel(Channel channel) {
		Span label = new Span(ChannelUtils.displayName(channel));
		if (channel.getConfiguration().isActive()) {
			//label.setClassName(UiConstants.ACTIVE_PARAM_STYLE);
		}
		return label;
	}

	private String formatThresholds(Channel channel) {
		if (!isRemoteValid(channel)) {
			return "";
		}
		return adapterProvider.thresholds(channel);
	}

	private String formatPermission(Channel channel) {
		if (!isRemoteValid(channel)) {
			return "";
		}
		return channel.getRemote().getPermission();
	}

	private String formatRemoteValue(Channel channel) {
		if (!isRemoteValid(channel)) {
			return "";
		}
		FeedKey feedKey = new FeedKey(channel.getDevice().getSerial(), channel.getKey());
		MeasureRaw measure = ChartUtils.lastMeasure(feedKey);
		if (measure == null || measure.getValue() == null) {
			return ChannelAdapterDataProvider.EMPTY_VALUE;
		}
		ChoiceFormat choiceFormat = ChannelUtils.enumChoiceFormat(channel);
		Double value = adapterProvider.calculateValue(measure.getValue(), channel.getDefaultMeasure());
		channel.getRemote().setValue(value);
		return adapterProvider.renderValue(value, choiceFormat, channel.getDefaultMeasure());
	}

	private String formatMeasureType(Channel channel) {
		return it.thisone.iotter.ui.common.MarkupsUtils.toHtmlMeasureUnit(channel.getMeasures());
	}

	private boolean isRemoteValid(Channel channel) {
		if (channel == null) {
			return false;
		}
		ChannelRemoteControl remote = channel.getRemote();
		return remote != null && remote.isValid();
	}

	private VerticalLayout createContent(Grid<Channel> grid) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.add(grid);
		layout.setFlexGrow(1f, grid);
		return layout;
	}

	private Button createModifyButton() {
		Button button = new Button();
		//button.setIcon(Va);
		button.getElement().setProperty("title", getI18nLabel("modify_dialog"));
		button.addClickListener(event -> openEditor(getCurrentValue()));
		button.setVisible(permissions.isModifyMode());
		return button;
	}

	private void openEditor(Channel item) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityForm<Channel> editor = getEditor(item,false);
		String caption = String.format("%s %s", getI18nLabel("remote.modify_dialog"),
				item.getConfiguration().getDisplayName());
		Dialog dialog = createDialog(caption, editor);
		editor.setSavedHandler(entity -> {
			dialog.close();
			refresh();
		});
		dialog.open();
	}

	@Override
	public void lazyLoad() {
		if (loaded) {
			return;
		}
		List<Channel> remoteChannels = new ArrayList<>();
		for (Channel channel : channels) {
			if (isRemoteValid(channel) && channel.getConfiguration() != null
					&& channel.getConfiguration().isActive()) {
				remoteChannels.add(channel);
			}
		}
		dataProvider.getItems().clear();
		dataProvider.getItems().addAll(remoteChannels);
		dataProvider.refreshAll();
		loaded = true;
	}

	@Override
	public boolean isLoaded() {
		return loaded;
	}

	@Override
	public void refresh() {
		if (dataProvider != null) {
			dataProvider.refreshAll();
		}
	}

	private boolean isSupervisor() {
		// TODO Flow migration: inject authenticated user context and evaluate roles.
		return false;
	}
}
