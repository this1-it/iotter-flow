package it.thisone.iotter.ui.common.charts;

import com.vaadin.addon.charts.model.ChartEnum;
import com.vaadin.addon.charts.model.MarkerSymbol;

public enum CustomMarkerSymbolEnum implements MarkerSymbol, ChartEnum {
	CIRCLE("circle"), //
	SQUARE("square"), //
	DIAMOND("diamond"), //
	TRIANGLE("triangle"), //
	TRIANGLE_DOWN("triangle-down"), //
	ARROW("arrow");

	private String symbol;

	private CustomMarkerSymbolEnum(String symbol) {
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return symbol;
	}
}