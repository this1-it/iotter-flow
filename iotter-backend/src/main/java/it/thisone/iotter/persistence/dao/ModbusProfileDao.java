package it.thisone.iotter.persistence.dao;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import it.thisone.iotter.enums.modbus.TemplateState;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.ifc.IModbusProfileDao;
import it.thisone.iotter.persistence.model.ModbusProfile;


/*

SELECT REVISION,TEMPLATE from MODBUS_PROFILE t1 WHERE 
RESOURCE IS NOT NULL AND 
t1.REVISION  = (SELECT max(REVISION) FROM MODBUS_PROFILE t2 WHERE t2.TEMPLATE = t1.TEMPLATE) ;

 */

@Repository
public class ModbusProfileDao extends BaseEntityDao<ModbusProfile> implements IModbusProfileDao {
    public ModbusProfileDao() {
        super();
        setClazz(ModbusProfile.class);
    }

	@Override
	public List<ModbusProfile> findTemplates() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<ModbusProfile> c = cb.createQuery(ModbusProfile.class);
		Root<ModbusProfile> root = c.from(ModbusProfile.class);
		Predicate predicate = cb.isNotNull(root.get("resource"));
		List<ModbusProfile> result = find(c.select(root).where(predicate));
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	// "displayName", "revision", "template" t1.id, t1.displayName,t1.revision,t1.template
	public List<ModbusProfile> findLastTemplates(boolean supervisor) {
		// Bug #1986
		Query q = null;
		if (supervisor) {
			String qlString = "SELECT t1 from ModbusProfile t1 WHERE t1.resource IS NOT NULL AND t1.revision  = (SELECT max(t2.revision) FROM ModbusProfile t2 WHERE t2.template = t1.template)";
			q = entityManager.createQuery(qlString);
		}
		else {
			String qlString = "SELECT t1 from ModbusProfile t1 WHERE t1.resource IS NOT NULL AND t1.revision  = (SELECT max(t2.revision) FROM ModbusProfile t2 WHERE t2.template = t1.template and t2.state = :enumState)";
			q = entityManager.createQuery(qlString).setParameter("enumState", TemplateState.PUBLIC);
		}
		List list = q.getResultList();
		return list;
	}

	
	@Override
	public ModbusProfile findTemplate(String displayName, String revision) throws BackendServiceException {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<ModbusProfile> c = cb.createQuery(ModbusProfile.class);
		Root<ModbusProfile> root = c.from(ModbusProfile.class);
		Predicate predicate = cb.and( //
				cb.equal(root.get("displayName"), displayName), //
				cb.equal(root.get("revision"), revision), //
				cb.isNotNull(root.get("resource")) //
				); //
		List<ModbusProfile> result = find(c.select(root).where(predicate));
		if (result.size() == 1) {
			return result.get(0);
		}
		if (result.size() > 1) {
			throw new BackendServiceException(
					"more than one ModbusProfile with displayName : "
							+ displayName);
		}
		return null;
	}
	
	@Override
	public boolean updateCreationDate(ModbusProfile entity) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaUpdate<ModbusProfile> updateCriteria = cb
				.createCriteriaUpdate(ModbusProfile.class);
		Root<ModbusProfile> root = updateCriteria.from(ModbusProfile.class);
		updateCriteria.set(root.get("creationDate"), new Date());
		// set where clause
		updateCriteria.where(cb.equal(root.get("id"), entity.getId()));
		// update
		int affected = entityManager.createQuery(updateCriteria)
				.executeUpdate();
		return (affected > 0);
	}

}
