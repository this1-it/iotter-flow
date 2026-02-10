package it.thisone.iotter.ui.designer;

public interface IPlaceHolder {
	// TODO(flow-migration): Flow components are classes; refactor this to a component base class
	// or expose a Component accessor so containers can add/remove placeholders safely.
	public String getIdentifier();
	
	public int getX();
	public int getY();

	public int getPixelWidth();
	public int getPixelHeight();

	public void setX(int value);
	public void setY(int value);

	public void setPixelWidth(int value);
	public void setPixelHeight(int value);
	
	public void addListener(PlaceHolderRemovedListener listener);
	public void addListener(PlaceHolderChangedListener listener);
	public void addListener(PlaceHolderSavedListener listener);
	
	
}
