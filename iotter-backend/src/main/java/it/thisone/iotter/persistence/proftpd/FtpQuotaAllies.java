package it.thisone.iotter.persistence.proftpd;

import java.io.Serializable;

public class FtpQuotaAllies implements Serializable {
	/**
CREATE TABLE `ftpquotatallies` (
  `name` varchar(30) NOT NULL DEFAULT '',
  `quota_type` enum('user','group','class','all') NOT NULL DEFAULT 'user',
  `bytes_in_used` int(10) unsigned NOT NULL DEFAULT '0',
  `bytes_out_used` int(10) unsigned NOT NULL DEFAULT '0',
  `bytes_xfer_used` int(10) unsigned NOT NULL DEFAULT '0',
  `files_in_used` int(10) unsigned NOT NULL DEFAULT '0',
  `files_out_used` int(10) unsigned NOT NULL DEFAULT '0',
  `files_xfer_used` int(10) unsigned NOT NULL DEFAULT '0'
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
	 */
	private static final long serialVersionUID = 1L;

	private String name;
	private String quota_type = "user";
	private int bytes_in_used;
	private int bytes_out_used;
	private int bytes_xfer_used;
	private int files_in_used;
	private int files_out_used;
	private int files_xfer_used;
	
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
	public int getBytes_in_used() {
		return bytes_in_used;
	}
	public void setBytes_in_used(int bytes_in_used) {
		this.bytes_in_used = bytes_in_used;
	}
	public int getBytes_out_used() {
		return bytes_out_used;
	}
	public void setBytes_out_used(int bytes_out_used) {
		this.bytes_out_used = bytes_out_used;
	}
	public int getBytes_xfer_used() {
		return bytes_xfer_used;
	}
	public void setBytes_xfer_used(int bytes_xfer_used) {
		this.bytes_xfer_used = bytes_xfer_used;
	}
	public int getFiles_in_used() {
		return files_in_used;
	}
	public void setFiles_in_used(int files_in_used) {
		this.files_in_used = files_in_used;
	}
	public int getFiles_out_used() {
		return files_out_used;
	}
	public void setFiles_out_used(int files_out_used) {
		this.files_out_used = files_out_used;
	}
	public int getFiles_xfer_used() {
		return files_xfer_used;
	}
	public void setFiles_xfer_used(int files_xfer_used) {
		this.files_xfer_used = files_xfer_used;
	}
	
}
