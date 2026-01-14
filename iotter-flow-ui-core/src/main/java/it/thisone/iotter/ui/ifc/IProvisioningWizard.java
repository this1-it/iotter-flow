package it.thisone.iotter.ui.ifc;

import java.util.List;
import java.util.Map;

import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.ModbusProfile;
import it.thisone.iotter.ui.common.EditorSavedListener;

public interface IProvisioningWizard {

	ModbusProfile getSelected();

	void setSelected(ModbusProfile selected);

	List<ModbusProfile> getProfiles();

	List<String> getOriginalProfiles();

	Map<String, String> getMapSlaves();

	Map<String, GroupWidget> getMapWidgets();
	
	Map<String, GraphicWidget> getMapGraphics();

	void addListener(EditorSavedListener editorSavedListener);

	float[] getWindowDimension();

	String getWindowStyle();
	
	int getMaxTotalRegisters();
	
	int getMaxAllowedBandwidthRatio();
	
	int getMaxProfiles();

	void clear();

}