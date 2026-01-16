package it.thisone.iotter.util;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.googlecode.jatl.Html;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.UI;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.common.UIUtils;
//import it.thisone.iotter.ui.main.IMainUI;
import it.thisone.iotter.ui.main.IMainUI;

public class MapUtils implements Serializable {
	public static final String GOOGLE_COM_RECAPTCHA_API_JS = "https://www.google.com/recaptcha/api.js";
	// https://sites.google.com/site/gmapsdevelopment/
//	private static final String GREEN_DOT = "https://maps.google.com/mapfiles/ms/micons/green-dot.png";
//	private static final String RED_DOT = "https://maps.google.com/mapfiles/ms/micons/red-dot.png";
//	private static final String YELLOW_DOT = "https://maps.google.com/mapfiles/ms/micons/red-dot.png";

	private static final String GREEN_DOT = "https://aernet.aermec.com/VAADIN/themes/aernet/micons/green-dot.png";
	private static final String RED_DOT = "https://aernet.aermec.com/VAADIN/themes/aernet/micons/red-dot.png";
	private static final String YELLOW_DOT = "https://aernet.aermec.com/VAADIN/themes/aernet/micons/red-dot.png";

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8826738658600702437L;


	public static String getIconUrl(Device device) {
		String icon = RED_DOT;
		if (device.getStatus().equals(DeviceStatus.CONNECTED) && !device.getChannels().isEmpty()) {
			icon = GREEN_DOT;
		} else  {
			icon = YELLOW_DOT;
		}
		
		if (device.isAlarmed()) {
			if (UIUtils.getServiceFactory().getAlarmService().hasActiveAlarms(device.getSerial())) {
				icon = RED_DOT;
			}
		}

		Date lastContactDate = UIUtils.getCassandraService().getFeeds().getLastContact(device.getSerial());
		device.setLastContactDate(lastContactDate);
		
		if (device.checkInactive(lastContactDate)) {
			icon = RED_DOT;
		}
		
		return icon;
	}

	public static Map<Device, Set<GroupWidget>> mappableDevices(Network network) {
		Collection<NetworkGroup> groups = new ArrayList<NetworkGroup>();
		UserDetailsAdapter details = ((IMainUI) UI.getCurrent()).getUserDetails();
		if (details == null) {
			if (network.isAnonymous()) {
				groups = UIUtils.getServiceFactory().getNetworkGroupService().findByNetwork(network);
			}
		}
		else {
			if (details.getNetworkId() !=null && details.getNetworkId().equals(network.getId())) {
				if (details.hasRole(Constants.ROLE_USER)) {
					User user = UIUtils.getServiceFactory().getUserService().findOne(details.getUserId());
					// groups = user.getGroups();
					// Feature #1884
					for (NetworkGroup group : user.getGroups()) {
						if (group.getNetwork().equals(network)) {
							groups.add(group);
						}		
					}
				}
				else {
					groups = UIUtils.getServiceFactory().getNetworkGroupService().findByNetwork(network);
				}

//				User user = UIUtils.getServiceFactory().getUserService().findOne(details.getUserId());
//				// Feature #1884 Bug #2053
//				for (NetworkGroup group : user.getGroups()) {
//					if (group.getNetwork().equals(network)) {
//						groups.add(group);
//					}		
//				}

				
				
			}
			else if (details.hasRole(Constants.ROLE_ADMINISTRATOR)) {
				if (network.getOwner().equals(details.getTenant())) {
					groups = UIUtils.getServiceFactory().getNetworkGroupService().findByNetwork(network);
				}
			}
			else if (details.hasRole(Constants.ROLE_SUPERVISOR)) {
				groups = UIUtils.getServiceFactory().getNetworkGroupService().findByNetwork(network);
			}
		}
		
		
		
		
		Map<Device, Set<GroupWidget>> map = UIUtils.getServiceFactory().getDeviceService().findMappableDevices(groups);
		return map;
	}
	
	
	public static String divIcon(GraphicFeed feed, String label, String measure) {
		
		if (feed.getLabel() != null && !feed.getLabel().isEmpty()) {
			label = feed.getLabel();
		}
		
		StringWriter writer = new StringWriter();
		Html markup = new Html(writer);
		String color = feed.getOptions().getFillColor();
		markup.div().id(feed.getKey()).style("background:" + color + "; padding: 4px; border-radius: 10px; -moz-border-radius: 10px;" );
		markup.raw(label);
		markup.raw("<br/>");
		//markup.raw(VaadinIcon.SIGNAL.create());
		markup.raw("&nbsp;");
		if (measure != null) {
			markup.raw(measure);
		}
		markup.end();
		markup.done();
		return writer.toString();
	}

	

}
