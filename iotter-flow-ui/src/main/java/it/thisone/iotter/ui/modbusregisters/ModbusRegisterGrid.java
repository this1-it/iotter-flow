package it.thisone.iotter.ui.modbusregisters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;

import it.thisone.iotter.enums.Priority;
import it.thisone.iotter.enums.modbus.Format;
import it.thisone.iotter.enums.modbus.FunctionCode;
import it.thisone.iotter.enums.modbus.Permission;
import it.thisone.iotter.enums.modbus.Signed;
import it.thisone.iotter.enums.modbus.TypeRead;
import it.thisone.iotter.enums.modbus.TypeVar;
import it.thisone.iotter.persistence.model.ModbusRegister;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChannelUtils;
import it.thisone.iotter.ui.main.UiConstants;

public class ModbusRegisterGrid extends BaseComponent {

    private static final long serialVersionUID = 2001077544797472399L;

    private static final String[] VISIBLE_COLUMNS = new String[] {
        "active", "address", "displayName", "measureUnit", "scaleMultiplier", "offset", "decimalDigits",
        "deltaLogging", "min", "max", "typeVar", "typeRead", "format", "signed", "permission",
        "functionCode", "priority", "bitmask", "qualifier"
    };

    private static final String[] NUMERIC_COLUMNS = new String[] {
        "address", "scaleMultiplier", "offset", "decimalDigits", "deltaLogging", "min", "max"
    };

    private final Grid<ModbusRegister> grid;
    private final ListDataProvider<ModbusRegister> dataProvider;
    private boolean importing;

    public ModbusRegisterGrid() {
        super("modbusregistrygrid");

        grid = new Grid<>();
        grid.setSizeFull();
        grid.addClassName("smallgrid");
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        dataProvider = new ListDataProvider<>(new ArrayList<>());
        grid.setDataProvider(dataProvider);

        initColumns();
        initColumnAlignments();
        initFilters();
        //grid.setFrozenColumnCount(3);

        setRootComposition(grid);
    }

    public String getI18nLabel(String key) {
        return getTranslation(UiConstants.PROVISIONING + "." + key);
    }

    private void initColumns() {
        grid.addComponentColumn(item -> {
            Checkbox checkBox = new Checkbox(Boolean.TRUE.equals(item.getActive()));
            checkBox.addValueChangeListener(event -> {
                item.setActive(Boolean.TRUE.equals(event.getValue()));
                dataProvider.refreshItem(item);
            });
            return checkBox;
        }).setKey("active").setHeader("");

        grid.addColumn(ModbusRegister::getAddress).setKey("address");
        grid.addColumn(ModbusRegister::getDisplayName).setKey("displayName");
        grid.addColumn(ModbusRegister::getMeasureUnit).setKey("measureUnit");
        grid.addColumn(ModbusRegister::getScaleMultiplier).setKey("scaleMultiplier");
        grid.addColumn(ModbusRegister::getOffset).setKey("offset");
        grid.addColumn(ModbusRegister::getDecimalDigits).setKey("decimalDigits");
        grid.addColumn(ModbusRegister::getDeltaLogging).setKey("deltaLogging");
        grid.addColumn(ModbusRegister::getMin).setKey("min");
        grid.addColumn(ModbusRegister::getMax).setKey("max");
        grid.addColumn(ModbusRegister::getTypeVar).setKey("typeVar");
        grid.addColumn(ModbusRegister::getTypeRead).setKey("typeRead");
        grid.addColumn(ModbusRegister::getFormat).setKey("format");
        grid.addColumn(ModbusRegister::getSigned).setKey("signed");
        grid.addColumn(ModbusRegister::getPermission).setKey("permission");
        grid.addColumn(ModbusRegister::getFunctionCode).setKey("functionCode");
        grid.addColumn(ModbusRegister::getPriority).setKey("priority");
        grid.addColumn(ModbusRegister::getBitmask).setKey("bitmask");
        grid.addColumn(ModbusRegister::getQualifier).setKey("qualifier");

        for (String column : VISIBLE_COLUMNS) {
            Column<ModbusRegister> c = grid.getColumnByKey(column);
            if (c != null && !"active".equals(column)) {
                c.setHeader(getI18nLabel(column));
            }
        }
    }

    private void initFilters() {
        HeaderRow filterRow = grid.appendHeaderRow();

        addTextFilter(filterRow, "address");
        addTextFilter(filterRow, "displayName");
        addEnumFilter(filterRow, "typeVar", Arrays.asList(TypeVar.ALARM, TypeVar.ANALOG, TypeVar.DIGITAL, TypeVar.INTEGER));
        addEnumFilter(filterRow, "typeRead", Arrays.asList(TypeRead.COIL, TypeRead.DISCRETE_INPUT, TypeRead.HOLDING, TypeRead.INPUT));
        addEnumFilter(filterRow, "permission", Arrays.asList(Permission.READ, Permission.READ_WRITE, Permission.WRITE));
        addEnumFilter(filterRow, "format", Arrays.asList(Format.BIT8, Format.BIT16, Format.BIT32, Format.FLOAT));
        addEnumFilter(filterRow, "signed", Arrays.asList(Signed.NO, Signed.YES));
        addEnumFilter(filterRow, "functionCode", Arrays.asList(FunctionCode.MULTIPLE, FunctionCode.SINGLE));
        addEnumFilter(filterRow, "priority", Arrays.asList(Priority.URGENT, Priority.HIGH, Priority.NORMAL, Priority.LOW));

        filterRow.getCell(grid.getColumnByKey("active")).setComponent(filterActiveMenuBar());
    }

