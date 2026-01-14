package it.thisone.iotter.persistence.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.ifc.IMeasureUnitTypeDao;
import it.thisone.iotter.persistence.model.MeasureUnitType;

@Service
public class MeasureUnitTypeService {

    @Autowired
    private IMeasureUnitTypeDao dao;

    public MeasureUnitTypeService() {
        super();
    }

    @Transactional
    public void create(MeasureUnitType entity) {
        dao.create(entity);
    }

    @Transactional
    public void update(MeasureUnitType entity) {
        dao.update(entity);
    }

    public MeasureUnitType findOne(String id ) {
        return dao.findOne(id);
    }

    public List<MeasureUnitType> findAll() {
        return dao.findAll();
    }
    

    @Transactional
    public void deleteById(String entityId ){
    	dao.deleteById(entityId);
    }
    
    public MeasureUnitType findByCode(int code){
    	try {
			return dao.findByCode(code);
		} catch (BackendServiceException e) {
		}
    	return null;
    }
    
    
    
    
}
