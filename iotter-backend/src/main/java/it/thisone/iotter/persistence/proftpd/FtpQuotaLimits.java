package it.thisone.iotter.persistence.proftpd;

import java.io.Serializable;

public class FtpQuotaLimits implements Serializable {

	/**
CREATE TABLE `ftpquotalimits` (
  `name` varchar(30) DEFAULT NULL,
  `quota_type` enum('user','group','class','all') NOT NULL DEFAULT 'user',
  `per_session` enum('false','true') NOT NULL DEFAULT 'false',
  `limit_type` enum('soft','hard') NOT NULL DEFAULT 'soft',
  `bytes_in_avail` int(10) unsigned NOT NULL DEFAULT '0',
  `bytes_out_avail` int(10) unsigned NOT NULL DEFAULT '0',
  `bytes_xfer_avail` int(10) unsigned NOT NULL DEFAULT '0',
  `files_in_avail` int(10) unsigned NOT NULL DEFAULT '0',
  `files_out_avail` int(10) unsigned NOT NULL DEFAULT '0',
  `files_xfer_avail` int(10) unsigned NOT NULL DEFAULT '0'
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String quota_type = "user";
	private String per_session = "true";
	private String limit_type = "hard";
	private int bytes_in_avail;
	private int bytes_out_avail;
	private int bytes_xfer_avail;
	private int files_in_avail;
	private int files_out_avail;
	private int files_xfer_avail;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getQuota_type() {
		return quota_type;
	}
	public void setQuota_type(String quota_type) {
		this.quota_type = quota_type;
	}
	public String getPer_session() {
		return per_session;
	}
	public void setPer_session(String per_session) {
		this.per_session = per_session;
	}
	public String getLimit_type() {
		return limit_type;
	}
	public void setLimit_type(String limit_type) {
		this.limit_type = limit_type;
	}
	public int getBytes_in_avail() {
		return bytes_in_avail;
	}
	public void setBytes_in_avail(int bytes_in_avail) {
		this.bytes_in_avail = bytes_in_avail;
	}
	public int getBytes_out_avail() {
		return bytes_out_avail;
	}
	public void setBytes_out_avail(int bytes_out_avail) {
		this.bytes_out_avail = bytes_out_avail;
	}
	public int getBytes_xfer_avail() {
		return bytes_xfer_avail;
	}
	public void setBytes_xfer_avail(int bytes_xfer_avail) {
		this.bytes_xfer_avail = bytes_xfer_avail;
	}
	public int getFiles_in_avail() {
		return files_in_avail;
	}
	public void setFiles_in_avail(int files_in_avail) {
		this.files_in_avail = files_in_avail;
	}
	public int getFiles_out_avail() {
		return files_out_avail;
	}
	public void setFiles_out_avail(int files_out_avail) {
		this.files_out_avail = files_out_avail;
	}
	public int getFiles_xfer_avail() {
		return files_xfer_avail;
	}
	public void setFiles_xfer_avail(int files_xfer_avail) {
		this.files_xfer_avail = files_xfer_avail;
	}

}
