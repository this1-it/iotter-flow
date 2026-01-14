package it.thisone.iotter.persistence.ifc;


import org.springframework.stereotype.Repository;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.MeasureUnitType;
@Repository
public interface IMeasureUnitTypeDao extends IBaseEntityDao<MeasureUnitType> {

	public int maxCode();
	
	public MeasureUnitType findByName(String name)throws BackendServiceException;

	public MeasureUnitType findByCode(int type)throws BackendServiceException;

}
