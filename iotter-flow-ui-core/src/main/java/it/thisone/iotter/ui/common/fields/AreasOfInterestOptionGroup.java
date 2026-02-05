package it.thisone.iotter.ui.common.fields;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

import com.vaadin.flow.component.checkbox.CheckboxGroup;

import it.thisone.iotter.enums.AreasOfInterest;
import it.thisone.iotter.ui.common.UIUtils;

public class AreasOfInterestOptionGroup extends CheckboxGroup<AreasOfInterest> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public AreasOfInterestOptionGroup() {
		super();
		
		// Set enum values as items, sorted by localized name
		setItems(Arrays.stream(AreasOfInterest.values())
			.sorted(Comparator.comparing(area -> getTranslation(area.getI18nKey())))
			.collect(Collectors.toList()));
		
		// Set caption generator to use i18n keys
		setItemLabelGenerator(area -> getTranslation(area.getI18nKey()));
	}
	

}
