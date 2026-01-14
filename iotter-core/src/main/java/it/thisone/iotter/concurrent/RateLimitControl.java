package it.thisone.iotter.concurrent;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import it.thisone.iotter.config.Constants;

@Component
public class RateLimitControl {
	//private static Logger logger = LoggerFactory.getLogger(RateLimitControl.class);
	private static Logger logger = LoggerFactory.getLogger(Constants.AsyncExecutor.LOG4J_CATEGORY);

//	private static final int maxRequests = 12;
//	private static final TimeUnit rateType = TimeUnit.MINUTES;
	private Map<String, Optional<SimpleRateLimiter>> limiters;

	public Optional<SimpleRateLimiter> getRateLimiter(String clientId, int requests, int period, TimeUnit timePeriod) {
		if (limiters.containsKey(clientId)) {
			return limiters.get(clientId);
		} else {
			synchronized (clientId.intern()) {
				// double-checked locking to avoid multiple-reinitializations
				if (limiters.containsKey(clientId)) {
					return limiters.get(clientId);
				}
				Optional<SimpleRateLimiter> rateLimiter = Optional.of(SimpleRateLimiter.create(requests, period, timePeriod));
				limiters.put(clientId, rateLimiter);
				return rateLimiter;
			}
		}
	}

//	public Optional<SimpleRateLimiter> createDefaultRateLimiter() {
//		return Optional.of(SimpleRateLimiter.create(maxRequests, rateType));
//	}
	
	@PostConstruct
	public void init() {
		logger.info("RateLimitControl initialized");
		limiters = new ConcurrentHashMap<>();
	}
	
	@PreDestroy
	public void stop() {
		for (Optional<SimpleRateLimiter> limiter : limiters.values()) {
			limiter.get().stop();
		}
	}
	


}
