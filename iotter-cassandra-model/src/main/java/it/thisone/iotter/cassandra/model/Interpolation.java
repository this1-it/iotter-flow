package it.thisone.iotter.cassandra.model;


import it.thisone.iotter.cassandra.CassandraConstants;
import it.thisone.iotter.common.Internationalizable;

public enum Interpolation implements Internationalizable {

	RAW(CassandraConstants.MEASURES_CF, 10, null), //
	MIN1(null, 60, RAW.name()), //
	MIN5(null, 5 * 60, RAW.name()), //
	MIN15(CassandraConstants.ROLL_UP_MIN15_CF, 15 * 60, null), //
	H1(CassandraConstants.ROLL_UP_H1_CF, 3600, null), //
	H6(null, 6 * 3600, H1.name()), //
	D1(CassandraConstants.ROLL_UP_D1_CF, 24 * 3600, null), //
	W1(CassandraConstants.ROLL_UP_W1_CF, 7 * 24 * 3600, null), //
	M1(CassandraConstants.ROLL_UP_M1_CF, 30 * 24 * 3600, null); //

	private final String columnFamily;
	private final int seconds;
	private final String virtual;

	Interpolation(String cf, int seconds, String virtual) {
		this.columnFamily = cf;
		this.seconds = seconds;
		this.virtual = virtual;
	}

	public String getColumnFamily() {
		return this.columnFamily;
	}

	public int getSeconds() {
		return this.seconds;
	}

	public String getVirtual() {
		return virtual;
	}

	public boolean greaterThan(Interpolation other) {
		if (other == null) return true;
		return seconds > other.getSeconds();
	}
	
	@Override
	public String getI18nKey() {
        return "enum.interpolation." + name().toLowerCase();        
	}

}