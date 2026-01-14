package it.thisone.iotter.provisioning;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import it.thisone.iotter.persistence.model.ModbusProfile;

public interface IProvisioningProvider {
	public abstract List<ModbusProfile> availableProfiles(); 
	public abstract ModbusProfile readProfileFromExcel(String resource, InputStream is) throws IOException;
	public abstract ModbusProfile readProfileFromExcel(String resource, byte[] bytes, StringWriter writer);
}
