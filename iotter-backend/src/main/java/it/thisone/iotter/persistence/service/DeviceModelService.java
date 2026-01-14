package it.thisone.iotter.persistence.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.ifc.IDeviceModelDao;
import it.thisone.iotter.persistence.model.DeviceModel;

@Service
public class DeviceModelService {

    @Autowired
    private IDeviceModelDao dao;

    public DeviceModelService() {
        super();
    }

    @Transactional
    public void create(DeviceModel entity) {
        dao.create(entity);
    }

    @Transactional
    public void update(DeviceModel entity) {
        dao.update(entity);
    }

    public DeviceModel findOne(String id ) {
        return dao.findOne(id);
    }

    public List<DeviceModel> findAll() {
        return dao.findAll();
    }
    

    @Transactional
    public void deleteById(String entityId ){
    	dao.deleteById(entityId);
    }
    
    public DeviceModel findByName(String name) throws BackendServiceException {
    	return dao.findByName(name);
    }
}
