package it.thisone.iotter.ui.graphicfeeds;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;

import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChartPlotOptions;
import it.thisone.iotter.persistence.model.ChartThreshold;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.MeasureRange;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.security.Permissions;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.common.ConfirmationDialog;
import it.thisone.iotter.ui.common.EditorSelectedListener;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChannelUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.common.fields.ChannelAcceptor;
import it.thisone.iotter.ui.common.fields.ChartPlotOptionsField;
import it.thisone.iotter.ui.eventbus.GraphWidgetParamsEvent;
import it.thisone.iotter.util.EncryptUtils;


public class GraphicFeedListing extends BaseComponent {

    private static final long serialVersionUID = 6629896864484892844L;

    private static final String ADD_BUTTON = "add";
    private static final String MODIFY_BUTTON = "edit";
    private static final String REMOVE_BUTTON = "remove";

    private GraphicWidget graph;
    private ChannelAcceptor acceptor;
    private Grid<GraphicFeed> grid;
    private ListDataProvider<GraphicFeed> dataProvider;
    private final List<GraphicFeed> feeds = new ArrayList<>();
    private final Map<String, Grid.Column<GraphicFeed>> columns = new LinkedHashMap<>();
    private final NumberFormat nf;
    private int index = 0;
    private Span title;
    private int maxSize = 0;
    private final Permissions permissions;

    private Button up;
    private Button down;
    private Button modifyButton;
    private Button removeButton;
    private Button infoButton;

    public GraphicFeedListing() {
        super("graphfeed.listing");
        permissions = new Permissions(true);
        nf = NumberFormat.getNumberInstance(UIUtils.getLocale());
        nf.setGroupingUsed(false);
        buildLayout();
    }

    private void buildLayout() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(false);
        mainLayout.setPadding(true);

        grid = createGrid();

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setSpacing(true);
        toolbar.setPadding(true);
        toolbar.addClassName(UIUtils.TOOLBAR_STYLE);

        title = new Span();
        toolbar.add(title);
        toolbar.setVerticalComponentAlignment(Alignment.CENTER, title);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        up = createUpButton();
        down = createDownButton();
        removeButton = createRemoveButton();
        modifyButton = createModifyButton();
        infoButton = createInfoButton();

        buttonsLayout.add(removeButton, modifyButton, infoButton, up, down, createAddButton());
        toolbar.add(buttonsLayout);
        toolbar.expand(title);

        enableButtons(null);

        mainLayout.add(toolbar, grid);
        mainLayout.setFlexGrow(1f, grid);
        setRootComposition(mainLayout);

