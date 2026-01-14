package it.thisone.iotter.persistence.dao;


import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.ifc.INetworkDao;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;

@Repository
public class NetworkDao extends BaseEntityDao<Network> implements INetworkDao {

	public NetworkDao() {
        super();
        setClazz(Network.class);
    }
    
    @Override
    /**
     * Creates network and default group
     */
    public void create(Network entity) {
    	addDefaultGroup(entity);
    	super.create(entity);
    }
    
    /**
     * default group is created after create/update
     */
    @Override
	public void addDefaultGroup(Network network) {
    	NetworkGroup group = network.getDefaultGroup();
    	if (group == null) {
    		group = new NetworkGroup();
    		group.setDefaultGroup(true);
    		group.setName(Constants.DEFAULT_GROUP);
    		group.setOwner(network.getOwner());
    		group.setNetwork(network);
    		network.addGroup(group);
    	}
    }

    @Override
	public Network findByName(String name, String owner) throws BackendServiceException {
    	String fieldName = "name";
    	String fieldOwner = "owner";
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Network> c = cb.createQuery(Network.class);
		Root<Network> root = c.from(Network.class);
		Predicate predicate = cb.and(cb.equal(root.get(fieldName), name), cb.equal(root.get(fieldOwner), owner));
		List<Network> result = find(c.select(root).where(predicate));
		if (result.size() == 1) {
			return result.get(0);
		}
		if (result.size() > 1) {
			throw new BackendServiceException("more than one network with same name : " + name);
		}
		return null;
    }
    
    @Override
	public Network findByExternalId(String externalId, String owner) throws BackendServiceException {
    	String fieldName = "externalId";
    	String fieldOwner = "owner";
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Network> c = cb.createQuery(Network.class);
		Root<Network> root = c.from(Network.class);
		Predicate predicate = cb.and(cb.equal(root.get(fieldName), externalId), cb.equal(root.get(fieldOwner), owner));
		List<Network> result = find(c.select(root).where(predicate));
		if (result.size() == 1) {
			return result.get(0);
		}
		if (result.size() > 1) {
			throw new BackendServiceException("more than one network with same externalId : " + externalId);
		}
		return null;
    }
    
	public Network findDefaultNetwork(String owner) throws BackendServiceException {
		String fieldDefaultNetwork = "defaultNetwork";
		String fieldOwner = "owner";
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Network> c = cb.createQuery(Network.class);
		Root<Network> root = c.from(Network.class);
		Predicate predicate = cb.and(cb.equal(root.get(fieldDefaultNetwork), true), cb.equal(root.get(fieldOwner), owner));
		List<Network> result = find(c.select(root).where(predicate));
		if (result.size() == 1) {
			return result.get(0);
		}
		if (result.size() > 1) {
			throw new BackendServiceException("more than one default network");
		}
		return null;
	}


}
