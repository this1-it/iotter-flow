package it.thisone.iotter.persistence.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.thisone.iotter.persistence.model.BaseEntity;

public abstract class BaseEntityDao<T extends BaseEntity> {
	protected static Logger logger = LoggerFactory
			.getLogger(BaseEntityDao.class);

	private Class<T> clazz;

	@PersistenceContext
	transient EntityManager entityManager;

	public final void setClazz(Class<T> clazzToSet) {
		clazz = clazzToSet;
	}

	public Class<T> getClazz() {
		return clazz;
	}
	
	public T findOne(String id) {
		return entityManager.find(clazz, id);
	}

	public List<T> findAll() {
		List<T> result = entityManager.createQuery("from " + clazz.getName() + " be",
				clazz).getResultList();
		if (result == null) result = new ArrayList<T>();
		return result;

	}



	public List<T> find(CriteriaQuery<T> c) {
		
		// Bug #267
		// After a device has been assigned to a group, it does not 
		// shows when configuring a visualization of the same group
		TypedQuery<T> q = entityManager.createQuery(c);
		// http://wiki.eclipse.org/EclipseLink/Examples/JPA/Caching
		// http://java-persistence-performance.blogspot.it/2013/01/got-cache.html
		q.setHint("javax.persistence.cache.storeMode", "REFRESH");
//		q.setHint(QueryHints.REFRESH, HintValues.TRUE);
//		q.setHint(QueryHints.REFRESH_CASCADE, CascadePolicy.CascadeAllParts);
		List<T> result = q.getResultList();
		if (result == null) result = new ArrayList<T>();
		return result;
	}

	public List<T> find(CriteriaQuery<T> c, int offset, int limit) {
		TypedQuery<T> q = entityManager.createQuery(c);
		q.setFirstResult(offset) // offset
        .setMaxResults(limit); // limit
		// http://wiki.eclipse.org/EclipseLink/Examples/JPA/Caching
		q.setHint("javax.persistence.cache.storeMode", "REFRESH");
		List<T> result = q.getResultList();
		if (result == null) result = new ArrayList<T>();
		return result;
	}

	public long count(Predicate predicate, boolean distinct) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		cq.select(cb.count(cq.from(clazz)));
		cq.distinct(distinct);
		cq.where(predicate);
		return entityManager.createQuery(cq).getSingleResult();
	}

	public List<T> findByOwner(String owner) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> c = cb.createQuery(clazz);
		Root<T> root = c.from(clazz);
		Predicate predicate = cb.equal(root.get("owner"), owner);
		return find(c.select(root).where(predicate));
	}

	public void deleteByOwner(String owner) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		// create delete
		CriteriaDelete<T> delete = cb.createCriteriaDelete(clazz);
		// set the root class
		Root<T> root = delete.from(clazz);
		// set where clause
		delete.where(cb.equal(root.get("owner"), owner));
		entityManager.createQuery(delete).executeUpdate();
	}

	/*
	 * Bean Validation constraint(s) violated while executing Automatic Bean
	 * Validation on callback event:'prePersist'. Please refer to embedded
	 * ConstraintViolations for details.
	 */
	public void create(T entity) {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<T>> constraintViolations = validator
				.validate(entity);
		if (constraintViolations.size() > 0) {
			Iterator<ConstraintViolation<T>> iterator = constraintViolations
					.iterator();
			while (iterator.hasNext()) {
				ConstraintViolation<T> cv = iterator.next();
				logger.error("constraint(s) violated: "
						+ cv.getRootBeanClass().getName() + "."
						+ cv.getPropertyPath() + " " + cv.getMessage());
			}
		} else {
			entityManager.persist(entity);
		}
	}

	/**
	 * The merge() method does the following: it takes a detached entity, loads
	 * the attached entity with the same ID from the database, copies the state
	 * of the detached entity to the attached one, and returns the attached
	 * entity. As you not in this description, the detached entity is not
	 * modified at all, and doesn't become attached.
	 * 
	 * @param entity
	 * @return
	 */
	public T merge(T entity) {
		return entityManager.merge(entity);
	}

	public void update(T entity) {
		entityManager.persist(merge(entity));
	}

	public void delete(T entity) {
		T toBeRemoved = entityManager.merge(entity);
		entityManager.remove(toBeRemoved);
	}

	public void deleteById(String entityId) {
		T entity = findOne(entityId);
		if (entity != null) {
			delete(entity);
		}
	}
	
	public CriteriaQuery<T> buildFilterCriteria(CriteriaBuilder cb, CriteriaQuery<T> cq, List<Predicate> andPredicates, List<Predicate> orPredicates) {
		// Here goes the magic to combine both lists
		if (andPredicates.size() > 0 && orPredicates.size() == 0) {
		    // no need to make new predicate, it is already a conjunction
		    cq.where(andPredicates.toArray(new Predicate[andPredicates.size()]));
		} else if (andPredicates.size() == 0 && orPredicates.size() > 0) {
		    // make a disjunction, this part is missing above
		    Predicate p = cb.disjunction();
		    p = cb.or(orPredicates.toArray(new Predicate[orPredicates.size()]));
		    cq.where(p);
		} else {
		    // both types of statements combined
		    Predicate o = cb.and(andPredicates.toArray(new Predicate[andPredicates.size()]));
		    Predicate p = cb.or(orPredicates.toArray(new Predicate[orPredicates.size()]));
		    cq.where(o, p);
		}		
		return cq;
	}
	
	public Predicate[] buildCriteriaPredicates(CriteriaBuilder cb, List<Predicate> andPredicates, List<Predicate> orPredicates) {
		// Here goes the magic to combine both lists
		if (andPredicates.size() > 0 && orPredicates.size() == 0) {
		    // no need to make new predicate, it is already a conjunction
		    return andPredicates.toArray(new Predicate[andPredicates.size()]);
		} else if (andPredicates.size() == 0 && orPredicates.size() > 0) {
		    // make a disjunction, this part is missing above
		    Predicate p = cb.disjunction();
		    return new Predicate[] {cb.or(orPredicates.toArray(new Predicate[orPredicates.size()]))};
		} else {
		    // both types of statements combined
		    Predicate o = cb.and(andPredicates.toArray(new Predicate[andPredicates.size()]));
		    Predicate p = cb.or(orPredicates.toArray(new Predicate[orPredicates.size()]));
		    return new Predicate[] {o,p};
		}		
	}
	
	public void evictFromJpaCache(T entity) {
		entityManager.getEntityManagerFactory().getCache().evict(getClazz(), entity);
	}
	
	
}