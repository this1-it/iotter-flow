package it.thisone.iotter.persistence.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CacheStoreMode;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.persistence.config.CascadePolicy;
import org.eclipse.persistence.config.QueryHints;
import org.springframework.stereotype.Repository;

import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.enums.FtpType;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.ifc.IDeviceDao;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.DeviceCriteria;
import it.thisone.iotter.persistence.model.DeviceModel;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;

@Repository
public class DeviceDao extends BaseEntityDao<Device> implements IDeviceDao {

	public DeviceDao() {
		super();
		setClazz(Device.class);
	}

	@Override
	public Device activate(Device device, String deviceActivation, String owner, Network network)
			throws BackendServiceException {

		boolean statuOk = device.isAvailableForActivation();
		if (!statuOk && !device.getActivationKey().equals(deviceActivation)) {
			throw new BackendServiceException("device cannot be activated" + device.getSerial());
		}
		Set<NetworkGroup> groups = new HashSet<NetworkGroup>();
		if (network != null && network.getDefaultGroup() != null) {
			groups.add(network.getDefaultGroup());
		}
		device.setOwner(owner);
		device.setStatus(DeviceStatus.ACTIVATED);
		device.setGroups(groups);
		device.getHistory().setOwner(null);
		device.getHistory().setStatus(null);
		if (device.getActivationDate() == null) {
			device.setActivationDate(new Date());
		}
		update(device);
		return device;
	}

	@Override
	public Device findBySerialCached(String serial) {
		Query q = entityManager.createNamedQuery("Device.findBySerialCached");
//		q.setHint(QueryHints.QUERY_RESULTS_CACHE, "true");
//		q.setHint(QueryHints.QUERY_RESULTS_CACHE_SIZE, "1000");
		q.setParameter("serial", serial);
		Device result = null;
		try {
			result = (Device) q.getSingleResult();
		} catch (NoResultException e) {
		}
		return result;
	}

	@Override
	public Device findBySerial(String serial) {
		if (serial == null)
			return null;
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Device> c = cb.createQuery(Device.class);
		Root<Device> root = c.from(Device.class);
		Predicate predicate = cb.equal(root.get("serial"), serial);
		TypedQuery<Device> q = entityManager.createQuery(c.select(root).where(predicate));
		/**
		 * Bug #185 [PERSISTENCE] DeviceService.findBySerial return unrefreshed list of
		 * Channel
		 */
		// http://wiki.eclipse.org/EclipseLink/Examples/JPA/Caching
		// https://en.wikibooks.org/wiki/Java_Persistence/Caching
		q.setHint(QueryHints.REFRESH_CASCADE, CascadePolicy.DEFAULT);

		// If the object/data is already in the cache, then refresh/replace it
		// with the database results.
		q.setHint(QueryHints.CACHE_STORE_MODE, CacheStoreMode.REFRESH);
		// q.setHint("javax.persistence.cache.storeMode", "REFRESH");

		List<Device> result = q.getResultList();

		if (result.size() > 0) {
			Device entity = result.get(0);
			// for (Channel channel : entity.getChannels()) {
			// for (MeasureUnit unit : channel.getMeasures()) {
			// LoggerFactory.getLogger(DeviceDao.class).error(unit.toString());
			// }
			// }
			return entity;
		}
		return null;
	}

