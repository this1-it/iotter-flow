package it.thisone.iotter.persistence.service;

import java.util.List;

import javax.persistence.RollbackException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.NetworkType;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.ifc.INetworkDao;
import it.thisone.iotter.persistence.ifc.INetworkGroupDao;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.model.User;

@Service
public class NetworkService {

	@Autowired
	private INetworkDao dao;

	@Autowired
	private INetworkGroupDao groupDao;

	public NetworkService() {
		super();
	}

	// API

	@Transactional
	public void create(Network entity) {
		dao.create(entity);
	}

	@Transactional
	public void update(Network entity) throws BackendServiceException {
        try {
    		dao.update(entity);
        } catch (RollbackException cause) {
        	throw new BackendServiceException(cause);
        }		

	
	}

	public Network findOne(String id ) {
		return dao.findOne(id);
	}

	public List<Network> findAll() {
		return dao.findAll();
	}

	public List<Network> findByOwner(String owner) {
		return dao.findByOwner(owner);
	}

	@Transactional
	public void deleteById(String entityId) {
		dao.deleteById(entityId);
	}

	public Network findByName(String name, String owner) throws BackendServiceException {
		return dao.findByName(name, owner);
	}

	public void addDefaultGroup(Network entity) {
		dao.addDefaultGroup(entity);
	}

	public Network merge(Network entity) {
		return dao.merge(entity);
	}

	@Transactional
	public void delete(Network entity) {
		dao.delete(entity);
	}

	@Transactional
	public void disconnect(Network entity) {
		List<NetworkGroup> groups = groupDao.findByNetwork(entity);
		for (NetworkGroup group : groups) {
			groupDao.removeMembers(group);
			groupDao.delete(group);
		}
	}
	
	
	@Transactional
	public void createDefaultNetwork(User user) throws BackendServiceException {
		if (!user.hasRole(Constants.ROLE_ADMINISTRATOR)) {
			throw new BackendServiceException("Not an administrator " + user.getUsername());
		}
		if (dao.findDefaultNetwork(user.getUsername()) == null) {
			Network network = new Network();
			network.setName(Constants.DEFAULT_NETWORK);
			network.setOwner(user.getUsername());
			network.setNetworkType(NetworkType.GEOGRAPHIC);
			network.setDefaultNetwork(true);
			dao.create(network);
		}

	}

}
