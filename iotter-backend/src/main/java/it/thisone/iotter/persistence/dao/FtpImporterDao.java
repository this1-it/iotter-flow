package it.thisone.iotter.persistence.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.persistence.config.QueryHints;
import org.springframework.stereotype.Repository;

import it.thisone.iotter.persistence.ifc.IFtpImporterDao;
import it.thisone.iotter.persistence.model.FtpImporter;


@Repository
public class FtpImporterDao extends BaseEntityDao<FtpImporter> implements IFtpImporterDao  {

	
	public FtpImporterDao() {
        super();
        setClazz(FtpImporter.class);
    }
	
	@Override
	public FtpImporter findByName(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FtpImporter findByName(String fileName, String deviceSerial) {
		Query q = entityManager.createNamedQuery("FtpImporter.findByName");
		q.setHint(QueryHints.QUERY_RESULTS_CACHE, "true");
		q.setHint(QueryHints.QUERY_RESULTS_CACHE_SIZE, "1000");
		q.setParameter("fileName", fileName);
		q.setParameter("deviceSerial", deviceSerial);
		FtpImporter result = null;
		try {
			result = (FtpImporter)q.getSingleResult();
		} catch (NoResultException e) {
		}
		return result;
	}

	@Override
	public void updateStatus(String id, int status) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaUpdate<FtpImporter> updateCriteria = cb
				.createCriteriaUpdate(FtpImporter.class);
		Root<FtpImporter> root = updateCriteria.from(FtpImporter.class);
		updateCriteria.set(root.get("status"), status);
		updateCriteria.set(root.get("lastOperationDate"), new Date());
		updateCriteria.where(cb.equal(root.get("id"), id));
		entityManager.createQuery(updateCriteria)
				.executeUpdate();
	}

	@Override
	public void updateRetries(String id, int retries) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaUpdate<FtpImporter> updateCriteria = cb
				.createCriteriaUpdate(FtpImporter.class);
		Root<FtpImporter> root = updateCriteria.from(FtpImporter.class);
		updateCriteria.set(root.get("retries"), retries);
		updateCriteria.set(root.get("lastOperationDate"), new Date());
		updateCriteria.where(cb.equal(root.get("id"), id));
		entityManager.createQuery(updateCriteria)
				.executeUpdate();
	}

	@Override
	public List<FtpImporter> getEntries(String deviceSerial, int limit) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<FtpImporter> cq = cb.createQuery(FtpImporter.class);
		Root<FtpImporter> root = cq.from(FtpImporter.class);
		Predicate[] predicates = new Predicate[] {cb.equal(root.get("deviceSerial"), deviceSerial)};
		cq = cq.select(root).where(predicates).orderBy(cb.desc(root.get("lastOperationDate")));
		return find( cq , 0, limit);
	}
}
