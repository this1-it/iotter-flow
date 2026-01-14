package it.thisone.iotter.ui.visualizers;

import java.util.Timer;
import java.util.TimerTask;

public class WidgetRefresher extends Timer {

	public WidgetRefresher() {
		super();
	}

/*
 * if the problem is that the anonymous inner class holds a reference to the outer class, 
 * then simply use a static named inner class - this will hold no reference. 
 */
    public static interface OnRefreshCallback {
    	void refresh();
    }
	
	public void scheduleRepeating(OnRefreshCallback callback, long millis ) {
		scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				callback.refresh();
			}
		}, millis, millis);
	}


}