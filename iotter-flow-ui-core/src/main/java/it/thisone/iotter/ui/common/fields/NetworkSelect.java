package it.thisone.iotter.ui.common.fields;

import java.util.Collection;

import com.vaadin.flow.component.combobox.ComboBox;

import it.thisone.iotter.persistence.model.Network;

public class NetworkSelect  extends ComboBox<Network> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * constructor which populates the select with existing networks.
	 */
	public NetworkSelect(Collection<Network> options) {
		super("",options);
		this.setEmptySelectionAllowed(false);
		this.setLabelCaptionGenerator(Network::getName);
	}

}
