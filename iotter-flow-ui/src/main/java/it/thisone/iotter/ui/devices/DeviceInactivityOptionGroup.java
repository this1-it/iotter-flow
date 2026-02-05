package it.thisone.iotter.ui.devices;

import java.util.LinkedHashMap;
import java.util.Map;

import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.themes.ValoTheme;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.ui.common.UIUtils;

public class DeviceInactivityOptionGroup extends RadioButtonGroup<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5037072247429655516L;

	public DeviceInactivityOptionGroup() {
		super();
		
		// Create the items map
		Map<Integer, String> items = new LinkedHashMap<>();
		items.put(0, UIUtils.localize("basic.editor.no"));
		items.put(Constants.Provisioning.INACTIVITY_MINUTES, UIUtils.localize("basic.editor.yes"));
		
		// Set items and captions
		setItems(items.keySet());
		setItemCaptionGenerator(items::get);
		
		// Apply horizontal styling
		addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
	}

}
