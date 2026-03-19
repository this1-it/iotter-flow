package it.thisone.iotter.persistence.model;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;
import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Indexes;
import jakarta.validation.constraints.NotEmpty;

@Cacheable(false)
//@Cache(type = CacheType.SOFT)
@Entity
@Indexes ({
	@Index(name="ROLE_OWNER_INDEX", columnNames={"OWNER"})
})
@Table(name = "ROLE", uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME", "OWNER"}) })
public class Role extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Role() {
		super();
	}

	public Role(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}

	@NotEmpty
	@Column(name = "NAME")
	private String name;
	
	@Column(name = "DESCRIPTION")
	private String description;

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
        return obj != null && obj instanceof Role && name.equals(((Role) obj).getName());
    }
	
	
}
