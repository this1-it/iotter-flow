package it.thisone.iotter.exporter.cassandra;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.google.common.collect.Range;


import it.thisone.iotter.cassandra.InterpolationUtils;
import it.thisone.iotter.cassandra.model.CassandraExportFeed;
import it.thisone.iotter.cassandra.model.ExportRow;
import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.IMeasureExporter;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.exporter.filegenerator.FileBuilder;
import it.thisone.iotter.lazyquerydataprovider.Query;
import it.thisone.iotter.lazyquerydataprovider.QueryDefinition;

/**
 * @author tisone
 * 
 */
public class CassandraExportQuery implements Query<ExportRow,Date> {
	public static final String TIMESTAMP = "ts";
	public static final String ID = "id";
	public static final int BATCH_SIZE = 100;

	private static Logger logger = LoggerFactory.getLogger(CassandraExportQuery.class);
	//private static Logger logger = LoggerFactory.getLogger(Constants.Exporter.LOG4J_CATEGORY);


	
	private Integer size;
	private CassandraExportQueryDefinition queryDefinition;
	private SimpleDateFormat sdf;
	private Set<Range<Date>> validities;
	
	private DecimalFormat nf;
	private IMeasureExporter queryExporter;

	public CassandraExportQuery(CassandraExportQueryDefinition definition, IMeasureExporter exporter) {
		queryDefinition = definition;
		queryExporter = exporter;
		sdf = new SimpleDateFormat(InterpolationUtils.DATE_FORMAT);
		sdf.setTimeZone(definition.getDataFormat().getTimeZone());

		DecimalFormatSymbols symbols = new DecimalFormatSymbols(definition.getDataFormat().getLocale());
		if (definition.getDataFormat().getDecimalFormatSymbols() != null) {
			symbols.setDecimalSeparator(definition.getDataFormat().getDecimalFormatSymbols().getDecimalSeparator());
		}
		
		nf = new DecimalFormat("##0.###", symbols);
		nf.setGroupingUsed(false);
		validities = new HashSet<Range<Date>>();
		for (String key : queryDefinition.getFeeds().keySet()) {
			List<Range<Date>> ranges = queryDefinition.getFeeds().get(key)
					.getValidities();
			validities.addAll(ranges);
		}
	}

//	@SuppressWarnings("unchecked")
//	public List<Item> loadItems(int startIndex, int batchSize) {
//		
//		List<Item> results = new ArrayList<Item>();
//		Map<Long, Item> map = new HashMap<Long, Item>();
//		List<FeedKey> keys = new ArrayList<>();
//		for (CassandraExportFeed feed : queryDefinition.getFeeds().values()){
//			keys.add(feed.getFeedKey());
//		}
//		
//		Range<Date> interval = getBatchInterval(startIndex, batchSize, queryDefinition.isAscending());
//		
//		Date from = interval.lowerEndpoint();
//		Date to = interval.upperEndpoint();
//		
//		List<Date> ticks = getBatchDates(startIndex, batchSize);
//		if (ticks != null) {
//			// Bug #1316 always order ticks ascending
//			if (ticks != null) {
//				Collections.sort(ticks);
//			}			
//		}		
//		List<MeasureRaw> measures = getQueryExporter()
//				.loadMeasuresWithTicks(keys, from, to, queryDefinition.getInterpolation(), queryDefinition.isAscending(), ticks);
//		
//		
//		for (MeasureRaw measure : measures) {
//			Long id = measure.getDate().getTime();
////			if (!map.containsKey(id)) {
////				Item item = constructItem();
////				item.getItemProperty(ID).setValue(id);
////				//item.getItemProperty(TIMESTAMP).setValue(sdf.format(measure.getDate()));
////				item.getItemProperty(TIMESTAMP).setValue(measure.getDate());
////				map.put(id, item);
////				results.add(item);
////			}
//			Item item = map.get(id);
//			String key = measure.getKey();
//			try {
//				if (key != null) {
//					if (measure.hasError()) {
//						item.getItemProperty(key).setValue("ERR:" + measure.getError());
//					} else {
//						if (measure.getValue() != null) {
//							item.getItemProperty(key).setValue(
//									formatMeasure(key, measure.getValue()));
//						}
//					}
//				}
//			} catch (Exception e) {
//				logger.error("loadItems setValue " + key, e);
//			}
//		}
//		logger.debug("loadItems startIndex {} batchSize {} results {} from {} to {} ",
//				startIndex, batchSize, results.size(), sdf.format(from), sdf.format(to)				
//				);
//		
//		
//		// fixed Bug #2091
////		while (results.size() < batchSize) {
////			Item item = constructItem();
////			item.getItemProperty(ID).setValue(System.nanoTime());
////			item.getItemProperty(TIMESTAMP).setValue("");
////			for (FeedKey key : keys) {
////				item.getItemProperty(key.getKey()).setValue("");
////			}
////			results.add(item);
////		}
//		return results;
//	}


