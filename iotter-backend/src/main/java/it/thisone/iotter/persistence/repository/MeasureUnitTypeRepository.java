package it.thisone.iotter.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import it.thisone.iotter.persistence.model.MeasureUnitType;

public interface MeasureUnitTypeRepository extends BaseEntityRepository<MeasureUnitType> {
	Page<MeasureUnitType> findByNameStartingWithIgnoreCase(String name, Pageable pageable);
}
