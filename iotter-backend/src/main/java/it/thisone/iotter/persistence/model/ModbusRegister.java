package it.thisone.iotter.persistence.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Indexes;
import org.eclipse.persistence.annotations.PrivateOwned;

import it.thisone.iotter.enums.Priority;
import it.thisone.iotter.enums.modbus.Format;
import it.thisone.iotter.enums.modbus.FunctionCode;
import it.thisone.iotter.enums.modbus.Permission;
import it.thisone.iotter.enums.modbus.Qualifier;
import it.thisone.iotter.enums.modbus.Signed;
import it.thisone.iotter.enums.modbus.TypeRead;
import it.thisone.iotter.enums.modbus.TypeVar;

@Cacheable(false)
@Entity
@Table(name = "MODBUS_REGISTER")
@Indexes({ @Index(name = "MODBUS_REGISTER_COMPATIBLE_INDEX", columnNames = { "DISPLAYNAME", "ADDRESS", "TYPEREAD" }) })
public class ModbusRegister extends BaseEntity {

	public static final String CRUCIAL_PROP = "CRUCIAL";
	public static final String WIDGET_PROP = "widget";
	public static final String ICONSET_PROP = "iconset";

	public ModbusRegister() {
		super();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "CRUCIAL")
	private Boolean crucial;
	
	@Column(name = "DISPLAYNAME")
	private String displayName;

	@Column(name = "ACTIVE")
	private Boolean active;

	@Column(name = "ADDRESS")
	private Integer address;

	@Column(name = "MEASUREUNIT")
	private Integer measureUnit;

	@Column(name = "SCALEMULTIPLIER")
	private Double scaleMultiplier;

	@Column(name = "OFFSET")
	private Double offset;

	@Column(name = "DECIMALDIGITS")
	private Integer decimalDigits;

	@Column(name = "DELTALOGGING")
	private Double deltaLogging;

	@Column(name = "MIN")
	private Double min;

	@Column(name = "MAX")
	private Double max;

	@Enumerated(EnumType.STRING)
	@Column(name = "TYPEVAR")
	private TypeVar typeVar;

	@Enumerated(EnumType.STRING)
	@Column(name = "TYPEREAD")
	private TypeRead typeRead;

	@Enumerated(EnumType.STRING)
	@Column(name = "FORMAT")
	private Format format;

	@Enumerated(EnumType.STRING)
	@Column(name = "SIGNED")
	private Signed signed;

	@Enumerated(EnumType.STRING)
	@Column(name = "PERMISSION")
	private Permission permission;

	@Enumerated(EnumType.STRING)
	@Column(name = "FUNCTIONCODE")
	private FunctionCode functionCode;

	@Enumerated(EnumType.STRING)
	@Column(name = "PRIORITY")
	private Priority priority;

	@Enumerated(EnumType.STRING)
	@Column(name = "QUALIFIER")
	private Qualifier qualifier;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MODBUS_PROFILE_ID")
	private ModbusProfile profile;

	@Column(name = "META_DATA")
	private String metaData;

	@Column(name = "BIT_MASK")
	private String bitmask;

	@PrivateOwned
	@OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
	private Set<MessageBundle> messages;

