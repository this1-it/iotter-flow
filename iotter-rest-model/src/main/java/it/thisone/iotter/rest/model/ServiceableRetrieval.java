package it.thisone.iotter.rest.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="ServiceableRetrieval",description="it describes a chunk of entries from query")
public class ServiceableRetrieval implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3201667060333825425L;

	@JsonProperty("next")
    @ApiModelProperty(value="index of next batch, -1 if last batch ",readOnly=true)
	public Long getNext() {
		return next;
	}

	
    @JsonProperty("expires")
    @ApiModelProperty(value="query expiry date in seconds",readOnly=true)
	public long getExpires() {
		return expires;
	}

	@JsonProperty("qid")
    @ApiModelProperty(value="query id",readOnly=true)
	public String getQid() {
		return qid;
	}

	@JsonProperty("total")
    @ApiModelProperty(value="total number of rows found within query",readOnly=true)
	public Long getTotal() {
		return total;
	}

	@JsonProperty("start")
    @ApiModelProperty(value="index of batch ", readOnly=true)
	public Long getStart() {
		return start;
	}

	
	@JsonProperty("batch_size")
    @ApiModelProperty(value="current batch size",readOnly=true)
	public Integer getBatchSize() {
		return batchSize;
	}
	
	@JsonIgnore
	private String qid;
	
	@JsonIgnore
	private Long total;
	
	@JsonIgnore
	private Long start;

	@JsonIgnore
	private Long next;
		
	@JsonIgnore
	private Integer batchSize;

	@JsonIgnore
	private long expires;

	@JsonIgnore
	public void setQid(String qid) {
		this.qid = qid;
	}

	@JsonIgnore
	public void setTotal(Long total) {
		this.total = total;
	}

	@JsonIgnore
	public void setStart(Long start) {
		this.start = start;
	}

	@JsonIgnore
	public void setBatchSize(Integer size) {
		this.batchSize = size;
	}

	@JsonIgnore
	public void setExpires(long expires) {
		this.expires = expires;
	}

	@JsonIgnore
	public void setNext(Long next) {
		this.next = next;
	}	


	
}
