package it.thisone.iotter.ui.modbusregisters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;

import it.thisone.iotter.enums.Priority;
import it.thisone.iotter.enums.modbus.Format;
import it.thisone.iotter.enums.modbus.FunctionCode;
import it.thisone.iotter.enums.modbus.Permission;
import it.thisone.iotter.enums.modbus.Qualifier;
import it.thisone.iotter.enums.modbus.Signed;
import it.thisone.iotter.enums.modbus.TypeRead;
import it.thisone.iotter.enums.modbus.TypeVar;
import it.thisone.iotter.persistence.model.ModbusRegister;
import it.thisone.iotter.security.Permissions;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.AbstractBaseEntityListing;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.common.ConfirmationDialog;
import it.thisone.iotter.ui.common.ConfirmationDialog.Callback;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.main.UiConstants;
import it.thisone.iotter.util.BacNet;

public class ModbusRegisterListing extends AbstractBaseEntityListing<ModbusRegister> {

	private static final long serialVersionUID = 1L;
	private static final String ADD_BUTTON = "add";

	private static final String MODBUS_REGISTER_VIEW = UiConstants.PROVISIONING;

	private final Permissions permissions;
	private final String[] collapsed = new String[] { //
			"measureUnit", //
			"scaleMultiplier", //
			"offset", //
			"decimalDigits", //
			"deltaLogging", //
			"min", //
			"max", //
			"priority", //
			"bitmask", //
			"qualifier" //
	};

	private Grid<ModbusRegister> grid;
	private ListDataProvider<ModbusRegister> dataProvider;
	private List<ModbusRegister> others;

	public ModbusRegisterListing() {
		this(new Permissions(true));
	}

	private ModbusRegisterListing(Permissions permissions) {
		super(ModbusRegister.class, MODBUS_REGISTER_VIEW, MODBUS_REGISTER_VIEW, false);
		this.permissions = permissions;
		setPermissions(permissions);
		buildLayout();
	}

	@Override
	protected AbstractBaseEntityForm<ModbusRegister> getEditor(ModbusRegister item, boolean readOnly) {
		return new ModbusRegisterForm(item, getOthers(item));
	}

	@Override
	protected void openDetails(ModbusRegister item) {
		// no details view
	}

	public void setRows(List<ModbusRegister> registers, List<ModbusRegister> others) {
		if (registers != null) {
			dataProvider.getItems().clear();
			dataProvider.getItems().addAll(registers);
			dataProvider.refreshAll();
			grid.scrollToStart();
			grid.asSingleSelect().clear();
			enableButtons(null);
		}
		this.others = others;
	}

	public List<ModbusRegister> getOthers(ModbusRegister bean) {
		List<ModbusRegister> items = this.others != null ? this.others : new ArrayList<>(dataProvider.getItems());
		return items.stream()
				.filter(s -> bean == null || !s.getId().equals(bean.getId()))
				.collect(Collectors.toList());
	}

	public List<ModbusRegister> getRows() {
		return new ArrayList<>(dataProvider.getItems());
	}

	private void buildLayout() {
		HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.setWidthFull();
		toolbar.setSpacing(true);
		toolbar.setPadding(true);
		toolbar.addClassName(UIUtils.TOOLBAR_STYLE);

		dataProvider = new ListDataProvider<>(new ArrayList<>());
		setDataProvider(dataProvider);

		grid = createGrid();
		VerticalLayout content = createContent(grid);
		setSelectable(grid);

		getButtonsLayout().add(createRemoveButton());
		getButtonsLayout().add(createModifyButton());
		getButtonsLayout().add(createAddButton());
		toolbar.add(getButtonsLayout());
		toolbar.setVerticalComponentAlignment(Alignment.CENTER, getButtonsLayout());
		enableButtons(null);

		getMainLayout().add(toolbar);
		getMainLayout().add(content);
		getMainLayout().setFlexGrow(1f, content);
	}

