package it.thisone.iotter.ui.groupwidgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ForkJoinPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;
import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.shared.Registration;

import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.exporter.ExportConfig;
import it.thisone.iotter.exporter.ExportGroupConfig;
import it.thisone.iotter.exporter.ExportProperties;
import it.thisone.iotter.exporter.IExportable;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GraphicWidgetOptions;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.service.GroupWidgetService;
import it.thisone.iotter.ui.charts.AbstractChartAdapter;
import it.thisone.iotter.ui.charts.controls.GraphicWidgetOptionsField;
import it.thisone.iotter.ui.charts.controls.TimeControl;
import it.thisone.iotter.ui.charts.controls.TimeIntervalField;
import it.thisone.iotter.ui.charts.controls.TimePeriodPopup;
import it.thisone.iotter.ui.common.AbstractWidgetVisualizer;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.common.export.ExportDialog;
import it.thisone.iotter.ui.eventbus.CloseOpenWindowsEvent;
import it.thisone.iotter.ui.eventbus.GraphWidgetOptionsEvent;
import it.thisone.iotter.ui.eventbus.TimeIntervalEvent;
import it.thisone.iotter.ui.eventbus.TimePeriodEvent;
import it.thisone.iotter.ui.eventbus.UIEventBus;
import it.thisone.iotter.ui.eventbus.WidgetRefreshEvent;
import it.thisone.iotter.ui.graphicwidgets.GraphicWidgetFactory;
import it.thisone.iotter.ui.ifc.IGroupWidgetUiFactory;
import it.thisone.iotter.ui.model.TimeInterval;
import it.thisone.iotter.ui.model.TimePeriod;

public class GroupWidgetVisualizer extends BaseComponent {
    public static Logger logger = LoggerFactory.getLogger(GroupWidgetVisualizer.class);

    private static final long serialVersionUID = -6776667672616201904L;
    private static final int TIMECONTROLS_HEIGHT = 40;
    private static final int DEFAULT_CANVAS_WIDTH = 1280;
    private static final int DEFAULT_CANVAS_HEIGHT = 720;

    private final GroupWidgetService groupWidgetService;
    private final UIEventBus uiEventBus;
    private final IGroupWidgetUiFactory config;

    private Registration intervalValueChangeRegistration;
    private Registration periodValueChangeRegistration;
    private Registration optionsValueChangeRegistration;

    private GroupWidget entity;
    private Div mainLayout;
    private Div mainPanel;
    private int canvasHeight;
    private int canvasWidth;
    private int unAvailableHeight;
    private List<AbstractWidgetVisualizer> widgets;
    private TimeZone tz;

    private TimeIntervalField intervalField;
    private Button exportButton;
    private TimePeriodPopup periodField;
    private TimeControl timeControl;
    private CustomField<GraphicWidgetOptions> optionsField;

    public GroupWidgetVisualizer(String entityId, boolean isTab, GroupWidgetService groupWidgetService) {
        super("groupwidget.visualizer");
        this.groupWidgetService = groupWidgetService;
        this.uiEventBus = resolveUiEventBus();
        this.config = new GroupWidgetUiFactory();

        this.entity = groupWidgetService.findOne(entityId);
        if (entity == null) {
            throw new IllegalArgumentException("GroupWidget not found: " + entityId);
        }

        setId(entity.getId());

        tz = entity.getTimeZone();
        if (tz == null) {
            tz = TimeZone.getDefault();
        }

        canvasWidth = canonicalWidth();
        unAvailableHeight = TIMECONTROLS_HEIGHT + (isTab ? TAB_HEIGHT : 0);
        canvasHeight = calculateCanvasHeight();
        widgets = new ArrayList<>();

        mainLayout = new Div();
        mainLayout.getStyle().set("position", "relative");
        mainLayout.setWidth(canvasWidth + "px");
        mainLayout.setHeight(canvasHeight + "px");

        Component content = buildContent(mainLayout);
        setRootComposition(content);
    }

    private UIEventBus resolveUiEventBus() {
        UI ui = UI.getCurrent();
        if (ui == null || ui.getSession() == null) {
            return null;
        }
        return ui.getSession().getAttribute(UIEventBus.class);
    }

    private int canonicalWidth() {
        int value = getUI().map(UI::getInternals)
                .map(internals -> internals.getExtendedClientDetails())
                .map(details -> details.getBodyClientWidth())
                .orElse(0);
        return value > 0 ? value : DEFAULT_CANVAS_WIDTH;
    }

    private int canonicalHeight() {
        int value = getUI().map(UI::getInternals)
                .map(internals -> internals.getExtendedClientDetails())
                .map(details -> details.getBodyClientHeight())
                .orElse(0);
        return value > 0 ? value : DEFAULT_CANVAS_HEIGHT;
    }

