package it.thisone.iotter.ui.model;

import java.io.Serializable;

import it.thisone.iotter.persistence.model.BaseEntity;
import it.thisone.iotter.persistence.model.GroupWidget;

public class GroupWidgetAdapter extends BaseEntity implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2385484553372046513L;

	public GroupWidgetAdapter() {
		super();
	}

	public GroupWidgetAdapter(GroupWidget item) {
		super();
		this.item = item;
	}
	
	@Override
	public long getConsistencyVersion() {
		return item.getConsistencyVersion();
	}
	
	private GroupWidget item;
	
	private String visualization;

	private boolean selected;
	
	private String network;

	private String networkId;

	public GroupWidget getItem() {
		return item;
	}

	public void setItem(GroupWidget item) {
		this.item = item;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public String getNetworkId() {
		return networkId;
	}

	public void setNetworkId(String networkId) {
		this.networkId = networkId;
	}

	public String getVisualization() {
		return visualization;
	}

	public void setVisualization(String visualization) {
		this.visualization = visualization;
	}



	
}
