package it.thisone.iotter.persistence.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Min;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotEmpty;

@MappedSuperclass
public abstract class BaseType extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BaseType() {
		super();
	}

	public BaseType(String name, Integer code) {
		super();
		this.name = name;
		this.code = code;
	}

	@NotEmpty
	@Column(name = "NAME")
	private String name;

	@Min(0)
	@Column(name = "CODE")
	private Integer code;
	
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getName());
		return sb.toString();
	}

    @Override
    public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getCode()).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
		if (obj instanceof BaseType == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final BaseType otherObject = (BaseType) obj;
		return new EqualsBuilder().append(getCode(), otherObject.getCode()).isEquals();
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}
	
}
