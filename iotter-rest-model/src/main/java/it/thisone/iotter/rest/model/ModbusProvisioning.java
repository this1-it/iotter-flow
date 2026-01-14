package it.thisone.iotter.rest.model;

import java.util.ArrayList;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="ModbusProvisioning", description="modbus slave configuration, all lists must have same size")
public class ModbusProvisioning {
	
	@JsonProperty("profileid")
    @ApiModelProperty(value="internal id",readOnly=true)
	public String getProfileId() {
		return profileId;
	}
	
	@JsonProperty("sn")
    @ApiModelProperty(value="virtual device id",required=true)
	public String getDeviceId() {
		return deviceId;
	}
	
	@JsonProperty("slavename")
    @ApiModelProperty(value="slave name must be alphanumeric, only undescore is accepted, unique",required=true)
	public String getSlaveName() {
		return slaveName;
	}
	
	@JsonProperty("slaveid")
    @ApiModelProperty(value="slave id, unique", allowableValues="range[0,50000]",required=true)
	public Integer getSlaveId() {
		return slaveId;
	}

	@JsonProperty("samplerate")
    @ApiModelProperty(value="sample rate in seconds",allowableValues="range[10,40]",required=true)
	public Integer getSampleRate() {
		return sampleRate;
	}
	
	@JsonProperty("protocol")
    @ApiModelProperty(value="slave protocol",allowableValues="SERIAL, ETHERNET, MODBUSTCP", required=true)
	public String getProtocol() {
		return protocol;
	}
	
	@JsonProperty("sp_speed")
    @ApiModelProperty(value="serial port speed",allowableValues="4800,9600,19200,38400,57600,115200")
	public String getSerialPortSpeed() {
		return serialPortSpeed;
	}

	@JsonProperty("sp_databits")
    @ApiModelProperty(value="serial port data bits",allowableValues="5,6,7,8")
	public String getSerialPortDataBits() {
		return serialPortDataBits;
	}

	@JsonProperty("sp_parity")
    @ApiModelProperty(value="serial port parity",allowableValues="N, O, E, M, S")
	public String getSerialPortParity() {
		return serialPortParity;
	}

	@JsonProperty("sp_stopbits")
    @ApiModelProperty(value="serial port stop bits",allowableValues="1, 1.5, 2")
	public String getSerialPortStopBits() {
		return serialPortStopBits;
	}

	
	@JsonProperty("eth_host")
    @ApiModelProperty(value="host address")
	public String getHost() {
		return host;
	}

	@JsonProperty("eth_port")
    @ApiModelProperty(value="host port")
	public String getPort() {
		return port;
	}

	@JsonProperty("oid")
    @ApiModelProperty(value="registry oid list",readOnly=true)
	public List<String> getOid() {
		return oid;
	}
	
	@JsonProperty("active")
    @ApiModelProperty(value="list of activation flags")
	public List<Boolean> getActive() {
		return active;
	}


	@JsonProperty("address")
    @ApiModelProperty(value="list of addresses")
	public List<Integer> getAddress() {
		return address;
	}
	
	@JsonProperty("label")
    @ApiModelProperty(value="list of labels")
	public List<String> getLabel() {
		return label;
	}

	@JsonProperty("typevar")
    @ApiModelProperty(value="list of variable types", allowableValues="AL, AN, D, I")
	public List<String> getTypeVar() {
		return typeVar;
	}

	@JsonProperty("typeread")
    @ApiModelProperty(value="list of read types", allowableValues="D, C, I, H")
	public List<String> getTypeRead() {
		return typeRead;
	}
	
	@JsonProperty("format")
    @ApiModelProperty(value="list of bit sizes", allowableValues="8, 16, 32, F")
	public List<String> getFormat() {
		return format;
	}
	
	@JsonProperty("signed")
    @ApiModelProperty(value="list of signs", allowableValues="Y, N")
	public List<String> getSigned() {
		return signed;
	}
	
