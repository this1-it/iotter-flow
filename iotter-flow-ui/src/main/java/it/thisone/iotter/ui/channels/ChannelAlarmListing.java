package it.thisone.iotter.ui.channels;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.dialog.Dialog;


import it.thisone.iotter.enums.Priority;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChannelAlarm;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.security.Permissions;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.AbstractBaseEntityListing;
import it.thisone.iotter.ui.common.EditorSavedEvent;
import it.thisone.iotter.ui.common.EditorSavedListener;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChannelUtils;
import it.thisone.iotter.ui.ifc.ITabContent;

public class ChannelAlarmListing extends AbstractBaseEntityListing<Channel> implements ITabContent {

	private static final long serialVersionUID = 1L;

	private final List<Channel> channels;
	private final Permissions permissions;
	private ListDataProvider<Channel> dataProvider;
	private Grid<Channel> grid;
	private NetworkGroup entity;
	private Button users;
	private boolean loaded;

	public ChannelAlarmListing(List<Channel> items) {
		super(Channel.class, "channel", "channel.alarm", false);
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

		users = createUsersButton();
		getButtonsLayout().add(users);
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
		return new ChannelAlarmForm(item, readonly);
	}


	private Grid<Channel> createGrid() {
		Grid<Channel> grid = new Grid<>();
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.setSizeFull();


		dataProvider = new ListDataProvider<>(new ArrayList<>());
		grid.setDataProvider(dataProvider);
		setDataProvider(dataProvider);

		List<Grid.Column<Channel>> columns = new ArrayList<>();
		columns.add(grid.addColumn(ChannelUtils::displayName).setKey(ChannelUtils.CHANNEL_LABEL));
		columns.add(grid.addComponentColumn(this::createArmedButton).setKey("alarmed"));
		columns.add(grid.addComponentColumn(this::createNotifyButton).setKey("notify"));
		columns.add(grid.addColumn(channel -> channel.getAlarm().getPriority()).setKey("alarm.priority"));
		columns.add(grid.addColumn(this::formatThresholds).setKey("thresholds"));
		columns.add(grid.addColumn(channel -> channel.getAlarm().getDelayMinutes()).setKey("alarm.delayMinutes"));
		columns.add(grid.addColumn(channel -> channel.getAlarm().getRepeatMinutes()).setKey("alarm.repeatMinutes"));

		for (Grid.Column<Channel> column : columns) {
			column.setSortable(false);
			column.setHeader(getI18nLabel(column.getKey()));
		}
		grid.getColumnByKey("alarmed").setHeader("");
		grid.getColumnByKey("notify").setHeader("");
		grid.setColumnOrder(columns.toArray(new Grid.Column[0]));
		return grid;
	}

	private Button createArmedButton(Channel channel) {
		Button button = new Button();
		button.setIcon(channel.getAlarm().isArmed() ? VaadinIcon.BELL.create() : VaadinIcon.BELL_SLASH.create());
		button.addClassName("small");
		button.setEnabled(channel.getAlarm().isValid());
		if (channel.getAlarm().isFired()) {
			button.addClassName("active-alarm");
		}
		button.addClickListener(event -> {
			channel.getAlarm().setArmed(!channel.getAlarm().isArmed());
			button.setIcon(channel.getAlarm().isArmed() ? VaadinIcon.BELL.create() : VaadinIcon.BELL_SLASH.create());
		});
		return button;
	}

	private Button createNotifyButton(Channel channel) {
		Button button = new Button();
		button.setIcon(channel.getAlarm().isNotify() ? VaadinIcon.MICROPHONE.create() : VaadinIcon.QUESTION.create());
		button.addClassName("small");
		button.setEnabled(channel.getAlarm().isValid());
		button.addClickListener(event -> {
			channel.getAlarm().setNotify(!channel.getAlarm().isNotify());
			button.setIcon(channel.getAlarm().isNotify() ? VaadinIcon.MICROPHONE.create() : VaadinIcon.QUESTION.create());
		});
		return button;
	}

