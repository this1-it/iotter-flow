package it.thisone.iotter.ui.users;

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

@Route(value = "users", layout = MainLayout.class)
@PageTitle("Users")
public class UsersView extends BaseView {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "users";

	private final ObjectProvider<UsersListing> listingProvider;
	private final NetworkService networkService;
	private final AuthenticatedUser authenticatedUser;
	private boolean initialized;

	public UsersView(ObjectProvider<UsersListing> listingProvider, NetworkService networkService,
			AuthenticatedUser authenticatedUser) {
		this.listingProvider = listingProvider;
		this.networkService = networkService;
		this.authenticatedUser = authenticatedUser;
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
		Network network = null;
		UserDetailsAdapter details = authenticatedUser.get().orElse(null);
		if (details != null && details.hasRole(Constants.ROLE_SUPERUSER)) {
			network = networkService.findOne(details.getNetworkId());
		}
		UsersListing listing = listingProvider.getObject();
		listing.init(network);
		add(listing.getMainLayout());
		setFlexGrow(1, listing.getMainLayout());
	}

	@Override
	public String getI18nKey() {
		return NAME;
	}
}