    private void addTextFilter(HeaderRow filterRow, String key) {
        Column<ModbusRegister> column = grid.getColumnByKey(key);
        if (column == null) {
            return;
        }
        TextField field = new TextField();
        field.setValueChangeMode(ValueChangeMode.LAZY);
        field.setWidthFull();
        field.addValueChangeListener(event -> {
            String needle = event.getValue() == null ? "" : event.getValue().trim().toLowerCase();
            dataProvider.clearFilters();
            if (!needle.isEmpty()) {
                dataProvider.addFilter(item -> {
                    Object value = "address".equals(key) ? item.getAddress() : item.getDisplayName();
                    return value != null && value.toString().toLowerCase().contains(needle);
                });
            }
        });
        filterRow.getCell(column).setComponent(field);
    }

    private <E> void addEnumFilter(HeaderRow filterRow, String key, List<E> values) {
        Column<ModbusRegister> column = grid.getColumnByKey(key);
        if (column == null) {
            return;
        }
        ComboBox<E> combo = new ComboBox<>();
        combo.setItems(values);
        combo.setClearButtonVisible(true);
        combo.setItemLabelGenerator(item -> getI18nLabel(key + "." + item));
        combo.addValueChangeListener(event -> {
            E selected = event.getValue();
            dataProvider.clearFilters();
            if (selected != null) {
                dataProvider.addFilter(item -> {
                    Object current;
                    switch (key) {
                        case "typeVar": current = item.getTypeVar(); break;
                        case "typeRead": current = item.getTypeRead(); break;
                        case "permission": current = item.getPermission(); break;
                        case "format": current = item.getFormat(); break;
                        case "signed": current = item.getSigned(); break;
                        case "functionCode": current = item.getFunctionCode(); break;
                        case "priority": current = item.getPriority(); break;
                        default: current = null;
                    }
                    return selected.equals(current);
                });
            }
        });
        filterRow.getCell(column).setComponent(combo);
    }

    protected MenuBar filterActiveMenuBar() {
        MenuBar root = new MenuBar();
        com.vaadin.flow.component.contextmenu.MenuItem select = root.addItem(getI18nLabel("grid.show_all"));

        select.getSubMenu().addItem(getI18nLabel("grid.select_all"), e -> {
            dataProvider.clearFilters();
            for (ModbusRegister item : dataProvider.getItems()) {
                item.setActive(true);
            }
            dataProvider.refreshAll();
        });
        select.getSubMenu().addItem(getI18nLabel("grid.select_none"), e -> {
            dataProvider.clearFilters();
            for (ModbusRegister item : dataProvider.getItems()) {
                item.setActive(false);
            }
            dataProvider.refreshAll();
        });
        select.getSubMenu().addItem(getI18nLabel("grid.show_active"), e -> {
            dataProvider.clearFilters();
            dataProvider.addFilter(item -> Boolean.TRUE.equals(item.getActive()));
        });
        select.getSubMenu().addItem(getI18nLabel("grid.show_all"), e -> dataProvider.clearFilters());
        return root;
    }

    private void initColumnAlignments() {
        for (String column : NUMERIC_COLUMNS) {
            Column<ModbusRegister> c = grid.getColumnByKey(column);
            if (c != null) {
                c.setClassNameGenerator(item -> "align-right");
            }
        }
        Column<ModbusRegister> active = grid.getColumnByKey("active");
        if (active != null) {
            active.setClassNameGenerator(item -> "align-center");
        }
    }

    public void setRows(List<ModbusRegister> items) {
        for (ModbusRegister item : items) {
            if (!importing) {
                String displayName = ChannelUtils.displayName(item.getMetaData());
                if (displayName != null && !displayName.isEmpty()) {
                    item.setDisplayName(displayName);
                }
            }
        }
        dataProvider.getItems().clear();
        dataProvider.getItems().addAll(items);
        dataProvider.refreshAll();
        //grid.setFrozenColumnCount(3);
    }

    public List<ModbusRegister> getRows() {
        dataProvider.clearFilters();
        return new ArrayList<>(dataProvider.getItems());
    }

    public List<ModbusRegister> getOthers(ModbusRegister bean) {
        dataProvider.clearFilters();
        return dataProvider.getItems().stream()
            .filter(s -> !s.getId().equals(bean.getId()))
            .collect(Collectors.toList());
    }

    public void setImporting(boolean importing) {
        this.importing = importing;
    }
}
