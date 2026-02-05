package it.thisone.iotter.ui.charts;

import java.util.Date;

import com.vaadin.addon.charts.Chart;
//import com.vaadin.addon.charts.ChartSelectionEvent;
//import com.vaadin.addon.charts.ChartSelectionListener;
import com.vaadin.addon.charts.model.AxisTitle;
import com.vaadin.addon.charts.model.AxisType;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.Hover;
import com.vaadin.addon.charts.model.Labels;
import com.vaadin.addon.charts.model.Marker;
import com.vaadin.addon.charts.model.PlotBand;
import com.vaadin.addon.charts.model.PlotOptionsArea;
import com.vaadin.addon.charts.model.States;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.charts.model.ZoomType;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.model.TimeInterval;

	// TODO(flow-migration): this class still contains Vaadin 8 APIs and needs manual Flow refactor.
public class TimeLineChartAdapter extends MultiTraceChartAdapter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1879789719298795301L;
	private Date dataSetStart;
	private Date dataSetEnd;
	private GraphicFeed feed;
	
	public TimeLineChartAdapter(GraphicWidget widget) {
		super(widget);
	}

	@Override
	protected Component buildVisualization() {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		if (getGraphWidget().getFeeds().isEmpty()) {
			return layout;
		}
		feed = getGraphWidget().getFeeds().get(0);
		
		if (feed.getChannel() != null && feed.getChannel().getConfiguration().getActivationDate() != null) {
			dataSetStart = feed.getChannel().getConfiguration().getActivationDate();
			dataSetEnd = new Date();
		}
		else {
			TimeInterval interval = intervalField.getHelper().currentDay(new Date());
			dataSetStart = interval.getStartDate();
			dataSetEnd = interval.getEndDate();
			
		}
		
		

		final Chart masterChart = getMasterChart();
		final Chart detailChart = getDetailChart();
		
		setChart(detailChart);

//		detailChart.addXAxesExtremesChangeListener(new XAxesExtremesChangeListener() {
//			private static final long serialVersionUID = 1L;
//			@Override
//			public void onXAxesExtremesChange(XAxesExtremesChangeEvent event) {
//				long start = event.getMinimum().longValue();
//				long end = event.getMaximum().longValue();
//				Date startZoom = ChartUtils.toNetworkDate(start, getNetworkTimeZone());
//				Date endZoom = ChartUtils.toNetworkDate(end, getNetworkTimeZone());
//				// no need to use UI.getCurrent().access(...)
//				intervalField.setValue(new TimeInterval(startZoom, endZoom));
//			}
//		});

//		masterChart.addChartSelectionListener(new ChartSelectionListener() {
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public void onSelection(ChartSelectionEvent event) {
//				long start = event.getSelectionStart().longValue();
//				long end = event.getSelectionEnd().longValue();
//				Date startZoom = ChartUtils.toNetworkDate(start, getNetworkTimeZone());
//				Date endZoom = ChartUtils.toNetworkDate(end, getNetworkTimeZone());
//
//				// set plot band to highlight the selection on the master chart
//				PlotBand plotBand1 = new PlotBand();
//				PlotBand plotBand2 = new PlotBand();
//				plotBand1.setColor(new SolidColor(0, 0, 0, 0.2));
//				plotBand2.setColor(new SolidColor(0, 0, 0, 0.2));
//				plotBand1.setFrom(startZoom);
//				plotBand1.setTo(start);
//				plotBand2.setFrom(end);
//				plotBand2.setTo(endZoom);
//				masterChart.getConfiguration().getxAxis().setPlotBands(plotBand1, plotBand2);
//				masterChart.drawChart();
//
//
//				intervalField.setValue(new TimeInterval(startZoom, endZoom));
//
//
//				//detailChart.getConfiguration().getxAxis().setExtremes(start, end);
//
//			}
//
//		});

		layout.add(detailChart);
		layout.add(masterChart);
		layout.setFlexGrow(1, detailChart);
		return layout;
	}

	private Chart getDetailChart() {
		Chart detailChart = new Chart();
		detailChart.setId("detail-chart");
//		detailChart.setHeight("100%");
//		detailChart.setWidth("100%");
		Configuration configuration = detailChart.getConfiguration();

		configuration.getCredits().setEnabled(false);
		configuration.setTitle("");
		configuration.setSubTitle("");
		configuration.getxAxis().setType(AxisType.DATETIME);
		configuration.getyAxis().setTitle(new AxisTitle((String) null));
		configuration.getyAxis().setMinRange(0.1);
		configuration.getTooltip().setXDateFormat(ChartUtils.X_DATEFORMAT);
		configuration.getTooltip().setShared(true);
		configuration.getLegend().setEnabled(false);
		configuration.setExporting(false);

		detailChart.drawChart(configuration);
		return detailChart;
	}

	private Chart getMasterChart() {
		Chart masterChart = new Chart(ChartType.AREA);
//		masterChart.setHeight("80px");
//		masterChart.setWidth("100%");
		masterChart.setId("master-chart");

		Configuration configuration = masterChart.getConfiguration();
		configuration.getChart().setZoomType(ZoomType.X);

		configuration.getChart().setReflow(false);
		configuration.getChart().setBorderWidth(0);
		configuration.getChart().setBackgroundColor(null);
		configuration.getChart().setMarginLeft(50);
		configuration.getChart().setMarginRight(20);

		configuration.getTitle().setText("");

		configuration.getxAxis().setType(AxisType.DATETIME);
		configuration.getxAxis().setShowLastLabel(true);
		configuration.getxAxis().setMinRange(14 * DAY_IN_MILLIS);
		configuration.getxAxis().setTitle(new AxisTitle(""));

		PlotBand mask = new PlotBand();
		mask.setColor(new SolidColor(0, 0, 0, 0.2));
		mask.setFrom(ChartUtils.toHighchartsTS(dataSetStart, getNetworkTimeZone()));
		mask.setTo(ChartUtils.toHighchartsTS(dataSetEnd, getNetworkTimeZone()));
		configuration.getxAxis().setPlotBands(mask);

		YAxis yAxis = configuration.getyAxis();
		yAxis.setGridLineWidth(0);
		yAxis.setLabels(new Labels(false));
		yAxis.setTitle(new AxisTitle(""));
		yAxis.setMin(0.6);
		yAxis.setShowFirstLabel(false);

		configuration.getTooltip().setEnabled(false);

		configuration.getLegend().setEnabled(false);
		configuration.getCredits().setEnabled(false);

		PlotOptionsArea plotOptions = new PlotOptionsArea();
		plotOptions.setLineWidth(1);
		plotOptions.setShadow(false);
		Hover hover = new Hover();
		hover.setLineWidth(1);
		States states = new States();
		states.setHover(hover);
		plotOptions.setStates(states);
		plotOptions.setEnableMouseTracking(false);
		plotOptions.setAnimation(false);
		configuration.setPlotOptions(plotOptions);
		TimeInterval interval = new TimeInterval(dataSetStart, dataSetEnd);
		DataSeries ds = getTimeSeries(feed, interval, false, null);
		//ds.setyAxis(1);
		PlotOptionsArea masterPlotOptions = new PlotOptionsArea();
//		GradientColor fillColor = GradientColor.createLinear(0, 0, 0, 1);
//		fillColor.addColorStop(0, new SolidColor(69, 114, 167, 1));
//		fillColor.addColorStop(1, new SolidColor(69, 114, 167, 0.5));
//		masterPlotOptions.setFillColor(fillColor);
		masterPlotOptions.setMarker(new Marker(false));
		masterPlotOptions.setPointStart(ChartUtils.toHighchartsTS(dataSetStart,getNetworkTimeZone()));
		ds.setPlotOptions(masterPlotOptions);
		ds.setName("");
		configuration.addSeries(ds);
		masterChart.drawChart(configuration);
		return masterChart;
	}


}
