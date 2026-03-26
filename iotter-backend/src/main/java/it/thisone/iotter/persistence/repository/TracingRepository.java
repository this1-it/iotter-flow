package it.thisone.iotter.persistence.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.persistence.model.Tracing;

@Transactional(readOnly = true)
public interface TracingRepository extends BaseEntityRepository<Tracing> {

	Page<Tracing> findByOwnerStartingWithIgnoreCase(String owner, Pageable pageable);

	Page<Tracing> findByDeviceStartingWithIgnoreCase(String device, Pageable pageable);

	Page<Tracing> findByOwnerStartingWithIgnoreCaseAndDeviceStartingWithIgnoreCase(String owner, String device,
			Pageable pageable);

	@Query("SELECT t FROM Tracing t WHERE "
			+ "(:search IS NULL OR LOWER(t.owner) LIKE LOWER(CONCAT(:search, '%')) "
			+ "OR LOWER(t.device) LIKE LOWER(CONCAT(:search, '%'))) "
			+ "AND (:action IS NULL OR t.action = :action) "
			+ "AND (:administrator IS NULL OR LOWER(t.administrator) = LOWER(:administrator)) "
			+ "AND (:fromDate IS NULL OR t.timeStamp >= :fromDate) "
			+ "AND (:toDate IS NULL OR t.timeStamp <= :toDate)")
	Page<Tracing> findAllByFilters(@Param("search") String search, @Param("action") TracingAction action,
			@Param("administrator") String administrator, @Param("fromDate") Date fromDate,
			@Param("toDate") Date toDate, Pageable pageable);
}
