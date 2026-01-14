package it.thisone.iotter.cassandra.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
@Deprecated
public class Provisioning {

	private Integer revision;
	private String slaveName;
	private Integer slaveID;
	private Integer sampleRate;
	private String serialPort;
	private List<Integer> address = new ArrayList<Integer>();
	private List<String> labelIT = new ArrayList<String>();
	private List<String> typeVar = new ArrayList<String>();
	private List<String> typeRead = new ArrayList<String>();
	private List<String> format = new ArrayList<String>();
	private List<String> signed = new ArrayList<String>();
	private List<String> permission = new ArrayList<String>();
	private List<String> functionCodeForWrite = new ArrayList<String>();
	private List<String> uoM = new ArrayList<String>();
	private List<Double> scaleMultiplier = new ArrayList<Double>();
	private List<String> offset = new ArrayList<String>();
	private List<Integer> decimalDigits = new ArrayList<Integer>();
	private List<Double> deltaLoggingOnlyForTypeReadHolding = new ArrayList<Double>();
	private List<Object> minimumForWrite = new ArrayList<Object>();
	private List<Object> maximumForWrite = new ArrayList<Object>();
	private List<String> priorityOnlyForTypeVarAlarm = new ArrayList<String>();
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 * 
	 * @return The revision
	 */
	public Integer getRevision() {
		return revision;
	}

	/**
	 * 
	 * @param revision
	 *            The Revision
	 */
	public void setRevision(Integer revision) {
		this.revision = revision;
	}

	/**
	 * 
	 * @return The slaveName
	 */
	public String getSlaveName() {
		return slaveName;
	}

	/**
	 * 
	 * @param slaveName
	 *            The Slave Name
	 */
	public void setSlaveName(String slaveName) {
		this.slaveName = slaveName;
	}

	/**
	 * 
	 * @return The slaveID
	 */
	public Integer getSlaveID() {
		return slaveID;
	}

	/**
	 * 
	 * @param slaveID
	 *            The Slave ID
	 */
	public void setSlaveID(Integer slaveID) {
		this.slaveID = slaveID;
	}

	/**
	 * 
	 * @return The sampleRate
	 */
	public Integer getSampleRate() {
		return sampleRate;
	}

	/**
	 * 
	 * @param sampleRate
	 *            The Sample Rate
	 */
	public void setSampleRate(Integer sampleRate) {
		this.sampleRate = sampleRate;
	}

	/**
	 * 
	 * @return The serialPort
	 */
	public String getSerialPort() {
		return serialPort;
	}

	/**
	 * 
	 * @param serialPort
	 *            The Serial Port
	 */
	public void setSerialPort(String serialPort) {
		this.serialPort = serialPort;
	}

	/**
	 * 
	 * @return The address
	 */
	public List<Integer> getAddress() {
		return address;
	}

	/**
	 * 
	 * @param address
	 *            The Address
	 */
	public void setAddress(List<Integer> address) {
		this.address = address;
	}

	/**
	 * 
	 * @return The labelIT
	 */
	public List<String> getLabelIT() {
		return labelIT;
	}

	/**
	 * 
	 * @param labelIT
	 *            The Label IT
	 */
	public void setLabelIT(List<String> labelIT) {
		this.labelIT = labelIT;
	}

	/**
	 * 
	 * @return The typeVar
	 */
	public List<String> getTypeVar() {
		return typeVar;
	}

	/**
	 * 
	 * @param typeVar
	 *            The Type Var
	 */
	public void setTypeVar(List<String> typeVar) {
		this.typeVar = typeVar;
	}

	/**
	 * 
	 * @return The typeRead
	 */
	public List<String> getTypeRead() {
		return typeRead;
	}

	/**
	 * 
	 * @param typeRead
	 *            The Type Read
	 */
	public void setTypeRead(List<String> typeRead) {
		this.typeRead = typeRead;
	}

	/**
	 * 
	 * @return The format
	 */
	public List<String> getFormat() {
		return format;
	}

	/**
	 * 
	 * @param format
	 *            The Format
	 */
	public void setFormat(List<String> format) {
		this.format = format;
	}

	/**
	 * 
	 * @return The signed
	 */
	public List<String> getSigned() {
		return signed;
	}

