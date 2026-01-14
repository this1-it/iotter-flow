package it.thisone.iotter.ui.eventbus;

import java.io.Serializable;

import it.thisone.iotter.enums.ChartScaleType;
import it.thisone.iotter.persistence.model.GraphicWidgetOptions;


public class GraphWidgetOptionsEvent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String source;
	
	private final ChartScaleType scale;
	private final Boolean showGrid;
 	private final Boolean realTime;
	private final Boolean showMarkers;
	private final Boolean autoScale;

	
	public GraphWidgetOptionsEvent(String source, GraphicWidgetOptions options) {
		this.autoScale = options.getAutoScale();
		this.realTime = options.getRealTime();
		this.source = source;
		this.scale = options.getScale();
		this.showGrid = options.getShowGrid();
		this.showMarkers = options.getShowMarkers();
		
	}




	public String getSource() {
		return source;
	}


	public ChartScaleType getScale() {
		return scale;
	}


	public Boolean getShowGrid() {
		return showGrid;
	}


	public Boolean getRealTime() {
		return realTime;
	}


	public Boolean getShowMarkers() {
		return showMarkers;
	}

	public Boolean getAutoScale() {
		return autoScale;
	}
}
