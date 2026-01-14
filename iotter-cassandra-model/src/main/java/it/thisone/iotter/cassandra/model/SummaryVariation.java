package it.thisone.iotter.cassandra.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SummaryVariation implements Serializable {

	private static final long serialVersionUID = 6320997515033050618L;
	private final Map<Double,Integer> variations = new HashMap<>();
	private final List<Double> limits;
	
	public SummaryVariation(List<Double> limits) {
		this.limits = limits;
	}
	
	public void addAll(List<MeasureRaw> measures) {
		variations.clear();
		for (Double limit : this.limits) {
			variations.put(limit, 0);
		}
		Double current = Double.MIN_VALUE;
		for (MeasureRaw measure : measures) {
			Double value = measure.getValue().doubleValue();
			Integer count = variations.get(value);
			if (count != null) {
				if (Double.compare(value, current) != 0) {
					variations.put(value, count + 1);
					current = value;
				}
			}
		}
	}
	
	public Map<Double,Integer> getVariations() {
		return variations;
	}

}
