package it.thisone.iotter.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import it.thisone.iotter.persistence.model.Tracing;

@Transactional(readOnly = true)
public interface TracingRepository extends BaseEntityRepository<Tracing> {

	Page<Tracing> findByOwnerStartingWithIgnoreCase(String owner, Pageable pageable);

	Page<Tracing> findByDeviceStartingWithIgnoreCase(String device, Pageable pageable);

	Page<Tracing> findByOwnerStartingWithIgnoreCaseAndDeviceStartingWithIgnoreCase(String owner, String device,
			Pageable pageable);
}
