package it.thisone.iotter.persistence.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.ifc.IMeasureSensorTypeDao;
import it.thisone.iotter.persistence.model.MeasureSensorType;

@Service
public class MeasureSensorTypeService {

    @Autowired
    private IMeasureSensorTypeDao dao;

    public MeasureSensorTypeService() {
        super();
    }

    @Transactional
    public void create(MeasureSensorType entity) {
        dao.create(entity);
    }

    @Transactional
    public void update(MeasureSensorType entity) {
        dao.update(entity);
    }

    public MeasureSensorType findOne(String id ) {
        return dao.findOne(id);
    }

    public List<MeasureSensorType> findAll() {
        return dao.findAll();
    }
    

    @Transactional
    public void deleteById(String entityId ){
    	dao.deleteById(entityId);
    }
    
    /**
     * https://code.google.com/p/ehcache-spring-annotations/
     * http://ehcache.org/documentation/recipes/spring-annotations
     * @return
     */
    @Cacheable("findByCode")
    public MeasureSensorType findByCode(int code){
    	try {
			return dao.findByCode(code);
		} catch (BackendServiceException e) {
		}
    	return null;
    }
    
    
    
    
    
}
