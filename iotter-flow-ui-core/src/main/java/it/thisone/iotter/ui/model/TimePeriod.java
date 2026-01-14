package it.thisone.iotter.ui.model;

import it.thisone.iotter.enums.Period;

public class TimePeriod {
	private Period period;
	private int amount;
	private TimePeriodEnum type;
	private String name;
	
	public TimePeriod() {
		this(Period.HOUR,1,TimePeriodEnum.CURRENT);

	}
	
	public TimePeriod(Period period, int amount, TimePeriodEnum type) {
		super();
		this.period = period;
		this.amount = amount;
		this.type = type;
	}
	
	public TimePeriod(Period period, int amount, TimePeriodEnum type, String name) {
		this(period,amount,type);
		this.name = name;
	}
	

	public TimePeriodEnum getType() {
		return type;
	}

	public void setType(TimePeriodEnum type) {
		this.type = type;
	}

	public Period getPeriod() {
		return period;
	}

	public void setPeriod(Period period) {
		this.period = period;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}


	
	public long getTime() {
		return period.getMillis() * amount;
	}
	
	public enum TimePeriodEnum {
		CURRENT, LAST;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
}