    private void addVisualizations() {
        if (!widgets.isEmpty()) {
            return;
        }
        List<GraphicWidget> gwidgets = entity.getWidgets();
        Collections.sort(gwidgets, Comparator.comparing(GraphicWidget::getY));

        for (GraphicWidget widget : gwidgets) {
            if (widget.getParent() != null) {
                continue;
            }
            widget.getOptions().setRealTime(entity.getOptions().isRealTime());
            AbstractWidgetVisualizer component = GraphicWidgetFactory.createWidgetVisualizer(widget);
            if (uiEventBus != null) {
                component.setEventBusFunctions(uiEventBus::register, uiEventBus::unregister);
            }
            widgets.add(component);
            component.setPosition(widgets.size());
        }

        redrawMainLayout();
    }

    private int calculateCanvasHeight() {
        int canonicalHeight = canonicalHeight();
        int actualHeight = canonicalHeight;
        for (GraphicWidget widget : entity.getWidgets()) {
            int height = (int) ((widget.getY() + widget.getHeight()) * canonicalHeight);
            if (height > actualHeight) {
                actualHeight = height;
            }
        }
        return actualHeight + 130;
    }

    private void changeCanvasSize(int canonicalWidth) {
        logger.debug("changeCanvasSize width from {} to {}", canvasWidth, canonicalWidth);
        canvasWidth = canonicalWidth;
        canvasHeight = calculateCanvasHeight();
        redrawMainLayout();
    }

    private void redrawMainLayout() {
        mainLayout.removeAll();
        mainLayout.setWidth(canvasWidth + "px");
        mainLayout.setHeight(canvasHeight + "px");

        for (AbstractWidgetVisualizer component : widgets) {
            GraphicWidget widget = (GraphicWidget) component.getWidget();
            int top = Math.round(widget.getY() * canvasHeight);
            int left = Math.round(widget.getX() * canvasWidth);

            Div child = new Div();
            child.getStyle()
                    .set("position", "absolute")
                    .set("top", top + "px")
                    .set("left", left + "px");

            child.add(component);
            mainLayout.add(child);
        }
    }

    private Component buildContent(Component scrollable) {
        mainPanel = new Div();
        mainPanel.getStyle().set("overflow", "auto");
        mainPanel.setHeight(calculatePanelHeight() + "px");
        mainPanel.addClassName("panel-borderless");
        mainPanel.add(scrollable);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setHeight(TIMECONTROLS_HEIGHT + "px");
        toolbar.setWidthFull();
        toolbar.setPadding(false);
        toolbar.setSpacing(true);
        Component controls = buildTimeControls();
        toolbar.add(controls);
        toolbar.setVerticalComponentAlignment(Alignment.CENTER, controls);
        toolbar.setFlexGrow(1f, controls);

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        content.setSizeFull();
        content.add(toolbar, mainPanel);
        content.setHorizontalComponentAlignment(Alignment.CENTER, toolbar);
        content.setFlexGrow(1f, mainPanel);
        content.addClickListener(event -> postEvent(new CloseOpenWindowsEvent()));
        return content;
    }

    private int calculatePanelHeight() {
        int height = unAvailableHeight + HEADER_HEIGHT + FOOTER_HEIGHT;
        return canonicalHeight() - height;
    }

    private Component buildTimeControls() {
        exportButton = new Button(VaadinIcon.DOWNLOAD.create());
        exportButton.getElement().setProperty("title", getI18nLabel("export"));
        exportButton.addClickListener(event -> {
            ExportDialog dialog = new ExportDialog(createExportConfig(), createExportProperties(), null, ForkJoinPool.commonPool());
            dialog.open();
        });

        intervalField = new TimeIntervalField(tz, config.getPeriods());
        intervalValueChangeRegistration = intervalField.addValueChangeListener(event -> {
            TimeInterval interval = event.getValue();
            if (interval == null) {
                return;
            }
            for (AbstractChartAdapter chart : getCharts()) {
                chart.startDrawing();
            }
            postEvent(new TimeIntervalEvent(entity.getId(), interval));
        });

        periodField = new TimePeriodPopup();
        periodValueChangeRegistration = periodField.addValueChangeListener(event -> {
            TimePeriod period = event.getValue();
            if (period != null) {
                postEvent(new TimePeriodEvent(entity.getId(), period));
            }
        });

        timeControl = new TimeControl(intervalField, periodField, null);
        timeControl.setUiEventBus(uiEventBus);

        optionsField = new GraphicWidgetOptionsField();
        GraphicWidgetOptions options = new GraphicWidgetOptions();
        options.setRealTime(entity.getOptions().isRealTime());
        optionsField.setValue(options);
        timeControl.setEnabled(!options.getRealTime());

        optionsValueChangeRegistration = optionsField.addValueChangeListener(event -> {
            GraphicWidgetOptions value = event.getValue();
            if (value == null) {
                return;
            }
            postEvent(new GraphWidgetOptionsEvent(entity.getId(), value));
            timeControl.setEnabled(!value.getRealTime());
            if (Boolean.TRUE.equals(value.getRealTime())) {
                intervalField.setValue(intervalField.getHelper().movingPeriod(new Date(), periodField.getValue()));
            }
        });

        HorizontalLayout controls = new HorizontalLayout();
        controls.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        controls.addClassName(TIME_BUTTONS_STYLE);
        controls.addClassName(TIME_CONTROLS_STYLE);
        controls.setPadding(false);
        controls.setSpacing(true);

        controls.add(timeControl);
        controls.setFlexGrow(0.8f, timeControl);
        controls.add(exportButton);
        controls.setFlexGrow(0.1f, exportButton);
        controls.add(optionsField);
        controls.setFlexGrow(0.1f, optionsField);
        return controls;
    }

