package it.thisone.iotter.ui.common.fields;

import java.util.Arrays;

import com.vaadin.flow.component.radiobutton.RadioButtonGroup;

import it.thisone.iotter.enums.Order;
import it.thisone.iotter.ui.common.UIUtils;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.component.html.Span;


public class OrderOptionGroup extends RadioButtonGroup<Order> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public OrderOptionGroup() {
		super();
		
		// Set enum values as items
		setItems(Arrays.asList(Order.values()));
		



        setRenderer(new ComponentRenderer<>(value ->
                new Span( getTranslation(value.getI18nKey()) )
        ));

		
	}
	

}
