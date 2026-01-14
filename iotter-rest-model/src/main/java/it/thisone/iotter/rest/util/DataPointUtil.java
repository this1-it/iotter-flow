package it.thisone.iotter.rest.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.thisone.iotter.rest.model.DataPoint;

public class DataPointUtil {

	/**
	 * Extracts an array of Float values from the given list of DataPoint objects,
	 * ordered according to the order of IDs in the provided list, using streams.
	 *
	 * @param dataPoints list of DataPoint objects
	 * @param ids        list of String identifiers
	 * @return an array of Float values corresponding to each id in the specified
	 *         order. If an id is not found, the corresponding value in the array
	 *         will be null.
	 */
	public static Float[] extractValues(List<DataPoint> dataPoints, List<String> ids) {

//		Map<String, Float> valueMap = dataPoints.stream().collect(Collectors.toMap(
//				it.thisone.iotter.rest.model.DataPoint::getId, it.thisone.iotter.rest.model.DataPoint::getValue));
		
		
// Keep the last value (overwrites previous on duplicate)
		
//		Map<String, Float> valueMap = dataPoints.stream().collect(
//			    Collectors.toMap(
//			        it.thisone.iotter.rest.model.DataPoint::getId,
//			        it.thisone.iotter.rest.model.DataPoint::getValue,
//			        (existing, replacement) -> replacement
//			    )
//			);
		
		
		// Keep the first value (ignore later duplicates)
		Map<String, Float> valueMap = dataPoints.stream().collect(
			    Collectors.toMap(
			        it.thisone.iotter.rest.model.DataPoint::getId,
			        it.thisone.iotter.rest.model.DataPoint::getValue,
			        (existing, replacement) -> existing
			    )
			);


		return ids.stream().map(valueMap::get).toArray(Float[]::new);
	}
	
	
	public static Object[] extractIdAndValueArrays(String slaveId, List<DataPoint> dataPoints) {
	    // Extract id array with slaveId prefix
	    String[] idArray = dataPoints.stream()
	      .map(dp -> slaveId + ":" + dp.getId())
	      .toArray(String[]::new);
	    
	    // Extract value array cast as Float
	    Float[] valueArray = dataPoints.stream()
	      .map(dp -> dp.getValue().floatValue())
	      .toArray(Float[]::new);
	    
	    return new Object[] { idArray, valueArray };
	}

	
	
}