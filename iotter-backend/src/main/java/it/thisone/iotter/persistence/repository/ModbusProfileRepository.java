package it.thisone.iotter.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import it.thisone.iotter.persistence.model.ModbusProfile;

public interface ModbusProfileRepository extends BaseEntityRepository<ModbusProfile> {
	Page<ModbusProfile> findByDisplayNameStartingWithIgnoreCase(String displayName, Pageable pageable);
	Page<ModbusProfile> findByDisplayNameStartingWithIgnoreCaseAndResourceIsNotNull(String displayName, Pageable pageable);
	Page<ModbusProfile> findByResourceIsNotNull(Pageable pageable);
}
