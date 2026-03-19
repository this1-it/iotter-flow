package it.thisone.iotter.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import it.thisone.iotter.persistence.model.GroupWidget;

@Transactional(readOnly = true)
public interface GroupWidgetRepository extends BaseEntityRepository<GroupWidget> {

	Page<GroupWidget> findByNameStartingWithIgnoreCase(String name, Pageable pageable);

	Page<GroupWidget> findByOwnerStartingWithIgnoreCase(String owner, Pageable pageable);

	Page<GroupWidget> findByNameStartingWithIgnoreCaseAndOwnerStartingWithIgnoreCase(String name, String owner,
			Pageable pageable);

	Page<GroupWidget> findByOwner(String owner, Pageable pageable);

	Page<GroupWidget> findByOwnerAndNameStartingWithIgnoreCase(String owner, String name, Pageable pageable);

	@Query(value = "select distinct gw.* from GROUP_WIDGET gw join NETWORK_GROUP g on gw.NETWORK_GROUP_ID = g.ID where g.NETWORK_ID = :networkId and gw.OWNER = :owner",
			countQuery = "select count(distinct gw.ID) from GROUP_WIDGET gw join NETWORK_GROUP g on gw.NETWORK_GROUP_ID = g.ID where g.NETWORK_ID = :networkId and gw.OWNER = :owner",
			nativeQuery = true)
	Page<GroupWidget> findByOwnerAndNetworkId(@Param("owner") String owner, @Param("networkId") String networkId,
			Pageable pageable);

	@Query(value = "select distinct gw.* from GROUP_WIDGET gw join NETWORK_GROUP g on gw.NETWORK_GROUP_ID = g.ID where g.NETWORK_ID = :networkId and gw.OWNER = :owner and lower(gw.NAME) like lower(concat('%', :name, '%'))",
			countQuery = "select count(distinct gw.ID) from GROUP_WIDGET gw join NETWORK_GROUP g on gw.NETWORK_GROUP_ID = g.ID where g.NETWORK_ID = :networkId and gw.OWNER = :owner and lower(gw.NAME) like lower(concat('%', :name, '%'))",
			nativeQuery = true)
	Page<GroupWidget> findByOwnerAndNetworkIdAndNameStartingWithIgnoreCase(@Param("owner") String owner,
			@Param("networkId") String networkId, @Param("name") String name, Pageable pageable);
}
