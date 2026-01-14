package it.thisone.iotter.persistence.ifc;



import org.springframework.stereotype.Repository;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.MeasureSensorType;
@Repository
public interface IMeasureSensorTypeDao extends IBaseEntityDao<MeasureSensorType>{

	public int maxCode();
	
	public MeasureSensorType findByName(String name) throws BackendServiceException;

	public MeasureSensorType findByCode(int type) throws BackendServiceException;



}
