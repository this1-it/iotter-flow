package it.thisone.iotter.cassandra.model;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Range;

public class CassandraExportContext {

	private final String serial;
	private final Range<Date> interval;
	private final List<String> addresses;
	private final List<String> labels;
	private final Map<String, CassandraExportFeed> feedsMap;
	private final DecimalFormat nf;
	private final DateFormat df;
	private final boolean ascending;
	private final boolean typed;

	public CassandraExportContext(List<CassandraExportFeed>feeds, Range<Date> interval,DecimalFormat nf, DateFormat df, boolean ascending, boolean typed) {
		List<String> labels = new ArrayList<String>();
		List<String> addresses = new ArrayList<String>();
		Map<String, CassandraExportFeed> feedsMap = new HashMap<String, CassandraExportFeed>();
		labels.add("Timestamp");
		String serial = null;
		for (CassandraExportFeed feed : feeds) {
			if (!addresses.contains(feed.getRegisterId()) && feed.isSelected()) {
				addresses.add(feed.getRegisterId());
				feedsMap.put(feed.getRegisterId(), feed);
				labels.add(String.format("%s %s", feed.getRegisterId(), feed.getLabel()));
			}
			serial = feed.getSerial();
		}
		
		this.serial = serial;
		this.interval = interval;
		this.addresses = addresses;
		this.labels = labels;
		this.feedsMap = feedsMap;
		this.nf = nf;
		this.df = df;
		this.ascending = ascending;
		this.typed = typed;
	}

	public String getSerial() {
		return serial;
	}

	public Range<Date> getInterval() {
		return interval;
	}

	public List<String> getAddresses() {
		return addresses;
	}
	
	public List<String> getLabels() {
		return labels;
	}

	public Map<String, CassandraExportFeed> getFeeds() {
		return feedsMap;
	}

	public DecimalFormat getNumberFormat() {
		return nf;
	}

	public DateFormat getDateFormat() {
		return df;
	}

	@Override
	public String toString() {
		return "CassandraExportContext{" + "serial='" + serial + '\'' + ", interval=" + interval + ", addresses="
				+ addresses + ", feeds=" + feedsMap + ", numberFormat=" + nf.toPattern() + ", dateFormat=" + df.toString()
				+ '}';
	}

	public boolean isAscending() {
		return ascending;
	}

	public boolean isTyped() {
		return typed;
	}
}
