package it.thisone.iotter.persistence.ifc;

public interface IMarker {

	public String getMarkerId();

	public String getLabel();

	public float getY();
	public float getX();
	
	public void setY(float y);
	public void setX(float x);

}
