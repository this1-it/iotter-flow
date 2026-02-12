package it.thisone.iotter.rest.model.client;

import java.util.Date;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "ConfortOnDemandEvent", description = "Confort On Demand Event root entity")
public class ConfortOnDemandEvent {

	@ApiModelProperty(value = "user priority", required = true, allowableValues = "0,1")
	@JsonProperty(value = "priority", required = true)
	public Integer getPriority() {
		return priority;
	}

	@ApiModelProperty(value = "event type", required = true, allowableValues = "IN,OUT")
	@JsonProperty(value = "type", required = true)
	public String getType() {
		return type;
	}

	@ApiModelProperty(value = "master plant serial", required = true)
	@JsonProperty(value = "serial", required = true)
	public String getSerial() {
		return serial;
	}

	@ApiModelProperty(value = "beacon mac address", required = true)
	@JsonProperty(value = "beacon", required = true)
	public String getBeacon() {
		return beacon;
	}

	@ApiModelProperty(value = "register addresses (last part of mqtt topic path) separated by | (pipe) ", required = true)
	@JsonProperty(value = "topic", required = true)
	public String getTopic() {
		return topic;
	}

	@ApiModelProperty(value = "wanted setpoint", required = true)
	@JsonProperty(value = "set_wanted", required = true)
	public Float getSetpointWanted() {
		return setpointWanted;
	}

	@ApiModelProperty(value = "min setpoint", required = true)
	@JsonProperty(value = "set_min", required = true)
	public Float getSetpointMin() {
		return setpointMin;
	}

	@ApiModelProperty(value = "max setpoint", required = true)
	@JsonProperty(value = "set_max", required = true)
	public Float getSetpointMax() {
		return setpointMax;
	}

	@ApiModelProperty(value = "default setpoint", required = true)
	@JsonProperty(value = "set_default", required = true)
	public Float getSetpointDefault() {
		return setpointDefault;
	}

	@ApiModelProperty(value = "minimum time of presence (seconds)", required = true)
	@JsonProperty(value = "delta1", required = true)
	public Integer getDelta1() {
		return delta1;
	}

	@ApiModelProperty(value = "maximun time of presence (seconds)", required = true)
	@JsonProperty(value = "delta2", required = true)
	public Integer getDelta2() {
		return delta2;
	}

	@JsonIgnore
	@NotNull(message = "Please provide a valid priority")
	private Integer priority;
	@JsonIgnore
	@NotNull(message = "Please provide a valid type")
	private String type;
	@JsonIgnore
	@NotNull(message = "Please provide a valid serial")
	private String serial;
	@JsonIgnore
	@NotNull(message = "Please provide a valid beacon")
	private String beacon;
	@JsonIgnore
	@NotNull(message = "Please provide a valid topic")
	private String topic;
	@JsonIgnore
	@NotNull(message = "Please provide a valid setpointWanted")
	private Float setpointWanted;
	@JsonIgnore
	@NotNull(message = "Please provide a valid setpointMin")
	private Float setpointMin;
	@JsonIgnore
	@NotNull(message = "Please provide a valid setpointMax")
	private Float setpointMax;
	@JsonIgnore
	@NotNull(message = "Please provide a valid setpointDefault")
	private Float setpointDefault;
	@JsonIgnore
	@NotNull(message = "Please provide a valid delta1")
	private Integer delta1;
	@JsonIgnore
	@NotNull(message = "Please provide a valid delta2")
	private Integer delta2;

	public void setPriority(@JsonProperty("priority") Integer priority) {
		this.priority = priority;
	}

	public void setType(@JsonProperty("type") String type) {
		this.type = type;
	}

	public void setSerial(@JsonProperty("serial") String serial) {
		this.serial = serial;
	}

	public void setBeacon(@JsonProperty("beacon") String beacon) {
		this.beacon = beacon;
	}

	public void setTopic(@JsonProperty("topic") String topic) {
		this.topic = topic;
	}

	public void setSetpointWanted(@JsonProperty("set_wanted") Float setpointWanted) {
		this.setpointWanted = setpointWanted;
	}

	public void setSetpointMin(@JsonProperty("set_min") Float setpointMin) {
		this.setpointMin = setpointMin;
	}

	public void setSetpointMax(@JsonProperty("set_max") Float setpointMax) {
		this.setpointMax = setpointMax;
	}

	public void setSetpointDefault(@JsonProperty("set_default") Float setpointDefault) {
		this.setpointDefault = setpointDefault;
	}

	public void setDelta1(@JsonProperty("delta1") Integer delta1) {
		this.delta1 = delta1;
	}

	public void setDelta2(@JsonProperty("delta2") Integer delta2) {
		this.delta2 = delta2;
	}

	@JsonIgnore
	private String userid;
	@JsonIgnore
	private Date timestamp;

    @JsonProperty("userid")
    @ApiModelProperty(name="userid", value="userid should not be set",readOnly=true)	
    public String getUserid() {
		return userid;
	}

    @JsonProperty("timestamp")
    @ApiModelProperty(name="timestamp", value="timestamp should not be set",readOnly=true)
	public Date getTimestamp() {
		return timestamp;
	}

	@JsonIgnore
	public void setUserid(String userid) {
		this.userid = userid;
	}

	@JsonIgnore
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
//	@JsonIgnore
//	public boolean isPresent() {
//		long millis = this.delta1 * 1000 + this.timestamp.getTime();
//		return millis > System.currentTimeMillis();
//	}
	
	@JsonIgnore
	public boolean isAbsent() {
		long millis = this.delta2 * 1000 + this.timestamp.getTime();
		return millis < System.currentTimeMillis();
	}
	
	@JsonIgnore
	public boolean isAvailable() {
		return !this.isAbsent() && this.type.equals("IN");
	}
	
	
//	@JsonIgnore
//	public Date triggerStartTime() {
//		long seconds = this.delta1  + (this.timestamp.getTime() / 1000);
//		return new Date(seconds * 1000);
//	}
	
	
	

}
