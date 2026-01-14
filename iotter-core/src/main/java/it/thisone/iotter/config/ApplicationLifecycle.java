package it.thisone.iotter.config;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * https://www.iancollington.com/graceful-shutdown-of-quartz-scheduler-and-jobs-using-spring/
 * http://www.gitshah.com/2014/04/how-to-setup-fixed-shutdown-sequence.html
 * 
 * @author bedinsky
 *
 */
@Component
public class ApplicationLifecycle implements SmartLifecycle {


	private final Logger logger = LoggerFactory.getLogger(ApplicationLifecycle.class);

	private boolean isRunning = false;

	public boolean isAutoStartup() {
		return true;
	}

	public void stop(Runnable runnable) {
		stop();
		runnable.run();
	}

	public void start() {
		isRunning = true;
	}

	public void stop() {
		isRunning = false;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public int getPhase() {
		return Integer.MAX_VALUE;
	}
}