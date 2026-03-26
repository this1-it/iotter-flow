package it.thisone.iotter.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import it.thisone.iotter.persistence.model.MeasureSensorType;

public interface MeasureSensorTypeRepository extends BaseEntityRepository<MeasureSensorType> {
	Page<MeasureSensorType> findByNameStartingWithIgnoreCase(String name, Pageable pageable);
}
