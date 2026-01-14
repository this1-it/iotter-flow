package it.thisone.iotter.ui.common.fields;

import java.util.Arrays;

import it.thisone.iotter.persistence.model.Channel;

public class ChannelAcceptor {
	public static final int SPEED_MEASURE_UNIT = 17;
	public static final int DEGREE_MEASURE_UNIT = 34;
	public static final int BOOLEAN_MEASURE_UNIT = 68;

	
	public static final Integer[] DIRECTION_SENSORS = new Integer[] { 63, 68,
			42, 52 };

	
	public boolean accept(Channel channel) {
		return channel.isAvailable();
	}
	
	
	public static boolean isSpeed(Channel channel) {
		if (channel.getDefaultMeasure().getType().equals(SPEED_MEASURE_UNIT)) {
			return true;
		}
		return false;
	}

	public static boolean isBoolean(Channel channel) {
		return channel.getDefaultMeasure().getType().equals(BOOLEAN_MEASURE_UNIT);
	}

	
	public static boolean isDirection(Channel channel) {
		if (channel.getDefaultMeasure().getType().equals(DEGREE_MEASURE_UNIT)) {
			return true;
		}
		/*
		 * Feature #231 Gestione dell'attributo "code sensor" nei parametri
		 * dello strumento.
		 */
		if (Arrays.asList(DIRECTION_SENSORS).contains(
				channel.getConfiguration().getSensor())) {
			return true;
		}
		return false;
	}
	
}