	private String formatMeasure(String key, Float value) {
		try {
			Number number = queryDefinition.getFeeds().get(key).getMeasureUnit().convert(value);
			if (number == null) {
				return FileBuilder.EMPTY_VALUE;
			}
			nf.applyPattern(queryDefinition.getFeeds().get(key).getMeasureFormat());
			nf.setDecimalSeparatorAlwaysShown(queryDefinition.getFeeds().get(key).getMeasureDecimals() > 0);
			if (queryDefinition.isExporting()) {
				return String.format("%s%s", FileBuilder.NUMBER_PREFIX, nf.format(number) ) ;
			}
			return nf.format(number);
		} catch (Exception e) {
			logger.error("formatMeasure key " + key, e);
		}
		return FileBuilder.EMPTY_VALUE;
	}
	
	
	public int size() {
		if (size != null) {
			logger.debug("size cached {}", size);
			return size;
		}
		size = 0;
		if (queryDefinition.getInterval() == null) {
			logger.debug("queryDefinition interval null !!");
			return 0;
		}
		
		Date from = InterpolationUtils.toUTCDate(queryDefinition.getInterval().lowerEndpoint(),
				queryDefinition.getDataFormat().getTimeZone());
		
		Date to = InterpolationUtils.toUTCDate(queryDefinition.getInterval().upperEndpoint(),
				queryDefinition.getDataFormat().getTimeZone());
		
		List<FeedKey> keys = new ArrayList<FeedKey>();
		for (CassandraExportFeed feed : queryDefinition.getFeeds().values()) {
			keys.add(feed.getFeedKey());
		}
		
		size = getQueryExporter().writeTimeStamps(queryDefinition.getUid(), keys, from, to,
						queryDefinition.getInterpolation(),
						queryDefinition.isAscending(), validities);

		logger.debug("export query size {} interval {}", size, queryDefinition.getInterval());
		return size;
	}

	private Date getBatchDate(int index) {
		Date batchDate = getQueryExporter()
				.getTimeStamp(queryDefinition.getUid(), index);
		return batchDate;
	}

	private Range<Date> getBatchInterval(int index, int batchSize, boolean ascending) {
		Date from = new Date();
		Date to = new Date();
		if (ascending) {
			from = getBatchDate(index);
			to = getBatchDate(index + batchSize - 1);
		} else {
			to = getBatchDate(index);
			from = getBatchDate(index + batchSize - 1);
		}
		return Range.closed(from, to);
	}
	
	
	private List<Date> getBatchDates(int index, int batchSize) {
		int from = index;
		int to = index + batchSize - 1;
		return getQueryExporter()
				.getTimeStamps(queryDefinition.getUid(), from, to);
	}

	

	private IMeasureExporter getQueryExporter() {
		return queryExporter;
	}

//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	@Override
//	public Item constructItem() {
//		PropertysetItem item = new PropertysetItem();
//		for (Object propertyId : queryDefinition.getPropertyIds()) {
//			Object value = queryDefinition.getPropertyDefaultValue(propertyId);
//			item.addItemProperty(propertyId, new ObjectProperty(value,
//					queryDefinition.getPropertyType(propertyId),
//					queryDefinition.isPropertyReadOnly(propertyId)));
//		}
//		return item;
//	}

	@Override
	public boolean deleteAllItems() {
		//throw new UnsupportedOperationException();
		return true;
	}





	@Override
	public int size(QueryDefinition<ExportRow, Date> queryDefinition) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Stream<ExportRow> loadItems(QueryDefinition<ExportRow, Date> queryDefinition, int offset, int limit) {
		// TODO Auto-generated method stub
		return null;
	}


}
