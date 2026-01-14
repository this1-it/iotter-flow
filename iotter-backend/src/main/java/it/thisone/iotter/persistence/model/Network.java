package it.thisone.iotter.persistence.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;
import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Indexes;
import org.eclipse.persistence.annotations.PrivateOwned;
import org.hibernate.validator.constraints.NotEmpty;

import it.thisone.iotter.enums.NetworkType;

@Cacheable(false)
//@Cache(type = CacheType.SOFT)
@Entity
@Indexes ({
	@Index(name="NETWORK_OWNER_INDEX", columnNames={"OWNER"}),
	@Index(name="NETWORK_NAME_INDEX", columnNames={"NAME"})
})
@Table(name = "NETWORK", uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME", "OWNER"} ) })
public class Network extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Network() {
		super();
		this.concurrentUsers = 10;
	}

	public Network(String name, String description) {
		super();
		this.name = name;                  
		this.description = description;
		this.concurrentUsers = 10;
	}

	@NotEmpty
	@Column(name = "NAME")
	private String name;
	
	@Column(name = "DESCRIPTION")
	private String description;
	
	@Column(name = "CONCURRENT_USERS")
	private Integer concurrentUsers;

	@Column(name = "DEFAULT_NETWORK")
	private boolean defaultNetwork = false;
	
	@Lob	
	@Column(name = "HEADER")
	@Basic(optional = false, fetch = FetchType.LAZY)
	private String header;

	@Lob	
	@Column(name = "FOOTER")
	@Basic(optional = false, fetch = FetchType.LAZY)
	private String footer;

	@PrivateOwned
	@OneToOne(orphanRemoval=true, cascade = CascadeType.ALL, fetch=FetchType.LAZY)
	private ImageData logo;
	
	@Column(name = "ANONYMOUS")
	private boolean anonymous;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "NETWORK_TYPE")
	private NetworkType networkType;
	
    @OneToMany(orphanRemoval=true, mappedBy="network", fetch = FetchType.LAZY, cascade = {CascadeType.ALL} )
    private Set<NetworkGroup> groups;

    
	@PrivateOwned
	@OneToMany(orphanRemoval=true, mappedBy = "network", fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
	@OrderColumn(name = "POSITION")
	private List<DeviceCustomMap> customMaps;
   
 
	@Column(name = "TIME_ZONE")
	private String timeZone;
	

	// Feature #364 new rules of insertion/activation for devices
	// serial of AP device which create network
	@Column(name = "EXTERNAL_ID")
	private String externalId;

	@Embedded
	private GeoMapPreferences mapPreferences;
	
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj != null && obj instanceof Network && name.equals(((Network) obj).getName());
    }
    
    
    public NetworkGroup getDefaultGroup() {
    	for (NetworkGroup group : getGroups()) {
			if (group.isDefaultGroup()) {
				return group;
			}
		}
    	return null;
    }
    
    public void addGroup(NetworkGroup group) {
    	if (group == null) return;
        if (!getGroups().contains(group)) {
        	getGroups().add(group);
        	group.setNetwork(this);
        }
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getConcurrentUsers() {
		if (concurrentUsers == null) concurrentUsers = 10;
		return concurrentUsers;
	}

	public void setConcurrentUsers(Integer concurrentUsers) {
		this.concurrentUsers = concurrentUsers;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getFooter() {
		return footer;
	}

	public void setFooter(String footer) {
		this.footer = footer;
	}

	public Set<NetworkGroup> getGroups() {
		if (groups == null) {
			groups = new HashSet<NetworkGroup>();
		}
		return groups;
	}

	public void setGroups(Set<NetworkGroup> groups) {
		this.groups = groups;
	}



	public boolean isDefaultNetwork() {
		return defaultNetwork;
	}

	public void setDefaultNetwork(boolean defaultNetwork) {
		this.defaultNetwork = defaultNetwork;
	}

	public boolean isAnonymous() {
		return anonymous;
	}

	public void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}

	public ImageData getLogo() {
		return logo;
	}

	public void setLogo(ImageData image) {
		if (image != null) {
			image.setOwner(this.getOwner());
		}
		this.logo = image;
	}

	public List<DeviceCustomMap> getCustomMaps() {
		if (customMaps == null) {
			customMaps = new ArrayList<DeviceCustomMap>();
		}
		return customMaps;
	}

	public void setCustomMaps(List<DeviceCustomMap> groupMaps) {
		this.customMaps = groupMaps;
	}
	
    public void addCustomMap(DeviceCustomMap map) {
        if (!getCustomMaps().contains(map)) {
        	getCustomMaps().add(map);
        	map.setNetwork(this);
        }
    }

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public NetworkType getNetworkType() {
		if (networkType == null) {
			networkType = NetworkType.GEOGRAPHIC;
		}
		return networkType;
	}

	public void setNetworkType(NetworkType type) {
		this.networkType = type;
	}
	
	public boolean isGeoLocated() {
		return getNetworkType().equals(NetworkType.GEOGRAPHIC);
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public GeoMapPreferences getMapPreferences() {
		return mapPreferences;
	}

	public void setMapPreferences(GeoMapPreferences mapPreferences) {
		this.mapPreferences = mapPreferences;
	}


}


