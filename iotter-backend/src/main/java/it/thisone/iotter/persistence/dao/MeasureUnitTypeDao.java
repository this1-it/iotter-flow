package it.thisone.iotter.persistence.dao;


import org.springframework.stereotype.Repository;

import it.thisone.iotter.persistence.ifc.IMeasureUnitTypeDao;
import it.thisone.iotter.persistence.model.MeasureUnitType;

@Repository
public class MeasureUnitTypeDao extends BaseTypeDao<MeasureUnitType> implements IMeasureUnitTypeDao {
	public MeasureUnitTypeDao() {
        super();
        setClazz(MeasureUnitType.class);
    }
}
