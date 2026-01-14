package it.thisone.iotter.ui.common.fields;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

import com.vaadin.flow.component.radiobutton.RadioButtonGroup;

import it.thisone.iotter.enums.NetworkType;
import it.thisone.iotter.ui.common.UIUtils;

public class NetworkTypeOptionGroup extends RadioButtonGroup<NetworkType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NetworkTypeOptionGroup() {
		super();
		
		// Set enum values as items, excluding LIST and sorted by enum ordinal
		setItems(Arrays.stream(NetworkType.values())
			.filter(type -> type != NetworkType.LIST)
			.sorted(Comparator.comparing(Enum::ordinal))
			.collect(Collectors.toList()));
		
		// Set caption generator to use i18n keys
		setLabelCaptionGenerator(type -> UIUtils.localize(type.getI18nKey()));
	}
	

}
