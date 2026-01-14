package it.thisone.iotter.provisioning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.ModbusProfile;

public class ProvisioningEvent {
	
	private final Device master;
	private final List<ModbusProfile> profiles;
	private final List<String> originalProfiles;
	private final Map<String, String> mapSlaves;
	private final Map<String, GroupWidget> mapWidgets;
	
	public ProvisioningEvent(Device master, List<ModbusProfile> profiles,
			List<String> originalProfiles, Map<String, String> mapSlaves,
			Map<String, GroupWidget> mapWidgets
			) {
		super();
		
		if (mapWidgets == null) {
			mapWidgets = new HashMap<String, GroupWidget>();
		}

		this.master = master;
		this.profiles = profiles;
		this.originalProfiles = originalProfiles;
		this.mapSlaves = mapSlaves;
		this.mapWidgets = mapWidgets;

	}
	public Device getMaster() {
		return master;
	}
	public List<ModbusProfile> getProfiles() {
		return profiles;
	}
	public List<String> getOriginalProfiles() {
		return originalProfiles;
	}
	public Map<String, String> getMapSlaves() {
		return mapSlaves;
	}
	public Map<String, GroupWidget> getMapWidgets() {
		
		return mapWidgets;
	}

}
