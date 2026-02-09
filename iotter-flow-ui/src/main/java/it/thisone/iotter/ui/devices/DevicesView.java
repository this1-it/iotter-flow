package it.thisone.iotter.ui.devices;

import org.springframework.beans.factory.ObjectProvider;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.service.NetworkService;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.MainLayout;
import it.thisone.iotter.ui.common.AuthenticatedUser;
import it.thisone.iotter.ui.common.BaseView;

@Route(value = "devices", layout = MainLayout.class)
@PageTitle("Devices")
public class DevicesView extends BaseView {
	private static final long serialVersionUID = 1L;

	public static final String VIEW_NAME = "devices";
	private final ObjectProvider<DevicesListing> listingProvider;
	private final NetworkService networkService;
	private final AuthenticatedUser authenticatedUser;
	private boolean initialized;

	public DevicesView(ObjectProvider<DevicesListing> listingProvider, NetworkService networkService,
			AuthenticatedUser authenticatedUser) {
		this.listingProvider = listingProvider;
		this.networkService = networkService;
		this.authenticatedUser = authenticatedUser;
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
		Network network = null;
		UserDetailsAdapter details = authenticatedUser.get().orElse(null);
		if (details != null && details.hasRole(Constants.ROLE_SUPERUSER)) {
			network = networkService.findOne(details.getNetworkId());
		}
		DevicesListing listing = listingProvider.getObject();
		listing.init(network);
		add(listing.getMainLayout());
		setFlexGrow(1, listing.getMainLayout());
	}

	@Override
	public String getI18nKey() {
		return VIEW_NAME;
	}
}
