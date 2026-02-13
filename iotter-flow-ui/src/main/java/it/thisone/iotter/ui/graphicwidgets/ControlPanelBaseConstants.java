package it.thisone.iotter.ui.graphicwidgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.ModbusRegister;
import it.thisone.iotter.provisioning.AernetXLSXParserConstants;

public interface ControlPanelBaseConstants {
	public static final String CONTROL_PANEL_WIDGET_PROVIDER = "AernetPro";

//	Feature #1886 remove status histogram
//	public static final String HISTOGRAM = "histogram";
//	public static final String STATUS = "status";
	// public static final int STATUS_POS = 3;
	// public static final int HISTOGRAM_POS = 1;

	public static final String POSITION_ID = "position_id";
	public static final String ICON = "icon";
	public static final String TYPE = "type";
	public static final String SECTION_ID = "section_id";
	public static final String FEED = "feed";
	public static final String FEED_NAME = "feed_name";
	public static final String SERIES = "series";
	public static final String QUICKCOM = "quickcom";
	public static final String RESET = "reset";
	public static final String SETPOINT = "setpoint";
	public static final String ASCII = "ascii";

	public static final int SERIES_POS = 20;
	public static final int SERIES_POS_CHECKED = 10;
	public static final int QUICKCOM_POS = 2;
	public static final int RESET_POS = 1;
	public static final int SETPOINT_POS = 8;
	public static final int ASCII_POS = 16;
	public static final String[] NAMES = new String[] { //
			QUICKCOM, RESET, //
			SERIES, SETPOINT, ASCII };
	public static final String[] ICONS = new String[] { QUICKCOM, RESET };

	public static List<GraphicFeed> createSectionFeeds(List<ModbusRegister> items) {
		List<GraphicFeed> feeds = new ArrayList<GraphicFeed>();
		for (ModbusRegister item : items) {
			if (item.isAvailable()) {
				GraphicFeed feed = createGraphicFeed(item);
				if (feed.getSection() != null) {
					if (feed.getSection().startsWith(SERIES)) {
						long count = feeds.stream().filter(GraphicFeed::isChecked).collect(Collectors.counting());
						feed.setChecked(count < SERIES_POS_CHECKED);
					}
					feeds.add(feed);
				}
			}
		}
		return feeds;
	}

	public static GraphicFeed createGraphicFeed(ModbusRegister register) {
		GraphicFeed feed = new GraphicFeed();
		feed.setLabel(register.getDisplayName());
		feed.setMetaData(register.getMetaData());
		String sectionId = register.getAdditionalProperties().get(AernetXLSXParserConstants.CONTROL_PANEL_NAME);
		if (sectionId != null) {
			sectionId = sectionId.toLowerCase();
			String sectionName = sectionId;
			if (sectionId.indexOf(":") > 0) {
				sectionName = sectionId.substring(0, sectionId.indexOf(":"));
				feed.setSection(sectionId);
			}
			String iconSet = register.getAdditionalProperties().get(AernetXLSXParserConstants.ICONSET_NAME);
			if (iconSet != null && Arrays.asList(ICONS).contains(sectionName)) {
				feed.setResourceID(iconSet);
			}
		}
		return feed;
	}
}
