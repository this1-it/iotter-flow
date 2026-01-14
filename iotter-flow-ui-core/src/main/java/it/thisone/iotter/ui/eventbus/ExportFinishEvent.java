package it.thisone.iotter.ui.eventbus;

import java.io.Serializable;

public class ExportFinishEvent implements Serializable {

	public ExportFinishEvent(String owner) {
		super();
		this.owner = owner;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6190272978945468886L;

	private final String owner;

	public String getOwner() {
		return owner;
	}
	
	
}
