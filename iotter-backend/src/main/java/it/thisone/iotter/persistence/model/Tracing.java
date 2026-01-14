package it.thisone.iotter.persistence.model;

import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Indexes;

import it.thisone.iotter.enums.TracingAction;

// ALTER TABLE TRACING ADD INDEX TRACING_TIMESTAMP_INDEX (TIMESTAMP);
// DELETE LOW_PRIORITY FROM TRACING WHERE TIMESTAMP < DATE_SUB(NOW(), INTERVAL 7 DAY)

/*

https://www.sitepoint.com/how-to-create-mysql-events/
 
CREATE EVENT IF NOT EXISTS tracing_cleaner_event
ON SCHEDULE AT CURRENT_TIMESTAMP + INTERVAL 1 DAY 
ON COMPLETION PRESERVE
DO 
DELETE LOW_PRIORITY FROM TRACING WHERE TIMESTAMP < DATE_SUB(NOW(), INTERVAL 1 DAY)

drop event tracing_cleaner_event;
CREATE EVENT IF NOT EXISTS tracing_cleaner_event
ON SCHEDULE EVERY 1 DAY_HOUR
COMMENT 'clean up tracings at 01:00 daily!'
DO
DELETE LOW_PRIORITY FROM TRACING WHERE TIMESTAMP < DATE_SUB(NOW(), INTERVAL 1 DAY);


CREATE EVENT IF NOT EXISTS tracing_cleaner_event
ON SCHEDULE
EVERY 1 DAY
STARTS '2017-08-08 13:00:00' ON COMPLETION PRESERVE ENABLE 
DO 
DELETE LOW_PRIORITY FROM TRACING WHERE TIMESTAMP < DATE_SUB(NOW(), INTERVAL 30 DAY);


CREATE EVENT IF NOT EXISTS ftp_importer_cleaner_event
ON SCHEDULE
EVERY 1 DAY
STARTS '2017-09-09 00:00:00' ON COMPLETION PRESERVE ENABLE 
DO 
DELETE LOW_PRIORITY FROM FTP_IMPORTER WHERE TIME_STAMP < DATE_SUB(NOW(), INTERVAL 7 DAY);




 *
 */


@Cacheable(false)
@Entity
@Indexes ({                   
	@Index(name="TRACING_OWNER_INDEX", columnNames={"OWNER"}),
	@Index(name="TRACING_ACTION_INDEX", columnNames={"ACTION"}),
	@Index(name="TRACING_ADMINISTRATOR_INDEX", columnNames={"ADMINISTRATOR"}),
	@Index(name="TRACING_NETWORK_INDEX", columnNames={"NETWORK"}),
	@Index(name="TRACING_TIMESTAMP_INDEX", columnNames={"TIMESTAMP"}),
})
@Table(name = "TRACING" )
public class Tracing extends BaseEntity {

	public Tracing(TracingAction action, String username, String administrator, String network, String device, String description) {
		super();
		setOwner(username);
		this.timeStamp = new Date();
		this.description = description;
		this.action = action;
		this.administrator = administrator;
		this.network = network;
		this.device = device;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Tracing() {
		super();
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TIMESTAMP")
	private Date timeStamp;
	
	@Lob	
	@Column(name = "DESCRIPTION")
	private String description;

    @Enumerated(EnumType.STRING)
	@Column(name = "ACTION")
	private TracingAction action;
	
	@Column(name = "ADMINISTRATOR")
	private String administrator;
	
	@Column(name = "NETWORK")
	private String network;

	@Column(name = "DEVICE")
	private String device;
	
	
	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TracingAction getAction() {
		return action;
	}

	public void setAction(TracingAction action) {
		this.action = action;
	}

	public String getAdministrator() {
		return administrator;
	}

	public void setAdministrator(String administrator) {
		this.administrator = administrator;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}
	
}
