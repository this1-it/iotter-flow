package it.thisone.iotter.exporter;

import java.io.Serializable;
/*
 * Feature #167 Export data from visualizations
 */

import it.thisone.iotter.enums.ExportFormat;

public interface IExportProperties extends Serializable {
	
	public boolean isLegacy();

	public String getFileExtension();
	
	public ExportFormat getFormat();
}
