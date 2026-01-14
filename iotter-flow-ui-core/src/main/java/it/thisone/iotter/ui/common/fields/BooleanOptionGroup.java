package it.thisone.iotter.ui.common.fields;

import java.util.LinkedHashMap;
import java.util.Map;

import com.vaadin.flow.component.radiobutton.RadioButtonGroup;

import it.thisone.iotter.ui.common.UIUtils;

public class BooleanOptionGroup extends RadioButtonGroup<Boolean> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public BooleanOptionGroup() {
		super();
		
		// Create the items map (sorted with false first, then true)
		Map<Boolean, String> items = new LinkedHashMap<>();
		items.put(false, UIUtils.localize("enum.boolean.false"));
		items.put(true, UIUtils.localize("enum.boolean.true"));
		
		// Set items and captions
		setItems(items.keySet());
		setLabelCaptionGenerator(items::get);
	}
	

}
