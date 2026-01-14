package it.thisone.iotter.persistence.model;

import java.util.Comparator;

import org.apache.commons.lang3.builder.CompareToBuilder;

public class GraphicFeedComparator implements Comparator<GraphicFeed> {
	@Override
	public int compare(GraphicFeed a, GraphicFeed b) {
	     return new CompareToBuilder().append(a.getMetaData(), b.getMetaData())
	    		 .toComparison();
	}
}
