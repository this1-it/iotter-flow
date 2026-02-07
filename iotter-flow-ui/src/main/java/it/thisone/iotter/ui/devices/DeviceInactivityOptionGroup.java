package it.thisone.iotter.ui.devices;

import java.util.LinkedHashMap;
import java.util.Map;

import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;

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
		items.put(0, getTranslation("basic.editor.no"));
		items.put(Constants.Provisioning.INACTIVITY_MINUTES, getTranslation("basic.editor.yes"));
		
		// Set items and captions
		setItems(items.keySet());
		setRenderer(new ComponentRenderer<>(value -> new Span(items.get(value))));
		
		// Apply horizontal styling through app css
		addClassName("option-group-horizontal");
	}

}
