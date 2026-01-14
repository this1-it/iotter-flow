package it.thisone.iotter.rest.model.billings;

import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RenewPlant {
	@JsonIgnore
	private List<RenewDevice> plant;
	
	
	@JsonProperty("plant")
	public List<RenewDevice> getPlant() {
		return plant;
	}
	
	public void setPlant(@JsonProperty("plant") List<RenewDevice> data) {
		this.plant = data;
	}
}
