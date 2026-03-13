package it.thisone.iotter.ui.graphicfeeds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.component.treegrid.TreeGrid;

import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.enums.modbus.TypeVar;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChannelComparator;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.common.EditorSelectedEvent;
import it.thisone.iotter.ui.common.EditorSelectedListener;
import it.thisone.iotter.ui.common.MarkupsUtils;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChannelUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.common.fields.ChannelAcceptor;
import it.thisone.iotter.util.EncryptUtils;
import it.thisone.iotter.util.PopupNotification;

class TreeItem {
    private String device;
    private String filtered;
    private Span param;
    private Channel channel;
    private Integer qualifier;
    private Span unit;
    private Span scale;
    private Span offset;
    private Span typeVar;
    private Checkbox checkbox;
    private final boolean deviceNode;
    private final int itemId;

    TreeItem(int itemId, boolean deviceNode) {
        this.itemId = itemId;
        this.deviceNode = deviceNode;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getFiltered() {
        return filtered;
    }

    public void setFiltered(String filtered) {
        this.filtered = filtered;
    }

    public Span getParam() {
        return param;
    }

    public void setParam(Span param) {
        this.param = param;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Integer getQualifier() {
        return qualifier;
    }

    public void setQualifier(Integer qualifier) {
        this.qualifier = qualifier;
    }

    public Span getUnit() {
        return unit;
    }

    public void setUnit(Span unit) {
        this.unit = unit;
    }

    public Span getScale() {
        return scale;
    }

    public void setScale(Span scale) {
        this.scale = scale;
    }

    public Span getOffset() {
        return offset;
    }

    public void setOffset(Span offset) {
        this.offset = offset;
    }

    public Span getTypeVar() {
        return typeVar;
    }

    public void setTypeVar(Span typeVar) {
        this.typeVar = typeVar;
    }

    public Checkbox getCheckbox() {
        return checkbox;
    }

    public void setCheckbox(Checkbox checkbox) {
        this.checkbox = checkbox;
    }

    public boolean isDevice() {
        return deviceNode;
    }

    public int getItemId() {
        return itemId;
    }
}

public class GraphicFeedChoice extends BaseComponent {

    private static final long serialVersionUID = 1L;

    private int maxParameters = -1;
    private Button confirmButton;
    private Button closeButton;
    private final boolean editable;

    private TreeGrid<TreeItem> tree;
    private TreeData<TreeItem> treeData;
    private TreeDataProvider<TreeItem> dataProvider;
    private Map<Integer, GraphicFeed> selected;
    private Map<Integer, String> units;
    private final List<EditorSelectedListener> listeners = new ArrayList<>();

    private Span selectionStatusLabel;
    private TextField textFilter;
    private final GraphicWidget widget;
    private final ChannelAcceptor acceptor;

    private ComboBox<String> comboMeasures;
    private Registration comboMeasuresRegistration;

    private ComboBox<TypeVar> comboTypeVars;
    private Registration comboTypeVarsRegistration;

    public GraphicFeedChoice(GraphicWidget widget, List<GraphicFeed> feeds, int max, boolean editable, String text,
            ChannelAcceptor acceptor) {
        super("graphfeed.editor");
        this.widget = widget;
        this.editable = editable;
        this.acceptor = acceptor;
        setMaxParameters(max);
        buildTree(feeds);
        setRootComposition((Component) buildLayout());
        if (text != null) {
            textFilter.setValue(text);
            filterTree(text);
            toggleTree(false);
        }
    }

    private void buildTree(List<GraphicFeed> feeds) {
        treeData = new TreeData<>();
        dataProvider = new TreeDataProvider<>(treeData);
        tree = new TreeGrid<>();
        tree.setDataProvider(dataProvider);
        tree.setSizeFull();
        tree.addClassName("smallgrid");

        tree.addHierarchyColumn(TreeItem::getDevice).setHeader(getI18nLabel("devices")).setFlexGrow(1);
        tree.addComponentColumn(TreeItem::getCheckbox).setHeader("").setWidth("50px").setFlexGrow(0);
        tree.addComponentColumn(TreeItem::getParam).setHeader(getI18nLabel("param")).setFlexGrow(2);
        tree.addComponentColumn(TreeItem::getUnit).setHeader(getI18nLabel("unit")).setFlexGrow(1);
        tree.addComponentColumn(TreeItem::getTypeVar).setHeader(getI18nLabel("typeVar")).setFlexGrow(1);
        tree.addColumn(TreeItem::getQualifier).setHeader(getI18nLabel("qualifier")).setFlexGrow(1);
        tree.addComponentColumn(TreeItem::getScale).setHeader(getI18nLabel("scale")).setFlexGrow(1);
        tree.addComponentColumn(TreeItem::getOffset).setHeader(getI18nLabel("offset")).setFlexGrow(1);

        createTreeData(widget, feeds);
        expandAll();

        selectionStatusLabel = new Span();
        updateSelectionStatus();
    }

    private void toggleTree(boolean collapsed) {
        for (TreeItem item : treeData.getRootItems()) {
            if (collapsed) {
                tree.collapse(item);
            } else {
                tree.expand(item);
            }
        }
    }

    private void expandAll() {
        for (TreeItem item : treeData.getRootItems()) {
            tree.expand(item);
        }
    }

    private VerticalLayout buildLayout() {
        confirmButton = new Button(getI18nLabel("add_selection"), event -> confirmSelection());
        confirmButton.setVisible(editable);
        closeButton = new Button(getI18nLabel("close"), event -> notifySelection(null));

        textFilter = createTextFilter();
        comboMeasures = createMeasureUnitFilter();
        comboTypeVars = createTypeVarFilter();

        textFilter.addValueChangeListener(event -> {
            if (comboMeasuresRegistration != null) {
                comboMeasuresRegistration.remove();
            }
            comboMeasures.clear();
            comboMeasuresRegistration = comboMeasures.addValueChangeListener(inner -> {
                String text = inner.getValue();
                filterTree(text != null ? "[" + text + "]" : "");
            });

            if (comboTypeVarsRegistration != null) {
                comboTypeVarsRegistration.remove();
            }
            comboTypeVars.clear();
            comboTypeVarsRegistration = comboTypeVars.addValueChangeListener(inner -> {
                TypeVar typeVar = inner.getValue();
                if (typeVar != null) {
                    textFilter.clear();
                    filterTree(typeVar.getDisplayName());
                }
            });

            filterTree(event.getValue());
        });

        comboMeasuresRegistration = comboMeasures.addValueChangeListener(event -> {
            String text = event.getValue();
            filterTree(text != null ? "[" + text + "]" : "");
        });

        comboTypeVarsRegistration = comboTypeVars.addValueChangeListener(event -> {
            TypeVar typeVar = event.getValue();
            if (typeVar != null) {
                textFilter.clear();
                filterTree(typeVar.getDisplayName());
            }
        });

        Button collapse = new Button(VaadinIcon.COMPRESS.create(), event -> toggleTree(true));
        collapse.addThemeName("tertiary-inline");

        Button expand = new Button(VaadinIcon.EXPAND.create(), event -> toggleTree(false));
        expand.addThemeName("tertiary-inline");

        HorizontalLayout filterLayout = new HorizontalLayout(collapse, expand, textFilter, comboMeasures, comboTypeVars);
        filterLayout.setSpacing(true);

        HorizontalLayout header = new HorizontalLayout(selectionStatusLabel, filterLayout);
        header.setPadding(true);
        header.setWidthFull();
        header.setVerticalComponentAlignment(Alignment.CENTER, selectionStatusLabel, filterLayout);
        header.expand(selectionStatusLabel);

        HorizontalLayout buttonLayout = new HorizontalLayout(confirmButton, closeButton);
        buttonLayout.setSpacing(true);

        HorizontalLayout footer = new HorizontalLayout(buttonLayout);
        footer.setWidthFull();
        footer.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        footer.setJustifyContentMode(HorizontalLayout.JustifyContentMode.CENTER);

        VerticalLayout layout = new VerticalLayout(header, tree, footer);
        layout.setSizeFull();
        layout.setFlexGrow(1f, tree);
        return layout;
    }

    private void createTreeData(GraphicWidget widget, List<GraphicFeed> feeds) {
        selected = new HashMap<>();
        units = new HashMap<>();
        Map<Channel, GraphicFeed> map = new HashMap<>();

        for (GraphicFeed feed : feeds) {
            map.put(feed.getChannel(), feed);
        }

        List<Device> devices = availableDevices(widget);
        int itemId = 0;

        for (Device device : devices) {
            TreeItem deviceItem = new TreeItem(itemId++, true);
            deviceItem.setDevice(device.toString());
            deviceItem.setFiltered(device.getSerial());
            treeData.addItem(null, deviceItem);

            List<Channel> channels = new ArrayList<>(device.getChannels());
            Collections.sort(channels, new ChannelComparator());

            for (Channel channel : channels) {
                if (!acceptor.accept(channel)) {
                    continue;
                }

                TreeItem channelItem = new TreeItem(itemId++, false);
                String displayName = ChannelUtils.displayName(channel);
                String typeVar = ChannelUtils.getTypeVar(channel.getMetaData());

                Span unitLabel = htmlSpan(MarkupsUtils.toHtmlMeasureUnit(channel.getMeasures()));
                Span channelLabel = new Span(displayName);
                if (channel.getConfiguration().isActive()) {
                    channelLabel.addClassName("active-param");
                }

                channelItem.setParam(channelLabel);
                channelItem.setChannel(channel);
                channelItem.setQualifier(channel.getConfiguration().getQualifier());
                channelItem.setUnit(unitLabel);

                String defaultMeasureUnit = collectMeasureUnit(channel);
                channelItem.setFiltered(device.getSerial() + " " + displayName + " [" + defaultMeasureUnit + "] " + typeVar);

                channelItem.setScale(htmlSpan(MarkupsUtils.toHtmlMeasureScale(channel.getMeasures())));
                channelItem.setOffset(htmlSpan(MarkupsUtils.toHtmlMeasureOffset(channel.getMeasures())));
                channelItem.setTypeVar(new Span(typeVar));

                Checkbox checkBox = new Checkbox();
                checkBox.setId(channel.getUniqueKey());
                checkBox.setVisible(editable);

                if (map.containsKey(channel)) {
                    checkBox.setVisible(true);
                    checkBox.setValue(true);
                    checkBox.setReadOnly(true);
                    selected.put(channelItem.getItemId(), map.get(channel));
                }

                checkBox.addValueChangeListener(event -> manageCheckBox(checkBox));
                channelItem.setCheckbox(checkBox);

                treeData.addItem(deviceItem, channelItem);
            }
        }
    }

    private ComboBox<String> createMeasureUnitFilter() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setClearButtonVisible(true);
        comboBox.setPlaceholder(getI18nLabel("filter_measures"));

        Set<String> unitSet = new HashSet<>(units.values());
        List<String> sortedUnits = new ArrayList<>(unitSet);
        Collections.sort(sortedUnits);
        comboBox.setItems(sortedUnits);
        return comboBox;
    }

    private ComboBox<TypeVar> createTypeVarFilter() {
        ComboBox<TypeVar> comboBox = new ComboBox<>();
        comboBox.setClearButtonVisible(true);
        comboBox.setPlaceholder(getI18nLabel("filter_typevar"));
        comboBox.setItems(Arrays.asList(TypeVar.ALARM, TypeVar.ANALOG, TypeVar.DIGITAL, TypeVar.INTEGER));
        comboBox.setItemLabelGenerator(TypeVar::getDisplayName);
        return comboBox;
    }

    private TextField createTextFilter() {
        TextField field = new TextField();
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        field.setPlaceholder(getI18nLabel("filter_params"));
        return field;
    }

    private String collectMeasureUnit(Channel channel) {
        MeasureUnit unit = channel.getDefaultMeasure();
        if (units.containsKey(unit.getType())) {
            return units.get(unit.getType());
        }
        throw new UnsupportedOperationException("vaadin8 legacy missing backend support");

        // String name;
        // if (ChannelAcceptor.isDirection(channel)) {
        //     name = UIUtils.getServiceFactory().getDeviceService()
        //             .getUnitOfMeasureName(ChannelAcceptor.DEGREE_MEASURE_UNIT);
        // } else {
        //     name = UIUtils.getServiceFactory().getDeviceService().getUnitOfMeasureName(unit.getType());
        // }
        // units.put(unit.getType(), name);
        // return name;
    }

    private void manageCheckBox(Checkbox box) {
        for (TreeItem rootItem : treeData.getRootItems()) {
            for (TreeItem childItem : treeData.getChildren(rootItem)) {
                Checkbox check = childItem.getCheckbox();
                Channel channel = childItem.getChannel();
                if (check == null || channel == null) {
                    continue;
                }
                if (Boolean.TRUE.equals(check.getValue())) {
                    selected.put(childItem.getItemId(), createGraphFeed(childItem.getItemId(), channel));
                } else {
                    selected.remove(childItem.getItemId());
                }
                if (check.equals(box)) {
                    if (Boolean.TRUE.equals(box.getValue())) {
                        tree.select(childItem);
                    } else {
                        tree.deselectAll();
                    }
                }
            }
        }
        allowSelection();
        updateSelectionStatus();
    }

    private void updateSelectionStatus() {
        selectionStatusLabel.setText(String.format("%s: %d , %s: %d",
                getI18nLabel("selected_params"), selected.size(),
                getI18nLabel("max_params"), getMaxParameters()));
    }

    private List<Device> availableDevices(GraphicWidget graph) {
        throw new UnsupportedOperationException("vaadin8 legacy missing backend support");

        // List<Device> available = new ArrayList<>();
        // String serial = graph.getGroupWidget().getDevice();
        // if (serial != null) {
        //     Device device = UIUtils.getServiceFactory().getDeviceService().findBySerial(serial);
        //     if (device != null) {
        //         available.add(device);
        //         return available;
        //     }
        // }

        // NetworkGroup group = graph.getGroupWidget().getGroup();
        // if (group == null) {
        //     return available;
        // }
        // List<Device> devices = UIUtils.getServiceFactory().getDeviceService().findByGroup(group);
        // for (Device device : devices) {
        //     if (accept(device)) {
        //         available.add(device);
        //     }
        // }
        // return available;
    }

    private boolean accept(Device device) {
        if (device.getChannels().isEmpty()) {
            return false;
        }
        if (widget.getDevice() != null && !widget.getDevice().equals(device.getSerial())) {
            return false;
        }
        if (widget.getType().equals(GraphicWidgetType.WIND_ROSE)) {
            for (Channel channel : device.getChannels()) {
                if (ChannelAcceptor.isDirection(channel)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public int getMaxParameters() {
        return maxParameters;
    }

    public void setMaxParameters(int maxParameters) {
        this.maxParameters = maxParameters;
    }

    public void addListener(EditorSelectedListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(EditorSelectedListener listener) {
        listeners.remove(listener);
    }

    public String getWindowStyle() {
        return "graphfeed-choice";
    }

    public float[] getWindowDimension() {
        return UIUtils.L_DIMENSION;
    }

    private void confirmSelection() {
        if (!allowSelection()) {
            return;
        }
        notifySelection(new ArrayList<>(selected.values()));
    }

    private void notifySelection(List<GraphicFeed> items) {
        EditorSelectedEvent<GraphicFeed> event = new EditorSelectedEvent<>(this, items);
        for (EditorSelectedListener listener : listeners) {
            listener.editorSelected(event);
        }
    }

    private void filterTree(String text) {
        if (text == null || text.trim().isEmpty()) {
            dataProvider.clearFilters();
        } else {
            String lowered = text.toLowerCase();
            dataProvider.setFilter(item -> item.isDevice()
                    || (item.getFiltered() != null && item.getFiltered().toLowerCase().contains(lowered)));
        }
        expandAll();
        dataProvider.refreshAll();
    }

    private boolean allowSelection() {
        if (selected.size() > getMaxParameters()) {
            PopupNotification.show(getI18nLabel("too_many_params"), PopupNotification.Type.WARNING);
            return false;
        }
        return true;
    }

    private GraphicFeed createGraphFeed(Object itemId, Channel channel) {
        GraphicFeed feed = new GraphicFeed();
        feed.setId(EncryptUtils.getUniqueId());
        feed.setWidget(widget);
        feed.setChannel(channel);
        feed.setMeasure(channel.getDefaultMeasure());
        feed.getOptions().setFillColor(ChartUtils.hexColor((Integer) itemId));
        if (widget.getType().equals(GraphicWidgetType.WIND_ROSE)) {
            Device device = channel.getDevice();
            for (Channel item : device.getChannels()) {
                if (ChannelAcceptor.isDirection(item)) {
                    feed.getOptions().setFeedReference(item.getKey());
                    break;
                }
            }
        }
        return feed;
    }

    private Span htmlSpan(String html) {
        Span span = new Span();
        span.getElement().setProperty("innerHTML", html);
        return span;
    }
}
