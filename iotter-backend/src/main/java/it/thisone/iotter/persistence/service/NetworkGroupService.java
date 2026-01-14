package it.thisone.iotter.persistence.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.ifc.IDeviceDao;
import it.thisone.iotter.persistence.ifc.IGroupWidgetDao;
import it.thisone.iotter.persistence.ifc.INetworkGroupDao;
import it.thisone.iotter.persistence.ifc.IUserDao;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;

@Service
public class NetworkGroupService {

    @Autowired
    private IUserDao userDao;
	
    @Autowired
    private IDeviceDao deviceDao;

    @Autowired
    private IGroupWidgetDao groupWidgetDao;
    
    
    @Autowired
    private INetworkGroupDao dao;

    public NetworkGroupService() {
        super();
    }

    // API

    @Transactional
    public void create(NetworkGroup entity) {
        dao.create(entity);
    }

    @Transactional
    public void update(NetworkGroup entity) {
        dao.update(entity);
    }

    public NetworkGroup findOne(String id ) {
        return dao.findOne(id);
    }

    public List<NetworkGroup> findAll() {
        return dao.findAll();
    }
    
    public List<NetworkGroup> findByOwner(String owner) {
        return dao.findByOwner(owner);
    }

    public List<NetworkGroup> findByNetwork(Network network) {
        return dao.findByNetwork(network);
    }
    
    
    @Transactional
    public void deleteById(String entityId ){
    	dao.deleteById(entityId);
    }
    
    
    public NetworkGroup findByName(String name, Network network) throws BackendServiceException{
		return dao.findByName(name, network);
   }

	public void merge(NetworkGroup entity) {
		dao.merge(entity);
	}
	
    @Transactional
	public void removeMembers(NetworkGroup entity) {
    	dao.removeMembers(entity);
    }

    @Transactional
	public void remove(NetworkGroup entity) {
    	dao.removeMembers(entity);
    	dao.deleteById(entity.getId());
    }
   
   
}
