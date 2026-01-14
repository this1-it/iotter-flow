package it.thisone.iotter.persistence.model;

import java.io.Serializable;
import java.text.DecimalFormat;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * A class to represent a latitude and longitude
 */



@Embeddable
public class GeoLocation implements Comparable<GeoLocation>, Serializable {

	/**
	 * ALTER TABLE DEVICE MODIFY COLUMN latitude DOUBLE NULL;
	 * ALTER TABLE DEVICE MODIFY COLUMN longitude DOUBLE NULL;
	 * ALTER TABLE DEVICE MODIFY COLUMN elevation DOUBLE NULL;
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "ADDRESS")
	private String address;
	@Column(name = "LATITUDE")
	private double latitude;
	@Column(name = "LONGITUDE")
	private double longitude;
	@Column(name = "ELEVATION")
	private double elevation;

	public GeoLocation() {
		super();
	}
	
	/**
	 * Constructor for this class
	 * 
	 * @param latitude
	 *            a latitude coordinate in decimal notation
	 * @param longitude
	 *            a longitude coordinate in decimal notation
	 */
	
	public GeoLocation(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}


	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getElevation() {
		return elevation;
	}

	public void setElevation(double elevation) {
		this.elevation = elevation;
	}

	@Override
	public String toString() {
		return format(latitude) + ", " + format(longitude);
	}

	public boolean isUndefined() {
		return (latitude == 0f && longitude == 0f);
	}
	
	private String format(double value) {
		DecimalFormat format = new DecimalFormat("##.#######");
		return format.format(value);
	}

	/**
	 * A method to determine if one event is the same as another
	 * 
	 * @param o
	 *            the object to compare this one to
	 * 
	 * @return true if they are equal, false if they are not
	 */
	@Override
	public boolean equals(Object o) {
		// check to make sure the object is an event
		if ((o instanceof GeoLocation) == false) {
			// o is not an event object
			return false;
		}

		// compare these two events
		GeoLocation c = (GeoLocation) o;
		// build items for comparison
		String me = this.toString();
		String you = c.toString();

		return me.equals(you);

	} // end equals method

	/**
	 * Overide the default hashcode method
	 * 
	 * @return a hashcode for this object
	 */
	@Override
	public int hashCode() {
		String me = this.toString();
		return 31 * me.hashCode();
	}

	/**
	 * The compareTo method compares the receiving object with the specified
	 * object and returns a negative integer, 0, or a positive integer depending
	 * on whether the receiving object is less than, equal to, or greater than
	 * the specified object.
	 * 
	 * @param c
	 *            the event to compare this one to
	 * 
	 * @return an integer indicating comparison result
	 */
	@Override
	public int compareTo(GeoLocation c) {
		String me = format(this.latitude) + format(this.longitude);
		String you = format(c.getLatitude()) + format(c.getLongitude());
		Double meDbl = Double.valueOf(me);
		Double youDbl = Double.valueOf(you);
		if (meDbl == youDbl) {
			return 0;
		} else {
			Double tmp = Math.floor(meDbl - youDbl);
			return tmp.intValue();
		}

	} // end compareTo method


}