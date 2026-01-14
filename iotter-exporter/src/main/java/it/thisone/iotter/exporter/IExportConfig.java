package it.thisone.iotter.exporter;

import java.io.Serializable;
import java.util.Date;

import com.google.common.collect.Range;
/*
 * Feature #167 Export data from visualizations
 */

import it.thisone.iotter.cassandra.model.Interpolation;

public interface IExportConfig extends Serializable {

	public void setInterval(Range<Date> range);

	public Range<Date> getInterval();
	
	public Interpolation getInterpolation();
	
	public void setInterpolation(Interpolation interpolation);
	
	public String uniqueFileName(String extension);
	
	public String getName();

	public String getLockId();
	
	public void setLockId(String lockId);
	
}
