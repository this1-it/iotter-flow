package it.thisone.iotter.persistence.dao;


import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import it.thisone.iotter.persistence.ifc.IRoleDao;
import it.thisone.iotter.persistence.model.Role;

@Repository
public class RoleDao extends BaseEntityDao<Role> implements IRoleDao {
    
	public RoleDao() {
        super();
        setClazz(Role.class);
    }
    
    @Override
	public Role findByName(String name){
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Role> c = cb.createQuery(Role.class);
		Root<Role> root = c.from(Role.class);
		Predicate predicate = cb.equal(root.get("name"), name);
		List<Role> result = find(c.select(root).where(predicate));
		if (result.size() > 0) {
			return result.get(0);
		}
		return null;
    }
    
}
