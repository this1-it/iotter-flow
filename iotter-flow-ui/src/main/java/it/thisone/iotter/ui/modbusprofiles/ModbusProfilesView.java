package it.thisone.iotter.ui.modbusprofiles;

import org.springframework.beans.factory.ObjectProvider;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import it.thisone.iotter.ui.MainLayout;
import it.thisone.iotter.ui.common.BaseView;

@Route(value = ModbusProfilesView.NAME, layout = MainLayout.class)
@PageTitle("Modbus Profiles")
public class ModbusProfilesView extends BaseView {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "modbusprofiles";

	private final ObjectProvider<ModbusProfileListing> listingProvider;
	private boolean initialized;

	public ModbusProfilesView(ObjectProvider<ModbusProfileListing> listingProvider) {
		this.listingProvider = listingProvider;
		setSizeFull();
		addClassName(NAME);
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		if (initialized) {
			return;
		}
		initialized = true;
		ModbusProfileListing listing = listingProvider.getObject();
		listing.init();
		add(listing.getMainLayout());
		setFlexGrow(1, listing.getMainLayout());
	}

	@Override
	public String getI18nKey() {
		return NAME;
	}
}
