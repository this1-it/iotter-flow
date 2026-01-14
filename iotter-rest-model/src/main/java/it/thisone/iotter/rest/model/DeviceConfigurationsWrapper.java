package it.thisone.iotter.rest.model;

import java.util.ArrayList;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value="DeviceConfigurationsWrapper",description="DeviceConfigurationsWrapper")
public class DeviceConfigurationsWrapper {
    public DeviceConfigurationsWrapper() {
        this.configurations = new ArrayList<DeviceConfigurationSet>();
    }
    
    @JsonProperty
    public List<DeviceConfigurationSet> getConfigurations() {
        return configurations;
    }

    @JsonIgnore
    public void setConfigurations(List<DeviceConfigurationSet> configurations) {
        this.configurations = configurations;
    }

    @JsonIgnore
    private List<DeviceConfigurationSet> configurations;

}