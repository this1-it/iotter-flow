package it.thisone.iotter.ui.graphicwidgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;

import it.thisone.iotter.enums.modbus.Permission;
import it.thisone.iotter.enums.modbus.TypeVar;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.ModbusProfile;
import it.thisone.iotter.persistence.model.ModbusRegister;
import it.thisone.iotter.provisioning.AernetXLSXParserConstants;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChannelUtils;
import it.thisone.iotter.ui.common.fields.DeviceSerialSelect;
import it.thisone.iotter.ui.common.fields.GraphicWidgetTypeComboBox;
import it.thisone.iotter.ui.ifc.IGraphicWidgetEditor;
import it.thisone.iotter.ui.visualizers.controlpanel.IconSetField;
import it.thisone.iotter.util.PopupNotification;
import it.thisone.iotter.util.PopupNotification.Type;

public class ControlPanelBaseForm extends AbstractBaseEntityForm<GraphicWidget>
        implements IGraphicWidgetEditor, ControlPanelBaseConstants, AernetXLSXParserConstants {

    public static final String CONTROLPANELBASE_EDITOR = "controlpanelbase.editor";

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ControlPanelBaseForm.class);

    private GraphicWidgetTypeComboBox type;
    private TextField provider;
    private TextField label;
    private DeviceSerialSelect deviceSelect;
    private boolean fieldsInitialized;

    private List<GraphicFeed> sectionFeeds;

    private Grid<ModbusRegister> grid;
    private ListDataProvider<ModbusRegister> registersDataProvider;
    private Grid<GraphicFeed> feedsGrid;
    private FeedDataProvider feedsDataProvider;
    private Device device;
    private Tabs sections;
    private final Map<Tab, VerticalLayout> sectionLayouts = new LinkedHashMap<>();
    private final Map<String, Tab> sectionTabsByName = new HashMap<>();
    private String sectionId;
    private int feedSize;

    public ControlPanelBaseForm(GraphicWidget entity) {
        super(entity, GraphicWidget.class, CONTROLPANELBASE_EDITOR, null,null,false);
        sectionFeeds = getEntity().getFeeds();
        if (sectionFeeds.isEmpty()) {
            logger.debug("sectionFeeds is empty");
        }
        bindFields();
    }

    protected void initializeFields() {
        if (fieldsInitialized) {
            return;
        }

        type = new GraphicWidgetTypeComboBox();
        type.setLabel(getI18nLabel("type"));
        type.setSizeFull();
        type.setClearButtonVisible(false);
        type.setAllowCustomValue(false);

        provider = new TextField(getI18nLabel("provider"));
        provider.setSizeFull();

        label = new TextField(getI18nLabel("label"));
        label.setSizeFull();
        label.setRequiredIndicatorVisible(true);

        deviceSelect = new DeviceSerialSelect();
        deviceSelect.setLabel(getI18nLabel("device"));
        deviceSelect.setSizeFull();

        addField("type", type);
        addField("provider", provider);
        addField("label", label);
        addField("device", deviceSelect);

        bindFields();
        fieldsInitialized = true;
    }

    protected void bindFields() {
        Binder<GraphicWidget> binder = getBinder();
        binder.forField(type).bind(GraphicWidget::getType, GraphicWidget::setType);
        binder.forField(provider).bind(GraphicWidget::getProvider, GraphicWidget::setProvider);
        binder.forField(label).bind(GraphicWidget::getLabel, GraphicWidget::setLabel);
        binder.forField(deviceSelect).bind(GraphicWidget::getDevice, GraphicWidget::setDevice);
    }

    public void reset() {
        feedsGrid.deselectAll();
        feedsDataProvider.refreshAll();
        grid.deselectAll();
        sections.setSelectedIndex(0);
        feedsDataProvider.filterSection(QUICKCOM);
    }

    @Override
    public VerticalLayout getFieldsLayout() {
        initializeFields();
        sectionFeeds = new ArrayList<>();

        VerticalLayout mainLayout = buildMainLayout();

        Tab generalTab = new Tab(getI18nLabel("general_tab"));
        Tab registersTab = new Tab(getI18nLabel("registers_tab"));
        Tabs mainTabs = new Tabs(generalTab, registersTab);
        mainTabs.setWidthFull();

        Div pages = new Div();
        pages.setSizeFull();

        Map<Tab, Component> mainPages = new LinkedHashMap<>();
        List<String> generalProps = Arrays.asList("type", "provider", "label", "device");
        mainPages.put(generalTab, buildForm(generalProps));
        mainPages.put(registersTab, createProfileRegistersLayout());

        pages.add(mainPages.get(generalTab));
        mainTabs.addSelectedChangeListener(event -> {
            pages.removeAll();
            pages.add(mainPages.get(event.getSelectedTab()));
        });

        mainLayout.add(mainTabs, pages);
        mainLayout.setFlexGrow(1f, pages);

        deviceSelect.setGraph(getEntity());
        initializeDeviceSelection(deviceSelect);
        return mainLayout;
    }

    private void initializeDeviceSelection(DeviceSerialSelect selector) {
        if (getEntity().getDevice() != null) {
            device = UIUtils.getServiceFactory().getDeviceService().findBySerial(getEntity().getDevice());
            if (device != null && !device.getProfiles().isEmpty()) {
                selector.setReadOnly(true);
                ModbusProfile entity = device.getProfiles().iterator().next();
                setRegisters(entity.getRegisters());
            } else {
                selector.setValue(null);
            }
        }

        selector.addValueChangeListener(event -> {
            String serial = event.getValue();
            device = UIUtils.getServiceFactory().getDeviceService().findBySerial(serial);
            if (device != null && !device.getProfiles().isEmpty()) {
                ModbusProfile entity = device.getProfiles().iterator().next();
                setRegisters(entity.getRegisters());
            }
            populateFeedDataProvider(new ArrayList<>());
            reset();
        });
    }

    public Component createProfileRegistersLayout() {
        HorizontalLayout content = new HorizontalLayout();
        content.setPadding(true);
        content.setSizeFull();

        VerticalLayout left = new VerticalLayout();
        left.setDefaultHorizontalComponentAlignment(Alignment.START);
        left.setSizeFull();

        VerticalLayout right = new VerticalLayout();
        right.setDefaultHorizontalComponentAlignment(Alignment.START);
        right.setSizeFull();

        VerticalLayout center = new VerticalLayout();
        center.setSizeFull();
        center.setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        Button moveRightButton = new Button(VaadinIcon.ARROW_CIRCLE_RIGHT.create());
        moveRightButton.addClassName("icon-only");
        moveRightButton.setEnabled(false);

        Button moveLeftButton = new Button(VaadinIcon.ARROW_CIRCLE_LEFT.create());
        moveLeftButton.addClassName("icon-only");
        moveLeftButton.setEnabled(false);

        VerticalLayout buttons = new VerticalLayout(moveRightButton, moveLeftButton);
        buttons.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        buttons.addClassName(UIUtils.BUTTONS_STYLE);
        center.add(buttons);

        content.add(left, center, right);
        content.setVerticalComponentAlignment(Alignment.START, left, center, right);
        content.setFlexGrow(0.55f, left);
        content.setFlexGrow(0.05f, center);
        content.setFlexGrow(0.4f, right);

        grid = createRegistersGrid();
        left.add(grid);
        left.setFlexGrow(1f, grid);
        left.setSpacing(true);

        feedsGrid = createFeedsGrid();
        sections = createSections();
        Div sectionContent = new Div();
        sectionContent.setSizeFull();
        right.add(sections, sectionContent);
        right.setFlexGrow(1f, sectionContent);
        right.setSpacing(true);

        grid.addSelectionListener(event -> moveRightButton.setEnabled(event.getFirstSelectedItem().isPresent()));

        populateFeedDataProvider(getEntity().getFeeds());
        sections.setSelectedIndex(0);
        showTable(sections.getSelectedTab(), sectionContent);

        sections.addSelectedChangeListener(event -> showTable(event.getSelectedTab(), sectionContent));

        feedsGrid.addSelectionListener(event -> moveLeftButton.setEnabled(event.getFirstSelectedItem().isPresent()));
        moveLeftButton.addClickListener(event -> removeFeed());
        moveRightButton.addClickListener(event -> addFeed());

        return content;
    }

    private void addFeed() {
        if (grid.getSelectedItems().isEmpty()) {
            return;
        }

        TypeVar typeVar = null;
        List<Permission> permissions = new ArrayList<>();
        int members = 0;
        switch (sectionId) {
            case QUICKCOM:
                typeVar = TypeVar.DIGITAL;
                permissions.add(Permission.READ_WRITE);
                members = QUICKCOM_POS;
                break;
            case RESET:
                typeVar = TypeVar.DIGITAL;
                permissions.add(Permission.WRITE);
                members = RESET_POS;
                break;
            case SERIES:
                members = SERIES_POS;
                break;
            case SETPOINT:
                permissions.add(Permission.READ_WRITE);
                permissions.add(Permission.WRITE);
                members = SETPOINT_POS;
                break;
            case ASCII:
                typeVar = TypeVar.INTEGER;
                members = ASCII_POS;
                break;
            default:
                break;
        }

        ModbusRegister register = grid.getSelectedItems().iterator().next();

        if (typeVar != null && !register.getTypeVar().equals(typeVar)) {
            PopupNotification.show(getI18nLabel("register_type_not_allowed"), Type.WARNING);
            return;
        }

        if (!permissions.isEmpty() && !permissions.contains(register.getPermission())) {
            PopupNotification.show(getI18nLabel("register_permission_not_allowed"), Type.WARNING);
            return;
        }

        long currentInSection = feedsDataProvider.getItems().stream()
                .filter(f -> sectionId.equals(f.getSection()))
                .count();

        if (currentInSection < members) {
            GraphicFeed feed = ControlPanelBaseConstants.createGraphicFeed(register);
            feed.setSection(sectionId);
            if (!feedsDataProvider.addFeed(feed)) {
                PopupNotification.show(getI18nLabel("register_already_assigned"), Type.WARNING);
                return;
            }
            feedSize++;
            updateSectionsCaption(feedSize);
            feedsDataProvider.refreshAll();
        } else {
            PopupNotification.show(getI18nLabel("section_already_filled"), Type.WARNING);
        }
    }

    private void removeFeed() {
        if (feedsGrid.getSelectedItems().isEmpty()) {
            return;
        }
        feedSize--;
        updateSectionsCaption(feedSize);
        GraphicFeed selectedFeed = feedsGrid.getSelectedItems().iterator().next();
        feedsDataProvider.getItems().remove(selectedFeed);
        feedsDataProvider.refreshAll();
    }

    private Tabs createSections() {
        Tabs tabs = new Tabs();
        tabs.setWidthFull();
        sectionLayouts.clear();
        sectionTabsByName.clear();

        for (String name : NAMES) {
            Tab tab = new Tab(getI18nLabel(name));
            VerticalLayout layout = new VerticalLayout();
            layout.setSizeFull();
            layout.setId(name);
            sectionLayouts.put(tab, layout);
            sectionTabsByName.put(name, tab);
            tabs.add(tab);
        }
        return tabs;
    }

    private Grid<GraphicFeed> createFeedsGrid() {
        Grid<GraphicFeed> table = new Grid<>();
        feedsDataProvider = new FeedDataProvider();
        table.setDataProvider(feedsDataProvider);
        //table.addClassName(UIUtils.TABLE_STYLE);
        table.setSelectionMode(Grid.SelectionMode.SINGLE);
        table.setSizeFull();

        Column<GraphicFeed> feedName = table.addColumn(GraphicFeed::getLabel).setKey(FEED_NAME).setHeader("");
        Column<GraphicFeed> icon = table.addComponentColumn(feed -> {
            if (feed.getSection() != null && feed.getSection().startsWith(SERIES)) {
                Checkbox field = new Checkbox(Boolean.TRUE.equals(feed.isChecked()));
                field.addValueChangeListener(event -> {
                    boolean checked = Boolean.TRUE.equals(event.getValue());
                    if (checked) {
                        if (feedsDataProvider.countChecked() < SERIES_POS_CHECKED) {
                            feed.setChecked(true);
                        } else {
                            field.setValue(false);
                        }
                    } else {
                        feed.setChecked(false);
                    }
                });
                return field;
            }

            if (feed.getResourceID() != null) {
                IconSetField field = new IconSetField();
                field.setValue(feed.getResourceID());
                field.addValueChangeListener(event -> {
                    String value = event.getValue();
                    if (value != null) {
                        feed.setResourceID(value);
                    }
                });
                return field;
            }
            return new Div();
        }).setKey(ICON).setHeader("");

        feedName.setFlexGrow(4);
        icon.setFlexGrow(1);
        return table;
    }

    private Grid<ModbusRegister> createRegistersGrid() {
        Grid<ModbusRegister> table = new Grid<>();
        registersDataProvider = new ListDataProvider<>(new ArrayList<>());
        table.setDataProvider(registersDataProvider);
        table.addClassName("smallgrid");
        table.setSelectionMode(Grid.SelectionMode.SINGLE);
        table.setSizeFull();

        table.addColumn(ModbusRegister::getAddress).setKey("address").setHeader(getI18nLabel("address"));
        table.addColumn(ModbusRegister::getDisplayName).setKey("displayName").setHeader(getI18nLabel("displayName"));
        table.addColumn(ModbusRegister::getTypeVar).setKey("typeVar").setHeader(getI18nLabel("typeVar"));
        table.addColumn(ModbusRegister::getPermission).setKey("permission").setHeader(getI18nLabel("permission"));
        //table.setFrozenColumnCount(2);

        HeaderRow filterRow = table.appendHeaderRow();
        addTextFilter(filterRow, table, "address");
        addTextFilter(filterRow, table, "displayName");
        addEnumFilter(filterRow, table, "typeVar", Arrays.asList(TypeVar.ALARM, TypeVar.ANALOG, TypeVar.DIGITAL, TypeVar.INTEGER));
        addEnumFilter(filterRow, table, "permission", Arrays.asList(Permission.READ, Permission.READ_WRITE, Permission.WRITE));

        Column<ModbusRegister> addressCol = table.getColumnByKey("address");
        if (addressCol != null) {
            addressCol.setClassNameGenerator(item -> "align-right");
        }
        return table;
    }

    private void addTextFilter(HeaderRow filterRow, Grid<ModbusRegister> table, String key) {
        Column<ModbusRegister> column = table.getColumnByKey(key);
        if (column == null) {
            return;
        }
        TextField filterField = new TextField();
        filterField.setValueChangeMode(ValueChangeMode.LAZY);
        filterField.setWidthFull();
        filterField.addValueChangeListener(event -> {
            String needle = event.getValue() == null ? "" : event.getValue().trim().toLowerCase();
            registersDataProvider.clearFilters();
            if (!needle.isEmpty()) {
                registersDataProvider.addFilter(item -> {
                    Object value = "address".equals(key) ? item.getAddress() : item.getDisplayName();
                    return value != null && value.toString().toLowerCase().contains(needle);
                });
            }
        });
        filterRow.getCell(column).setComponent(filterField);
    }

    private <E> void addEnumFilter(HeaderRow filterRow, Grid<ModbusRegister> table, String key, List<E> values) {
        Column<ModbusRegister> column = table.getColumnByKey(key);
        if (column == null) {
            return;
        }
        ComboBox<E> combo = new ComboBox<>();
        combo.setItems(values);
        combo.setClearButtonVisible(true);
        combo.setAllowCustomValue(false);
        combo.addValueChangeListener(event -> {
            E selected = event.getValue();
            registersDataProvider.clearFilters();
            if (selected != null) {
                registersDataProvider.addFilter(item -> {
                    Object current = "typeVar".equals(key) ? item.getTypeVar() : item.getPermission();
                    return selected.equals(current);
                });
            }
        });
        filterRow.getCell(column).setComponent(combo);
    }

    public List<GraphicFeed> getFeeds() {
        return getEntity().getFeeds();
    }

    public String getWindowStyle() {
        return null;
    }

    public float[] getWindowDimension() {
        return UIUtils.XL_DIMENSION;
    }

    public void setRegisters(List<ModbusRegister> items) {
        registersDataProvider.getItems().clear();
        if (items.isEmpty()) {
            sectionFeeds = new ArrayList<>();
            return;
        }
        for (ModbusRegister item : items) {
            String displayName = ChannelUtils.displayName(item.getMetaData());
            if (displayName != null && !displayName.isEmpty()) {
                item.setDisplayName(displayName);
            }
            if (item.isAvailable()) {
                registersDataProvider.getItems().add(item);
            }
        }
        registersDataProvider.refreshAll();
        sectionFeeds = ControlPanelBaseConstants.createSectionFeeds(items);
        updateGridCaption();
    }

    private void populateFeedDataProvider(List<GraphicFeed> feeds) {
        if (feeds.isEmpty()) {
            feeds = sectionFeeds;
        }
        feedsDataProvider.removeAllItems();
        feedsDataProvider.addFeeds(feeds);
        feedsDataProvider.refreshAll();
        feedSize = feeds.size();
        updateSectionsCaption(feeds.size());
    }

    @Override
    protected void afterCommit() {
        if (device != null) {
            Map<String, Channel> map = new HashMap<>();
            for (Channel chnl : device.getChannels()) {
                if (chnl.getMetaIdentifier() != null && chnl.getConfiguration().isActive()) {
                    map.put(chnl.getMetaIdentifier(), chnl);
                }
            }
            for (GraphicFeed feed : feedsDataProvider.getFeeds()) {
                if (feed.getChannel() == null) {
                    Channel chnl = map.get(feed.getMetaIdentifier());
                    if (chnl != null) {
                        feed.setChannel(chnl);
                        feed.setLabel(chnl.getConfiguration().getLabel());
                        feed.setMeasure(chnl.getDefaultMeasure());
                    }
                }
            }
        }
        getEntity().setFeeds(feedsDataProvider.getFeeds());
    }

    @Override
    protected void beforeCommit() throws EditorConstraintException {
        String value = label.getValue();
        if (value == null || value.trim().isEmpty()) {
            throw new EditorConstraintException(getTranslation("validators.fieldgroup_errors"));
        }
    }

    @Override
    public int getMaxParameters() {
        return 0;
    }

    private void showTable(Tab tab, Div sectionContent) {
        if (tab == null) {
            return;
        }
        VerticalLayout layout = sectionLayouts.get(tab);
        if (layout == null) {
            return;
        }
        sectionId = layout.getId().orElse(null);
        feedsDataProvider.filterSection(sectionId);
        feedsGrid.deselectAll();
        layout.removeAll();
        layout.add(feedsGrid);
        sectionContent.removeAll();
        sectionContent.add(layout);
    }

    private void updateGridCaption() {
        // No direct caption API in Flow Grid; kept for behavioral parity placeholder.
    }

    private void updateSectionsCaption(int size) {
        for (String name : NAMES) {
            Tab tab = sectionTabsByName.get(name);
            if (tab != null) {
                long count = feedsDataProvider.getItems().stream().filter(f -> name.equals(f.getSection())).count();
                tab.setLabel(getI18nLabel(name) + " (" + count + ")");
            }
        }
    }

    private static class FeedDataProvider extends ListDataProvider<GraphicFeed> {
        private static final long serialVersionUID = 1L;
        private String currentFilter;

        FeedDataProvider() {
            super(new ArrayList<>());
        }

        void addFeeds(List<GraphicFeed> feeds) {
            for (GraphicFeed feed : feeds) {
                addFeed(feed);
            }
        }

        boolean addFeed(GraphicFeed feed) {
            if (feed.getMetaData() == null || feed.getSection() == null) {
                return false;
            }
            boolean exists = getItems().stream().anyMatch(existing -> feed.getMetaData().equals(existing.getMetaData()));
            if (exists) {
                return false;
            }
            getItems().add(feed);
            refreshAll();
            return true;
        }

        void filterSection(String section) {
            currentFilter = section;
            setFilter(feed -> section == null || section.equals(feed.getSection()));
        }

        void removeAllFilters() {
            currentFilter = null;
            setFilter(null);
        }

        void removeAllItems() {
            getItems().clear();
            refreshAll();
        }

        int countChecked() {
            return (int) getItems().stream()
                    .filter(feed -> currentFilter == null || currentFilter.equals(feed.getSection()))
                    .filter(GraphicFeed::isChecked)
                    .count();
        }

        List<GraphicFeed> getFeeds() {
            removeAllFilters();
            return getItems().stream()
                    .sorted((a, b) -> {
                        String sa = a.getSection() == null ? "" : a.getSection();
                        String sb = b.getSection() == null ? "" : b.getSection();
                        return sa.compareTo(sb);
                    })
                    .collect(Collectors.toList());
        }
    }
}
