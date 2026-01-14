package it.thisone.iotter.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.thisone.iotter.persistence.model.GeoLocation;

public class GeoLocationUtil {

	// declare public constants

	/**
	 * The minimum allowed latitude
	 */
	public static Double MIN_LATITUDE = Double.valueOf("-90.0000");

	/**
	 * The maximum allowed latitude
	 */
	public static Double MAX_LATITUDE = Double.valueOf("90.0000");

	/**
	 * The minimum allowed longitude
	 */
	public static Double MIN_LONGITUDE = Double.valueOf("-180.0000");

	/**
	 * The maximum allowed longitude
	 */
	public static Double MAX_LONGITUDE = Double.valueOf("180.0000");

	/**
	 * The diameter of the Earth used in calculations
	 */
	public static Double EARTH_DIAMETER = Double.valueOf("12756.274");

	/**
	 * A method to validate a latitude value
	 * 
	 * @param latitude
	 *            the latitude to check is valid
	 * 
	 * @return true if, and only if, the latitude is within the MIN and MAX
	 *         latitude
	 */
	public static boolean isValidLatitude(double latitude) {
		if (latitude >= MIN_LATITUDE && latitude <= MAX_LATITUDE) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * A method to validate a longitude value
	 * 
	 * @param longitude
	 *            the longitude to check is valid
	 * 
	 * @return true if, and only if, the longitude is between the MIN and MAX
	 *         longitude
	 */
	public static boolean isValidLongitude(double longitude) {
		if (longitude >= MIN_LONGITUDE && longitude <= MAX_LONGITUDE) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * A private method to calculate the latitude constant
	 * 
	 * @return a double representing the latitude constant
	 */
	public static double latitudeConstant() {
		return EARTH_DIAMETER * (Math.PI / 360f );
	}

	/**
	 * A private method to calculate the longitude constant
	 * 
	 * @param latitude
	 *            a latitude coordinate in decimal notation
	 * 
	 * @return a double representing the longitude constant
	 */
	public static double longitudeConstant(double latitude) {
		return EARTH_DIAMETER * Math.PI * Math.abs(Math.cos(Math.abs(latitude))) / Double.valueOf("360");
	}

	/**
	 * A method to add distance in a northerly direction to a coordinate
	 * 
	 * @param latitude
	 *            a latitude coordinate in decimal notation
	 * @param longitude
	 *            a longitude coordinate in decimal notation
	 * @param distance
	 *            the distance to add in meters
	 * 
	 * @return the new coordinate
	 */
	public static GeoLocation addDistanceNorth(double latitude, double longitude, int distance) {
		// check on the parameters
		if (isValidLatitude(latitude) == false || isValidLongitude(longitude) == false || distance <= 0) {
			throw new IllegalArgumentException("All parameters are required and must be valid");
		}
		// convert the distance from meters to kilometers
		double kilometers = distance / new Double(1000);
		// calculate the new latitude
		double newLat = latitude + (kilometers / latitudeConstant());
		return new GeoLocation(new Double(newLat).doubleValue(), longitude);

	}

	/**
	 * A method to add distance in a southerly direction to a coordinate
	 * 
	 * @param latitude
	 *            a latitude coordinate in decimal notation
	 * @param longitude
	 *            a longitude coordinate in decimal notation
	 * @param distance
	 *            the distance to add in meters
	 * 
	 * @return the new coordinate
	 */
	public static GeoLocation addDistanceSouth(double latitude, double longitude, int distance) {

		// check on the parameters
		if (isValidLatitude(latitude) == false || isValidLongitude(longitude) == false || distance <= 0) {
			throw new IllegalArgumentException("All parameters are required and must be valid");
		}

		// convert the distance from meters to kilometers
		double kilometers = distance / new Double(1000);

		// calculate the new latitude
		double newLat = latitude - (kilometers / latitudeConstant());

		return new GeoLocation(new Double(newLat).doubleValue(), longitude);

	}

	/**
	 * A method to add distance in an easterly direction to a coordinate
	 * 
	 * @param latitude
	 *            a latitude coordinate in decimal notation
	 * @param longitude
	 *            a longitude coordinate in decimal notation
	 * @param distance
	 *            the distance to add in meters
	 * 
	 * @return the new coordinate
	 */
	public static GeoLocation addDistanceEast(double latitude, double longitude, int distance) {
		// check on the parameters
		if (isValidLatitude(latitude) == false || isValidLongitude(longitude) == false || distance <= 0) {
			throw new IllegalArgumentException("All parameters are required and must be valid");
		}
		// convert the distance from meters to kilometers
		double kilometers = distance / new Double(1000);

		// calculate the new longitude
		double newLng = longitude + (kilometers / longitudeConstant(latitude));

		return new GeoLocation(latitude, new Double(newLng).doubleValue());
	}

	/**
	 * A method to add distance in an westerly direction to a coordinate
	 * 
	 * @param latitude
	 *            a latitude coordinate in decimal notation
	 * @param longitude
	 *            a longitude coordinate in decimal notation
	 * @param distance
	 *            the distance to add in meters
	 * 
	 * @return the new coordinate
	 */
	public static GeoLocation addDistanceWest(double latitude, double longitude, int distance) {

		// check on the parameters
		if (isValidLatitude(latitude) == false || isValidLongitude(longitude) == false || distance <= 0) {
			throw new IllegalArgumentException("All parameters are required and must be valid");
		}

		// convert the distance from meters to kilometers
		double kilometers = distance / new Double(1000);

		// calculate the new longitude
		double newLng = longitude - (kilometers / longitudeConstant(latitude));

		return new GeoLocation(latitude, new Double(newLng).doubleValue());
	}

	/**
	 * A method to build four coordinates representing a bounding box given a
	 * start coordinate and a distance
	 * 
	 * @param latitude
	 *            a latitude coordinate in decimal notation
	 * @param longitude
	 *            a longitude coordinate in decimal notation
	 * @param distance
	 *            the distance to add in meters
	 * 
	 * @return a hashMap representing the bounding box (NE,SE,SW,NW)
	 */
	public static Map<String, GeoLocation> getBoundingBox(double latitude, double longitude, int distance) {

		// check on the parameters
		if (isValidLatitude(latitude) == false || isValidLongitude(longitude) == false || distance <= 0) {
			throw new IllegalArgumentException("All parameters are required and must be valid");
		}

		// declare helper variables
		Map<String, GeoLocation> boundingBox = new HashMap<String, GeoLocation>();

		// calculate the coordinates
		GeoLocation north = addDistanceNorth(latitude, longitude, distance);
		GeoLocation south = addDistanceSouth(latitude, longitude, distance);
		GeoLocation east = addDistanceEast(latitude, longitude, distance);
		GeoLocation west = addDistanceWest(latitude, longitude, distance);

		// build the bounding box object
		boundingBox.put("NE", new GeoLocation(north.getLatitude(), east.getLongitude()));
		boundingBox.put("SE", new GeoLocation(south.getLatitude(), east.getLongitude()));
		boundingBox.put("SW", new GeoLocation(south.getLatitude(), west.getLongitude()));
		boundingBox.put("NW", new GeoLocation(north.getLatitude(), west.getLongitude()));

		// return the bounding box object
		return boundingBox;
	}

	/**
	 * Get a center latitude/longitude from an list of like locations
	 * 
	 * Convert each latitude,longitude pair into a unit-length 3D vector.
	 * Sum each of those vectors
     * Normalize the resulting vector
     * Convert back to spherical coordinates
	 * 
	 * @param locations
	 * @return
	 */
	public static GeoLocation getCenterPoint(List<GeoLocation> locations)
	{
	    int total = locations.size();

	    double X = 0;
	    double Y = 0;
	    double Z = 0;

	    for (GeoLocation location : locations) {
	        
	    	if (!location.isUndefined()) {
		    	double lat = location.getLatitude() * Math.PI / 180;
		        double lon = location.getLongitude() * Math.PI / 180;

		        double x = Math.cos(lat) * Math.cos(lon);
		        double y = Math.cos(lat) * Math.sin(lon);
		        double z = Math.sin(lat);

		        X += x;
		        Y += y;
		        Z += z;
		     }


	    }

	    X = X / total;
	    Y = Y / total;
	    Z = Z / total;

	    double Lon = Math.atan2(Y, X);
	    double Hyp = Math.sqrt(X * X + Y * Y);
	    double Lat = Math.atan2(Z, Hyp);

	    return new GeoLocation((double)(Lat * 180 / Math.PI), (double) (Lon * 180 / Math.PI));
	}
	
	/**
	 * This is the implementation Haversine Distance Algorithm between two places
	 * @author ananth
	 *  R = earth’s radius (mean radius = 6,371km)
	    Δlat = lat2− lat1
	    Δlong = long2− long1
	    a = sin²(Δlat/2) + cos(lat1).cos(lat2).sin²(Δlong/2)
	    c = 2.atan2(√a, √(1−a))
	    d = R.c
	 *
	 */
	
	public static double getHaversineDistance(GeoLocation loc1, GeoLocation loc2) {
        double lat1 = loc1.getLatitude();
        double lon1 = loc1.getLongitude();
        double lat2 = loc2.getLatitude();
        double lon2 = loc2.getLongitude();
            
		double latDistance = toRad(lat2-lat1);
        double lonDistance = toRad(lon2-lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + 
                   Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * 
                   Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = (EARTH_DIAMETER/2) * c;
		
		return distance * 1000;
	}
	
    private static Double toRad(double value) {
        return value * Math.PI / 180;
    }
	
}