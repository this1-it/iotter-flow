package it.thisone.iotter.persistence.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.persistence.annotations.PrivateOwned;

import it.thisone.iotter.enums.modbus.TemplateState;

/*
ALTER TABLE MODBUS_PROFILE ADD COLUMN STATE varchar(255) DEFAULT NULL;
update MODBUS_PROFILE set state='PUBLIC' where RESOURCE is not NULL;


List<ModbusRegister> copy1 = list.stream().filter(s -> !s.getCrucial()).collect(Collectors.toList());
List<ModbusRegister> copy2 = list.stream().filter(s -> s.getCrucial()).collect(Collectors.toList());


*/

@Cacheable(false)
@Entity
@Table(name = "MODBUS_PROFILE")
public class ModbusProfile extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "")
	private String resource;

	@Column(name = "TEMPLATE")
	private String template;

	@Column(name = "REVISION")
	private String revision;

	@Column(name = "DISPLAY_NAME")
	private String displayName;

	@Embedded
	private ModbusConfiguration configuration;

	@PrivateOwned
	@OneToMany(orphanRemoval = true, mappedBy = "profile", fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
	@OrderColumn(name = "POSITION")
	private List<ModbusRegister> registers;

	@PrivateOwned
	@OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private ResourceData data;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATION_DATE")
	private Date creationDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATE")
	private TemplateState state;

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public ModbusConfiguration getConfiguration() {
		if (configuration == null) {
			configuration = new ModbusConfiguration();
		}
		return configuration;
	}

	public void setConfiguration(ModbusConfiguration configuration) {
		this.configuration = configuration;
	}

	public List<ModbusRegister> getRegisters() {
		if (registers == null) {
			registers = new ArrayList<ModbusRegister>();
		}
		return registers;
	}

	public void setRegisters(List<ModbusRegister> registers) {
		for (ModbusRegister register : registers) {
			// Feature #1885
			// register.setId(null);
			register.setProfile(this);
			register.setOwner(getOwner());
		}
		this.registers = registers;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public void addRegister(ModbusRegister register) {
		if (!getRegisters().contains(register)) {
			register.setProfile(this);
			register.setOwner(getOwner());
			getRegisters().add(register);
		}
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public ResourceData getData() {
		return data;
	}

	public void setData(ResourceData data) {
		this.data = data;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getRevision()).append(" ");
		sb.append(getDisplayName());
		return sb.toString().trim();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getId()). //
				toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof ModbusProfile == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final ModbusProfile otherObject = (ModbusProfile) obj;
		return new EqualsBuilder(). //
				append(this.getId(), otherObject.getId()).isEquals();
	}

	@Transient
	public int countActiveRegisters() {
		int cnt = 0;
		for (ModbusRegister register : registers) {
			if (register.getActive())
				cnt++;
		}
		return cnt;
	}

	@Transient
	public double bandWidthRatio() {
		return (int) countActiveRegisters() / getConfiguration().getSampleRate();
	}

	public TemplateState getState() {
		return state;
	}

	public void setState(TemplateState state) {
		this.state = state;
	}


//	public void populateMetaData() {
//		this.setId(UUID.randomUUID().toString());
//		for (ModbusRegister register : this.getRegisters()) {
//			register.setId(UUID.randomUUID().toString());
//			String metaData = register.getMetaData();
//			int index = register.getMetaData().lastIndexOf("|");
//			if (index > 0) {
//				String pre = metaData.substring(0, index);
//				String post = metaData.substring(index + 1);
//				metaData = String.format("%s|%s|%s", pre, register.getId(), post);
//			} else {
//				metaData = String.format("%s|%s", register.getId(), metaData);
//			}
//			register.setMetaData(metaData);
//		}
//		
//	}

}
