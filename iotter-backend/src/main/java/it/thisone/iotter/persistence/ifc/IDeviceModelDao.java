package it.thisone.iotter.persistence.ifc;

import org.springframework.stereotype.Repository;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.DeviceModel;
@Repository
public interface IDeviceModelDao extends IBaseEntityDao<DeviceModel> {
	public DeviceModel findByName(String name) throws BackendServiceException;
}
