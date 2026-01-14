package it.thisone.iotter.persistence.dao;


import org.springframework.stereotype.Repository;

import it.thisone.iotter.persistence.ifc.IDeviceGroupMapDao;
import it.thisone.iotter.persistence.model.DeviceCustomMap;

@Repository
public class DeviceGroupMapDao extends BaseEntityDao<DeviceCustomMap> implements IDeviceGroupMapDao {
	public DeviceGroupMapDao() {
        super();
        setClazz(DeviceCustomMap.class);
    }
}
