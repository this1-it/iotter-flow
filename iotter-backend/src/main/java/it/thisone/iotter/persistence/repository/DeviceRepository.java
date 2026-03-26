package it.thisone.iotter.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.persistence.model.Device;

public interface DeviceRepository extends BaseEntityRepository<Device> {

	Page<Device> findByLabelStartingWithIgnoreCase(String label, Pageable pageable);

	@Query("select d from Device d where lower(d.label) like lower(concat('%', :search, '%')) "
			+ "or lower(d.serial) like lower(concat('%', :search, '%'))")
	Page<Device> searchByLabelOrSerial(@Param("search") String search, Pageable pageable);

	@Query("select d from Device d where (lower(d.label) like lower(concat('%', :search, '%')) "
			+ "or lower(d.serial) like lower(concat('%', :search, '%'))) and d.status = :status")
	Page<Device> searchByLabelOrSerialAndStatus(@Param("search") String search,
			@Param("status") DeviceStatus status, Pageable pageable);

	Page<Device> findByOwner(String owner, Pageable pageable);

	Page<Device> findByOwnerAndLabelStartingWithIgnoreCase(String owner, String label, Pageable pageable);

	@Query("select d from Device d where d.owner = :owner "
			+ "and (lower(d.label) like lower(concat('%', :search, '%')) "
			+ "or lower(d.serial) like lower(concat('%', :search, '%')))")
	Page<Device> searchByOwnerAndLabelOrSerial(@Param("owner") String owner, @Param("search") String search,
			Pageable pageable);

	Page<Device> findByOwnerAndStatus(String owner, DeviceStatus status, Pageable pageable);

	Page<Device> findByOwnerAndLabelStartingWithIgnoreCaseAndStatus(String owner, String label,
			DeviceStatus status, Pageable pageable);

	@Query("select d from Device d where d.owner = :owner "
			+ "and (lower(d.label) like lower(concat('%', :search, '%')) "
			+ "or lower(d.serial) like lower(concat('%', :search, '%'))) and d.status = :status")
	Page<Device> searchByOwnerAndLabelOrSerialAndStatus(@Param("owner") String owner,
			@Param("search") String search, @Param("status") DeviceStatus status, Pageable pageable);

	@Query("select distinct d from Device d join d.groups g where g.network.id = :networkId and d.owner = :owner")
	Page<Device> findByOwnerAndNetworkId(@Param("owner") String owner,
			@Param("networkId") String networkId, Pageable pageable);

	@Query("select distinct d from Device d join d.groups g where g.network.id = :networkId and d.owner = :owner "
			+ "and lower(d.label) like lower(concat('%', :label, '%'))")
	Page<Device> findByOwnerAndNetworkIdAndLabelStartingWithIgnoreCase(@Param("owner") String owner,
			@Param("networkId") String networkId, @Param("label") String label, Pageable pageable);

	@Query("select distinct d from Device d join d.groups g where g.network.id = :networkId and d.owner = :owner "
			+ "and (lower(d.label) like lower(concat('%', :search, '%')) "
			+ "or lower(d.serial) like lower(concat('%', :search, '%')))")
	Page<Device> searchByOwnerAndNetworkIdAndLabelOrSerial(@Param("owner") String owner,
			@Param("networkId") String networkId, @Param("search") String search, Pageable pageable);

	@Query("select distinct d from Device d join d.groups g where g.network.id = :networkId and d.owner = :owner "
			+ "and d.status = :status")
	Page<Device> findByOwnerAndNetworkIdAndStatus(@Param("owner") String owner,
			@Param("networkId") String networkId, @Param("status") DeviceStatus status, Pageable pageable);

	@Query("select distinct d from Device d join d.groups g where g.network.id = :networkId and d.owner = :owner "
			+ "and lower(d.label) like lower(concat('%', :label, '%')) and d.status = :status")
	Page<Device> findByOwnerAndNetworkIdAndLabelStartingWithIgnoreCaseAndStatus(@Param("owner") String owner,
			@Param("networkId") String networkId, @Param("label") String label,
			@Param("status") DeviceStatus status, Pageable pageable);

	@Query("select distinct d from Device d join d.groups g where g.network.id = :networkId and d.owner = :owner "
			+ "and (lower(d.label) like lower(concat('%', :search, '%')) "
			+ "or lower(d.serial) like lower(concat('%', :search, '%'))) and d.status = :status")
	Page<Device> searchByOwnerAndNetworkIdAndLabelOrSerialAndStatus(@Param("owner") String owner,
			@Param("networkId") String networkId, @Param("search") String search,
			@Param("status") DeviceStatus status, Pageable pageable);

	Page<Device> findByStatus(DeviceStatus status, Pageable pageable);

	Page<Device> findByLabelStartingWithIgnoreCaseAndStatus(String label, DeviceStatus status, Pageable pageable);

	Page<Device> findByOwnerStartingWithIgnoreCase(String owner, Pageable pageable);

	@Query("select d from Device d where lower(d.owner) like lower(concat(:owner, '%')) "
			+ "and (lower(d.label) like lower(concat('%', :search, '%')) "
			+ "or lower(d.serial) like lower(concat('%', :search, '%')))")
	Page<Device> searchByOwnerStartingWithIgnoreCaseAndLabelOrSerial(@Param("owner") String owner,
			@Param("search") String search, Pageable pageable);

	Page<Device> findByOwnerStartingWithIgnoreCaseAndLabelStartingWithIgnoreCase(String owner, String label,
			Pageable pageable);

	Page<Device> findByOwnerStartingWithIgnoreCaseAndStatus(String owner, DeviceStatus status, Pageable pageable);

	Page<Device> findByOwnerStartingWithIgnoreCaseAndLabelStartingWithIgnoreCaseAndStatus(String owner, String label,
			DeviceStatus status, Pageable pageable);

	@Query("select d from Device d where lower(d.owner) like lower(concat(:owner, '%')) "
			+ "and (lower(d.label) like lower(concat('%', :search, '%')) "
			+ "or lower(d.serial) like lower(concat('%', :search, '%'))) and d.status = :status")
	Page<Device> searchByOwnerStartingWithIgnoreCaseAndLabelOrSerialAndStatus(@Param("owner") String owner,
			@Param("search") String search, @Param("status") DeviceStatus status, Pageable pageable);
}
