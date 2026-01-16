package it.thisone.iotter.ui.common.fields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.component.grid.Grid;

import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;

public class NetworkGroupSelect extends Grid<NetworkGroup> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean mandatoryNetwork = true;

	/**
	 * table should be enough smart to show networks/groups of a specific
	 * administrator
	 */
	private ListDataProvider<NetworkGroup> dataProvider;
	
	public NetworkGroupSelect(Collection<NetworkGroup> options, boolean multi) {
		super();
		List<NetworkGroup> items = new ArrayList<>();
		if (options != null) {
			items.addAll(options);
		}
		dataProvider = new ListDataProvider<>(items);
		setDataProvider(dataProvider);
		
		removeAllColumns();
		addColumn(NetworkGroup::getName);
		//setHeaderVisible(false);
		
		if (multi) {
			setSelectionMode(SelectionMode.MULTI);
		} else {
			setSelectionMode(SelectionMode.SINGLE);
			// TODO: Add validator for single selection
		}
		
//		setHeightMode(HeightMode.ROW);
//		setHeightByRows(3);
		setWidth("100%");
	}

	/**
	 * Handling a ComboBox that select network for group filtering
	 * 
	 * @param combo
	 *            contains all available network
	 * @param defaultNetwork
	 *            preselected network
	 * @param currentNetwork
	 *            assigned network
	 */
	public void setNetworkSelection(NetworkSelect combo, Network defaultNetwork,  Network currentNetwork) {
		final NetworkGroupSelect grid = NetworkGroupSelect.this;
		final ListDataProvider<NetworkGroup> groups = grid.dataProvider;
		// Note: combo parameter handling will need to be updated when NetworkSelect is migrated

		 if (defaultNetwork != null) {
		 	currentNetwork = defaultNetwork;
		 }

		final AtomicReference<String> currentNetworkRef = new AtomicReference<>();
		if (defaultNetwork != null) {
			currentNetworkRef.set(defaultNetwork.getId());
		}

		// Feature #298 Disassocia strumento dalla rete
		// leave network combo not selected unless use groups
		// TODO: Handle network selection when NetworkSelect is migrated
		
		if (currentNetwork != null) {
			// TODO: combo.select(currentNetwork) when NetworkSelect is migrated
			groups.clearFilters();
			groups.addFilter(group -> group.getNetwork().getId().equals(currentNetworkRef));
		}

		/**
		 * a network has been preselected then group selection is mandatory
		 */
		if (defaultNetwork != null) {
			combo.setValue(currentNetwork);
			combo.setReadOnly(true);
		}

		/**
		 * no network / no groups then group selection should not have any
		 * constraints
		 */
		if (groups.getItems().isEmpty()) {
			// TODO: Remove validators when implemented
		}

		/**
		 * if groups are hidden, default group must be preselected
		 */
		Set<NetworkGroup> selectedItems = grid.getSelectedItems();
		if (selectedItems.isEmpty()) {
			if (currentNetwork != null) {
				grid.select(currentNetwork.getDefaultGroup());
			}
		}

		// TODO: Add value change listener when NetworkSelect is migrated to Vaadin 8
		combo.addValueChangeListener(event -> {
		    Network network = event.getValue();
		    if (network != null) {
		        groups.clearFilters();
		        grid.deselectAll();
		        groups.addFilter(group -> group.getNetwork().getId().equals(network.getId()));
		        groups.getItems().stream()
		            .filter(NetworkGroup::isDefaultGroup)
		            .findFirst()
		            .ifPresent(grid::select);
		        //grid.setHeightByRows(3);
		    }
		});
	}

	public void selectDefaultGroup() {
		deselectAll();
		dataProvider.getItems().stream()
			.filter(NetworkGroup::isDefaultGroup)
			.findFirst()
			.ifPresent(this::select);
	}

	public boolean isMandatoryNetwork() {
		return mandatoryNetwork;
	}

	public void setMandatoryNetwork(boolean mandatoryNetwork) {
		this.mandatoryNetwork = mandatoryNetwork;
	}
	
	
	public void setExclusiveGroups(Collection<NetworkGroup> options) {
		dataProvider.getItems().clear();
		for (NetworkGroup group : options) {
			if (group.isExclusive()) {
				dataProvider.getItems().add(group);
			}
		}
		dataProvider.refreshAll();
		
	}

	public boolean isEmpty() {
		return dataProvider.getItems().isEmpty();
	}


}
