package it.thisone.iotter.persistence.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.Index;

import it.thisone.iotter.enums.FtpImporterEntryStatus;

@Entity
@Table(name = "FTP_IMPORTER")
@Index(name="FTP_IMPORTER_NAME_INDEX", columnNames={"FILE_NAME", "DEVICE_SERIAL" }, unique=true )
@NamedQuery(name = "FtpImporter.findByName", query = "SELECT e FROM FtpImporter e WHERE e.fileName = :fileName AND e.deviceSerial = :deviceSerial")
public class FtpImporter extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * create channel with empty configuration
	 */
	public FtpImporter() {
		super();
	}
	
	@Override
	public String toString() {
		return String.format("fileName=%s ,retries=%d, status=%d, deviceSerial=%s", fileName, retries, status, deviceSerial);
	}
	
	@Column(name = "URL", unique = true)
	private String url;
	
	@Column(name = "DEVICE_SERIAL")
	private String deviceSerial;
	
	@Column(name = "STATUS")
	private int status;

	@Column(name = "FILE_NAME")
	private String fileName;
	
	@Column(name = "RETRIES")
	private int retries;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TIME_STAMP")
	private Date timestamp;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_OPERATION_TIME")
	private Date lastOperationDate;

	public String getDeviceSerial() {
		return deviceSerial;
	}

	public void setDeviceSerial(String deviceSerial) {
		this.deviceSerial = deviceSerial;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getLastOperationDate() {
		return lastOperationDate;
	}

	public void setLastOperationDate(Date lastOperationDate) {
		this.lastOperationDate = lastOperationDate;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getRetries() {
		return retries;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@Transient
	public FtpImporterEntryStatus getStatusAsEnum() {
		FtpImporterEntryStatus value = FtpImporterEntryStatus.NOT_EXIST;
		for (FtpImporterEntryStatus entry : FtpImporterEntryStatus.values()) {
			  if (entry.getValue() == status) {
				  value = entry;
				  break;
			  }
		}
		return value;
	}
	
}
