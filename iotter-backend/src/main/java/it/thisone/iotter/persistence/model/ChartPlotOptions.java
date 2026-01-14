package it.thisone.iotter.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import it.thisone.iotter.enums.ChartAxis;


/**
 * Bug #187 [PERSISTENCE] GraphPlotOptions default values are not correct
 * spline from com.vaadin.addon.charts.model.ChartType
   CIRCLE from it.thisone.iotter.ui.charts.DedaloMarkerEnum
   SOLID from com.vaadin.addon.charts.model.DashStyle
 * @author tisone
 *
 */
@Embeddable
public class ChartPlotOptions implements Serializable {
	public static String DEFAULT_CHARTYPE = "spline";
	public static String DEFAULT_MARKERSYMBOL = "CIRCLE";
	public static String DEFAULT_DASHSTYLE = "SOLID";
	public static String ARROW_MARKERSYMBOL = "ARROW";
	/**
	 * 
	 */
	private static final long serialVersionUID = -6370754483032470523L;

	public ChartPlotOptions() {
		super();
		chartType = DEFAULT_CHARTYPE;
		markerSymbol = DEFAULT_MARKERSYMBOL;
		dashStyle = DEFAULT_DASHSTYLE;
		axis = ChartAxis.Y.name();
		axisTitle = true;
	}

	public boolean hasMarkerReference() {
		boolean arrow = getMarkerSymbol().equals(ARROW_MARKERSYMBOL);
		String markerReference = getFeedReference();
		return (markerReference != null && arrow);
	}
	
	@Column(name = "CHART_TYPE")
	private String chartType;
	
	@Column(name = "MARKER_SYMBOL")
	private String markerSymbol;
	
	@Column(name = "DASH_STYLE")
	private String dashStyle;
	
	@Column(name = "FILL_COLOR")
	private String fillColor;
	
	@Column(name = "MARKER_REF")
	private String feedReference;

	@Column(name = "AXIS")
	private String axis;
	
	@Column(name = "AXIS_TITLE") 
	private boolean axisTitle;
	
	private MeasureRange extremes;
	
	public String getChartType() {
		return chartType;
	}

	public void setChartType(String chartType) {
		this.chartType = chartType;
	}

	public String getMarkerSymbol() {
		if (markerSymbol == null) {
			markerSymbol = DEFAULT_MARKERSYMBOL;
		}
		return markerSymbol;
	}

	public void setMarkerSymbol(String markerSymbol) {
		this.markerSymbol = markerSymbol;
	}

	public String getDashStyle() {
		return dashStyle;
	}

	public void setDashStyle(String dashStyle) {
		this.dashStyle = dashStyle;
	}

	public String getFillColor() {
		return fillColor;
	}

	public void setFillColor(String fillColor) {
		this.fillColor = fillColor;
	}

	public String getFeedReference() {
		return feedReference;
	}

	public void setFeedReference(String reference) {
		this.feedReference = reference;
	}

	public String getAxis() {
		return axis;
	}

	public void setAxis(String axis) {
		this.axis = axis;
	}

	public MeasureRange getExtremes() {
		return extremes;
	}

	public void setExtremes(MeasureRange range) {
		this.extremes = range;
	}

	public boolean isAxisTitle() {
		return axisTitle;
	}

	public void setAxisTitle(boolean axisTitle) {
		this.axisTitle = axisTitle;
	}
	
}
