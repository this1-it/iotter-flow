package it.thisone.iotter.ui.designer;

import com.vaadin.flow.component.page.BrowserWindowResizeListener;



public interface IParkingPlace {
	
	public boolean containPlaceHolder(IPlaceHolder component);
	public void addPlaceHolder(IPlaceHolder component);
	public void removePlaceHolder(IPlaceHolder component);
	public void changePlaceHolder(IPlaceHolder placeHolder);
	public BrowserWindowResizeListener createBrowserWindowResizeListener();
	public ToggleMenuListener createToggleMenuListener();


	public int getScrollLeft();


	public void setScrollLeft(int scrollLeft);

	public int getScrollTop() ;


	public void setScrollTop(int scrollTop) ;


}
