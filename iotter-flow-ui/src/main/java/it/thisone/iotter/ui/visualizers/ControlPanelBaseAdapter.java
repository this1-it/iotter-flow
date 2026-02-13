package it.thisone.iotter.ui.visualizers;

import static it.thisone.iotter.ui.graphicwidgets.ControlPanelBaseForm.CONTROLPANELBASE_EDITOR;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.enums.ChartScaleType;
import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.exporter.ExportConfig;
import it.thisone.iotter.exporter.IExportable;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.ui.channels.ChannelRemoteControlForm;
import it.thisone.iotter.ui.common.AbstractWidgetVisualizer;
import it.thisone.iotter.ui.common.ConfirmationDialog.Callback;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.WidgetRefreshUIRunnable;
import it.thisone.iotter.ui.common.charts.ChannelUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.common.charts.TimeIntervalHelper;
import it.thisone.iotter.ui.eventbus.WidgetRefreshEvent;
import it.thisone.iotter.ui.graphicwidgets.ControlPanelBaseConstants;
import it.thisone.iotter.ui.groupwidgets.GroupWidgetUiFactory;
import it.thisone.iotter.ui.model.ChannelAdapter;
import it.thisone.iotter.ui.model.ChannelAdapterDataProvider;
import it.thisone.iotter.ui.model.TimeInterval;
import it.thisone.iotter.ui.model.TimePeriod;
import it.thisone.iotter.ui.providers.ControlPanelBaseProvider;
import it.thisone.iotter.ui.visualizers.controlpanel.ConfiguratorLabel;
import it.thisone.iotter.ui.visualizers.controlpanel.ControlPanelBaseDataProvider;
import it.thisone.iotter.ui.visualizers.controlpanel.IconSetResolver;
import it.thisone.iotter.ui.visualizers.controlpanel.IconSetResolver.IconSet;
import it.thisone.iotter.ui.visualizers.controlpanel.ParameterAdapterListing;
import it.thisone.iotter.ui.visualizers.controlpanel.QuickCommandButton;
import it.thisone.iotter.ui.visualizers.controlpanel.QuickCommandButton.QuickCommandCallback;
import it.thisone.iotter.ui.visualizers.controlpanel.QuickCommandButton.QuickCommandClickCallback;
import it.thisone.iotter.ui.visualizers.controlpanel.SetpointButton;

