package it.thisone.iotter.exporter;

import java.io.File;
import it.thisone.iotter.persistence.model.Channel;

import it.thisone.iotter.cassandra.model.CassandraExportFeed;


public interface IExportProvider {
	public File createExportDataFile(IExportConfig config, IExportProperties properties);
	public CassandraExportFeed createExportFeed(Channel channel, String label);

}
