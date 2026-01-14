package it.thisone.iotter.persistence.ifc;

import java.util.List;

import org.springframework.stereotype.Repository;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.ModbusProfile;

@Repository
public interface IModbusProfileDao extends IBaseEntityDao<ModbusProfile> {

	public List<ModbusProfile> findTemplates();

	public ModbusProfile findTemplate(String name, String revision) throws BackendServiceException;

	public List<ModbusProfile> findLastTemplates(boolean supervisor);

	boolean updateCreationDate(ModbusProfile entity);

}
