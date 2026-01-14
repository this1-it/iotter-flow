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

	@Query("select distinct gw from GroupWidget gw join gw.group g where g.network.id = :networkId and gw.owner = :owner")
	Page<GroupWidget> findByOwnerAndNetworkId(@Param("owner") String owner, @Param("networkId") String networkId,
			Pageable pageable);

	@Query("select distinct gw from GroupWidget gw join gw.group g where g.network.id = :networkId and gw.owner = :owner and lower(gw.name) like lower(concat('%', :name, '%'))")
	Page<GroupWidget> findByOwnerAndNetworkIdAndNameStartingWithIgnoreCase(@Param("owner") String owner,
			@Param("networkId") String networkId, @Param("name") String name, Pageable pageable);
}
