package it.thisone.iotter.ui.channels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChannelComparator;
import it.thisone.iotter.provisioning.ProvisionedEvent;
import it.thisone.iotter.security.Permissions;

import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.AbstractBaseEntityListing;
import it.thisone.iotter.ui.common.ConfirmationDialog;
import it.thisone.iotter.ui.common.EntityRemovedEvent;
import it.thisone.iotter.ui.common.EntityRemovedListener;
import it.thisone.iotter.ui.common.EntitySelectedEvent;
import it.thisone.iotter.ui.common.EntitySelectedListener;
import it.thisone.iotter.ui.common.MarkupsUtils;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.ConfirmationDialog.Callback;
import it.thisone.iotter.ui.common.charts.ChannelUtils;
import it.thisone.iotter.ui.devices.DeviceForm;
import it.thisone.iotter.ui.ifc.ITabContent;
import it.thisone.iotter.util.PopupNotification;

public class ChannelListing extends AbstractBaseEntityListing<Channel> implements ITabContent {

	private static final long serialVersionUID = 1L;

	private final Permissions permissions;
	private final List<Channel> channels;
	private final List<Channel> removed = new ArrayList<>();
	private ListDataProvider<Channel> dataProvider;
	private Grid<Channel> grid;
	private boolean loaded;

	public ChannelListing() {
		super(Channel.class, DeviceForm.NAME, "channel.listing", false);
		this.permissions = new Permissions(true);
		this.channels =  new ArrayList<Channel>();
		buildLayout();
	}

	public void setItems(List<Channel> channels) {
		dataProvider.getItems().clear();
		dataProvider.getItems().addAll(channels);
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

		getButtonsLayout().add(createViewButton());
		getButtonsLayout().add(createRemoveButton());
		toolbar.add(getButtonsLayout());
		toolbar.setAlignSelf(Alignment.END, getButtonsLayout());
		enableButtons(null);

		getMainLayout().add(toolbar);
		getMainLayout().add(content);
		getMainLayout().setFlexGrow(1f, content);
	}

	@Override
	public AbstractBaseEntityForm<Channel> getEditor(Channel item, boolean readonly) {
		return null;
	}


	private Grid<Channel> createGrid() {
		Grid<Channel> grid = new Grid<>();
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.setSizeFull();
		//grid.addClassName(UIUtils.TABLE_STYLE);

		dataProvider = new ListDataProvider<>(new ArrayList<>());
		grid.setDataProvider(dataProvider);
		setDataProvider(dataProvider);

		List<Grid.Column<Channel>> columns = new ArrayList<>();
		Grid.Column<Channel> labelColumn = grid.addColumn(ChannelUtils::displayName)
				.setKey(ChannelUtils.CHANNEL_LABEL);
		columns.add(labelColumn);

		Grid.Column<Channel> typeColumn = grid.addColumn(channel -> MarkupsUtils.toHtmlMeasureUnit(channel.getMeasures())).setKey("measures.type");
		columns.add(typeColumn);

		Grid.Column<Channel> typeVarColumn = grid.addColumn(
				channel -> ChannelUtils.getTypeVar(channel.getMetaData()))
				.setKey("measures.typeVar");
		columns.add(typeVarColumn);

		Grid.Column<Channel> qualifierColumn = grid.addColumn(this::formatQualifier)
				.setKey("configuration.qualifier");
		columns.add(qualifierColumn);

		Grid.Column<Channel> scaleColumn = grid.addColumn(channel -> MarkupsUtils.toHtmlMeasureScale(channel.getMeasures())).setKey("measures.scale");
		columns.add(scaleColumn);

		Grid.Column<Channel> offsetColumn = grid.addColumn(channel -> MarkupsUtils.toHtmlMeasureOffset(channel.getMeasures())).setKey("measures.offset");
		columns.add(offsetColumn);

		Grid.Column<Channel> validitiesColumn = grid.addColumn(
				channel -> MarkupsUtils.channelRange(channel)).setKey("validities");
		columns.add(validitiesColumn);

		Grid.Column<Channel> numberColumn = grid.addColumn(Channel::getNumber).setKey("number");
		columns.add(numberColumn);

		if (isSupervisor()) {
			Grid.Column<Channel> uniqueKeyColumn = grid.addColumn(this::formatUniqueKey).setKey("uniqueKey");
			columns.add(0, uniqueKeyColumn);
		}

		for (Grid.Column<Channel> column : columns) {
			column.setSortable(false);
			column.setHeader(getI18nLabel(column.getKey()));
		}

		grid.setColumnOrder(columns.toArray(new Grid.Column[0]));
		return grid;
	}

