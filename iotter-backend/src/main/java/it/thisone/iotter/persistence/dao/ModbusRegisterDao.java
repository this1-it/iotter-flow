package it.thisone.iotter.persistence.dao;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import it.thisone.iotter.enums.modbus.TypeRead;
import it.thisone.iotter.persistence.ifc.IModbusRegisterDao;
import it.thisone.iotter.persistence.model.ModbusRegister;

@Repository
public class ModbusRegisterDao extends BaseEntityDao<ModbusRegister> implements IModbusRegisterDao {
	public ModbusRegisterDao() {
		super();
		setClazz(ModbusRegister.class);
	}

	@Override
	public String getMetadata(String id) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<ModbusRegister> root = query.from(ModbusRegister.class);
		query.select(root.<String> get("metaData")).where(cb.equal(root.get("id"), id));
		TypedQuery<String> typedQuery = entityManager.createQuery(query);
		List<String> values = (List<String>) typedQuery.getResultList();
		if (values != null && !values.isEmpty()) {
			return values.remove(0);
		}
		return null;

	}
	@Override
	public List<ModbusRegister> findCompatibleRegisters(String displayName, Integer address, TypeRead typeRead) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<ModbusRegister> query = cb.createQuery(ModbusRegister.class);
		Root<ModbusRegister> root = query.from(ModbusRegister.class);
		List<Predicate> predicates = new ArrayList<Predicate>();
		if (displayName != null) {
			predicates.add(cb.equal(root.get("displayName"), displayName));
		}
		if (address != null) {
			predicates.add(cb.equal(root.get("address"), address));
		}
		if (typeRead != null) {
			predicates.add(cb.equal(root.get("typeRead"), typeRead));
		}
		return find(query.where(cb.and(predicates.toArray(new Predicate[predicates.size()]))));
	}

	
	
	

}
