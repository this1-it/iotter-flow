package it.thisone.iotter.exporter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CDataPointUtil {

	private CDataPointUtil() {
	}

	public static Float[] extractValues(List<CDataPoint> dataPoints, List<String> addresses) {
		Float[] values = new Float[addresses.size()];
		if (dataPoints == null || dataPoints.isEmpty()) {
			return values;
		}
		Map<String, Float> lookup = new HashMap<>();
		for (CDataPoint dataPoint : dataPoints) {
			if (dataPoint != null) {
				lookup.put(dataPoint.getId(), dataPoint.getValue());
			}
		}
		for (int i = 0; i < addresses.size(); i++) {
			values[i] = lookup.get(addresses.get(i));
		}
		return values;
	}
}
