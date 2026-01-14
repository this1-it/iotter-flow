package it.thisone.iotter.cassandra.util;

import java.text.DecimalFormat;
import java.text.ParseException;

import it.thisone.iotter.cassandra.model.CassandraExportFeed;
import it.thisone.iotter.config.Constants;


public class CassandraExportUtil {
	
	public static Object convertFormat(Float value, CassandraExportFeed feed, DecimalFormat nf, boolean typed) {
		Number number = feed.getMeasureUnit().convert(value);
		if (number == null) {
			return Constants.Exporter.EMPTY_VALUE;
		}
		nf.applyPattern(feed.getMeasureFormat());
		nf.setDecimalSeparatorAlwaysShown(feed.getMeasureDecimals() > 0);
		String formatted = nf.format(number);

	    if (typed) {
	        try {
	            Number parsed = nf.parse(formatted);
	            return parsed.doubleValue();
	        } catch (ParseException e) {
	            return Constants.Exporter.EMPTY_VALUE;
	        }
	    }

	    return formatted;
	}

}