	private Grid<ModbusRegister> createGrid() {
		Grid<ModbusRegister> grid = new Grid<>();
		grid.setDataProvider(dataProvider);
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.addItemClickListener(event -> grid.select(event.getItem()));
		grid.setSizeFull();
		//grid.addClassName(UIUtils.TABLE_STYLE);

		List<Grid.Column<ModbusRegister>> columns = new ArrayList<>();
		columns.add(grid.addColumn(ModbusRegister::getAddress).setKey("address"));
		columns.add(grid.addColumn(ModbusRegister::getDisplayName).setKey("displayName"));
		columns.add(grid.addColumn(ModbusRegister::getActive).setKey("active"));
		columns.add(grid.addColumn(ModbusRegister::getMeasureUnit).setKey("measureUnit"));
		columns.add(grid.addColumn(ModbusRegister::getScaleMultiplier).setKey("scaleMultiplier"));
		columns.add(grid.addColumn(ModbusRegister::getOffset).setKey("offset"));
		columns.add(grid.addColumn(ModbusRegister::getDecimalDigits).setKey("decimalDigits"));
		columns.add(grid.addColumn(ModbusRegister::getDeltaLogging).setKey("deltaLogging"));
		columns.add(grid.addColumn(ModbusRegister::getMin).setKey("min"));
		columns.add(grid.addColumn(ModbusRegister::getMax).setKey("max"));
		columns.add(grid.addColumn(ModbusRegister::getTypeVar).setKey("typeVar"));
		columns.add(grid.addColumn(ModbusRegister::getTypeRead).setKey("typeRead"));
		columns.add(grid.addColumn(ModbusRegister::getFormat).setKey("format"));
		columns.add(grid.addColumn(ModbusRegister::getSigned).setKey("signed"));
		columns.add(grid.addColumn(ModbusRegister::getPermission).setKey("permission"));
		columns.add(grid.addColumn(ModbusRegister::getFunctionCode).setKey("functionCode"));
		columns.add(grid.addColumn(ModbusRegister::getPriority).setKey("priority"));
		columns.add(grid.addColumn(ModbusRegister::getBitmask).setKey("bitmask"));
		columns.add(grid.addColumn(ModbusRegister::getQualifier).setKey("qualifier"));

		for (Grid.Column<ModbusRegister> column : columns) {
			String columnId = column.getKey();
			column.setHeader(getI18nLabel(columnId));
			column.setSortable(false);
		}

		grid.setColumnOrder(columns.toArray(new Grid.Column[0]));

		List<String> collapsedList = Arrays.asList(collapsed);
		for (Grid.Column<ModbusRegister> column : columns) {
			if (collapsedList.contains(column.getKey())) {
				column.setVisible(false);
			}
		}
		return grid;
	}

	private VerticalLayout createContent(Grid<ModbusRegister> grid) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.add(grid);
		layout.setFlexGrow(1f, grid);
		return layout;
	}

	private Button createAddButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.PLUS.create());
		button.getElement().setProperty("title", getI18nLabel(ADD_BUTTON));
		button.setId(ADD_BUTTON + getId() + ALWAYS_ENABLED_BUTTON);
		button.addClickListener(event -> openAdd());
		button.setVisible(permissions.isCreateMode());
		return button;
	}

	private Button createModifyButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.EDIT.create());
		button.getElement().setProperty("title", getI18nLabel("modify_action"));
		button.addClickListener(event -> openEditor(getCurrentValue(), getI18nLabel("modify_dialog"), false));
		button.setVisible(permissions.isModifyMode());
		return button;
	}

	private Button createRemoveButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.TRASH.create());
		button.getElement().setProperty("title", getI18nLabel("remove_action"));
		button.addClickListener(event -> openRemove(getCurrentValue()));
		button.setVisible(permissions.isRemoveMode());
		return button;
	}

	private void openAdd() {
		ModbusRegister target = new ModbusRegister();
		target.setId(UUID.randomUUID().toString());
		target.setMeasureUnit(BacNet.ADIM);
		target.setCrucial(true);
		target.setActive(true);
		target.setPermission(Permission.READ_WRITE);
		target.setFormat(Format.BIT16);
		target.setFunctionCode(FunctionCode.MULTIPLE);
		target.setPriority(Priority.LOW);
		target.setTypeRead(TypeRead.HOLDING);
		target.setTypeVar(TypeVar.ANALOG);
		target.setSigned(Signed.YES);
		target.setScaleMultiplier(1d);
		target.setOffset(0d);
		target.setMax(+32767d);
		target.setMin(-32768d);
		target.setDecimalDigits(0);
		target.setDeltaLogging(0d);
		target.setBitmask("");
		target.setQualifier(Qualifier.AVG);
		openEditor(target, getI18nLabel("add_crucial_setpoint"), true);
	}

	private void openEditor(ModbusRegister item, String label, boolean addMode) {
		if (item == null) {
			return;
		}
		AbstractBaseEntityForm<ModbusRegister> editor = getEditor(item, false);
		Dialog dialog = BaseComponent.createDialog(label, editor);
		editor.setSavedHandler(entity -> {
			if (addMode && !dataProvider.getItems().contains(entity)) {
				int cnt = dataProvider.getItems().size() + 1;
				String metaData = ModbusRegister.buildMetadata(cnt, entity) + "|" + ModbusRegister.CRUCIAL_PROP;
				entity.setMetaData(metaData);
				dataProvider.getItems().add(entity);
			} else {
				dataProvider.refreshItem(entity);
			}
			dataProvider.refreshAll();
			dialog.close();
			enableButtons(null);
		});
		dialog.open();
	}

	@Override
	protected void openRemove(ModbusRegister item) {
		if (item == null) {
			return;
		}
		Callback callback = result -> {
			if (result) {
				dataProvider.getItems().remove(item);
				dataProvider.refreshAll();
				grid.asSingleSelect().clear();
				enableButtons(null);
			}
		};
		String caption = getTranslation("basic.editor.are_you_sure");
		String message = getI18nLabel("remove_action");
		new ConfirmationDialog(caption, message, callback).open();
	}
}