	/**
	 * 
	 * @param signed
	 *            The Signed
	 */
	public void setSigned(List<String> signed) {
		this.signed = signed;
	}

	/**
	 * 
	 * @return The permission
	 */
	public List<String> getPermission() {
		return permission;
	}

	/**
	 * 
	 * @param permission
	 *            The Permission
	 */
	public void setPermission(List<String> permission) {
		this.permission = permission;
	}

	/**
	 * 
	 * @return The functionCodeForWrite
	 */
	public List<String> getFunctionCodeForWrite() {
		return functionCodeForWrite;
	}

	/**
	 * 
	 * @param functionCodeForWrite
	 *            The Function Code(for Write)
	 */
	public void setFunctionCodeForWrite(List<String> functionCodeForWrite) {
		this.functionCodeForWrite = functionCodeForWrite;
	}

	/**
	 * 
	 * @return The uoM
	 */
	public List<String> getUoM() {
		return uoM;
	}

	/**
	 * 
	 * @param uoM
	 *            The UoM
	 */
	public void setUoM(List<String> uoM) {
		this.uoM = uoM;
	}

	/**
	 * 
	 * @return The scaleMultiplier
	 */
	public List<Double> getScaleMultiplier() {
		return scaleMultiplier;
	}

	/**
	 * 
	 * @param scaleMultiplier
	 *            The Scale (Multiplier)
	 */
	public void setScaleMultiplier(List<Double> scaleMultiplier) {
		this.scaleMultiplier = scaleMultiplier;
	}

	/**
	 * 
	 * @return The offset
	 */
	public List<String> getOffset() {
		return offset;
	}

	/**
	 * 
	 * @param offset
	 *            The Offset
	 */
	public void setOffset(List<String> offset) {
		this.offset = offset;
	}

	/**
	 * 
	 * @return The decimalDigits
	 */
	public List<Integer> getDecimalDigits() {
		return decimalDigits;
	}

	/**
	 * 
	 * @param decimalDigits
	 *            The Decimal digits
	 */
	public void setDecimalDigits(List<Integer> decimalDigits) {
		this.decimalDigits = decimalDigits;
	}

	/**
	 * 
	 * @return The deltaLoggingOnlyForTypeReadHolding
	 */
	public List<Double> getDeltaLoggingOnlyForTypeReadHolding() {
		return deltaLoggingOnlyForTypeReadHolding;
	}

	/**
	 * 
	 * @param deltaLoggingOnlyForTypeReadHolding
	 *            The Delta Logging (Only for Type Read Holding)
	 */
	public void setDeltaLoggingOnlyForTypeReadHolding(List<Double> deltaLoggingOnlyForTypeReadHolding) {
		this.deltaLoggingOnlyForTypeReadHolding = deltaLoggingOnlyForTypeReadHolding;
	}

	/**
	 * 
	 * @return The minimumForWrite
	 */
	public List<Object> getMinimumForWrite() {
		return minimumForWrite;
	}

	/**
	 * 
	 * @param minimumForWrite
	 *            The Minimum(for Write)
	 */
	public void setMinimumForWrite(List<Object> minimumForWrite) {
		this.minimumForWrite = minimumForWrite;
	}

	/**
	 * 
	 * @return The maximumForWrite
	 */
	public List<Object> getMaximumForWrite() {
		return maximumForWrite;
	}

	/**
	 * 
	 * @param maximumForWrite
	 *            The Maximum(for Write)
	 */
	public void setMaximumForWrite(List<Object> maximumForWrite) {
		this.maximumForWrite = maximumForWrite;
	}

	/**
	 * 
	 * @return The priorityOnlyForTypeVarAlarm
	 */
	public List<String> getPriorityOnlyForTypeVarAlarm() {
		return priorityOnlyForTypeVarAlarm;
	}

	/**
	 * 
	 * @param priorityOnlyForTypeVarAlarm
	 *            The Priority(Only for Type Var Alarm)
	 */
	public void setPriorityOnlyForTypeVarAlarm(List<String> priorityOnlyForTypeVarAlarm) {
		this.priorityOnlyForTypeVarAlarm = priorityOnlyForTypeVarAlarm;
	}

	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}