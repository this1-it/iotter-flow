package it.thisone.iotter.cassandra.model;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.google.common.collect.Range;

import it.thisone.iotter.enums.MeasureQualifier;

public class SummaryMeasure implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final int qualifier;
	private SummaryStatistics stats = new SummaryStatistics();
	private SummaryErrors errors = new SummaryErrors();
	private Float minValue;
	private Float maxValue;
	private Date minDate;
	private Date maxDate;
	private Date firstDate;
	private Date lastDate;

	public SummaryMeasure(int qual) {
		qualifier = qual;
		firstDate = new Date();
		lastDate = new Date(0);
	}

	public void add(MeasureRaw measure) {
		if (measure == null) {
			return;
		}
		
		Float value = measure.getValue();
		Date date = measure.getDate();
		
		if (date.before(firstDate)) {
			firstDate = date;
		}
		if (date.after(lastDate)) {
			lastDate = date;
		}
		if (measure.hasError()) {
			errors.add(measure);
		} else {
			if (value != null) {
				stats.addValue(value);
				setMin(value, date);
				setMax(value, date);
			}
		}
	}

	public void add(MeasureAggregation measure) {
		if (measure == null) {
			return;
		}
		// Bug #249 (In Progress): [CASSANDRA] wrong values in rollup tables
		if (measure.getRecords() == 0) {
			return;
		}

		if (measure.hasError()) {
			errors.add(new MeasureRaw(measure.getErrorDate(),measure.getValue(),measure.getError()));
		} 

		if (measure.getValue() != null) {
			stats.addValue(measure.getValue());
		}
		setMin(measure.getMinValue(), measure.getMinDate());
		setMax(measure.getMaxValue(), measure.getMaxDate());
		
	}
	
	
	private void setMin(Float value, Date date) {
		if (value == null) {
			return;
		}
		if (minValue == null) {
			minValue = value;
			minDate = date;
		} else if (value < minValue) {
			minValue = value;
			minDate = date;
		}
	}

	private void setMax(Float value, Date date) {
		if (value == null) {
			return;
		}
		if (maxValue == null) {
			maxValue = value;
			maxDate = date;
		} else if (value > maxValue) {
			maxValue = value;
			maxDate = date;
		} else if (value == maxValue) {
			maxDate = date;
		}
	}

	public int getQualifier() {
		return qualifier;
	}

	public SummaryStatistics getStats() {
		return stats;
	}

	public MeasureRaw getError() {
		return errors.getFirstError();
	}

	public Float getMinValue() {
		return minValue;
	}

	public Float getMaxValue() {
		return maxValue;
	}

	public Date getMinDate() {
		return minDate;
	}

	public Date getMaxDate() {
		return maxDate;
	}

	/**
	 * check valid values have been collected
	 * @return
	 */
	public boolean isValid() {
		return (stats.getN() > 0);
	}

	public Float getValue() {
		// TODO choose statistics based on qualifier
		if (isValid()) {
			MeasureQualifier qual = getMeasureQualifier();
			double value = 0f;
			switch (qual) {
			case ONE:
			case AVG:
				value = stats.getMean();
				break;
			case MIN:
				value = stats.getMin();
				break;
			case MAX:
				value = stats.getMax();
				break;
			case TOT:
				value = stats.getSum();
				break;
			case STD:
				value = stats.getSumsq();
				break;
			case INSTANT_MIN:// TODO unsupported !!!
			case INSTANT_MAX:// TODO unsupported !!!
			case ALM: // TODO unsupported !!!
			default:
				value = stats.getMean();
				break;
			}
			
			if (value == Double.NaN)
				return null;
			
			return (float) value;
		}
		return null;
	}
	
	public MeasureQualifier getMeasureQualifier() {
		MeasureQualifier[] values = MeasureQualifier.values();
		for (int i = 0; i < values.length; i++) {
			MeasureQualifier qual = values[i];
			if (qual.getValue() == qualifier) {
				return qual;
			}
		}
		return MeasureQualifier.AVG;
	}

	public MeasureAggregation getAggregation() {
		
		MeasureAggregation aggregation = new MeasureAggregation();
		aggregation.setQualifier(getQualifier());
		aggregation.setMaxValue(getMaxValue());
		aggregation.setMaxDate(getMaxDate());
		aggregation.setMinValue(getMinValue());
		aggregation.setMinDate(getMinDate());
		aggregation.setCount((int)stats.getN());
		aggregation.setRecords((int)stats.getN());
		
		Range<Date> interval = null;
		if (firstDate.equals(lastDate)) {
			interval = Range.singleton(lastDate);
		}
		else {
			interval = Range.closed(firstDate, lastDate);
		}
		
		aggregation.setInterval(interval);
		aggregation.setDate(interval.lowerEndpoint());
		aggregation.setValue(getValue());
		
		return aggregation;
	}
	
	public Date getLastDate() {
		return lastDate;
	}





}
