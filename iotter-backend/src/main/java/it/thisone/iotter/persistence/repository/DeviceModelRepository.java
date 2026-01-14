package it.thisone.iotter.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import it.thisone.iotter.persistence.model.DeviceModel;

public interface DeviceModelRepository extends BaseEntityRepository<DeviceModel> {
	Page<DeviceModel> findByNameStartingWithIgnoreCase(String name, Pageable pageable);
}
