package it.thisone.iotter.ui.common.charts;

import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.PlotOptionsSeries;

public class PlotOptionsColumn extends PlotOptionsSeries {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
    @Override
    public ChartType getChartType() {
        return ChartType.COLUMN;
    }

}
