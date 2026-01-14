package it.thisone.iotter.exporter.cassandra;

import java.util.Date;
import java.util.Map;
import java.util.UUID;



import com.google.common.collect.Range;

import it.thisone.iotter.cassandra.model.CassandraExportFeed;
import it.thisone.iotter.cassandra.model.ExportRow;
import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.exporter.DataFormat;
import it.thisone.iotter.lazyquerydataprovider.LazyQueryDefinition;

public class CassandraExportQueryDefinition extends LazyQueryDefinition<ExportRow, Date> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -30102833830134301L;

	private Range<Date> interval;

	private Interpolation interpolation;
	
	private DataFormat dataFormat;
	
	private Map<String, CassandraExportFeed> feeds;
	
	private boolean ascending;

	private final String uid; 
	
	private final String consistency; 

	private boolean exporting;

    /**
     * Constructor which sets the batch size.
     *
     * @param compositeItems True if native items should be wrapped to
     *                       CompositeItems.
     * @param batchSize      Value for batch size.
     * @param idPropertyId   The ID of the ID property or null if item index in result set is used as ID.
     */

	public CassandraExportQueryDefinition(boolean compositeItems, int batchSize, Object idPropertyId, boolean asc) {
		super(ExportRow.class, batchSize);
		ascending = asc;
		uid = UUID.randomUUID().toString();
		consistency = "LOCAL_QUORUM";
	}


	public Range<Date> getInterval() {
		return interval;
	}

	public void setInterval(Range<Date> interval) {
		this.interval = interval;
	}


	public Map<String, CassandraExportFeed> getFeeds() {
		return feeds;
	}


	public void setFeeds(Map<String, CassandraExportFeed> feeds) {
		this.feeds = feeds;
	}


	public boolean isAscending() {
		return ascending;
	}


	public DataFormat getDataFormat() {
		return dataFormat;
	}


	public void setDataFormat(DataFormat dataFormat) {
		this.dataFormat = dataFormat;
	}

	public String getUid() {
		return uid;
	}


	public Interpolation getInterpolation() {
		return interpolation;
	}


	public void setInterpolation(Interpolation interpolation) {
		this.interpolation = interpolation;
	}


	public boolean isExporting() {
		return exporting;
	}


	public void setExporting(boolean exporting) {
		this.exporting = exporting;
	}


	public String getConsistency() {
		return consistency;
	}
	
}
