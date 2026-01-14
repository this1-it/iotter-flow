package it.thisone.iotter.persistence.dao;


import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import it.thisone.iotter.enums.AccountStatus;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.ifc.IDeviceDao;
import it.thisone.iotter.persistence.ifc.IGroupWidgetDao;
import it.thisone.iotter.persistence.ifc.INetworkGroupDao;
import it.thisone.iotter.persistence.ifc.IUserDao;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.model.User;

@Repository
public class NetworkGroupDao extends BaseEntityDao<NetworkGroup> implements INetworkGroupDao {
    @Autowired
    private IUserDao userDao;
	
    @Autowired
    private IDeviceDao deviceDao;

    @Autowired
    private IGroupWidgetDao groupWidgetDao;

	
	public NetworkGroupDao() {
        super();
        setClazz(NetworkGroup.class);
    }

    @Override
	public NetworkGroup findByExternalId(String externalId, Network network) throws BackendServiceException {
    	if (network == null) {
    		return null;
    	}
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<NetworkGroup> cq = cb.createQuery(NetworkGroup.class);
		Root<NetworkGroup> root = cq.from(NetworkGroup.class);
		Predicate predicate = cb.and(cb.equal(root.get("externalId"), externalId), cb.equal(root.get("network").get("id"), network.getId()));
		List<NetworkGroup> result = find(cq.select(root).where(predicate));
		if (result.size() == 1) {
			return result.get(0);
		}
		if (result.size() > 1) {
			throw new BackendServiceException("more than one group with same externalId : " + externalId);
		}
		return null;
    }
    
    
    @Override
	public NetworkGroup findByName(String name, Network network) throws BackendServiceException {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<NetworkGroup> cq = cb.createQuery(NetworkGroup.class);
		Root<NetworkGroup> root = cq.from(NetworkGroup.class);
		Predicate predicate = cb.and(cb.equal(root.get("name"), name), cb.equal(root.get("network").get("id"), network.getId()));
		List<NetworkGroup> result = find(cq.select(root).where(predicate));
		if (result.size() == 1) {
			return result.get(0);
		}
		if (result.size() > 1) {
			throw new BackendServiceException("more than one group with same name : " + name);
		}
		return null;
    }


	@Override
	public List<NetworkGroup> findByNetwork(Network network) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<NetworkGroup> cq = cb.createQuery(NetworkGroup.class);
		Root<NetworkGroup> root = cq.from(NetworkGroup.class);
		Predicate predicate =cb.equal(root.get("network").get("id"), network.getId());
		return find(cq.select(root).where(predicate));
	}
 
	
	@Override
	public void removeMembers(NetworkGroup entity) {
    	List <User> users = userDao.findByGroup(entity);
    	for (User user : users) {
    		if (user.getAccountStatus().equals(AccountStatus.HIDDEN)) {
    			userDao.delete(user);
    		}
    		else {
    			user.getGroups().remove(entity);
    			userDao.update(user);    			
    		}
		}
    	List <Device> devices = deviceDao.findByGroup(entity);
    	for (Device device : devices) {
    		device.getGroups().remove(entity);
    		deviceDao.update(device);
		}
    	List <GroupWidget> groupWidgets = groupWidgetDao.findByNetworkGroup(entity);
    	for (GroupWidget groupWidget : groupWidgets) {
    		groupWidget.setGroup(null);
    		groupWidgetDao.update(groupWidget);
		}
	}

	@Override
	public void addMembers(NetworkGroup entity, List<User> users, List<Device> devices) {
    	for (User user : users) {
			user.getGroups().add(entity);
			userDao.update(user);
		}
    	for (Device device : devices) {
    		device.getGroups().add(entity);
    		deviceDao.update(device);
		}
		
	}
	
}
