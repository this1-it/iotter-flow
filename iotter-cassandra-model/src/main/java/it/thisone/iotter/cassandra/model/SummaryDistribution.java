package it.thisone.iotter.cassandra.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class SummaryDistribution implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6320997515033050618L;
	private final EmpiricalDistribution distribution;
	private final List<Double> limits;
	
	public SummaryDistribution(List<Double> limits) {
		this.limits = limits;
		if (limits != null && !limits.isEmpty()) {
			this.distribution = new EmpiricalDistribution(limits.size());
		}
		else{
			this.distribution = new EmpiricalDistribution();
		}
	}
	
	public void addAll(List<MeasureRaw> measures) {
		double[] in = new double[measures.size()];
		for (int i = 0; i < in.length; i++) {
			in[i] = measures.get(i).getValue();
		}
		distribution.load(in);
	}
	
	public Map<Double,Long> getStats() {
		Map<Double,Long> stats = new HashMap<>();
		for (SummaryStatistics binStat : distribution.getBinStats()) {
			if (binStat.getN() > 0 && limits.contains(binStat.getMin())) {
				stats.put(binStat.getMin(), binStat.getN());
			}
		}
		return stats;
	}

}
