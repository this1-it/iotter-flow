package it.thisone.iotter.persistence.model;

import java.util.Comparator;

import org.apache.commons.lang3.builder.CompareToBuilder;

public class ChannelNumberComparator implements Comparator<Channel> {
	@Override
	public int compare(Channel a, Channel b) {
	     return new CompareToBuilder().append(a.getNumber(), b.getNumber()).toComparison();
	}
}
