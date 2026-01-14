package it.thisone.iotter.persistence.model;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;


import org.eclipse.persistence.annotations.Index;

/**
 * @author Engeenious
 * 
 */

@Cacheable(true)
@Entity
@Index(name="MEASURE_UNIT_INDEX", columnNames={"NAME"})
@Table(name = "MEASURE_UNIT_TYPE", uniqueConstraints = { @UniqueConstraint(columnNames = { "CODE" }) })
public class MeasureUnitType extends BaseType {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MeasureUnitType() {
		super();
	}

	public MeasureUnitType(String value, Integer code) {
		super(value, code);
	}

	@Override
	public String toString() {
		return String.format("%d#%s", getCode(),getName());
	}

}
