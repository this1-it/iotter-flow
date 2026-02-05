package it.thisone.iotter.ui.charts;

import it.thisone.iotter.ui.model.TimeInterval;
import it.thisone.iotter.ui.uitask.UIRunnable;

public class ChartDrawIntervalUIRunnable implements UIRunnable {

	private TimeInterval interval;
	private final AbstractChartAdapter adapter;

	public ChartDrawIntervalUIRunnable(AbstractChartAdapter adapter) {
		super();
		this.adapter = adapter;
	}

	// do some slow processing here, but don't use any UI components.
	// Bug #307 [Cassandra] too many connection errors
	@Override
	public void runInBackground() {
//		if (adapter.getUI() != null && adapter.getUI().getSession() != null)
//			adapter.getUI().getSession().getLockInstance().lock();
//		try {
//			adapter.createChartConfiguration(interval);
//		} finally {
//			if (adapter.getUI() != null && adapter.getUI().getSession() != null)
//				try {
//					adapter.getUI().getSession().getLockInstance().unlock();
//				} catch (Throwable e) {
//				}
//		}
	}

	// do all UI changes based on the work done previously.
	@Override
	public void runInUI(Throwable ex) {
		// Bug #224 (In Progress): [Vaadin] on changing chart interval,
		// sometime loading indicators are showing indefinitely
		adapter.endDrawing();
		adapter.draw();
	}

	public void setInterval(TimeInterval interval) {
		this.interval = interval;
	}
}