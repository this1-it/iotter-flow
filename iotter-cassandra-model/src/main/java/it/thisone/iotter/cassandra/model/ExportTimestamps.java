package it.thisone.iotter.cassandra.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExportTimestamps implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5132932591117484154L;
	private List<Date> timestamps;
	private String qid;
	
	public ExportTimestamps() {
	}
	
	public String getQid() {
		return qid;
	}

	public void setQid(String qid) {
		this.qid = qid;
	}

	public List<Date> getTimestamps() {
		return timestamps;
	}

	public void setTimestamps(List<Date> timestamps) {
		this.timestamps = timestamps;
	}

	public Date get(int index) {
		try {
			return timestamps.get(index);
		} catch (IndexOutOfBoundsException e) {
		}
		
		return null;
	}

    /**
     * Returns a view of the portion of timestamps between the specified
     * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, inclusive.
     */
	public List<Date> subList(int fromIndex, int toIndex) {
		toIndex++;
		if (toIndex > timestamps.size()) {
			toIndex = timestamps.size();
		}
		try {
			return timestamps.subList(fromIndex, toIndex);
		} catch (IndexOutOfBoundsException e) {
		}
		return new ArrayList<Date>();
	}
}
