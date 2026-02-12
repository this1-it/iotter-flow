package it.thisone.iotter.ui.charts;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Range;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;

import it.thisone.iotter.cassandra.model.CassandraExportFeed;
import it.thisone.iotter.cassandra.model.ExportRow;
import it.thisone.iotter.cassandra.model.IMeasureExporter;
import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.enums.Order;
import it.thisone.iotter.exporter.DataFormat;
import it.thisone.iotter.exporter.cassandra.CassandraExportDataProvider;
import it.thisone.iotter.exporter.cassandra.CassandraExportQuery;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GraphicWidgetOptions;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.model.TimeInterval;

	// TODO(flow-migration): this class still contains Vaadin 8 APIs and needs manual Flow refactor.
public class TableAdapter extends AbstractChartAdapter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4605827865960616327L;
	private SimpleDateFormat sdf;
	private CassandraExportDataProvider container;

	public TableAdapter(GraphicWidget widget) {
		super(widget);
		optionsField.getScale().setVisible(false);
		optionsField.getShowGrid().setVisible(false);
		optionsField.getRealTime().setVisible(false);
		optionsField.getShowMarkers().setVisible(false);
		optionsField.getAutoScale().setVisible(false);
		sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss ZZZ");
		sdf.setTimeZone(getNetworkTimeZone());
	}

	@Override
	protected Component buildVisualization() {
		Grid<ExportRow> grid = new Grid<>();
		
		DataFormat dataFormat = new DataFormat(UIUtils.getLocale(), getNetworkTimeZone());
		
		List<CassandraExportFeed> feeds = ChartUtils.createExportFeeds(getGraphWidget().getFeeds());
		
		boolean ascending = getGraphWidget().getOptions().getOrder().equals(Order.ASCENDING);
		
		IMeasureExporter exporter = UIUtils.getCassandraService().getExport();
		container = new CassandraExportDataProvider(exporter, feeds, dataFormat, CassandraExportQuery.BATCH_SIZE, ascending);
		container.getQueryDefinition().setInterpolation(Interpolation.RAW);
		
		grid.setDataProvider(container);
		grid.setSelectionMode(SelectionMode.NONE);
		grid.setSizeFull();

		return grid;
	}

	@Override
	public boolean refresh() {
		return false;
	}

	@Override
	protected void createChartConfiguration(TimeInterval interval) {
		Range<Date> range = Range.closed(interval.getStartDate(), interval.getEndDate());
		container.getQueryDefinition().setInterval(range);
		if (!UIUtils.getCassandraService().getRollup().isFullyAvailableInterpolation(Interpolation.RAW, range , null, null)){
			container.getQueryDefinition().setInterpolation(UIUtils.getCassandraService().getMeasures().minimalInterpolation());
		}
		container.refreshAll();
	}

	@Override
	public void draw() {
		// no-op: grid reflects the refreshed DataProvider state.
	}
	
	@Override
	protected void setGraphWidgetOptionsOnChange(GraphicWidgetOptions options) {
		changedLocalControls(options);
	}

	@Override
	protected boolean isRealTime() {
		return false;
	}

	@Override
	public Component getChart() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void setChart(Component chart) {
		throw new UnsupportedOperationException();
	}


	@Override
	protected boolean changedRealTime(GraphicWidgetOptions options) {
		return false;
		
	}

	
}
