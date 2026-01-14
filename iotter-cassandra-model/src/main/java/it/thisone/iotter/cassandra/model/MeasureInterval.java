package it.thisone.iotter.cassandra.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MeasureInterval implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private SummaryErrors errors;
    private MeasureRaw low;
    private MeasureRaw high;
	
	private void setValues(MeasureRaw measure) {
		if (measure.hasError()) {
			errors.add(measure);
			measure.setError("");
		}
		if (low == null || low.getValue() > measure.getValue()) {
			low = measure;
		}
		if (high == null || high.getValue() < measure.getValue()) {
			high = measure;
		}
	}

	public void setValues(List<MeasureRaw> series) {
		low = null;
		high = null;
		errors = new SummaryErrors();
		for (MeasureRaw measure : series) {
			setValues(measure);
		}
	}
	
	public List<MeasureRaw> getValues() {
		if (low == null && high == null) {
			// empty list
			return new ArrayList<MeasureRaw>();
		}
		return SummaryErrors.interval(low,high,errors.getFirstError());
	}
	
	
	
}
