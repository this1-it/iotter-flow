package it.thisone.iotter.ui.eventbus;

public class ExportCSVEvent {
	private final Long entityId;
	private final char delimiter;
	private final char decimalSeparator;
	public ExportCSVEvent(Long entityId, char delimiter, char decimalSeparator) {
        this.entityId = entityId;
        this.delimiter = delimiter;
        this.decimalSeparator = decimalSeparator;
    }
	public Long getEntityId() {
		return entityId;
	}
	public char getDelimiter() {
		return delimiter;
	}
	public char getDecimalSeparator() {
		return decimalSeparator;
	}    	
}    
