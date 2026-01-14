package it.thisone.iotter.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.persistence.model.Device;

public interface DeviceRepository extends BaseEntityRepository<Device> {

	// Existing method
	Page<Device> findByLabelStartingWithIgnoreCase(String label, Pageable pageable);

	// Owner-Scoped Methods (Default mode)
	Page<Device> findByOwner(String owner, Pageable pageable);

	Page<Device> findByOwnerAndLabelStartingWithIgnoreCase(String owner, String label, Pageable pageable);

	Page<Device> findByOwnerAndStatus(String owner, DeviceStatus status, Pageable pageable);

	Page<Device> findByOwnerAndLabelStartingWithIgnoreCaseAndStatus(String owner, String label,
			DeviceStatus status, Pageable pageable);

	// Network-Scoped Methods (with @Query for many-to-many join)
	@Query("select distinct d from Device d join d.groups g where g.network.id = :networkId and d.owner = :owner")
	Page<Device> findByOwnerAndNetworkId(@Param("owner") String owner,
			@Param("networkId") String networkId, Pageable pageable);

	@Query("select distinct d from Device d join d.groups g where g.network.id = :networkId and d.owner = :owner "
			+ "and lower(d.label) like lower(concat('%', :label, '%'))")
	Page<Device> findByOwnerAndNetworkIdAndLabelStartingWithIgnoreCase(@Param("owner") String owner,
			@Param("networkId") String networkId, @Param("label") String label, Pageable pageable);

	@Query("select distinct d from Device d join d.groups g where g.network.id = :networkId and d.owner = :owner "
			+ "and d.status = :status")
	Page<Device> findByOwnerAndNetworkIdAndStatus(@Param("owner") String owner,
			@Param("networkId") String networkId, @Param("status") DeviceStatus status, Pageable pageable);

	@Query("select distinct d from Device d join d.groups g where g.network.id = :networkId and d.owner = :owner "
			+ "and lower(d.label) like lower(concat('%', :label, '%')) and d.status = :status")
	Page<Device> findByOwnerAndNetworkIdAndLabelStartingWithIgnoreCaseAndStatus(@Param("owner") String owner,
			@Param("networkId") String networkId, @Param("label") String label,
			@Param("status") DeviceStatus status, Pageable pageable);

	// ViewAll Mode Methods (no owner constraint)
	Page<Device> findByStatus(DeviceStatus status, Pageable pageable);

	Page<Device> findByLabelStartingWithIgnoreCaseAndStatus(String label, DeviceStatus status, Pageable pageable);

	Page<Device> findByOwnerStartingWithIgnoreCase(String owner, Pageable pageable);

	Page<Device> findByOwnerStartingWithIgnoreCaseAndLabelStartingWithIgnoreCase(String owner, String label,
			Pageable pageable);

	Page<Device> findByOwnerStartingWithIgnoreCaseAndStatus(String owner, DeviceStatus status, Pageable pageable);

	Page<Device> findByOwnerStartingWithIgnoreCaseAndLabelStartingWithIgnoreCaseAndStatus(String owner, String label,
			DeviceStatus status, Pageable pageable);
}