        grid.addSelectionListener(event -> enableButtons(getCurrentValue()));
    }

    public void setTitle(String content) {
        title.setText(content);
    }

    public Button createAddButton() {
        Button button = new Button(VaadinIcon.PLUS.create());
        button.getElement().setProperty("title", getI18nLabel(ADD_BUTTON));
        button.setId(ADD_BUTTON + getId());
        button.addClickListener(event -> openChoice(true, null));
        button.setVisible(permissions.isCreateMode());
        return button;
    }

    public Button createInfoButton() {
        Button button = new Button(VaadinIcon.QUESTION_CIRCLE.create());
        button.setId("info_action");
        button.getElement().setProperty("title", getI18nLabel("info_action"));
        button.addClickListener(event -> {
            GraphicFeed selected = getCurrentValue();
            if (selected != null) {
                openChoice(false, selected.getLabel());
            }
        });
        button.setVisible(permissions.isModifyMode());
        return button;
    }

    public Button createModifyButton() {
        Button button = new Button(VaadinIcon.EDIT.create());
        button.setId(MODIFY_BUTTON + getId());
        button.getElement().setProperty("title", getI18nLabel("modify_action"));
        button.addClickListener(event -> openEditor(getCurrentValue(), getI18nLabel("modify_dialog")));
        button.setVisible(permissions.isModifyMode());
        return button;
    }

    public Button createRemoveButton() {
        Button button = new Button(VaadinIcon.TRASH.create());
        button.setId(REMOVE_BUTTON + getId());
        button.getElement().setProperty("title", getI18nLabel("remove_action"));
        button.addClickListener(event -> openRemove(getCurrentValue()));
        button.setVisible(permissions.isRemoveMode());
        return button;
    }

    public Button createUpButton() {
        Button button = new Button(VaadinIcon.ARROW_UP.create());
        button.setId("up_action");
        button.getElement().setProperty("title", getI18nLabel("up_action"));
        button.addClickListener(event -> {
            GraphicFeed selected = getCurrentValue();
            if (moveItem(selected, -1)) {
                grid.select(selected);
            } else {
                grid.deselectAll();
            }
        });
        button.setVisible(permissions.isModifyMode());
        return button;
    }

    public Button createDownButton() {
        Button button = new Button(VaadinIcon.ARROW_DOWN.create());
        button.setId("down_action");
        button.getElement().setProperty("title", getI18nLabel("down_action"));
        button.addClickListener(event -> {
            GraphicFeed selected = getCurrentValue();
            if (moveItem(selected, 1)) {
                grid.select(selected);
            } else {
                grid.deselectAll();
            }
        });
        button.setVisible(permissions.isModifyMode());
        return button;
    }

    public void setFeeds(List<GraphicFeed> newFeeds) {
        feeds.clear();
        if (newFeeds != null) {
            for (GraphicFeed feed : newFeeds) {
                if (feed.getId() == null) {
                    feed.setId(EncryptUtils.getUniqueId());
                }
                feeds.add(feed);
            }
        }
        dataProvider.refreshAll();
    }

    public List<GraphicFeed> getFeeds() {
        return new ArrayList<>(feeds);
    }

    public Grid<GraphicFeed> getSelectable() {
        return grid;
    }

    public void setVisibleColumns(String... columnIds) {
        for (Map.Entry<String, Grid.Column<GraphicFeed>> entry : columns.entrySet()) {
            entry.getValue().setVisible(false);
        }
        for (String columnId : columnIds) {
            Grid.Column<GraphicFeed> column = columns.get(columnId);
            if (column != null) {
                column.setVisible(true);
            }
        }
    }

    public void setColumnHeader(String columnId, String caption) {
        Grid.Column<GraphicFeed> column = columns.get(columnId);
        if (column != null) {
            column.setHeader(caption);
        }
    }

    public void setGraphicWidget(GraphicWidget graph, List<GraphicFeed> feeds, ChannelAcceptor acceptor) {
        this.graph = graph;
        this.acceptor = acceptor;
        setVisibleColumns("device", "displayName", "measure");
        setFeeds(feeds);
    }

    public GraphicWidget getGraph() {
        return graph;
    }

    public ChannelAcceptor getAcceptor() {
        return acceptor;
    }

    @Subscribe
    public void paramsChoice(GraphWidgetParamsEvent event) {
        if (getGraph() != null && getGraph().getId().equals(event.getParent())) {
            openChoice(true, null);
        }
    }

    @SuppressWarnings("unchecked")
    private void openChoice(boolean editable, String text) {
        List<GraphicFeed> currentFeeds = getFeeds();
        GraphicFeedChoice content = new GraphicFeedChoice(getGraph(), currentFeeds, getMaxSize(), editable, text, getAcceptor());

        String caption = editable ? getI18nLabel("add_params") : text;
        Dialog dialog = createDialog(caption, content);
        dialog.addThemeName("side-drawer-fullscreen");

        content.addListener(event -> {
            dialog.close();
            if (event.getSelected() != null) {
                Set<GraphicFeed> items = (Set<GraphicFeed>) event.getSelected();
                for (GraphicFeed item : items) {
                    if (!feeds.contains(item)) {
                        feeds.add(item);
                    }
                }
                dataProvider.refreshAll();
            }
        });

        UI.getCurrent().add(dialog);
        dialog.open();
    }

    private void openEditor(GraphicFeed feed, String label) {
        if (feed == null) {
            return;
        }

        if (feed.getId() == null) {
            feed.setId(EncryptUtils.getUniqueId());
            ChartPlotOptions options = feed.getOptions();
            if (options == null) {
                options = new ChartPlotOptions();
                feed.setOptions(options);
            }
            options.setFillColor(ChartUtils.hexColor(index));
            index++;
        }

        feed.setWidget(getGraph());
        GraphicFeedForm form = new GraphicFeedForm(feed, false);
        Dialog dialog = createDialog(label, form);
        dialog.addThemeName("side-drawer-fullscreen");
        form.setSavedHandler(entity -> {
            dataProvider.refreshAll();
            dialog.close();
        });

        UI.getCurrent().add(dialog);
        dialog.open();
    }

    private void openDetails(GraphicFeed feed, String label) {
        if (feed == null) {
            return;
        }
        GraphicFeedForm details = new GraphicFeedForm(feed, true);
        Dialog dialog = createDialog(label, details);
        dialog.addThemeName("side-drawer-fullscreen");
        UI.getCurrent().add(dialog);
        dialog.open();
    }

    private void openRemove(GraphicFeed feed) {
        if (feed == null) {
            return;
        }
        ConfirmationDialog.Callback callback = result -> {
            if (result) {
                feeds.remove(feed);
                dataProvider.refreshAll();
            }
        };
        Dialog dialog = new ConfirmationDialog(getI18nLabel("remove_dialog"), getI18nLabel("remove_action"), callback);
        UI.getCurrent().add(dialog);
        dialog.open();
    }

    private void enableButtons(GraphicFeed feed) {
        boolean enabled = feed != null;
        modifyButton.setEnabled(enabled);
        removeButton.setEnabled(enabled);
        infoButton.setEnabled(enabled);
        up.setEnabled(enabled);
        down.setEnabled(enabled);
    }

    private GraphicFeed getCurrentValue() {
        return grid.getSelectedItems().stream().findFirst().orElse(null);
    }

    private Grid<GraphicFeed> createGrid() {
        dataProvider = new ListDataProvider<>(feeds);
        Grid<GraphicFeed> table = new Grid<>();
        table.setDataProvider(dataProvider);
        table.setSelectionMode(Grid.SelectionMode.SINGLE);
        table.setSizeFull();
        table.addClassName("smallgrid");

        columns.put("device", table.addColumn(GraphicFeed::getDevice).setKey("device").setHeader(getI18nLabel("device")).setFlexGrow(1));
        columns.put("channel", table.addColumn(GraphicFeed::getChannel).setKey("channel").setHeader(getI18nLabel("channel")).setFlexGrow(1));
        columns.put("displayName", table.addColumn(this::displayName).setKey("displayName").setHeader(getI18nLabel("displayName")).setFlexGrow(1));
        columns.put("label", table.addColumn(GraphicFeed::getLabel).setKey("label").setHeader(getI18nLabel("label")).setFlexGrow(1));
        columns.put("measure", table.addColumn(this::formatMeasure).setKey("measure").setHeader(getI18nLabel("measure")).setFlexGrow(1));
        columns.put("options.fillColor", table.addComponentColumn(this::colorButton).setKey("options.fillColor").setHeader(getI18nLabel("options.fillColor")).setFlexGrow(1));
        columns.put("options.chartType", table.addColumn(feed -> optionsI18nLabel(getChartType(feed))).setKey("options.chartType").setHeader(getI18nLabel("options.chartType")).setFlexGrow(1));
        columns.put("options.markerSymbol", table.addColumn(feed -> optionsI18nLabel(getMarkerSymbol(feed))).setKey("options.markerSymbol").setHeader(getI18nLabel("options.markerSymbol")).setFlexGrow(1));
        columns.put("options.dashStyle", table.addColumn(feed -> optionsI18nLabel(getDashStyle(feed))).setKey("options.dashStyle").setHeader(getI18nLabel("options.dashStyle")).setFlexGrow(1));
        columns.put("options.extremes", table.addColumn(this::formatExtremes).setKey("options.extremes").setHeader(getI18nLabel("options.extremes")).setFlexGrow(1));
        columns.put("thresholds", table.addColumn(this::formatThresholds).setKey("thresholds").setHeader(getI18nLabel("thresholds")).setFlexGrow(1));

        return table;
    }

    private Component colorButton(GraphicFeed feed) {
        String color = feed != null && feed.getOptions() != null ? feed.getOptions().getFillColor() : null;
        if (color == null) {
            return new Span();
        }
        Button button = new Button();
        button.getStyle().set("background-color", color);
        button.setWidthFull();
        button.addThemeName("tertiary-inline");
        button.addClickListener(event -> grid.select(feed));
        return button;
    }

    private String displayName(GraphicFeed feed) {
        String displayName = ChannelUtils.displayName(feed);
        if (displayName != null) {
            return displayName;
        }
        if (feed.getChannel() != null && feed.getChannel().getConfiguration() != null) {
            return feed.getChannel().getConfiguration().getLabel();
        }
        return feed.getLabel();
    }

    private String formatMeasure(GraphicFeed feed) {
        MeasureUnit value = feed != null ? feed.getMeasure() : null;
        //return value != null ? UIUtils.getServiceFactory().getDeviceService().getUnitOfMeasureName(value.getType()) : "";
        return "";
    }

    private String formatExtremes(GraphicFeed feed) {
        MeasureRange value = feed != null && feed.getOptions() != null ? feed.getOptions().getExtremes() : null;
        if (value != null) {
            return String.format("[%s - %s]", nf.format(value.getLower()), nf.format(value.getUpper()));
        }
        return getTranslation("graphplotoptions.field.autoscale");
    }

    private String formatThresholds(GraphicFeed feed) {
        List<ChartThreshold> thresholds = feed != null ? feed.getThresholds() : null;
        if (thresholds != null) {
            List<String> values = new ArrayList<>();
            for (ChartThreshold threshold : thresholds) {
                values.add(nf.format(threshold.getValue()));
            }
            return StringUtils.join(values, " - ");
        }
        return "";
    }

    private String optionsI18nLabel(String value) {
        if (value == null) {
            return "";
        }
        return getTranslation(ChartPlotOptionsField.I18NKEY + "." + value.toLowerCase());
    }

    private String getChartType(GraphicFeed feed) {
        return feed != null && feed.getOptions() != null ? feed.getOptions().getChartType() : null;
    }

    private String getMarkerSymbol(GraphicFeed feed) {
        return feed != null && feed.getOptions() != null ? feed.getOptions().getMarkerSymbol() : null;
    }

    private String getDashStyle(GraphicFeed feed) {
        return feed != null && feed.getOptions() != null ? feed.getOptions().getDashStyle() : null;
    }

    private boolean moveItem(GraphicFeed feed, int delta) {
        if (feed == null) {
            return false;
        }
        int currentIndex = feeds.indexOf(feed);
        int targetIndex = currentIndex + delta;
        if (currentIndex < 0 || targetIndex < 0 || targetIndex >= feeds.size()) {
            return false;
        }
        Collections.swap(feeds, currentIndex, targetIndex);
        dataProvider.refreshAll();
        return true;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        up.setVisible(maxSize > 1);
        down.setVisible(maxSize > 1);
    }

    public String getWindowStyle() {
        return "graphfeed-listing";
    }

    public float[] getWindowDimension() {
        return UIUtils.XL_DIMENSION;
    }

    public String getWindowStyleDetails() {
        return UIUtils.S_WINDOW_STYLE;
    }

    // public String optionsFeedReference(String value) {
    //     if (value == null) {
    //         return "";
    //     }
    //     String serial = value.substring(0, value.indexOf("."));
    //     Device device = UIUtils.getServiceFactory().getDeviceService().findBySerial(serial);
    //     for (Channel channel : device.getChannels()) {
    //         if (channel.getKey().equals(value)) {
    //             return channel.toString();
    //         }
    //     }
    //     return value;
    // }

    // TODO(flow-migration): replace the old UI event bus registration with injected UIEventBus wiring.
    // The listing still exposes paramsChoice(GraphWidgetParamsEvent) for callers that invoke it directly.
}
