package it.thisone.iotter.enums;

public enum FtpImporterEntryStatus {
	ERROR(-3),
	UNPARSABLE(-2),
	UNIMPORTABLE(-1),
	NOT_EXIST(0),
	DONE(1),
	CREATED(2),
	DOWNLOADING(3),
	DOWNLOADED(4),
	IMPORTING(5),
	STORING(6);

	private int value;


	private FtpImporterEntryStatus(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}

}
