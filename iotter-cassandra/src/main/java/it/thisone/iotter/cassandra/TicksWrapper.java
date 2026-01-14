package it.thisone.iotter.cassandra;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class TicksWrapper implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3069408785480468724L;
	private final List<Date> ticks;

	public TicksWrapper(List<Date> values) {
		this.ticks = values;
	}

	public List<Date> getTicks() {
		return ticks;
	}
}
