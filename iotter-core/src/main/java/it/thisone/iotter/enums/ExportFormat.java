package it.thisone.iotter.enums;


public enum ExportFormat {
	EXCEL("Excel"), CSV("CSV"), ;

	private String displayName;

	private ExportFormat(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}

}
