package it.thisone.iotter.ui.visualizers.controlpanel;

import static it.thisone.iotter.ui.graphicwidgets.ControlPanelBaseForm.CONTROLPANELBASE_EDITOR;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import it.thisone.iotter.cassandra.model.FeedAlarm;
import it.thisone.iotter.cassandra.model.IFeedAlarm;
import it.thisone.iotter.integration.CassandraService;
import it.thisone.iotter.mqtt.MqttOutboundService;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.ui.common.ConfirmationDialog;
import it.thisone.iotter.ui.common.ConfirmationDialog.Callback;
import it.thisone.iotter.ui.common.MarkupsUtils;
import it.thisone.iotter.ui.common.charts.ChannelUtils;
import it.thisone.iotter.ui.visualizers.controlpanel.IconSetResolver.IconSet;

public class QuickCommandButton extends Button {

    public interface QuickCommandCallback {
        void onSuccess();
        void onError();
        void beforeCommand();
        boolean checkResult(float value);
    }

    public interface QuickCommandClickCallback {
        void clicked();
    }

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter ALARM_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final IconSet set;
    private final MqttOutboundService mqttOutboundService;
    private final CassandraService cassandraService;
    private String topic;
    private String key;
    private String serial;
    private QuickCommandCallback callback;
    private final QuickCommandUIRunnable runnable;
    private boolean running;

    public QuickCommandButton(IconSet set) {
        this(set, null, null);
    }

    public QuickCommandButton(IconSet set, MqttOutboundService mqttOutboundService) {
        this(set, mqttOutboundService, null);
    }

    public QuickCommandButton(IconSet set, MqttOutboundService mqttOutboundService, CassandraService cassandraService) {
        super();
        this.set = set;
        this.mqttOutboundService = mqttOutboundService;
        this.cassandraService = cassandraService;
        showIcon();
        runnable = new QuickCommandUIRunnable(this);
    }

    public void showIcon() {
        setIcon((Icon) set.createIconValue());
        running = false;
    }

    public void setIconValue(int value) {
        if (set.contains(value)) {
            set.setIconValue(value);
            setIcon((Icon) set.createIconValue());
        }
    }

    public int getNextValue() {
        return set.getNextValue();
    }

    public String getTopic() {
        return topic;
    }

    public String getKey() {
        return key;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public IconSet getSet() {
        return set;
    }

    public boolean isSingleState() {
        return set.getValues().length == 1;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public QuickCommandCallback getCallback() {
        return callback;
    }

    public void setCallback(QuickCommandCallback resultCallback) {
        this.callback = resultCallback;
    }

    public boolean isRunning() {
        return running;
    }

    public QuickCommandUIRunnable getRunnable() {
        return runnable;
    }

    public MqttOutboundService getMqttOutboundService() {
        return mqttOutboundService;
    }

    public CassandraService getCassandraService() {
        return cassandraService;
    }

    public String getI18nLabel(String key) {
        return getTranslation(CONTROLPANELBASE_EDITOR + "." + key);
    }

    public static QuickCommandClickCallback createClickCallback(final QuickCommandButton button) {
        return () -> {
            button.setIcon(new Icon(VaadinIcon.REFRESH));
            button.running = true;
            button.setEnabled(false);
            UI ui = button.getUI().orElse(null);
            CompletableFuture.runAsync(button.getRunnable()::runInBackground)
                    .whenComplete((v, ex) -> {
                        if (ui != null) {
                            ui.access(() -> button.getRunnable().runInUI(ex));
                        } else {
                            button.getRunnable().runInUI(ex);
                        }
                    });
        };
    }

    public static ComponentEventListener<ClickEvent<Button>> createClickListener(final QuickCommandClickCallback cb) {
        return event -> cb.clicked();
    }

    public ComponentEventListener<ClickEvent<Button>> buildResetAlarmsClickListener(final Device device,
            final Callback callback) {
        return createResetAlarmsClickListener(device, callback,
                key -> getI18nLabel(key),
                () -> listActiveAlarmEntries(device, cassandraService));
    }

    public static ComponentEventListener<ClickEvent<Button>> createResetAlarmsClickListener(final Device device,
            final Callback callback) {
        return createResetAlarmsClickListener(device, callback, key -> key, ArrayList::new);
    }

    public static ComponentEventListener<ClickEvent<Button>> createResetAlarmsClickListener(final Device device,
            final Callback callback,
            final Function<String, String> i18nProvider,
            final Supplier<List<String>> alarmsSupplier) {
        return event -> {
            String caption = i18nProvider.apply("quick_command_dialog");
            String warning = MarkupsUtils.color(i18nProvider.apply("quick_command_warning"), "#FF8000");
            String title = String.format("%s %s", VaadinIcon.BELL_SLASH.create().getElement().getOuterHTML(),
                    i18nProvider.apply("no_alarms"));
            String content = "";
            List<String> alarms = alarmsSupplier.get();
            if (!alarms.isEmpty()) {
                title = String.format("%s %s", VaadinIcon.BELL.create().getElement().getOuterHTML(),
                        i18nProvider.apply("fired_alarms"));
                content = MarkupsUtils.simpleTable(alarms);
            }
            String message = String.format("<b>%s</b><br/>%s<br/>%s", warning, title, content);
            Dialog dialog = new ConfirmationDialog(caption, message, callback);
            dialog.open();
        };
    }

    public static List<String> listActiveAlarmEntries(Device device, CassandraService cassandraService) {
        List<String> entries = new ArrayList<>();
        if (device == null || cassandraService == null) {
            return entries;
        }

        Map<String, Channel> channels = new HashMap<>();
        for (Channel channel : device.getChannels()) {
            if (channel.getAlarm().isArmed()) {
                channels.put(channel.getKey(), channel);
            }
        }

        List<FeedAlarm> alarms = cassandraService.getAlarms().findActiveAlarms(device.getSerial());
        for (IFeedAlarm alarm : alarms) {
            Channel channel = channels.get(alarm.getKey());
            if (channel == null) {
                continue;
            }
            String timestamp = "-";
            if (alarm.getTimestamp() != null) {
                timestamp = ALARM_DATE_FORMAT.format(alarm.getTimestamp().toInstant().atZone(ZoneId.systemDefault()));
            }
            String entry = String.format("%s %s", timestamp, ChannelUtils.displayName(channel));
            entries.add(entry);
        }
        return entries;
    }
}
