package it.thisone.iotter.persistence.ifc;

import java.util.List;

import org.springframework.stereotype.Repository;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
@Repository
public interface IGroupWidgetDao extends IBaseEntityDao<GroupWidget>{

	
	public GroupWidget findByName(String name, String owner) throws BackendServiceException;

	public List<GroupWidget> findByNetwork(Network network);

	public List<GroupWidget> findByNetworkGroup(NetworkGroup group);
	
	public List<GroupWidget> findByDevice(Device device);

	public GroupWidget findByExternalId(String externalId, String serial) throws BackendServiceException;

	/**
	 * @param serial
	 * @return exclusive visualizations created from device
	 */
	public List<GroupWidget> findExclusiveVisualizations(String serial);

	/**
	 * find all graph feeds which have channels belonging to a device
	 * 
	 * @param device
	 * @return
	 */
	public List<GraphicFeed> findGraphFeedByDevice(Device entity);

	public List<GraphicFeed> findGraphFeedByChannel(Channel chnl);

	public List<GroupWidget> findByCreator(String username);

	public List<GroupWidget> findByDeviceSerial(String device);


}
