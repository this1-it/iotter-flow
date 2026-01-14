package it.thisone.iotter.exporter.cassandra;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.thisone.iotter.cassandra.model.CassandraExportFeed;
import it.thisone.iotter.cassandra.model.ExportRow;
import it.thisone.iotter.cassandra.model.IMeasureExporter;
import it.thisone.iotter.exporter.DataFormat;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDataProvider;

public class CassandraExportDataProvider extends LazyQueryDataProvider<ExportRow, Date> 
{
	/**
	 * 
 */
	private static final long serialVersionUID = -1488605299044091345L;
	private List<String> valueHeaders;
	private String timestampHeader = "Timestamp";

	public CassandraExportDataProvider(IMeasureExporter exporter, List<CassandraExportFeed> feeds, DataFormat dataFormat, int batchSize, boolean ascending) {
		super( new CassandraExportQueryDefinition(true, batchSize, CassandraExportQuery.ID, ascending), new CassandraExportQueryFactory(exporter));
		CassandraExportQueryDefinition queryDefinition = getQueryDefinition();
		
		List<String> labels = new ArrayList<String>();
		Map<String, CassandraExportFeed> feedKeys = new HashMap<String, CassandraExportFeed>();
		
		//addContainerProperty(CassandraExportQuery.ID, Long.class, null, false, false);
		//queryDefinition.addProperty(CassandraExportQuery.ID, Long.class, null, false, false);
		
		//addContainerProperty(CassandraExportQuery.TIMESTAMP, Date.class, null, false, false);
		//queryDefinition.addProperty(CassandraExportQuery.TIMESTAMP, Date.class, null, false, false);
		
		for (CassandraExportFeed feed : feeds) {
			String propertyId = feed.getKey();
			//String serial = feed.getSerial();
			String feedLabel = String.format("%s %s", feed.getLabel(), feed.getDevice());
			if (! feedKeys.containsKey(propertyId)) {
				labels.add(feedLabel);
				feedKeys.put(propertyId, feed);
				//addContainerProperty(propertyId, String.class, FileBuilder.EMPTY_VALUE, false, false);
				//queryDefinition.addProperty(propertyId, String.class, FileBuilder.EMPTY_VALUE, false, false);
			}
		}
		queryDefinition.setFeeds(feedKeys);
		queryDefinition.setDataFormat(dataFormat);
		valueHeaders = labels;
	}
	
	public CassandraExportQueryDefinition getQueryDefinition() {
		return (CassandraExportQueryDefinition) super.getQueryDefinition();
	}

	public List<String> getValueHeaders() {
		return valueHeaders;
	}

	public String getTimestampHeader() {
		return timestampHeader;
	}

}
