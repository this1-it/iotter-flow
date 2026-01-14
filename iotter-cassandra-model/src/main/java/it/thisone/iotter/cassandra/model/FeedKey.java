package it.thisone.iotter.cassandra.model;

public class FeedKey implements IFeedKey {
	public FeedKey(String serial, String key) {
		super();
		this.serial = serial;
		this.key = key;
	}
	private final String serial;
	private final String key;
	private int qualifier;

	
	public String getSerial() {
		return serial;
	}
	public String getKey() {
		return key;
	}
	public int getQualifier() {
		return qualifier;
	}
	public void setQualifier(int qualifier) {
		this.qualifier = qualifier;
	}
	
	@Override
	public String toString() {
		return String.format("key=%s ,sn=%s", key, serial);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof FeedKey == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final FeedKey otherObject = (FeedKey) obj;
		return otherObject.getKey().equals(getKey());
	}


}
