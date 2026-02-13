package it.thisone.iotter.ui.provisioning;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.persistence.model.ModbusRegister;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.main.UiConstants;
import it.thisone.iotter.ui.modbusregisters.ModbusRegisterGrid;
import it.thisone.iotter.ui.modbusregisters.ModbusRegisterListing;
public class ModbusRegistersField extends Composite<VerticalLayout>
//CustomField<List<ModbusRegister>> 

{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2936983441775994254L;

	private ModbusRegisterGrid grid;
	private ModbusRegisterListing table;

	
	public ModbusRegistersField() {
		super();
		grid = new ModbusRegisterGrid();
		
		table = new ModbusRegisterListing();

		Tab gridTab = new Tab(getI18nLabel("slave_registers"));
		Tab tableTab = new Tab(getI18nLabel("crucial_setpoints"));
		Map<Tab, com.vaadin.flow.component.Component> pages = new LinkedHashMap<>();
		pages.put(gridTab, grid);

		Tabs tabs = new Tabs(gridTab);
		tabs.setWidthFull();

		Div pagesContainer = new Div();
		pagesContainer.setSizeFull();
		pagesContainer.add(grid);

		if (UIUtils.getUserDetails().hasRole(Constants.ROLE_SUPERVISOR)) {
			pages.put(tableTab, table);
			tabs.add(tableTab);
		}

		tabs.addSelectedChangeListener(event -> {
			pagesContainer.removeAll();
			pagesContainer.add(pages.get(event.getSelectedTab()));
		});

		VerticalLayout content = getContent();
		content.setSizeFull();
		content.setPadding(false);
		content.setSpacing(false);
		content.add(tabs, pagesContainer);
		content.setFlexGrow(1, pagesContainer);
	}
	
	private String getI18nLabel(String key) {
		return getTranslation(UiConstants.PROVISIONING  + "." + key);
	}


	public List<ModbusRegister> getRegisters() {
		List<ModbusRegister> list = Stream.concat(grid.getRows().stream(), table.getRows().stream())
				.collect(Collectors.toList());
		return list;
	}

	// Bug #2030
	public void setRegisters(List<ModbusRegister> list) {
		List<ModbusRegister> normal = list.stream().filter(s -> !s.getCrucial()).collect(Collectors.toList());
		List<ModbusRegister> crucial = list.stream().filter(s -> s.getCrucial()).collect(Collectors.toList());
		
		grid.setRows(normal);
		table.setRows(crucial, list);
	}
	
	public void clear() {
		grid.setRows(new ArrayList<ModbusRegister>());
		table.setRows(new ArrayList<ModbusRegister>(), new ArrayList<ModbusRegister>());
	}

}