    private List<AbstractChartAdapter> getCharts() {
        List<AbstractChartAdapter> charts = new ArrayList<>();
        for (AbstractWidgetVisualizer widget : widgets) {
            if (widget instanceof AbstractChartAdapter) {
                charts.add((AbstractChartAdapter) widget);
            }
        }
        return charts;
    }

    private ExportProperties createExportProperties() {
        ExportProperties props = config.getExportProperties();
        props.setTimeZone(tz);
        props.setLocale(UI.getCurrent() != null ? UI.getCurrent().getLocale() : java.util.Locale.getDefault());
        return props;
    }

    private ExportGroupConfig createExportConfig() {
        ExportGroupConfig cfg = new ExportGroupConfig();
        cfg.setInterpolation(Interpolation.RAW);

        Range<Date> range = Range.closed(intervalField.getValue().getStartDate(), intervalField.getValue().getEndDate());
        if (optionsField.getValue().getRealTime()) {
            TimePeriod period = config.getDefaultPeriod();
            TimeInterval interval = intervalField.getHelper().movingPeriod(new Date(), period);
            range = Range.closed(interval.getStartDate(), interval.getEndDate());
        }

        cfg.setInterval(range);
        cfg.setName(entity.getName());
        for (AbstractWidgetVisualizer widget : widgets) {
            if (widget instanceof IExportable) {
                ExportConfig single = (ExportConfig) ((IExportable) widget).createExportConfig();
                if (single.getInterval() == null) {
                    single.setInterval(range);
                }
                cfg.getExportConfigs().add(single);
            }
        }
        return cfg;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (uiEventBus != null) {
            uiEventBus.register(this);
        }

        int latestWidth = canonicalWidth();
        if (latestWidth != canvasWidth) {
            changeCanvasSize(latestWidth);
        }

        addVisualizations();
        for (AbstractWidgetVisualizer widget : widgets) {
            if (!widget.isAttached()) {
                widget.register();
            }
        }
        initFields();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (uiEventBus != null) {
            uiEventBus.unregister(this);
        }
        for (AbstractWidgetVisualizer widget : widgets) {
            widget.unregister();
        }
        if (intervalValueChangeRegistration != null) {
            intervalValueChangeRegistration.remove();
            intervalValueChangeRegistration = null;
        }
        if (periodValueChangeRegistration != null) {
            periodValueChangeRegistration.remove();
            periodValueChangeRegistration = null;
        }
        if (optionsValueChangeRegistration != null) {
            optionsValueChangeRegistration.remove();
            optionsValueChangeRegistration = null;
        }
        super.onDetach(detachEvent);
    }

    private void initFields() {
        if (intervalField.getValue() == null) {
            TimePeriod period = config.getDefaultPeriod();
            periodField.setValue(period);
            TimeInterval interval = intervalField.getHelper().movingPeriod(new Date(), period);
            intervalField.setValue(interval);
        }
    }

    @Subscribe
    public void refreshWithUiAccess(WidgetRefreshEvent event) {
        try {
            if (optionsField != null && optionsField.getValue().getRealTime()) {
                getUI().ifPresent(ui -> ui.access(() -> {
                    if (intervalValueChangeRegistration != null) {
                        intervalValueChangeRegistration.remove();
                    }
                    TimeInterval interval = intervalField.getHelper().movingPeriod(new Date(), periodField.getValue());
                    intervalField.setValue(interval);
                    intervalValueChangeRegistration = intervalField.addValueChangeListener(changeEvent -> {
                        TimeInterval value = changeEvent.getValue();
                        if (value == null) {
                            return;
                        }
                        for (AbstractChartAdapter chart : getCharts()) {
                            chart.startDrawing();
                        }
                        postEvent(new TimeIntervalEvent(entity.getId(), value));
                    });
                }));
            }
        } catch (Throwable ignored) {
        }
    }

    private void postEvent(Object event) {
        if (uiEventBus != null) {
            uiEventBus.post(event);
        }
    }
}