	private String formatThresholds(Channel channel) {
		ChannelAlarm alarm = channel.getAlarm();
		if (alarm == null || alarm.isEmpty()) {
			return "";
		}
		if (ChannelUtils.isTypeAlarm(channel) || ChannelUtils.isTypeDigital(channel)) {
			String direction = alarm.getLowLow() < 0
					? getTranslation("channel.alarm.digital.up")
					: getTranslation("channel.alarm.digital.down");
			return String.format("%s:%s", getTranslation("channel.alarm.digital"), direction);
		}
		DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(UIUtils.getLocale());
		return String.format("%s:%s, %s:%s, %s:%s, %s:%s",
				getTranslation("channel.alarm.lowLow"), decimalFormat.format(alarm.getLowLow()),
				getTranslation("channel.alarm.low"), decimalFormat.format(alarm.getLow()),
				getTranslation("channel.alarm.high"), decimalFormat.format(alarm.getHigh()),
				getTranslation("channel.alarm.highHigh"), decimalFormat.format(alarm.getHighHigh()));
	}

	private VerticalLayout createContent(Grid<Channel> grid) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.add(grid);
		layout.setFlexGrow(1f, grid);
		return layout;
	}

	private Button createUsersButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.USERS.create());
		button.setId("users_button" + ALWAYS_ENABLED_BUTTON);
		button.getElement().setProperty("title", getI18nLabel("alarm.notify_users"));
		button.addClickListener(event -> {
			ChannelAlarmsUsers content = new ChannelAlarmsUsers(entity);
			Dialog dialog = createDialog(getI18nLabel("alarm.notify_users"), content);
			content.addListener(new EditorSavedListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void editorSaved(EditorSavedEvent event) {
					dialog.close();
				}
			});
			dialog.open();
		});
		return button;
	}

	private Button createModifyButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.EDIT.create());
		button.getElement().setProperty("title", getI18nLabel("modify_action"));
		button.addClickListener(event -> openEditor(getCurrentValue()));
		button.setVisible(permissions.isModifyMode());
		return button;
	}

	private void openEditor(Channel item) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityForm<Channel> editor = getEditor(item, false);
		String caption = String.format("%s %s", getI18nLabel("alarm.modify_dialog"),
				item.getConfiguration().getDisplayName());
		Dialog dialog = createDialog(caption, editor);
		editor.setSavedHandler(entity -> {
			dialog.close();
			refresh();
		});
		dialog.open();
	}

	public void setAlarmGroup(NetworkGroup group) {
		entity = group;
		users.setVisible(entity != null);
	}

	public void commitAlarms(Device device) {
		Map<String, Channel> sources = new HashMap<>();
		Map<String, Channel> targets = new HashMap<>();
		boolean alarmed = false;
		for (Channel channel : getChannels()) {
			sources.put(channel.getId(), channel);
		}
		for (Channel channel : device.getChannels()) {
			targets.put(channel.getId(), channel);
		}
		for (String id : sources.keySet()) {
			Channel channel = sources.get(id);
			ChannelAlarm alarm = channel.getAlarm();
			if (!alarm.isEmpty()) {
				if (alarm.getDelayMinutes() == null) {
					alarm.setDelayMinutes(0);
				}
				if (alarm.getRepeatMinutes() == null) {
					alarm.setRepeatMinutes(0);
				}
				if (alarm.getPriority() == null) {
					alarm.setPriority(Priority.LOW);
				}
				if (alarm.isArmed()) {
					alarmed = true;
				}
			}
			targets.get(id).setAlarm(alarm);
		}
		device.setAlarmed(alarmed);
	}

	private List<Channel> getChannels() {
		return new ArrayList<>(dataProvider.getItems());
	}

	@Override
	public void lazyLoad() {
		if (loaded) {
			return;
		}
		List<Channel> alarms = new ArrayList<>();
		for (Channel channel : channels) {
			if (channel.getConfiguration().isActive()) {
				alarms.add(channel);
			}
		}
		dataProvider.getItems().clear();
		dataProvider.getItems().addAll(alarms);
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
}
