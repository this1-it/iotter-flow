package it.thisone.iotter.exporter;

import java.io.File;


public interface IExportProvider {
	public File createExportDataFile(IExportConfig config, IExportProperties properties);
}
