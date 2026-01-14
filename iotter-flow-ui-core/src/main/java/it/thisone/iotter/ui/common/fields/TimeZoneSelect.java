package it.thisone.iotter.ui.common.fields;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.vaadin.flow.component.combobox.ComboBox;

import it.thisone.iotter.ui.common.UIUtils;

public class TimeZoneSelect extends ComboBox<TimeZone> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static List<TimeZone> getTimeZones() {
		List<TimeZone> timeZones = new ArrayList<>();
		String[] ids = TimeZone.getAvailableIDs();
		for (String id : ids) {
			timeZones.add(TimeZone.getTimeZone(id));
		}
		return timeZones;
	}

	public TimeZoneSelect() {
		super(UIUtils.localize("basic.combobox.timezone"));
		
		// Set the timezone list as items
		setItems(getTimeZones());
		
		// Set custom item caption generator to show timezone with offset
		setLabelCaptionGenerator(this::formatTimeZone);
		
		// Configuration
		setWidth(16, Unit.EM);
		setEmptySelectionAllowed(false);
		setTextInputAllowed(true);
		setPageLength(10);
	}

	private String formatTimeZone(TimeZone timeZone) {
		SimpleDateFormat sdf = new SimpleDateFormat("XXX");
		sdf.setTimeZone(timeZone);
		Date now = new Date();
		return String.format("%s [%s]", timeZone.getID(), sdf.format(now));
	}
}