	@JsonProperty("permission")
    @ApiModelProperty(value="list of permissions", allowableValues="R, W, RW")
	public List<String> getPermission() {
		return permission;
	}
	
	@JsonProperty("functioncode")
    @ApiModelProperty(value="list of single/multiple reads", allowableValues="S, M")
	public List<String> getFunctionCode() {
		return functionCode;
	}
	
	@JsonProperty("unit")
    @ApiModelProperty(value="list of measurement units bacnet")
	public List<Integer> getUnit() {
		return unit;
	}
	
	@JsonProperty("scalemultiplier")
    @ApiModelProperty(value="list of measurement scale")
	public List<Double> getScaleMultiplier() {
		return scaleMultiplier;
	}
	
	@JsonProperty("offset")
    @ApiModelProperty(value="list of measurement offset")
	public List<Double> getOffset() {
		return offset;
	}
	
	@JsonProperty("decimaldigits")
    @ApiModelProperty(value="list of measurement decimal digits")
	public List<Integer> getDecimalDigits() {
		return decimalDigits;
	}
	
	@JsonProperty("deltalogging")
    @ApiModelProperty(value="list of delta logging")
	public List<Double> getDeltaLogging() {
		return deltaLogging;
	}
	
	@JsonProperty("min")
    @ApiModelProperty(value="list of measurement min")
	public List<Double> getMin() {
		return min;
	}
	
	@JsonProperty("max")
    @ApiModelProperty(value="list of measurement max")
	public List<Double> getMax() {
		return max;
	}
	
	@JsonProperty("priority")
    @ApiModelProperty(value="list of alarm priority")
	public List<String> getPriority() {
		return priority;
	}
	
	@JsonProperty("qualifier")
    @ApiModelProperty(value="list of qualifiers", allowableValues="AVG,MIN,MAX,TOT,STD")
	public List<String> getQualifier() {
		return qualifier;
	}
	
	@JsonProperty("bitmask")
    @ApiModelProperty(value="list of bit masks")
	public List<String> getBitmask() {
		return bitmask;
	}

	
	@JsonIgnore
	private String profileId;
	@JsonIgnore
	private String deviceId;
	@JsonIgnore
	private String slaveName;
	@JsonIgnore
	private Integer slaveId;
	@JsonIgnore
	private Integer sampleRate;
	@JsonIgnore
	private String protocol;
	@JsonIgnore
	private String host;
	@JsonIgnore
	private String port;
	@JsonIgnore
	private String serialPortSpeed;
	@JsonIgnore
	private String serialPortDataBits;
	@JsonIgnore
	private String serialPortParity;
	@JsonIgnore
	private String serialPortStopBits;
	@JsonIgnore
	private List<Boolean> active = new ArrayList<Boolean>();
	@JsonIgnore
	private List<Integer> address = new ArrayList<Integer>();
	@JsonIgnore
	private List<String> label = new ArrayList<String>();
	@JsonIgnore
	private List<String> typeVar = new ArrayList<String>();
	@JsonIgnore
	private List<String> typeRead = new ArrayList<String>();
	@JsonIgnore
	private List<String> format = new ArrayList<String>();
	@JsonIgnore
	private List<String> signed = new ArrayList<String>();
	@JsonIgnore
	private List<String> permission = new ArrayList<String>();
	@JsonIgnore
	private List<String> functionCode = new ArrayList<String>();
	@JsonIgnore
	private List<Integer> unit = new ArrayList<Integer>();
	@JsonIgnore
	private List<Double> scaleMultiplier = new ArrayList<Double>();
	@JsonIgnore
	private List<Double> offset = new ArrayList<Double>();
	@JsonIgnore
	private List<Integer> decimalDigits = new ArrayList<Integer>();
	@JsonIgnore
	private List<Double> deltaLogging = new ArrayList<Double>();
	@JsonIgnore
	private List<Double> min = new ArrayList<Double>();
	@JsonIgnore
	private List<Double> max = new ArrayList<Double>();
	@JsonIgnore
	private List<String> priority = new ArrayList<String>();
	@JsonIgnore
	private List<String> oid = new ArrayList<String>();
	@JsonIgnore
	private List<String> bitmask = new ArrayList<String>();
	@JsonIgnore
	private List<String> qualifier = new ArrayList<String>();
	
