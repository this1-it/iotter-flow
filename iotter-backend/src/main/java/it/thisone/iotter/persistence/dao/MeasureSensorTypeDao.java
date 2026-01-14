package it.thisone.iotter.persistence.dao;


import org.springframework.stereotype.Repository;

import it.thisone.iotter.persistence.ifc.IMeasureSensorTypeDao;
import it.thisone.iotter.persistence.model.MeasureSensorType;

@Repository
public class MeasureSensorTypeDao extends BaseTypeDao<MeasureSensorType> implements IMeasureSensorTypeDao {
    
	public MeasureSensorTypeDao() {
        super();
        setClazz(MeasureSensorType.class);
    }
	
}