	private String formatQualifier(Channel channel) {
		if (channel.getConfiguration() == null) {
			return "";
		}
		return String.format("%d - %d",
				channel.getConfiguration().getQualifier(),
				channel.getConfiguration().getSensor());
	}

	private String formatUniqueKey(Channel channel) {
		if (channel.getDevice() == null) {
			return channel.getUniqueKey();
		}
		return channel.getDevice().getSerial() + "." + channel.getUniqueKey();
	}

	private VerticalLayout createContent(Grid<Channel> grid) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.add(grid);
		layout.setFlexGrow(1f, grid);
		return layout;
	}

	private Button createViewButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.BAR_CHART.create());
		button.getElement().setProperty("title", getI18nLabel("channel_timeline"));
		button.addClickListener(event -> openDetails(getCurrentValue()));
		button.setVisible(permissions.isViewMode());
		return button;
	}

	private Button createRemoveButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.TRASH.create());
		button.setId("remove" + getId());
		button.getElement().setProperty("title", getI18nLabel("remove"));
		button.addClickListener(event -> openRemove(getCurrentValue()));
		button.setVisible(permissions.isRemoveMode());
		return button;
	}
	
	protected void openRemove(Channel channel) {
		
		FormLayout layout = new FormLayout();
		TextField label = new TextField(getI18nLabel(ChannelUtils.CHANNEL_LABEL));
		label.setWidthFull();
		label.setReadOnly(false);
		label.setValue(ChannelUtils.displayName(channel));
		label.setReadOnly(true);
		layout.add(label);
		
		Callback callback = new Callback() {
			@Override
			public void onDialogResult(boolean result) {
				if (!result) {
					return;
				}
				removed.add(channel);
				dataProvider.getItems().remove(channel);
				dataProvider.refreshAll();
				enableButtons(null);
			}
			
		};
		
		Dialog dialog = new ConfirmationDialog(getI18nLabel("remove_channel"), layout, callback);
		dialog.open();
		
	}

	protected void openDetails(Channel channel) {
		if (channel == null) {
			return;
		}
		
		Dialog dialog = createDialog(getI18nLabel("channel_timeline"), new ChannelTimelineDetails(channel));
		dialog.open();
		
//		AbstractBaseEntityDetails<Channel> details = getDetails(item, remove);
//		Dialog dialog = createDialog(label, details);
//		details.addListener(new EntityRemovedListener() {
//			@Override
//			public void entityRemoved(EntityRemovedEvent<?> event) {
//				dialog.close();
//				if (event.getItem() != null) {
//					removed.add(item);
//					dataProvider.getItems().remove(item);
//					dataProvider.refreshAll();
//					enableButtons(null);
//				}
//			}
//		});
//		details.addListener(new EntitySelectedListener() {
//			@Override
//			public void entitySelected(EntitySelectedEvent<?> event) {
//				dialog.close();
//			}
//		});
//		dialog.open();
	}

	@Override
	public void enableButtons(Channel item) {
		super.enableButtons(item);
		if (item == null) {
			return;
		}
//		getButtonsLayout().iterator().forEachRemaining(component -> {
//			if (component instanceof Button) {
//				Button button = (Button) component;
//				if (button.getId() != null && button.getId().contains("remove")) {
//					button.setEnabled(!item.getConfiguration().isActive());
//				}
//			}
//		});
	}

	public List<Channel> getRemoved() {
		return removed;
	}

	@Override
	public void lazyLoad() {
		if (loaded) {
			return;
		}
		List<Channel> ordered = new ArrayList<>(channels);
		Collections.sort(ordered, new ChannelComparator());
		dataProvider.getItems().clear();
		dataProvider.getItems().addAll(ordered);
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
