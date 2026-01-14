package it.thisone.iotter.exporter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Range;

import it.thisone.iotter.cassandra.InterpolationUtils;
import it.thisone.iotter.cassandra.model.CassandraExportFeed;
import it.thisone.iotter.cassandra.model.Interpolation;

public class ExportConfig implements IExportConfig {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private List<CassandraExportFeed> feeds;
	private Range<Date> interval;
	private Interpolation interpolation;
	private String lockId;

	@Override
	public String toString() {
		String range = "";
		if (interval != null) {
			range = interval.toString();
		}
		return String.format("%s interval:%s", name, range);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String uniqueFileName(String ext) {
		return getName().replaceAll("[^a-zA-Z0-9.-]", "_") +  "." + ext;
	}

	
	public void setName(String name) {
		this.name = name;
	}
	public List<CassandraExportFeed> getFeeds() {
		if (feeds == null) {
			feeds = new ArrayList<CassandraExportFeed>();
		}
		return feeds;
	}
	public void setFeeds(List<CassandraExportFeed> feeds) {
		this.feeds = feeds;
	}

	@Override
	public Range<Date> getInterval() {
		return interval;
	}

	public void setInterval(Range<Date> interval) {
		this.interval = interval;
	}

	@Override
	public Interpolation getInterpolation() {
		return interpolation;
	}

	@Override
	public void setInterpolation(Interpolation interpolation) {
		this.interpolation = interpolation;
	}

	public String getLockId() {
		return lockId;
	}

	public void setLockId(String lockId) {
		this.lockId = lockId;
	}

	
	
}
