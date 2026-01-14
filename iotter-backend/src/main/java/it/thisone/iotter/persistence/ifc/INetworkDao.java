package it.thisone.iotter.persistence.ifc;



import org.springframework.stereotype.Repository;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.Network;
@Repository
public interface INetworkDao extends IBaseEntityDao<Network> {

	public Network findDefaultNetwork(String owner) throws BackendServiceException;
	
	public Network findByName(String name, String owner) throws BackendServiceException;
	
	public void addDefaultGroup(Network entity);

	public Network findByExternalId(String name, String owner) throws BackendServiceException;

}
