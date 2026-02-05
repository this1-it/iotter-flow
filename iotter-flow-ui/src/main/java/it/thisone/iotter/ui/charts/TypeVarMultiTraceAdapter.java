package it.thisone.iotter.ui.charts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.vaadin.addon.charts.Chart;
//import com.vaadin.addon.charts.ChartSelectionEvent;
//import com.vaadin.addon.charts.ChartSelectionListener;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.enums.ChartScaleType;
import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GraphicWidgetOptions;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChannelUtils;
import it.thisone.iotter.ui.common.charts.TimeIntervalHelper;
import it.thisone.iotter.ui.eventbus.WidgetRefreshEvent;
import it.thisone.iotter.ui.model.TimeInterval;
import it.thisone.iotter.ui.model.TimePeriod;

	// TODO(flow-migration): this class still contains Vaadin 8 APIs and needs manual Flow refactor.
public class TypeVarMultiTraceAdapter extends AbstractChartAdapter {
	private MultiTraceChartAdapter analogChartAdapter;
	private MultiTraceChartAdapter digitalChartAdapter;

	/**
	 * 
	 */
	private static final long serialVersionUID = -1879789719298795301L;

	public TypeVarMultiTraceAdapter(GraphicWidget widget) {
		super(widget);
		Component visualization = buildVisualization();
		//visualization.setSizeFull();
		setRootComposition(createContentWrapper(visualization));
	}

	@Override
	protected Component buildVisualization() {
		VerticalLayout vlayout = new VerticalLayout();
		vlayout.setSizeFull();
		List<GraphicFeed> digital = new ArrayList<GraphicFeed>();
		List<GraphicFeed> analog = new ArrayList<GraphicFeed>();
		for (GraphicFeed feed : getGraphWidget().getFeeds()) {
			if (feed.getChannel()!= null) {
				if (ChannelUtils.isTypeDigital(feed.getChannel())) {
					digital.add(feed);
				}
				else {
					analog.add(feed);
				}
			}
		}
		
		if (!analog.isEmpty()) {
			analogChartAdapter = buildMultiTraceChart(analog);
			vlayout.add(analogChartAdapter.getChart());
		}
		
		if (!digital.isEmpty()) {
			digitalChartAdapter = buildMultiTraceChart(digital);
			vlayout.add(digitalChartAdapter.getChart());
		}
		
		if (!digital.isEmpty() && !analog.isEmpty() && !UIUtils.isMobile()) {
			Integer margin = 200;
			digitalChartAdapter.getChart().getConfiguration().getChart().setMarginLeft(margin);
			digitalChartAdapter.getChart().getConfiguration().getChart().setMarginRight(margin);
			analogChartAdapter.getChart().getConfiguration().getChart().setMarginLeft(margin);
			analogChartAdapter.getChart().getConfiguration().getChart().setMarginRight(margin);
		}
		
		return vlayout;
	}

	
//	public GraphicWidget getGraphWidget() {
//		return (GraphicWidget) getWidget();
//	}

	
	private MultiTraceChartAdapter buildMultiTraceChart(List<GraphicFeed> feeds) {
		GraphicWidget widget = new GraphicWidget();
		widget.setGroupWidget(getGraphWidget().getGroupWidget());
		widget.setType(GraphicWidgetType.MULTI_TRACE);
		widget.getOptions().setExporting(false);
		widget.getOptions().setRealTime(getGraphWidget().getOptions().getRealTime());
		widget.setLabel(getGraphWidget().getLabel() + " multitrace");
		widget.getOptions().setScale(ChartScaleType.LINEAR);
		widget.getOptions().setShowGrid(false);
		widget.getOptions().setShowLegend(true);
		widget.setFeeds(feeds);

		MultiTraceChartAdapter multitrace = new MultiTraceChartAdapter(widget);
		TimeIntervalHelper helper = new TimeIntervalHelper(multitrace.getNetworkTimeZone());
		TimePeriod period = config.getDefaultPeriod();
		TimeInterval interval = helper.period(new Date(), period);
		multitrace.setTimePeriod(period);
		multitrace.setTimeInterval(interval);
		multitrace.register();
		
//		multitrace.getChart().removeChartSelectionListener(multitrace.getChartSelectionListener());
//		multitrace.getChart().addChartSelectionListener(createChartSelectionListener());
		return multitrace;
	}

	

	@Override
	public void unregister() {
		if (analogChartAdapter != null) {
			analogChartAdapter.unregister();
		}
		if (digitalChartAdapter != null) {
			digitalChartAdapter.unregister();
		}
		super.unregister();
	}

	@Override
	public void register() {
		if (analogChartAdapter != null) {
			analogChartAdapter.register();
		}
		if (digitalChartAdapter != null) {
			digitalChartAdapter.register();
		}
		super.register();
	}


	@Override
	public boolean refresh() {
		return false;
	}

	@Override
	public void draw() {
		
	}

	@Override
	public void refreshWithUiAccess(WidgetRefreshEvent event) {
		
	}

	@Override
	protected void createChartConfiguration(TimeInterval interval) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setGraphWidgetOptionsOnChange(GraphicWidgetOptions options) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean isRealTime() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean changedRealTime(GraphicWidgetOptions options) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Chart getChart() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void setChart(Chart chart) {
		throw new UnsupportedOperationException();
	}
	
	
//	private ChartSelectionListener createChartSelectionListener() {
//		ChartSelectionListener chartSelectionListener = new ChartSelectionListener() {
//			private static final long serialVersionUID = 325958356424593206L;
//			@Override
//			public void onSelection(ChartSelectionEvent event) {
//				long start = event.getSelectionStart().longValue();
//				long end = event.getSelectionEnd().longValue();
//				Date startZoom = ChartUtils.toNetworkDate(start, getNetworkTimeZone());
//				Date endZoom = ChartUtils.toNetworkDate(end, getNetworkTimeZone());
//				if (analogChartAdapter != null) {
//					analogChartAdapter.setTimeInterval(new TimeInterval(startZoom, endZoom));
//				}
//				if (digitalChartAdapter != null) {
//					digitalChartAdapter.setTimeInterval(new TimeInterval(startZoom, endZoom));
//				}
//			}
//		};
//		return chartSelectionListener;
//	}
//

}
