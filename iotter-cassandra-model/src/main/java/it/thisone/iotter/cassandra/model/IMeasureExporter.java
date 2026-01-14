package it.thisone.iotter.cassandra.model;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Range;

public interface IMeasureExporter {
	public static final int BATCH_ITEMS = 360 * 6;

	public static final int MAX_BATCH_ITEMS = 500 * 100;
	
	public List<MeasureRaw> loadMeasures(List<FeedKey> feeds, Date from, Date to, boolean ascending);
	
	public List<MeasureRaw> loadMeasuresWithTicks(List<FeedKey> feeds, Date from, Date to, Interpolation interpolation,
			boolean ascending, List<Date> ticks);

	public Date getTimeStamp(String uid, int id);

	public List<Date> getTimeStamps(String uid, int start, int end);

	public int writeTimeStamps(String uid, List<FeedKey> feeds, Date from, Date to, Interpolation interpolation,
			boolean ascending, Set<Range<Date>> validities);

	public ExportQuery retrieveExportQuery(String qid);

	public ExportQuery buildExportQuery(String serial, List<String> keys, long from, long to, Boolean ascending, String interpolation, String apiKey);

	public void writeTimeStamps(ExportQuery query);
	
	public List<ExportRow> loadRows(ExportQuery query);


}