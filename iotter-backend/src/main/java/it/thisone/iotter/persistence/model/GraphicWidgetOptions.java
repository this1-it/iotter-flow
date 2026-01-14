package it.thisone.iotter.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import it.thisone.iotter.enums.ChartScaleType;
import it.thisone.iotter.enums.Order;

@Embeddable
public class GraphicWidgetOptions implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3384637807636294207L;


	public GraphicWidgetOptions() {
		super();
		scale = ChartScaleType.LINEAR;
		autoScale = true;
		showGrid = false;
		realTime = false;
		localControls = false;
		showControls = false;
		showMarkers = false;
		exporting = true;
		order = Order.DESCENDING;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "SCALE")
	private ChartScaleType scale;

	@Column(name = "SHOW_GRID")
	private Boolean showGrid;
    
	@Column(name = "REAL_TIME")
	private Boolean realTime;

	@Column(name = "LOCAL_CONTROLS")
	private Boolean localControls;

	@Column(name = "SHOW_CONTROLS")
	private Boolean showControls;
	
	@Column(name = "EXPORTING")
	private Boolean exporting;
	
	@Column(name = "AUTO_SCALE")
	private Boolean autoScale;
	
	@Column(name = "SHOW_MARKERS")
	private Boolean showMarkers;

	@Column(name = "FILL_COLOR")
	private String fillColor;

	@Enumerated(EnumType.STRING)
	@Column(name = "TABLE_ORDER")
	private Order order;
	
	@Transient
	private Boolean showLegend = true;

	@Transient
	private Boolean zoomable = true;
	
	public ChartScaleType getScale() {
		return scale;
	}

	public void setScale(ChartScaleType scale) {
		this.scale = scale;
	}

	public Boolean getShowGrid() {
		return showGrid;
	}

	public void setShowGrid(Boolean showGrid) {
		this.showGrid = showGrid;
	}

	public Boolean getRealTime() {
		return realTime;
	}

	public void setRealTime(Boolean realTime) {
		this.realTime = realTime;
	}

	public Boolean getLocalControls() {
		return localControls;
	}

	public void setLocalControls(Boolean timeControls) {
		this.localControls = timeControls;
	}

	public Boolean getAutoScale() {
		return autoScale;
	}

	public void setAutoScale(Boolean autoScale) {
		this.autoScale = autoScale;
	}

	public Boolean getShowControls() {
		return showControls;
	}

	public void setShowControls(Boolean showControls) {
		this.showControls = showControls;
	}

	public Boolean getExporting() {
		return exporting;
	}

	public void setExporting(Boolean exporting) {
		this.exporting = exporting;
	}

	public Boolean getShowMarkers() {
		return showMarkers;
	}

	public void setShowMarkers(Boolean showMarkers) {
		this.showMarkers = showMarkers;
	}

	public String getFillColor() {
		return fillColor;
	}

	public void setFillColor(String fillColor) {
		this.fillColor = fillColor;
	}

	public Boolean getShowLegend() {
		return showLegend;
	}

	public void setShowLegend(Boolean showLegend) {
		this.showLegend = showLegend;
	}

	public Boolean getZoomable() {
		return zoomable;
	}

	public void setZoomable(Boolean zoomable) {
		this.zoomable = zoomable;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}


}
