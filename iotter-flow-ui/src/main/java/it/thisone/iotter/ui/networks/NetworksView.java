package it.thisone.iotter.ui.networks;

import org.springframework.beans.factory.ObjectProvider;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import it.thisone.iotter.ui.MainLayout;
import it.thisone.iotter.ui.common.BaseView;

@Route(value = "networks", layout = MainLayout.class)
@PageTitle("Networks")
public class NetworksView extends BaseView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String VIEW_NAME = "networks";
	private final ObjectProvider<NetworkListing> listingProvider;
	private boolean initialized;
	
	@Override
	public String getI18nKey() {
		return VIEW_NAME;
	}
	
	public NetworksView(ObjectProvider<NetworkListing> listingProvider) {
		this.listingProvider = listingProvider;
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
		NetworkListing listing = listingProvider.getObject();
		listing.init();
		add(listing.getMainLayout());
		setFlexGrow(1, listing.getMainLayout());
	}

}
