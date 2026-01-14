package it.thisone.iotter.persistence.dao;


import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.ifc.IDeviceModelDao;
import it.thisone.iotter.persistence.model.DeviceModel;

@Repository
public class DeviceModelDao extends BaseEntityDao<DeviceModel> implements IDeviceModelDao {
    
	public DeviceModelDao() {
        super();
        setClazz(DeviceModel.class);
    }

	
    @Override
	public DeviceModel findByName(String name) throws BackendServiceException {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<DeviceModel> c = cb.createQuery(DeviceModel.class);
		Root<DeviceModel> root = c.from(DeviceModel.class);
		Predicate predicate = cb.equal(root.get("name"), name);
		List<DeviceModel> result = find(c.select(root).where(predicate));
		if (result.size() == 1) {
			return result.get(0);
		}
		if (result.size() > 1) {
			throw new BackendServiceException("more than one entity with same name : " + name);
		}
		return null;
    }



	
	
}
