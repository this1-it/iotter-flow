package it.thisone.iotter.persistence.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.eclipse.persistence.annotations.Index;


@Entity
@Index(name="MEASURE_SENSOR_INDEX", columnNames={"NAME"})
@Table(name = "MEASURE_SENSOR_TYPE", uniqueConstraints = { @UniqueConstraint(columnNames = { "CODE" }) })
public class MeasureSensorType extends BaseType {
	private static final long serialVersionUID = 1L;

	public MeasureSensorType() {
		super();
	}

	public MeasureSensorType(String name, Integer code) {
		super(name,code);
	}
}
