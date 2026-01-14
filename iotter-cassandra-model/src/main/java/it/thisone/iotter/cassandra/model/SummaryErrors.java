package it.thisone.iotter.cassandra.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class SummaryErrors implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6036202602837832404L;
	
	private Map<String, Integer> map;
	
	private MeasureRaw firstError;


	public SummaryErrors() {
		map = new HashMap<String, Integer>();
	}
	
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	private String errors() {
		StringBuffer sb = new StringBuffer();
		for (String key : map.keySet()) {
			sb.append(String.format("%s:%s;", key, map.get(key)));
		}
		return sb.toString();
	}
	
	public void add(MeasureRaw measure) {
		if (!measure.hasError()) return;
		
		if (firstError == null) {
			firstError = new MeasureRaw(measure.getDate(), measure.getValue(),null);
		}
		else {
			if (measure.getDate().before(firstError.getDate())) {
				firstError = new MeasureRaw(measure.getDate(), measure.getValue(),null);
			}
		}
		
		if (measure.hasError()) {
			String error = measure.getError();
			if (error.contains(";")) {
				String[] errors = StringUtils.split(error, ";");
				for (int i = 0; i < errors.length; i++) {
					String[] tokens = StringUtils.split(errors[i], ":");
					if (tokens.length > 1) {
						try {
							addEntry(tokens[0], Integer.parseInt(tokens[1]));
						} catch (NumberFormatException e) {
							//
						}
					}
					else {
						addEntry(tokens[0], 1);
					}
				}
			}
			else {
				addEntry(error, 1);
			}
		}
		
	}

	private void addEntry(String error, int count) {
		if (error == null || error.isEmpty()) return;
		Integer value = map.get(error);
		if (value == null) {
			map.put(error, count);
		}
		else {
			map.put(error, value + count);
		}
	}

	public MeasureRaw getFirstError() {
		if (firstError != null) {
			firstError.setError(errors());
		}
		return firstError;
	}
	
	public static List<MeasureRaw> interval(MeasureRaw min, MeasureRaw max, MeasureRaw err) {
		// use a set to eliminate duplicate entry
		Set<MeasureRaw> measures = new HashSet<MeasureRaw>();
		if (min.isValid()) {
			measures.add(min);
		}
		if (max.isValid()) {
			measures.add(max);
		}
		if (err != null && err.isValid()) {
			measures.add(err);
		}

		List<MeasureRaw> sorted = new ArrayList<MeasureRaw>(measures);
		Collections.sort(sorted, new MeasureDateComparator());
		if (err == null) {
			return sorted;
		}
		
		if (sorted.size() < 3) {
			for (MeasureRaw measure : sorted) {
				if (measure.getDate().equals(err.getDate())) {
					measure.setError(err.getError());
				}
			}
			return sorted;
		}
		
		List<MeasureRaw> values = new ArrayList<MeasureRaw>();
		
		MeasureRaw lower = null;
		MeasureRaw upper = null;
		
		if (min.getDate().before(max.getDate())) {
			lower = min;
			upper = max;
		}
		else {
			lower = max;
			upper = min;
		}
		
		long tlower = lower.getDate().getTime();
		long terr = err.getDate().getTime();
		long tupper = upper.getDate().getTime();
		
		if (terr <= tlower) {
			err.setValue(lower.getValue());
			values.add(err);
			values.add(upper);
		} else if (tlower < terr  && terr <= tupper ) {
			// choose nearest
			long distLower = terr - tlower;
			long distUpper = tupper - terr;
			if (distLower < distUpper) {
				err.setValue(lower.getValue());
				values.add(err);
				values.add(upper);
			}
			else {
				err.setValue(upper.getValue());
				values.add(lower);
				values.add(err);
			}
		} else if (terr > tupper) {
			err.setValue(upper.getValue());
			values.add(lower);
			values.add(err);
		}
		
		return values;
	}
	
	
}
