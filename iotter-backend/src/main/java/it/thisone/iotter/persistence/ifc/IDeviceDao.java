package it.thisone.iotter.persistence.ifc;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.DeviceCriteria;
import it.thisone.iotter.persistence.model.DeviceModel;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
@Repository
public interface IDeviceDao extends IBaseEntityDao<Device> {

	public Device findBySerial(String serial);
 
	public Device activate(Device device, String activation, String owner, Network network) throws BackendServiceException;

	public Channel findActiveChannel(String serial, String channelNumber) throws BackendServiceException;

	public List<Device> findByGroup(NetworkGroup group);

	public List<Device> findByModel(DeviceModel model);
	
	public List<Device> findByNetwork(Network network);
	
	public List<Device> findAllFtpAccessActive();

	public long count(DeviceCriteria criteria);

	public List<Device> search(DeviceCriteria criteria, int offset, int limit);

	public Device findByActivationKey(String value) throws BackendServiceException;

	public Device findBySerialCached(String serial);

	public List<Device> findSlaves(Device master);	
	
	public List<Device> findInactive();

	boolean updateOnConfiguration(Device entity);

	Collection<Channel> findChannels(String id, String number);

	boolean deactivateDevice(Device device, boolean blocked);
	
	void evictFromJpaCache(Device entity);
	
	int updateLastExportDate(String deviceId, Date lastExportDate);
	
}
