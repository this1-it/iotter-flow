package it.thisone.iotter.ui.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.vaadin.flow.data.provider.ListDataProvider;

import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.NetworkGroup;

public class GroupWidgetContainer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 168192322554059939L;
	private final List<GroupWidgetAdapter> items = new ArrayList<>();

	public GroupWidgetContainer() throws IllegalArgumentException {
	}
	
	public void addItems(Collection<GroupWidget> items, Set<NetworkGroup> groups) {
		this.items.clear();
		Collection<GroupWidgetAdapter> collection = new ArrayList<>();
		for (GroupWidget item : items) {
			GroupWidgetAdapter adapter = new GroupWidgetAdapter(item);
			adapter.setId(item.getId());
			adapter.setVisualization(item.getName());
			if (item.getGroup() != null && item.getGroup().getNetwork() != null) {
				adapter.setNetwork(item.getGroup().getNetwork().getName());
				adapter.setNetworkId(item.getGroup().getNetwork().getId());
				if (groups != null) {
					adapter.setSelected(groups.contains(item.getGroup()));
				}
				collection.add(adapter);
			}
			
		}
		this.items.addAll(collection);
		this.items.sort(Comparator.comparing(GroupWidgetAdapter::getNetwork, Comparator.nullsLast(String::compareToIgnoreCase)));
	}

	public List<GroupWidgetAdapter> getItems() {
		return items;
	}

	public ListDataProvider<GroupWidgetAdapter> asDataProvider() {
		return new ListDataProvider<>(items);
	}
	
	
}
