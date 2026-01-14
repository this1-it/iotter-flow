package it.thisone.iotter.ui.common;


import it.thisone.iotter.ui.uitask.UIRunnable;

public class WidgetRefreshUIRunnable implements UIRunnable {
	
	//private final AbstractWidgetVisualizer widget;
	
	private OnRefreshCallback callback;
	private boolean redraw = false;
	
    public static interface OnRefreshCallback {
    	boolean refresh();
    	void draw();
    }
	
	public WidgetRefreshUIRunnable(AbstractWidgetVisualizer widget) {
		super();
		
		callback = new OnRefreshCallback() {
			@Override
			public boolean refresh() {
				return widget.refresh();
			}
			@Override
			public void draw() {
				widget.draw();
			}
		};
		
	}

	// do some slow processing here, but don't use any UI components.
	@Override
	public void runInBackground() {
		redraw = callback.refresh();
	}
	
	// do all UI changes based on the work done previously.
	@Override
	public void runInUI(Throwable ex) {
		if (redraw) {
			callback.draw();
		}
	}
}