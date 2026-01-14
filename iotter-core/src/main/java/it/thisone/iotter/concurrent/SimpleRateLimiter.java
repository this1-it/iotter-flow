package it.thisone.iotter.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.exceptions.ApplicationRuntimeException;

// @see https://dzone.com/articles/basic-api-rate-limiting
public class SimpleRateLimiter {
	private static Logger logger = LoggerFactory.getLogger(Constants.AsyncExecutor.LOG4J_CATEGORY);

	private Semaphore semaphore;
	private int maxPermits;
	private int period;
	private TimeUnit timePeriod;
	private ScheduledExecutorService scheduler;

	public static SimpleRateLimiter create(int permits, int period, TimeUnit timePeriod) {
		SimpleRateLimiter limiter = new SimpleRateLimiter(permits, period, timePeriod);
		limiter.schedulePermitReplenishment();
		return limiter;
	}

	private SimpleRateLimiter(int permits, int period, TimeUnit timePeriod) {
		this.semaphore = new Semaphore(permits);
		this.maxPermits = permits;
		this.period = period;
		this.timePeriod = timePeriod;
	}

	public void acquire() {
		try {
			if (semaphore.availablePermits() <= 0) {
				logger.debug(String.format( "SimpleRateLimiter acquiring %d", maxPermits - semaphore.availablePermits()));
			}
			
			semaphore.acquire();
		} catch (InterruptedException e) {
			throw new ApplicationRuntimeException("Interrupted RateLimiter", e);
		}
	}

	public boolean tryAcquire() {
		// System.out.println(String.format( "availablePermits %d",
		// semaphore.availablePermits()));
		return semaphore.tryAcquire();
	}

	public void stop() {
		scheduler.shutdownNow();
	}

	public void schedulePermitReplenishment() {
		scheduler = Executors.newScheduledThreadPool(1);
//        scheduler.schedule(() -> {
//            System.out.println(String.format( "release %d", maxPermits - semaphore.availablePermits()));
//            semaphore.release(maxPermits - semaphore.availablePermits());
//        }, 1, timePeriod);

		ScheduledFuture<?> promise = scheduler.scheduleAtFixedRate(() -> {
			logger.debug(String.format( "SimpleRateLimiter release %d", maxPermits - semaphore.availablePermits()));
			semaphore.release(maxPermits - semaphore.availablePermits());
		}, 0, period, timePeriod);

	}
}