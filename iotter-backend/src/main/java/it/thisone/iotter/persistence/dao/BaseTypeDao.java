package it.thisone.iotter.persistence.dao;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.BaseType;

public abstract class BaseTypeDao<T extends BaseType> extends BaseEntityDao<T>{

	// Feature #188 [REST] ParamConfiguration added unit_label for file import
    public T findByName(String name) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(getClazz());
		Root<T> root = cq.from(getClazz());
		// case insensitive
		Predicate predicate = cb.equal(cb.lower(root.<String>get("name")), name.toLowerCase());
		List<T> result = find(cq.select(root).where(predicate));
		if (result != null && result.size() > 0) {
			return result.get(0);
		}
		return null;
    }
	
    public T findByCode(int code) throws BackendServiceException {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(getClazz());
		Root<T> root = cq.from(getClazz());
		Predicate predicate = cb.equal(root.get("code"), code);
		List<T> result = find(cq.select(root).where(predicate));
		if (result != null && result.size() == 1) {
			return result.get(0);
		}
		if (result != null && result.size() > 1) {
			throw new BackendServiceException("more than one entity with same type : " + code);
		}
		return null;
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
    //  Feature #188 [REST] ParamConfiguration added unit_label for file import
	public int maxCode() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(getClazz());
		Root root = cq.from(getClazz());
		cq.select((Selection<? extends T>) cb.max(root.get("code")));
		Query query = entityManager.createQuery(cq);
		Integer max = (Integer)query.getSingleResult();		
		if (max == null) {
			return 0;
		}
		return max;
    }

}
