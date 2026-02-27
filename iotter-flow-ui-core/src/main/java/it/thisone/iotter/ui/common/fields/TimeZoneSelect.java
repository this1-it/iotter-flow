package it.thisone.iotter.ui.common.fields;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.combobox.ComboBox;

public class TimeZoneSelect extends ComboBox<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static List<String> getTimeZones() {
		List<String> timeZones = new ArrayList<>();
		String[] ids = TimeZone.getAvailableIDs();
		for (String id : ids) {
			timeZones.add(id);
		}
		return timeZones;
	}

	public TimeZoneSelect() {
		super("basic.combobox.timezone");
		
		// Set the timezone list as items
		setItems(getTimeZones());
		
		// Set custom item caption generator to show timezone with offset
		setItemLabelGenerator(this::formatTimeZone);
		
		// Configuration
		setWidth(16, Unit.EM);
//		setEmptySelectionAllowed(false);
//		setTextInputAllowed(true);
//		setPageLength(10);
	}

	private String formatTimeZone(String zoneId) {
		TimeZone timeZone = TimeZone.getTimeZone(zoneId);
		SimpleDateFormat sdf = new SimpleDateFormat("XXX");
		sdf.setTimeZone(timeZone);
		Date now = new Date();
		return String.format("%s [%s]", timeZone.getID(), sdf.format(now));
	}
}
