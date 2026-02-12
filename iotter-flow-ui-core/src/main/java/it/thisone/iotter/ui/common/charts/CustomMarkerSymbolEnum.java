package it.thisone.iotter.ui.common.charts;


public enum CustomMarkerSymbolEnum  {
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