public class ControlPanelBaseAdapter extends AbstractWidgetVisualizer
        implements IExportable, ControlPanelBaseConstants {

    private static final String COLOR_ORANGE = "#FF8000";
    private static final String COLOR_AZURE = "#00CDFF";
    private static final String CONTROLPANEL_SECTION = "controlpanel-section";
    private static final String CONTROLPANEL_SMALL_LABEL = "controlpanel-small-label";
    private static final String CONTROLPANEL_LABEL = "controlpanel-label";
    private static final String CONTROLPANEL_CONFIGURATOR = "controlpanel-configurator";
    private static final String CONTROLPANEL_CONTAINER = "controlpanel-container";
    private static final String CONTROLPANEL_OFFLINE = "controlpanel-offline";

    private static final long serialVersionUID = -3358685726101702654L;

    private TandemTraceChartAdapter multitrace;
    private WidgetRefreshUIRunnable runnable;
    private CompletableFuture<Void> future;

    private ConfiguratorLabel configurator;

    private QuickCommandButton quick1;
    private QuickCommandButton quick2;
    private QuickCommandButton resetAlarms;

    // TODO(flow-migration): Vaadin 8 Window replaced with Dialog; size/style behavior may differ.
    private Dialog dialog;
    private ChannelRemoteControlForm control;
    private Device device;
    private SimpleDateFormat sdf;

    private IconSetResolver resolver;
    private ControlPanelBaseDataProvider feedContainer;
    private ChannelAdapterDataProvider channelContainer;
    private ChannelAdapterDataProvider parameterContainer;

    private List<Component> adapters = new ArrayList<>();

    private String[] colors = new String[] {
            "#7798BF",
            "#FF00FF",
            "#FF9900",
            "#32CD32",
            "#3399CC",
            "#9370DB",
            "#FF6600",
            "#006400",
            "#3366CC",
            "#CC33FF",
            "#F08080",
            "#339900",
            "#FFFF00",
            "#CC00FF"
    };

    private boolean anonymous;

    private static String[] values = new String[] {
            "#98DF58",
            "#3090F0",
            "#EC6464",
            "#F9DD51",
            "#24DCD4",
            "#EC64A5",
            "#FF7D42",
            "#00FFFF",
            "#00FF00",
            "#7FB17F",
            "#FFE0C1",
            "#FFFF00",
            "#B966B9",
            "#FF0000",
            "#AA514D",
            "#7FB053",
            "#BBA85B",
            "#247981",
            "#963970",
            "#4B56A8",
            "#9A593D",
            "#336190",
            "#685CB0",
    };

    public ControlPanelBaseAdapter(GraphicWidget widget) {
        super(widget);
        anonymous = !UIUtils.getUserDetails().isEnabled();
        resolver = new IconSetResolver();
        widget.getOptions().setRealTime(widget.getGroupWidget().getOptions().isRealTime());
        TimeZone tz = widget.getGroupWidget().getTimeZone();
        if (tz == null) {
            tz = TimeZone.getDefault();
        }
        sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss ZZZ");
        sdf.setTimeZone(tz);
        if (widget.getDevice() == null) {
            widget.setDevice(widget.getGroupWidget().getDevice());
        }

        device = UIUtils.getServiceFactory().getDeviceService().findBySerial(widget.getDevice());

        materializeFeeds();
        feedContainer = new ControlPanelBaseDataProvider();
        feedContainer.addFeeds(widget.getFeeds());

        channelContainer = new ChannelAdapterDataProvider();
        channelContainer.addFeeds(widget.getFeeds());
        parameterContainer = new ChannelAdapterDataProvider();
        parameterContainer.addFeeds(this.getParameters());
        runnable = new WidgetRefreshUIRunnable(ControlPanelBaseAdapter.this);

        Component visualization = buildVisualization();
        setRootComposition(visualization);
        drawValues();
    }

    private void materializeFeeds() {
        if (device == null) {
            return;
        }

        Map<String, Channel> map = new HashMap<>();
        for (Channel chnl : device.getChannels()) {
            if (chnl.getMetaIdentifier() != null && chnl.getConfiguration().isActive()) {
                map.put(chnl.getMetaIdentifier(), chnl);
            }
        }

        for (GraphicFeed feed : getGraphWidget().getFeeds()) {
            Channel chnl = map.get(feed.getMetaIdentifier());
            if (chnl != null) {
                feed.setChannel(chnl);
                feed.setMeasure(chnl.getDefaultMeasure());
            }
        }
    }

    @Override
    public boolean refresh() {
        return true;
    }

    protected boolean isRealTime() {
        return (getGraphWidget() != null) && getGraphWidget().getOptions().getRealTime();
    }

    @Override
    @Subscribe
    public void refreshWithUiAccess(WidgetRefreshEvent event) {
        if (isRealTime()) {
            if (future != null) {
                future.cancel(true);
            }
            UI ui = getUI().orElse(UI.getCurrent());
            future = CompletableFuture.runAsync(runnable::runInBackground)
                    .whenComplete((v, ex) -> {
                        if (ui != null) {
                            ui.access(() -> runnable.runInUI(ex));
                        } else {
                            runnable.runInUI(ex);
                        }
                    });
        }
    }

    public String getI18nLabel(String key) {
        return getTranslation(getI18nKey() + "." + key);
    }

    public String getI18nKey() {
        return CONTROLPANELBASE_EDITOR;
    }

    @Override
    protected Component buildVisualization() {
        Component charts = buildCharts();
        Component tabs = buildTabs();
        if (UIUtils.isMobile()) {
            VerticalLayout content = new VerticalLayout();
            content.addClassName("controlpanel-margins");
            content.setPadding(true);
            content.setSpacing(true);
            content.setSizeFull();
            content.add(tabs, charts);
            return content;
        }
        HorizontalLayout container = new HorizontalLayout();
        container.setPadding(false);
        container.addClassName(CONTROLPANEL_CONTAINER);
        container.setSizeFull();
        container.add(tabs, charts);
        container.setFlexGrow(3f, tabs);
        container.setFlexGrow(7f, charts);
        return container;
    }

    private void drawValues() {
        if (device == null) {
            return;
        }

        if (!anonymous) {
            long count = UIUtils.getCassandraService().getAlarms().countActiveAlarms(device.getSerial());
            resetAlarms.setEnabled(count > 0);
        }

        Date lastContactDate = UIUtils.getCassandraService().getFeeds().getLastContact(device.getSerial());
        device.setLastContactDate(lastContactDate);
        if (device.checkInactive(lastContactDate)) {
            getContent().addClassName(CONTROLPANEL_OFFLINE);
        } else {
            getContent().removeClassName(CONTROLPANEL_OFFLINE);
        }
        channelContainer.refresh();
        parameterContainer.refresh();

        ChannelAdapter item = null;
        for (Component component : adapters) {
            if (component instanceof ParameterAdapterListing) {
                // TODO(flow-migration): Vaadin 8 grid editor disable has no direct Flow equivalent here.
            } else if (component instanceof QuickCommandButton) {
                QuickCommandButton quick = (QuickCommandButton) component;
                item = channelContainer.getAdapter(quick.getKey());
                if (item != null && !quick.isRunning()) {
                    Float value = item.getLastMeasure();
                    if (value != null) {
                        quick.setIconValue(value.intValue());
                    }
                }
            } else if (component instanceof SetpointButton) {
                SetpointButton setpoint = (SetpointButton) component;
                item = channelContainer.getAdapter(setpoint.getKey());
                if (item != null) {
                    String unit = item.getMeasureUnit();
                    String number = item.getLastMeasureValue();
                    setpoint.setMeasure(number, unit);
                }
            } else if (component instanceof ConfiguratorLabel) {
                ConfiguratorLabel label = (ConfiguratorLabel) component;
                StringBuffer sb = new StringBuffer();
                for (String key : label.getKeys()) {
                    try {
                        item = channelContainer.getAdapter(key);
                        char ascii = (char) item.getLastMeasure().intValue();
                        if (ConfiguratorLabel.isValidAscii(ascii)) {
                            sb.append(ascii);
                        } else {
                            sb.append("?");
                        }
                    } catch (Exception e) {
                        sb.append("?");
                    }
                }
                label.setText(sb.toString());
            }
        }
    }

    protected GraphicWidget getGraphWidget() {
        return (GraphicWidget) getWidget();
    }

    public Span buildLabel(String caption) {
        Span label = new Span(caption);
        if (caption.length() > 36) {
            label.addClassName(CONTROLPANEL_SMALL_LABEL);
        } else {
            label.addClassName(CONTROLPANEL_LABEL);
        }
        return label;
    }

    private Component buildTabs() {
        Component summary = buildSummary();
        Component setpoints = buildSetpoints();
        if (UIUtils.isMobile()) {
            VerticalLayout vlayout = new VerticalLayout();
            vlayout.setPadding(false);
            vlayout.setSpacing(true);
            vlayout.setSizeFull();
            vlayout.add(summary, setpoints);
            return vlayout;
        }

        // TODO(flow-migration): TabSheet replaced with Tabs + manual content switching.
        Tab summaryTab = new Tab(getI18nLabel("summary"));
        Tab setpointsTab = new Tab(getI18nLabel("setpoints"));
        Tabs tabs = new Tabs(summaryTab, setpointsTab);

        Div pages = new Div(summary, setpoints);
        pages.setSizeFull();
        setpoints.setVisible(false);

        Map<Tab, Component> tabToPage = new HashMap<>();
        tabToPage.put(summaryTab, summary);
        tabToPage.put(setpointsTab, setpoints);

        tabs.addSelectedChangeListener(event -> {
            tabToPage.values().forEach(page -> page.setVisible(false));
            Component selected = tabToPage.get(tabs.getSelectedTab());
            if (selected != null) {
                selected.setVisible(true);
            }
        });

        VerticalLayout multi = new VerticalLayout(tabs, pages);
        multi.setPadding(false);
        multi.setSpacing(false);
        multi.setSizeFull();
        multi.setFlexGrow(1f, pages);
        return multi;
    }

    private Component buildParameters() {
        ParameterAdapterListing parameters = new ParameterAdapterListing(parameterContainer, multitrace);
        parameters.setHeightFull();
        adapters.add(parameters);

        if (UIUtils.isMobile()) {
            VerticalLayout content = new VerticalLayout();
            content.addClassName("controlpanel-margins");
            content.setPadding(true);
            content.setSpacing(false);
            content.setSizeFull();
            content.add(parameters);
            content.setFlexGrow(1f, parameters);
            return content;
        }
        return parameters;
    }

    private Component buildSetpoints() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.addClassName(CONTROLPANEL_SECTION);

        // TODO(flow-migration): GridLayout removed; replaced with FormLayout 2-column structure.
        FormLayout grid = new FormLayout();
        grid.setWidthFull();
        grid.setResponsiveSteps(new ResponsiveStep("0", 2));

        String[] scolors = new String[8];
        Arrays.fill(scolors, "#ffff00");
        scolors[0] = COLOR_AZURE;
        scolors[1] = COLOR_ORANGE;

        for (int pos = 1; pos <= SETPOINT_POS; pos++) {
            GraphicFeed feed = getGraphicFeed("setpoint", pos, scolors[pos - 1]);
            SetpointButton setpoint = new SetpointButton(feed);
            setpoint.setValue(getI18nLabel("not_available"));
            setpoint.setWidthFull();

            setpoint.setEnabled(!anonymous);

            if (feed.getChannel() != null && feed.getChannel().getRemote().getTopic() != null) {
                adapters.add(setpoint);
                setpoint.addClickListener(openChannelRemoteControl(setpoint, feed.getChannel()));
            } else {
                setpoint.setEnabled(false);
            }

            grid.add(buildLabel(feed.getLabel()), setpoint);
        }

        layout.add(grid);
        return layout;
    }

    private Component buildCommands() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.addClassName(CONTROLPANEL_SECTION);
        layout.setPadding(true);

        GraphicFeed feed4 = getGraphicFeed(QUICKCOM, 1, colors[0]);
        GraphicFeed feed5 = getGraphicFeed(QUICKCOM, 2, colors[0]);
        GraphicFeed feed6 = getGraphicFeed(RESET, 1, colors[0]);

        Span feedLabel4 = buildLabel(feed4.getLabel());
        Span feedLabel5 = buildLabel(feed5.getLabel());
        Span feedLabel6 = buildLabel(feed6.getLabel());

        quick1 = buildQuickCommand(feed4);
        if (quick1.isEnabled()) {
            QuickCommandClickCallback cb = QuickCommandButton.createClickCallback(quick1);
            quick1.addClickListener(QuickCommandButton.createClickListener(cb));
        }

        quick2 = buildQuickCommand(feed5);
        if (quick2.isEnabled()) {
            QuickCommandClickCallback cb = QuickCommandButton.createClickCallback(quick2);
            quick2.addClickListener(QuickCommandButton.createClickListener(cb));
        }

        resetAlarms = buildQuickCommand(feed6);
        Callback callback = result -> {
            if (result) {
                resetAlarms.setIcon(IconSetResolver.LOADER.create());
                resetAlarms.setEnabled(false);
                QuickCommandClickCallback cb = QuickCommandButton.createClickCallback(resetAlarms);
                cb.clicked();
            }
        };
        resetAlarms.addClickListener(resetAlarms.buildResetAlarmsClickListener(device, callback));

        // TODO(flow-migration): GridLayout removed; replaced with FormLayout 2-column structure.
        FormLayout grid = new FormLayout();
        grid.setWidthFull();
        grid.setResponsiveSteps(new ResponsiveStep("0", 2));
        grid.add(feedLabel4, quick1, feedLabel5, quick2, feedLabel6, resetAlarms);

        layout.add(grid);

        adapters.add(quick1);
        adapters.add(quick2);
        adapters.add(resetAlarms);

        return layout;
    }

    private Component buildCharts() {
        return buildMultiTraceChart();
    }

    private Component buildSummary() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);

        Component configuratorComponent = buildConfigurator();
        if (configuratorComponent != null) {
            layout.add(configuratorComponent);
        }

        Component parameters = createContentWrapper("parameters", buildParameters());
        layout.add(parameters);
        Component commands = createContentWrapper("commands", buildCommands());
        layout.add(commands);
        layout.setFlexGrow(0.7f, parameters);
        layout.setFlexGrow(0.3f, commands);

        return layout;
    }

    private Component buildMultiTraceChart() {
        GraphicWidget widget = new GraphicWidget();
        widget.setId(getGraphWidget().getId());
        widget.setGroupWidget(getGraphWidget().getGroupWidget());
        widget.setProvider(ControlPanelBaseProvider.CONTROLPANEL);
        widget.setType(GraphicWidgetType.CUSTOM);
        widget.getOptions().setExporting(false);
        widget.getOptions().setRealTime(getGraphWidget().getOptions().getRealTime());
        widget.setLabel(getGraphWidget().getLabel());
        widget.getOptions().setScale(ChartScaleType.LINEAR);
        widget.getOptions().setShowGrid(false);
        widget.getOptions().setShowLegend(false);
        widget.getFeeds().addAll(getParameters());

        multitrace = new TandemTraceChartAdapter(widget);
        TimeIntervalHelper helper = new TimeIntervalHelper(multitrace.getNetworkTimeZone());
        TimePeriod period = new GroupWidgetUiFactory().getDefaultPeriod();
        TimeInterval interval = helper.period(new Date(), period);
        multitrace.setTimePeriod(period);
        multitrace.setTimeInterval(interval);
        multitrace.register();
        return multitrace.getContent();
    }

    private QuickCommandButton buildQuickCommand(GraphicFeed feed) {
        IconSet set = resolver.resolveIconSetName(feed.getResourceID());
        final QuickCommandButton button = new QuickCommandButton(set);
        button.addClassName("controlpanel-setpoint");
        if (feed.getChannel() != null && feed.getChannel().getRemote().getTopic() != null) {
            button.setTopic(feed.getChannel().getRemote().getTopic());
            button.setKey(feed.getChannel().getKey());
            button.setSerial(feed.getChannel().getDevice().getSerial());
            button.setEnabled(!anonymous);

            if (feed.getSection().startsWith(RESET.toLowerCase())) {
                button.setCallback(new QuickCommandCallback() {
                    @Override
                    public boolean checkResult(float value) {
                        long count = UIUtils.getCassandraService().getAlarms().countActiveAlarms(button.getSerial());
                        return count == 0;
                    }

                    @Override
                    public void beforeCommand() {
                        UIUtils.getServiceFactory().getAlarmService().notifyAlarmReset(button.getSerial(),
                                UIUtils.getUserDetails().getUsername());
                    }

                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                    }
                });
            } else {
                button.setCallback(new QuickCommandCallback() {
                    @Override
                    public boolean checkResult(float value) {
                        FeedKey feedKey = new FeedKey(button.getSerial(), button.getKey());
                        MeasureRaw measure = ChartUtils.lastMeasure(feedKey);
                        return measure != null && measure.getValue() != null && measure.getValue().equals(value);
                    }

                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void beforeCommand() {
                    }

                    @Override
                    public void onError() {
                    }
                });
            }

            button.setEnabled(!anonymous);
        } else {
            button.setEnabled(false);
        }
        button.setWidthFull();
        button.setHeight("30px");
        return button;
    }

    private List<GraphicFeed> getParameters() {
        List<GraphicFeed> feeds = new ArrayList<>();
        List<GraphicFeed> sectionFeeds = getSectionFeeds(SERIES);
        if (sectionFeeds.isEmpty()) {
            return feeds;
        }
        for (int pos = 0; pos < sectionFeeds.size(); pos++) {
            GraphicFeed feed = sectionFeeds.get(pos);
            String displayName = ChannelUtils.displayName(feed);
            if (displayName != null && !displayName.isEmpty()) {
                feed.setLabel(displayName);
            }
            feed.getOptions().setFillColor(values[pos]);
            feed.getOptions().setAxisTitle(false);
            feeds.add(feed);
        }
        return feeds;
    }

    private GraphicFeed getGraphicFeed(String section, int pos, String color) {
        GraphicFeed feed = new GraphicFeed();
        feed.setLabel(getI18nLabel(section) + " " + pos);
        feed.setSection(section);
        pos--;
        List<GraphicFeed> sectionFeeds = getSectionFeeds(section);
        if (pos < sectionFeeds.size()) {
            feed = sectionFeeds.get(pos);
            String displayName = ChannelUtils.displayName(feed);
            if (displayName != null) {
                feed.setLabel(displayName);
            }
        }
        feed.getOptions().setFillColor(color);
        return feed;
    }

    private ConfiguratorLabel buildConfiguratorLabel() {
        List<GraphicFeed> asciiFeeds = getSectionFeeds(ASCII);
        if (asciiFeeds.isEmpty()) {
            return null;
        }
        List<String> keys = new ArrayList<>();
        for (GraphicFeed feed : asciiFeeds) {
            keys.add(feed.getKey());
        }
        return new ConfiguratorLabel(keys);
    }

    private List<GraphicFeed> getSectionFeeds(String section) {
        return feedContainer.getFeeds().stream()
                .filter(feed -> section.equals(feed.getSection()))
                .collect(Collectors.toList());
    }

    private Component buildConfigurator() {
        configurator = buildConfiguratorLabel();
        if (configurator != null) {
            adapters.add(configurator);
            HorizontalLayout h1 = new HorizontalLayout();
            h1.addClassName(CONTROLPANEL_CONFIGURATOR);
            h1.setWidthFull();
            h1.setJustifyContentMode(HorizontalLayout.JustifyContentMode.CENTER);
            h1.setDefaultVerticalComponentAlignment(Alignment.CENTER);
            h1.add(configurator);

            HorizontalLayout h2 = new HorizontalLayout();
            h2.addClassName("controlpanel-configurator-outer");
            h2.setWidthFull();
            h2.add(h1);
            return h2;
        }
        return null;
    }

    @Override
    public void draw() {
        drawValues();
    }

    @Override
    public void unregister() {
        if (multitrace != null) {
            multitrace.unregister();
        }
        super.unregister();
    }

    @Override
    public void register() {
        if (multitrace != null) {
            multitrace.register();
        }
        super.register();
    }

    @Override
    public ExportConfig createExportConfig() {
        ExportConfig config = new ExportConfig();
        config.setName(getWidget().getLabel());
        List<GraphicFeed> feeds = ((GraphicWidget) getWidget()).getFeeds();
        config.setFeeds(ChartUtils.createExportFeeds(feeds));
        config.setInterpolation(Interpolation.RAW);
        return config;
    }

    private ComponentEventListener<ClickEvent<Button>> openChannelRemoteControl(final SetpointButton setpoint,
            final Channel channel) {
        return event -> {
            control = new ChannelRemoteControlForm(channel);
            String caption = String.format("%s %s", getI18nLabel("control_setpoint"),
                    channel.getConfiguration().getDisplayName());

            dialog = createDialog(caption, control);
            control.setSavedHandler(entity -> {
                ChannelAdapter adapter = channelContainer.getAdapter(setpoint.getKey());
                if (adapter != null) {
                    MeasureRaw measure = ChartUtils.lastMeasure(adapter);
                    if (measure != null) {
                        channelContainer.refresh(adapter, measure);
                        String unit = adapter.getMeasureUnit();
                        String number = adapter.getLastMeasureValue();
                        setpoint.setMeasure(number, unit);
                    }
                }
                dialog.close();
            });
            dialog.open();
        };
    }

    protected VerticalLayout createContentWrapper(String key, Component content) {
        Span title = new Span(getI18nLabel(key));
        title.addClassName("controlpanel-title");

        HorizontalLayout header = new HorizontalLayout(title);
        header.setWidthFull();
        header.setJustifyContentMode(HorizontalLayout.JustifyContentMode.CENTER);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        VerticalLayout topLayout = new VerticalLayout(header);
        topLayout.setSpacing(true);
        topLayout.setPadding(false);
        topLayout.setWidthFull();
        topLayout.setAlignItems(Alignment.CENTER);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);

        mainLayout.add(topLayout, content);
        mainLayout.setFlexGrow(1, content);
        mainLayout.setHorizontalComponentAlignment(Alignment.CENTER, topLayout, content);

        return mainLayout;
    }
}
