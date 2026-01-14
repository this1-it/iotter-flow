package it.thisone.iotter.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ModbusConfiguration implements Serializable {
	
	/**
	 * "^[a-zA-Z0-9_]+$"
	 */
	private static final long serialVersionUID = -775665680850693799L;

	public ModbusConfiguration() {
		super();
		protocol = "SERIAL";
		speed = "19200";
		dataBits = "8";
		parity = "N";
		stopBits = "2";
		sampleRate = 10;
		host = "";
		port = "";
	}

	@Column(name = "SLAVE_NAME")
	private String slaveName;
	
	@Column(name = "SLAVE_ID")
	private Integer slaveID;
	
	@Column(name = "SAMPLE_RATE")
	private Integer sampleRate;
	
	@Column(name = "PROTOCOL")
	private String protocol;
	
	@Column(name = "SPEED")
	private String speed;

	@Column(name = "DATA_BITS")
	private String dataBits;
	
	@Column(name = "PARITY")
	private String parity;
	
	@Column(name = "STOP_BITS")
	private String stopBits;

	@Column(name = "HOST")
	private String host;

	@Column(name = "PORT")
	private String port;

	@Column(name = "LOCAL")
	private boolean local;
	
	public String getSlaveName() {
		return slaveName;
	}

	public void setSlaveName(String slaveName) {
		this.slaveName = slaveName;
	}

	public Integer getSlaveID() {
		return slaveID;
	}

	public void setSlaveID(Integer slaveID) {
		this.slaveID = slaveID;
	}

	public Integer getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(Integer sampleRate) {
		this.sampleRate = sampleRate;
	}

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public String getDataBits() {
		return dataBits;
	}

	public void setDataBits(String dataBits) {
		this.dataBits = dataBits;
	}

	public String getParity() {
		return parity;
	}

	public void setParity(String parity) {
		this.parity = parity;
	}

	public String getStopBits() {
		return stopBits;
	}

	public void setStopBits(String stopBits) {
		this.stopBits = stopBits;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public boolean getLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

}
