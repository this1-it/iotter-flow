package it.thisone.iotter.persistence.ifc;

import java.util.List;

import org.springframework.stereotype.Repository;

import it.thisone.iotter.persistence.model.FtpImporter;
@Repository
public interface IFtpImporterDao extends IBaseEntityDao<FtpImporter>{
	public FtpImporter findByName(String name);
	public FtpImporter findByName(String fileName, String deviceSerial);
	public void updateStatus(String id, int status);
	public void updateRetries(String id, int retries);
	public List<FtpImporter> getEntries(String serial, int size);
}
