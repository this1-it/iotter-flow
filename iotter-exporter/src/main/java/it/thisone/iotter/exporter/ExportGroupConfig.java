package it.thisone.iotter.exporter;

import java.util.ArrayList;
import java.util.List;

import it.thisone.iotter.cassandra.model.CassandraExportFeed;

public class ExportGroupConfig extends ExportConfig {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<ExportConfig> exportConfigs = new ArrayList<ExportConfig>();

//	@Override
//	public String uniqueFileName(String ext) {
//		return getName().replaceAll("[^a-zA-Z0-9.-]", "_") + ".zip";
//	}

	
	public List<ExportConfig> getExportConfigs() {
		for (ExportConfig config : exportConfigs) {
			config.setInterpolation(getInterpolation());
		}
		return exportConfigs;
	}
	
	public void setExportConfigs(List<ExportConfig> configs) {
		if (exportConfigs == null) {
			exportConfigs = new ArrayList<ExportConfig>();
		}
		this.exportConfigs = configs;
	}
	
	@Override
	public List<CassandraExportFeed> getFeeds() {
		List<CassandraExportFeed> feeds = new ArrayList<CassandraExportFeed>();
		for (ExportConfig config : exportConfigs) {
			feeds.addAll(config.getFeeds());
		}
		return feeds;
	}
	
	

}
