package it.thisone.iotter.persistence.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.persistence.ifc.IRoleDao;
import it.thisone.iotter.persistence.model.Role;

@Service
public class RoleService {

	@Autowired
	private IRoleDao dao;

	public RoleService() {
		super();
	}

	// API

	@Transactional
	public void create(Role entity) {
		dao.create(entity);
	}

	@Transactional
	public void update(Role entity) {
		dao.update(entity);
	}

	public Role findOne(String id) {
		return dao.findOne(id);
	}

	@Cacheable(value = Constants.Cache.ROLE, unless="#result == null")
	public Role findByName(String name) {
		return dao.findByName(name);
	}

	public List<Role> findAll() {
		return dao.findAll();
	}

	public List<Role> findByOwner(String owner) {
		return dao.findByOwner(owner);
	}

	@Transactional
	public void deleteById(String entityId) {
		dao.deleteById(entityId);
	}

	@Transactional
	public Role safeCreate(String name, String description) {
		Role entity = findByName(name);
		if (entity == null) {
			entity = new Role(name, description);
			create(entity);
		}
		return entity;
	}

}
