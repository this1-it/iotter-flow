package it.thisone.iotter.persistence.ifc;

import java.util.List;

import org.springframework.stereotype.Repository;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.model.User;

@Repository
public interface INetworkGroupDao extends IBaseEntityDao<NetworkGroup> {
	
	public NetworkGroup findByName(String name, Network network) throws BackendServiceException;

	public NetworkGroup findByExternalId(String externalId, Network network) throws BackendServiceException;
	
	public void removeMembers(NetworkGroup entity);
	
	public List<NetworkGroup> findByNetwork(Network network);
	
	public void addMembers(NetworkGroup entity, List<User> users, List<Device> devices);



}
