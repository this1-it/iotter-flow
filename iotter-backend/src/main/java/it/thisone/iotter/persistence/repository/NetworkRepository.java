package it.thisone.iotter.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import it.thisone.iotter.persistence.model.Network;

public interface NetworkRepository extends BaseEntityRepository<Network> {
	Page<Network> findByNameStartingWithIgnoreCase(String name, Pageable pageable);

	Page<Network> findByOwner(String owner, Pageable pageable);

	Page<Network> findByOwnerAndNameStartingWithIgnoreCase(String owner, String name, Pageable pageable);

	Page<Network> findByOwnerStartingWithIgnoreCase(String owner, Pageable pageable);

	Page<Network> findByNameStartingWithIgnoreCaseAndOwnerStartingWithIgnoreCase(String name, String owner,
			Pageable pageable);
}
