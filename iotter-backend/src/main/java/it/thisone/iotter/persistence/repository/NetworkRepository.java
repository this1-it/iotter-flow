package it.thisone.iotter.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.thisone.iotter.enums.NetworkType;
import it.thisone.iotter.persistence.model.Network;

public interface NetworkRepository extends BaseEntityRepository<Network> {
	Page<Network> findByNameStartingWithIgnoreCase(String name, Pageable pageable);

	Page<Network> findByOwner(String owner, Pageable pageable);

	Page<Network> findByOwnerAndNameStartingWithIgnoreCase(String owner, String name, Pageable pageable);

	Page<Network> findByOwnerStartingWithIgnoreCase(String owner, Pageable pageable);

	Page<Network> findByNameStartingWithIgnoreCaseAndOwnerStartingWithIgnoreCase(String name, String owner,
			Pageable pageable);

	@Query("SELECT n FROM Network n WHERE n.owner = :owner "
			+ "AND (:search IS NULL OR LOWER(n.name) LIKE LOWER(CONCAT(:search, '%'))) "
			+ "AND (:networkType IS NULL OR n.networkType = :networkType) "
			+ "AND (:anonymous IS NULL OR n.anonymous = :anonymous)")
	Page<Network> findByOwnerAndFilters(@Param("owner") String owner, @Param("search") String search,
			@Param("networkType") NetworkType networkType, @Param("anonymous") Boolean anonymous,
			Pageable pageable);

	@Query("SELECT n FROM Network n WHERE (:search IS NULL OR LOWER(n.name) LIKE LOWER(CONCAT(:search, '%'))) "
			+ "AND (:owner IS NULL OR LOWER(n.owner) LIKE LOWER(CONCAT(:owner, '%'))) "
			+ "AND (:networkType IS NULL OR n.networkType = :networkType) "
			+ "AND (:anonymous IS NULL OR n.anonymous = :anonymous)")
	Page<Network> findAllByFilters(@Param("search") String search, @Param("owner") String owner,
			@Param("networkType") NetworkType networkType, @Param("anonymous") Boolean anonymous, Pageable pageable);
}
