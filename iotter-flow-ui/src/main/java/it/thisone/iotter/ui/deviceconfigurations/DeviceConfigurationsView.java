package it.thisone.iotter.ui.deviceconfigurations;

import org.springframework.beans.factory.ObjectProvider;
import org.vaadin.flow.components.TabSheet;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import it.thisone.iotter.ui.MainLayout;
import it.thisone.iotter.ui.common.BaseView;
import it.thisone.iotter.ui.modbusprofiles.ModbusProfileListing;

@Route(value = DeviceConfigurationsView.VIEW_NAME, layout = MainLayout.class)
@PageTitle("Device Configurations")
public class DeviceConfigurationsView extends BaseView {

	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "deviceconfigurations";

	private final ObjectProvider<ModbusProfileListing> modbusProfileListingProvider;
	private final ObjectProvider<DeviceModelsListing> deviceModelsListingProvider;
	private final ObjectProvider<MeasureUnitTypesListing> measureUnitTypesListingProvider;
	private final ObjectProvider<MeasureSensorTypesListing> measureSensorTypesListingProvider;
	private boolean initialized;

	public DeviceConfigurationsView(
			ObjectProvider<ModbusProfileListing> modbusProfileListingProvider,
			ObjectProvider<DeviceModelsListing> deviceModelsListingProvider,
			ObjectProvider<MeasureUnitTypesListing> measureUnitTypesListingProvider,
			ObjectProvider<MeasureSensorTypesListing> measureSensorTypesListingProvider) {
		this.modbusProfileListingProvider = modbusProfileListingProvider;
		this.deviceModelsListingProvider = deviceModelsListingProvider;
		this.measureUnitTypesListingProvider = measureUnitTypesListingProvider;
		this.measureSensorTypesListingProvider = measureSensorTypesListingProvider;
		setSizeFull();
		addClassName(VIEW_NAME);
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		if (initialized) {
			return;
		}
		initialized = true;

		TabSheet tabsheet = new TabSheet();
		tabsheet.setSizeFull();

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(false);
		mainLayout.setPadding(false);
		mainLayout.add(tabsheet);
		mainLayout.setFlexGrow(1, tabsheet);

		add(mainLayout);
		setFlexGrow(1, mainLayout);

		ModbusProfileListing profiles = modbusProfileListingProvider.getObject();
		profiles.init();
		Tab profilesTab = tabsheet.addTab(profiles.getI18nLabel("title"), profiles.getMainLayout());
		tabsheet.setSelectedTab(profilesTab);
		
		DeviceModelsListing models = deviceModelsListingProvider.getObject();
		models.init();
		tabsheet.addTab(models.getI18nLabel("title"), models.getMainLayout());


		MeasureUnitTypesListing units = measureUnitTypesListingProvider.getObject();
		units.init();
		tabsheet.addTab(units.getI18nLabel("title"), units.getMainLayout());

		MeasureSensorTypesListing sensors = measureSensorTypesListingProvider.getObject();
		sensors.init();
		tabsheet.addTab(sensors.getI18nLabel("title"), sensors.getMainLayout());
	}

	@Override
	public String getI18nKey() {
		return VIEW_NAME;
	}
}
