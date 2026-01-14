package it.thisone.iotter.persistence.ifc;

import java.util.List;

import it.thisone.iotter.persistence.model.BaseEntity;


public interface IBaseEntityDao<T extends BaseEntity> {
	public T merge(T entity);
	public List<T> findByOwner(String owner);
	public T findOne(String id);
	public List<T> findAll();
	public void create(T entity);
	public void update(T entity);
	public void delete(T entity);
	public void deleteById(String entityId);
}
