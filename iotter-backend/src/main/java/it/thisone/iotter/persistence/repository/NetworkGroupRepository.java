package it.thisone.iotter.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import it.thisone.iotter.persistence.model.NetworkGroup;

public interface NetworkGroupRepository extends BaseEntityRepository<NetworkGroup> {
	Page<NetworkGroup> findByOwner(String owner, Pageable pageable);

	Page<NetworkGroup> findByOwnerStartingWithIgnoreCase(String owner, Pageable pageable);

	Page<NetworkGroup> findByNameStartingWithIgnoreCase(String name, Pageable pageable);

	Page<NetworkGroup> findByDescriptionStartingWithIgnoreCase(String description, Pageable pageable);

	Page<NetworkGroup> findByNameStartingWithIgnoreCaseAndDescriptionStartingWithIgnoreCase(String name,
			String description, Pageable pageable);

	Page<NetworkGroup> findByOwnerAndNameStartingWithIgnoreCase(String owner, String name, Pageable pageable);

	Page<NetworkGroup> findByOwnerAndDescriptionStartingWithIgnoreCase(String owner, String description,
			Pageable pageable);

	Page<NetworkGroup> findByOwnerAndNameStartingWithIgnoreCaseAndDescriptionStartingWithIgnoreCase(String owner,
			String name, String description, Pageable pageable);

	Page<NetworkGroup> findByOwnerStartingWithIgnoreCaseAndNameStartingWithIgnoreCase(String owner, String name,
			Pageable pageable);

	Page<NetworkGroup> findByOwnerStartingWithIgnoreCaseAndDescriptionStartingWithIgnoreCase(String owner,
			String description, Pageable pageable);

	Page<NetworkGroup> findByOwnerStartingWithIgnoreCaseAndNameStartingWithIgnoreCaseAndDescriptionStartingWithIgnoreCase(
			String owner, String name, String description, Pageable pageable);

	Page<NetworkGroup> findByOwnerAndNetworkId(String owner, String networkId, Pageable pageable);

	Page<NetworkGroup> findByOwnerAndNetworkIdAndNameStartingWithIgnoreCase(String owner, String networkId, String name,
			Pageable pageable);

	Page<NetworkGroup> findByOwnerAndNetworkIdAndDescriptionStartingWithIgnoreCase(String owner, String networkId,
			String description, Pageable pageable);

	Page<NetworkGroup> findByOwnerAndNetworkIdAndNameStartingWithIgnoreCaseAndDescriptionStartingWithIgnoreCase(
			String owner, String networkId, String name, String description, Pageable pageable);
}
