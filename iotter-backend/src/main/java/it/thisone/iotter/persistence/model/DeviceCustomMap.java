package it.thisone.iotter.persistence.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;
import org.eclipse.persistence.annotations.PrivateOwned;
import org.eclipse.persistence.annotations.TimeOfDay;

import it.thisone.iotter.persistence.ifc.IMarker;

@Cache(type = CacheType.SOFT, // Cache everything until the JVM decides memory is low.
expiryTimeOfDay=@TimeOfDay(hour=3) // 3:00 AM
//expiry = 3600000 // 60 minutes
)
@Entity
@Table(name = "DEVICE_CUSTOM_MAP") 
public class DeviceCustomMap extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DeviceCustomMap() {
		super();
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getExternalId()).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof DeviceCustomMap == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final DeviceCustomMap otherObject = (DeviceCustomMap) obj;
		return new EqualsBuilder().append(getExternalId(), otherObject.getExternalId()).isEquals();
	}


	@Column(name = "NAME")
	private String name;

	
	@Column(name = "EXTERNAL_ID")
	private String externalId;


	@Column(name = "DEFAULT_MAP")
	private boolean defaultMap;
	
	
	@PrivateOwned
	@OneToOne(orphanRemoval=true, cascade = CascadeType.ALL, fetch=FetchType.LAZY)
	private ImageData image;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "NETWORK_ID")
	private Network network;

	@PrivateOwned
	@OneToMany(orphanRemoval=true, mappedBy = "customMap", fetch = FetchType.EAGER, cascade = { CascadeType.ALL })
	private Set<DeviceWidget> widgets;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public Set<DeviceWidget> getWidgets() {
		if (widgets == null) {
			widgets = new HashSet<DeviceWidget>();
		}
		return widgets;
	}

	public void setWidgets(Set<DeviceWidget> widgets) {
		for (DeviceWidget widget : widgets) {
			widget.setCustomMap(this);
			widget.setOwner(getOwner());
		}
		this.widgets = widgets;
	}

	public void addDeviceWidgets(List<DeviceWidget> widgets) {
		for (DeviceWidget widget : widgets) {
			addDeviceWidget(widget);
		}
	}

	public void addDeviceWidget(DeviceWidget widget) {
		if (!getWidgets().contains(widget)) {
			widget.setId(null);
			widget.setCustomMap(this);
			widget.setOwner(getOwner());
			getWidgets().add(widget);
		}
	}

	public ImageData getImage() {
		return image;
	}

	public void setImage(ImageData image) {
		if (image != null) {
			image.setOwner(this.getOwner());
		}
		this.image = image;
	}

	public Network getNetwork() {
		return network;
	}

	public void setNetwork(Network network) {
		this.network = network;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public boolean isDefaultMap() {
		return defaultMap;
	}

	public void setDefaultMap(boolean defaultMap) {
		this.defaultMap = defaultMap;
	}
	
	public List<IMarker> getIMarkers() {
		List<IMarker> markers = new ArrayList<>();
		for (DeviceWidget widget : getWidgets()) {
			markers.add(widget);
		}
		return markers;
	}

}
