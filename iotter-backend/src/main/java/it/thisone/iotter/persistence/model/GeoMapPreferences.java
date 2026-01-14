
package it.thisone.iotter.persistence.model;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class GeoMapPreferences implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2285162432129360206L;
	
	@Column(name = "MAP_TYPE")
	private String type;
	
	@Column(name = "MAP_ZOOM")
	private Integer zoom;
	
	@Column(name = "MAP_LAT")
	private Double latitude;
	
	@Column(name = "MAP_LON")
	private Double longitude;
	
	public GeoMapPreferences() {
		super();
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getZoom() {
		return zoom;
	}

	public void setZoom(Integer zoom) {
		this.zoom = zoom;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}


	
}