	/*
	 * 
	 */
	
	@JsonIgnore
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	@JsonIgnore
	public void setSlaveName(String slaveName) {
		this.slaveName = slaveName;
	}
	@JsonIgnore
	public void setSlaveId(Integer slaveID) {
		this.slaveId = slaveID;
	}
	@JsonIgnore
	public void setSampleRate(Integer sampleRate) {
		this.sampleRate = sampleRate;
	}
	@JsonIgnore
	public void setSerialPortSpeed(String serialPortSpeed) {
		this.serialPortSpeed = serialPortSpeed;
	}
	@JsonIgnore
	public void setSerialPortDataBits(String serialPortDataBits) {
		this.serialPortDataBits = serialPortDataBits;
	}
	@JsonIgnore
	public void setSerialPortParity(String serialPortParity) {
		this.serialPortParity = serialPortParity;
	}
	@JsonIgnore
	public void setSerialPortStopBits(String serialStopBits) {
		this.serialPortStopBits = serialStopBits;
	}
	
	@JsonIgnore
	public void setActive(List<Boolean> active) {
		this.active = active;
	}

	@JsonIgnore
	public void setAddress(List<Integer> address) {
		this.address = address;
	}
	@JsonIgnore
	public void setLabel(List<String> label) {
		this.label = label;
	}
	@JsonIgnore
	public void setTypeVar(List<String> typeVar) {
		this.typeVar = typeVar;
	}
	@JsonIgnore
	public void setTypeRead(List<String> typeRead) {
		this.typeRead = typeRead;
	}
	@JsonIgnore
	public void setFormat(List<String> format) {
		this.format = format;
	}
	@JsonIgnore
	public void setSigned(List<String> signed) {
		this.signed = signed;
	}
	@JsonIgnore
	public void setPermission(List<String> permission) {
		this.permission = permission;
	}
	@JsonIgnore
	public void setFunctionCode(List<String> functionCode) {
		this.functionCode = functionCode;
	}
	@JsonIgnore
	public void setUnit(List<Integer> unit) {
		this.unit = unit;
	}
	@JsonIgnore
	public void setScaleMultiplier(List<Double> scaleMultiplier) {
		this.scaleMultiplier = scaleMultiplier;
	}
	@JsonIgnore
	public void setOffset(List<Double> offset) {
		this.offset = offset;
	}
	@JsonIgnore
	public void setDecimalDigits(List<Integer> decimalDigits) {
		this.decimalDigits = decimalDigits;
	}
	@JsonIgnore
	public void setDeltaLogging(List<Double> deltaLogging) {
		this.deltaLogging = deltaLogging;
	}
	@JsonIgnore
	public void setMin(List<Double> min) {
		this.min = min;
	}
	@JsonIgnore
	public void setMax(List<Double> max) {
		this.max = max;
	}
	@JsonIgnore
	public void setPriority(List<String> priority) {
		this.priority = priority;
	}
	
	@JsonIgnore
	public void setProfileId(String uid) {
		this.profileId = uid;
	}
	@JsonIgnore
	public void setOid(List<String> externalId) {
		this.oid = externalId;
	}

	@JsonIgnore
	public void setHost(String ethHost) {
		this.host = ethHost;
	}

	@JsonIgnore
	public void setPort(String ethPort) {
		this.port = ethPort;
	}

	@JsonIgnore
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@JsonIgnore
	public void setBitmask(List<String> bitmask) {
		this.bitmask = bitmask;
	}
	
	@JsonIgnore
	public void setQualifier(List<String> qualifier) {
		this.qualifier = qualifier;
	}

}