package it.thisone.iotter.ui.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.UI;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.main.BaseView;
import it.thisone.iotter.ui.main.IMainUI;

public class DevicesView extends BaseView {
	private static Logger logger = LoggerFactory.getLogger(DevicesView.class);

	private static final long serialVersionUID = 1L;

	public static final String NAME = "devices";

	@Override
	public void enter(ViewChangeEvent event) {
		// Initialize view on enter
		setSizeFull();

		UIUtils.startWidgetRefresher();

		Network network = null;
		UserDetailsAdapter details = ((IMainUI) UI.getCurrent()).getUserDetails();
		if (details != null && details.hasRole(Constants.ROLE_SUPERUSER)) {
			network = UIUtils.getServiceFactory().getNetworkService().findOne(details.getNetworkId());
		}

		// Use Spring-managed DeviceListing prototype bean
		DevicesListing listing = ((IMainUI) UI.getCurrent()).getBean(DevicesListing.class);
		listing.init(network);
		listing.setRootComposition(null);
		addComponent(listing.getMainLayout());
		setExpandRatio(listing.getMainLayout(), 1);
	}

	@Override
	public String getI18nKey() {
		return NAME;
	}
}
