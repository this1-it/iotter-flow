package it.thisone.iotter.persistence.ifc;

import java.util.List;

import org.springframework.stereotype.Repository;

import it.thisone.iotter.enums.modbus.TypeRead;
import it.thisone.iotter.persistence.model.ModbusRegister;

@Repository
public interface IModbusRegisterDao extends IBaseEntityDao<ModbusRegister> {

	String getMetadata(String id);

	List<ModbusRegister> findCompatibleRegisters(String displayName, Integer address, TypeRead typeRead);

}
