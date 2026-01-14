package it.thisone.iotter.exporter.cassandra;

import java.io.Serializable;
import java.util.List;

import it.thisone.iotter.cassandra.model.CassandraExportFeed;
import it.thisone.iotter.exporter.DataFormat;

public class CassandraExportConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String name;
	private List<CassandraExportFeed> feeds;
	private DataFormat dataFormat;
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<CassandraExportFeed> getFeeds() {
		return feeds;
	}


	public void setFeeds(List<CassandraExportFeed> feeds) {
		this.feeds = feeds;
	}

	public DataFormat getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(DataFormat dataFormat) {
		this.dataFormat = dataFormat;
	}
	
	
	
}
