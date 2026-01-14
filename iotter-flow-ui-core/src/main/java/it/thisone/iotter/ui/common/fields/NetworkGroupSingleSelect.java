package it.thisone.iotter.ui.common.fields;

import java.util.Collection;

import it.thisone.iotter.persistence.model.NetworkGroup;

public class NetworkGroupSingleSelect extends NetworkGroupSelect {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NetworkGroupSingleSelect(Collection<NetworkGroup> options) {
		super(options,false);
	}

}