	@Override
	public List<Device> findAllFtpAccessActive() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Device> c = cb.createQuery(Device.class);
		Root<Device> root = c.from(Device.class);
		Predicate predicate = cb.notEqual(root.get("ftpAccess").get("type"), FtpType.DISABLE);
		List<Device> result = find(c.select(root).where(predicate));
		return result;
	}

	@Override
	public List<Device> findInactive() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Device> c = cb.createQuery(Device.class);
		Root<Device> root = c.from(Device.class);
		Predicate predicate = cb.greaterThan(root.<Integer>get("inactivityMinutes"), 0);
		List<Device> result = find(c.select(root).where(predicate));
		return result;
	}

	@Override
	public List<Device> findByNetwork(Network network) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Device> cq = cb.createQuery(Device.class);
		Root<Device> root = cq.from(Device.class);
		Join<Device, NetworkGroup> group = root.join("groups", JoinType.LEFT);
		Predicate predicate = cb.equal(group.get("network"), network);
		// distinct groups
		cq.distinct(true);
		cq.orderBy(cb.asc(root.get("serial")));
		return find(cq.select(root).where(predicate));
	}

	@Override
	public Channel findActiveChannel(String deviceSerial, String channelNumber) throws BackendServiceException {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Channel> cq = cb.createQuery(Channel.class);
		Root<Channel> root = cq.from(Channel.class);
		Join<Channel, Device> device = root.join("device", JoinType.LEFT);
		Predicate predicate = cb.and( //
				cb.equal(root.get("number"), channelNumber), //
				cb.equal(root.get("configuration").get("active"), true), //
				cb.equal(device.get("serial"), deviceSerial) //
		); //
		List<Channel> result = entityManager.createQuery(cq.select(root).where(predicate)).getResultList();
		if (result.size() == 1) {
			return result.get(0);
		}
		if (result.size() > 1) {
			throw new BackendServiceException("more than one active channel with same number : " + channelNumber);
		}
		return null;
	}

	@Override
	public List<Device> findByGroup(NetworkGroup group) {
		if (group.isNew())
			return new ArrayList<>();
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Device> cq = cb.createQuery(Device.class);
		Root<Device> root = cq.from(Device.class);
		cq.orderBy(cb.asc(root.get("serial")));
		Join<Device, NetworkGroup> groups = root.join("groups", JoinType.LEFT);
		Predicate predicate = cb.equal(groups.get("id"), group.getId());
		return find(cq.select(root).where(predicate));
	}

	@Deprecated
	public boolean updateBatteryLevel(String entityId, int batteryLevel) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaUpdate<Device> updateCriteria = cb.createCriteriaUpdate(Device.class);
		Root<Device> root = updateCriteria.from(Device.class);
		// update dateOfBirth property
		updateCriteria.set(root.get("batteryLevel"), batteryLevel);
		// set where clause
		updateCriteria.where(cb.equal(root.get("id"), entityId));
		// update
		int affected = entityManager.createQuery(updateCriteria).executeUpdate();
		return (affected > 0);
	}

	@Deprecated
	public boolean updateChannelLastMeasure(String channelId, float lastMeasure) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaUpdate<Channel> updateCriteria = cb.createCriteriaUpdate(Channel.class);
		Root<Channel> root = updateCriteria.from(Channel.class);
		// update dateOfBirth property
		updateCriteria.set(root.get("lastMeasure"), lastMeasure);
		// set where clause
		updateCriteria.where(cb.equal(root.get("id"), channelId));
		// update
		int affected = entityManager.createQuery(updateCriteria).executeUpdate();
		return (affected > 0);
	}

	@Override
	public long count(DeviceCriteria criteria) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Device> root = cq.from(Device.class);
		cq.select(cb.count(root));
		cq.distinct(true);
		Predicate predicate = buildCriteria(cb, root, criteria);
		cq.where(predicate);
		return entityManager.createQuery(cq).getSingleResult();
	}

	@Override
	public List<Device> search(DeviceCriteria criteria, int offset, int limit) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Device> cq = cb.createQuery(Device.class);
		Root<Device> root = cq.from(Device.class);
		if (criteria.isActivated() != null) {
			cq.orderBy(cb.asc(root.get("productionDate")));
		}
		Predicate predicate = buildCriteria(cb, root, criteria);
		return find(cq.select(root).where(predicate), offset, limit);
	}

	/**
	 * http://stackoverflow.com/questions/11138118/really-dynamic-jpa-
	 * criteriabuilder
	 * 
	 * @param criteria
	 * @return
	 */
	private Predicate buildCriteria(CriteriaBuilder cb, Root<Device> root, DeviceCriteria criteria) {
		List<Predicate> predicates = new ArrayList<Predicate>();

		if (criteria.isExporting() != null) {
			predicates.add(cb.equal(root.get("exporting"), criteria.isExporting()));
		}

		if (criteria.isPublishing() != null) {
			predicates.add(cb.equal(root.get("publishing"), criteria.isPublishing()));
		}

		if (criteria.isMaster() != null) {
			if (criteria.isMaster()) {
				predicates.add(cb.isNull(root.get("master")));
			} else {
				predicates.add(cb.isNotNull(root.get("master")));
			}
		}

		if (criteria.isActivated() != null) {
			if (criteria.isActivated()) {
				predicates.add(cb.isNotNull(root.get("activationDate")));
			}
		}

		if (criteria.getStatus() != null) {
			predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
		}

		if (criteria.getOwner() != null) {
			predicates.add(cb.equal(root.get("owner"), criteria.getOwner()));
		}

		return cb.and(predicates.toArray(new Predicate[predicates.size()]));
	}

	// Bug #333 [PERSISTENCE] a runtime error occurs inserting a new device
	// (duplicate activation key)
	/*
	 * 
	 * (non-Javadoc)
	 * 
	 * @see it.thisone.iotter.persistence.ifc.IDeviceDao#findByActivationKey(java.
	 * lang.String)
	 */
	@Override
	public Device findByActivationKey(String activationKey) throws BackendServiceException {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Device> c = cb.createQuery(Device.class);

		Root<Device> root = c.from(Device.class);
		Predicate predicate = cb.equal(root.get("activationKey"), activationKey);

		TypedQuery<Device> q = entityManager.createQuery(c.select(root).where(predicate));
		List<Device> result = q.getResultList();
		if (result.size() == 1) {
			return result.get(0);
		}
		if (result.size() > 1) {
			throw new BackendServiceException("more than one entity with same activationKey : " + activationKey);
		}
		return null;
	}

	@Override
	public List<Device> findByModel(DeviceModel model) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Device> cq = cb.createQuery(Device.class);
		Root<Device> root = cq.from(Device.class);
		Predicate predicate = cb.equal(root.get("model").get("id"), model.getId());
		return find(cq.select(root).where(predicate));
	}

	@Override
	public List<Device> findSlaves(Device master) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Device> cq = cb.createQuery(Device.class);
		Root<Device> root = cq.from(Device.class);
		Predicate predicate = cb.equal(root.get("master").get("id"), master.getId());
		return find(cq.select(root).where(predicate));
	}

	@Override
	public boolean updateOnConfiguration(Device entity) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaUpdate<Device> updateCriteria = cb.createCriteriaUpdate(Device.class);
		Root<Device> root = updateCriteria.from(Device.class);
		updateCriteria.set(root.get("status"), entity.getStatus());
		updateCriteria.set(root.get("alarmed"), entity.isAlarmed());
		updateCriteria.set(root.get("configurationDate"), entity.getConfigurationDate());
		// set where clause
		updateCriteria.where(cb.equal(root.get("id"), entity.getId()));
		// update
		int affected = entityManager.createQuery(updateCriteria).executeUpdate();
		return (affected > 0);
	}

	@Override
	public Collection<Channel> findChannels(String id, String number) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Channel> cq = cb.createQuery(Channel.class);
		Root<Channel> root = cq.from(Channel.class);
		Join<Channel, Device> device = root.join("device", JoinType.LEFT);
		Predicate predicate = cb.and( //
				cb.equal(root.get("number"), number), //
				cb.equal(device.get("id"), id) //
		); //
		List<Channel> result = entityManager.createQuery(cq.select(root).where(predicate)).getResultList();
		if (result == null) {
			result = new ArrayList<>();
		}
		return result;
	}

	@Override
	public boolean deactivateDevice(Device entity, boolean blocked) {
		if (entity.isDeActivated() && blocked)
			return false;
		if (!entity.isDeActivated() && !blocked)
			return false;
		DeviceStatus status;
		if (blocked) {
			status = DeviceStatus.DEACTIVATED;
		} else {
//			status = entity.getHistory().getStatus();
//			if (status == null) status = DeviceStatus.ACTIVATED;
			status = DeviceStatus.ACTIVATED;
		}
		entity.setStatus(status);
		update(entity);
		return true;

	}

	public int updateLastExportDate(String deviceId, Date lastExportDate) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaUpdate<Device> update = cb.createCriteriaUpdate(Device.class);
		Root<Device> root = update.from(Device.class);
		update.set("lastExportDate", lastExportDate);
		update.where(cb.equal(root.get("serial"), deviceId));
		return entityManager.createQuery(update).executeUpdate();
	}

}
