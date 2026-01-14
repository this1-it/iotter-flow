package it.thisone.iotter.ui.common.fields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.combobox.ComboBox;

import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.util.BacNet;

public class BacNetMeasureUnitSelect extends ComboBox<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final Object PROPERTY_NAME = "name";
	private Map<String, String> unitDescriptions = new HashMap<>();

	private List<String> getUnitCodes() {
		List<String> codes = new ArrayList<>();
		unitDescriptions.clear();
		
		for (int unit = 0; unit < 255; unit++) {
			String description = BacNet.lookUp(unit);
			if (description != null && !description.trim().isEmpty()) {
				String code = String.valueOf(unit);
				codes.add(code);
				unitDescriptions.put(code, description);
			}
		}
		return codes;
	}


	public BacNetMeasureUnitSelect() {
		super(UIUtils.localize("basic.combobox.bacnet"));
		initialize();
	}

	private void initialize() {
		// Set items and caption generator
		setItems(getUnitCodes());
		setLabelCaptionGenerator(code -> unitDescriptions.getOrDefault(code, code));
		
		// Set a reasonable width
		setWidth(16, Unit.EM);
		// Disallow null selections
		setEmptySelectionAllowed(false);
		setTextInputAllowed(true);
		setPageLength(10);
	}
}
