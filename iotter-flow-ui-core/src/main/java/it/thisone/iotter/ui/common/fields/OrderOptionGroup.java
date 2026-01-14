package it.thisone.iotter.ui.common.fields;

import java.util.Arrays;

import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.themes.ValoTheme;

import it.thisone.iotter.enums.Order;
import it.thisone.iotter.ui.common.UIUtils;

public class OrderOptionGroup extends RadioButtonGroup<Order> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public OrderOptionGroup() {
		super();
		
		// Set enum values as items
		setItems(Arrays.asList(Order.values()));
		
		// Set caption generator to use i18n keys
		setLabelCaptionGenerator(order -> UIUtils.localize(order.getI18nKey()));
		
		// Apply horizontal styling
		setStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
	}
	

}
