package it.thisone.iotter.cassandra.model;

import java.util.Comparator;

import org.apache.commons.lang3.builder.CompareToBuilder;

public class MeasureDateComparator implements Comparator<MeasureRaw> {
	@Override
	public int compare(MeasureRaw a, MeasureRaw b) {
	     return new CompareToBuilder().append(a.getDate(), b.getDate())
	    		 .toComparison();
	}
}