	@ElementCollection(fetch = FetchType.LAZY)
	@MapKeyColumn(name = "NAME")
	@Column(name = "VALUE")
	@CollectionTable(name = "MODBUS_REGISTER_PROPS", joinColumns = @JoinColumn(name = "REGISTER_ID"))
	private Map<String, String> additionalProperties = new HashMap<String, String>();

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getAddress()). //
				append(getTypeRead()). //
				toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof ModbusRegister == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final ModbusRegister otherObject = (ModbusRegister) obj;
		return new EqualsBuilder(). //
				append(this.getBitmask(), otherObject.getBitmask()).append(this.getAddress(), otherObject.getAddress())
				.append(this.getTypeRead(), otherObject.getTypeRead()).isEquals();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getAddress()).append(":");
		sb.append(getTypeRead()).append(":");
		sb.append(" ").append(getDisplayName());
		return sb.toString().trim();
	}

	public String getDisplayName() {
		return displayName;
	}

	public Boolean getActive() {
		return active;
	}

	public Integer getAddress() {
		return address;
	}

	public Integer getMeasureUnit() {
		return measureUnit;
	}

	public Double getScaleMultiplier() {
		return scaleMultiplier;
	}

	public Double getOffset() {
		return offset;
	}

	public Integer getDecimalDigits() {
		return decimalDigits;
	}

	public Double getDeltaLogging() {
		return deltaLogging;
	}

	public Double getMin() {
		return min;
	}

	public Double getMax() {
		return max;
	}

	public TypeVar getTypeVar() {
		return typeVar;
	}

	public TypeRead getTypeRead() {
		return typeRead;
	}

	public Format getFormat() {
		return format;
	}

	public Signed getSigned() {
		return signed;
	}

	public Permission getPermission() {
		return permission;
	}

	public FunctionCode getFunctionCode() {
		return functionCode;
	}

	public Priority getPriority() {
		return priority;
	}

	public ModbusProfile getProfile() {
		return profile;
	}

	public String getMetaData() {
		return metaData;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public void setAddress(Integer address) {
		this.address = address;
	}

	public void setMeasureUnit(Integer measureUnit) {
		this.measureUnit = measureUnit;
	}

	public void setScaleMultiplier(Double scaleMultiplier) {
		this.scaleMultiplier = scaleMultiplier;
	}

	public void setOffset(Double offset) {
		this.offset = offset;
	}

	public void setDecimalDigits(Integer decimalDigits) {
		this.decimalDigits = decimalDigits;
	}

	public void setDeltaLogging(Double deltaLogging) {
		this.deltaLogging = deltaLogging;
	}

	public void setMin(Double min) {
		this.min = min;
	}

	public void setMax(Double max) {
		this.max = max;
	}

	public void setTypeVar(TypeVar typeVar) {
		this.typeVar = typeVar;
	}

	public void setTypeRead(TypeRead typeRead) {
		this.typeRead = typeRead;
	}

	public void setFormat(Format format) {
		this.format = format;
	}

	public void setSigned(Signed signed) {
		this.signed = signed;
	}

	public void setPermission(Permission permission) {
		this.permission = permission;
	}

	public void setFunctionCode(FunctionCode functionCode) {
		this.functionCode = functionCode;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public void setProfile(ModbusProfile profile) {
		this.profile = profile;
	}

	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}

	public Set<MessageBundle> getMessages() {
		if (messages == null) {
			messages = new HashSet<MessageBundle>();
		}
		return messages;
	}

	public void setMessages(Set<MessageBundle> messages) {
		this.messages = messages;
	}

	public Map<String, String> getAdditionalProperties() {
		if (additionalProperties == null) {
			additionalProperties = new HashMap<>();
		}
		return additionalProperties;
	}

	public void setAdditionalProperties(Map<String, String> additionalProperties) {
		this.additionalProperties = additionalProperties;
	}

	public String getBitmask() {
		return bitmask;
	}

	public void setBitmask(String bitmask) {
		this.bitmask = bitmask;
	}

	public static String buildMetadata(int cnt, ModbusRegister register) {
		if (register.isNew()) {
			register.setId(UUID.randomUUID().toString());
		}
		String metaData = String.format("%04d|%d|%s|%s|%s|%s", //
				cnt, //
				register.getAddress(), //
				register.getTypeRead(), //
				register.getTypeVar(), //
				register.getBitmask(), //
				register.getId() //
		);
		return metaData;
	}
	
	public Qualifier getQualifier() {
		return qualifier;
	}

	public void setQualifier(Qualifier qualifier) {
		this.qualifier = qualifier;
	}
	
	public static ModbusRegister parseMetadata(String metaData) {
		ModbusRegister register = new ModbusRegister();
		register.setCrucial(false);
		try {
			int addressIdx = 1;
			int typeReadIdx = 2;
			int typeVarIdx = 3;
			int bitMaskIdx = 4;
			int idIdx = 5;
			String[] parts = metaData.split("\\|");
			if (parts == null)
				return null;
			if (parts.length < 6)
				return null;
			String address = parts[addressIdx];
			register.setAddress(Integer.parseInt(address));
			String typeRead = parts[typeReadIdx];
			for (TypeRead literal : TypeRead.values()) {
				if (literal.getDisplayName().equalsIgnoreCase(typeRead)) {
					register.setTypeRead(literal);
					break;
				}
			}
			String typeVar = parts[typeVarIdx];
			for (TypeVar literal : TypeVar.values()) {
				if (literal.getDisplayName().equalsIgnoreCase(typeVar)) {
					register.setTypeVar(literal);
					break;
				}
			}
			String bitmask = parts[bitMaskIdx];
			if (!bitmask.isEmpty()) {
				register.setBitmask(bitmask);
			}
			String id = parts[idIdx];
			register.setId(id);
		} catch (Throwable t) {
			register = null;
		}
		return register;
	}
	
	@Transient
	public String getMetaIdentifier() {
		if (this.metaData != null) {
			int i = StringUtils.ordinalIndexOf(this.metaData,"|", 3 );
			if (i > 0) {
				return this.metaData.substring(0, i);
			}
		}
		return null;
	}
	
	@Transient
	public boolean isAvailable() {
		return this.getActive() && !this.getCrucial();
	}
	

	public Boolean getCrucial() {
		if (crucial == null) return false;
		return crucial;
	}

	public void setCrucial(Boolean crucial) {
		this.crucial = crucial;
	}

    public static Predicate<ModbusRegister> IS_EQUAL(ModbusRegister s) {
        return t -> t.getAddress().equals(s.getAddress()) && t.getTypeRead().equals(s.getTypeRead());
    }

}
