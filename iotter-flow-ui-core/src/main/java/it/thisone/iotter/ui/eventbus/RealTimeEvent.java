package it.thisone.iotter.ui.eventbus;

import java.io.Serializable;


public class RealTimeEvent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

 	private final Boolean realTime;
	
	public RealTimeEvent(Boolean value) {
		this.realTime = value;
	}

	public Boolean getRealTime() {
		return realTime;
	}
}
