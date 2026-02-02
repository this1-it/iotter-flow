package it.thisone.iotter.persistence.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;
import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Indexes;
import javax.validation.constraints.NotEmpty;

import it.thisone.iotter.enums.NetworkGroupType;

@Cacheable(false)
//@Cache(type = CacheType.SOFT)
@Entity
@Indexes ({
	@Index(name="NETWORK_GROUP_OWNER_INDEX", columnNames={"OWNER"}),
	@Index(name="NETWORK_GROUP_NAME_INDEX", columnNames={"NAME","OWNER" })
})
@Table(name = "NETWORK_GROUP")
public class NetworkGroup extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    public NetworkGroup() {
		super();
	}

    @Override
    public String toString() {
        return String.format("%s [%s, %s]", name, externalId, groupType);
    }

    @Override
    public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getName()).append(getId()).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
		if (obj instanceof NetworkGroup == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final NetworkGroup otherObject = (NetworkGroup) obj;
		return new EqualsBuilder().append(this.getName(), otherObject.getName()).append(this.getNetwork().getName(), otherObject.getNetwork().getName()).isEquals();
    }
    
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "NETWORK_ID")
    private Network network;
	
	@NotEmpty
	@Column(name = "NAME")
	private String name;
	
	@Column(name = "DESCRIPTION")
	private String description;
	
	@Column(name = "DEFAULT_GROUP")
	private boolean defaultGroup = false;
	
	// feature #287 (In Progress): Eliminazione gruppi
	@Column(name = "EXCLUSIVE")
	private boolean exclusive = false;

	// feature #287 (In Progress): Eliminazione gruppi
	@Column(name = "EXTERNAL_ID")
	private String externalId;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "GROUP_TYPE")
	private NetworkGroupType groupType;

	public Network getNetwork() {
		return network;
	}

	public void setNetwork(Network network) {
		setOwner(network.getOwner());
		this.network = network;
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

	public boolean isDefaultGroup() {
		return defaultGroup;
	}

	public void setDefaultGroup(boolean defaultGroup) {
		this.defaultGroup = defaultGroup;
	}

	public boolean isExclusive() {
		return exclusive;
	}

	public void setExclusive(boolean exclusive) {
		this.exclusive = exclusive;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public NetworkGroupType getGroupType() {
		return groupType;
	}

	public void setGroupType(NetworkGroupType groupType) {
		this.groupType = groupType;
	}
	
	
}
