package it.thisone.iotter.integration;

import java.util.List;

import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GroupWidget;

public interface IExclusiveVisualizationHelper {
	
	public String getName();
	
	public List<GraphicFeed> materializeFeeds(Device device, GroupWidget entity);
}
